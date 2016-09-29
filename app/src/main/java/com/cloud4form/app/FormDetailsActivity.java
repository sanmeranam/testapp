package com.cloud4form.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.cloud4form.app.db.FormMeta;
import com.cloud4form.app.db.IEntity;

import java.io.File;

public class FormDetailsActivity extends AppCompatActivity {
    private FloatingActionButton fabAddNew;
    private FormMeta formMeta;
    private AppController appController;
    private WebView mWebView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_details);

        formMeta = (FormMeta) getIntent().getSerializableExtra(IEntity.ARG_DATA);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(formMeta.getName().toUpperCase());
        setSupportActionBar(toolbar);

        appController = AppController.getInstance(this);

        this.initControls();
        this.initWebView();
    }

    private void initControls(){
        fabAddNew = (FloatingActionButton) findViewById(R.id.fab);
        fabAddNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent formInten=new Intent(FormDetailsActivity.this,NewFormActivity.class);
                formInten.putExtra(IEntity.ARG_DATA,formMeta);
                formInten.putExtra(IEntity.ARG_MODE,IEntity.ARG_MODE_NEW);
                startActivity(formInten);
            }
        });
    }

    private void initWebView(){
        String token= appController.getPref(AppController.PREE_SYNC_TOKEN,"");
        String domain= appController.getPref(AppController.PREE_APP_TENANT);

        mWebView = (WebView) findViewById(R.id.formWeb);
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

        JSInterface newFormWebInterface =new JSInterface(this,mWebView);

        mWebView.addJavascriptInterface(newFormWebInterface, "FD");

        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });

        try{
            String sURL= appController.AppConfig.getString("app_url")+ appController.AppConfig.getString("form_hist").replace("{domain}",domain);
            sURL+="?_f="+ formMeta.getServerId()+"&_t="+token;
            mWebView.loadUrl(sURL);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public class JSInterface{
        private Context mContext;
        private WebView _mCView;
        JSInterface(Context c, WebView _mCView) {
            mContext = c;
            this._mCView=_mCView;
        }

        @JavascriptInterface
        public void openViewForm(String formFieldId){
            Intent formInten=new Intent(FormDetailsActivity.this,NewFormActivity.class);
            formInten.putExtra(IEntity.ARG_DATA, (Parcelable) formMeta);
            formInten.putExtra(IEntity.ARG_MODE,IEntity.ARG_MODE_VIEW);
            startActivity(formInten);
        }

        @JavascriptInterface
        public void openEditForm(String formFieldId){
            Intent formInten=new Intent(FormDetailsActivity.this,NewFormActivity.class);
            formInten.putExtra(IEntity.ARG_DATA, (Parcelable) formMeta);
            formInten.putExtra(IEntity.ARG_MODE,IEntity.ARG_MODE_EDIT);
            startActivity(formInten);
        }

        @JavascriptInterface
        public void playAudio(String path,String callback) {
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            File file = new File(path);
            intent.setDataAndType(Uri.fromFile(file), "audio/*");
            FormDetailsActivity.this.startActivity(intent);
        }

        @JavascriptInterface
        public void playVideo(String path) {
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            File file = new File(path);
            intent.setDataAndType(Uri.fromFile(file), "video/*");
            FormDetailsActivity.this.startActivity(intent);
        }

        @JavascriptInterface
        public void showImage(String path) {
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            File file = new File(path);
            intent.setDataAndType(Uri.fromFile(file), "image/*");
            FormDetailsActivity.this.startActivity(intent);
        }

        @JavascriptInterface
        public void openMap(String lat,String lng) {
            String addr=lat+","+lng;

            Intent intent = new Intent(android.content.Intent.ACTION_VIEW,Uri.parse("http://maps.google.com/maps?saddr="+addr+"&daddr="+addr));
            FormDetailsActivity.this.startActivity(intent);
        }
    }
}
