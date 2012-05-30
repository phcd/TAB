package com.archermind.txtbl.domain;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.Serializable;
import java.util.Date;

public class User implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id = "";

	private String peek_account = "";

	private String device_id = "";

	private Date registeTime = null;

	private String status = "";

	private String alias = "";

	private String firstname = "";

	private String lastname = "";

	private String middlename = "";

	private String gender = "";

	private Date birthday = null;

	private String description = "";

	private String comment = "";

	private String actionFlag = "";

	private String operateMsg = "";

	private String sourceIPAdreess = "";

	private String change_flag = "";
    private Country country;
    private PartnerCode partnerCode;

    // Property accessors

	public String getSourceIPAdreess() {
		return sourceIPAdreess;
	}

	public void setSourceIPAdreess(String sourceIPAdreess) {
		this.sourceIPAdreess = sourceIPAdreess;
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

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getRegisteTime() {
		return this.registeTime;
	}

	public void setRegisteTime(Date registeTime) {
		this.registeTime = registeTime;
	}

	public String getStatus() {
		return this.status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getAlias() {
		return this.alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getFirstname() {
		return this.firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return this.lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getMiddlename() {
		return this.middlename;
	}

	public void setMiddlename(String middlename) {
		this.middlename = middlename;
	}

	public String getGender() {
		return this.gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public Date getBirthday() {
		return this.birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getComment() {
		return this.comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getDevice_id() {
		return device_id;
	}

	public void setDevice_id(String device_id) {
		this.device_id = device_id;
	}

	public String getPeek_account() {
		return peek_account;
	}

	public void setPeek_account(String peek_account) {
		this.peek_account = peek_account;
	}

	public String getChange_flag() {
		return change_flag;
	}

	public void setChange_flag(String change_flag) {
		this.change_flag = change_flag;
	}

    @Override
    public String toString()
    {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public Country getCountry()
    {
        return country;
    }

    public void setCountry(Country country)
    {
        this.country = country;
    }

    public PartnerCode getPartnerCode() {
        return partnerCode;
    }

    public void setPartnerCode(PartnerCode partnerCode) {
        this.partnerCode = partnerCode;
    }
}