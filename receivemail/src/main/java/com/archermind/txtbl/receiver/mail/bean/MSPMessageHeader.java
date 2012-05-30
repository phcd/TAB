package com.archermind.txtbl.receiver.mail.bean;

public class MSPMessageHeader {
    private String messageId;
    private Integer size;
    private Boolean hasAttachments;

    public MSPMessageHeader(String messageId, Integer size, Boolean hasAttachments) {
        this.messageId = messageId;
        this.size = size;
        this.hasAttachments = hasAttachments;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Boolean getHasAttachments() {
        return hasAttachments;
    }

    public void setHasAttachments(Boolean hasAttachments) {
        this.hasAttachments = hasAttachments;
    }
}
