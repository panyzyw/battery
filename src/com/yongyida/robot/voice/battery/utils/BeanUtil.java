package com.yongyida.robot.voice.battery.utils;

import com.google.gson.Gson;

/**
 * Created by panyzyw on 2017/6/19.
 */

public class BeanUtil {

    public static <T> T parseJsonWithGson(String json, Class<T> cls){
        try{
            Gson gson = new Gson();
            return gson.fromJson(json,cls);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

}
