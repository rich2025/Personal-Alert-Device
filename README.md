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

* All vital sensors (pulse oximeter and thermistor) are integrated and tested individually. The pulse oximeter measures IR frequency to compute the user's heart rate and blood oxygen saturation. The voltage is measured across the thermistor and computed to temperature using the Steinhart equation.
* The 3.7V 1100 mAh LiPo battery supports a lifetime of ~72 hours with a maximum charging time of 4 hours at 300 mA charging current.
* System data packaged into individual characteristics 



