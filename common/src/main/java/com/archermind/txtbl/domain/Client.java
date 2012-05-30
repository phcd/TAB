package com.archermind.txtbl.domain;

import java.io.Serializable;

public class Client implements Serializable {

	private String id = "";

	private String name = "";

	private String feature_id = "";

    private String key_id = "";

    private String login_id = "";

    private String encoding = "";

    private String api_type = "";

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getFeature_id() {
        return this.feature_id;
    }

    public String getKey_id() {
        return this.key_id;
    }

    public String getLogin_id() {
        return this.login_id;
    }

    public String getEncoding() {
        return this.encoding;
    }

    public String getApi_type() {
        return this.api_type;
    }

    public void setId(String id) {
        this.id=id;
    }

    public void setName(String name) {
        this.name=name;
    }

    public void setFeature_id(String feature_id) {
        this.feature_id=feature_id;
    }

    public void setKey_id(String key_id) {
        this.key_id=key_id;
    }

    public void setLogin_id(String login_id) {
        this.login_id=login_id;
    }

    public void setEncoding(String encoding) {
        this.encoding=encoding;
    }

    public void setApi_type(String api_type) {
        this.api_type=api_type;
    }
}
