package com.archermind.txtbl.domain;

import java.util.Date;

public class AccountStats {
    private String status;
    private int totalEmailsFetched;
    private int emailsInInbox;
    private Date lastServerFetch;
    private int loginFailureCount;
    private boolean initialFetchInProgress;
    private int totalEmailsUploaded;
    private String syncUrl;
    private String sessionId;
    private String sendStatus;

    public AccountStats(Account account) {
        status = account.getStatus();
        totalEmailsFetched = account.getMessage_count();
        emailsInInbox = account.getFolder_depth() != null ? account.getFolder_depth() : 0;
        lastServerFetch = account.getLast_received_date();
        loginFailureCount = account.getLogin_failures() != null ? account.getLogin_failures() : 0;
    }

    public void setTotalEmailsUploaded(int totalEmailsUploaded) {
        this.totalEmailsUploaded = totalEmailsUploaded;
    }

    public void setInitialFetchInProgress(boolean initialFetchInProgress) {
        this.initialFetchInProgress = initialFetchInProgress;
    }

    public void updateXobniDetails(XobniAccount xobniAccount) {
        syncUrl = xobniAccount.getSyncUrl();
        sessionId = xobniAccount.getSessionId();
        sendStatus = xobniAccount.isActive() ? "1" : "0";
    }
}
