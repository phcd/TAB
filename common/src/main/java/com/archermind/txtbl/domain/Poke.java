package com.archermind.txtbl.domain;

import com.archermind.txtbl.utils.MailUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Poke {

    private String key;
    private String sentDateUTC;
    private String to;
    private String from ;
    private String alias;
    private String subject;
    private String body;
    private String type;
    private Date mailDate;
    private LocationServiceResponse response;
    private static boolean isTransliteratePoke= true;
    private static boolean isTransliteratePokePeekvetica = true;

    public Poke(String key, String sentDateUTC, String to, String from, String alias, String subject, String body, String type) {
        this.key = key;
        this.sentDateUTC = sentDateUTC;
        this.to = to;
        this.from = from;
        this.alias = alias;
        this.subject = subject;
        this.body = body;
        this.type = type;
        this.response = new LocationServiceResponse();

    }


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSentDateUTC() {
        return sentDateUTC;
    }

    public void setSentDateUTC(String sentDateUTC) {
        this.sentDateUTC = sentDateUTC;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getMailDate() {
        return mailDate;
    }

    public void setMailDate(Date mailDate) {
        this.mailDate = mailDate;
    }

    public LocationServiceResponse getResponse() {
        return response;
    }

    public void setResponse(LocationServiceResponse response) {
        this.response = response;
    }

    public void clean(){


        subject = MailUtils.clean(subject,isTransliteratePoke, isTransliteratePokePeekvetica);
        alias = MailUtils.clean(alias, isTransliteratePoke, isTransliteratePokePeekvetica);

    }

    public OriginalReceivedEmail toOriginalReceivedEmail(Integer userId){

        OriginalReceivedEmail originalEmail = new OriginalReceivedEmail();
        originalEmail.setSubject(subject);
        originalEmail.setEmailFrom(from);

        if (to.endsWith("@twitterpeek")) {
            originalEmail.setEmailTo(to.split("@")[0]);
        } else {
            originalEmail.setEmailTo(to);
        }

        originalEmail.setUserId(String.valueOf(userId.intValue()));
        originalEmail.setMail_type(type);
        originalEmail.setMailTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(mailDate));

        return originalEmail;

    }
}
