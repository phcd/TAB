package com.archermind.txtbl.receiver.mail.bean;

public class MSPAttachmentBean {
    private String fileName;
    private String data;

    public MSPAttachmentBean(String fileName) {
        this.fileName = fileName;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getFileName() {
        return fileName;
    }

    @SuppressWarnings("unused")
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
