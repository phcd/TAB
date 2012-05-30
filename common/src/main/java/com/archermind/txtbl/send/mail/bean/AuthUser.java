package com.archermind.txtbl.send.mail.bean;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class AuthUser extends Authenticator {

	private String mailboxName = null;

	private String password = null;

	public AuthUser(String mailboxName, String password) {
		this.mailboxName = mailboxName;
		this.password = password;
	}

	protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(mailboxName, password);
	}
}
