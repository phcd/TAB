package com.archermind.txtbl.domain;

import java.io.Serializable;

public class  Body implements Serializable {

	private static final long serialVersionUID = 1L;
	// Fields

	private int emailid=0;

	private byte[] data=null;

	private String comment="";

	// Constructors

	/** default constructor */
	

	// Property accessors

	public int getEmailid() {
		return this.emailid;
	}

	public void setEmailid(int emailid) {
		this.emailid = emailid;
	}

	public byte[] getData() {
		return this.data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public String getComment() {
		return this.comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}


}