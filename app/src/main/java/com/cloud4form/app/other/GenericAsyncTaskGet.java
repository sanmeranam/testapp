package com.cloud4form.app.other;

import android.os.AsyncTask;

import org.json.JSONObject;

/**
 * Created by I326482 on 9/7/2016.
 */
public class GenericAsyncTaskGet extends AsyncTask<Void,Void,JSONObject> {

    private  IAsyncCallback iAsyncCallback;
    private String sURL;
    public GenericAsyncTaskGet(String sURL, IAsyncCallback iAsyncCallback){
        this.iAsyncCallback=iAsyncCallback;
        this.sURL=sURL;
    }
    @Override
    protected JSONObject doInBackground(Void... params) {
        JSONSync jsync = new JSONSync(null);
        return jsync.getJsonGet(this.sURL,"");
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
         try {
             this.iAsyncCallback.onResult(jsonObject);
         }catch (Exception ex){
             ex.printStackTrace();
         }
    }

    public static interface IAsyncCallback{
        public void onResult(JSONObject result) throws Exception;
    }
}
