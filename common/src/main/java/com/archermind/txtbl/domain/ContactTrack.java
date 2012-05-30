package com.archermind.txtbl.domain;

import java.io.Serializable;
import java.util.Date;

public class ContactTrack implements Serializable {

	private static final long serialVersionUID = 1L;

	private String user_id = "";

	private String name = "";

	private Date start_time = new Date();

	private Date current_time = new Date();

	private Date sys_time = new Date();

	private int current_seqno = 0;

	private String status_message = "";

	private String comment = "";

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Date getCurrent_time() {
		return current_time;
	}

	public void setCurrent_time(Date current_time) {
		this.current_time = current_time;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getStart_time() {
		return start_time;
	}

	public void setStart_time(Date start_time) {
		this.start_time = start_time;
	}

	public String getStatus_message() {
		return status_message;
	}

	public void setStatus_message(String status_message) {
		this.status_message = status_message;
	}

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public int getCurrent_seqno() {
		return current_seqno;
	}

	public void setCurrent_seqno(int current_seqno) {
		this.current_seqno = current_seqno;
	}

	public Date getSys_time() {
		return sys_time;
	}

	public void setSys_time(Date sys_time) {
		this.sys_time = sys_time;
	}

}
