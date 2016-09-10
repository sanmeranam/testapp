package com.cloud4form.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.WindowManager;

import com.cloud4form.app.db.FileConnection;
import com.cloud4form.app.db.User;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.InetAddress;

/**
 * Created by Santanu Kumar Sahu on 8/13/2016.
 */
public class AppController {



    public static final String SERVICE_INCOMING_MSG="SERVICE_INCOMING_MSG";
    public static final String ARG_MSG_FROM="ARG_MSG_FROM";
    public static final String ARG_MSG_TO="ARG_MSG_TO";
    public static final String ARG_MSG_DATA="ARG_MSG_DATA";
    public static final String ARG_MSG_CONTENT="ARG_MSG_CONTENT";

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

    public static User CURRENT_USER;

    public SharedPreferences PREFF;
    public JSONObject AppConfig;

    private static AppController instance;
    private Context context;

    public FileConnection Filo;

    private AppController(Context context){
        this.context=context;
        this.PREFF = this.context.getSharedPreferences(PREFF_FILE, Context.MODE_PRIVATE);
        this.AppConfig=this.loadAssetFile();
        Filo=new FileConnection(this.context);
    }

    public void setPref(String key,String value){
        this.PREFF.edit().putString(key,value).commit();
    }


    public static AppController getInstance(Context context){
        if(AppController.instance==null){
            AppController.instance=new AppController(context);
        }
        return AppController.instance;
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

    public String checkGCMToken(){

        String sCurrentToken=this.getPref(AppController.PREE_GCM_TOKEN,"");
        sCurrentToken=sCurrentToken.trim();

        if(sCurrentToken.length()==0){
            try{
                InstanceID instanceID = InstanceID.getInstance(this.context);
                String gcm_token = instanceID.getToken(this.context.getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                gcm_token=gcm_token.trim();

                if(gcm_token.length()>0 && !gcm_token.equals(sCurrentToken)){
                    this.setPref(AppController.PREE_GCM_TOKEN,gcm_token);
                    return gcm_token;
                }else{
                    return sCurrentToken;
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        return sCurrentToken;
    }

    public String generateURL(String base,String path){
        try {
            String sBaseValue=this.AppConfig.getString(base);
            String sBasePath=this.AppConfig.getString(path);
            String sFullURL=sBaseValue+sBasePath;

            if(sFullURL.contains("{domain}")){
                sFullURL=sFullURL.replace("{domain}",this.getPref(AppController.PREE_APP_TENANT));
            }
            return sFullURL;
        }catch (Exception ex){
            return null;
        }
    }

    public static boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com"); //You can replace it with your name
            return !ipAddr.equals("");

        } catch (Exception e) {
            return false;
        }

    }

    private iNotify listenr1;
    private iNotify listenr2;



    public void attachListener1(iNotify listenr1){
        this.listenr1=listenr1;
    }

    public void attachListener2(iNotify listenr2){
        this.listenr2=listenr2;
    }

    public void removeListener1(){
        this.listenr1=null;
    }

    public void removeListener2(){
        this.listenr2=null;
    }

    public static boolean doNotify(Bundle bundle){
        boolean isSent=false;

        if(AppController.instance!=null){
            if(AppController.instance.listenr1!=null){
                AppController.instance.listenr1.onMessage(bundle);
                isSent=true;
            }

            if(AppController.instance.listenr2!=null){
                AppController.instance.listenr2.onMessage(bundle);
                isSent=true;
            }
        }
        return isSent;
    }

    public interface iNotify{
        public void onMessage(Bundle bundle);
    }
}
