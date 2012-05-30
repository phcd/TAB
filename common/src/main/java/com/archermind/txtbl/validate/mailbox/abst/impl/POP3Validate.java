package com.archermind.txtbl.validate.mailbox.abst.impl;

import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.validate.mailbox.abst.Validate;

import javax.mail.Session;
import javax.mail.Store;
import java.security.Security;
import java.util.Properties;

public class POP3Validate extends Validate {

	/**
	 * @param account
	 * @see validate mailbox config
	 */
	public void validate(Account account) throws Exception {
		Properties props = new Properties();
		props.setProperty("mail.pop3.port", account.getReceivePort());
		if ("ssl".equals(account.getReceiveTs())) {
			Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
            props.put("mail.pop3.socketFactory.class", "com.archermind.txtbl.mail.DummySSLSocketFactory");
			props.setProperty("mail.pop3.socketFactory.fallback", "false");
			props.setProperty("mail.pop3.socketFactory.port", account.getReceivePort());
		}else if ("tls".equals(account.getReceiveTs())) {
			props.setProperty("mail.pop3.starttls.enable", "true");
			java.security.Security.setProperty("ssl.SocketFactory.provider", "com.archermind.txtbl.mail.DummySSLSocketFactory");
		}
		props.setProperty("mail.pop3.connectiontimeout", "60000");
		Session session = Session.getInstance(props);
		Store store = session.getStore("pop3");
		store.connect(account.getReceiveHost(), account.getLoginName(), account.getPassword());
		store.close();
	}

    public static void main(String[] args) throws Exception {
        Account account = new Account();
        account.setReceiveHost("mail.12daysofpeekmas.com");
        account.setReceivePort("110");
        account.setName("amol@12daysofpeekmas.com");
        account.setLoginName("amol@12daysofpeekmas.com");
        account.setPassword("mailster");
        account.setReceiveTs(null);
        new POP3Validate().validate(account);
    }
}
