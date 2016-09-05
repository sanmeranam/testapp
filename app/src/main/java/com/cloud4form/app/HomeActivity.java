package com.cloud4form.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.cloud4form.app.barcode.JSONSync;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private ArrayList<FormDetails> formList=new ArrayList();
    private Util util;
    private  ImageAdapter imageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        util=Util.getInstance(this);

        String sForms=util.PREFF.getString(getString(R.string.app_forms),"[]");
        try {
            JSONArray arr=new JSONArray(sForms);
            for(int i=0;i<arr.length();i++){
                formList.add(new FormDetails(arr.getJSONObject(i)));
            }
        }catch (Exception ex){ex.printStackTrace();}


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        GridView gridview = (GridView) findViewById(R.id.gridview);
        imageAdapter=new ImageAdapter(this);
        gridview.setAdapter(imageAdapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,int position, long id) {
                FormDetails fd=formList.get(position);
                Intent formInten=new Intent(HomeActivity.this,FormDetailsActivity.class);
                formInten.putExtra("data",fd.toString());
                startActivity(formInten);
            }
        });
    }

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return formList.size();
        }

        public FormDetails getItem(int position) {
            return formList.get(position);
        }

        public long getItemId(int position) {
            return formList.get(position)._id;
        }

        public View getView(int position, View rootView, ViewGroup parent) {
            if(rootView==null){
                LayoutInflater factory = LayoutInflater.from(HomeActivity.this);
                rootView = factory.inflate(R.layout.ome_item, null);
                FormDetails fd=formList.get(position);
                rootView.setTag(fd);
                ((TextView)rootView.findViewById(R.id.textViewItem)).setText(fd.formName.toUpperCase());
            }
            return rootView;
        }

    }

    @Override
    public void onBackPressed() {
        finish();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public static class FormDetails{
        long _id;
        String formId;
        String formName;
        int version;
        FormDetails(){}

        FormDetails(JSONObject obj){
            try{
                _id=Math.round(Math.random()*199999);
                this.formId=obj.getString("_id");
                this.formName=obj.getString("form_name");
                this.version=obj.getInt("version");
            }catch (Exception ex){
                ex.printStackTrace();
            }

        }

        @Override
        public String toString() {
            return _id+"#"+formId+"#"+formName+"#"+version;
        }
        public static FormDetails parse(String dd){
            String[] data=dd.split("#");
            FormDetails fc=new FormDetails();
            fc._id=Long.parseLong(data[0]);
            fc.formId=data[1];
            fc.formName=data[2];
            fc.version=Integer.parseInt(data[3]);
            return fc;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            String url = util.AppConfig.getString("api_url");
            String path = util.AppConfig.getString("from_path");
            String token=util.PREFF.getString(getString(R.string.app_token),"");
            String domain=util.PREFF.getString(getString(R.string.app_tenant),"");

            path=path.replace("{1}",domain);

            JSONObject dataToSend = new JSONObject();
            dataToSend.put("ENTRY_ID", token);


            new BackGroundTask(url + path).execute(dataToSend);
        } catch (Exception ex) {
            Log.e("RESULT_ERROR", ex.getMessage());
        }
    }

    private void onResultRemote(Object result){
        if(result instanceof JSONArray){
            JSONArray res=(JSONArray)result;
            util.PREFF.edit().putString(getString(R.string.app_forms),res.toString()).commit();
            try{
                formList.clear();
                for(int i=0;i<res.length();i++){
                    formList.add(new FormDetails(res.getJSONObject(i)));
                }
                imageAdapter.notifyDataSetChanged();
            }catch (Exception ex){}
        }

    }

    private class BackGroundTask extends AsyncTask<JSONObject, String, Object> {
        private String url;

        BackGroundTask(String url) {
            this.url = url;
        }

        @Override
        protected Object doInBackground(JSONObject... params) {
            JSONSync jsync = new JSONSync(HomeActivity.this, null);
            return jsync.getJsonPost(this.url, params[0]);
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Object result) {
            onResultRemote(result);
        }
    }

}
