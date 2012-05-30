package com.archermind.txtbl.domain;

import java.io.Serializable;
import java.util.Date;

public class BillHistory implements Serializable {

	private static final long serialVersionUID = 1L;

	// Fields

	private int id = 0;

	private String account = "";

	private String userid = "";

	private Date billDate = null;

	private Date startDate = null;

	private String approach = "";

	private String type = "";

	private int term = 0;

	private String comment = "";

	private String status = "";

	private String creditcard = "";

	private Date expiry_date = null;

	private String billcomment = "";



	// Property accessors

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getBillDate() {
		return this.billDate;
	}

	public void setBillDate(Date billDate) {
		this.billDate = billDate;
	}

	public Date getStartDate() {
		return this.startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public String getApproach() {
		return this.approach;
	}

	public void setApproach(String approach) {
		this.approach = approach;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getTerm() {
		return this.term;
	}

	public void setTerm(int term) {
		this.term = term;
	}

	public String getComment() {
		return this.comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getBillcomment() {
		return billcomment;
	}

	public void setBillcomment(String billcomment) {
		this.billcomment = billcomment;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getCreditcard() {
		return creditcard;
	}

	public void setCreditcard(String creditcard) {
		this.creditcard = creditcard;
	}

	public Date getExpiry_date() {
		return expiry_date;
	}

	public void setExpiry_date(Date expiry_date) {
		this.expiry_date = expiry_date;
	}

	



	

}