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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/*
 * NO:TS		| NO:TS
 * CONNECT:TS	| NO:TS
 * CONNECT:TS	| CONNECT:TS
 * BUTTON_A3:TS	|
 * 
 */
public class ServerConnection extends Thread {
	private TTTServer mClientHandler;
	private boolean forceStop = false;
	private String server = "http://androino-tic-tac-toe.appspot.com";
	private static String	NEW_EVENT_CGI 			= "/new_event";
	private static String	CHECK_EVENTS_CGI 		= "/check_events";
	
	private String user = null;

	public ServerConnection(TTTServer handler) {
		this.mClientHandler = handler;
	}

	public void run() {
		connect();
		
		this.forceStop = false;
		while (true) {
			if (this.forceStop)	break;
			try {
				checkNewMessages();
				Thread.sleep(1000*1);
				//TTTServer.debugMessage("ServerConnection: loop");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	private void connect(){
		try {
			String url = this.server + "/" + NEW_EVENT_CGI + "?MSG=CONNECT";
			String content = getURLContent(url);
			this.user = content;
		} catch (IOException e) {
			this.mClientHandler.notifyEvent("ERROR:" + e.getMessage());
			e.printStackTrace();
		}
		
	}
	
	private void checkNewMessages(){
		try {
			String url = this.server + "/" + CHECK_EVENTS_CGI + "?USER="+this.user;
			String content = getURLContent(url);
			if (! content.startsWith("OK")){
				this.mClientHandler.notifyEvent(content);
			}
			TTTServer.debugMessage("ServerConnection: content=" + content);
		} catch (IOException e) {
			this.mClientHandler.notifyEvent("ERROR:" + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void stopAndClean(){
		this.forceStop = true;
	}
	
	public String sentEvent(TTTEvent event){
		String url = this.server + "/" + NEW_EVENT_CGI + "?USER=" + this.user + "&MSG=" + event.getEncodedMessage();
		try {
			String content = getURLContent(url);
			return content;
		} catch (IOException e) {
			e.printStackTrace();
			this.mClientHandler.notifyEvent("ERROR:" + e.getMessage());
			return null;
		}
	}

	/*
	 * Returns a String with content of the specified URL.
	 */
	public static String getURLContent(String url) throws IOException {
		// code snippet from http://www.java2s.com/Code/Java/Network-Protocol/UseBufferedReadertoreadcontentfromaURL.htm
		URL u = new URL(url);
		HttpURLConnection urlConnection = (HttpURLConnection)u.openConnection();
	    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
	    String urlString = "";
	    String current;
	    while ((current = in.readLine()) != null) {
	      urlString += current;
	    }
	    in.close();
	    return urlString;
	}
	

}
