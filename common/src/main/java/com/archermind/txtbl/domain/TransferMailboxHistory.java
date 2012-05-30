package com.archermind.txtbl.domain;

import java.util.Date;

public class TransferMailboxHistory {

    private int id = 0;

    private int user_id = 0;
    private String peekAccountId;
    private String otherPeekAccountId;

    private int to_user_id = 0;

    private String mailbox = "";

    private Date operate_time = null;

    private String owner = "";

    public String getPeekAccountId() {
        return peekAccountId;
    }

    public void setPeekAccountId(String peekAccountId) {
        this.peekAccountId = peekAccountId;
    }

    public String getOtherPeekAccountId() {
        return otherPeekAccountId;
    }

    public void setOtherPeekAccountId(String otherPeekAccountId) {
        this.otherPeekAccountId = otherPeekAccountId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMailbox() {
        return mailbox;
    }

    public void setMailbox(String mailbox) {
        this.mailbox = mailbox;
    }

    public Date getOperate_time() {
        return operate_time;
    }

    public void setOperate_time(Date operate_time) {
        this.operate_time = operate_time;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public int getTo_user_id() {
        return to_user_id;
    }

    public void setTo_user_id(int to_user_id) {
        this.to_user_id = to_user_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }
}
