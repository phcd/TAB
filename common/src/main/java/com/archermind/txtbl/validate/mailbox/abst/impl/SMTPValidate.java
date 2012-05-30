package com.archermind.txtbl.validate.mailbox.abst.impl;

import java.security.Security;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.Transport;


import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.send.mail.bean.AuthUser;
import com.archermind.txtbl.validate.mailbox.abst.Validate;

public class SMTPValidate extends Validate {

	public void validate(Account account) throws Exception {
		Properties props = new Properties();
		props.setProperty("mail.transport.protocol", account.getSendProtocolType());
		props.setProperty("mail.smtp.host", account.getSendHost());
		props.setProperty("mail.smtp.port", account.getSendPort());
		if ("1".equals(account.getNeedAuth())) {
			props.put("mail.smtp.auth", "true");
		} else {
			props.put("mail.smtp.auth", "false");
		}
		if ("ssl".equals(account.getSendTs())) {
			Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
			props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			props.setProperty("mail.smtp.socketFactory.fallback", "false");
			props.setProperty("mail.smtp.socketFactory.port", account.getSendPort());
		}else if("tls".equals(account.getSendTs())){
			props.setProperty("mail.smtp.starttls.enable", "true");
		    java.security.Security.setProperty("ssl.SocketFactory.provider", "com.archermind.txtbl.mail.DummySSLSocketFactory");
		}
		props.setProperty("mail.smtp.connectiontimeout", "60000");
		Session session;
		if ("1".equals(account.getNeedAuth())) {
			session = Session.getInstance(props, new AuthUser(account.getLoginName(), account.getPassword()));
		} else {
			session = Session.getInstance(props);
		}
		Transport transport = session.getTransport();
		transport.connect();
		transport.close();
	}

}
