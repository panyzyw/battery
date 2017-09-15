package com.yongyida.robot.voice.battery.service;

import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import com.yongyida.robot.voice.battery.R;
import com.yongyida.robot.voice.battery.app.MyApplication;
import com.yongyida.robot.voice.battery.constant.IntentConstant;
import com.yongyida.robot.voice.battery.constant.StringConstant;
import com.yongyida.robot.voice.battery.dialog.BatteryDialog;
import com.yongyida.robot.voice.battery.utils.SPUtil;
import com.zccl.ruiqianqi.brain.system.SystemPresenter;

import java.io.IOException;

public class MonitorService extends Service {

    private static final String TAG = "MonitorService";
    private AssetManager assetManager;
    private MediaPlayer mediaPlayer;
    private AssetFileDescriptor fileDescriptor;
    private SPUtil sPUtil;
    private Long time;
    private BatteryStatusReceiver batReceiver;
    private static AlertDialog.Builder builder = null;
    private static AlertDialog dialog = null;
    private static Button leftButton;
    private static Button rightButton;
    private TextView txtView, txtMessage;
    private int second = 120;
    private Context mContext;
    private boolean isBatteryWarn = false;
    Handler handler = new Handler();

    private BatteryState batteryState = BatteryState.NONE;

    private SystemPresenter mSystemPresenter ;

    public enum BatteryState {
        NOCHARGE_LOW, LowPower, NormalPower, FullPower, NONE
    }

    public class BatteryStatusReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            mContext = context;
            String action = intent.getAction();
            int mBatteryLevel;
            time = System.currentTimeMillis();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                int level = intent.getIntExtra("level", 0);
                int scale = intent.getIntExtra("scale", 100);
                int plugType = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
                boolean isConn = false;
                if (plugType == BatteryManager.BATTERY_PLUGGED_AC || plugType == BatteryManager.BATTERY_PLUGGED_USB) {
                    isConn = true;
                }
                synchronized (this) {
                    mBatteryLevel = level * 100 / scale;
                }
                if (mBatteryLevel == 20
                        && mBatteryLevel < sPUtil.getInt()) {
                    try {
                        sPUtil.putLong(time);
                        player(StringConstant.LOWPOWERPATH);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                } else if (mBatteryLevel == 15 && mBatteryLevel < sPUtil.getInt()) {
                    try {
                        sPUtil.putLong(time);
                        player(StringConstant.LOWPOWERPATH);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                } else if (mBatteryLevel == 10
                        && mBatteryLevel < sPUtil.getInt()) {

                    if(MyApplication.isRS20){
                        showBatteryDialog(context,mBatteryLevel) ;
                    }

                    try {
                        sPUtil.putLong(time);
                        player(StringConstant.LOWPOWERPATH);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                } else if (mBatteryLevel == 5
                        && mBatteryLevel < sPUtil.getInt()) {
                    try {
                        sPUtil.putLong(time);
                        player(StringConstant.LOWPOWERPATH);

                    } catch (Throwable e) {
                        e.printStackTrace();
                    }

                } else if (mBatteryLevel == 3
                        && mBatteryLevel < sPUtil.getInt()) {

                    if(MyApplication.isRS20){

                        showBatteryDialog(context,mBatteryLevel) ;
                    }else if(!isBatteryWarn){

                        init_dialog(context);
                    }

                    try {
                        sPUtil.putLong(time);
                        player(StringConstant.LOWPOWERPATH);

                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                } else if (mBatteryLevel < 3
                        && mBatteryLevel <= sPUtil.getInt() && !isConn) {
                    try {
                        sPUtil.putLong(time);
                        player(StringConstant.LOWPOWERPATH);
                        if (!isBatteryWarn && !MyApplication.isRS20) {
                            init_dialog(context);
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                } else if (mBatteryLevel == 100
                        && mBatteryLevel > sPUtil.getInt()) {
                    try {
                        sPUtil.putLong(time);
                        player(StringConstant.COMPLATEPATH);

                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
                sPUtil.putInt(mBatteryLevel);
                Log.i(TAG, "sPUtil.getLong:" + sPUtil.getLong());
                Log.i(TAG, "sPUtil.getInt:" + sPUtil.getInt());
                dealWithBreathLight(mBatteryLevel, isConn);
            } else if (action.equals(Intent.ACTION_POWER_CONNECTED)) {
                        
                dismissBatteryDialog() ;
                try {
                    if (isBatteryWarn) {
                        if (dialog != null) {
                            dialog.dismiss();
                            dialog = null;
                        }
                        if (builder != null) {
                            builder = null;
                        }
                        handler.removeCallbacks(lowBatteryRunable);
                        isBatteryWarn = false;
                    }
                    if (sPUtil.getInt() < 100
                            && (time - sPUtil.getLong() > 5000)) {
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.stop();
                        }
                        player(StringConstant.CONNECTPATH);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }

            } else if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
                try {
                    if (sPUtil.getInt() < 100 && (time - sPUtil.getLong() > 5000)) {
                        Log.i(TAG, "sptil:" + sPUtil.getLong());
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.stop();
                        }
                        player(StringConstant.DISCONNECTPATH);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private BatteryDialog mBatteryDialog ;
    public void showBatteryDialog(Context context ,int battery){

        if(mBatteryDialog == null){

            mBatteryDialog = new BatteryDialog(this, battery) ;

        }else{

            mBatteryDialog.setBattery(battery) ;
        }

        mBatteryDialog.show();
    }


    private void dismissBatteryDialog(){

        if(mBatteryDialog != null && mBatteryDialog.isShowing()){

            mBatteryDialog.dismiss();
        }

    }


    private void dealWithBreathLight(int batteryLevel, boolean isPlug) {
        if (!isPlug) {
            closePowerLight(batteryLevel);
            return;
        } else {
            if (batteryState == BatteryState.NOCHARGE_LOW) {
                closeNoChargeLowLight();
                batteryState = BatteryState.NONE;
            }
        }
        if (batteryLevel < 20) {
            if (batteryState != BatteryState.LowPower) {
                if (batteryState == BatteryState.NormalPower)
                    closeNormalLight();
                handler.postDelayed(openLowPowerRunnable, 200);
            }
            batteryState = BatteryState.LowPower;
        } else if (batteryLevel >= 20 && batteryLevel < 100) {
            if (batteryState != BatteryState.NormalPower) {
                if (batteryState == BatteryState.LowPower) {
                    closeLowPowerLight();
                } else if (batteryState == BatteryState.FullPower) {
                    closeFullLight();
                }
                handler.postDelayed(openNormalPowerRunnable, 200);
            }
            batteryState = BatteryState.NormalPower;
        } else if (batteryLevel == 100) {
            if (batteryState != BatteryState.FullPower) {
                if (batteryState == BatteryState.NormalPower)
                    closeNormalLight();
                handler.postDelayed(openFullPowerRunnable, 200);
            }
            batteryState = BatteryState.FullPower;
        }
    }

    private void closePowerLight(int batteryLevel) {
        if (batteryState == BatteryState.LowPower) {
            closeLowPowerLight();
            batteryState = BatteryState.NONE;
        } else if (batteryState == BatteryState.NormalPower) {
            closeNormalLight();
            batteryState = BatteryState.NONE;
        } else if (batteryState == BatteryState.FullPower) {
            closeFullLight();
            batteryState = BatteryState.NONE;
        }
        if (batteryLevel < 20) {
            if (batteryState == BatteryState.NONE) {
                openNoChargeLowLight();
                batteryState = BatteryState.NOCHARGE_LOW;
            }
        } else {
            if (batteryState == BatteryState.NOCHARGE_LOW) {
                closeNoChargeLowLight();
                batteryState = BatteryState.NONE;
            }
        }
    }

    private void openNoChargeLowLight() {
        Intent intentRemindChargeOpen = new Intent("com.yongyida.robot.change.BREATH_LED");
        intentRemindChargeOpen.putExtra("on_off", true);
        intentRemindChargeOpen.putExtra("colour", 1);
        intentRemindChargeOpen.putExtra("frequency", 3);
        intentRemindChargeOpen.putExtra("Permanent", "remindCharge");
        intentRemindChargeOpen.putExtra("priority", 2);
        sendBroadcast(intentRemindChargeOpen);
    }


    private void closeNoChargeLowLight() {
        Intent intentRemindChargeClose = new Intent("com.yongyida.robot.change.BREATH_LED");
        intentRemindChargeClose.putExtra("on_off", false);
        intentRemindChargeClose.putExtra("Permanent", "remindCharge");
        sendBroadcast(intentRemindChargeClose);
    }

    private void openLowPowerLight() {
        Intent intentLowPowerOpen = new Intent(
                "com.yongyida.robot.change.BREATH_LED");
        intentLowPowerOpen.putExtra("on_off", true);
        intentLowPowerOpen.putExtra("colour", 1);
        intentLowPowerOpen.putExtra("frequency", 2);
        intentLowPowerOpen.putExtra("Permanent", "lowPower");
        intentLowPowerOpen.putExtra("priority", 2);
        sendBroadcast(intentLowPowerOpen);
    }

    private void closeLowPowerLight() {
        Intent intentLowPowerClose = new Intent("com.yongyida.robot.change.BREATH_LED");
        intentLowPowerClose.putExtra("on_off", false);
        intentLowPowerClose.putExtra("Permanent", "lowPower");
        sendBroadcast(intentLowPowerClose);
    }

    private void openNormalLight() {
        Intent intentNormalPowerOpen = new Intent("com.yongyida.robot.change.BREATH_LED");
        intentNormalPowerOpen.putExtra("on_off", true);
        intentNormalPowerOpen.putExtra("colour", 2);
        intentNormalPowerOpen.putExtra("frequency", 1);
        intentNormalPowerOpen.putExtra("Permanent", "normalPower");
        intentNormalPowerOpen.putExtra("priority", 2);
        sendBroadcast(intentNormalPowerOpen);
    }

    private void closeNormalLight() {
        Intent intentNormalPowerClose = new Intent("com.yongyida.robot.change.BREATH_LED");
        intentNormalPowerClose.putExtra("on_off", false);
        intentNormalPowerClose.putExtra("Permanent", "normalPower");
        sendBroadcast(intentNormalPowerClose);
    }

    private void openFullLight() {
        Intent intentFullPowerOpen = new Intent("com.yongyida.robot.change.BREATH_LED");
        intentFullPowerOpen.putExtra("on_off", true);
        intentFullPowerOpen.putExtra("colour", 2);
        intentFullPowerOpen.putExtra("frequency", 3);
        intentFullPowerOpen.putExtra("Permanent", "fullPower");
        intentFullPowerOpen.putExtra("priority", 2);
        sendBroadcast(intentFullPowerOpen);
    }

    private void closeFullLight() {
        Intent intentFullPowerClose = new Intent("com.yongyida.robot.change.BREATH_LED");
        intentFullPowerClose.putExtra("on_off", false);
        intentFullPowerClose.putExtra("Permanent", "fullPower");
        sendBroadcast(intentFullPowerClose);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mSystemPresenter = SystemPresenter.getInstance(this) ;
        try {
            sPUtil = new SPUtil(this);
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        batReceiver = new BatteryStatusReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        intentFilter.addAction(IntentConstant.MAIN_ACTION);
        intentFilter.addAction(IntentConstant.STOP_ACTION);
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        this.registerReceiver(batReceiver, intentFilter);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "startCommand");
        flags = START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }

    private void player(String path) {

        if(MyApplication.isRS20){

            if(path.equals(StringConstant.LOWPOWERPATH)){

                BatteryService.startTTS(this, mSystemPresenter, R.array.low_power);

            }else if(path.equals(StringConstant.CONNECTPATH)){

                BatteryService.startTTS(this, mSystemPresenter, R.array.connect);

                return;

            }else if(path.equals(StringConstant.DISCONNECTPATH)){

                BatteryService.startTTS(this, mSystemPresenter, R.array.disconnect);

            }else if(path.equals(StringConstant.COMPLATEPATH)){

                BatteryService.startTTS(this, mSystemPresenter, R.array.full);
            }
        }


        try {
            assetManager = MonitorService.this.getAssets();
            fileDescriptor = assetManager.openFd(path);
            mediaPlayer.reset();
            mediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getLength());
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {

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

    private void init_dialog(Context context) {
        View view = View.inflate(context, R.layout.dilog, null);
        if (builder == null) {
            builder = new AlertDialog.Builder(context);
            builder.setView(view);
            builder.setCancelable(false);
            if (dialog == null) {
                dialog = builder.create();
                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            }
            leftButton = (Button) view.findViewById(com.yongyida.robot.voice.battery.R.id.left);
            leftButton.setVisibility(View.INVISIBLE);
            rightButton = (Button) view.findViewById(com.yongyida.robot.voice.battery.R.id.right);
            rightButton.setText(context.getString(R.string.ok));
            txtView = (TextView) view.findViewById(R.id.title);
            txtView.setText(context.getString(R.string.shutdown));
            txtMessage = (TextView) view.findViewById(R.id.message);
            rightButton.setOnClickListener(new OnClickListener() {
                public void onClick(View arg0) {
                    dialog.dismiss();
                    dialog = null;
                    builder = null;
                    Intent intent = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
                    intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
                    //其中false换成true,会弹出是否关机的确认窗口
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }
            });
            second = 120;
            dialog.show();
            isBatteryWarn = true;
            handler.post(lowBatteryRunable);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        this.unregisterReceiver(batReceiver);
        try {
            Intent in = new Intent(this, MonitorService.class);
            this.startService(in);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    Runnable lowBatteryRunable = new Runnable() {
        @Override
        public void run() {
            if (second > 0) {
                second--;
                String msg = mContext.getResources().getString(
                        R.string.low_battery_warn);
                txtMessage.setText(String.format(msg, second));
                handler.postDelayed(this, 1000);
            } else {
                Intent intent = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
                intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
                //其中false换成true,会弹出是否关机的确认窗口
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
                if (dialog != null)
                    dialog.dismiss();
            }
        }
    };

    Runnable openLowPowerRunnable = new Runnable() {
        @Override
        public void run() {
            openLowPowerLight();
        }
    };

    Runnable openNormalPowerRunnable = new Runnable() {
        @Override
        public void run() {
            openNormalLight();
        }
    };

    Runnable openFullPowerRunnable = new Runnable() {
        @Override
        public void run() {
            openFullLight();
        }
    };
}
