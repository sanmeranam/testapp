package com.cloud4form.app.barcode;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by I326482 on 8/13/2016.
 */
public class JSONSync {
    private Context context;
    private String token;
    public JSONSync(Context context,String token){
        this.context=context;
        this.token=token;
    }

    public JSONObject getJsonGet(String surl,String token){

        try{
            URL url=new URL(surl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            if(this.token!=null)
                urlConnection.setRequestProperty("auth_token",token);

            urlConnection.setDoOutput(false);
            urlConnection.setRequestMethod("GET");

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

    public Object getJsonPost(String surl,JSONObject data){
        try{
            URL url=new URL(surl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            if(this.token!=null)
                urlConnection.setRequestProperty("auth_token",token);
            urlConnection.setRequestProperty("Content-Type","application/json");
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
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
                try{
                    JSONObject obj = new JSONObject(stringBuilder.toString());
                    return obj;
                }catch (Exception e){}
                try{
                    JSONArray arr = new JSONArray(stringBuilder.toString());
                    return arr;
                }catch (Exception e){}
                return null;
            }else{
                return null;
            }

        }catch (Exception ex){
            return null;
        }
    }

    public JSONObject getJsonPut(String surl,JSONObject data){
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
