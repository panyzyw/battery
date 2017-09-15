package com.yongyida.robot.voice.battery.bean;

/**
 * Created by panyzyw on 2017/6/19.
 */

public class ShutDownBean extends BaseBean {
    public Semantic semantic;
    public class Semantic{
        public Slots slots;
    }
    public class Slots{
        public Datetime datetime;
    }
    public class Datetime{
        public String date;
        public String time;
    }
}
