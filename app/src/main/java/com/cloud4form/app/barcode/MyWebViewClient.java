package com.cloud4form.app.barcode;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.cloud4form.app.db.FormMeta;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;

/**
 * Created by I326482 on 9/2/2016.
 */
public class MyWebViewClient extends WebViewClient {

    private Context context;
    private FormMeta formMeta;
    public MyWebViewClient(Context context, FormMeta formMeta){
        this.context=context;
        this.formMeta=formMeta;
    }

//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    @Override
//    public WebResourceResponse shouldInterceptRequest(final WebView view, WebResourceRequest request) {
//
//
//        if (request.getUrl().getPath().contains("_form_data.js")) {
//            return getWebResourceResponseFromString();
//        } else {
//            return super.shouldInterceptRequest(view, request);
//        }
//    }
//
//    @Override
//    public WebResourceResponse shouldInterceptRequest(final WebView view, String url) {
//        if (url.contains("_form_data.js")) {
//            return getWebResourceResponseFromString();
//        } else {
//            return super.shouldInterceptRequest(view, url);
//        }
//    }


    private WebResourceResponse getWebResourceResponseFromString() {
        String data="window.FromMeta=";

        try{
            JSONObject obj=new JSONObject();
            obj.put("id",this.formMeta.getServerId());
            obj.put("name",this.formMeta.getName());
            obj.put("version",this.formMeta.getVersion());
            obj.put("model_view",this.formMeta.getModel());
            data+=obj.toString();

        }catch (Exception ex){
        }

        try{
            return new WebResourceResponse("text/javascript", "UTF-8",new ByteArrayInputStream(data.getBytes("UTF-8")));
        }catch (Exception ex){

        }

        return new WebResourceResponse("text/javascript", "UTF-8",new ByteArrayInputStream(data.getBytes()));
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return false;
    }
    //ProgressDialogue
    ProgressDialog pd = null;

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        pd=new ProgressDialog(this.context);
        pd.setTitle("Please Wait..");
        pd.setMessage("Starting...");
        pd.show();
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        pd.dismiss();
        super.onPageFinished(view, url);
    }
}