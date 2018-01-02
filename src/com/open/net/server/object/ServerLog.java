package com.open.net.server.object;

public final class ServerLog {

	private static ServerLog INS = new ServerLog();
	private ServerLog(){}
	
	public static ServerLog getIns(){
		return INS;
	}
	
	//-------------------------------------------------
	private LogListener mLogListener;
	
	public void log(String tag, String msg){
		if(null != mLogListener){
			mLogListener.onLog(tag, msg);
		}
	};

	public void setLogListener(LogListener mLogListener){
		this.mLogListener = mLogListener;
	}
	
	//-------------------------------------------------
	public static interface LogListener{
		public void onLog(String tag, String msg);
	}
	
}
