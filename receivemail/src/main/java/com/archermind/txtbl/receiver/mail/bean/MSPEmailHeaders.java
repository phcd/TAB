package com.archermind.txtbl.receiver.mail.bean;

import java.util.ArrayList;
import java.util.List;

public class MSPEmailHeaders {
    private List<MSPMessageHeader> headers = new ArrayList<MSPMessageHeader>();
    private boolean hasAttachments;
    private String error;

    public void addHeader(String messageId, Integer size, Boolean hasAttach){
        headers.add(new MSPMessageHeader(messageId, size, hasAttach));
    }

    public List<MSPMessageHeader> getHeaders() {
        return headers;
    }

    public boolean isHasAttachments() {
        return hasAttachments;
    }

    public void setHasAttachments(boolean hasAttachments) {
        this.hasAttachments = hasAttachments;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
