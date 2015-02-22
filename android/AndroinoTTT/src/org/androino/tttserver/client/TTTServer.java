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

public class TTTServer implements iTTTEventListener{
	
	private static TTTServer instance;
	private iTTTEventListener listener;
	private ServerConnection server;

	private TTTServer(){
		this.server = new ServerConnection(this);
	}
	public static TTTServer getInstance(){
		if (instance == null){
			instance = new TTTServer();
		}
		return instance;
	}
	public void start(){
		this.server.start();
	}
	public void stop(){
		this.server.stopAndClean();
		instance = null;
	}
	
	protected void notifyEvent(String eventMessage){
		TTTEvent event = TTTEvent.parseEvent(eventMessage);
		if (this.listener !=null)
			listener.eventReceived(event);
	}
	
	
	public void registerEventListener(iTTTEventListener listener){
		// current implementation only allows a unique listener registration
		this.listener = listener;
	}
	public void unRegisterEventListener(iTTTEventListener listener){
		this.listener = null;
	}

	public void buttonClick(String buttonId){
		TTTEvent event = new TTTEvent(TTTEvent.TYPE_BUTTON_CLICK, buttonId);
		this.server.sentEvent(event);
	}
	public void startGameClick(){
		TTTEvent event = new TTTEvent(TTTEvent.TYPE_STARTGAME_CLICK, null);
		this.server.sentEvent(event);
	}
	public void endGame(String result){
		TTTEvent event = new TTTEvent(TTTEvent.TYPE_ENDGAME, result);
		this.server.sentEvent(event);
	}
	
	// test implementation
	
	public static void main(String[] args){
		TTTServer server = TTTServer.getInstance();
		debugMessage("TTTServer instance");
		server.registerEventListener(server);
		server.start();
		debugMessage("TTTServer start");
		server.buttonClick("A3");
		debugMessage("TTTServer button click");
		try {
			Thread.sleep(100*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		server.stop();
		debugMessage("TTTServer stop");
	}

	public void eventReceived(TTTEvent event) {
		debugMessage("Event received: " + event.toString() );
		switch (event.getType()) {
		case TTTEvent.TYPE_BUTTON_CLICK:
			this.buttonClick( "345");
			break;
		default:
			break;
		}
	}
	static void debugMessage(String msg){
		System.out.println(">>" + msg);
	}

}
