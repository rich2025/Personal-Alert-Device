Personal Alert Device
=================
EC463/464 2024-2025\
Team 19\
Richard Yang, Tanveer Dhilon, Logan Lechuga, Renad Alanazi

## Engineering Addendum

### Overview
The Personal Alert Device is a smart emergency alert actuator system designed for the purpose of providing independently living senior citizens a greater sense of safety. At the center of the system is wearable device which features vitals sensing, speech recognition, fall detection, and emergency actuation. Accompanying this device is a wireless charging transmission module which allows users to recharge the device's 3.7V LiPo battery. The computational hub of the system lies in an Android based mobile application which collects user information, handles emergency responses, and displays device metrics such as live vitals and battery status. In addition, the app interfaces with Firebase in the backend to handle user management, and real-time data storage/retrieval in Firestore.

### System Block Diagram

High-level system block diagram illustrating the interconnection of various sub-systems within the Personal Alert Device system.

![Copy of Communications](https://github.com/user-attachments/assets/35403a93-ef55-4306-aa80-25ca30373815)

### Current State of the Project

#### Hardware

Both enclosures for the wearable device and charging station are complete and printed. The custom PCB with the microcontroller and any additional electrical components have been soldered and placed within the wearable enclosure. The sensor-integrated wrist-strap has been constructed and attatched to the wearable enclosure. The magnets used to connect the receiving module on the base of the wearable to the top of the charging stand are in place. The battery is soldered and powers the system efficiently. The three LEDs, button switch, and buzzer are in place and are functioning as expected.

* The 3.7V 1100 mAh LiPo battery supports a lifetime of ~72 hours with a maximum charging time (wireless) of 4 hours at 300 mA charging current.
* Wearable device based user feedback is through one of three LEDs and an internal buzzer.
* Input to the wearable device from speech, acceleration, gyproscope (angular momentum), and button switch.
* Additional components integrated into custom PCB

#### Firmware (Microcontroller)

All programming to the microcontroller which governs the functionality of the wearable are in place. The wearable runs inferencing on speech over double-buffered input windows while asynchronously collecting sensor data, button inputs, advertising data over BLE, and reading IMU data for fall detection. Additionally, the controller reads battery voltage to compute battery level. Finally, button and debouncing logic is implemented to either check battery level or trigger and emergency response. The LEDs are used for battery level indication or emergency trigger indication. The buzzer is used for emergency trigger indication. The six characteristics advertised to BLE are: vitals, speech, battery, connection status, fall, and manual. 

* C++ using Arduino framework targeting the Seeed Studio XIAO nRF52840 Sense controller
* All vital sensors (pulse oximeter and thermistor) are integrated and tested individually. The pulse oximeter measures IR frequency to compute the user's heart rate and blood oxygen saturation. The voltage is measured across the thermistor and computed to temperature using the Steinhart equation.
* Bluetooth 5.0/BLE communication is used to transmit real-time sensor data, emergency events, and device status to the mobile app
* Button switch double functionality as emergency trigger and battery level indicator
* Fall detection using initial threshold and confirmation threshold
* Speech recognition using machine learning model imported from Edge Impulse

#### Software (Frontend/Backend)

* Mobile app built in Kotlin using Jetpack Compose UI framework
* BLE scanning using LightBlue and Adafruit IO as cloud database
* Firebase for user authentication using Google SSO
* Firestore for user information, emergency records, device status, and sensor data
* SMS via Adafruit IO set up for emergency notifications
* App communications to Firestore and Adafruit IO through Firebase SDK messages and webhooks
* App collects phone information such as contacts, photos, and location using Android permissions API
* User information and medical information input by the user within the app
* The app handles emergency responses, records, and notifications
* Display real time vitals and emergency alerts



