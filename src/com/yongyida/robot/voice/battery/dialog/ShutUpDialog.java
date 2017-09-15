package com.yongyida.robot.voice.battery.dialog;

import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import com.yongyida.robot.voice.battery.R;

public class ShutUpDialog extends DialogFragment implements OnClickListener{
	
	private final String TAG = "ShutUpDialog";
	private TextView count_tv; 
	private TextView title_tv; 
	private Button left_btn;
	private Button right_btn;
	private int recLen = 11;   
    Handler handler = new Handler();
    private ShutDownListener listener;
    //private Context context;
    private static Context context;
    private boolean isRun = true;
    //private int action = -1;
    private static int action = -1;
    public static final int ACTION_SHUTDOWN = 0;
    public static final int ACTION_REBOOT = 1;

    public ShutUpDialog(){}

    public static ShutUpDialog newInstance(Context mContext, int actionTpye) {
        ShutUpDialog newFragment = new ShutUpDialog();
        context = mContext;
        action = actionTpye;
        return newFragment;
    }
    public interface ShutDownListener{
        void onShutDownListenerSure();
        void onShutDownListenerCancel();
        void onShutDownListenerComplete(); 
    }
    
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
    	getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);	
    	 View view = inflater.inflate(R.layout.dilog, null);
    	 count_tv = (TextView)view.findViewById(R.id.message);
    	 left_btn = (Button) view.findViewById(R.id.left);
    	 left_btn.setOnClickListener(this);
    	 right_btn = (Button) view.findViewById(R.id.right);
    	 right_btn.setOnClickListener(this);
    	 title_tv = (TextView) view.findViewById(R.id.title);
        if (action == ACTION_REBOOT) {
            title_tv.setText(getResources().getString(R.string.dyc_reboot));
        } else {
            title_tv.setText(getResources().getString(R.string.dyc_poweroff));
        }
    	 handler.post(runnable);
    	  listener = (ShutDownListener) getActivity(); 
		return view;
	}
    
    @Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.left:
			Log.i(TAG, "dyc_battery_left");
                if (action == ACTION_REBOOT) {
                    Log.i(TAG, "reboot start");
                    onReboot();
                } else {
                    Intent intent = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
                    intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
                    //其中false换成true,会弹出是否关机的确认窗口
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
                listener.onShutDownListenerSure();
                //getActivity().finish();
                break;
            case R.id.right:
                listener.onShutDownListenerCancel();
                handler.removeCallbacks(runnable);
                isRun = false;
                action = -1;
                //getActivity().finish();
                break;
            default:
                break;
        }
    }

    Runnable runnable = new Runnable() { 
        @Override 
        public void run() { 
            
        	recLen--;
            if(recLen > 0){
            count_tv.setText(recLen + "s"); 
            handler.postDelayed(this, 1000);
            }else{
            	if(isRun){
                    if (action == ACTION_REBOOT) {
                        Log.i(TAG, "reboot start");
                        onReboot();
                    } else {
                        Log.i(TAG, "shutdown start");
                        Intent intent = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
                        intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
                        //其中false换成true,会弹出是否关机的确认窗口
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                }
            	Log.i(TAG, "isRun:" + isRun);
            	listener.onShutDownListenerComplete();
            }
          
        }
    };

    void onReboot() {
        Intent intent2 = new Intent(Intent.ACTION_REBOOT);
        intent2.putExtra("nowait", 1);
        intent2.putExtra("interval", 1);
        intent2.putExtra("window", 0);
        context.sendBroadcast(intent2);
    }
    
	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
	}

    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }

}
