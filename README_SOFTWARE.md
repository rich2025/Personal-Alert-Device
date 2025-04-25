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

Shown below is a complete flow chart showing the dependencies between various screens, libraries, and permissions within the mobile application.

![Capture](https://github.com/user-attachments/assets/80f12ebb-a33e-4f1b-9622-9f650f7043f7)
https://www.canva.com/design/DAGk7dr4EGk/H9p2tAtZztpfraC8wl7Fqw/edit?utm_content=DAGk7dr4EGk&utm_campaign=designshare&utm_medium=link2&utm_source=sharebutton

To run the mobile application in Android Studio, follow the instructions below:

1. Run `git clone https://github.com/rich2025/Personal-Alert-Device.git`
2. Launch Android Studio
3. Click on "**Open**" and navigate to the cloned folder
4. Wait for Gradle to sync (installing necessary dependencies)

The necessary dependencies added to the app-level Gradle build file (build.gradle.kts) are:

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
7. Retrieve the `google-services.json` file from `Personal-Alert-Device/app/google-services.json` and add it in the Project level app folder if not present already
8. In Android Studio, navigate to the Gradle > Personal Alert Device > Tasks > android > signingReport to access the SHA-1 fingerprint associated with your device
9. Add the SHA-1 finger to the app's certified fingerprints in Firebase > Project Overview > Project Settings > Your Apps

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
10. Connect the device to the phone via Bluetooth (LightBlue)

### Backend/Cloud Storage
Cloud-based data storage and retrieval is handled in Firestore and Adafruit IO.

When the user firsts enters the app, they will be prompted to login using Google. Using Google Single Sign On, Google accounts associated with the user's phone will automatically be retrieved and displayed. After the user logs in, data associated with their Google account such as their name, email, profile picture, and a unique user identifier will be stored in a newly created Firestore collection using the user's unique identifier. By using this id created at sign-in, data storage Firestore is both segmented but also secure and scalable.

Each user's collection within Firestore will be distinuishable with their identifier. All future Firestore updates will be called using this identifier, which is a dynamic variable within the app's logic. This ensures that the user's associated data can be accesible from any device they may choose to login with. The Google SSO is handled via Firebase, where a list of all users, their date of account creation, and their last logged in data is stored.

All API calls, webhooks, or HTTP requests are already in place within the mobile app source code. APIs, such as Android Permissions, is used to gather user data without any extraneous user input. Such data includes the user's contacts, photos, or location. Using these permissions improves the app's ease of usability. Additionally, the Android Firebase SDK is used to abstract the REST APIs employed to either store or fetch user data from Firestore. Adafriot IO serves as a midpoint between raw data sent over BLE and data read within the app.

The app uses Adafruit IO feed web APIs to fetch data sent over BLE and format it into values and timestamps. These values can then be parsed to display in the app or store in Firestore. As long as the keys and URLs remain consistent, backend functionality will work automatically due to the dynamic logic within the app after adding the new SHA-1 fingerprint to the Google Cloud project.

Logic within the app ensures that new user data will be contained securely within one their specific document. Adafruit IO communications with the user's phone/emulator and the device, through BLE, is already setup with no need to download additional software. However, a steady Bluetooth and network connection is necessary.

### Communications via Bluetooth
Due to the limitations of the XIAO nRF52840 Sense, Bluetooth 5.0/BLE is the sole means of data communication out of the wearable device. Thus, for the current design iteration, a BLE scanner is needed. However, in future iterations, a Bluetooth or WiFi module will remove the need for an additional BLE scanner. 

For our purposes, LightBlue is used to connect to the device. From the device's firmware, BLE characteristics will automatically be advertised consistently. All that is needed is to connect to the device within LightBlue and connect each characteristic with its respective feed in Adafruit IO. To do this, the feed name and API key for each feed is needed. These can be found within the app's `MainActivity.kt` and `MainScreen.kt` as they are also used to fetch feed data from Adafruit IO. Once connected to Adafruit IO, data will automatically begin to populate in the respective feeds, and be read from the phone.



   
   








