package com.yongyida.robot.voice.battery.app;

import android.app.Application;

import com.iflytek.cloud.SpeechUtility;
import com.yongyida.robot.voice.battery.R;
import com.yongyida.robot.voice.battery.utils.CrashHandler;

public class MyApplication extends Application {

	private static MyApplication instance;

	public static boolean isRS20 = false ;	// 判断是否是RS20，RS20做一些特殊化处理

	@Override
	public void onCreate() {
		super.onCreate();

		isRS20 = "A8".equals(android.os.Build.MODEL) ;

		CrashHandler.getInstance().init(getApplicationContext());
		SpeechUtility.createUtility(MyApplication.this, "appid=" + getString(R.string.app_id));
	}

	public static MyApplication getInstance() {
		if (instance == null) {
			instance = new MyApplication();
		}
		return instance;
	}

}
