package com.cloud4form.app.other;

import android.os.AsyncTask;

import org.json.JSONObject;

/**
 * Created by I326482 on 9/7/2016.
 */
public class GenericAsyncTask extends AsyncTask<JSONObject,Void,JSONObject> {

    private  IAsyncCallback iAsyncCallback;
    private String sURL;
    public GenericAsyncTask(String sURL,IAsyncCallback iAsyncCallback){
        this.iAsyncCallback=iAsyncCallback;
        this.sURL=sURL;
    }
    @Override
    protected JSONObject doInBackground(JSONObject... params) {
        JSONSync jsync = new JSONSync(null);
        return jsync.getJsonPost(this.sURL, params[0]);
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
         try {
             this.iAsyncCallback.onResult(jsonObject);
         }catch (Exception ex){
         }
    }

    public static interface IAsyncCallback{
        public void onResult(JSONObject result) throws Exception;
    }
}
