package com.archermind.txtbl.domain;

import com.archermind.txtbl.utils.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class XobniAccount {

    private String userID;
    private String name;
    private String oauthToken;
    private String oauthTokenSecret;
    private String sessionId;
    private String dbVersion;
    private String syncUrl;
    private String consumerKey;
    private String consumerSecret;
    private boolean status = true;
    String lastSyncFailure;
    Date lastSyncFailureDate;
    int syncFailureCount;

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }


    public String getDbVersion() {
        return dbVersion;
    }

    public void setDbVersion(String dbVersion) {
        this.dbVersion = dbVersion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOauthToken() {
        return oauthToken;
    }

    public void setOauthToken(String oauthToken) {
        this.oauthToken = oauthToken;
    }

    public String getOauthTokenSecret() {
        return oauthTokenSecret;
    }

    public void setOauthTokenSecret(String oauthTokenSecret) {
        this.oauthTokenSecret = oauthTokenSecret;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSyncUrl() {
        return syncUrl;
    }

    public void setSyncUrl(String syncUrl) {
        this.syncUrl = syncUrl;
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public String getConsumerSecret() {
        return consumerSecret;
    }

    public void setConsumerSecret(String consumerSecret) {
        this.consumerSecret = consumerSecret;
    }

    public List<String> validate() {
        ArrayList<String> errors = new ArrayList<String>();
        if(StringUtils.isEmpty(sessionId)) {
            errors.add("sessionId is emtpy");
        }
        return errors;
    }

    public boolean isActive() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public void resetSyncErrors() {
        this.status = true;
        this.lastSyncFailure = null;
        this.lastSyncFailureDate = null;
        this.syncFailureCount = 0;
    }

    public void setSyncError(String syncError, int maxAllowedSyncRetries) {
        lastSyncFailure = syncError;
        lastSyncFailureDate = new Date();
        syncFailureCount += 1;
        if(syncFailureCount > maxAllowedSyncRetries) {
            status = false;
        }
    }

    public boolean hasSyncError() {
        return lastSyncFailure != null || syncFailureCount > 0 || lastSyncFailureDate != null || !isActive();
    }
}
