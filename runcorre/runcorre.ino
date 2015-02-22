/*
  RunCorre
 
 RunCorre game. 
 
 */


#define S_READY        0
#define S_GAME_STARTS  1
#define S_CHALLENGE    2
#define S_RESPONSE     3
#define S_GAME_ENDS    4

#define CHALLENGE_TIMEOUT  1000
#define BLINK_LED_INTERVAL 500

int STATE = S_READY; //initial state

// internal variables
unsigned long timeStartChallenge;
unsigned long timeStartLed;
int challenge = 1;
boolean blinkLedState = false;

// pin mapping
int inputPin = A0;
int ALedPin = 10;
int ABuzzerPin = 11;
int BLedPin = 12;
int BBuzzerPin = 13;

int led = 13;


// the setup routine runs once when you press reset:
void setup() {
  // initialize serial communication at 9600 bits per second:
  Serial.begin(9600);

  // pin intialization
  pinMode(ALedPin, OUTPUT);  
  pinMode(ABuzzerPin, OUTPUT);  
  pinMode(BLedPin, OUTPUT);  
  pinMode(BBuzzerPin, OUTPUT);  
  debugMessage("RunCorre Version 0.0");
}

// the loop routine runs over and over again forever:
void loop() {
  stateMachine();
  //  delay(1000);
}

// main game state machine 
void stateMachine(){
  //  Serial.print("stateMachine: STATE="); Serial.println(STATE);
  int response = 0;
  switch (STATE) {
  case S_READY:
    //initialization checks
    setState(S_GAME_STARTS);
    //  Serial.print("stateMachine: READY STATE="); Serial.println(STATE);
    break;
  case S_GAME_STARTS:
    // game start tune
    playStartGameTune();
    setState(S_CHALLENGE);
    break;
  case S_CHALLENGE:
    // launch challenge
    launchChallenge();
    setState(S_RESPONSE);
    break;
  case S_RESPONSE:
    // play challenge
    playChallenge(challenge);
    // wait for response or timeout
    response = checkResponse();
    //  Serial.print("stateMachine: response="); Serial.println(response);
    if (response == 1) { // solved, go for next challenge
      setState(S_CHALLENGE);
    };
    if (response == -1) { // timeout or failure, game ends
      setState(S_GAME_ENDS);
    };
    break;
  case S_GAME_ENDS:
    // you loose tune and back to the beginning
    playEndGameTune();
    setState(S_READY);
    break;
  }
  //  Serial.print("stateMachine: >>STATE="); Serial.println(STATE);
}

void setState(int state){
  Serial.print("setState: >>STATE="); 
  Serial.print(STATE);
  Serial.print(">>");
  Serial.println(state);
  STATE = state;
}

void playStartGameTune(){
  debugMessage("playStartTune");
  dummyTune();
}
void playEndGameTune(){
  debugMessage("playEndTune");
  dummyTune();
}
void playChallenge(int id){
  int pin = ALedPin;
  int buzzer = ABuzzerPin;
  if (id == 2) {
    pin = BLedPin;
    buzzer = BBuzzerPin;
  }
  playASynchro(pin, buzzer);
}

void playASynchro(int pin, int buzzer){
  unsigned long t = millis();
  long counter =  t-timeStartLed; 
  if (counter > BLINK_LED_INTERVAL)  {
    timeStartLed = millis();
    if ( blinkLedState ) {
      blinkLedState = false;
      digitalWrite(pin, LOW);
      tone(buzzer, 1000);
    } 
    else {
      blinkLedState = true;
      digitalWrite(pin, HIGH);
      tone(buzzer, 3000);
    }   
  };

}

void playSynchro(int pin, int buzzer){
  tone(buzzer, 3000, 2000);
  for (int i=0; i<10; i++){
    digitalWrite(pin, HIGH);
    delay(200);
    digitalWrite(pin, LOW);
    delay(200);
  }
}


void dummyTune(){
  digitalWrite(led, HIGH);
  delay(1000);
  digitalWrite(led, LOW);
  delay(1000);    
}


void launchChallenge(){
  long n = random(1,3);
  challenge = n;
  Serial.print("launchChallenge: challenge="); 
  Serial.println(challenge);
  timeStartChallenge = millis(); //start timeout counter
  timeStartLed = timeStartChallenge; // led counter
  blinkLedState = true;
  resetPlayChallenge();
}

void resetPlayChallenge(){
  for(int i=0; i<4; i=i+2){
    digitalWrite(ALedPin +i, LOW);  
    noTone(ABuzzerPin +i);
  }
}

int checkResponse(){
  // wait for user response, returns
  // 0: no response
  // 1: ok
  // -1: timeout or failure
  //  if (isTimeOut) return -1;
  int response = readUserInput();
  if (response == challenge) {
    debugMessage(String( "checkResponse:" + response));
    return 1;
  } 
  else 
    return 0;
}
boolean isTimeOut(){
  unsigned long t = millis();
  long counter =  t-timeStartChallenge; 
  if (counter > CHALLENGE_TIMEOUT) return true;
  else false;
}

int readUserInput(){
  // returns 0(no input), 1(A button), 2(B button)
  int userInput = 0;
  // read analog inputs 
  float meanValue = 0;
  int nSamples = 10;
  for (int i = 0; i < nSamples; i++)  {  
    meanValue += analogRead(inputPin); // 0-1023
  }
  meanValue = meanValue/nSamples;
  //  Serial.print("readUserInput: meanValue="); Serial.println(meanValue);
  if ( 80 < meanValue && meanValue < 500 ) {
    userInput = 2;
    debugMessage("userInput:2");
  }
  if ( 500 < meanValue && meanValue < 900 ) {
    userInput = 1;
    debugMessage("userInput:1");
  }
  return userInput;  
}

void debugMessage(String message){
  Serial.println(message);
}

