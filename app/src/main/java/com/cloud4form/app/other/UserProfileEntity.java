package com.cloud4form.app.other;

import android.graphics.Bitmap;

import org.json.JSONObject;

/**
 * Created by I326482 on 9/7/2016.
 */
public class UserProfileEntity {

    public String id;
    public String firstName;
    public String lastName;
    public String email;
    public String chat_token;
    public GroupEntity groupEntity;
    public boolean isAdmin=false;
    public Bitmap profile;


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


}
