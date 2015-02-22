package org.androino.prototype;

public class AndroinoException extends RuntimeException {

	public static final int TYPE_FSK_DECODING_ERROR = 1001;
	public static final int TYPE_FSK_DEBUG = 1002;
	
	private int type;
	private Object debugInfo;
	
	public int getType(){
		return this.type;
	}
	
	public AndroinoException(String detailMessage, Throwable throwable, int type) {
		super(detailMessage, throwable);
		this.type = type;
	}

	public AndroinoException(String detailMessage, int type) {
		super(detailMessage);
		this.type = type;
	}
	void setDebugInfo(Object o){
		this.debugInfo = o;
	}
	Object getDebugInfo(){
		return this.debugInfo;
	}
}
