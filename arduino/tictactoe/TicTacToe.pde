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

// LED multiplexing

#include <SoftModem.h>
#include <ctype.h> 
SoftModem modem; 

#define LEDTime 1

#define A0 0
#define A1 1
#define A2 2

#define CODE_REPEAT_LAST_MESSAGE    20
#define CODE_ACK_MESSAGE            21
#define MESSAGE_CHECKSUM_ERROR      -2
#define PARITY_EVEN                 64
#define PARITY_ODD                  32
#define RETRY_MESSAGE_INTERVAL    2000 //ms

int lastMessageSent = -1;
long lastMessageTime = -1;
boolean lastMessageAckReceived = true;


#define BUTTON_CLICK_1          1
#define BUTTON_CLICK_2          2
#define BUTTON_CLICK_3          3
#define BUTTON_CLICK_4          4
#define BUTTON_CLICK_5          5
#define BUTTON_CLICK_6          6
#define BUTTON_CLICK_7          7
#define BUTTON_CLICK_8          8
#define BUTTON_CLICK_9          9

#define HANDSHAKE_START_MIN    24
#define HANDSHAKE_START_MAX    30



word num=0;
word ButtonPressed=0;
int RedLEDPins[] = {
  4,3,2};
int GreenLEDPins[] = {
  11,12,13};
int CathodePins[] = {
  10,9,8};
int AIN[]={
  A0,A1,A2};

void setup()
{
  Serial.begin(115200);
  Serial.println("Androino Tic-tac-toe");

  //  modem.begin();
  for (int i=0;i<4;i++)
  {
    pinMode(AIN[i],INPUT);
    //digitalWrite(AIN[i],LOW);
  }
  // initalize random generator
  randomSeed(analogRead(3));
  modem.begin ();
}

word readButton(){

  int voltage;
  int SelectRow = 0;
  int SelectCol = 0;

  for (int j=0;j<3;j++)
  {

    voltage = 0;

    for (int i=0;i<=10;i++)
    {
      voltage += analogRead(AIN[j]);
      if (analogRead(AIN[j])>0)
      {
        //        Serial.print("AIN");
        //        Serial.println(j);
        //        Serial.println(analogRead(AIN[j]));
      }
    }

    if (voltage>=671*11 && voltage<=677*11)
    {
      SelectCol=j+1;
      SelectRow=1; 
      break;      
    }

    else if (voltage>=324*11 && voltage<=328*11)
    {
      SelectCol=j+1;
      SelectRow=2;
      break;         
    }

    else if ( voltage<=164*11 && voltage>=160*11)
    {
      SelectCol=j+1;
      SelectRow=3;
      break;        
    }
  }




  if ((SelectRow > 0) && (SelectCol > 0))
  {
    //    Serial.println("Aqui estoy!!");
    num= (1 << (SelectRow+(SelectCol-1)*3)-1);
    //    Serial.println(num,BIN);  
    //    Serial.println(SelectCol);
    //    Serial.println(SelectRow);

    //Serial.println(num);


    return num;

  } 
  else
  {
    return (0);
  } 

}


void lightLED(word LEDOnOff, word LEDColour)
{
  // shift the bits to the right, turning on the LEDs whenever
  // there is a 1, turning off whenever there is a 0 in LEDOnOff
  // If the LED is lit, LEDColour determines which LED is lit
  // 1 is red, 0 is green

  for (int j=0;j<3;j++)
  {
    pinMode(RedLEDPins[j], INPUT);
    digitalWrite(RedLEDPins[j], LOW);
    pinMode(GreenLEDPins[j], INPUT);
    digitalWrite(GreenLEDPins[j], LOW);
    pinMode(CathodePins[j], INPUT);
    digitalWrite(CathodePins[j], LOW);
  }

  for (int i=0;i<9;i++)
  {
    if (LEDOnOff & 1)
    {     
      if (LEDColour & 1)
      {
        pinMode(RedLEDPins[i/3], OUTPUT);
        pinMode(CathodePins[i%3], OUTPUT);
        digitalWrite(RedLEDPins[i/3], HIGH);
        digitalWrite(CathodePins[i%3], LOW);

        delay(LEDTime);

        digitalWrite(RedLEDPins[i/3], LOW);
        pinMode(RedLEDPins[i/3], INPUT);
        pinMode(CathodePins[i%3], INPUT);
      } 
      else 
      {
        pinMode(GreenLEDPins[i/3], OUTPUT);
        pinMode(CathodePins[i%3], OUTPUT);
        digitalWrite(GreenLEDPins[i/3], HIGH);
        digitalWrite(CathodePins[i%3], LOW);

        delay(LEDTime);

        digitalWrite(GreenLEDPins[i/3], LOW);
        pinMode(GreenLEDPins[i/3], INPUT);
        pinMode(CathodePins[i%3], INPUT);        
      }

    }
    LEDOnOff = LEDOnOff >> 1;
    LEDColour = LEDColour >> 1;
  }

}

word checkWinner(word GridOnOff, word GridColour, boolean Turn)
{


  word winArray[] = {
    7, 56, 73, 84, 146, 273, 292, 448                                                                      };  

  if (Turn)        // red's turn, check for green
  {
    for (int i=0;i<8;i++)
    {
      if ( ((GridOnOff & ~GridColour) & winArray[i]) == winArray[i])
      {
        return winArray[i];
      }
    }
    return 0;
  } 
  else        // green's turn, check for red
  {
    for (int i=0;i<8;i++)
    {
      if ( ((GridOnOff & GridColour) & winArray[i]) == winArray[i])
      {      
        return winArray[i];
      }
    }
    return 0;
  }
}

void displayWin(word winCondition, boolean Turn)
{
  word winColour;

  if (Turn)
  {
    winColour = 0;
  } 
  else
  {
    winColour = 511;
  }  
  for (int i=256;i>=0;i--)
  {
    lightLED(winCondition, winColour);  // light up the winning combo
    //    lightLEDPWM(~winCondition, ~winColour, i);  // fade out the other colour
  }

  for (int i=0;i<10;i++)      // blink winning combo a few times
  {
    lightLED(winCondition, winColour);
    delay(100);
  }    
}
void sending(word ButtonPressed)
{
  //void sending(uint8_t (ButtonPressed))
  //{

  //  int move=uint8_t (ButtonPressed);
  //  modem.write(ButtonPressed); -> 64=@

  Serial.println(ButtonPressed);
  if(ButtonPressed==1)
  {
    sendMessage(BUTTON_CLICK_1,true);

  }
  else if(ButtonPressed==2)
  {
    sendMessage(BUTTON_CLICK_2,true);
  }
  else if(ButtonPressed==4)
  {
    sendMessage(BUTTON_CLICK_3,true);
  }
  else if(ButtonPressed==8)
  {
    sendMessage(BUTTON_CLICK_4,true);
  }
  else if(ButtonPressed==16)
  {
    sendMessage(BUTTON_CLICK_5,true);
  }
  else if(ButtonPressed==32)
  {
    sendMessage(BUTTON_CLICK_6,true);
  }
  else if(ButtonPressed==64)
  {
    sendMessage(BUTTON_CLICK_7,true);
  }
  else if(ButtonPressed==128)
  {
    sendMessage(BUTTON_CLICK_8,true);
  }
  else if(ButtonPressed==256)
  {
    sendMessage(BUTTON_CLICK_9,true);
  }

  delay(100);
}


void loop()
{

  byte NoOfTurns = 0;
  word LedOnOff = 0;
  word LedColour = 0;
  //boolean Turn=1;
  boolean Turn=startHandshake(); // this call blocks until negotiation is completed
  word WinCondition=0;


  while (1)
  {
    // send message again if no ACK is received
    reSendMessageLoop();

    if ((WinCondition == 0) && (NoOfTurns < 9))
    {
      switch(Turn){
      case 0:
        ButtonPressed=readButton();   // take a reading

        if(ButtonPressed>0)
        {
          sending(ButtonPressed);
        }


        if( ButtonPressed & ~LedOnOff)   // if an empty space is selected
        { 
          LedOnOff = LedOnOff | ButtonPressed;
          Turn = !Turn;  
          NoOfTurns+=1;
          WinCondition=checkWinner(LedOnOff, LedColour, Turn);  
        }
        // Recepcion del evento de que ha hecho su movimiento. 
        lightLED(LedOnOff,LedColour);    // light up the LED    
        break;


      case 1:
        word array[2];

        word c=0;
        word a=0;
        while (modem. available ())// check that data received from phone
        {
          c = modem. read (); 
          Serial.print("modem available:"); 
          Serial.print(c,DEC); 
          Serial.print(":");
          Serial.println(c,BIN);
          int msg = c;
          int number = messageReceived(msg); 
          Serial.print("decoded:"); 
          Serial.print(number,DEC); 
          Serial.print(":");
          Serial.println(number,BIN);
          if (number>-1) {
            switch(number){
            case 1:
              a=1;
              break;
            case 2:
              a=2;
              break;
            case 3:
              a=4;
              break;
            case 4:
              a=8;
              break;
            case 5:
              a=16;
              break;
            case 6:
              a=32;
              break;
            case 7:
              a=64;
              break;
            case 8:
              a=128;
              break;
            case 9:
              a=256;
              break;           
            }
          }

        }
        if( a & ~LedOnOff)   // if an empty space is selected
        {

          LedOnOff = LedOnOff | a;
          LedColour = LedColour | a;



          Turn = !Turn;  
          NoOfTurns+=1;

          WinCondition=checkWinner(LedOnOff, LedColour, Turn);   
        }   




        lightLED(LedOnOff,LedColour);    // light up the LED    


      }
    }  

    else {
      break;
    }
  }



  if (WinCondition > 0)              // did anybody win?
  {


    displayWin(WinCondition, Turn);  

  } 
  else if(NoOfTurns= 9)                            // it was a draw, fade out all lights
  {
    for (int i=0;i<150;i++)      // blink winning combo a few times
    {

      lightLED(LedOnOff,LedColour);

    }    


    for (int i=0;i<10;i++)      // blink winning combo a few times
    {

      lightLED(LedOnOff,LedColour);
      delay(100);
    }    



  }

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
  Serial.print("     encodeMessage:number="); 
  Serial.print(number, DEC); 
  Serial.print(":"); 
  Serial.println(number, BIN);
  Serial.print("     encodeMessage:chk="); 
  Serial.print(cSum, DEC); 
  Serial.print(":"); 
  Serial.println(cSum, BIN);
  Serial.print("     encodeMessage:message="); 
  Serial.print(msg, DEC); 
  Serial.print(":"); 
  Serial.println(msg, BIN);
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
  Serial.print("     decodeMessage:"); 
  Serial.print(message, DEC); 
  Serial.print(":"); 
  Serial.println(message, BIN);
  Serial.print("     number="); 
  Serial.print(number, DEC); 
  Serial.print(":"); 
  Serial.println(number, BIN);
  Serial.print("     chk="); 
  Serial.print(chk, DEC); 
  Serial.print(":"); 
  Serial.println(chk, BIN);
  Serial.print("     cSum="); 
  Serial.print(cSum, DEC); 
  Serial.print(":"); 
  Serial.println(cSum, BIN);

  if ( chk != cSum) {
    return MESSAGE_CHECKSUM_ERROR; // erroneus message received
  } 
  else
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


boolean startHandshake(){
  // algorithm explained:
  // I generated a number and wait to receive my opponent number
  // if bigger => I start and send the max number
  // if smaller => My opponenet stars and I send the min number
  // if equals regenerate my number and repeat handshake

    boolean Turn;
  int opponentNumber = -1;
  boolean handshake = false;
  int myNumber=random(HANDSHAKE_START_MIN+1, HANDSHAKE_START_MAX-1);
  Serial.println("------------------------------------"); 
  Serial.print("startHandshake:n="); 
  Serial.println(myNumber);
  sendMessage(myNumber,true);  
  do {
  Serial.println("------------------------------------"); 
    // send message again if no ACK is received
    reSendMessageLoop();
    if ( modem.available() ){
      word c = modem.read();
      opponentNumber = messageReceived(c); 
      if ( opponentNumber <= HANDSHAKE_START_MIN || opponentNumber >= HANDSHAKE_START_MAX ) 
        opponentNumber = -1; // skip non-handshaking messages
    }
    if (opponentNumber>0) {
      Serial.print("startHandshake:opponent n=");
      Serial.println(opponentNumber);
      // response received
      if (myNumber > opponentNumber) { // I starts
        sendMessage(HANDSHAKE_START_MAX, true);
        handshake = true;
        Turn=0;
        for (int i=0;i<200;i++) {      // blinking waiting for your first movement
          lightLED(511,0);
          delay(100);
        } 
      }
      if (myNumber < opponentNumber) { // My opponent starts
        sendMessage(HANDSHAKE_START_MIN, true);
        handshake = true;
        Turn=1;
        for (int i=0;i<400;i++){      // lights your colour waitting for the first movement of your opponent
          lightLED(511,0);
        }
      }
      if (myNumber == opponentNumber) { // Regenerate my number
        myNumber = random(HANDSHAKE_START_MIN+1, HANDSHAKE_START_MAX-1);
        Serial.print("startHandshake: regenerate n=");
        Serial.println(myNumber);
        sendMessage(myNumber,true);  
      }
      opponentNumber = -1; // reset opponent number
    } 
    else {
      delay(2000);
    }
  } 
  while (!handshake);
  Serial.print("startHandshake: end Turn="); Serial.println(Turn);
  return Turn;
}

