/*		
* Copyright (C) 2011 Androino authors		
*		
* Licensed under the Apache License, Version 2.0 (the "License");		
* you may not use this file except in compliance with the License.		
* You may obtain a copy of the License at		
*		
*      http://www.apache.org/licenses/LICENSE-2.0		
*		
* Unless required by applicable law or agreed to in writing, software		
* distributed under the License is distributed on an "AS IS" BASIS,		
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.		
* See the License for the specific language governing permissions and		
* limitations under the License.		
*/

#include <SoftModem.h>  
#include <ctype.h> 

#define CODE_REPEAT_LAST_MESSAGE    20
#define CODE_ACK_MESSAGE            21
#define MESSAGE_CHECKSUM_ERROR      -2
#define PARITY_EVEN                 64
#define PARITY_ODD                  32
#define RETRY_MESSAGE_INTERVAL    5000 //ms

int lastMessageSent = -1;
long lastMessageTime = -1;
boolean lastMessageAckReceived = true;

SoftModem modem; //create an instance of SoftModem 

void setup () 
{ 
  Serial.begin (115200); //permite 315bps en emision y recepcion
  Serial.println("Androino SoftTerm: Enter one character: [0,1,2,9,:,;,<,=,>,?,@,A,B,C,...,O]");
  Serial.println("D= repeat, E=ACK code");
  modem.begin (); // setup () call to begin with 
}  

void loop () 
{ 
  while (modem. available ())// check that data received from phone
  {
    int c = modem. read (); // 1byte Reed 
    Serial.print("Received message:"); Serial.print(c, DEC); Serial.print(":"); Serial.println(c, BIN);
    int number = messageReceived(c);
    if (number>-1) {
      // information received from android
    }
  }
  if (Serial.available()) { // data received from the PC
    while(Serial.available() ){
      // send data character by character
      int c = Serial.read();
      Serial.print("Sending character:"); Serial.print(c, DEC); Serial.print(":"); Serial.println(c, BIN);
      //modem.write(c);
      sendMessage(c-48, true); // 48=0, 49=1
    }
   }
   // send message again if no ACK is received
   reSendMessageLoop();
}

//-----------------------------------------
// ERROR DETECTION
// http://en.wikipedia.org/wiki/Error_detection_and_correction
//-----------------------------------------

void sendMessage(int number, boolean persistent){
  // encodes and sends the message to the modem
  // number must [0,16]
  int msg = encodeMessage(number);
  modem.write(msg);
  if (persistent) {
    lastMessageSent = number;
    lastMessageTime = millis();
    lastMessageAckReceived = false;
  }
}

int encodeMessage(int number){
  // adds the checksum
  // Example: 3 (000.00011) => (101.00011)
  int cSum = checkSum(number);
  int msg = number + cSum;
Serial.print("     encodeMessage:number="); Serial.print(number, DEC); Serial.print(":"); Serial.println(number, BIN);
Serial.print("     encodeMessage:chk="); Serial.print(cSum, DEC); Serial.print(":"); Serial.println(cSum, BIN);
Serial.print("     encodeMessage:message="); Serial.print(msg, DEC); Serial.print(":"); Serial.println(msg, BIN);
  return msg;
}

int checkSum(int number){
  // calculates the checkSum for error correction
  // simple implementation even => 010, odd =>001
  int sign = 1;
  for (int i=0; i < 5; i++){
    int b = bitRead(number, i);
    if (b==1){
      sign = sign * (-1);
    }
  }
  if (sign>0)
    return PARITY_EVEN;
  else 
    return PARITY_ODD; 
}

int decodeMessage(int message){
  // Message format: 111.11111 (3bits=checksum 5bits=information)
  int number = B00011111 & message; //extract number from message 
  int chk =    B11100000 & message;  //extract checksum from message
  int cSum = checkSum(number);
Serial.print("     decodeMessage:"); Serial.print(message, DEC); Serial.print(":"); Serial.println(message, BIN);
Serial.print("     number="); Serial.print(number, DEC); Serial.print(":"); Serial.println(number, BIN);
Serial.print("     chk="); Serial.print(chk, DEC); Serial.print(":"); Serial.println(chk, BIN);
Serial.print("     cSum="); Serial.print(cSum, DEC); Serial.print(":"); Serial.println(cSum, BIN);
  
  if ( chk != cSum) {
    return MESSAGE_CHECKSUM_ERROR; // erroneus message received
  } else
    return number;
}

int messageReceived(int message){
  // process the received messages, if transmission error ask for a repetition
  // if info received returns a positive number oherwise a negative number is returned

  int number = decodeMessage(message);
  int last = lastMessageSent;
  switch (number) {
    case MESSAGE_CHECKSUM_ERROR:
      // reception error, ask for a repetition of the message
      sendMessage(CODE_REPEAT_LAST_MESSAGE, false);
      lastMessageSent = last;
      number = -1;
      break;
    case CODE_REPEAT_LAST_MESSAGE:
      // repetition required
      sendMessage(lastMessageSent, true);
      number = -1;
      break;
    case CODE_ACK_MESSAGE:
      lastMessageAckReceived = true;
      number = -1;
      break;
  }
  return number;
}

void reSendMessageLoop(){
  // after retry interval, if no ack is received the last msg is sent again
  if (!lastMessageAckReceived) {
    long time = millis();
    if ( (time-lastMessageTime) > RETRY_MESSAGE_INTERVAL ) {
      // retry send message
      sendMessage(lastMessageSent, true);  
    }
  }
}

