
#include <DMXSerial.h>

const int DMX_CHANNEL = 1;
const int PIN_RED_GREEN = 3;
const int PIN_BLUE = 4;


void setup() {
   DMXSerial.init(DMXReceiver);

    pinMode(13, OUTPUT);
    pinMode(PIN_RED_GREEN, OUTPUT);
    pinMode(PIN_BLUE, OUTPUT);
}

void loop() {
  unsigned long lastPacket = DMXSerial.noDataSince();

  if (lastPacket < 5000) {
    if (DMXSerial.read(DMX_CHANNEL)<100){
         digitalWrite(PIN_BLUE, LOW);
          digitalWrite(PIN_RED_GREEN, LOW);
        return;
    }
    if (DMXSerial.read(DMX_CHANNEL)<150){
        digitalWrite(PIN_BLUE, HIGH);
        digitalWrite(PIN_RED_GREEN, LOW);
        return;
    }
    if (DMXSerial.read(DMX_CHANNEL)<200){
        digitalWrite(PIN_BLUE, LOW);
        digitalWrite(PIN_RED_GREEN, HIGH);
        return;
    }

    digitalWrite(PIN_BLUE, HIGH);
    digitalWrite(PIN_RED_GREEN, HIGH);

  }
}
