package com.archermind.txtbl.domain;

import java.io.Serializable;

public class Login implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id = "";

    private String loginTime = "";

    private String location = "";

    private String comment = "";

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLoginTime() {
        return this.loginTime;
    }

    public void setLoginTime(String loginTime) {
        this.loginTime = loginTime;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getComment() {
        return this.comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

}