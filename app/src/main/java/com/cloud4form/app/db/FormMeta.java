package com.cloud4form.app.db;

import com.itextpdf.text.pdf.PdfName;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by I326482 on 9/9/2016.
 */
public class FormMeta extends Entity {
    private String serverId;
    private String name;
    private int version;
    private String model;
    private String flow;


    public FormMeta(JSONObject obj){
        try{
            this.serverId=obj.getString("_id");
            this.name=obj.getString("form_name");
            this.version=obj.getInt("version");

            if(obj.has("model_view")){
                this.model=obj.getJSONObject("model_view").toString();
            }

            if(obj.has("flow")){
                this.flow=obj.getJSONObject("flow").toString();
            }

        }catch (Exception ex){

        }
    }

    public JSONObject getFlow() {
        JSONObject data=null;
        try{
            data=new JSONObject(this.flow);
        }catch (Exception ex){
        }
        return data;
    }

    public void setFlow(JSONArray flow) {
        this.flow = flow.toString();
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public JSONObject getModel() {
        try {
            return new JSONObject(model);
        }catch (Exception ex){

        }
        return new JSONObject();
    }

    public void setModel(JSONObject model) {
        this.model = model.toString();
    }
}
