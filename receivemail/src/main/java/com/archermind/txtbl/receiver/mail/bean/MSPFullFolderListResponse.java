package com.archermind.txtbl.receiver.mail.bean;

import java.util.ArrayList;

@SuppressWarnings("unused")
public class MSPFullFolderListResponse {
    private String messagingId;
    private ArrayList<String> folderIds = new ArrayList<String>();
    private String error;
    private String response;

    public String getMessagingId() {
        return messagingId;
    }

    public void setMessagingId(String messagingId) {
        this.messagingId = messagingId;
    }

    public void addFolderId(String folderId) {
        folderIds.add(folderId);
    }

    public String getInboxFolderId() {
        return folderIds.size() > 0?folderIds.get(0):null;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
