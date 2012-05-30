package com.archermind.txtbl.domain;

public class TwitterAccount {
    private String name;
    private String oAuthToken;
    private String oAuthTokenSecret;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getoAuthToken() {
        return oAuthToken;
    }

    public void setoAuthToken(String oAuthToken) {
        this.oAuthToken = oAuthToken;
    }

    public String getoAuthTokenSecret() {
        return oAuthTokenSecret;
    }

    public void setoAuthTokenSecret(String oAuthTokenSecret) {
        this.oAuthTokenSecret = oAuthTokenSecret;
    }
}
