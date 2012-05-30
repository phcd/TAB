package com.archermind.txtbl.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

public class Device implements Serializable {

	private static final long serialVersionUID = 1L;

	private int id = 0;

	private String user_id = "";

	private String deviceCode = "";

	private String model = "";

	private int capability = 0;

	private String comment = "";

	private String Sim_code = "";

	private String state = "";

	private Date operate_time = null;

	private String clientsw = "";

	private String lacid = "";

	private String cellid = "";

	private String pin = "";

	private int pp_flag = 0;

	private String imsi = "";

	private String msisdn = "";

	// private String imei_code="";

	private transient HashMap<String, String> meta;

	// Property accessors

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDeviceCode() {
		return this.deviceCode;
	}

	public void setDeviceCode(String deviceCode) {
		this.deviceCode = deviceCode;
	}

	public String getModel() {
		return this.model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public int getCapability() {
		return this.capability;
	}

	public void setCapability(int capability) {
		this.capability = capability;
	}

	public String getComment() {
		return this.comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public HashMap<String, String> getMeta() {
		return meta;
	}

	public void setMeta(HashMap<String, String> meta) {
		this.meta = meta;
	}

	public String getSim_code() {
		return Sim_code;
	}

	public void setSim_code(String sim_code) {
		Sim_code = sim_code;
	}

	public Date getOperate_time() {
		return operate_time;
	}

	public void setOperate_time(Date operate_time) {
		this.operate_time = operate_time;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCellid() {
		return cellid;
	}

	public void setCellid(String cellid) {
		this.cellid = cellid;
	}

	public String getClientsw() {
		return clientsw;
	}

	public void setClientsw(String clientsw) {
		this.clientsw = clientsw;
	}

	public String getLacid() {
		return lacid;
	}

	public void setLacid(String lacid) {
		this.lacid = lacid;
	}

	public String getPin() {
		return pin;
	}

	public void setPin(String pin) {
		this.pin = pin;
	}

	public String getImsi() {
		return imsi;
	}

	public void setImsi(String imsi) {
		this.imsi = imsi;
	}

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public int getPp_flag() {
		return pp_flag;
	}

	public void setPp_flag(int pp_flag) {
		this.pp_flag = pp_flag;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}
}