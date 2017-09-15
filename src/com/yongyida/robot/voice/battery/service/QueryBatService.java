package com.yongyida.robot.voice.battery.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import com.iflytek.cloud.SpeechError;
import com.yongyida.robot.voice.battery.R;
import com.yongyida.robot.voice.battery.utils.BroadUtils;
import com.yongyida.robot.voice.battery.utils.SpeechUtil;

public class QueryBatService extends Service {

    private final static String TAG = "queryBatService";
    private BueryBatReceiver queryBatReceiver;
    private boolean isFrist = true;
    private int mBatteryLevel;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    Log.i(TAG, "msg.what = 0");
                    String text = (String) msg.obj;
                    voiceTTS(text);
                    break;
            }

        }
    };

    public class BueryBatReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                Log.d(TAG, "batteryReceiver ACTION_BATTERY_CHANGED");
                int level = intent.getIntExtra("level", 0);
                int scale = intent.getIntExtra("scale", 100);
                synchronized (this) {
                    mBatteryLevel = level * 100 / scale;
                }
                Log.d(TAG, "mBatteryLevel = " + mBatteryLevel + " %");
                if (isFrist) {
                    isFrist = false;
                    Resources res = null;
                    res = getResources();
                    String text = res.getString(R.string.dyc_querystart) + String.valueOf(mBatteryLevel) + res.getString(R.string.dyc_queryend);
                    Message msg = mHandler.obtainMessage();
                    msg.what = 0;
                    msg.obj = text;
                    mHandler.sendMessage(msg);
                }


            }
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        queryBatReceiver = new BueryBatReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        this.registerReceiver(queryBatReceiver, intentFilter);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isFrist = true;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        SpeechUtil.getInstance(QueryBatService.this).stopSpeak();
        this.unregisterReceiver(queryBatReceiver);
        super.onDestroy();
    }


    private void voiceTTS(String speakText) {
        Log.e(TAG, "voiceTTS: " + speakText);
        SpeechUtil.getInstance(QueryBatService.this).speak(speakText, new SpeechUtil.SpeechCallBack() {
            @Override
            public void onSpeakBeginCallback() {
                Log.e(TAG, "voiceTTS: onSpeakBeginCallback");
            }

            @Override
            public void onCompletedCallback(SpeechError error) {
                Log.e(TAG, "voiceTTS: onCompletedCallback");
                if (error != null) {
                    Log.e(TAG, "error: " + error.getErrorCode());
                }
                BroadUtils.sendBroadcast(getApplicationContext(), BroadUtils.INTENT_RECYCLE, "battery", "");
                Log.e(TAG, "onCompletedCallback: " + BroadUtils.INTENT_RECYCLE);
                QueryBatService.this.stopSelf();
            }
        });
    }
}
