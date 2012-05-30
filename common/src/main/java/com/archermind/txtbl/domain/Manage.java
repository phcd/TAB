package com.archermind.txtbl.domain;

public class Manage {
	private String actionFlag = "";

	private String operateMsg = "";

	private String id = "";

	private String user_id = "";

	private String role_id = "";

	private String username = "";

	private String password = "";

	private String user_description = "";

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRole_id() {
		return role_id;
	}

	public void setRole_id(String role_id) {
		this.role_id = role_id;
	}

	public String getUser_description() {
		return user_description;
	}

	public void setUser_description(String user_description) {
		this.user_description = user_description;
	}

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getActionFlag() {
    	return actionFlag;
    }

	public void setActionFlag(String actionFlag) {
    	this.actionFlag = actionFlag;
    }

	public String getOperateMsg() {
    	return operateMsg;
    }

	public void setOperateMsg(String operateMsg) {
    	this.operateMsg = operateMsg;
    }

}
