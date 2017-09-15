package com.yongyida.robot.voice.battery.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by panyzyw on 2017/6/22.
 */

public class DateUtil {
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String getLocalDatetime(){
        return simpleDateFormat.format(new Date());
    }

    public static long date2timestamp(String strDate){
        long timestamp = 0;
        try {
            Date date = simpleDateFormat.parse(strDate);
            timestamp = date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timestamp;
    }

    public static long getLocaltimestamp(){
        Date date = new Date();
        return date.getTime();
    }

    public static String timestamp2Datetime(long timestamp){
        return simpleDateFormat.format(new Date(timestamp));
    }
}
