package com.yongyida.robot.voice.battery.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.yongyida.robot.voice.battery.R;


/**
 *  RS20的关机dialog
 * */
public class ShutDownDialog extends AlertDialog implements OnClickListener{

    private final String TAG = ShutDownDialog.class.getSimpleName() ;
    private TextView tipsTvw;
    private Button cancelBtn;
    private Button confirmBtn;

    private int recLen = 11;
    Handler handler = new Handler();
    private ShutDownListener listener;
    private  Context context;
    private boolean isRun = true;
    private int action = -1;

    public static final int ACTION_SHUTDOWN             = 0;
    public static final int ACTION_REBOOT               = 1;
    public static final int ACTION_LONG_PRESS_POWER     = 2;

    public ShutDownDialog(Context context,int action){
        super(context);

        if(context instanceof ShutDownListener){

            listener = (ShutDownListener) context;
        }
        this.context = context ;
        this.action = action ;

    }

    public static ShutDownDialog newInstance(Context mContext, int actionTpye) {

        ShutDownDialog newFragment = new ShutDownDialog(mContext,actionTpye);

        return newFragment;
    }


    public interface ShutDownListener{

        void onShutDownListenerCancel();
        void onShutDown() ;
        void onReboot() ;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_shut_down);
        setCanceledOnTouchOutside(false);

        tipsTvw = (TextView)findViewById(R.id.tips);
        cancelBtn = (Button) findViewById(R.id.cancel);
        cancelBtn.setOnClickListener(this);
        confirmBtn = (Button) findViewById(R.id.confirm);
        confirmBtn.setOnClickListener(this);

        tipsTvw.setTextColor(context.getResources().getColor(R.color.text_warn));
        if (action == ACTION_REBOOT) {
            tipsTvw.setText(R.string.tips_reboot);
        } else {
            tipsTvw.setText(R.string.tips_shutdown);
        }

        if(action != ACTION_LONG_PRESS_POWER){

            handler.postDelayed(runnable,3000);
        }

        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        //这句就是设置dialog横向满屏了。
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.confirm:

                Log.i(TAG, "dyc_battery_left");
                if (action == ACTION_REBOOT) {
                    Log.i(TAG, "reboot start");
                    onReboot();
                } else {
                    Log.i(TAG, "shutdown start");
                    onShutDown() ;
                }
                break;
            case R.id.cancel:
                if(listener != null){

                    listener.onShutDownListenerCancel();
                }
                handler.removeCallbacks(runnable);
                isRun = false;
                action = -1;
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
                tipsTvw.setTextColor(context.getResources().getColor(R.color.text_white));
                tipsTvw.setText(recLen + "s");
                handler.postDelayed(this, 1000);
            }else{
                if(isRun){
                    if (action == ACTION_REBOOT) {
                        Log.i(TAG, "reboot start");
                        onReboot();
                    } else {
                        Log.i(TAG, "shutdown start");
                        onShutDown() ;
                    }
                }
                Log.i(TAG, "isRun:" + isRun);
            }

        }
    };


    private void onShutDown(){

        dismiss();

        if(listener != null){

            listener.onShutDown();
        }
    }


    private void onReboot() {

        dismiss();

        if(listener != null){

            listener.onReboot();
        }
    }

}
