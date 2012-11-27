/*
 Mozobi Prototype
 
 Reads IDTECH SPI Mag Head and Chip & Pin card present switch.
 Pushes Mag Stripe Data onto USB Serial Port.
 
 created 25 Oct 2012
 by Mark Leyden
 */

#include <SPI.h>
#include <avr/io.h>
#include <avr/interrupt.h>
#include <util/delay.h>

#define MAG_CS 10
#define MAG_DAV 2
#define CARD_PRESENT 3

#define TRACK_1_MAX_LENGTH  79
#define TRACK_2_MAX_LENGTH  40

#define IDLE 0xFF

// Card Present switch is normally closed
int switchState;            // the current reading from the input pin
int lastSwitchState = LOW;  // the previous reading from the input pin
unsigned char card_present = LOW;

long lastDebounceTime = 0;  // the last time the output pin was toggled
long debounceDelay = 50;    // the debounce time; increase if the output flickers

void setup() {
  // initalize the  data ready and chip select pins:
  pinMode(MAG_DAV, INPUT_PULLUP);
  pinMode(MAG_CS, OUTPUT);
  
  pinMode(CARD_PRESENT, INPUT_PULLUP);
  
  Serial.begin(9600);
  
  // start the SPI library:
  SPI.begin();
  SPI.setBitOrder(MSBFIRST);
  SPI.setDataMode(SPI_MODE0);
  SPI.setClockDivider(SPI_CLOCK_DIV128);
  
  sei();
  
  Serial.print("\nHELO\n");
  // Ignore MAG Reader for 1 sec after reset
  delay(1000);
}

void loop() {

  // Don't do anything until the data ready pin is high:
  if (digitalRead(MAG_DAV) == HIGH) {
        
    // Take the chip select low to select the device:
    digitalWrite(MAG_CS, LOW);
    
    // Send IDLE data to read the first byte returned:
    unsigned char val = SPI.transfer(IDLE);
    delay(40);
    
    while (val == IDLE) {
      val = SPI.transfer(IDLE);
      delay(40);
      if (val != '%')
        val = IDLE;      // Ignore, because we don't have an STX
    }
    
    unsigned int i = 0;
    while (i < (TRACK_1_MAX_LENGTH+TRACK_2_MAX_LENGTH)) {
      Serial.write(val);
      i++;
      val = SPI.transfer(IDLE);
      delay(40);
      if (val == IDLE)
        break;
    }
    
    // take the chip select high to de-select:
    digitalWrite(MAG_CS, HIGH);
    Serial.print("\n");
  }
  
  // Now, check if the card is in the slot
  int pin = digitalRead(CARD_PRESENT);
  
  if (pin != lastSwitchState) {
    // reset the debouncing timer
    lastDebounceTime = millis();
  }
  
  if ((millis() - lastDebounceTime) > debounceDelay) {
    // whatever the reading is at, it's been there for longer
    // than the debounce delay, so take it as the actual current state:
    switchState = pin;
    if (switchState == LOW) {
      card_present = LOW;
    }
  }
  
  if ((switchState == HIGH) & (card_present == LOW)) {
    Serial.print("CARD PRESENT\n");
    card_present = HIGH;
  }
  // save the reading.  Next time through the loop,
  // it'll be the lastButtonState:
  lastSwitchState = pin;
}
