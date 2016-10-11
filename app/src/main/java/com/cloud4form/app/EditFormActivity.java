package com.cloud4form.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.cloud4form.app.barcode.MyWebViewClient;
import com.cloud4form.app.db.FormData;
import com.cloud4form.app.db.IEntity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONObject;

public class EditFormActivity  extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private WebView mWebView;
    private EditFormWebInterface editFormWebInterface;

    private FormData formData;
    private AppController appController;
    private String FORM_MODE= IEntity.ARG_MODE_NEW;

    private GoogleApiClient client;
    public Location mLastLocationLast;
    private String[] aPageActions=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        formData=(FormData) getIntent().getSerializableExtra(IEntity.ARG_DATA);

        toolbar.setTitle(formData.getFormName().toUpperCase());
        setSupportActionBar(toolbar);

        FORM_MODE=getIntent().getStringExtra(IEntity.ARG_MODE);


        appController = AppController.getInstance(this);
        String token= appController.getPref(AppController.PREE_SYNC_TOKEN);
        String domain= appController.getPref(AppController.PREE_APP_TENANT);


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

        this.editFormWebInterface =new EditFormWebInterface(this,mWebView,formData,appController);

        mWebView.addJavascriptInterface(this.editFormWebInterface, "FC");

        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });

        mWebView.setWebViewClient(new MyWebViewClient(this));

        try{
            String sURL="file:///android_asset/foreapp/index2.html";
            mWebView.loadUrl(sURL);
        }catch (Exception e){
            e.printStackTrace();
        }


        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==RESULT_OK) {
            data=data==null?new Intent():data;
            this.editFormWebInterface.onSuccessResultFromActivity(requestCode,data);
        }
    }

    private String[] getCreateActions(){
        String[] as=new String[]{"Submit"};
        try{
            JSONObject cStageAction=this.formData.getNextStage();
            if(cStageAction!=null){
                JSONArray act=cStageAction.getJSONArray("_a");
                as=new String[act.length()];
                for(int i=0;i<as.length;i++){
                    as[i]=act.getJSONObject(i).getString("n");
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return as;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.aPageActions=getCreateActions();
        for(int i=0;i<this.aPageActions.length;i++){
            menu.add(Menu.NONE, i, Menu.NONE, this.aPageActions[i]).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        requestToCollectData(item.getItemId(),this.aPageActions[item.getItemId()]);

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        client.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        client.disconnect();
    }

    private void requestToCollectData(int id,String action){
        this.editFormWebInterface.collectDataToSend(id,action,"CREATE","111");
    }

    @Override
    public void onBackPressed() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        finish();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Closing this page without submitting may cause data loss." +
                "Are sure want to close?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try{
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(client);
            if (mLastLocation != null) {
                this.mLastLocationLast=mLastLocation;
            }
        }catch (SecurityException ex){

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
