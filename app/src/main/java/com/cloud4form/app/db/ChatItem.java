package com.cloud4form.app.db;

import java.util.Date;

/**
 * Created by I326482 on 9/9/2016.
 */
public class ChatItem extends Entity {
    public enum item_type{UP,DOWN};
    private String messageId;
    private String message;
    private String from;
    private String to;
    private Date createDate;
    private boolean read=false;
    private item_type type;
    private boolean changed=false;

    public ChatItem(String from,String message){
        this.from=from;
        this.message=message;
        this.createDate=new Date();
        this.messageId=Math.round(Math.random()*999999)+"";
    }
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public boolean isRead() {
        return read;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public item_type getType() {
        return type;
    }

    public void setType(item_type type) {
        this.type = type;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }
}
