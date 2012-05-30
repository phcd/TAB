package com.archermind.txtbl.domain;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class OriginalReceivedEmail implements Serializable {

	private static final long serialVersionUID = -1421613666584579589L;

	private Integer id = 0;

	private String userId = "";

	private String emailFrom = "";

	private String emailTo = "";

	private String reply = "";

	private String cc = "";

	private String bcc = "";

	private String subject = "";

	private byte[] body = null;

	private String createTime = "";

	private String mailTime = "";

	private String uid = "";

	private String original_account = "";

	private String mail_type = "";

    private String emailFromAlias = "";

    private int imapFlags = 0;

    private int messageSize = 0;

	private List<OriginalReceivedAttachment> attachList = new ArrayList<OriginalReceivedAttachment>();

    private boolean sent;

    public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
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

	public String getReply() {
		return reply;
	}

	public void setReply(String reply) {
		this.reply = reply;
	}

	public String getCc() {
		return cc;
	}

	public void setCc(String cc) {
		this.cc = cc;
	}

	public String getBcc() {
		return bcc;
	}

	public void setBcc(String bcc) {
		this.bcc = bcc;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public byte[] getBody() {
		return body;
	}

	public void setBody(byte[] body) {
		this.body = body;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getMailTime() {
		return mailTime;
	}

	public void setMailTime(String mailTime) {
		this.mailTime = mailTime;
	}

	public List<OriginalReceivedAttachment> getAttachList() {
		return attachList;
	}

	public void setAttachList(List<OriginalReceivedAttachment> attachList) {
		this.attachList = attachList;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getOriginal_account() {
		return original_account;
	}

	public void setOriginal_account(String original_account) {
		this.original_account = original_account;
	}

	public String getMail_type() {
		return mail_type;
	}

	public void setMail_type(String mail_type) {
		this.mail_type = mail_type;
	}

    public String getEmailFromAlias() {
        return emailFromAlias;
    }

    public void setEmailFromAlias(String emailFromAlias) {
        this.emailFromAlias = emailFromAlias;
    }

    public String toString() {
		String[] excludeFieldNames = new String[] {"body", "attachList"};
		return ReflectionToStringBuilder.toStringExclude(this, excludeFieldNames);
	}

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public boolean isSent() {
        return sent;
    }

    public int getImapFlags(){
        return imapFlags;
    }

    public void setImapFlags(int flags){
        this.imapFlags = flags;
    }

    public void setMessageSize(int messageSize) {
        this.messageSize = messageSize;
    }

    public int getMessageSize() {
        return messageSize;
    }
}
