package com.yongyida.robot.voice.battery.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.yongyida.robot.voice.battery.R;
import com.yongyida.robot.voice.battery.app.MyApplication;
import com.yongyida.robot.voice.battery.constant.IntentConstant;
import com.yongyida.robot.voice.battery.constant.StringConstant;
import com.yongyida.robot.voice.battery.utils.BroadUtils;
import com.yongyida.robot.voice.battery.utils.SpeechUtil;
import com.zccl.ruiqianqi.brain.system.SystemPresenter;

import java.io.IOException;
import java.util.Random;

public class BatteryService extends Service{

	private static final String TAG = "batteryService";
	private SpeechUtil speechUtil;
	private AssetManager assetManager;
	private MediaPlayer mediaPlayer;
	private AssetFileDescriptor fileDescriptor;
	
	private static int MODE;
	private static int MODE_MEDIA = 1;
	private static int MODE_TEXT = 2;
	private boolean isringpause = false;

	private SystemPresenter mSystemPresenter ;
	
	public class PhoneStatRec extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			TelephonyManager mTelManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

			switch (mTelManager.getCallState()) {
			case TelephonyManager.CALL_STATE_RINGING:
				if (MODE==MODE_MEDIA && mediaPlayer != null && mediaPlayer.isPlaying()) {
					mediaPlayer.pause();
					isringpause = true;
				}
				if (MODE==MODE_TEXT && SpeechUtil.getInstance(BatteryService.this).isSpeaking()) {
                    SpeechUtil.getInstance(BatteryService.this).pause();
					isringpause = true;
				}
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				if (MODE==MODE_MEDIA && mediaPlayer != null && mediaPlayer.isPlaying()) {
					mediaPlayer.pause();
					isringpause = true;
				}
				if (MODE==MODE_TEXT && SpeechUtil.getInstance(BatteryService.this).isSpeaking()) {
                    SpeechUtil.getInstance(BatteryService.this).pause();
					isringpause = true;
				}
				break;
			case TelephonyManager.CALL_STATE_IDLE:
				if (MODE==MODE_MEDIA && mediaPlayer != null && isringpause == true) {
					mediaPlayer.start();
					// mTts.pauseSpeaking();
					isringpause = false;
				}
				if (MODE==MODE_TEXT && isringpause == true) {
                    SpeechUtil.getInstance(BatteryService.this).resume();
					isringpause = false;
				}
				break;
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
		mSystemPresenter = SystemPresenter.getInstance(this) ;
		try {
			if(mediaPlayer == null){
				mediaPlayer = new MediaPlayer();
				}
				assetManager = this.getAssets();	
		} catch (Throwable e) {
			e.printStackTrace();
		}
		  
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onCreate();	
		if(intent == null){
			return super.onStartCommand(intent, flags, startId);
		}
		try {
			String powerStatus = intent.getStringExtra(StringConstant.POWERSTATUS);
			if(powerStatus.equals(StringConstant.LOWPOWE)){
				player(StringConstant.LOWPOWERPATH);
			}else if(powerStatus.equals(StringConstant.CONNECT)){
			    //player(StringConstant.CONNECTPATH);
			}else if(powerStatus.equals(StringConstant.DISCONNECT)){
				//player(StringConstant.DISCONNECTPATH);
			}else if(powerStatus.equals(StringConstant.FULL)){
				player(StringConstant.FULLPATH);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return super.onStartCommand(intent, flags, startId);		
	}
	
	private void player(String path){

		if(MyApplication.isRS20){

			if(path.equals(StringConstant.LOWPOWERPATH)){

				startTTS(this, mSystemPresenter, R.array.low_power);

			}else if(path.equals(StringConstant.FULLPATH)){

				startTTS(this, mSystemPresenter, R.array.full) ;
			}
		}


		try {
			MODE = MODE_MEDIA;
			fileDescriptor = assetManager.openFd(path);
			mediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(),fileDescriptor.getStartOffset(),fileDescriptor.getLength());
			mediaPlayer.prepare();
			mediaPlayer.start();
			mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					Intent mIntent = new Intent(IntentConstant.RECYCLE_ACTION);
                    BroadUtils.sendBroadcast(getApplicationContext(), BroadUtils.INTENT_RECYCLE, "battery", "");
                    Log.e(TAG, "onCompletedCallback: " + BroadUtils.INTENT_RECYCLE);
					BatteryService.this.stopSelf();
				}
			});
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public static void startTTS(Context context,SystemPresenter systemPresenter, int id){

		String word = getRandomString(context,id) ;
		if (!TextUtils.isEmpty(word)){

			systemPresenter.startTTS(word, null) ;
		}
	}

	private static String getRandomString(Context context, int id){

		String[] words = context.getResources().getStringArray(id) ;
		if(words.length == 0){
			return null ;
		}
		return words[new Random().nextInt(words.length)] ;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if(mediaPlayer != null){
			mediaPlayer.stop();
			mediaPlayer.release();
			mediaPlayer = null;
		}
	}
	
}
