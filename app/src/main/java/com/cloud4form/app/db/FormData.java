package com.cloud4form.app.db;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by I326482 on 9/9/2016.
 */
public class FormData extends Entity {
    public static enum FIELD_TYPE{audio_record,video_record,sign_input,file_attach,photo_attach};
    public static enum SENT_STATUS{NEW,SENDING,SENT,ERROR,INCOMING};

    private String formName;
    private String serverId;
    private String internalId;
    private String metaId;

    private String model;
    private String data;
    private String flow;
    private String currentAction;
    private String stageHistory;
    private String nextStage;




    private Date createDate;
    private int version;
    public SENT_STATUS SentStatus=SENT_STATUS.NEW;
    private HashMap<String,FieldEntity> fields =new HashMap<>();

    public FormData(JSONObject object){
        try{
            this.SentStatus=SENT_STATUS.INCOMING;
            this.serverId=object.getString("_id");
            this.internalId=object.getString("internal_id");
            this.metaId=object.getString("meta_id");
            this.createDate=new Date();
            this.version=object.getInt("version");
            this.setModel(object.getJSONObject("model"));
            this.setStageHistory(object.getJSONArray("stage_history"));
            this.setCurrentAction(object.getJSONObject("current_action"));
            this.setFlow(object.getJSONObject("flow"));
            this.setNextStage(object.getJSONObject("next_stage"));

            JSONArray data=object.getJSONArray("data");
            for(int i=0;i<data.length();i++){
                JSONObject ins=data.getJSONObject(i);
                this.fields.put(ins.getString("_i"),new FieldEntity(ins));
            }

            this.formName=object.getString("form_name");
        }catch (Exception ex){

        }
    }

    public FormData(JSONObject object,int version,String mdetaId){
        this.internalId="id:"+Math.round(Math.random()*99999);
        this.serverId="";
        try{
            this.metaId=mdetaId;
            this.createDate=new Date();
            this.version=version;
            Iterator iterator=object.keys();
            while(iterator.hasNext()){
                String sKey=iterator.next().toString();
                JSONObject ind=object.getJSONObject(sKey);
                FieldEntity entity=new FieldEntity(sKey,ind.getString("_n"),ind.get("_v").toString());
                if(ind.has("_l")){
                    entity.label=ind.getString("_l");
                }

                fields.put(sKey,entity);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public JSONObject getNextStage() {
        try {
            return new JSONObject(nextStage);
        }catch (Exception ex){
        }
        return new JSONObject();
    }

    public void setNextStage(JSONObject nextStage) {
        this.nextStage = nextStage.toString();
    }

    public JSONArray getStageHistory() {
        try {
            return new JSONArray(stageHistory);
        }catch (Exception ex){

        }
        return new JSONArray();
    }

    public void setStageHistory(JSONArray lastAction) {
        this.stageHistory = lastAction.toString();
    }

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public String getInternalId() {
        return internalId;
    }

    public void setInternalId(String internalId) {
        this.internalId = internalId;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getMetaId() {
        return metaId;
    }

    public void setMetaId(String metaId) {
        this.metaId = metaId;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }


    public JSONObject getFlow() {
        try {
            return new JSONObject(this.flow);
        }catch (Exception ex){
        }
        return null;
    }

    public void setFlow(JSONObject flow) {
        this.flow = flow.toString();
    }

    public JSONObject getModel() {
        try {
            return new JSONObject(this.model);
        }catch (Exception ex){
        }
        return null;
    }

    public void setModel(JSONObject model) {
        this.model = model.toString();
    }

//    public JSONObject getData() {
//        try {
//            return new JSONObject(this.data);
//        }catch (Exception ex){
//        }
//        return null;
//    }
//
//    public void setData(JSONObject model) {
//        this.data = model.toString();
//    }

    public JSONObject getCurrentAction() {
        try {
            return new JSONObject(this.currentAction);
        }catch (Exception ex){
        }
        return null;
    }

    public void setCurrentAction(JSONObject currentAction) {
        this.currentAction = currentAction.toString();
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public HashMap<String, FieldEntity> getFields() {
        return fields;
    }

    public class FieldEntity implements Serializable {


        public String fieldId;
        public String type;
        public String value;
        public String label;
        public SENT_STATUS SentStatus=SENT_STATUS.NEW;

        public FieldEntity(JSONObject object){
            try{
                this.fieldId=object.getString("_i");
                this.type=object.getString("_t");
                this.value=object.getString("_v");
                this.label=object.getString("_l");
            }catch (Exception ex){

            }
        }

        public FieldEntity(String fieldId, String type, String value) {
            this.fieldId = fieldId;
            this.type = type;
            this.value = value;
        }
    }
}
