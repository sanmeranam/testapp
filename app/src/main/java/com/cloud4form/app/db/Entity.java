package com.cloud4form.app.db;

/**
 * Created by I326482 on 9/9/2016.
 */
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Date;


public abstract class Entity implements Serializable{
    protected int id;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
}
