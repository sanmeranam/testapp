package com.cloud4form.app;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cloud4form.app.other.GenericAsyncTask;
import com.cloud4form.app.barcode.SimpleScannerActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    private AppController appController;
    private ProgressBar _mProg;
    private Button _mScanButton;
    private TextView _mTextView;

    private GoogleApiClient client;
    Location mLastLocationLast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appController = AppController.getInstance(this);

        _mProg = (ProgressBar) findViewById(R.id.progress);
        _mScanButton = (Button) findViewById(R.id.button_scan);
        _mTextView = (TextView) findViewById(R.id.text_descprit);

        _mScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SimpleScannerActivity.class);
                startActivityForResult(intent, 111);
            }
        });
        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    @Override
    protected void onStart() {

        String appConfig = appController.getPref(AppController.PREE_APP_CONFIG);
        String appToken = appController.getPref(AppController.PREE_USER_PROFILE);

        if (appConfig == null) {
            _mTextView.setVisibility(View.VISIBLE);
            _mScanButton.setVisibility(View.VISIBLE);
            _mProg.setVisibility(View.INVISIBLE);
        } else if (appToken == null || appToken.trim().length()==0) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, HomeActivitySlider.class);
            startActivity(intent);
        }

        super.onStart();

        client.connect();

    }

    private void onResultRemote(JSONObject response){
        String sErrorMsg="ERROR:Something went wrong. Try later.";
        try{
            if(response.has("success") && response.getInt("success")==1){
                JSONObject oData=response.getJSONObject("data");
                appController.saveJSONData(oData, AppController.PREE_APP_CONFIG);
                appController.setPref(AppController.PREE_APP_TENANT,oData.getString("domain"));
                appController.setPref(AppController.PREE_APP_WORK_MODE, AppController.PREE_APP_WORK_MODE_ONLINE);

                Intent loginPage=new Intent(MainActivity.this,LoginActivity.class);
                startActivity(loginPage);

            }else{
                this._mTextView.setText(sErrorMsg);
            }
        }catch (Exception ex){
            this._mTextView.setText(sErrorMsg);
        }
    }

    private void getAppConfig(String scanId) {
        this._mProg.setVisibility(View.VISIBLE);
        this._mScanButton.setVisibility(View.GONE);
        this._mTextView.setText("Sync in progress. Please wait...");


        try {

            JSONObject dataToSend = new JSONObject();
            dataToSend.put("DEVICE_ID", this.appController.getDeviceId());
            dataToSend.put("SCAN_ID", scanId);
            dataToSend.put("DEVICE_MODEL", this.appController.getDeviceModel());
            dataToSend.put("DEVICE_SCREEN", this.appController.getDeviceScreen());

            if(mLastLocationLast!=null){
                JSONObject loca=new JSONObject();
                loca.put("LAT",mLastLocationLast.getLatitude());
                loca.put("LNG",mLastLocationLast.getLongitude());
                dataToSend.put("LOCATION",loca);
            }

            new GenericAsyncTask(appController.generateURL("api_url","sync_path"),new GenericAsyncTask.IAsyncCallback() {
                @Override
                public void onResult(JSONObject result) {
                    onResultRemote(result);
                }
            }).execute(dataToSend);
        } catch (Exception ex) {
            Log.e("RESULT_ERROR", ex.getMessage());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        client.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try{
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(client);
            if (mLastLocation != null) {
                mLastLocationLast=mLastLocation;
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 111 && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            String _mResultText = bundle.getString("text");
            String _mResultType = bundle.getString("format");
            getAppConfig(_mResultText);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
