package com.cloud4form.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
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

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by I326482 on 8/14/2016.
 */
public class NewFormWebInterface {
    private Context mContext;
    private WebView _mCView;
    private int baseReqId=1000;
    private AppController controller;


    private enum PTYPE{PHOTO,FILE_SCAN,VIDEO,AUDIO,BARCODE,GEO,COMPASS,SIGN};
    private HashMap<Integer,ReqPacket> reqMap=new HashMap<>();
    private FormMeta mCurrentForm;

    /** Instantiate the interface and set the context */
    NewFormWebInterface(Context c, WebView _mCView,FormMeta mCurrentForm,AppController controller) {
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
            obj.put("name",this.mCurrentForm.getName());
            obj.put("version",this.mCurrentForm.getVersion());
            obj.put("model_view",this.mCurrentForm.getModel());
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
        this.reqMap.put(reqId,new ReqPacket(reqId,PTYPE.GEO,callback));
    }

    @JavascriptInterface
    public void getCompass(int reqId,String callback) {
        this.reqMap.put(reqId,new ReqPacket(reqId,PTYPE.COMPASS,callback));
    }


    @JavascriptInterface
    public void capturePhoto(int reqId,String callback) {
        ReqPacket reqPack=new ReqPacket(reqId,PTYPE.PHOTO,callback);

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
        ReqPacket reqPack=new ReqPacket(reqId,PTYPE.VIDEO,callback);

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
        this.reqMap.put(reqId,new ReqPacket(reqId,PTYPE.SIGN,callback));
        Intent intent = new Intent(this.mContext, SingCaptureActivity.class);
        ((Activity)this.mContext).startActivityForResult(intent, reqId);

    }

    @JavascriptInterface
    public void scanFile(int reqId,String callback) {
        this.reqMap.put(reqId,new ReqPacket(reqId,PTYPE.FILE_SCAN,callback));
        Intent intent = new Intent(this.mContext, ScannerHomeActivity.class);
        ((Activity)this.mContext).startActivityForResult(intent, reqId);
    }

    @JavascriptInterface
    public void scanBarcode(int reqId,String callback) {
        this.reqMap.put(reqId,new ReqPacket(reqId,PTYPE.BARCODE,callback));

        Intent intent = new Intent(this.mContext, SimpleScannerActivity.class);
        ((Activity)this.mContext).startActivityForResult(intent, reqId);
    }

    @JavascriptInterface
    public void captureAudio(int reqId,int duration,String callback) {
        this.reqMap.put(reqId,new ReqPacket(reqId,PTYPE.AUDIO,callback));
        Intent intent = new Intent(this.mContext, AudioRecord.class);
        intent.putExtra(AudioRecord.DURATION,duration);
        ((Activity)this.mContext).startActivityForResult(intent, reqId);
    }

    @JavascriptInterface
    public void captureGPS(int reqId,String callback) {
        this.reqMap.put(reqId,new ReqPacket(reqId,PTYPE.GEO,callback));
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            ((Activity) this.mContext).startActivityForResult(builder.build(((Activity) this.mContext)), reqId);
        }catch (Exception ex){}
    }




    @JavascriptInterface
    public void submitData(String data,String formId,int version) {
        try {
            JSONObject jData=new JSONObject(data);
            FormData formData=new FormData(jData,version,formId);

            ArrayList<FormData> dataQueue=this.controller.Filo.readArray(FormData.class);
            dataQueue.add(formData);
            this.controller.Filo.saveArray(dataQueue,FormData.class);
            showToast("Form submitted.");


            if(AppController.isInternetAvailable()) {
                Intent uploadServe = new Intent(this.mContext, SingleUploadService.class);
                uploadServe.putExtra("data", formData);
                ((Activity) this.mContext).startService(uploadServe);
            }else{
                showToast("No Network Connection.");
            }

            ((Activity) this.mContext).finish();
        }catch (Exception ex){
            showToast("Error on submit");
        }
    }

    @JavascriptInterface
    public void readyDataToSend(boolean isEnable) {
        ((NewFormActivity)this.mContext).sendEnableState(isEnable);
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
//        Uri data =Uri.fromFile(file);
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
        intent.setAction(android.content.Intent.ACTION_VIEW);
        File file = new File(path);
        intent.setDataAndType(Uri.fromFile(file), "image/*");
        ((Activity)this.mContext).startActivity(intent);
    }


    @JavascriptInterface
    public void openMap(String lat,String lng) {
        String addr=lat+","+lng;

        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,Uri.parse("http://maps.google.com/maps?q="+ lat  +"," + lng +"(Location)&iwloc=A&hl=es"));
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

    public void collectDataToSend() {
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
