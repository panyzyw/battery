package com.yongyida.robot.voice.battery.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.yongyida.robot.voice.battery.bean.BaseBean;
import com.yongyida.robot.voice.battery.receiver.BatteryReceiver;
import org.json.JSONException;
import org.json.JSONObject;

public class SpeechUtil {
    private static String TAG = "SpeechUtil";
    public static final String APP_NAME = "YYDRobotBattery";
    // 语音合成对象
    private SpeechSynthesizer mTts;
    // 默认发音人
    private String voicer = "aisxa";
    private String mEngineType = SpeechConstant.TYPE_CLOUD;
    private Toast mToast;
    static SpeechUtil speechUtil;
    private Context context;

    public static SpeechUtil getInstance(Context context){
        if( speechUtil == null){
            synchronized (SpeechUtil.class){
                speechUtil = new SpeechUtil(context);
            }
        }
        return speechUtil;
    }
    private SpeechUtil(Context context) {
        this.context = context;
        // 设置参数
        SpeechUtility.createUtility(context, "appid=56065ce8");
        mTts = SpeechSynthesizer.createSynthesizer(context, mTtsInitListener);
        setParam();
    }

    public void speak(String text, final SpeechCallBack speechCallBack) {
        int code = mTts.startSpeaking(text, new SynthesizerListener() {
            @Override
            public void onSpeakResumed() {}
            @Override
            public void onSpeakProgress(int arg0, int arg1, int arg2) {}
            @Override
            public void onSpeakPaused() {}
            @Override
            public void onSpeakBegin() {
                speechCallBack.onSpeakBeginCallback();
            }
            @Override
            public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {}
            @Override
            public void onCompleted(SpeechError arg0) {
                speechCallBack.onCompletedCallback(arg0);
            }

            @Override
            public void onBufferProgress(int arg0, int arg1, int arg2, String arg3) {}
        });
        Log.i(TAG, "code:" + code);
        collectInfoToServer(BatteryReceiver.baseBean,text);
    }

    /**
     * 初始化监听。
     */
    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.i(TAG, "InitListener init() code = " + code);
        }
    };

    private void setParam() {
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        // 根据合成引擎设置相应参数
        if (mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
            // 设置在线合成发音人
            mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);
        } else {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
            // 设置本地合成发音人 voicer为空，默认通过语记界面指定发音人。
            mTts.setParameter(SpeechConstant.VOICE_NAME, "");
        }
        // 设置合成语速
        mTts.setParameter(SpeechConstant.SPEED, "50");
        // 设置合成音调
        mTts.setParameter(SpeechConstant.PITCH, "50");
        // 设置合成音量
        mTts.setParameter(SpeechConstant.VOLUME, "100");
        // 设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/tts.wav");
    }

    public void stopSpeak() {
        if (mTts != null) {
            if (mTts.isSpeaking()) {
                mTts.stopSpeaking();
            }
            mTts.destroy();
        }
    }

    public void pause() {
        mTts.pauseSpeaking();
    }

    public void resume() {
        mTts.resumeSpeaking();
    }

    public boolean isSpeaking() {
        return mTts.isSpeaking();
    }

    public interface SpeechCallBack {
        void onSpeakBeginCallback();
        void onCompletedCallback(SpeechError error);
    }

    public void collectInfoToServer(BaseBean bean, String answer) {
        String info = "";
        if (bean != null) {
            try {
                JSONObject infoJsonObject = new JSONObject();
                infoJsonObject.put("semantic", "");
                infoJsonObject.put("service", bean.service);
                infoJsonObject.put("operation", bean.operation);
                infoJsonObject.put("text", bean.text);
                infoJsonObject.put("answer", answer);
                info = infoJsonObject.toString();
                Log.e("collectInfoToServer:", info);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.e("collectInfoToServer:", "bean=null");
        }
        Intent intent = new Intent("com.yongyida.robot.COLLECT");
        intent.putExtra("collect_result", info);
        intent.putExtra("collect_from", APP_NAME);
        context.sendBroadcast(intent);
        Log.e("collectInfoToServer:", "com.yongyida.robot.COLLECT");
    }
}