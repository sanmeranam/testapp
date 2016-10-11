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

import com.cloud4form.app.AppController;
import com.cloud4form.app.FormListView;
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

public class SingleUploadService extends Service {
    public static boolean isRunningService=false;

    private FormData formData;
    private AppController controller;
    private ArrayList<FormData.FieldEntity> fieldList;

    private static final String FILE_FIELD_SEP="##";

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.controller=AppController.getInstance(this);
        this.formData =(FormData) intent.getSerializableExtra("data");

        if(this.formData.getFields()==null){
            this.stopSelf();
            return START_NOT_STICKY;
        }
        if(!AppController.isInternetAvailable()){
            this.stopSelf();
            return START_NOT_STICKY;
        }

        SingleUploadService.isRunningService=true;


        this.fieldList=new ArrayList<>(this.formData.getFields().values());

        formData.SentStatus= FormData.SENT_STATUS.SENDING;
        doSaveFormData();

        boolean hasFileToUpload=false;
        for(FormData.FieldEntity entity:fieldList){
            if(isAttachmentField(entity) &&
                    (entity.SentStatus.equals(FormData.SENT_STATUS.ERROR)  || entity.SentStatus.equals(FormData.SENT_STATUS.NEW))  &&
                    entity.value.trim().length()>0){

                doFileFieldUpload(entity,formData);
                hasFileToUpload=true;
            }
        }

        if(!hasFileToUpload){
            doUploadFieldData();
        }

        return START_STICKY;
    }

    /**
     *
     * @param data
     * @param formData
     * @return
     */
    private void doFileFieldUpload(final FormData.FieldEntity data, FormData formData){
        String params="?field_id="+data.fieldId+"&field_type="+data.type+"&temp_form_id="+formData.getInternalId();
        String url=controller.generateURL("api_url","upload_url")+params;

        try {
            MultipartUploadRequest mpRequest = new MultipartUploadRequest(this, url);

            if(data.value.contains(FILE_FIELD_SEP)){
                String[] vals=data.value.split(FILE_FIELD_SEP);
                for(int i=0;i<vals.length;i++){
                    mpRequest.addFileToUpload(vals[i],data.fieldId+i);
                }
            }else{
                mpRequest.addFileToUpload(data.value,data.fieldId);
            }

            UploadNotificationConfig  config=new UploadNotificationConfig();
            String sName=data.label==null?"File":data.label;
            config.setTitle("Uploading "+sName);
            config.setRingToneEnabled(false);
            config.setAutoClearOnSuccess(true);
            Intent formd=new Intent(this,FormListView.class);
            config.setClickIntent(formd);

            mpRequest.setNotificationConfig(config)
                    .setMaxRetries(2)
                    .setDelegate(new UploadStatusDelegate() {
                        @Override
                        public void onProgress(UploadInfo uploadInfo) {
                            data.SentStatus= FormData.SENT_STATUS.SENDING;
                        }

                        @Override
                        public void onError(UploadInfo uploadInfo, Exception exception) {
                            onAfterFileUpload(null,data);
                        }

                        @Override
                        public void onCompleted(UploadInfo uploadInfo, ServerResponse serverResponse) {
                            if(serverResponse.getHttpCode()==200){
                                try {
                                    JSONObject res=new JSONObject(serverResponse.getBodyAsString());
                                    if(res.has("success") && res.getInt("success")==1){
                                        if(res.getInt("array")==1){
                                            JSONArray resBody=res.getJSONArray("data");
                                            onAfterFileUpload(parseResponse(resBody),data);
                                        }else{
                                            JSONObject resBody=res.getJSONObject("data");
                                            onAfterFileUpload(parseResponse(resBody),data);
                                        }
                                    }
                                }catch (Exception ex){
                                    onAfterFileUpload(null,data);
                                }
                            }else{
                                onAfterFileUpload(null,data);
                            }
                        }

                        @Override
                        public void onCancelled(UploadInfo uploadInfo) {
                            onAfterFileUpload(null,data);
                        }
                    })
                    .startUpload();

        } catch (Exception exc) {
            onAfterFileUpload(null,data);
        }
    }

    /**
     *
     * @param response
     * @param data
     */
    private void onAfterFileUpload(FieldResponse response, FormData.FieldEntity data){

        if(response==null){
            data.value="";
            data.SentStatus= FormData.SENT_STATUS.ERROR;
        }else{
            data=this.formData.getFields().get(response.field_id);
            data.value=response.value;
            data.SentStatus= FormData.SENT_STATUS.SENT;
        }

        /**
         * Check all file fields are uploaded or attempted
         */
        if(checkDataStatus(FormData.SENT_STATUS.SENT,false)){
            //All sent successfully
            doUploadFieldData();
        }else if(checkDataStatus(FormData.SENT_STATUS.ERROR,true)
                && !checkDataStatus(FormData.SENT_STATUS.NEW,true)
                && !checkDataStatus(FormData.SENT_STATUS.SENDING,true)){
            //check at least 1 having ERROR, but should not have NEW, should not have SENDING,means all attempted

            this.formData.SentStatus= FormData.SENT_STATUS.ERROR;

            doSaveFormData();
            doMarkComplete(true);
        }
    }

    /**
     *
     */
    private void doUploadFieldData(){
        JSONObject dataToSend=new JSONObject();
        try {
            dataToSend.put("internal_id",formData.getInternalId())
                    .put("meta_id",formData.getMetaId())
                    .put("form_name",formData.getFormName())
                    .put("create_date",formData.getCreateDate())
                    .put("version",formData.getVersion())
                    .put("model",formData.getModel())
                    .put("stage_history",formData.getStageHistory())
                    .put("current_action",formData.getCurrentAction())
                    .put("flow",formData.getFlow());





            JSONArray arrData=new JSONArray();

            for(FormData.FieldEntity entity:SingleUploadService.this.fieldList){
                arrData.put(new JSONObject()
                        .put("_i",entity.fieldId)
                        .put("_l",entity.label)
                        .put("_v",entity.value)
                        .put("_t",entity.type));
            }

            dataToSend.put("data",arrData);

            new GenericAsyncTask(controller.generateURL("api_url","create_form"),new GenericAsyncTask.IAsyncCallback() {
                @Override
                public void onResult(JSONObject response) {
                    onAfterUploadFieldData(response);
                }
            }).execute(dataToSend);


        }catch (Exception ex){
            onAfterUploadFieldData(null);
        }
    }

    /**
     *
     * @param res
     */
    private void onAfterUploadFieldData(JSONObject res){

        if(res==null){
            formData.SentStatus= FormData.SENT_STATUS.ERROR;
            sendNotification("Error uploading data.","Data Sync");
        }else{
            formData.SentStatus= FormData.SENT_STATUS.SENT;
            formData.setServerId(getUploadedDataId(res));
            sendNotification("Data uploaded successfully!","Data Sync");
        }
        doSaveFormData();
        doMarkComplete(res==null);
    }

    /**
     *
     */
    private void doSaveFormData(){
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

    /**
     *
     */
    private void doMarkComplete(boolean finalFailded){
        SingleUploadService.isRunningService=false;
        this.stopSelf();

        if(finalFailded) {
            Intent myIntent = new Intent(this, FormUploadService.class);
            this.startService(myIntent);
        }
    }

    /**
     *
     * @param entity
     * @return
     */
    private boolean isAttachmentField(FormData.FieldEntity entity){
        return entity.type.equalsIgnoreCase("audio_record")||
                entity.type.equalsIgnoreCase("video_record")||
                entity.type.equalsIgnoreCase("sign_input")||
                entity.type.equalsIgnoreCase("file_attach")||
                entity.type.equalsIgnoreCase("photo_attach");
    }

    private boolean checkDataStatus(FormData.SENT_STATUS status,boolean any){
        int count=0 ,sentCount=0;
        for(FormData.FieldEntity entity:this.fieldList){
            if(isAttachmentField(entity) && entity.value.trim().length()>0){
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
