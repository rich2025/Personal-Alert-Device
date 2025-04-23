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

The software is comprised of the mobile app, LightBlue, Adafruit IO, and Firebase. The mobile app UI is fully completed and interfaces with Adafruit IO and Firebase to store/retrieve system data. Firebase stores user data, emergency records, device data, and handles authentication. LightBlue interfaces with Adafruit IO to transfer data from the device to the phone.

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

### Common Issues and Solutions

Throughout the development for the Personal Alert Device, there were several technical challenges and design hurdles which requires significant troubleshooting. Below are the most important lessons learned, which knowing beforehand would have saved a great deal of time.

#### Data Loss and Persistence
Early versions of the mobile app had trouble maintaining data consistency across screens. Navigation away from a screen or relaunching the app would cause user data to reset. This caused an inconsistency between what appeared on the app and what was stored in Firestore. However, leaning heavily on Firestore's built-in data persistence, shared view models were introduced to the app which automatically caches data locally and synchronizes it when connectivity is available. Additionally, data would be refetched upon entering the screen or updating information, whcih provided real-time updates and offline support.

#### Charging Alignment
Charging the wearable was unreliable due to the wireless charging contacts requirements adequate alignment. To solve this issue, both a magnetic and mechanical fit were introduced in the transmission and receiving modules. The magnets forced correct alignment while holding the contacts together. Additionally, the enclosure designed were refined so that the charging coils would lie flush on top of one another.

#### App Crashes
Sometimes, the app would crash or fail to upload data to Firestore with no apparent cause. However, the solution was to clear the app's memory and cache within the phone settings. This could be because of outdated Android permissions or API tokens.

#### False Alarms
To lessen the issue of false positives for emergency detection (falls, speech requests, manual requests), an emergency warning period was introduced to allow the user adequate time to cancel the emergency request. This was implemented on the wearable where after an emergency is first triggered, a warning period of ten seconds would initiate. For these ten seconds, the red LED and buzzer blinks periodically. This provides the user visual and auditory feedback that an emergency is triggered. The request can be canceled via a singular button push, or if the button is not pushed within the warning period, then the emergency response initiated.

### Things to Keep in Mind

While the Personal Alert Device has been carefully designed and tested, there are several important caveats and limitations that one should be aware of.

#### Bluetooth Dependency
The system relies entirely on BLE for device-app communication. If the Bluetooth connection drops, the device will not send alerts. This is because of the XIAO nRF52840's limitations to only Bluetooth 5.0. A highly recommended alternative would be to implement a cellular module and/or WiFi compatible module in the next design iteration.

#### Device Reliability and Proof-of-Concept Reminder
The Personal Alert Device is a prototype and not certified for real-world emergencies. As the system is still very much a proof of concept, users should remain cautious. For the product to come to market, rigorous testing would be needed to become legally certified.

#### Battery Management
Overcharing the LiPo battery may degrade the battery or cause damage over time. Additionally, the microcontroller may occasionally drop into a "sleep" mode if disconnected for long periods of time. This requires the microcontroller be plugged into a power source via USB-C to "wake" the device.

#### Speech Recognition
The speech recognition keyword, "send help", has sensitivity tradeoffs where certain background noises could accidentally trigger an emergency. This threshold can be adjusted, but requires access to the firmware. However, if the threshold is too high, then the device may experience false negatives. A solutiont to this would be independetly rebuilding the model off of sample data from each individual user.

#### Mobile App Version Dependencies
The app is developed for Android 14+. It is still unknown at this time how future OS updates may impact the app's support of various features. Additionally, older OS versions may not function correctly.

#### Housing
The enclosure is not waterproof and any mositure ingress may cause internal damages. Additionally, the housing will break if an excessive force is placed upon it. The wrist-strap is prone to fraying. The suggestion for the next iteration's develepors is to construct the device with stronger, more robust materials.




