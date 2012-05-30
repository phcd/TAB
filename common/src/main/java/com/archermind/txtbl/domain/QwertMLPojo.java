package com.archermind.txtbl.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class QwertMLPojo implements Serializable {

	private static final long serialVersionUID = 1L;
	private UserPojo userpojo=new UserPojo();
	private List <EmailPojo>emails=  new ArrayList<EmailPojo>(); //--> EmailPojo;
	public String version="";
	private String Method="";
	private String msg="";
	private String msgCode="";
	
	public List<EmailPojo> getEmails() {
		return emails;
	}
	public void setEmails(List<EmailPojo> emails) {
		this.emails = emails;
	}
	public UserPojo getUserPojo() {
		return userpojo;
	} 
	public void setUserPojo(UserPojo userpojo) {
		this.userpojo = userpojo;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getMethod() {
		return Method;
	}
	public void setMethod(String method) {
		Method = method;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public String getMsgCode() {
		return msgCode;
	}
	public void setMsgCode(String msgCode) {
		this.msgCode = msgCode;
	}
}
