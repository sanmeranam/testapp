package com.cloud4form.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Point;
import android.os.Build;
import android.provider.Settings;
import android.view.Display;
import android.view.WindowManager;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Santanu Kumar Sahu on 8/13/2016.
 */
public class Util {
    private static Util instance;
    public static Util getInstance(Context context){
        if(Util.instance==null){
            Util.instance=new Util(context);
        }
        return Util.instance;
    }

    private Context context;
    public SharedPreferences PREFF;
    public JSONObject AppConfig;
    private Util(Context context){
        this.context=context;
        this.PREFF = this.context.getSharedPreferences(this.context.getString(R.string.file_master), Context.MODE_PRIVATE);
        this.AppConfig=this.loadAssetFile();
    }

    private JSONObject loadAssetFile(){
        try {
            AssetManager manager = context.getAssets();
            InputStream file = manager.open("app_config.json");
            byte[] formArray = new byte[file.available()];
            file.read(formArray);
            file.close();

            return new JSONObject(new String(formArray));
        }catch (Exception ex){
            return null;
        }
    }

    public String getDeviceId(){
        return Settings.Secure.getString(this.context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public String getDeviceModel(){
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    public String getDeviceScreen(){
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        return height+"x"+width;
    }

    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }


}
