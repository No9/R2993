/*
  Serial Event example

 Based on:

 http://www.arduino.cc/en/Tutorial/SerialEvent

 */
//int in = 0;
void setup() {
  Serial.begin(115200);
  
  //0x4138
}

void loop() {
 
  //if (Serial.available() > 0) {
                // read the incoming byte:
                int in = Serial.read();
  //}
   Serial.write(in);
   Serial.write(45);
  
}

