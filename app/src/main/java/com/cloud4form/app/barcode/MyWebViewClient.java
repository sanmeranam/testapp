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
    public MyWebViewClient(Context context){
        this.context=context;
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