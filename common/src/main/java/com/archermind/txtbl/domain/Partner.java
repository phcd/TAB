package com.archermind.txtbl.domain;

public class Partner {
    private String id = "";

    private String name = "";

    private String status = "";

    private String client_id = "";


    public String getId() {
        return this.id;
    }

    public String getClient_id() {
        return this.client_id;
    }

    public String getStatus() {
        return this.status;
    }

    public String getName() {
        return this.name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
