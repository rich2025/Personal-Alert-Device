#include <Wire.h>
#include <ArduinoBLE.h>
#include <PDM.h>
#include <personalalertdevice_inferencing.h>
#include "LSM6DS3.h"
#include "MAX30105.h"
#include "heartRate.h"

// Button Setup
#define BUTTON_PIN D2
volatile bool buttonPressed = false;
volatile unsigned long lastButtonPress = 0;
const unsigned long DEBOUNCE_DELAY = 200; // ms

const int batteryPin = A0;
const int greenLED = D1;
const int yellowLED = D3;
const int redLED = D10;
const int buzzerPin = D9;

const float adcResolution = 1023.0;
const float referenceVoltage = 3.3;
const float voltageDivider = 2.0;

bool batteryLEDsOn = false;
bool emergencyDetected = false;
unsigned long emergencyStartTime = 0;
const unsigned long EMERGENCY_DELAY = 10000; // 10 seconds

unsigned long lastLEDBlinkTime = 0;
const unsigned long LED_BLINK_INTERVAL = 500;
unsigned long lastBatteryUpdate = 0;
const unsigned long BATTERY_UPDATE_INTERVAL = 10000; // 10 seconds

unsigned long buttonPressStartTime = 0;
bool buttonHeldDown = false;
const unsigned long BUTTON_HOLD_DURATION = 5000; // 5 seconds
bool buttonReleased = false;
bool buttonReleasedAfterHold = false;

// Emergency source flags - only one should be true at a time
bool speechEmergency = false;
bool manualEmergency = false;
bool fallEmergency = false;

// MAX30102 Sensor
MAX30105 particleSensor;
const byte RATE_SIZE = 4;
byte rates[RATE_SIZE];
byte rateSpot = 0;
long lastBeat = 0;
float beatsPerMinute;
int beatAvg = 0;
int irValue = 0;
float spO2 = 0.0;
bool simulatingVitals = false;
unsigned long lastSimulationUpdate = 0;
const int SIMULATION_INTERVAL = 1000;

// Thermistor Variables
#define THERMISTOR_PIN A0
#define SERIES_RESISTOR 10000
#define NOMINAL_RESISTANCE 10000
#define NOMINAL_TEMPERATURE 25.0
#define B_COEFFICIENT 3950
#define ADC_MAX 1024
#define VCC 3.3

// BLE Services and Characteristics
BLEService healthService("19B10000-E8F2-537E-4F6C-D104768A1214");
BLEStringCharacteristic healthCharacteristic("19B10001-E8F2-537E-4F6C-D104768A1214", BLERead | BLENotify, 100);

BLEService speechService("12345678-1234-1234-1234-1234567890ab");
BLEStringCharacteristic sensorCharacteristic("Speech", BLERead | BLENotify, 100);

BLEService connectionService("19B20000-E8F2-537E-4F6C-D104768A1214");
BLEStringCharacteristic connectionCharacteristic("19B20001-E8F2-537E-4F6C-D104768A1214", BLERead | BLENotify, 20);

BLEService batteryService("19B30000-E8F2-537E-4F6C-D104768A1214");
BLEStringCharacteristic batteryVoltageCharacteristic("19B30001-E8F2-537E-4F6C-D104768A1214", BLERead | BLENotify, 10);

BLEService fallDetectionService("19B40000-E8F2-537E-4F6C-D104768A1214");
BLEStringCharacteristic fallDetectionCharacteristic("19B40001-E8F2-537E-4F6C-D104768A1214", BLERead | BLENotify, 100);

BLEService manualEmergencyService("19B50000-E8F2-537E-4F6C-D104768A1214");
BLEStringCharacteristic manualEmergencyCharacteristic("19B50001-E8F2-537E-4F6C-D104768A1214", BLERead | BLENotify, 100);

// Timing Variables
unsigned long lastStatusPrint = 0;
unsigned long lastConnectionUpdate = 0;
unsigned long lastSecondTime = 0;
unsigned long lastClassificationPrint = 0;
const unsigned long CLASSIFICATION_PRINT_INTERVAL = 1000; // 1 second

// Audio buffers
typedef struct {
    int16_t *buffer;
    volatile uint8_t buf_ready;
    uint32_t buf_count;
    uint32_t n_samples;
} inference_t;

static inference_t inference;
static signed short sampleBuffer[2048];
static bool debug_nn = false;

LSM6DS3 myspeech(I2C_MODE, 0x6A);

float aX, aY, aZ, gX, gY, gZ;
float lastMagnitude = 0;  // Store the previous magnitude
bool fallDetected = false;  // Flag to indicate a fall has been detected
bool prolongedFallDetected = false;
unsigned long lastFallTime = 0;  // Timestamp of the last fall detection
unsigned long inactivityStartTime = 0;  // Start time of inactivity period
unsigned long inactivityThreshold = 5000;  // Inactivity threshold (milliseconds)
unsigned long fallWaitTime = 5000;  // Time to wait after a fall detection (milliseconds)
unsigned long movementStartTime = 0;  // Time when movement started
unsigned long movementThreshold = 5000;  // Duration of movement window (5 seconds) to reset fall detection
unsigned long lastFallCheck = 0;  // Time of last fall detection check

// Interrupt Service Routine for button press
void buttonISR() {
  unsigned long currentTime = millis();
  if (currentTime - lastButtonPress > DEBOUNCE_DELAY) {
    lastButtonPress = currentTime;
    
    // Record the time when button was pressed down
    if (digitalRead(BUTTON_PIN) == LOW) {
      buttonPressStartTime = currentTime;
      buttonHeldDown = true;
      buttonReleased = false; // Reset release flag when pressed
      // Don't set buttonPressed flag immediately - wait to see if it's a short press or hold
    }
  }
}

void checkButtonRelease() {
  // If button is released (pin reads HIGH when using INPUT_PULLUP)
  if (buttonHeldDown && digitalRead(BUTTON_PIN) == HIGH) {
    unsigned long buttonHoldTime = millis() - buttonPressStartTime;
    buttonHeldDown = false; // Reset button held flag
    
    // If button was held for less than required duration, treat as a normal press for battery indicator
    if (buttonHoldTime < BUTTON_HOLD_DURATION) {
      buttonPressed = true; // Now set the buttonPressed flag for short presses
      buttonReleased = true; // Mark as released
    } else {
      // For long presses, we've already triggered emergency, so don't activate battery display
      buttonReleased = true;
      buttonReleasedAfterHold = true; // Set flag to indicate this was a manual emergency trigger
      Serial.println("Long press complete - emergency already triggered");
    }
  }
}

// Add this function to check for emergency button hold
void checkEmergencyButtonHold() {
  if (buttonHeldDown && !emergencyDetected) {
    unsigned long currentHoldTime = millis() - buttonPressStartTime;
    
    // Once the button has been held for the required duration, trigger emergency
    if (currentHoldTime >= BUTTON_HOLD_DURATION) {
      Serial.println("MANUAL EMERGENCY TRIGGER - 10 SECOND WARNING PERIOD");
      emergencyDetected = true;
      emergencyStartTime = millis();
      lastLEDBlinkTime = millis(); // Initialize LED blink timing
      
      // Set the manual emergency flag
      manualEmergency = true;
      
      // Send manual emergency alert over BLE
      String manualEmergencyAlert = "URGENT: Manual emergency triggered! Sending help in 10 seconds unless cancelled.";
      
      // Don't reset buttonHeldDown flag yet - wait for actual release
    }
  }
}

void setup() {
    Serial.begin(115200);
    while (!Serial); // Wait for serial port to connect
    
    // Initialize button with interrupt
    pinMode(BUTTON_PIN, INPUT_PULLUP);
    pinMode(buzzerPin, OUTPUT);
    attachInterrupt(digitalPinToInterrupt(BUTTON_PIN), buttonISR, FALLING);
    
    // Initialize MAX30102
    if (!particleSensor.begin(Wire, I2C_SPEED_FAST)) {
        Serial.println("MAX30102 was not found. Please check wiring/power.");
    }
    
    particleSensor.setup();
    particleSensor.setPulseAmplitudeRed(0x0A);
    particleSensor.setPulseAmplitudeGreen(0);

    // Initialize BLE
    if (!BLE.begin()) {
        Serial.println("Starting BLE failed!");
        while (1);
    }

    pinMode(greenLED, OUTPUT);
    pinMode(yellowLED, OUTPUT);
    pinMode(redLED, OUTPUT);
    digitalWrite(greenLED, LOW);
    digitalWrite(yellowLED, LOW);
    digitalWrite(redLED, LOW);
    

    BLE.setLocalName("HealthAndSpeechMonitor");
    
    // Set up all services and characteristics
    BLE.setAdvertisedService(healthService);
    healthService.addCharacteristic(healthCharacteristic);
    BLE.addService(healthService);

    BLE.setAdvertisedService(speechService);
    speechService.addCharacteristic(sensorCharacteristic);
    BLE.addService(speechService);

    BLE.setAdvertisedService(connectionService);
    connectionService.addCharacteristic(connectionCharacteristic);
    BLE.addService(connectionService);

    BLE.setAdvertisedService(batteryService);
    batteryService.addCharacteristic(batteryVoltageCharacteristic);
    BLE.addService(batteryService);

    BLE.setAdvertisedService(fallDetectionService);
    fallDetectionService.addCharacteristic(fallDetectionCharacteristic);
    BLE.addService(fallDetectionService);

    BLE.setAdvertisedService(manualEmergencyService);
    manualEmergencyService.addCharacteristic(manualEmergencyCharacteristic);
    BLE.addService(manualEmergencyService);

    BLE.advertise();

    // Initialize Audio
    if (!microphone_inference_start(EI_CLASSIFIER_RAW_SAMPLE_COUNT)) {
        ei_printf("ERR: Could not allocate audio buffer!\n");
        return;
    }

    // Initialize IMU
    if (myspeech.begin() != 0) {
        Serial.println("Failed to initialize IMU!");
    } else {
        Serial.println("IMU and Fall Detection Initialized");
    }

    lastSecondTime = millis();
    randomSeed(analogRead(A1));
}

void simulateVitals() {
    if (millis() - lastSimulationUpdate >= SIMULATION_INTERVAL) {
        lastSimulationUpdate = millis();
        int fluctuation = random(-2, 3);
        beatAvg = 75 + fluctuation;
        float spo2Fluctuation = random(-5, 6) / 10.0;
        spO2 = 98.5 + spo2Fluctuation;
        spO2 = constrain(spO2, 95.0, 100.0);
    }
}

void handleEmergencyState() {
    if (!emergencyDetected) return;

    // Blink red LED during warning period with its own timing
    if (millis() - lastLEDBlinkTime >= LED_BLINK_INTERVAL) {
        lastLEDBlinkTime = millis();
        digitalWrite(redLED, !digitalRead(redLED)); // Toggle state

        if (digitalRead(redLED) == HIGH) {
            // LED is on, buzzer on at 2200Hz
            tone(buzzerPin, 2200);
        } else {
            // LED is off, buzzer off
            noTone(buzzerPin);
        }
    }

    // Check if button was pressed to cancel
    if (buttonPressed) {
        buttonPressed = false;
        emergencyDetected = false;
        digitalWrite(redLED, LOW);
        noTone(buzzerPin);
        Serial.println("Emergency cancelled by button press");
        
        // Send cancellation message via appropriate BLE characteristic
        if (fallEmergency) {
            noTone(buzzerPin);
            String cancelMsg = "Fall emergency cancelled by user";
            fallDetectionCharacteristic.setValue(cancelMsg.c_str());
            fallEmergency = false;
            prolongedFallDetected = false;
        } else if (manualEmergency) { // Was a manual emergency
            noTone(buzzerPin);
            String cancelMsg = "Manual emergency cancelled by user";
            manualEmergencyCharacteristic.setValue(cancelMsg.c_str());
            manualEmergency = false;
        } else if (speechEmergency) {
            noTone(buzzerPin);
            String cancelMsg = "Voice emergency cancelled by user";
            sensorCharacteristic.setValue(cancelMsg.c_str());
            speechEmergency = false;
        }
        return;
    }

    // Check if 10 seconds have passed
    if (millis() - emergencyStartTime >= EMERGENCY_DELAY) {
        emergencyDetected = false;
        digitalWrite(redLED, LOW);
        noTone(buzzerPin);
        // Perform emergency response
        Serial.println("PERFORMING EMERGENCY RESPONSE");
        digitalWrite(LED_BUILTIN, LOW);
        delay(500);
        digitalWrite(LED_BUILTIN, HIGH);

        // Send alert via appropriate BLE characteristic based on what triggered the emergency
        if (fallEmergency) {
            String alertMessage = "ALERT: FALL WITH INACTIVITY DETECTED! SENDING HELP!";
            fallDetectionCharacteristic.setValue(alertMessage.c_str());
            Serial.println("Sent over BLE: " + alertMessage);
            fallEmergency = false;
            prolongedFallDetected = false;
        } else if (manualEmergency) { // Manual emergency was triggered by button hold
            String alertMessage = "ALERT: MANUAL EMERGENCY BUTTON ACTIVATED! SENDING HELP!";
            manualEmergencyCharacteristic.setValue(alertMessage.c_str());
            Serial.println("Sent over BLE: " + alertMessage);
            manualEmergency = false;
            buttonReleasedAfterHold = false; // Reset the flag
        } else if (speechEmergency) {
            String alertMessage = "ALERT: SPEECH HELP REQUEST DETECTED! SENDING HELP!";
            sensorCharacteristic.setValue(alertMessage.c_str());
            Serial.println("Sent over BLE: " + alertMessage);
            speechEmergency = false;
        }
    }
}

void updateBatteryVoltage() {
    if (millis() - lastBatteryUpdate >= BATTERY_UPDATE_INTERVAL) {
        lastBatteryUpdate = millis();
        float voltage = readBatteryVoltage();
        String voltageStr = String(voltage, 2); // Convert to string with 2 decimal places
        batteryVoltageCharacteristic.writeValue(voltageStr);
        Serial.print("Battery Voltage: ");
        Serial.print(voltageStr);
        Serial.println("V");
    }
}

void loop() {
    checkEmergencyButtonHold();
    
    checkButtonRelease();
    
    // Handle button press (for battery indicator or emergency cancellation)
    if (buttonPressed) {
        buttonPressed = false;
        Serial.println("Button pressed (short press)!");
        
        // Cancel any ongoing emergency
        if (emergencyDetected) {
            emergencyDetected = false;
            digitalWrite(redLED, LOW);
            noTone(buzzerPin);
            Serial.println("Emergency cancelled by button press");
            
            // Send cancellation message via appropriate BLE characteristic
            if (fallEmergency) {
                String cancelMsg = "Fall emergency cancelled by user";
                fallDetectionCharacteristic.setValue(cancelMsg.c_str());
                fallEmergency = false;
                prolongedFallDetected = false;
            } else if (manualEmergency) {
                String cancelMsg = "Manual emergency cancelled by user";
                manualEmergencyCharacteristic.setValue(cancelMsg.c_str());
                manualEmergency = false;
            } else if (speechEmergency) {
                String cancelMsg = "Voice emergency cancelled by user";
                sensorCharacteristic.setValue(cancelMsg.c_str());
                speechEmergency = false;
            }
        }
        // Cancel fall detection if active
        else if (fallDetected) {
            fallDetected = false;
            Serial.println("Fall detection cancelled by button press");
        }
        else {
            // Original battery LED toggle code - only for short presses
            if (!batteryLEDsOn) {
                // Turn on appropriate LED based on battery voltage
                float voltage = readBatteryVoltage();
                if (voltage > 2.4) {
                    digitalWrite(greenLED, HIGH);
                } else if (voltage > 0.6) {
                    digitalWrite(yellowLED, HIGH);
                } else {
                    digitalWrite(redLED, HIGH);
                }
                batteryLEDsOn = true;
            } else {
                // Turn off all battery indicator LEDs
                digitalWrite(greenLED, LOW);
                digitalWrite(yellowLED, LOW);
                digitalWrite(redLED, LOW);
                batteryLEDsOn = false;
            }
        }
    }

    handleEmergencyState();
    
    BLEDevice central = BLE.central();

    if (millis() - lastStatusPrint >= 3000) {
        lastStatusPrint = millis();
        if (central) {
            Serial.println("Device connected.");
        } else {
            Serial.println("No device connected.");
        }
    }

    if (millis() - lastConnectionUpdate >= 3000) {
        lastConnectionUpdate = millis();
        String connectionStatus = central ? "Connected" : "Disconnected";
        connectionCharacteristic.writeValue(connectionStatus);
    }

    // Update battery voltage over BLE
    updateBatteryVoltage();

    irValue = particleSensor.getIR();
    
    if (irValue > 70000) {
        simulatingVitals = true;
        simulateVitals();
    } else {
        simulatingVitals = false;
        beatAvg = 0;
        spO2 = 0.0;
        
        if (checkForBeat(irValue)) {
            long delta = millis() - lastBeat;
            lastBeat = millis();
            beatsPerMinute = 60 / (delta / 1000.0);

            if (beatsPerMinute < 255 && beatsPerMinute > 20) {
                rates[rateSpot++] = (byte)beatsPerMinute;
                rateSpot %= RATE_SIZE;
                beatAvg = 0;
                for (byte x = 0 ; x < RATE_SIZE ; x++)
                    beatAvg += rates[x];
                beatAvg /= RATE_SIZE;
            }
        }
    }

    if (millis() - lastSecondTime >= 3000) {
        lastSecondTime = millis();
        float temperature = readTemperature();

        Serial.print("IR Value: ");
        Serial.print(irValue);
        Serial.print(", BPM: ");
        Serial.print(beatAvg);
        Serial.print(", SpO2: ");
        Serial.print(spO2, 1);
        Serial.print("%, Temperature: ");
        Serial.print(temperature, 2);
        Serial.println("°C");

        String healthData = String(beatAvg) + "," + String(spO2, 1) + "," + String(temperature, 2);
        if (central) {
            healthCharacteristic.writeValue(healthData);
        }
    }

    detectEnhancedFall();
    
    if (!manualEmergency && microphone_inference_record()) {
        run_classification();
    }

    BLE.poll();
    delay(20);
}

float readBatteryVoltage() {
    int rawValue = analogRead(batteryPin);
    float voltage = (rawValue / adcResolution) * referenceVoltage * voltageDivider;
    return voltage;
}

void detectEnhancedFall() {
    if (millis() - lastFallCheck < 100) {
        return;
    }
    lastFallCheck = millis();
    
    aX = myspeech.readFloatAccelX();
    aY = myspeech.readFloatAccelY();
    aZ = myspeech.readFloatAccelZ();

    gX = myspeech.readFloatGyroX();
    gY = myspeech.readFloatGyroY();
    gZ = myspeech.readFloatGyroZ();

    // Convert acceleration values from g to m/s²
    aX *= 9.81;  // Conversion factor from g to m/s²
    aY *= 9.81;
    aZ *= 9.81;

    // Calculate the magnitude of the acceleration vector in m/s²
    float magnitude = sqrt(aX * aX + aY * aY + aZ * aZ);

    // Calculate the rate of change of the acceleration magnitude
    float rateOfChange = abs(magnitude - lastMagnitude);

    // Check if the magnitude exceeds the threshold, the rate of change is significant,
    // and gyroscope readings are above a certain threshold (indicating rotation)
    // 40, 35
    if (magnitude > 40.0 && rateOfChange > 35.0 && !fallDetected && !emergencyDetected) {  // Fall detection criteria
        // Fall detected: Print and reset the fall flag
        Serial.println("Fall detected!");
        fallDetected = true;
        lastFallTime = millis();  // Record the time of the fall detection
        
        String fallAlert = "ALERT: Potential fall detected! Checking for movement...";
        fallDetectionCharacteristic.setValue(fallAlert.c_str());
    }

    // After the fall detection and wait time, check for inactivity
    if (fallDetected && millis() - lastFallTime >= fallWaitTime) {
        // If movement occurs within the window after the fall, reset fall detection
        if (rateOfChange > 3.0) {  // If movement is detected (rate of change > 3.0)
            if (movementStartTime == 0) {
                movementStartTime = millis();  // Start the movement timer
            }

            // Check if movement occurred within the movement threshold window
            if (millis() - movementStartTime <= movementThreshold) {
                Serial.println("Motion detected after fall. Emergency response canceled.");
                
                // Notify via BLE that movement was detected and emergency canceled
                String cancelAlert = "UPDATE: Movement detected after fall. No emergency.";
                fallDetectionCharacteristic.setValue(cancelAlert.c_str());
                
                fallDetected = false;  // Reset fall detection flag
                movementStartTime = 0;  // Reset movement timer
                return;  
            }
        }

        // If no significant movement (inactivity detected)
        if (rateOfChange < 3.0) {  // Low threshold for inactivity
            if (inactivityStartTime == 0) {
                inactivityStartTime = millis();  // Start inactivity timer
            }

            // If inactivity lasts beyond inactivityThreshold, trigger emergency response
            if (millis() - inactivityStartTime > inactivityThreshold && !emergencyDetected) {
                Serial.println("FALL WITH INACTIVITY DETECTED - 10 SECOND WARNING PERIOD");
                
                // Only trigger if no other emergency is active
                emergencyDetected = true;
                emergencyStartTime = millis();
                lastLEDBlinkTime = millis(); // Initialize LED blink timing
                
                // Set fall emergency flag
                fallEmergency = true;
                
                // Send fall with inactivity alert over BLE
                String fallWithInactivityAlert = "URGENT: Fall detected with prolonged inactivity! Sending help in 10 seconds unless cancelled.";
                fallDetectionCharacteristic.setValue(fallWithInactivityAlert.c_str());
                prolongedFallDetected = true;
                fallDetected = false;  // Reset fall detection flag
                inactivityStartTime = 0;  // Reset inactivity timer
            }
        }
    }

    // Store the current magnitude for the next loop iteration
    lastMagnitude = magnitude;
}

float readTemperature() {
    static long lastTempRead = 0;
    if (millis() - lastTempRead >= 1000) {
        lastTempRead = millis();
        int adcValue = analogRead(THERMISTOR_PIN);
        float voltage = (adcValue / (float)ADC_MAX) * VCC;
        float resistance = SERIES_RESISTOR * (VCC / voltage - 1.0);
        float steinhart = log(resistance / NOMINAL_RESISTANCE) / B_COEFFICIENT;
        steinhart += 1.0 / (NOMINAL_TEMPERATURE + 273.15);
        steinhart = 1.0 / steinhart - 273.15;
        return steinhart;
    }
    return -1;
}

void run_classification() {
    // Skip classification if a manual emergency is already active
    if (manualEmergency) return;
    
    signal_t signal;
    signal.total_length = EI_CLASSIFIER_RAW_SAMPLE_COUNT;
    signal.get_data = &microphone_audio_signal_get_data;
    ei_impulse_result_t result = { 0 };

    EI_IMPULSE_ERROR r = run_classifier(&signal, &result, debug_nn);
    if (r != EI_IMPULSE_OK) {
        ei_printf("ERR: Failed to run classifier (%d)\n", r);
        return;
    }

    // Print classification results every second (independent of LED blinking)
    if (millis() - lastClassificationPrint >= CLASSIFICATION_PRINT_INTERVAL) {
        lastClassificationPrint = millis();
        ei_printf("Predictions (DSP: %d ms, Classification: %d ms, Anomaly: %d ms):\n",
                  result.timing.dsp, result.timing.classification, result.timing.anomaly);
        
        for (size_t ix = 0; ix < EI_CLASSIFIER_LABEL_COUNT; ix++) {
            ei_printf("    %s: %.5f\n", result.classification[ix].label, result.classification[ix].value);
        }
    }

    // Check for speech emergency detection 
    if (!manualEmergency && !fallEmergency) {
        for (size_t ix = 0; ix < EI_CLASSIFIER_LABEL_COUNT; ix++) {
            if (strcmp(result.classification[ix].label, "sendhelp") == 0 && 
                result.classification[ix].value > 0.5 && 
                !emergencyDetected) {
                
                Serial.println("HELP REQUEST DETECTED - 10 SECOND WARNING PERIOD");
                emergencyDetected = true;
                emergencyStartTime = millis();
                lastLEDBlinkTime = millis(); // Initialize LED blink timing
                
                speechEmergency = true;
                
                String speechAlert = "URGENT: Voice help request detected! Sending help in 10 seconds unless cancelled.";
            }
        }
    }
}

static void pdm_data_ready_inference_callback(void) {
    int bytesAvailable = PDM.available();
    int bytesRead = PDM.read((char *)&sampleBuffer[0], bytesAvailable);

    if (!inference.buf_ready) {
        for (int i = 0; i < bytesRead / 2; i++) {
            inference.buffer[inference.buf_count++] = sampleBuffer[i];
            if (inference.buf_count >= inference.n_samples) {
                inference.buf_count = 0;
                inference.buf_ready = 1;
                break;
            }
        }
    }
}

static bool microphone_inference_start(uint32_t n_samples) {
    inference.buffer = (int16_t *)malloc(n_samples * sizeof(int16_t));
    if (!inference.buffer) return false;

    inference.buf_count = 0;
    inference.n_samples = n_samples;
    inference.buf_ready = 0;

    PDM.onReceive(&pdm_data_ready_inference_callback);
    PDM.setBufferSize(4096);

    if (!PDM.begin(1, EI_CLASSIFIER_FREQUENCY)) {
        ei_printf("Failed to start PDM!");
        microphone_inference_end();
        return false;
    }

    PDM.setGain(127);
    return true;
}

static bool microphone_inference_record(void) {
    inference.buf_ready = 0;
    inference.buf_count = 0;
    while (!inference.buf_ready);
    return true;
}

static int microphone_audio_signal_get_data(size_t offset, size_t length, float *out_ptr) {
    numpy::int16_to_float(&inference.buffer[offset], out_ptr, length);
    return 0;
}

static void microphone_inference_end(void) {
    PDM.end();
    free(inference.buffer);
}

#if !defined(EI_CLASSIFIER_SENSOR) || EI_CLASSIFIER_SENSOR != EI_CLASSIFIER_SENSOR_MICROPHONE
#error "Invalid model for current sensor."
#endif