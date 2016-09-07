package com.cloud4form.app.other;

import org.json.JSONObject;

/**
 * Created by I326482 on 9/7/2016.
 */
public class FormMetaEntity {
    public static String ARG_DATA="ARG_DATA";
    public static String ARG_FORM_ENTRY_ID="ARG_FORM_ENTRY_ID";
    public static String ARG_MODE="ARG_MODE";
    public static String ARG_MODE_VIEW="ARG_MODE_VIEW";
    public static String ARG_MODE_EDIT="ARG_MODE_EDIT";
    public static String ARG_MODE_NEW="ARG_MODE_NEW";


    public String formId;
    public String formName;
    public JSONObject formMeta;
    public long _id;
    public int version;

    FormMetaEntity(){}

    public FormMetaEntity(JSONObject obj){
        try{
            _id=Math.round(Math.random()*199999);
            this.formId=obj.getString("_id");
            this.formName=obj.getString("form_name");
            this.version=obj.getInt("version");

            if(obj.has("model_view")){
                this.formMeta=obj.getJSONObject("model_view");
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }

    @Override
    public String toString() {
        return _id+"#"+formId+"#"+formName+"#"+version;
    }
    public static FormMetaEntity parse(String dd){
        String[] data=dd.split("#");
        FormMetaEntity fc=new FormMetaEntity();
        fc._id=Long.parseLong(data[0]);
        fc.formId=data[1];
        fc.formName=data[2];
        fc.version=Integer.parseInt(data[3]);
        return fc;
    }
}
