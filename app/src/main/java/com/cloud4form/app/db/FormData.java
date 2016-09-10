package com.cloud4form.app.db;

import org.json.JSONObject;

import java.util.Date;

/**
 * Created by I326482 on 9/9/2016.
 */
public class FormData extends Entity {
    private String serverId;
    private String metaId;
    private Date createDate;
    private int version;
    private JSONObject data;

    public FormData(JSONObject object){
        try{

        }catch (Exception ex){

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

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
        this.data = data;
    }
}
