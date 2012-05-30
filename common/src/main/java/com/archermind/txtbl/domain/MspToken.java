package com.archermind.txtbl.domain;

import java.io.Serializable;

public class MspToken implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String user_id = "";

	private String name = "";

	private byte[] token_id = null;

	private String transaction_id = "";

	private long create_number = 0;

	private String create_time = "";

	private String comment = "";

	public long getCreate_number() {
		return create_number;
	}

	public void setCreate_number(long create_number) {
		this.create_number = create_number;
	}

	public String getCreate_time() {
		return create_time;
	}

	public void setCreate_time(String create_time) {
		this.create_time = create_time;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	
	public String getTransaction_id() {
		return transaction_id;
	}

	public void setTransaction_id(String transaction_id) {
		this.transaction_id = transaction_id;
	}

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public byte[] getToken_id() {
		return token_id;
	}

	public void setToken_id(byte[] token_id) {
		this.token_id = token_id;
	}
}
