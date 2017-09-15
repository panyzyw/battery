package com.yongyida.robot.voice.battery.utils;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.yongyida.robot.voice.battery.constant.IntentConstant;

/**
 * Created by Pasu on 2015/7/15.
 */
public class SPUtil {
    private Context context;
    private SharedPreferences sharedPreferences;
    private Editor editor;

    public SPUtil(Context context) {
        super();
        this.context=context;
        sharedPreferences=context.getSharedPreferences(IntentConstant.SP,context.MODE_PRIVATE);
        editor=sharedPreferences.edit();
    }

    public void putString(String key){
    	editor.putString(IntentConstant.SP_KEY_STRING, key);
    	editor.commit();
    }
    
    public String getString(){
    	return sharedPreferences.getString(IntentConstant.SP_KEY_STRING, "");
    }
    
    public void putLong(Long value){
    	editor.putLong(IntentConstant.SP_KEY_LONG, value);
    	editor.commit();
    }
    
    public Long getLong(){
    	return sharedPreferences.getLong(IntentConstant.SP_KEY_LONG, 0);
    }
    
    public void putInt(int value){
    	editor.putInt(IntentConstant.SP_KEY_TOTAL, value);
    	editor.commit();
    }

    public void putInt(String key,int value){
        editor.putInt(key, value);
        editor.commit();
    }
    
    public int getInt(){
    	return sharedPreferences.getInt(IntentConstant.SP_KEY_TOTAL, 0);
    }

    public int getInt(String key){
        return sharedPreferences.getInt(key, 0);
    }

    public void putLong(String key,long value){
        editor.putLong(key,value);
        editor.commit();
    }

    public long getLong(String key){
        return sharedPreferences.getLong(key,0);
    }

}
