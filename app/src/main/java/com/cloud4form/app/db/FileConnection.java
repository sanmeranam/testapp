package com.cloud4form.app.db;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * Created by I326482 on 9/9/2016.
 */
public class FileConnection {

    private Context context;

    public FileConnection(Context context) {
        this.context = context;
    }

    public void saveObject(Object obj){
        if(obj!=null){
            writeToFile(obj,obj.getClass().getName().toLowerCase()+"_ins.obj");
        }
    }

    public void saveArray(ArrayList obj,Class className){
        if(obj!=null){
            writeToFile(obj,className.getName().toLowerCase()+"_arr.obj");
        }
    }

    public Object readObject(Class className){
        return readFromFile(className.getName().toLowerCase()+"_ins.obj");

    }

    public ArrayList readArray(Class className){
        Object res= readFromFile(className.getName().toLowerCase()+"_arr.obj");
        if(res!=null){
            return (ArrayList)res;
        }
        return new ArrayList();
    }

    private Object readFromFile(String Name){
        Object result=null;
        try{
            FileInputStream fileInputStream = context.openFileInput(Name);
            ObjectInputStream stream=new ObjectInputStream(fileInputStream);
            result=stream.readObject();
            stream.close();
        }catch (Exception ex){
            ex.printStackTrace();
        }

        return result;
    }

    private void writeToFile(Object obj,String Name){
        try {
            FileOutputStream fos = context.openFileOutput(Name,Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(obj);
            oos.close();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    private File getFilePath(){
        File storageDir = Environment.getExternalStorageDirectory();
        File filePath = new File(storageDir.getAbsolutePath(), "c4f_objects");
        if (!filePath.exists()) {
            filePath.mkdir();
        }
        return filePath;
    }

}
