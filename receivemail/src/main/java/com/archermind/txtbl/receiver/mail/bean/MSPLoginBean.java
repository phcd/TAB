package com.archermind.txtbl.receiver.mail.bean;

@SuppressWarnings("unused")
public class MSPLoginBean {
    private String createdTime;
    private String token;
    private String error;

    public MSPLoginBean() {

    }

    public MSPLoginBean(String createdTime, String token) {
        this.createdTime = createdTime;
        this.token = token;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
