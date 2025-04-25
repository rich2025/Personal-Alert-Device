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

1. Run `git clone https://github.com/rich2025/Personal-Alert-Device.git`
2. Launch Android Studio
3. Click on "**Open**" and navigate to the cloned folder
4. Wait for Gradle to sync (installing necessary dependencies)

The necessary dependencies added to the app-level Gradle build file are:

#### Authentication
- `com.google.firebase:firebase-auth`
- `com.google.firebase:firebase-auth-ktx`
- `com.google.android.gms:play-services-auth`

#### Database / Backend
- `com.google.firebase:firebase-bom`
- `com.google.firebase:firebase-firestore-ktx`
- `org.jetbrains.kotlinx:kotlinx-coroutines-play-services`

#### Networking
- `com.squareup.retrofit2:retrofit`
- `com.squareup.retrofit2:converter-gson`
- `com.squareup.okhttp3:okhttp`
- `com.squareup.okhttp3:logging-interceptor`

#### Debugging / Logging
- `com.jakewharton.timber:timber`

#### Navigation
- `androidx.navigation:navigation-compose`

#### Maps / Location
- `com.google.android.gms:play-services-maps`
- `com.google.maps.android:maps-compose`

#### UI / Image Handling
- `com.google.accompanist:accompanist-coil`
- `io.coil-kt:coil-compose`
- `com.github.yalantis:ucrop`

#### Permissions
- `com.google.accompanist:accompanist-permissions`
  
Additional permissions declared in the application's manifest file (`AndroidManifest.XML`):

#### Contacts
- `android.permission.READ_CONTACTS`

#### Location
- `android.permission.ACCESS_FINE_LOCATION`
- `android.permission.ACCESS_COARSE_LOCATION`

#### Network Access
- `android.permission.INTERNET`
- `android.permission.ACCESS_NETWORK_STATE`

#### Camera & Media Storage
- `android.permission.CAMERA`
- `android.permission.WRITE_EXTERNAL_STORAGE`
- `android.permission.READ_EXTERNAL_STORAGE`
- `android.permission.READ_MEDIA_IMAGES`

#### UCrop Image Editor Activity
- `android:name="com.yalantis.ucrop.UCropActivity"`
- `android:screenOrientation="portrait"`
- `android:theme="@style/Theme.AppCompat.Light.NoActionBar"`

#### Google Maps API Key
- `android:name="com.google.android.geo.API_KEY"`
- `android:value="redacted"`

Ensure that these dependencies and permissions are properly installed. 

5. Navigate to Device Manager and create a new emulated device (recommended: Medium Phone API 35)
   - OS Version must be Android 15.0 and above
6. Begin the device emulator and the Personal Alert Device app will open automatically

### Firmware

1. Open Arduino IDE
2. Import the new sketch `Personal_Alert_Device.ino`
3. Connect the device via USB-C
4. Add Seeed Studio XIAO nRF52840 (Sense) board package to your Arduino IDE
5. Navigate to File > Preferences, and fill "Additional Boards Manager URLs" with the url: https://files.seeedstudio.com/arduino/package_seeeduino_boards_index.json
6. Navigate to Tools > Board > Boards Manager and install `Seeed nRF52 mbed-enabled boards`
7. Select the board and port
8. Install the following libraries:
   * Edge Impulse Speech Recognition ML Model\
     [ei-personalalertdevice-arduino-1.0.11.zip](https://github.com/rich2025/Personal-Alert-Device/blob/d2f2b531bbc02767a61a1968adb4b268a8044e4b/ei-personalalertdevice-arduino-1.0.11.zip)
   * `Wire.h`
   * `PDM.h`
   * `ArduinoBLE.h`\
     https://github.com/arduino-libraries/ArduinoBLE
   * `Arduino_LSM6DS3.h`\
     https://github.com/arduino-libraries/Arduino_LSM6DS3
   * `MAX30105.h`\
     https://github.com/sparkfun/SparkFun_MAX3010x_Sensor_Library
   * `heartRate.h`
9. Compile and upload the sketch onto the microcontroller

### Backend/Cloud Storage
Cloud-based data storage and retrieval is handled in Firestore and Adafruit IO. Authentication is handled in Google Firebase. All API calls, webhooks, or HTTP requests are already in place within the mobile app source code. Thus, as long as the keys and URLs remain consistent, backend functionality will work automatically. Logic within the app ensures that new user data will be contained securely within one their specific document. Adafruit IO communications with the user's phone/emulator and the device, through BLE, is already setup with no need to download additional software. However, a steady Bluetooth and network connection is necessary.

   
   








