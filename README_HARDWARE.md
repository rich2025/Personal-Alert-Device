# Hardware Information

This README file is dedicated to providing all the relative information regarding the hardware for the Personal Alert Device. This includes documentation on the enclosure, charging stand, information regarding the power system, the sensors we used, and a breakdown of the cost for all the parts included in our project.

## Relevant Files For CAD Drawings and Assemblies

- [Body of Enclosure Drawing](./PDFs/Body_Drawing.pdf)  
  The PDF file above depicts the body of the enclosure and includes notable measurements for the device in millimeters. This is the central part of the enclosure, as it houses all of the sensors, the PCB, and the device’s battery. The holes in the enclosure are for the LEDs, USB-C, and a button.

- [Bottom of Enclosure Drawing](./PDFs/Bottom_Drawing.pdf)  
  The PDF file above depicts the bottom of the enclosure. There are holes included on the bottom for magnets and for the wireless charging coil. There is also a window cut-out for wires to pass through, including the coil from the battery, the jumper cables for the heart rate sensor, and the thermistor. The length and width of the bottom also match the measurements for the body.

- [Charging-Stand Drawing](./PDFs/Charging-Stand_Drawing.pdf)  
  The PDF file above shows the wireless charging stand used for our device. The bottom of the enclosure sits on top of the stand and uses magnets to ensure proper connection. The coil also sits on top of the charging stand to stay in place. The port connection is what’s stored inside the charging stand and also offers support to the stand.

- [PAD OnShape File](https://bu.onshape.com/documents/028d79f541c7e4d4f72e95ac/w/96d4ac62ff30451bad3e9328/e/472430a287832b7987da914a?renderMode=0&uiState=6809a08bf0e943499a3269d9)  
  The link above will direct you to the OnShape file containing all of the drawings above, as well as the actual CAD assemblies for the body, bottom, and top of the enclosure and the charging stand.

## Images of the Full Assembly and PCB
![Enclosure with exposed interior](./images/enclosure1.jpeg)
![Enclosure fully assembled](./images/enclosure1.jpeg)
![PCB Wiring Diagram](./images/pcbwiring.png)
![PCB Design](./images/pcb1.png)

## Bill of Materials
![BOM](./images/BOM.png)

## Sensors and other Hardware Equipment Used
Below you will find a list of the sensors we used for our device as well as the datasheets from their respective manufactures' page.
<br></br>
[Seeed Studio XIAO nRF52840 Sense - TinyML/TensorFlow Lite- IMU / Microphone - Bluetooth5.0](./PDFs/microcontroller.pdf)
We chose this microcontroller for its multi-functionality. The microcontroller mainly serves as the hub for all of our sensor code, but it also came equipped with a microphone, which we used for our ML voice recognition model, and bluetooth built in which we were able to use to send our data to our database. The price of the microcontroller was also kept in mind when sourcing parts, and this one came out to $18 USD.
<br></br>
[MAX30102 Pulse Oximeter](./PDFs/max30102.pdf)
We chose this heart rate sensor because it was advertised as a sensor you could use in contact with an individual's skin. The heart rate detection range is 50-180 BPM and we believed that was suitable for our device's use case. During our first tests with the sensor we were unable to obtain accurate readings for our BPM compared to reading we found using an apple watch. We concluded that the reason for this faultiness could have been due to the sensor optical lens picking up on light from external sources while testing. Although we originally had trouble gaining accurate sensor data from the heart rate sensor, we were able to calibrate the sensor to pick up heart rate values in the nomral range of 60-85 BPM and also use a rolling average to calculate the heart rate instead of continuos values. Also, to avoid light from external sources interfering with the sensors readings, we decided to house the sensor along the watch band in order for the sensor to only interact with the user's wrist.
<br></br>
[3.7V 1100mAh LiPo Battery](./PDFs/battery.pdf)
**ADD INFO HERE ON BATTERY**
<br></br>
[B57703M0103A018 Thermistor](./PDFs/NTC_thermistor.pdf)
We chose this temperature sensor as NTC thermistor's are typically used in thermometers, and it was also one of the only temperature sensors we could find which was able to make contact with the users skin. We had no issues with the thermistor and it was able to provide us with accurate readings of typical room temperature values as well as body temperature values. Similar to the heart rate sensor, the thermistor is also house along the watch band in order to make contact with the user's wrist for accurate sensor values. 
<br></br>
The fillament that we used for the 3D printed enclosure and housing stand was Bambu PLA filament. The printer that we used was the Bambu Lab X1E 3D printer which provide us with fast print times and the ability to print multiple parts at once. This was great for iterating on models whenever necessary and made the development of our housing quick and effective. 
<br></br>
The material we used for our watch band was nylon polymer and we chose this material as it was a great insulator. The material we chose had to have properties of an insulator since we decided to store the heart rate and thermistor along the watch band. 
<br></br>
**ADD INFO HERE ON POWER SYSTEM**
