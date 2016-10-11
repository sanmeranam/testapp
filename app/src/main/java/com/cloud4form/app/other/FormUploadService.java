package com.cloud4form.app.other;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.cloud4form.app.AppController;
import com.cloud4form.app.db.FormData;
import com.google.android.gms.analytics.internal.zzy;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadStatusDelegate;

import org.json.JSONObject;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class FormUploadService extends Service {



    public static final long NOTIFY_INTERVAL = 60 * 1000;
    private Handler mHandler = new Handler();
    private Timer mTimer = null;
    private AppController controller;

    @Override
    public void onCreate() {
        super.onCreate();

        controller=AppController.getInstance(this);

        if(mTimer != null) {
            mTimer.cancel();
        } else {
            mTimer = new Timer();
        }
        mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 0, NOTIFY_INTERVAL);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }



    private class TimeDisplayTimerTask extends TimerTask {
        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(!SingleUploadService.isRunningService && AppController.isInternetAvailable()){
                        ArrayList<FormData> dataQueue=controller.Filo.readArray(FormData.class);
                        if(dataQueue.size()>0){
                            for(FormData formData:dataQueue){
                                if(formData.SentStatus.equals(FormData.SENT_STATUS.NEW)||formData.SentStatus.equals(FormData.SENT_STATUS.ERROR)){

                                    Intent uploadServe = new Intent(FormUploadService.this, SingleUploadService.class);
                                    uploadServe.putExtra("data", formData);
                                    FormUploadService.this.startService(uploadServe);

                                    break;
                                }
                            }
                        }
                    }
                }
            });
        }
    }
}
