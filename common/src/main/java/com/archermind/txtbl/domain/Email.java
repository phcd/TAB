package com.archermind.txtbl.domain;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class Email implements Serializable {

    private static final long serialVersionUID = 1L;

    private String original_account = "";

    private int id = 0;

    private int mailid = 0;

    private String userId = "";

    private byte[] from = null;

    private byte[] to = null;

    private byte[] reply = null;

    private byte[] cc = null;

    private byte[] bcc = null;

    //private String alias = "";

    private byte[] subject = null;

    private int bodySize = 0;

    private String comment = "";

    private String status = "";

    private String sentTime = "";

    private String receivedTime = "";

    private String email_type = "normal";

    private int origin_id = 0;

    private String modify_time = "";

    private transient HashMap<String, String> meta;

    // abstraction for the twitter "source" and messageId stored in the comment column
    private transient TwitterMeta twitterMeta;

    private String destination = "";

    /**
     * 0:send text type 1:sned html type 2:send alternative type
     */
    private String dataType = "0";

    private String maildate = "";

    private String bcc_flag = "0";

    private String from_alias = "";

    private String message_type = "";

    private boolean sent;
    private String messageId;

    private int imap_status = 0;   //TODO: #ME Int for now, turn this into a custom class later, with easy get/set etc for flags.

    public String getMessage_type() {
        return message_type;
    }

    public void setMessage_type(String message_type) {
        this.message_type = message_type;
    }

    public String getBcc_flag() {
        return bcc_flag;
    }

    public void setBcc_flag(String bcc_flag) {
        this.bcc_flag = bcc_flag;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getMaildate() {
        return maildate;
    }

    public void setMaildate(String maildate) {
        this.maildate = maildate;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFrom() {
        String result = null;
        if (this.from != null) {
            try {
                result = new String(this.from, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                result = new String(this.from);
            }
        }
        return result;
    }

    public void setFrom(String from) {
        if (from == null)
            this.from = null;
        else {
            try {
                this.from = from.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                this.from = from.getBytes();
            }
        }
    }

    public String getTo() {
        String result = null;
        if (this.to != null) {
            try {
                result = new String(this.to, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                result = new String(this.to);
            }
        }
        return result;
    }

    public void setTo(String to) {
        if (to == null)
            this.to = null;
        else {
            try {
                this.to = to.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                this.to = to.getBytes();
            }
        }
    }

    public String getReply() {
        String result = null;
        if (this.reply != null) {
            try {
                result = new String(this.reply, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                result = new String(this.reply);
            }
        }
        return result;
    }

    public void setReply(String reply) {
        if (reply == null)
            this.reply = null;
        else {
            try {
                this.reply = reply.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                this.reply = reply.getBytes();
            }
        }
    }

    public String getCc() {
        String result = null;
        if (this.cc != null) {
            try {
                result = new String(this.cc, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                result = new String(this.cc);
            }
        }
        return result;
    }

    public void setCc(String cc) {
        if (cc == null)
            this.cc = null;
        else {
            try {
                this.cc = cc.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                this.cc = cc.getBytes();
            }
        }
    }

    public String getBcc() {
        String result = null;
        if (this.bcc != null) {
            try {
                result = new String(this.bcc, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                result = new String(this.bcc);
            }
        }
        return result;
    }

    public void setBcc(String bcc) {
        if (bcc == null)
            this.bcc = null;
        else {

            try {
                this.bcc = bcc.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                this.bcc = bcc.getBytes();
            }
        }
    }


    public String getSubject() {
        String result = null;
        if (this.subject != null) {
            try {
                result = new String(this.subject, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                result = new String(this.subject);
            }
        }
        return result;
    }

    public void setSubject(String subject) {
        if (subject == null)
            this.subject = null;
        else {
            try {
                this.subject = subject.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                this.subject = subject.getBytes();
            }
        }
    }

    public int getBodySize() {
        return this.bodySize;
    }

    public void setBodySize(int bodySize) {
        this.bodySize = bodySize;
    }

    public String getComment() {
        return this.comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getMailid() {
        return mailid;
    }

    public void setMailid(int mailid) {
        this.mailid = mailid;
    }

    public HashMap<String, String> getMeta() {
        return meta;
    }

    public void setMeta(HashMap<String, String> meta) {
        this.meta = meta;
    }

    public String getSentTime() {
        return sentTime;
    }

    public void setSentTime(String sentTime) {
        this.sentTime = sentTime;
    }

    public String getReceivedTime() {
        return receivedTime;
    }

    public void setReceivedTime(String receivedTime) {
        this.receivedTime = receivedTime;
    }

    public String getEmail_type() {
        return email_type;
    }

    public void setEmail_type(String email_type) {
        this.email_type = email_type;
    }

    public int getOrigin_id() {
        return origin_id;
    }

    public void setOrigin_id(int origin_id) {
        this.origin_id = origin_id;
    }

    public String getModify_time() {
        return modify_time;
    }

    public void setModify_time(String modify_time) {
        this.modify_time = modify_time;
    }

    public String getOriginal_account() {
        return original_account;
    }

    public void setOriginal_account(String original_account) {
        this.original_account = original_account;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getFrom_alias() {
        return from_alias;
    }

    public void setFrom_alias(String from_alias) {
        this.from_alias = from_alias;
    }

    public Date getMailDateAsDate() {
        // mailDate string format is: "yyyy-MM-dd HH:mm:ss"
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return formatter.parse(this.getMaildate());
        } catch (ParseException e) {
            throw new RuntimeException("Unable to parse received time " + this.getReceivedTime());
        }
    }

    public Date getReceivedDateAsDate() {
        // mailDate string format is: "yyyy-MM-dd HH:mm:ss"
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return formatter.parse(this.getReceivedTime());
        } catch (ParseException e) {
            throw new RuntimeException("Unable to parse received time " + this.getReceivedTime());
        }
    }

    public void setTwitterMeta(TwitterMeta twitterMeta) {
        this.setComment(twitterMeta.toComment());
    }

    public TwitterMeta getTwitterMeta() {
        return TwitterMeta.createFromString(this.getComment());
    }

    public boolean getSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageId() {
        return messageId;
    }

    public int getImap_status() {
        return imap_status;
    }

    public void setImap_status(int imap_status) {
        this.imap_status = imap_status; //TODO:  create the custom object here.
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
