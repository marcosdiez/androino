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

package org.androino.tttserver.client;

import java.util.StringTokenizer;

public class TTTEvent {

	public static final int TYPE_SERVER_ERROR = -1;
	public static final int TYPE_BUTTON_CLICK = 0;
	public static final int TYPE_STARTGAME_CLICK = 1;
	public static final int TYPE_CONNECT = 2;
	public static final int TYPE_DISCONNECT = 3;
	public static final int TYPE_ENDGAME = 4;

	private int type;
	private String message;

	public TTTEvent(int type, String message) {
		this.type = type;
		this.message = message;
	}

	public static TTTEvent parseEvent(String token) {
		// expected TEXT:TS
		StringTokenizer sT = new StringTokenizer(token, ":");
		String message = sT.nextToken();
		int t = TYPE_SERVER_ERROR;
		if (message.startsWith("CONNECT"))
			t = TYPE_CONNECT;
		else if (message.startsWith("B_"))
			t = TYPE_BUTTON_CLICK;
		else if (message.startsWith("START"))
			t = TYPE_STARTGAME_CLICK;
		else if (message.startsWith("DISCONNECT"))
			t = TYPE_DISCONNECT;
		else if (message.startsWith("END_"))
			t = TYPE_ENDGAME;
		int index = message.indexOf("_");
		if (index > -1)
			message = message.substring(index + 1, message.length() - index + 1);
		else 
			message = "0";
		TTTEvent event = new TTTEvent(t, message);
		return event;
	}

	protected String getEncodedMessage() {
		String msg = "";
		switch (this.getType()) {
		case TYPE_SERVER_ERROR:
			msg = "ERROR_" + this.message;
			break;
		case TYPE_BUTTON_CLICK:
			msg = "B_" + this.message;
			break;
		case TYPE_STARTGAME_CLICK:
			msg = "START";
			break;
		case TYPE_CONNECT:
			msg = "CONNECT";
			break;
		case TYPE_DISCONNECT:
			msg = "DISCONNECT";
			break;
		case TYPE_ENDGAME:
			msg = "END_" + this.message;
			break;
		}
		return msg;
	}

	public int getType() {
		return type;
	}

	public String getMessage() {
		return message;
	}

	public String toString() {
		return super.toString() + " type=" + this.type + " msg=" + this.message;
	}

}
