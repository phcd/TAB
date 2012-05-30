package com.archermind.txtbl.sender.mail.abst.impl;

import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.EmailPojo;
import com.archermind.txtbl.send.mail.bean.AuthUser;
import com.archermind.txtbl.sender.mail.abst.Operator;
import org.jboss.logging.Logger;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import java.security.Security;
import java.util.List;
import java.util.Properties;

public class SMTPOperator extends Operator {

    private static final Logger log = Logger.getLogger(SMTPOperator.class);

    /**
     * @param list
     */
    public String sendMail(List<EmailPojo> list) {

        Transport transport = null;

        Account account = null;
        String failrueId = null;
        try {
            Properties props = new Properties();
            account = list.get(0).getAccount();
            props.setProperty("mail.transport.protocol", "smtp");
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
            } else if ("tls".equals(account.getSendTs())) {
                props.setProperty("mail.imap.starttls.enable", "true");
                java.security.Security.setProperty("ssl.SocketFactory.provider", "com.archermind.txtbl.mail.DummySSLSocketFactory");
            }
            Session session = "1".equals(account.getNeedAuth()) ? Session.getInstance(props, new AuthUser(account.getLoginName(), account.getPassword())) : Session.getInstance(props);
            // session.setDebug(true);
            transport = session.getTransport();
            transport.connect();
            // transport.connect(account.getSendHost(), account.getLoginName(),
            // account.getPassword());
            for (EmailPojo emailPojo : list) {
                try {
                    /* create a new message */
                    Message msg = createMsg(emailPojo, session);
                    transport.sendMessage(msg, msg.getAllRecipients());
                    log.info("[" + account.getName() + "] [sending success] [" + emailPojo.getEmail().getMailid() + "]");
                    updateMailFlag(emailPojo.getEmail().getMailid());
                } catch (Exception e) {
                    if (e.getMessage() != null) {
                        if (account.getName().indexOf("@aol.com") != -1 && e.getMessage().indexOf("COMPLETE IMAGE PUZZLE BEFORE SENDING") != -1) {
                            saveErrorMsg(e, account);
                        }
                    }
                    if (alarm(e, account.getName())) {
                        log.fatal("sendMail/SMTPOperator/Exception: " + "[" + account.getName() + "]", e);
                    } else if (exceStyle(e, account.getName())) {
                        log.fatal("sendMail/SMTPOperator/Exception: " + "[" + account.getName() + "]", e);
                    } else {
                        log.fatal("sendMail/SMTPOperator/Exception: " + "[" + account.getName() + "]", e);
                    }
                    failrueId = failrueId == null ? String.valueOf(emailPojo.getEmail().getMailid()) : failrueId + "," + emailPojo.getEmail().getMailid();
                }
            }
        } catch (Exception e) {
            saveErrorMsg(e, account);
            if (alarm(e, account.getName())) {
                log.fatal("sendMail/SMTPOperator/Exception: " + "[" + account.getName() + "] [login failure]" + e, e);
            } else if (exceStyle(e, account.getName())) {
                log.fatal("sendMail/SMTPOperator/Exception: " + "[" + account.getName() + "] [login failure]" + e, e);
            } else {
                log.fatal("sendMail/SMTPOperator/Exception: " + "[" + account.getName() + "] [login failure]", e);
            }
            try {
                for (EmailPojo emailPojo : list) {
                    failrueId = failrueId == null ? String.valueOf(emailPojo.getEmail().getMailid()) : failrueId + "," + emailPojo.getEmail().getMailid();
                }
            } catch (Exception ex) {
                log.fatal("sendMail/SMTPOperator/Exception: " + "[" + account.getName() + "] [losing mail]", e);
            }
        } finally {
            try {
                if (transport != null && transport.isConnected()) {
                    transport.close();
                }
            } catch (Exception e) {
                log.fatal("sendMail/SMTPOperator/Exception: [" + account.getName() + "]" + " [closing transport failure]", e);
            }
        }
        return failrueId;
    }
}
