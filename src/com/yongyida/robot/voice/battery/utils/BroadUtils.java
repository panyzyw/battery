package com.yongyida.robot.voice.battery.utils;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

public class BroadUtils {
	public static final String FROM = "from";
	public static final String FOR = "for";
	
	public static final String INTENT_RECYCLE = "com.yydrobot.RECYCLE";
	/**
	 * 发送广播
	 * 
	 * @param action
	 * @param from
	 *            广播的发送者
	 * @param for_
	 *            发送该广播的目的
	 */
	public static void sendBroadcast(Context context, String action,
			String from, String for_) {
		Intent intent = new Intent(action);
		if (TextUtils.isEmpty(from)) {
			from = "";
		}
		if (TextUtils.isEmpty(for_)) {
			for_ = "";
		}
		intent.putExtra(BroadUtils.FROM, from);
		intent.putExtra(BroadUtils.FOR, for_);
		context.sendBroadcast(intent);
	}
}
