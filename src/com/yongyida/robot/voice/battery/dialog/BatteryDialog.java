package com.yongyida.robot.voice.battery.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.yongyida.robot.voice.battery.R;

public class BatteryDialog extends AlertDialog implements OnClickListener{

    private final String TAG = BatteryDialog.class.getSimpleName() ;
    private TextView tipsTvw;
    private Button confirmBtn;

    private Context context;
    private int battery = -1 ;

    public BatteryDialog(Context context, int battery){
        super(context,R.style.Dialog);

        this.battery = battery ;
        this.context = context ;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_battery);

        tipsTvw = (TextView)findViewById(R.id.tips);
        confirmBtn = (Button) findViewById(R.id.confirm);
        confirmBtn.setOnClickListener(this);

        changView();

        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        //这句就是设置dialog横向满屏了。
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);

        window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
    }

    @Override
    public void onClick(View v) {

        dismiss();
    }

    public void setBattery(int battery) {

        if(this.battery != battery){
            this.battery = battery;
            changView();
        }
    }

    private void changView(){

        Drawable top ;

        tipsTvw.setText(context.getString(R.string.tips_electricity,battery+"%"));
        if(battery <10){

            tipsTvw.setTextColor(context.getResources().getColor(R.color.text_error));

            top = context.getResources().getDrawable(R.drawable.electricity_empty);
            top.setBounds(0,0,top.getMinimumWidth(), top.getMinimumHeight());
            tipsTvw.setCompoundDrawables(null,top,null,null);

        }else if ((battery <30)){

            tipsTvw.setTextColor(context.getResources().getColor(R.color.text_warn));

            top = context.getResources().getDrawable(R.drawable.electricity_low);
            top.setBounds(0,0,top.getMinimumWidth(), top.getMinimumHeight());

        }else{

            top = null ;
        }
        tipsTvw.setCompoundDrawables(null,top,null,null);
    }


}
