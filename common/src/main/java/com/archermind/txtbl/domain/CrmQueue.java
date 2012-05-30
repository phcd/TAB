package com.archermind.txtbl.domain;

import java.io.Serializable;

public class CrmQueue implements Serializable {

	private static final long serialVersionUID = 1L;

	private String user_id = "";

	private String name = "";

	private String device_code = "";

	private String sim_code = "";

	private String cmd = "";

	private String messages = "";

	private String status = "";

	private String create_time = "";

	private String comment = "";

	private String peek_account = "";

	private int server_id = 0;
    private Country country;
    private PartnerCode partnerCode;

    public String getCmd() {
		return cmd;
	}

	public void setCmd(String cmd) {
		this.cmd = cmd;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getCreate_time() {
		return create_time;
	}

	public void setCreate_time(String create_time) {
		this.create_time = create_time;
	}

	public String getDevice_code() {
		return device_code;
	}

	public void setDevice_code(String device_code) {
		this.device_code = device_code;
	}

	public String getMessages() {
		return messages;
	}

	public void setMessages(String messages) {
		this.messages = messages;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSim_code() {
		return sim_code;
	}

	public void setSim_code(String sim_code) {
		this.sim_code = sim_code;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public String getPeek_account() {
		return peek_account;
	}

	public void setPeek_account(String peek_account) {
		this.peek_account = peek_account;
	}

	public int getServer_id() {
		return server_id;
	}

	public void setServer_id(int server_id) {
		this.server_id = server_id;
	}

    public void setCountry(Country country) {
        this.country = country;
    }

    public Country getCountry() {
        return country;
    }

    public PartnerCode getPartnerCode() {
        return partnerCode;
    }

    public void setPartnerCode(PartnerCode partnerCode) {
        this.partnerCode = partnerCode;
    }
}
