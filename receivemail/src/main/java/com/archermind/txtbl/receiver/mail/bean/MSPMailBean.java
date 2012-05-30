package com.archermind.txtbl.receiver.mail.bean;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class MSPMailBean {
    private String bcc;
    private String cc;
    private String emailFromAlias;
    private String emailFrom;
    private String emailTo;
    private String subject;
    private String mailTime;
    private String plainBody;
    private String response;
    private String recipientType;
    private String errorText;
    private int attachNumber;
    private List<MSPAttachmentBean> attachments = new ArrayList<MSPAttachmentBean>();

    public void addArrachment(String attachName) {
        attachments.add(new MSPAttachmentBean(attachName));
    }

    public void setAttachNumber(int attachNumber) {
        this.attachNumber = attachNumber;
    }

    public void setAttachData(String data) {
        attachments.get(attachNumber-1).setData(data);
    }

    public String getBcc() {
        return bcc;
    }

    public void setBcc(String bcc) {
        this.bcc = bcc;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public String getEmailFrom() {
        return emailFrom;
    }

    public void setEmailFrom(String emailFrom) {
        this.emailFrom = emailFrom;
    }

    public String getEmailTo() {
        return emailTo;
    }

    public void setEmailTo(String emailTo) {
        this.emailTo = emailTo;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMailTime() {
        return mailTime;
    }

    public void setMailTime(String mailTime) {
        this.mailTime = mailTime;
    }

    public String getPlainBody() {
        return plainBody;
    }

    public void setPlainBody(String plainBody) {
        this.plainBody = plainBody;
    }

    public List<MSPAttachmentBean> getAttachments() {
        return attachments;
    }

    public boolean hasAttachments() {
        return attachments.size() > 0;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getRecipientType() {
        return recipientType;
    }

    public void setRecipientType(String recipientType) {
        this.recipientType = recipientType;
    }

    public void setRecipientValue(String recipientValue) {
        if ("To".equals(recipientType))
        {
            emailTo = recipientValue;
        }
        else if ("Cc".equals(recipientType))
        {
            cc = recipientValue;
        }
    }

    public String getEmailFromAlias() {
        return emailFromAlias;
    }

    public void setEmailFromAlias(String emailFromAlias) {
        this.emailFromAlias = emailFromAlias;
    }

    public String getErrorText() {
        return errorText;
    }

    public void setErrorText(String errorText) {
        this.errorText = errorText;
    }
}
