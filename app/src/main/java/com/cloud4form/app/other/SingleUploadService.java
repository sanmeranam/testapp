package com.cloud4form.app.other;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.cloud4form.app.AppController;
import com.cloud4form.app.R;
import com.cloud4form.app.db.FormData;
import com.cloud4form.app.pages.ChatProfileActivity;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadStatusDelegate;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class SingleUploadService extends Service {
    public static boolean isRunningService=false;

    private FormData formData;
    private AppController controller;

    public SingleUploadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isRunningService=true;

        controller=AppController.getInstance(this);

        formData=(FormData) intent.getSerializableExtra("data");
        HashMap<String,FormData.FieldEntity> data= formData.getData();

        if(data==null)
            return START_NOT_STICKY;

        formData.SentStatus= FormData.SENT_STATUS.SENDING;
        saveFormData();

        Set<String> sKeys=data.keySet();

        boolean hasFileToUpload=false;

        for(String key:sKeys){
            FormData.FieldEntity entity=data.get(key);
            if(isAttachment(entity) &&
                    (entity.SentStatus.equals(FormData.SENT_STATUS.ERROR)  || entity.SentStatus.equals(FormData.SENT_STATUS.NEW))  &&
                    entity.value.trim().length()>0){

                fileUpload(entity,formData);
                hasFileToUpload=true;
            }
        }

        if(!hasFileToUpload){
            //Only data to upload
            uploadFieldData();
            SingleUploadService.this.stopSelf();
            isRunningService=false;
        }

        return START_STICKY;
    }



    private boolean isAttachment(FormData.FieldEntity entity){
        return entity.type.equalsIgnoreCase("audio_record")||
                entity.type.equalsIgnoreCase("video_record")||
                entity.type.equalsIgnoreCase("sign_input")||
                entity.type.equalsIgnoreCase("file_attach")||
                entity.type.equalsIgnoreCase("photo_attach");
    }

    private boolean checkDataStatus(FormData.SENT_STATUS status,boolean any){
        HashMap<String,FormData.FieldEntity> data= formData.getData();
        if(data==null)
            return false;

        int count=0;
        int sentCount=0;
        Set<String> sKeys=data.keySet();
        for(String key:sKeys){
            FormData.FieldEntity entity=data.get(key);
            if(isAttachment(entity) && entity.value.trim().length()>0){
                count++;
                if(entity.SentStatus.equals(status)){
                    sentCount++;
                    if(any){
                        return true;
                    }
                }
            }
        }


        return count==sentCount;
    }

    private void saveFormData(){
        ArrayList<FormData> list=controller.Filo.readArray(FormData.class);
        int index=-1;
        for(int i=0;i<list.size();i++){
            FormData fd=list.get(i);
            if(fd.getInternalId().equals(formData.getInternalId())){
                index=i;
                break;
            }
        }
        if(index!=-1){
            list.remove(index);
        }
        list.add(formData);

        controller.Filo.saveArray(list,FormData.class);
    }

    private void onFileUploadComplete(FieldResponse response,FormData.FieldEntity data){
        if(response==null){
            data.value="";
            data.SentStatus= FormData.SENT_STATUS.ERROR;
        }else{
            data=formData.getData().get(response.field_id);
            data.value=response.value;
            data.SentStatus= FormData.SENT_STATUS.SENT;

            if(checkDataStatus(FormData.SENT_STATUS.SENT,false)){
                uploadFieldData();

            }else if(checkDataStatus(FormData.SENT_STATUS.ERROR,true) && !checkDataStatus(FormData.SENT_STATUS.NEW,true)){
                formData.SentStatus= FormData.SENT_STATUS.ERROR;
                saveFormData();
                SingleUploadService.this.stopSelf();
                isRunningService=false;
            }
        }
    }

    private void sendNotification(String message,String title) {

        Intent intent = new Intent(this, ChatProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_action_crop)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }

    private void dataSent(JSONObject res){

        if(res==null){
            formData.SentStatus= FormData.SENT_STATUS.ERROR;
            sendNotification("Error uploading data.","Data Sync");
        }else{
            formData.SentStatus= FormData.SENT_STATUS.SENT;
            formData.setServerId(getUploadedDataId(res));
            sendNotification("Data uploaded successfully!","Data Sync");
        }
        saveFormData();
        SingleUploadService.this.stopSelf();
        isRunningService=false;
    }

    private void uploadFieldData(){
        JSONObject dataToSend=new JSONObject();
        try {

            formData.SentStatus= FormData.SENT_STATUS.SENDING;

            dataToSend.put("internal_id",formData.getInternalId());
            dataToSend.put("meta_id",formData.getMetaId());
            dataToSend.put("create_date",formData.getCreateDate());
            dataToSend.put("version",formData.getVersion());
            dataToSend.put("user",controller.getAsJSON(AppController.PREE_USER_PROFILE).getString("_id"));

            JSONArray arrData=new JSONArray();
            JSONObject oData;

            HashMap<String,FormData.FieldEntity> data= formData.getData();
            if(data!=null){
                Set<String> sKeys=data.keySet();
                for(String key:sKeys){
                    oData=new JSONObject();
                    FormData.FieldEntity entity=data.get(key);
                    oData.put("_i",entity.fieldId);
                    oData.put("_l",entity.label);
                    oData.put("_v",entity.value);
                    oData.put("_t",entity.type);

                    arrData.put(oData);
                }
            }

            dataToSend.put("data",arrData);


            new GenericAsyncTask(controller.generateURL("api_url","create_form"),new GenericAsyncTask.IAsyncCallback() {
                @Override
                public void onResult(JSONObject response) {
                    dataSent(response);
                }
            }).execute(dataToSend);


        }catch (Exception ex){
            formData.SentStatus= FormData.SENT_STATUS.ERROR;
        }
    }

    synchronized private boolean fileUpload(final FormData.FieldEntity data,FormData formData){
        String params="?field_id="+data.fieldId+"&field_type="+data.type+"&temp_form_id="+formData.getInternalId();
        String url=controller.generateURL("api_url","upload_url")+params;
        try {
            MultipartUploadRequest mpreq = new MultipartUploadRequest(this, url);
            if(data.value.contains("##")){
                String[] vals=data.value.split("##");
                int num=0;
                for(String v:vals){
                    mpreq.addFileToUpload(v,data.fieldId+(num++));
                }
            }else{
                mpreq.addFileToUpload(data.value,data.fieldId);
            }
            UploadNotificationConfig  config=new UploadNotificationConfig();
            String sName=data.label==null?"File":data.label;
            config.setTitle("Uploading "+sName);
            config.setRingToneEnabled(false);
            config.setAutoClearOnSuccess(true);
//            config.setAutoClearOnError(true);

            mpreq.setNotificationConfig(config)
                    .setMaxRetries(3)
                    .setDelegate(new UploadStatusDelegate() {
                        @Override
                        public void onProgress(UploadInfo uploadInfo) {
                            data.SentStatus= FormData.SENT_STATUS.SENDING;
                        }

                        @Override
                        public void onError(UploadInfo uploadInfo, Exception exception) {
                            onFileUploadComplete(null,data);
                        }

                        @Override
                        public void onCompleted(UploadInfo uploadInfo, ServerResponse serverResponse) {
                            if(serverResponse.getHttpCode()==200){
                                try {
                                    JSONObject res=new JSONObject(serverResponse.getBodyAsString());
                                    if(res.has("success") && res.getInt("success")==1){
                                        if(res.getInt("array")==1){
                                            JSONArray resBody=res.getJSONArray("data");
                                            onFileUploadComplete(parseResponse(resBody),data);
                                        }else{
                                            JSONObject resBody=res.getJSONObject("data");
                                            onFileUploadComplete(parseResponse(resBody),data);
                                        }
                                    }
                                }catch (Exception ex){
                                    onFileUploadComplete(null,data);
                                }
                            }else{
                                onFileUploadComplete(null,data);
                            }
                        }

                        @Override
                        public void onCancelled(UploadInfo uploadInfo) {
                            onFileUploadComplete(null,data);
                        }
                    })
                    .startUpload();

        } catch (Exception exc) {
            Log.e("AndroidUploadService", exc.getMessage(), exc);
        }
        return false;
    }

    private String getUploadedDataId(JSONObject res){
        String sId="";
        try{
            JSONArray insId;

            if(res.get("data") instanceof JSONArray){
                insId=res.getJSONArray("data").getJSONObject(0).getJSONArray("insertedIds");
            }else{
                insId=res.getJSONObject("data").getJSONArray("insertedIds");
            }

            sId=insId.getString(0);
        }catch (Exception ex){

        }

        return sId;
    }

    private FieldResponse parseResponse(JSONObject res){
        FieldResponse result=new FieldResponse();
        try{
            JSONArray ops=res.getJSONArray("ops");
            JSONObject body=ops.getJSONObject(0);

            result.field_id=body.getJSONObject("params").getString("field_id");
            result.field_type=body.getJSONObject("params").getString("field_type");
            result.form_id=body.getJSONObject("params").getString("temp_form_id");

            result.value=body.getString("_id");

        }catch (Exception ex){

        }

        return  result;
    }

    private FieldResponse parseResponse(JSONArray arrres){
        FieldResponse result=new FieldResponse();
        for(int i=0;i<arrres.length();i++) {
            try {
                JSONObject res=arrres.getJSONObject(i);
                JSONArray ops = res.getJSONArray("ops");
                JSONObject body = ops.getJSONObject(0);


                result.field_id=body.getJSONObject("params").getString("field_id");
                result.field_type=body.getJSONObject("params").getString("field_type");
                result.form_id=body.getJSONObject("params").getString("temp_form_id");

                result.value=result.value.length()==0? body.getString("_id"):result.value+"|"+body.getString("_id");

            } catch (Exception ex) {

            }
        }

        return  result;
    }

    private class FieldResponse{
        String field_id="";
        String field_type="";
        String form_id="";
        String value="";
    }
}
