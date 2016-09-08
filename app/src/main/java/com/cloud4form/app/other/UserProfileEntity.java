package com.cloud4form.app.other;

import android.graphics.Bitmap;

import com.cloud4form.app.pages.ChatActivity;

import org.json.JSONObject;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

/**
 * Created by I326482 on 9/7/2016.
 */
public class UserProfileEntity {

    public static final String ARG_DATA="ARG_DATA";

    public String id;
    public String firstName;
    public String lastName;
    public String email;
    public String chat_token;
    public GroupEntity groupEntity;
    public boolean isAdmin=false;
    public Bitmap profile;
    public Queue<ChatItemEntity> inbox=new ArrayDeque<>();

    public UserProfileEntity(){

    }
    public UserProfileEntity(JSONObject obj){
        try{
            this.id=obj.getString("_id");
            this.firstName=obj.getString("first_name");
            this.lastName=obj.getString("last_name");
            this.email=obj.getString("email");
            this.chat_token=obj.getString("cgm_token");
            this.isAdmin=obj.getBoolean("admin");
        }catch (Exception ex){

        }
    }

    @Override
    public String toString() {
        return id+"##"+firstName+"##"+lastName+"##"+email+"##"+chat_token;
    }

    public static UserProfileEntity parse(String d){
        UserProfileEntity  entity=new UserProfileEntity();
        String[] dd=d.split("##");
        entity.id=dd[0];
        entity.firstName=dd[1];
        entity.lastName=dd[2];
        entity.email=dd[3];
        if(dd.length>3)
        entity.chat_token=dd[4];

        return entity;
    }
}
