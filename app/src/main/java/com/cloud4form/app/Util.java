package com.cloud4form.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Point;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.WindowManager;

import com.cloud4form.app.other.UserProfileEntity;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Santanu Kumar Sahu on 8/13/2016.
 */
public class Util {
    public static final String PREFF_FILE="PREFF_FILE_C4F";

    public static final String PREE_APP_CONFIG="PREE_APP_CONFIG";
    public static final String PREE_USER_PROFILE="PREE_USER_PROFILE";
    public static final String PREE_SYNC_TOKEN="PREE_SYNC_TOKEN";
    public static final String PREE_GCM_TOKEN="PREE_GCM_TOKEN";
    public static final String PREE_APP_TENANT="PREE_APP_TENANT";
    public static final String PREE_APP_FORMS="PREE_APP_FORMS";
    public static final String PREE_APP_WORK_MODE="PREE_APP_WORK_MODE";
    public static final String PREE_APP_WORK_MODE_OFFLINE="PREE_APP_WORK_MODE_OFFLINE";
    public static final String PREE_APP_WORK_MODE_ONLINE="PREE_APP_WORK_MODE_ONLINE";
    public static final String PREE_APP_LOCAL_PATH="PREE_APP_LOCAL_PATH";

    public static UserProfileEntity CURRENT_USER;

    public void setPref(String key,String value){
        this.PREFF.edit().putString(key,value).commit();
    }

    public String getPref(String key,String def){
        return this.PREFF.getString(key,def);
    }

    public String getPref(String key){
        return this.PREFF.getString(key,null);
    }

    public void removePref(String key){
        this.PREFF.edit().remove(key).commit();
    }

    public void saveJSONData(JSONObject appConfig,String key){
        this.setPref(key,appConfig.toString());
    }
    public JSONObject getAsJSON(String key){
        try {
            String sData=this.getPref(key,"{}");
            return new JSONObject(sData);
        }catch (Exception ex){
            return new JSONObject();
        }
    }

    public JSONArray getAsJSONArray(String key){
        try {
            String sData=this.getPref(key,"{}");
            return new JSONArray(sData);
        }catch (Exception ex){
            return new JSONArray();
        }
    }

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
        this.PREFF = this.context.getSharedPreferences(PREFF_FILE, Context.MODE_PRIVATE);
        this.AppConfig=this.loadAssetFile();
    }

    @Nullable
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

    public boolean checkGCMToken(){
        boolean bGotNew=false;

        String sCurrentToken=this.getPref(Util.PREE_GCM_TOKEN,"");
        sCurrentToken=sCurrentToken.trim();

        if(sCurrentToken.length()==0){
            try{
                InstanceID instanceID = InstanceID.getInstance(this.context);
                String gcm_token = instanceID.getToken(this.context.getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                gcm_token=gcm_token.trim();

                if(gcm_token.length()>0 && !gcm_token.equals(sCurrentToken)){
                    this.setPref(Util.PREE_GCM_TOKEN,gcm_token);
                    return true;
                }else{
                    return false;
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        return bGotNew;
    }

    public String generateURL(String base,String path){
        try {
            String sBaseValue=this.AppConfig.getString(base);
            String sBasePath=this.AppConfig.getString(path);
            String sFullURL=sBaseValue+sBasePath;

            if(sFullURL.contains("{domain}")){
                sFullURL=sFullURL.replace("{domain}",this.getPref(Util.PREE_APP_TENANT));
            }
            return sFullURL;
        }catch (Exception ex){
            return null;
        }
    }
}
