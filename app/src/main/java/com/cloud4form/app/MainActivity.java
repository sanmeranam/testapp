package com.cloud4form.app;

import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cloud4form.app.barcode.JSONSync;
import com.cloud4form.app.barcode.SimpleScannerActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    private Util util;
    private ProgressBar _mProg;
    private Button _mScanButton;
    private TextView _mTextView;
//    private LocationManager locationManager;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    Location mLastLocationLast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        util = Util.getInstance(this);

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

        String appConfig = util.PREFF.getString(getString(R.string.app_config), null);
        String appToken = util.PREFF.getString(getString(R.string.app_token), null);

        if (appConfig == null) {
            _mTextView.setVisibility(View.VISIBLE);
            _mScanButton.setVisibility(View.VISIBLE);
            _mProg.setVisibility(View.INVISIBLE);
        } else if (appToken == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, HomeActivitySlider.class);
            startActivity(intent);
        }

        super.onStart();

        client.connect();

    }

    private void onResultRemote(JSONObject data){
        try{
            Log.v("RESULT", data.toString());
            this._mProg.setVisibility(View.INVISIBLE);
            if(data.has("error")){
                this._mTextView.setText("ERROR:Something went wrong. Try later.");
            }else{
                util.PREFF.edit().putString(getString(R.string.app_config),data.toString()).commit();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
            }
        }catch (Exception ex){
            this._mTextView.setText("ERROR:Something went wrong. Try later.");
        }
    }

    private void getAppConfig(String scanId) {
        this._mProg.setVisibility(View.VISIBLE);
        this._mTextView.setText("Sync in progress. Please wait...");
        this._mScanButton.setVisibility(View.INVISIBLE);

        try {
            String url = util.AppConfig.getString("api_url");
            String path = util.AppConfig.getString("sync_path");

            JSONObject dataToSend = new JSONObject();
            dataToSend.put("DEVICE_ID", this.util.getDeviceId());
            dataToSend.put("SCAN_ID", scanId);
            dataToSend.put("DEVICE_MODEL", this.util.getDeviceModel());
            dataToSend.put("DEVICE_SCREEN", this.util.getDeviceScreen());

            if(mLastLocationLast!=null){
                JSONObject loca=new JSONObject();
                loca.put("LAT",mLastLocationLast.getLatitude());
                loca.put("LNG",mLastLocationLast.getLongitude());
                dataToSend.put("LOCATION",loca);
            }


            new BackGroundTask(url + path).execute(dataToSend);
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


    private class BackGroundTask extends AsyncTask<JSONObject, String, JSONObject> {
        private String url;

        BackGroundTask(String url) {
            this.url = url;
        }

        @Override
        protected JSONObject doInBackground(JSONObject... params) {
            JSONSync jsync = new JSONSync(MainActivity.this, null);
            return (JSONObject) jsync.getJsonPost(this.url, params[0]);
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(JSONObject result) {
            onResultRemote(result);
        }
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
