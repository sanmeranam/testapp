package com.cloud4form.app.db;

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
    public static enum SENT_STATUS{NEW,SENDING,SENT,ERROR};
    private String serverId;
    private String metaId;
    private Date createDate;
    private int version;
    public SENT_STATUS SentStatus=SENT_STATUS.NEW;
    private HashMap<String,FieldEntity> data=new HashMap<>();

    public FormData(JSONObject object,int version,String mdetaId){
        this.serverId="id:"+Math.round(Math.random()*99999);
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

                data.put(sKey,entity);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
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

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public HashMap<String, FieldEntity> getData() {
        return data;
    }

    public class FieldEntity implements Serializable {


        public String fieldId;
        public String type;
        public String value;
        public String label;
        public SENT_STATUS SentStatus=SENT_STATUS.NEW;

        public FieldEntity(String fieldId, String type, String value) {
            this.fieldId = fieldId;
            this.type = type;
            this.value = value;
        }
    }
}
