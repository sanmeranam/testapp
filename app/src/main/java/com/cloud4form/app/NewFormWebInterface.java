package com.cloud4form.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import com.cloud4form.app.audio.AudioRecord;
import com.cloud4form.app.barcode.SimpleScannerActivity;
import com.cloud4form.app.filescan.ScannerHomeActivity;
import com.cloud4form.app.sign.SingCaptureActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by I326482 on 8/14/2016.
 */
public class NewFormWebInterface {
    private Context mContext;
    private WebView _mCView;
    private int baseReqId=1000;
    private enum PTYPE{PHOTO,FILE_SCAN,VIDEO,AUDIO,BARCODE,GEO,COMPASS,SIGN};
    private HashMap<Integer,ReqPacket> reqMap=new HashMap<>();

    /** Instantiate the interface and set the context */
    NewFormWebInterface(Context c, WebView _mCView) {
        mContext = c;
        this._mCView=_mCView;
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
                photoFile = createImageFile();
                reqPack.data=photoFile.getAbsolutePath();
            } catch (IOException ex) {
            }

            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoFile.toURI());
                ((Activity)this.mContext).startActivityForResult(takePictureIntent, reqId);
            }
        }
    }

    @JavascriptInterface
    public void captureVideo(int reqId,int duration,String callback) {
        this.reqMap.put(reqId,new ReqPacket(reqId,PTYPE.VIDEO,callback));
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(this.mContext.getPackageManager()) != null) {
            ((Activity)this.mContext).startActivityForResult(takeVideoIntent, reqId);
        }
    }

    @JavascriptInterface
    public void captureSign(int reqId,String callback) {
        this.reqMap.put(reqId,new ReqPacket(reqId,PTYPE.VIDEO,callback));
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
        intent.putExtra(AudioRecord.DURATION,70);
        ((Activity)this.mContext).startActivityForResult(intent, reqId);
    }


    @JavascriptInterface
    public void submitData(String data,String callback) {

    }

    @JavascriptInterface
    public void readyDataToSend(boolean isEnable) {
        ((NewFormActivity)this.mContext).sendEnableState(isEnable);
    }

    @JavascriptInterface
    public void playAudio(String path,String callback) {
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        File file = new File(path);
        intent.setDataAndType(Uri.fromFile(file), "audio/*");
        ((Activity)this.mContext).startActivity(intent);
    }

    @JavascriptInterface
    public void playVideo(String path) {
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        File file = new File(path);
        intent.setDataAndType(Uri.fromFile(file), "video/*");
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

        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,Uri.parse("http://maps.google.com/maps?saddr="+addr+"&daddr="+addr));
        ((Activity)this.mContext).startActivity(intent);
    }


    public void onSuccessResultFromActivity(int reqId, Intent intent){
        ReqPacket req=this.reqMap.remove(reqId);
        Bundle bundle = intent.getExtras();
        switch (req.type){
            case AUDIO:
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
                triggerCallback(req.callback,req.reqId+"",intent.getData().toString());
                break;
            case GEO:
                break;
            case COMPASS:
                break;
            case SIGN:
                break;
            default:
        }
    }

    private void triggerCallback(String callback,String... params){
        String d="";
        for(int i=0;i<params.length;i++){
            d+="'"+params[i]+"',null";
        }
        this._mCView.loadUrl("javascript:" + callback + "("+d+")");
    }

    private File createImageFile() throws IOException {

        File storageDir = Environment.getExternalStorageDirectory();
        storageDir = new File(storageDir.getAbsolutePath(), "c4f_files");
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }

        String imageFileName = "photo_" + System.currentTimeMillis() + "_";
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
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
