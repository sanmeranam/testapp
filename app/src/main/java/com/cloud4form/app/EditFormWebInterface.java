package com.cloud4form.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.webkit.JavascriptInterface;
import android.webkit.MimeTypeMap;
import android.webkit.WebView;
import android.widget.Toast;

import com.cloud4form.app.audio.AudioRecord;
import com.cloud4form.app.barcode.SimpleScannerActivity;
import com.cloud4form.app.db.FormData;
import com.cloud4form.app.db.FormMeta;
import com.cloud4form.app.filescan.ScannerHomeActivity;
import com.cloud4form.app.other.SingleUploadService;
import com.cloud4form.app.sign.SingCaptureActivity;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by I326482 on 8/14/2016.
 */
public class EditFormWebInterface {
    private Context mContext;
    private WebView _mCView;
    private int baseReqId=1000;
    private AppController controller;
    private Action action;

    private enum PTYPE{PHOTO,FILE_SCAN,VIDEO,AUDIO,BARCODE,GEO,COMPASS,SIGN};
    private HashMap<Integer,ReqPacket> reqMap=new HashMap<>();
    private FormData mCurrentForm;

    /** Instantiate the interface and set the context */
    EditFormWebInterface(Context c, WebView _mCView, FormData mCurrentForm, AppController controller) {
        mContext = c;
        this._mCView=_mCView;
        this.mCurrentForm=mCurrentForm;
        this.controller=controller;
    }

    @JavascriptInterface
    public String getFormModel(){
        String data="{}";
        try{
            JSONObject obj=new JSONObject();
            obj.put("id",this.mCurrentForm.getServerId());
            obj.put("name",this.mCurrentForm.getFormName());
            obj.put("version",this.mCurrentForm.getVersion());
            obj.put("model_view",this.mCurrentForm.getModel());

            Set<String> sKeys=this.mCurrentForm.getFields().keySet();
            JSONObject ddr=new JSONObject();

            for(String key:sKeys){
                ddr.put(key,this.mCurrentForm.getFields().get(key).value);
            }
            obj.put("data",ddr);
            data=obj.toString();
        }catch (Exception ex){
        }

        return data;
    }

    /** Show a toast from the web page */
    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void getLocation(int reqId,String callback) {
        this.reqMap.put(reqId,new ReqPacket(reqId, PTYPE.GEO,callback));
    }

    @JavascriptInterface
    public void getCompass(int reqId,String callback) {
        this.reqMap.put(reqId,new ReqPacket(reqId, PTYPE.COMPASS,callback));
    }


    @JavascriptInterface
    public void capturePhoto(int reqId,String callback) {
        ReqPacket reqPack=new ReqPacket(reqId, PTYPE.PHOTO,callback);

        this.reqMap.put(reqId,reqPack);


        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(this.mContext.getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createFile("jpg");
                reqPack.data=photoFile.getAbsolutePath();
            } catch (IOException ex) {
            }

            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                ((Activity)this.mContext).startActivityForResult(takePictureIntent, reqId);
            }
        }
    }

    @JavascriptInterface
    public void captureVideo(int reqId,int duration,String callback) {
        ReqPacket reqPack=new ReqPacket(reqId, PTYPE.VIDEO,callback);

        this.reqMap.put(reqId,reqPack);

        File videoFile=null;
        try {
            videoFile = createFile("mp4");
            reqPack.data=videoFile.getAbsolutePath();
        } catch (IOException ex) {
        }

        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, duration);
        takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(videoFile));

        if (takeVideoIntent.resolveActivity(this.mContext.getPackageManager()) != null) {
            ((Activity)this.mContext).startActivityForResult(takeVideoIntent, reqId);
        }
    }

    @JavascriptInterface
    public void captureSign(int reqId,String callback) {
        this.reqMap.put(reqId,new ReqPacket(reqId, PTYPE.SIGN,callback));
        Intent intent = new Intent(this.mContext, SingCaptureActivity.class);
        ((Activity)this.mContext).startActivityForResult(intent, reqId);

    }

    @JavascriptInterface
    public void scanFile(int reqId,String callback) {
        this.reqMap.put(reqId,new ReqPacket(reqId, PTYPE.FILE_SCAN,callback));
        Intent intent = new Intent(this.mContext, ScannerHomeActivity.class);
        ((Activity)this.mContext).startActivityForResult(intent, reqId);
    }

    @JavascriptInterface
    public void scanBarcode(int reqId,String callback) {
        this.reqMap.put(reqId,new ReqPacket(reqId, PTYPE.BARCODE,callback));

        Intent intent = new Intent(this.mContext, SimpleScannerActivity.class);
        ((Activity)this.mContext).startActivityForResult(intent, reqId);
    }

    @JavascriptInterface
    public void captureAudio(int reqId,int duration,String callback) {
        this.reqMap.put(reqId,new ReqPacket(reqId, PTYPE.AUDIO,callback));
        Intent intent = new Intent(this.mContext, AudioRecord.class);
        intent.putExtra(AudioRecord.DURATION,duration);
        ((Activity)this.mContext).startActivityForResult(intent, reqId);
    }

    @JavascriptInterface
    public void captureGPS(int reqId,String callback) {
        this.reqMap.put(reqId,new ReqPacket(reqId, PTYPE.GEO,callback));
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            ((Activity) this.mContext).startActivityForResult(builder.build(((Activity) this.mContext)), reqId);
        }catch (Exception ex){}
    }




    @JavascriptInterface
    public void submitData(String data,String formId,int version) {

        NewFormActivity formActivity=((NewFormActivity) this.mContext);

        try {
            JSONObject jData=new JSONObject(data);
            FormData formData=new FormData(jData,version,formId);
            formData.setModel(this.mCurrentForm.getModel());
            formData.setFormName(mCurrentForm.getFormName());



            JSONObject stage_data=new JSONObject();

            stage_data.put("type",this.action.type);
            stage_data.put("uid",this.action.uid);
            stage_data.put("user",AppController.CURRENT_USER.getServerId());
            stage_data.put("date",new Date().getTime());
            if(formActivity.mLastLocationLast!=null){
                stage_data.put("lat",formActivity.mLastLocationLast.getLatitude());
                stage_data.put("lng",formActivity.mLastLocationLast.getLongitude());
            }


            JSONObject oaction=new JSONObject();

            if(this.action!=null){
                oaction.put("name",this.action.action);
                oaction.put("index",this.action.id);
                oaction.put("uid",this.action.uid);
            }
            formData.setFlow(mCurrentForm.getFlow());
            formData.setCurrentAction(oaction);
            formData.setStageHistory(new JSONArray().put(stage_data));





            ArrayList<FormData> dataQueue=this.controller.Filo.readArray(FormData.class);
            dataQueue.add(formData);
            this.controller.Filo.saveArray(dataQueue,FormData.class);
            showToast("Form submitted.");


            if(AppController.isInternetAvailable()) {
                Intent uploadServe = new Intent(this.mContext, SingleUploadService.class);
                uploadServe.putExtra("data", formData);
                formActivity.startService(uploadServe);
            }else{
                showToast("No Network Connection.");
            }

            formActivity.finish();
        }catch (Exception ex){
            showToast("Error on submit");
        }
    }

    @JavascriptInterface
    public void readyDataToSend(boolean isEnable) {
//        ((NewFormActivity)this.mContext).sendEnableState(isEnable);
    }

    @JavascriptInterface
    public void playAudio(String path) {
        File file = new File(path);
        MimeTypeMap map = MimeTypeMap.getSingleton();
        String ext = MimeTypeMap.getFileExtensionFromUrl(file.getName());
        String type = map.getMimeTypeFromExtension(ext);

        if (type == null)
            type = "audio/*";

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri data = Uri.fromFile(file);
        intent.setDataAndType(data, type);
        ((Activity)this.mContext).startActivity(intent);
    }

    @JavascriptInterface
    public void playVideo(String path) {
        File file = new File(path);
        MimeTypeMap map = MimeTypeMap.getSingleton();
        String ext = MimeTypeMap.getFileExtensionFromUrl(file.getName());
        String type = map.getMimeTypeFromExtension(ext);

        if (type == null)
            type = "video/*";

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri data = Uri.parse(path);
        intent.setDataAndType(data, type);
        ((Activity)this.mContext).startActivity(intent);
    }

    @JavascriptInterface
    public void openPdf(String path) {
        File file = new File(path);
        MimeTypeMap map = MimeTypeMap.getSingleton();
        String ext = MimeTypeMap.getFileExtensionFromUrl(file.getName());
        String type = map.getMimeTypeFromExtension(ext);

        if (type == null)
            type = "application/pdf";

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri data = Uri.fromFile(file);
        intent.setDataAndType(data, type);
        ((Activity)this.mContext).startActivity(intent);
    }

    @JavascriptInterface
    public void showImage(String path) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        File file = new File(path);
        intent.setDataAndType(Uri.fromFile(file), "image/*");
        ((Activity)this.mContext).startActivity(intent);
    }


    @JavascriptInterface
    public void openMap(String lat,String lng) {
        String addr=lat+","+lng;

        Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse("http://maps.google.com/maps?q="+ lat  +"," + lng +"(Location)&iwloc=A&hl=es"));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ((Activity)this.mContext).startActivity(intent);
    }


    public void onSuccessResultFromActivity(int reqId, Intent intent){
        ReqPacket req=this.reqMap.remove(reqId);
        Bundle bundle = intent.getExtras();
        switch (req.type){
            case AUDIO:
                triggerCallback(req.callback,req.reqId+"",bundle.getString("data"));
                break;
            case BARCODE:
                triggerCallback(req.callback,req.reqId+"",bundle.getString("text"));
                break;
            case FILE_SCAN:
                triggerCallback(req.callback,req.reqId+"",intent.getStringExtra("data"));
                break;
            case PHOTO:
                scaleImage(1024,820,req.data);
                triggerCallback(req.callback,req.reqId+"",req.data);
                break;
            case VIDEO:
                triggerCallback(req.callback,req.reqId+"",req.data);
                break;
            case GEO:
                Place place = PlacePicker.getPlace(((Activity)mContext),intent);
                triggerCallback(req.callback,req.reqId+"",place.getLatLng().latitude+"",place.getLatLng().latitude+"");
                break;
            case COMPASS:
                triggerCallback(req.callback,req.reqId+"",bundle.getString("data"));
                break;
            case SIGN:
                triggerCallback(req.callback,req.reqId+"",bundle.getString("data"));
                break;
            default:
        }
    }


    private void scaleImage(int targetW,int targetH,String sPath) {

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(sPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(sPath, bmOptions);
        try{
            FileOutputStream out = new FileOutputStream(sPath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        }catch (Exception ex){

        }
    }


    public void collectDataToSend(int actionIndex,String actionString,String mode,String uid) {
        this.action=new Action();
        this.action.id=actionIndex;
        this.action.uid=uid;
        this.action.action=actionString;
        this.action.type=mode;
        this.action.params=new JSONObject();

        triggerCallback("window.Device.gotRequestForDataCollection","");
    }

    private void triggerCallback(String callback,String... params){
        String d="";
        for(int i=0;i<params.length;i++){
            d+="'"+params[i]+"',";
        }
        d+="null";
        this._mCView.loadUrl("javascript:" + callback + "("+d+")");
    }

    private File createFile(String ext) throws IOException {

        File storageDir = Environment.getExternalStorageDirectory();
        storageDir = new File(storageDir.getAbsolutePath(), "c4f_files");
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }

        String imageFileName = "file_" + System.currentTimeMillis() + "_."+ext;
        File image = new File(storageDir, imageFileName);
        return image;
    }


    public static class Action{
        int id;
        String type;
        String uid;
        String action;
        JSONObject params;
    }

    private class ReqPacket{
        int reqId;
        PTYPE type;
        String callback;
        String data;
        ReqPacket(int reqId,PTYPE type,String callback){
            this.reqId=reqId;
            this.type=type;
            this.callback=callback;
        }
    }
}
