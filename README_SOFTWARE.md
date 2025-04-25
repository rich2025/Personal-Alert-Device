Software and Firmware Report
=================

## Introduction

Here is a comprehensive guide on how to setup the software necessary to run the Personal Alert Device system, including the firmware running on the device's XIAO nRF52840 Sense microcontroller. While the mobile app will be eventually released for download on the Google Play store in one package, instructions are also provided for developers or testers who wish to build and run the app manually from the source code. This includes setting up the Android development environment, cloning the project repository, configuring Firebase services, and enabling the necessary permissions. Additionally, guidance is provided on how to connect the app to the wearable device via Bluetooth and how to test emergency alerts, sensor data streaming, and backend integration with services like Firestore and Adafruit IO.

### Additional Software / Services

1. Arduino IDE 2.3.6 or newer https://www.arduino.cc/en/software/
2. Android Studio Ladybug or newer https://developer.android.com/studio/install
3. LightBlue (BLE Scanner) https://punchthrough.com/lightblue/
4. Personal Alert Device Firebase
5. Personal Alert Device Adafruit IO 

### Minimum PC Requirements

#### Android Studio
Due to the computation resources required for the Android emulator, the following minimum PC specifications are recommended:
* Operation System
  - Windows 10/11 (64-bit)
  - macOS (10.14 Mojave or higher)
* RAM: 8 GB minimum (16 GB recommended)
* Disk Space: 8 GB minimum (SSD recommended) + at least 4 GB for Android SDK and emulator system images
* CPU: At least 4 cores

## Installation

### Mobile Application

To run the mobile application in Android Studio, follow the instructions below:

1. 'git clone https://github.com/rich2025/Personal-Alert-Device.git'








