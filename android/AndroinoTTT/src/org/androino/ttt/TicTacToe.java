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

package org.androino.ttt;

import org.androino.tttserver.client.TTTEvent;
import org.androino.tttserver.client.TTTServer;
import org.androino.tttserver.client.iTTTEventListener;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.RadioButton;

public class TicTacToe implements iTTTEventListener{

	private static final String TAG = "TicTacToe";
	// arduino messages
	// 	 [0-9] button events
	// [10-19] specific messages
	// [20-30] protocol codes
	
	public static final int ARDUINO_MSG_START_GAME 		= 10;
	public static final int ARDUINO_MSG_END_GAME_WINNER = 11;
	public static final int ARDUINO_MSG_END_GAME_LOSER 	= 12;
	public static final int ARDUINO_PROTOCOL_ARQ		= 20; //Automatic repeat request
	public static final int ARDUINO_PROTOCOL_ACK		= 21; //Message received acknowledgment

	public static final int LAST_MESSAGE_MAX_RETRY_TIMES= 5; //Number of times that last message is repeated
	public static final int CONSECUTIVE_CHK_ERROR_LIMIT = 1; //Number of consecutive checksum errors to send an ARQ
	
	public static final int HANDLER_MESSAGE_FROM_SERVER = 2001;

	
	private Handler mHandler;
	private ArduinoService mArduinoS;
	private TTTServer mServer;
	private MainActivity mActivity;
	private int lastMessage;
	private int lastMessageCounter=0;
	private int checksumErrorCounter=0;
	
	public TicTacToe(MainActivity activity){
		this.mActivity = activity;
		// 
		this.mHandler = new Handler() {
			public void handleMessage(Message msg) {
				messageReceived(msg);
			}

		};
	}
	public void stop(){
		Log.i(TAG, "stop()");
		((RadioButton) this.mActivity.findViewById(R.id.RadioButton01)).setChecked(false);
		
		// STOP the arduino service
		if (this.mArduinoS != null)
			this.mArduinoS.stopAndClean();
		// DISCONNECT from server
		if (this.mServer != null)
			this.mServer.stop();
	}

	public void start(){
		Log.i(TAG, "start()");
		((RadioButton) this.mActivity.findViewById(R.id.RadioButton01)).setChecked(true);

		// START the arduino service
		this.mArduinoS = new ArduinoService(this.mHandler);
		new Thread(this.mArduinoS).start();
		// CONNECT to the TTT Server
		// DEVELOPMENT:SERVERDISABLED	
		this.mServer = TTTServer.getInstance();
		this.mServer.registerEventListener(this);
		this.mServer.start();
	}
	
	@Override
	public void eventReceived(TTTEvent event) {
		Log.w(TAG, "tic-tac-toe:eventReceived(): type=" + event.getType()+ " message=" + event.getMessage());
		int value = Integer.parseInt(event.getMessage());
		this.mHandler.obtainMessage(HANDLER_MESSAGE_FROM_SERVER, value, event.getType()).sendToTarget();
	}
	
	private void messageReceived(Message msg) {
		int target = msg.what;
		int value = msg.arg1;
		int type = msg.arg2;
		Log.w(TAG, "tic-tac-toe:messageReceived(): target=" + target + " value=" + value + " type=" + type);
		
		switch (target) {
		case HANDLER_MESSAGE_FROM_SERVER:
			int msgCode = value;
			switch (type) {
				case TTTEvent.TYPE_BUTTON_CLICK:
					msgCode = value;
					this.lastMessage = msgCode;
					this.sendMessage(msgCode);
					break;
/*
				case TTTEvent.TYPE_STARTGAME_CLICK:
					msgCode = ARDUINO_MSG_START_GAME;
					break;
				case TTTEvent.TYPE_ENDGAME:
					if (value == 1) 
						msgCode = ARDUINO_MSG_END_GAME_WINNER;
					else 
						msgCode = ARDUINO_MSG_END_GAME_LOSER;
*/
				default:
					break;
			}
			this.mActivity.showDebugMessage("SERVER=" + msgCode, false);
			Log.i(TAG, "tic-tac-toe:messageReceived() from server value=" + msgCode);
			//this.lastMessage = msgCode;
			//this.sendMessage(msgCode);
			break;
		case ArduinoService.HANDLER_MESSAGE_FROM_ARDUINO:
			switch (value) {
				case ARDUINO_PROTOCOL_ARQ:
					checksumErrorCounter=0;
					sendLastMessage();
					break;
				case ErrorDetection.CHECKSUM_ERROR:
					checksumErrorCounter++;
					if (checksumErrorCounter>CONSECUTIVE_CHK_ERROR_LIMIT){
						checksumErrorCounter=0;
						sendMessage(ARDUINO_PROTOCOL_ARQ);//ARQ after two consecutive CHK ERROR received
					}
					break;
					
/*
				case ARDUINO_MSG_START_GAME:
					this.mServer.startGameClick();
					break;
				case ARDUINO_MSG_END_GAME_WINNER:
					this.mServer.endGame("0");
					break;
				case ARDUINO_MSG_END_GAME_LOSER:
					this.mServer.endGame("1");
					break;
				default:
					this.mServer.buttonClick(""+value);
					break;
*/
				default:
					checksumErrorCounter=0;
					this.mServer.buttonClick(""+value);
					Log.i(TAG, "tic-tac-toe:messageReceived() ACK send");
					this.sendMessage(ARDUINO_PROTOCOL_ACK);
					break;
			}
			
			this.mActivity.showDebugMessage("ARD: " + value, false);
			Log.i(TAG, "tic-tac-toe:messageReceived() from arduino value=" + value);
			break;
		default:
			//FIXME error happened handling messages
			break;
		}
	}

	private void sendLastMessage(){
		this.sendMessage(lastMessage);
		if (lastMessageCounter> LAST_MESSAGE_MAX_RETRY_TIMES) {
			// stop repeating last message, ERROR
			this.mActivity.showDebugMessage("ERROR MAX RETRY msg=" + this.lastMessage, false);
			Log.e(TAG, "tic-tac-toe:sendLastMessage() ERROR MAX RETRY value=" + lastMessage);
			this.sendMessage(ARDUINO_PROTOCOL_ACK); // send ack to avoid ARQ
			Log.e(TAG, "tic-tac-toe:sendLastMessage() ERROR MAX RETRY SENDING ACK instead");
			lastMessageCounter=0;
		} else {
			Log.i(TAG, "tic-tac-toe:sendLastMessage() value=" + lastMessage);
			this.sendMessage(lastMessage);
			lastMessageCounter++;
		}
	}

	private void sendMessage(int number){
		Log.i(TAG, "tic-tac-toe:sendMessage() number=" + number);
		this.mArduinoS.write(number);
	}
	
	protected void developmentSendMessage(int number){
		this.sendMessage(number);
	}
	protected void developmentSendServerMessage(int number){
		this.mServer.buttonClick(""+number);
	}
	
	
}
