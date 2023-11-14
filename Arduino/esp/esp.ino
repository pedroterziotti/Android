// this sample code provided by www.programmingboss.com
#define RXp2 16
#define TXp2 17

#include "BluetoothSerial.h"

//#define USE_PIN // Uncomment this to use PIN during pairing. The pin is specified on the line below
const char *pin = "1234"; // Change this to more secure PIN.

String device_name = "ESP32-BT-Slave";

#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth is not enabled! Please run `make menuconfig` to and enable it
#endif

#if !defined(CONFIG_BT_SPP_ENABLED)
#error Serial Bluetooth not available or not enabled. It is only available for the ESP32 chip.
#endif

BluetoothSerial SerialBT;


void setup() {
  // put your setup code here, to run once:
  //Serial.begin(115200);
  Serial2.begin(9600, SERIAL_8N1, RXp2, TXp2);
  SerialBT.begin(device_name); //Bluetooth device 
  #ifdef USE_PIN
    SerialBT.setPin(pin);
    Serial.println("Using PIN");
  #endif

}
void loop() {
  SerialBT.pri
    if(Serial2.available())
    {
      String word = Serial2.readString();
      //Serial.println(word);
      SerialBT.println(word);
    }
}