# FWSE01 project

## Goal
Complete a kernel module and control the device with the module via Android app.

## Components

### RemoteLight
- Control the device via JNI.
- Send and receive packet from gateway.

### Gateway
- Forward the packet from IoT device to the Internet and from the Internet to IoT device.
- Does not complete the Internet side.

### kernel module
- Control the device in kernel space.
- use `insmod` to install the module.
