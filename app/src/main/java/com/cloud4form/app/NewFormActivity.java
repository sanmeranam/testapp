package com.cloud4form.app;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.cloud4form.app.barcode.MyWebViewClient;
import com.cloud4form.app.other.FormMetaEntity;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

public class NewFormActivity extends AppCompatActivity {

    private  WebView mWebView;
    private NewFormWebInterface newFormWebInterface;
    private FormMetaEntity formDetails;
    private Util util;
    private FloatingActionButton fabSend;
    private String FORM_MODE=FormMetaEntity.ARG_MODE_NEW;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        formDetails= FormMetaEntity.parse(getIntent().getStringExtra(FormMetaEntity.ARG_DATA));

        toolbar.setTitle(formDetails.formName.toUpperCase());
        setSupportActionBar(toolbar);

        FORM_MODE=getIntent().getStringExtra(FormMetaEntity.ARG_MODE);


        util=Util.getInstance(this);
        String token=util.getPref(Util.PREE_SYNC_TOKEN);
        String domain=util.getPref(Util.PREE_APP_TENANT);



        fabSend = (FloatingActionButton) findViewById(R.id.fab123);
        fabSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        mWebView = (WebView) findViewById(R.id.webView);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSettings.setAppCacheEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSettings.setUseWideViewPort(true);
        webSettings.setSavePassword(true);
        webSettings.setSaveFormData(true);
        webSettings.setEnableSmoothTransition(true);

        this.newFormWebInterface =new NewFormWebInterface(this,mWebView);

        mWebView.addJavascriptInterface(this.newFormWebInterface, "FC");

        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });

        mWebView.setWebViewClient(new MyWebViewClient(this));

        try{
            String sURL=util.AppConfig.getString("app_url")+util.AppConfig.getString("form_new").replace("{1}",domain);

            sURL+="?_f="+formDetails.formId+"&_t="+token+"&_m="+FORM_MODE;

            mWebView.loadUrl(sURL);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void sendEnableState(boolean bState){
        this.fabSend.setEnabled(bState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==RESULT_OK) {
            this.newFormWebInterface.onSuccessResultFromActivity(requestCode,data);
        }
    }
}
