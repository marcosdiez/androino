package org.androino.prototype.client;

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
		server.debugMessage("TTTServer instance");
		server.registerEventListener(server);
		server.start();
		server.debugMessage("TTTServer start");
		server.buttonClick("A3");
		server.debugMessage("TTTServer button click");
		try {
			Thread.sleep(100*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		server.stop();
		server.debugMessage("TTTServer stop");
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
