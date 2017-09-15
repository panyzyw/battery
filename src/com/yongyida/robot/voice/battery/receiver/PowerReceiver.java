package com.yongyida.robot.voice.battery.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.yongyida.robot.voice.battery.activity.ShutdownActivity;

/**
 * Created by Huangxiangxiang on 2017/8/1.
 */
public class PowerReceiver extends BroadcastReceiver {

    private final static String ACTION_SHUTDOWN = "com.yongyida.robot.LONG_PRESS_POWER" ;


    @Override
    public void onReceive(Context context, Intent intent) {

        if(ACTION_SHUTDOWN.equals(intent.getAction())){

            Intent in = new Intent(context, ShutdownActivity.class);
            in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            in.putExtra("type",4);
            context.startActivity(in);

        }


    }

}
