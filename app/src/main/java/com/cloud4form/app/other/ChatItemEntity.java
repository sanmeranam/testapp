package com.cloud4form.app.other;

/**
 * Created by I326482 on 9/7/2016.
 */
public class ChatItemEntity {
    public enum CHAT_STATUS{NEW,SENT,FAILED,RETRYING,DELEVERED};
    String chatText;
    long timestamp;
    CHAT_STATUS status;
    int chat_id;
}
