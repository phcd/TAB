package com.archermind.txtbl.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EmailPojo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Email email;

    List<Attachment> attachement = new ArrayList<Attachment>();

    Body body;

    private Account account = null;
    private int messageSize;

    public List<Attachment> getAttachement() {
        return attachement;
    }

    public void setAttachement(List<Attachment> attachement) {
        this.attachement = attachement;
    }

    public void setImapStatus(int imapStatus) {
        email.setImap_status(imapStatus);
    }

    public int getImapStats() {
        return email.getImap_status();
    }

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public void addAttachment(Attachment attachment) {
        this.attachement.add(attachment);
    }

    public void setMessageSize(int messageSize) {
        this.messageSize = messageSize;
    }

    public int getMessageSize() {
        return messageSize;
    }
}
