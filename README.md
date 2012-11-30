# R2993

## Intro 
Is a reference implementation of using Android as a USB host to provide power and wireless network connectivity to an Arduino.
It consists of two parts.

1. Relay01 - A native android application for relaying data over a TCP connection to the USB on the Android device. 
2. firmata-streams - An implementation of the firmata protocol in node.js streams.

In order to use this application you will need the following pieces of hardware. 

1. Android Phone - Most phones do support USB Host mode with the known exception of Huewai.
2. An On The Go (OTG) cable http://www.expansys.ie/expansys-micro-usb-to-usb-otg-adapter-cable-234050/
3. Arduino Uno R3 - With the standard firmata installed  

## Install 
Check out and build the [Relay01 Repo](git@github.com:No9/Relay01.git)
Install the apk onto your android device. 

Install the Standard Firmata Sketch onto the Arduino

Check out R2993 
```
$ git clone git@github.com:No9/R2993.git
$ cd R2993
$ npm install 
```

## Run 

Connect the Arduino to your Android using the OTG cable. 
Start the Relay01 application and click the "Start Button"
The IP address on you phone is displayed in the UI along with the connection status. 

```
$ cd R2993/test 
$ node tcpledtest.js 
```
This should make the light blink. 