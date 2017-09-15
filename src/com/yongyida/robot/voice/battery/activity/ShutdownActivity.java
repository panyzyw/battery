package com.yongyida.robot.voice.battery.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.iflytek.cloud.SpeechError;
import com.yongyida.robot.voice.battery.R;
import com.yongyida.robot.voice.battery.app.MyApplication;
import com.yongyida.robot.voice.battery.bean.ShutDownBean;
import com.yongyida.robot.voice.battery.constant.IntentConstant;
import com.yongyida.robot.voice.battery.dialog.ShutDownDialog;
import com.yongyida.robot.voice.battery.dialog.ShutUpDialog;
import com.yongyida.robot.voice.battery.utils.BeanUtil;
import com.yongyida.robot.voice.battery.utils.BroadUtils;
import com.yongyida.robot.voice.battery.utils.DateUtil;
import com.yongyida.robot.voice.battery.utils.SPUtil;
import com.yongyida.robot.voice.battery.utils.SpeechUtil;
import com.yongyida.robot.voice.battery.utils.SpeechUtil.SpeechCallBack;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShutdownActivity extends Activity implements ShutUpDialog.ShutDownListener,
        ShutDownDialog.ShutDownListener {

    private static final String TAG = "ShutdownActivity";
    ShutUpDialog editNameDialog;
    private static int MODE;
    private static int MODE_MEDIA = 1;
    private static int MODE_TEXT = 2;
    private boolean isringpause = false;
    private MediaPlayer mediaPlayer;
    Context mContext;
    private int count = 14;
    public static ShutDownBean shutDownBean;
    private String[] seconds = new String[]{"零","一","二","三","四","五","六","七","八","九","十",};
    private SPUtil spUtil;
    public static ShutdownActivity shutdownActivity;
    private boolean isShowDialog = false;

    public class PhoneStatRec extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            TelephonyManager mTelManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            switch (mTelManager.getCallState()) {
                case TelephonyManager.CALL_STATE_RINGING:
                    if (MODE == MODE_MEDIA && mediaPlayer != null && mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        isringpause = true;
                    }
                    if (MODE == MODE_TEXT && SpeechUtil.getInstance(context).isSpeaking()) {
                        SpeechUtil.getInstance(context).pause();
                        isringpause = true;
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    if (MODE == MODE_MEDIA && mediaPlayer != null && mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        isringpause = true;
                    }
                    if (MODE == MODE_TEXT && SpeechUtil.getInstance(context).isSpeaking()) {
                        SpeechUtil.getInstance(context).pause();
                        isringpause = true;
                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    if (MODE == MODE_MEDIA && mediaPlayer != null && isringpause == true) {
                        mediaPlayer.start();
                        // mTts.pauseSpeaking();
                        isringpause = false;
                    }
                    if (MODE == MODE_TEXT && isringpause == true) {
                        SpeechUtil.getInstance(context).resume();
                        isringpause = false;
                    }
                    break;
            }
        }
    }

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_shutdown);
        mContext = this;
        ShutdownActivity.shutdownActivity = this;
        Intent intent = getIntent();
        String result = intent.getStringExtra("result");
        int type = intent.getIntExtra("type", -1);
        Log.e(TAG, "type: " + type);
        spUtil = new SPUtil(mContext);
        if (type == 1) {
            shutDownBean = BeanUtil.parseJsonWithGson(result, ShutDownBean.class);
            AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
            if (result != null && shutDownBean != null && "poweroff".equals(shutDownBean.operation) && shutDownBean.semantic.slots != null) { //某个时间后关机
                String date = shutDownBean.semantic.slots.datetime.date;
                String time = shutDownBean.semantic.slots.datetime.time;
                long shutDownTimestamp = DateUtil.date2timestamp(date + " " + time);
                if(Pattern.compile(".*[^时秒].*").matcher(shutDownBean.text).matches()){  //XX分钟后关机
                    String minute = "";
                    Pattern pattern = Pattern.compile("(.+)分钟后关机.*");
                    Matcher matcher = pattern.matcher(shutDownBean.text);
                    if (matcher.find()) {
                        minute = matcher.group(1);
                    }
                    Log.e(TAG, "minute: " + minute );
                    if("一百".equals(minute)){    //如下四种情况，讯飞返回的是当前时间，需加上相应的时间
                        shutDownTimestamp = shutDownTimestamp + 100 * 60 * 1000;
                    }else if("两百".equals(minute)){
                        shutDownTimestamp = shutDownTimestamp + 200 * 60 * 1000;
                    }else if("一千".equals(minute)){
                        shutDownTimestamp = shutDownTimestamp + 1000 * 60 * 1000;
                    }else if("两千".equals(minute)){
                        shutDownTimestamp = shutDownTimestamp + 2000 * 60 * 1000;
                    }
                }
                long localtimestamp = DateUtil.getLocaltimestamp();
                Log.e(TAG, "getLocalDatetime: " + DateUtil.getLocalDatetime() );
                spUtil.putLong("oldLocaltime",localtimestamp);
                long dx = shutDownTimestamp - localtimestamp;
                long maxTimestamp = 120 * 60 * 1000 + 15 * 1000;//最大关机时间暂定为120分钟（由于本地时间和服务器时间有误差暂时多加15秒）

                String onlySecond = getSecond(shutDownBean.text);
                if( !"".equals(onlySecond) ){ //说法中只包含秒的情况
                    int intSecond = getNumSecond(onlySecond);
                    Log.e(TAG, "getNumSecond:" + intSecond );
                    if( !(intSecond > (120 * 60)) ){
                        cancelShutdown();
                        setShutdown(localtimestamp + intSecond * 1000);
                        sendBroadcast2MainService(intSecond);
                        if(intSecond >= 60){
                            voiceTTS(shutDownBean.text,true);
                        }else{
                            voiceTTS(shutDownBean.text,false);
                        }
                    }else {
                        Log.e(TAG, "getNumSecond:" + intSecond );
                        voiceTTS(mContext.getString(R.string.max_shutdown),true);
                    }
                    return;
                }
                if (dx > maxTimestamp) {
                    Log.e(TAG, "shutDownTimestamp - localtimestamp = " +
                            shutDownTimestamp + "-" + localtimestamp + " = " + dx/1000 + "秒"  );
                    voiceTTS(mContext.getString(R.string.max_shutdown),true);
                    return;
                }
                if (dx > 0) {
                    cancelShutdown();
                    setShutdown(shutDownTimestamp);
                    int intSecond = (int) ((shutDownTimestamp - localtimestamp) / 1000);
                    sendBroadcast2MainService(intSecond);
                    if(intSecond >= 60){
                        voiceTTS(shutDownBean.text,true);
                    }else{
                        voiceTTS(shutDownBean.text,false);
                    }
                } else {
                    voiceTTS(mContext.getString(R.string.timeover),true);
                }
            } else if ("cancel".equals(shutDownBean.operation)) {   //取消关机
                cancelShutdown();
                sendBroadcast2MainService(-1);
                voiceTTS(mContext.getString(R.string.cancel_shutdown),true);
            }
        } else if (type == 2) { //直接说关机

            String text = null ;

            if (result != null && (result.contains(getString(R.string.dyc_poweroff)) || result.contains("poweroff"))) {

                text = getRandomText(this,R.array.tips_power_off);
                showShutUpDialog(ShutUpDialog.ACTION_SHUTDOWN);
            } else {

                text = getRandomText(this,R.array.tips_reboot);
                showShutUpDialog(ShutUpDialog.ACTION_REBOOT);
            }

            if(!MyApplication.isRS20){

                text = getRandomText(this,R.array.dyc_poweroff_greet);
            }

            if (TextUtils.isEmpty(text)) {
                return;
            }
            voiceTTS(text,false);
        }else if(type == 3){ //设置的关机时间到了
			checkScreenState(mContext);
            showShutUpDialog(ShutUpDialog.ACTION_SHUTDOWN);

            String text;
            if(MyApplication.isRS20){

                text = getRandomText(this,R.array.tips_power_off);
            }else{

                text = getRandomText(this,R.array.dyc_poweroff_greet);
            }

            if (TextUtils.isEmpty(text)) {
                return;
            }
            voiceTTS(text,false);
        }else if(type ==4){ // 长按电源实体键发送广播处理的

            showShutUpDialog(ShutDownDialog.ACTION_LONG_PRESS_POWER);
            String text = getRandomText(this,R.array.tips_power_off);
            if (TextUtils.isEmpty(text)) {
                return;
            }
            voiceTTS(text,false);

        }
    }

    @SuppressLint("NewApi")
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (editNameDialog != null && editNameDialog.isInLayout()) {
            editNameDialog.dismiss();

        }
        SpeechUtil.getInstance(ShutdownActivity.this).stopSpeak();
    }

    @Override
    public void onShutDownListenerSure() {
        finish();
    }

    @Override
    public void onShutDownListenerCancel() {
        finish();
    }

    @Override
    public void onShutDownListenerComplete() {
        finish();
    }

    @Override
    public void onShutDown() {

        String speakText = getRandomText(this, R.array.dyc_poweroff_greet);
        SpeechUtil.getInstance(ShutdownActivity.this).speak(speakText, new SpeechCallBack() {

            @Override
            public void onSpeakBeginCallback() {

            }

            @Override
            public void onCompletedCallback(SpeechError error) {

                Intent intent = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
                intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
                //其中false换成true,会弹出是否关机的确认窗口
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                finish();
            }
        });


    }

    @Override
    public void onReboot() {


        String speakText = getRandomText(this, R.array.dyc_reboot_greet);
        SpeechUtil.getInstance(ShutdownActivity.this).speak(speakText, new SpeechCallBack() {


            @Override
            public void onSpeakBeginCallback() {

            }

            @Override
            public void onCompletedCallback(SpeechError error) {

                Intent intent2 = new Intent(Intent.ACTION_REBOOT);
                intent2.putExtra("nowait", 1);
                intent2.putExtra("interval", 1);
                intent2.putExtra("window", 0);
                sendBroadcast(intent2);

                finish();
            }
        });



    }


    private String getRandomText(Context context, int id){

        String[] textArr = context.getResources().getStringArray(id);
        int index = new Random().nextInt(textArr.length) ;

        return textArr[index];
    }


    private void showShutUpDialog(int flag){

        if(MyApplication.isRS20){

            showShutDownDialog(flag) ;

        }else{

            isShowDialog = true;
            editNameDialog = ShutUpDialog.newInstance(this,flag);
            editNameDialog.show(getFragmentManager(), "EditNameDialog");
            editNameDialog.setCancelable(false);
        }

    }

    private ShutDownDialog mShutDownDialog ;
    private void showShutDownDialog(int flag){
        mShutDownDialog = ShutDownDialog.newInstance(this,flag);
        mShutDownDialog.show();
    }


    private PendingIntent getShutdownPendingIntent() {
        Intent shutdownIntent = new Intent(IntentConstant.ALARM_SHUTDOWN);
        return PendingIntent.getBroadcast(mContext, 0, shutdownIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void sendBroadcast2MainService(int shutdownTime) { //发送单位为秒的广播给主服务
        Intent intent = new Intent(IntentConstant.ACTION_MAIN_RECE);
        intent.putExtra("from", getPackageName());
        intent.putExtra("function", "shutdown");
        intent.putExtra("result", shutdownTime);
        sendBroadcast(intent);
        spUtil.putInt("shutdownTime",shutdownTime);
        Log.e(TAG, "sendBroadcast2MainService: " + shutdownTime + "秒 " + shutdownTime/60 + "分钟");
    }

    public void cancelShutdown() {
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        am.cancel(getShutdownPendingIntent());
        Log.e(TAG, "cancelShutdown");
    }

    public void setShutdown(long shutdownTime) {
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, shutdownTime, getShutdownPendingIntent());
        Log.e(TAG, "setAlarmShutdowntime: " + DateUtil.timestamp2Datetime(shutdownTime));
    }

    public String getSecond(String text){
        String second = "";
        Pattern pattern = Pattern.compile("(?![分时].*)(\\d+)秒后关机.*");
        Matcher matcher = pattern.matcher(text);
        if(matcher.matches()){
            matcher = pattern.matcher(text);
            if(matcher.find()){
                second = matcher.group(1);
            }
        }else {
            pattern = Pattern.compile("(?![分时].*)(.+)秒后关机.*");
            matcher = pattern.matcher(text);
            if(matcher.matches()) {
                matcher.reset();
                if (matcher.find()) {
                    second = matcher.group(1);
                }
            }
        }
        return second;
    }

    private boolean isNum(String text){
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher matcher = pattern.matcher(text);
        return matcher.matches();
    }

    private int getNumSecond(String strSecond){
        int intSecond = 0;
        if(isNum(strSecond)){  //数字秒（例：3秒）
            intSecond = Integer.parseInt(strSecond);
        }else { //中文秒（例：三秒）
            if(seconds[0].equals(strSecond)){
                intSecond = 0;
            }else if(seconds[1].equals(strSecond)){
                intSecond = 1;
            }else if(seconds[2].equals(strSecond)){
                intSecond = 2;
            }else if(seconds[3].equals(strSecond)){
                intSecond = 3;
            }else if(seconds[4].equals(strSecond)){
                intSecond = 4;
            }else if(seconds[5].equals(strSecond)){
                intSecond = 5;
            }else if(seconds[6].equals(strSecond)){
                intSecond = 6;
            }else if(seconds[7].equals(strSecond)){
                intSecond = 7;
            }else if(seconds[8].equals(strSecond)){
                intSecond = 8;
            }else if(seconds[9].equals(strSecond)){
                intSecond = 9;
            }else if(seconds[10].equals(strSecond)){
                intSecond = 10;
            }else if("一百".equals(strSecond)){
                intSecond = 100;
            }else if("两百".equals(strSecond)){   //一百、两百返回中文，三百等返回300,真坑
                intSecond = 200;
            }else if("一千".equals(strSecond)){
                intSecond = 100;
            }else if("两千".equals(strSecond)){
                intSecond = 2000;
            }
        }
        return intSecond;
    }

    private void voiceTTS(String speakText, final boolean isRecycle) {
        Log.e(TAG, "voiceTTS: " + speakText);
        SpeechUtil.getInstance(ShutdownActivity.this).speak(speakText, new SpeechCallBack() {
            @Override
            public void onSpeakBeginCallback() {
                Log.e(TAG, "voiceTTS: onSpeakBeginCallback");
            }
            @Override
            public void onCompletedCallback(SpeechError error) {
                Log.e(TAG, "voiceTTS onCompletedCallback");
                if(isRecycle){
                    BroadUtils.sendBroadcast(getApplicationContext(), BroadUtils.INTENT_RECYCLE, "battery", "");
                    Log.e(TAG, "" + BroadUtils.INTENT_RECYCLE);
                }
                if(!MyApplication.isRS20 && !isShowDialog){
                    finish();
                }
            }
        });
    }
	
	private void checkScreenState(Context context){
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if(!powerManager.isScreenOn()){
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
            wakeLock.acquire();
            wakeLock.release();
        }
    }
}
