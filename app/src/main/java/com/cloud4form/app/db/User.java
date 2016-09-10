package com.cloud4form.app.db;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by I326482 on 9/9/2016.
 */
public class User extends Entity{
    private String firstName;
    private String lastName;
    private String contact;
    private String email;
    private String image;
    private String serverId;

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }


    public User(JSONObject obj){
        try{
            this.serverId=obj.getString("_id");
            this.firstName=obj.getString("first_name");
            this.lastName=obj.getString("last_name");
            this.email=obj.getString("email");
            this.image=obj.getString("profile");
            if(obj.has("contact"))
                this.contact=obj.getString("contact");
        }catch (Exception ex){

        }
    }

    @Override
    public boolean equals(Object o) {
        return this.getServerId().equals(((User)o).getServerId());
    }
}