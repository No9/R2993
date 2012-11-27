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

void setup() {
  // initalize the  data ready and chip select pins:
  pinMode(MAG_DAV, INPUT_PULLUP);
  pinMode(MAG_CS, OUTPUT);
  
  Serial.begin(9600);
  
  // start the SPI library:
  SPI.begin();
  SPI.setBitOrder(MSBFIRST);
  SPI.setDataMode(SPI_MODE0);
  SPI.setClockDivider(SPI_CLOCK_DIV128);
  
  sei();
  
  Serial.print("HELO\n");
  // Ignore MAG Reader for 1 sec after reset
  delay(1000);
}

void loop() {
  unsigned char val, card_present;
  unsigned int i;
  
  card_present = 0;
  
  // don't do anything until the data ready pin is high:
  if (digitalRead(MAG_DAV) == HIGH) {
        
    // take the chip select low to select the device:
    digitalWrite(MAG_CS, LOW);
    
    // send IDLE data to read the first byte returned:
    val = SPI.transfer(IDLE);
    delay(40);
    
    while (val == IDLE) {
      val = SPI.transfer(IDLE);
      delay(40);
      if (val != '%')
        val = IDLE;      // Ignore, because we don't have an STX
    }
    
    i = 0;
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
  
  if ((digitalRead(CARD_PRESENT) == HIGH) & card_present == 0) {
    card_present = 1;
    Serial.print("CARD_PRESENT\n");
  } else {
    card_present = 0;
  }
}
