package com.archermind.txtbl.receiver.mail.bean;

import java.util.Date;

import javax.mail.Message;

public class MailDateBean implements Comparable<MailDateBean> {

	private String msgID = "";

	private Message msg = null;

	private Date mailDate = null;

	public MailDateBean(String msgID, Message msg, Date mailDate) {
		super();
		this.msg = msg;
		this.msgID = msgID;
		this.mailDate = mailDate;
	}

	public String getMsgID() {
		return msgID;
	}

	public void setMsgID(String msgID) {
		this.msgID = msgID;
	}

	public Message getMsg() {
		return msg;
	}

	public void setMsg(Message msg) {
		this.msg = msg;
	}

	public int compareTo(MailDateBean bean) {
		int flag = 0;
		if (!this.mailDate.equals(bean.getMailDate())) {
			flag = this.mailDate.before(bean.getMailDate()) ? 1 : -1;
		}
		return flag;
	}

	public Date getMailDate() {
		return mailDate;
	}

	public void setMailDate(Date mailDate) {
		this.mailDate = mailDate;
	}

}
