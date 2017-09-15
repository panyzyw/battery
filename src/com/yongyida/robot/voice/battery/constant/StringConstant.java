package com.yongyida.robot.voice.battery.constant;

import com.yongyida.robot.voice.battery.app.MyApplication;

public class StringConstant {

	public static String voicer = "aisxa";
	public final static String LOWPOWERPATH 	= MyApplication.isRS20 ? "lowPower_rs.mp3" : "lowPower.mp3";
	public final static String CONNECTPATH 		= MyApplication.isRS20 ? "connect_rs.mp3" : "connect.mp3";
	public final static String DISCONNECTPATH 	= MyApplication.isRS20 ? "disconnect_rs.mp3" : "disconnect.mp3";
	public final static String FULLPATH	 		= MyApplication.isRS20 ? "fullPower_rs.mp3" : "fullPower.mp3";
	public final static String COMPLATEPATH 	= MyApplication.isRS20 ? "complatePower_rs.mp3" : "complatePower.mp3";
	
	public final static String LOWPOWE = "lower";
	public final static String CONNECT = "connect";
	public final static String DISCONNECT = "disconnect";
	public final static String FULL = "full";
	public final static String POWERSTATUS = "powerStatus";
	
	public static final String SP="book";
    public static final String SP_KEY_ISFIRSTRUN="SP_KEY_ISFIRSTRUN";
    public static final String SP_KEY_STRING ="SP_KEY_STRING";
    public static final String SP_KEY_LONG ="SP_KEY_LONG";
    public static final String SP_KEY_TOTAL = "SP_KEY_TOTAL";
}
