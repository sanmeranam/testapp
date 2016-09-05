package com.cloud4form.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class FormDetailsActivity extends AppCompatActivity {
    private  FloatingActionButton fabAddNew;
    private HomeActivity.FormDetails formDetails;
    private Util util;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        formDetails= HomeActivity.FormDetails.parse(getIntent().getStringExtra("data"));
        toolbar.setTitle(formDetails.formName.toUpperCase());
        setSupportActionBar(toolbar);

        util=Util.getInstance(this);
        String token=util.PREFF.getString(getString(R.string.app_token),"");
        String domain=util.PREFF.getString(getString(R.string.app_tenant),"");

        fabAddNew = (FloatingActionButton) findViewById(R.id.fab);
        fabAddNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent formInten=new Intent(FormDetailsActivity.this,NewFormActivity.class);
                formInten.putExtra("data",formDetails.toString());
                startActivity(formInten);
            }
        });

        WebView mWebView = (WebView) findViewById(R.id.formWeb);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
//        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webSettings.setAppCacheEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSettings.setUseWideViewPort(true);
        webSettings.setSavePassword(true);
        webSettings.setSaveFormData(true);
        webSettings.setEnableSmoothTransition(true);

//        this.newFormWebInterface =new NewFormWebInterface(this,mWebView);

//        mWebView.addJavascriptInterface(this.newFormWebInterface, "App");

        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });

        try{
            String sURL=util.AppConfig.getString("app_url")+util.AppConfig.getString("form_hist").replace("{1}",domain);

            sURL+="?_f="+formDetails.formId+"&_t="+token;

            mWebView.loadUrl(sURL);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
