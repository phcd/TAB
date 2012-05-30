package com.archermind.txtbl.domain;

import java.util.Date;

public class PeekAccountHistory {
	
	private int id=0;
	
	private int user_id=0;
	
	private String old_operate="";
	
	private String new_operate="";
	
	private Date operate_time=null;
	
	private String owner="";

	public String getNew_operate() {
		return new_operate;
	}

	public void setNew_operate(String new_operate) {
		this.new_operate = new_operate;
	}

	public String getOld_operate() {
		return old_operate;
	}

	public void setOld_operate(String old_operate) {
		this.old_operate = old_operate;
	}

	public Date getOperate_time() {
		return operate_time;
	}

	public void setOperate_time(Date operate_time) {
		this.operate_time = operate_time;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public int getUser_id() {
		return user_id;
	}

	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	 


}
