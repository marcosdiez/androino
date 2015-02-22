// LED multiplexing

#include <SoftModem.h>
#include <ctype.h> 
SoftModem modem; 

#define LEDTime 1
word num=0;
word play=0;
int RedLEDPins[] = {7,6,5};
int GreenLEDPins[] = {11,12,13};
int CathodePins[] = {10,9,8};
int AIN[]={A0,A1,A2};

void setup()
{
  Serial.begin(9600);
  //  modem.begin();
  for (int i=0;i<3;i++)
  {
    pinMode(AIN[i],INPUT);
    //digitalWrite(AIN[i],LOW);
  }
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
    7, 56, 73, 84, 146, 273, 292, 448            };  

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



void loop()
{
  word WinCondition = 0;
  byte NoOfTurns = 0;
  word LedOnOff = 0;
  word LedColour = 0;
  boolean Turn = 1;       // 1 = red's turn, 0 = green's turn



  do
  {
    play=readButton();
    // Serial.println(play);
    if( play & ~LedOnOff)
    { 
      LedOnOff = LedOnOff | play;  // light up the space
      //-> Envio del movimiento realizado.
      //
      if (Turn)                         // set colour according to whose turn it is
      {
        LedColour = LedColour | play;
      }

      Turn = !Turn;  //-> Escribir evento del turno y envio al server
      NoOfTurns+=1;

      WinCondition=checkWinner(LedOnOff, LedColour, Turn);      
    }

    //  <- Recepcion del evento de que ha hecho su movimiento. 

    lightLED(LedOnOff,LedColour);    // light up the LED    

  }  
  while ((WinCondition == 0) && (NoOfTurns < 9));


  if (WinCondition > 0)              // did anybody win?
  {
    displayWin(WinCondition, Turn);   
  } 
  else                            // it was a draw, fade out all lights
  {
    lightLED(0,512);
  }

}


















