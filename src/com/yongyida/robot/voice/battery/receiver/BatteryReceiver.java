package com.yongyida.robot.voice.battery.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.yongyida.robot.voice.battery.activity.ShutdownActivity;
import com.yongyida.robot.voice.battery.bean.BaseBean;
import com.yongyida.robot.voice.battery.constant.IntentConstant;
import com.yongyida.robot.voice.battery.service.AdjustVolumeService;
import com.yongyida.robot.voice.battery.service.MonitorService;
import com.yongyida.robot.voice.battery.service.QueryBatService;
import com.yongyida.robot.voice.battery.utils.BeanUtil;
import com.yongyida.robot.voice.battery.utils.DateUtil;
import com.yongyida.robot.voice.battery.utils.SPUtil;
import com.yongyida.robot.voice.battery.utils.SpeechUtil;
import java.util.Date;

public class BatteryReceiver extends BroadcastReceiver{
	private final static String TAG = "batteryReceiver";
    public static BaseBean baseBean;
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
        String result = intent.getStringExtra("result");
        Log.e(TAG, "action:" + action + "");
        Log.e(TAG, "result:" + result + "");
        if(action.equals(Intent.ACTION_BOOT_COMPLETED)){
        	Intent in = new Intent(context, MonitorService.class);
        	context.startService(in);
        	
        }else if(action.equals(Intent.ACTION_PACKAGE_RESTARTED)){
        	Intent in = new Intent(context, MonitorService.class);
        	context.startService(in);
        	
        }else if (action.equals(IntentConstant.BATTERY)) {  
        	try {
                baseBean = BeanUtil.parseJsonWithGson(result,BaseBean.class);
            	Intent in = new Intent(context, QueryBatService.class);
            	context.startService(in);
			} catch (Throwable e) {
				e.printStackTrace();
			}  
        }else if(action.equals(IntentConstant.SHUTDOWN)){
        	try {
                if(result != null){
                    baseBean = BeanUtil.parseJsonWithGson(result,BaseBean.class);
                    if(result.contains("datetime") || result.contains("cancel")){   //取消关机或几分钟后关机
                        Intent in = new Intent(context, ShutdownActivity.class);
                        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        in.putExtra("result", result);
                        in.putExtra("type",1);
                        context.startActivity(in);
                    }else{ //直接说关机或重启
                        Intent in = new Intent(context, ShutdownActivity.class);
                        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        in.putExtra("result", result);
                        in.putExtra("type",2);
                        context.startActivity(in);
                    }
                }
			} catch (Throwable e) {
				e.printStackTrace();
			}
        	
        } else if(action.equals(IntentConstant.SOUND)){
        	try {
                baseBean = BeanUtil.parseJsonWithGson(result,BaseBean.class);
            	Intent it = new Intent(context, AdjustVolumeService.class);
    			it.putExtra("json", result);
    			context.startService(it);
			} catch (Throwable e) {
				e.printStackTrace();
			}
        	
        }else if(action.equals(IntentConstant.STOP_ACTION)){
        	try {
                SpeechUtil.getInstance(context).stopSpeak();
                if(ShutdownActivity.shutdownActivity != null){
                    ShutdownActivity.shutdownActivity.finish();
                }
	        } catch (Throwable e) {
				e.printStackTrace();
			}
        }else if(action.equals(IntentConstant.ALARM_SHUTDOWN)){ //设置的关机时间到了
            Log.e(TAG, "getLocalDatetime: " + DateUtil.getLocalDatetime() );
            Intent in = new Intent(context, ShutdownActivity.class);
            in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            in.putExtra("type",3);
            context.startActivity(in);
        }else if(action.equals(IntentConstant.ACTION_BATTERY_RECV)){    //手机端通过主服务发送过来的查询关机广播 result:-2
            String from = intent.getStringExtra("from");
            String function = intent.getStringExtra("function");
            Log.e(TAG, "onReceive: from:" + from );
            Log.e(TAG, "onReceive: function:" + function );
            Log.e(TAG, "onReceive: result:" + result );
            if("-2".equals(result)){
                SPUtil spUtil = new SPUtil(context);
                int shutdownTime = spUtil.getInt("shutdownTime");
                long oldLocaltime = spUtil.getLong("oldLocaltime");
                Log.e(TAG, "onReceive: saved shutdownTime:" + shutdownTime );
                Log.e(TAG, "onReceive: oldLocaltime:" + oldLocaltime );
                if(shutdownTime != 0 && oldLocaltime != 0 ){
                    long curLocaltime = new Date().getTime();
                    int overSecond = (int)((curLocaltime - oldLocaltime) / 1000);
                    sendBroadcast2MainService(context,shutdownTime - overSecond);
                }
            }

        }
	}

    private void sendBroadcast2MainService(Context context,int shutdownTime) { //发送单位为秒的广播给主服务
        Intent intent = new Intent(IntentConstant.ACTION_MAIN_RECE);
        intent.putExtra("from", context.getPackageName());
        intent.putExtra("function", "shutdown");
        intent.putExtra("result", shutdownTime);
        context.sendBroadcast(intent);
        Log.e(TAG, "query shutdownTime:" + shutdownTime + "秒");
    }

}
