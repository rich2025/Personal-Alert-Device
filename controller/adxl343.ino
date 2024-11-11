#include <Wire.h>
#include <Adafruit_Sensor.h>
#include <Adafruit_ADXL343.h>

/* Assign a unique ID to this sensor at the same time */
Adafruit_ADXL343 accel = Adafruit_ADXL343(12345);

/** The input pin to enable the interrupt on, connected to INT1 on the ADXL. */
#define INPUT_PIN_INT1   (5) // SAMD21/SAMD51 = 5 for interrupt pin

uint32_t g_tap_count = 0;
int_config g_int_config_enabled = { 0 };
int_config g_int_config_map = { 0 };

/** Interrupt service routine for INT1 events. This will be called when a fall is detected. */
void int1_isr(void)
{
    g_tap_count++;
}

void setup(void)
{
  Serial.begin(9600);
  while (!Serial);
  Serial.println("ADXL343 Single Tap INT Tester"); Serial.println("");
  

  /* Initialise the sensor */
  if(!accel.begin())
  {
    /* There was a problem detecting the ADXL343 ... check your connections */
    Serial.println("Ooops, no ADXL343 detected ... Check your wiring!");
    while(1);
  }

  /* Set the range to whatever is appropriate for your project */
  accel.setRange(ADXL343_RANGE_16_G);

  /* Configure the HW interrupts. */
  pinMode(INPUT_PIN_INT1, INPUT);
  attachInterrupt(digitalPinToInterrupt(INPUT_PIN_INT1), int1_isr, RISING);

  /* Enable single tap interrupts on the accelerometer. */
  g_int_config_enabled.bits.freefall   = true;
  accel.enableInterrupts(g_int_config_enabled);

  /* Map single tap interrupts to INT1 pin. */
  g_int_config_map.bits.freefall   = ADXL343_INT1;
  accel.mapInterrupts(g_int_config_map);

  accel.writeRegister(ADXL3XX_REG_OFSX, 0x06); // X-axis offset
  accel.writeRegister(ADXL3XX_REG_OFSY, 0xF9); // Y-axis offset
  accel.writeRegister(ADXL3XX_REG_OFSZ, 0xFC); // Z-axis offset
  accel.writeRegister(ADXL3XX_REG_THRESH_ACT, 0x20); // Activity threshold: Set as 2g (0x20) or 0.5g (0x08)
  accel.writeRegister(ADXL3XX_REG_THRESH_INACT, 0x03); // Inactivity threshold: Set as 0.1875g (0x03)
  accel.writeRegister(ADXL3XX_REG_TIME_INACT, 0x02); // Inactivity time: Set as 2s (0x02)
  accel.writeRegister(ADXL3XX_REG_ACT_INACT_CTL, 0x7F); // Enable Activity/Inactivity control on all axes (X, Y, Z), set as DC-coupled for activity and AC-coupled for inactivity
  accel.writeRegister(ADXL3XX_REG_THRESH_FF, 0x05); // Free-Fall threshold
  accel.writeRegister(ADXL3XX_REG_TIME_FF, 0x03);     // Free-Fall time 
  accel.writeRegister(ADXL3XX_REG_BW_RATE, 0x0A);  // Data rate and power mode: Set sample rate as 100 Hz (0x0A)

  /* Reset fall counter. */
  fall_count = 0;

  Serial.println("ADXL343 init complete. Waiting for single tap INT activity.");
}

void loop(void)
{
  /* Get a new sensor event */
  /* Reading data clears the interrupts. */
  sensors_event_t event;
  accel.getEvent(&event);
  delay(10);

  while (g_tap_count) {
      Serial.println("Fall Detected!");
      /* Clear the interrupt as a side-effect of reading the interrupt source register.. */
      accel.checkInterrupts();
      /* Decrement the local interrupt counter. */
      g_tap_count--;
  }
}
