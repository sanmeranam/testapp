package com.cloud4form.app.other;

/**
 * Created by I326482 on 9/7/2016.
 */
public class ChatItemEntity {
    public static enum _TYPE{DOWN,UP};

    public String message;
    public String from;
    public String to;
    public _TYPE type;
    public long time;
    public int read=0;

    public ChatItemEntity(String from,String message,long time){
        this.type=_TYPE.DOWN;
        this.from=from;
        this.message=message;
        this.time=time;
    }

    public ChatItemEntity(String to,String message){
        this.type=_TYPE.UP;
        this.to=to;
        this.message=message;
    }



}
