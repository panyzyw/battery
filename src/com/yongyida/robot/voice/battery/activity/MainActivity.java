package com.yongyida.robot.voice.battery.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.yongyida.robot.voice.battery.R;
import com.yongyida.robot.voice.battery.service.MonitorService;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Intent in = new Intent(MainActivity.this, MonitorService.class);
		MainActivity.this.startService(in);
		finish();
	}
}
