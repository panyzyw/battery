package com.yongyida.robot.voice.battery.utils;
import com.yongyida.robot.voice.battery.constant.IntentConstant;

import android.app.Activity;
import android.app.Service;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Created by Pasu on 2015/7/15.
 */
public class FristUtil {
    private Context context;
    private SharedPreferences sharedPreferences;
    private Editor editor;
    public FristUtil(Context context)
    {
        super();
        this.context=context;
        sharedPreferences=context.getSharedPreferences(IntentConstant.FRIST,context.MODE_PRIVATE);
        editor=sharedPreferences.edit();
    }

    public void putString(String key){
    	editor.putString(IntentConstant.FRIST_KEY_STRING, key);
    	editor.commit();
    }
    
    public String getString(){
    	return sharedPreferences.getString(IntentConstant.FRIST_KEY_STRING, "");
    }
    
    public void putLong(Long value){
    	editor.putLong(IntentConstant.FRIST_KEY_LONG, value);
    	editor.commit();
    }
    
    public Long getLong(){
    	return sharedPreferences.getLong(IntentConstant.FRIST_KEY_LONG, 0);
    }
    
    public void putInt(int value){
    	editor.putInt(IntentConstant.FRIST_KEY_TOTAL, value);
    	editor.commit();
    }
    
    public int getInt(){
    	return sharedPreferences.getInt(IntentConstant.FRIST_KEY_TOTAL, 0);
    }
    
    public boolean contains(String key){
    	return sharedPreferences.contains(key);
    }
    
    
    
    public void setIsFirstRun(boolean isfirstrun)
    {
        editor.putBoolean(IntentConstant.FRIST_KEY_ISFIRSTRUN,isfirstrun);
        editor.commit();
    }

    public boolean getIsFirstRun()
    {
        return sharedPreferences.getBoolean(IntentConstant.FRIST_KEY_ISFIRSTRUN,true);
    }

}
