package com.archermind.txtbl.domain;

import java.io.Serializable;

public class CRMDirectMessage implements Serializable{

	private static final long serialVersionUID = -512142334790703109L;
	/**
	 * <account>1092cd0b-e107-4dcc-850f-439ad9b4dedf</account>
<from_address>care@getpeek.com</email_address>
<email_address>test@getpeek.com</email_address>
<message_subject>Your Peek service will expire in two days!</message_subject>
<message_text><![CDATA[
Hello Jimmy,
Your Peek service is set to expire in two days. Please do a balance top-up at an authorized Peek dealer.
Thank you,
The Peek Team 
]]>
</message_text>

	 */
	String account = null;
	String from_address = null ; 
	String email_address = null;
	String message_subject = null;
	String message_text = null;
	
	public String getFrom_address() {
		return from_address;
	}

	public void setFrom_address(String from_address) {
		this.from_address = from_address;
	}

	public String getEmail_address() {
		return email_address;
	}

	public void setEmail_address(String email_address) {
		this.email_address = email_address;
	}

	public String getMessage_subject() {
		return message_subject;
	}

	public void setMessage_subject(String message_subject) {
		this.message_subject = message_subject;
	}

	public String getMessage_text() {
		return message_text;
	}

	public void setMessage_text(String message_text) {
		this.message_text = message_text;
	}



	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

}
