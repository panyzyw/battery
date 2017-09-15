package com.yongyida.robot.voice.battery.service;

import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.IBinder;
import android.util.Log;
import com.iflytek.cloud.SpeechError;
import com.yongyida.robot.voice.battery.R;
import com.yongyida.robot.voice.battery.utils.BroadUtils;
import com.yongyida.robot.voice.battery.utils.SpeechUtil;
import com.yongyida.robot.voice.battery.utils.SpeechUtil.SpeechCallBack;

import org.json.JSONObject;

public class AdjustVolumeService extends Service {

    private final static String TAG = "AdjustVolumeService";
    private AudioManager audioManager;
    private String serviceStr;
    private String operationStr;
    private String size = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        audioManager = (AudioManager) getSystemService(Service.AUDIO_SERVICE);// 指定调节音乐的音频，增大音量，而且显示音量的图形示意
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return super.onStartCommand(intent, flags, startId);
        }
        Resources res = null;
        res = getResources();
        try {
            String str = intent.getExtras().getString("json");
            if (str == null || str.equals("")) {
                return super.onStartCommand(intent, flags, startId);
            } else if (str != null && !str.equals("")) {
                JSONObject obj = new JSONObject(str);
                if (obj.has("service")) {
                    serviceStr = obj.getString("service");
                }
                if (obj.has("operation")) {
                    operationStr = obj.getString("operation");
                }
                JSONObject jsonObj = obj.getJSONObject("semantic").getJSONObject("slots");
                if (jsonObj.has("size")) {
                    size = jsonObj.getString("size");
                }
            }

            if (serviceStr.equals("sound")) {
                if (operationStr.equals(getResources().getString(R.string.dyc_sound_up))) {
                    if (size == null) {
                        setSoundUp(1);
                    } else {
                        if (size.equals(res.getString(R.string.dyc_sound_two))) {
                            setSoundUp(2);
                        } else if (size.equals(res.getString(R.string.dyc_sound_three))) {
                            setSoundUp(3);
                        } else if (size.equals(res.getString(R.string.dyc_sound_four))) {
                            setSoundUp(4);
                        } else if (size.equals(res.getString(R.string.dyc_sound_five))) {
                            setSoundUp(5);
                        }
                    }
                } else if (operationStr.equals(getResources().getString(R.string.dyc_sound_down))) {
                    if (size == null) {
                        setSoundDown(1);
                    } else {
                        if (size.equals(res.getString(R.string.dyc_sound_two))) {
                            setSoundDown(2);
                        } else if (size.equals(res.getString(R.string.dyc_sound_three))) {
                            setSoundDown(3);
                        } else if (size.equals(res.getString(R.string.dyc_sound_four))) {
                            setSoundDown(4);
                        } else if (size.equals(res.getString(R.string.dyc_sound_five))) {
                            setSoundDown(5);
                        }
                    }
                } else if (operationStr.equals(getResources().getString(R.string.dyc_sound_max))) {
                    setSoundMax();
                } else if (operationStr.equals(getResources().getString(R.string.dyc_sound_min))) {
                    setSoundMin();

                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void setSoundUp(int number) {
        int vSystem = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM) + number;
        if (vSystem > audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM)) {
            vSystem = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
        }
        int vMusic = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) + number;
        if (vMusic > audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) {
            vMusic = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        }
        int vAlarm = audioManager.getStreamVolume(AudioManager.STREAM_ALARM) + number;
        if (vAlarm > audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)) {
            vAlarm = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        }
        int vNotification = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION) + number;
        if (vNotification > audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)) {
            vNotification = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
        }
        int vRing = audioManager.getStreamVolume(AudioManager.STREAM_RING) + number;
        if (vRing > audioManager.getStreamMaxVolume(AudioManager.STREAM_RING)) {
            vRing = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        }
        int vCall = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL) + number;
        if (vCall > audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)) {
            vCall = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
        }
        int vDtmf = audioManager.getStreamVolume(AudioManager.STREAM_DTMF) + number;
        if (vDtmf > audioManager.getStreamMaxVolume(AudioManager.STREAM_DTMF)) {
            vDtmf = audioManager.getStreamMaxVolume(AudioManager.STREAM_DTMF);
        }
        audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, vSystem, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vMusic, AudioManager.FLAG_SHOW_UI);
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, vAlarm, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, vNotification, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        audioManager.setStreamVolume(AudioManager.STREAM_RING, vRing, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, vCall, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        audioManager.setStreamVolume(AudioManager.STREAM_DTMF, vDtmf, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        String text = getResources().getString(R.string.dyc_up) + String.valueOf(number) + getResources().getString(R.string.dyc_scale);
        voiceTTS(text);
    }

    private void setSoundDown(int number) {
        int sys = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM) - number;
        if (sys < 0) {
            sys = 1;
        }
        int mus = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) - number;
        if (mus < 0) {
            mus = 1;
        }
        int alm = audioManager.getStreamVolume(AudioManager.STREAM_ALARM) - number;
        if (alm < 0) {
            alm = 1;
        }
        int not = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION) - number;
        if (not < 0) {
            not = 1;
        }
        int ring = audioManager.getStreamVolume(AudioManager.STREAM_RING) - number;
        if (ring < 0) {
            ring = 1;
        }
        int call = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL) - number;
        if (call < 0) {
            call = 1;
        }
        int dtmf = audioManager.getStreamVolume(AudioManager.STREAM_DTMF) - number;
        if (dtmf < 0) {
            dtmf = 1;
        }
        audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, sys, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mus, AudioManager.FLAG_SHOW_UI);
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, alm, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, not, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        audioManager.setStreamVolume(AudioManager.STREAM_RING, ring, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, call, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        audioManager.setStreamVolume(AudioManager.STREAM_DTMF, dtmf, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        String text = getResources().getString(R.string.dyc_down) + String.valueOf(number) + getResources().getString(R.string.dyc_scale);
        voiceTTS(text);

    }

    private void setSoundMax() {
        audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM,
                audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM), AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        Log.i(TAG, "STREAM_SYSTEM:" + audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM));
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), AudioManager.FLAG_SHOW_UI);
        Log.i(TAG, "STREAM_MUSIC:" + audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM,
                audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM), AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        Log.i(TAG, "STREAM_ALARM:" + audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM));
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION,
                audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION), AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        Log.i(TAG, "STREAM_NOTIFICATION:" + audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION));
        audioManager.setStreamVolume(AudioManager.STREAM_RING,
                audioManager.getStreamMaxVolume(AudioManager.STREAM_RING), AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        Log.i(TAG, "STREAM_RING:" + audioManager.getStreamMaxVolume(AudioManager.STREAM_RING));
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        Log.i(TAG, "STREAM_VOICE_CALL:" + audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL));
        audioManager.setStreamVolume(AudioManager.STREAM_DTMF,
                audioManager.getStreamMaxVolume(AudioManager.STREAM_DTMF), AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        String text = getResources().getString(R.string.dyc_up_max);
        voiceTTS(text);

    }

    private void setSoundMin() {
        audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM,1, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,1, AudioManager.FLAG_SHOW_UI);
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM,1, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION,1, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        audioManager.setStreamVolume(AudioManager.STREAM_RING,1, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,1, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        audioManager.setStreamVolume(AudioManager.STREAM_DTMF,1, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        String text = getResources().getString(R.string.dyc_up_min);
        voiceTTS(text);
    }

    private void voiceTTS(String speakText) {
        Log.e(TAG, "voiceTTS: " + speakText);
        SpeechUtil.getInstance(AdjustVolumeService.this).speak(speakText, new SpeechCallBack() {
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
                AdjustVolumeService.this.stopSelf();
            }
        });
    }
}

