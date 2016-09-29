package com.cloud4form.app;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.cloud4form.app.barcode.MyWebViewClient;
import com.cloud4form.app.db.FormMeta;
import com.cloud4form.app.db.IEntity;

public class NewFormActivity extends AppCompatActivity{

    private WebView mWebView;
    private NewFormWebInterface newFormWebInterface;
    private FormMeta formDetails;
    private AppController appController;
    private FloatingActionButton fabSend;
    private String FORM_MODE= IEntity.ARG_MODE_NEW;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        formDetails=(FormMeta) getIntent().getSerializableExtra(IEntity.ARG_DATA);//FormMetaEntity.parse(getIntent().getStringExtra(FormMetaEntity.ARG_DATA));

        toolbar.setTitle(formDetails.getName().toUpperCase());
        setSupportActionBar(toolbar);

        FORM_MODE=getIntent().getStringExtra(IEntity.ARG_MODE);


        appController = AppController.getInstance(this);
        String token= appController.getPref(AppController.PREE_SYNC_TOKEN);
        String domain= appController.getPref(AppController.PREE_APP_TENANT);



        fabSend = (FloatingActionButton) findViewById(R.id.fab1233);
        fabSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestToCollectData();
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

        mWebView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        mWebView.setLongClickable(false);
        mWebView.setHapticFeedbackEnabled(false);

        this.newFormWebInterface =new NewFormWebInterface(this,mWebView,formDetails,appController);

        mWebView.addJavascriptInterface(this.newFormWebInterface, "FC");

        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });

        mWebView.setWebViewClient(new MyWebViewClient(this,formDetails));

        try{
            String sURL= appController.AppConfig.getString("app_url")+ appController.AppConfig.getString("form_new").replace("{domain}",domain);

            sURL+="?_f="+formDetails.getServerId()+"&_t="+token+"&_m="+FORM_MODE;

            sURL="file:///android_asset/foreapp/index.html";

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
        if(resultCode==RESULT_OK) {
            data=data==null?new Intent():data;
            this.newFormWebInterface.onSuccessResultFromActivity(requestCode,data);
        }
    }


    private void requestToCollectData(){
        this.newFormWebInterface.collectDataToSend();
    }

}
