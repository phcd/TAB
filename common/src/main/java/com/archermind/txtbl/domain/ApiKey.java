package com.archermind.txtbl.domain;

public class ApiKey {

    private String id = "";

    private String access_key = "";

    private String secret_access_key = "";

    private String client_id = "";

    private String status = "1";

    private String user_id = "";

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getId() {
        return this.id;
    }

    public String getClient_id() {
        return this.client_id;
    }


    public void setId(String id) {
        this.id = id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getAccess_key() {
        return access_key;
    }

    public void setAccess_key(String access_key) {
        this.access_key = access_key;
    }

    public String getSecret_access_key() {
        return secret_access_key;
    }

    public void setSecret_access_key(String secret_access_key) {
        this.secret_access_key = secret_access_key;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
