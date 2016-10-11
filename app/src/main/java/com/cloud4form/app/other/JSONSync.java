package com.cloud4form.app.other;

import com.cloud4form.app.AppController;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by I326482 on 8/13/2016.
 */
public class JSONSync {

    private String token;
    public JSONSync(String token){
            this.token=token;
    }

    public JSONObject getJsonGet(String surl,String token){

        if(!AppController.isInternetAvailable()){
            return null;
        }
        try{
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(surl)
                    .build();

            Response response = client.newCall(request).execute();
            if(response.isSuccessful()){
                return new JSONObject(response.body().string());
            }


        }catch (Exception ex){

        }
        return  null;


//        try{
//            URL url=new URL(surl);
//            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
//            if(this.token!=null)
//                urlConnection.setRequestProperty("auth_token",token);
//
//            urlConnection.setDoOutput(false);
//            urlConnection.setRequestMethod("GET");
//
//            Scanner reader=new Scanner(new BufferedInputStream(urlConnection.getInputStream()));
//
//            if(urlConnection.getResponseCode()==200 || urlConnection.getResponseCode()==302){
//                StringBuilder stringBuilder=new StringBuilder();
//                while (reader.hasNext()){
//                    stringBuilder.append(reader.next());
//                }
//
//                urlConnection.disconnect();
//                JSONObject obj = new JSONObject(stringBuilder.toString());
//                return obj;
//            }else{
//                return null;
//            }
//
//        }catch (Exception ex){
//            return null;
//        }
    }

    public JSONObject getJsonPost(String surl,JSONObject data){
        if(!AppController.isInternetAvailable()){
            return null;
        }

        final MediaType JSON= MediaType.parse("application/json; charset=utf-8");
        try{
            RequestBody body = RequestBody.create(JSON, data.toString());

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(surl)
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();
            if(response.isSuccessful()){
                return new JSONObject(response.body().string());
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;





//        try{
//            URL url=new URL(surl);
//            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
//            if(this.token!=null)
//                urlConnection.setRequestProperty("auth_token",token);
//            urlConnection.setRequestProperty("Content-Type","application/json");
//            urlConnection.setDoOutput(true);
//            urlConnection.setRequestMethod("POST");
//            urlConnection.setReadTimeout(10000);
//            urlConnection.setConnectTimeout(15000);
//            urlConnection.setChunkedStreamingMode(0);
//            OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
//            out.write(data.toString().getBytes());
//            out.flush();
//
//            Scanner reader=new Scanner(new BufferedInputStream(urlConnection.getInputStream()));
//
//            if(urlConnection.getResponseCode()==200 || urlConnection.getResponseCode()==302){
//                StringBuilder stringBuilder=new StringBuilder();
//                while (reader.hasNext()){
//                    stringBuilder.append(reader.next());
//                }
//
//                urlConnection.disconnect();
//                return new JSONObject(stringBuilder.toString());
//            }else{
//                return null;
//            }
//
//        }catch (Exception ex){
//            ex.printStackTrace();
//        }
//        return null;
    }

    public JSONObject getJsonPut(String surl,JSONObject data){
        if(!AppController.isInternetAvailable()){
            return null;
        }
        try{
            URL url=new URL(surl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            if(this.token!=null)
                urlConnection.setRequestProperty("auth_token",token);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("PUT");
            urlConnection.setChunkedStreamingMode(0);

            OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
            out.write(data.toString().getBytes());
            out.flush();

            Scanner reader=new Scanner(new BufferedInputStream(urlConnection.getInputStream()));

            if(urlConnection.getResponseCode()==200 || urlConnection.getResponseCode()==302){
                StringBuilder stringBuilder=new StringBuilder();
                while (reader.hasNext()){
                    stringBuilder.append(reader.next());
                }

                urlConnection.disconnect();
                JSONObject obj = new JSONObject(stringBuilder.toString());
                return obj;
            }else{
                return null;
            }

        }catch (Exception ex){
            return null;
        }
    }

    public JSONObject getJsonDelete(String surl){
        if(!AppController.isInternetAvailable()){
            return null;
        }
        try{
            URL url=new URL(surl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            if(this.token!=null)
                urlConnection.setRequestProperty("auth_token",token);

            urlConnection.setDoOutput(false);
            urlConnection.setRequestMethod("DELETE");

            Scanner reader=new Scanner(new BufferedInputStream(urlConnection.getInputStream()));

            if(urlConnection.getResponseCode()==200 || urlConnection.getResponseCode()==302){
                StringBuilder stringBuilder=new StringBuilder();
                while (reader.hasNext()){
                    stringBuilder.append(reader.next());
                }

                urlConnection.disconnect();
                JSONObject obj = new JSONObject(stringBuilder.toString());
                return obj;
            }else{
                return null;
            }

        }catch (Exception ex){
            return null;
        }
    }
}
