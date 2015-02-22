package org.androino.prototype;

import android.os.Handler;
import android.util.Log;

public class ArduinoService implements Runnable {

	public static final int HANDLER_MSG_RECEIVED = 1;
	public static final int HANDLER_MSG_STOPPED = 2;

	protected Handler mClientHandler;
	private int mCounter =0;
	protected boolean forceStop = false;

	public ArduinoService(Handler handler) {
		this.mClientHandler = handler;
	}

	@Override
	public void run() {
		this.forceStop = false;
		// AUDIO RECORDING
		while (true) {
			if (this.forceStop)	break;
			try {
				this.mCounter +=100;
				sendMessage(this.mCounter, 0);
				Thread.sleep(1000*10);
			} catch (InterruptedException e) {
				Log.e("ArduinoService:run", "error", e);
				e.printStackTrace();
			}
		}
		sendStopMessage();

	}

	public void write(String message) {
		Log.i("ArduinoService::MSG", message);
		this.mCounter += Integer.parseInt(message);
	}


	protected void sendMessage(int arg1, int arg2){
		int bData = 0;
		if (mClientHandler != null) {
			//int arg2 = bData & 0xFF;
			this.mClientHandler.obtainMessage(HANDLER_MSG_RECEIVED, arg1, arg2)
					.sendToTarget();
		}
	}
	private void sendStopMessage(){
		if (mClientHandler != null)
			this.mClientHandler.obtainMessage(HANDLER_MSG_STOPPED, 0, 0).sendToTarget();
		
	}
	public void stopAndClean(){
		this.forceStop = true;
	}

}
