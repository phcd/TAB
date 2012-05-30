package com.archermind.txtbl.utils;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.dal.business.impl.EmailServerService;
import com.archermind.txtbl.domain.Server;
import com.archermind.txtbl.send.mail.bean.AuthUser;
import com.google.gson.Gson;
import org.jboss.logging.Logger;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.security.Security;
import java.util.Properties;

public class Mailer implements IMailer {
    private static final Logger logger = Logger.getLogger(Mailer.class);

    private Server localMailServer = null;
    private static Mailer instance = new Mailer();
    private String loginName;
    private String password;
    private String mailFrom;

    Gson gson = new Gson();

    private Mailer() {
        try {
            localMailServer = new EmailServerService().getSentServerConfig("localsmtp.com");
            if (logger.isTraceEnabled())
                logger.trace("localMailServer:" + localMailServer);

            loginName = SysConfigManager.instance().getValue("loginName");
            if (logger.isTraceEnabled())
                logger.trace("loginName:" + loginName);

            password = SysConfigManager.instance().getValue("loginPassword");
            if (logger.isTraceEnabled())
                logger.trace("password:" + password);

            mailFrom = SysConfigManager.instance().getValue("mailFrom");
            if (logger.isTraceEnabled())
                logger.trace("mailFrom:" + mailFrom);
        }
        catch (DALException e) {
            logger.error("Unabled to load local smtp config ", e);
        }
    }

    public static Mailer getInstance() {
        return instance;
    }

    public void sendMail(String from, String to, String cc, String bcc, String message, String subject) {
        if (logger.isTraceEnabled())
            logger.trace(String.format("sendMail(from=%s, to=%s, cc=%s, bcc=%s, message=%s, subject=%s)", String.valueOf(from),
                    String.valueOf(to), String.valueOf(cc), String.valueOf(bcc), String.valueOf(message), String.valueOf(subject)));

        if (localMailServer == null) {
            logger.error("Local smtp server not setup");
            return;
        }

        Session session;
        Transport transport = null;
        try {
            Properties props = new Properties();

            if (mailFrom != null && !"".equals(mailFrom.trim())) {
                props.put("mail.smtp.from", mailFrom);
            }
            props.setProperty("mail.transport.protocol", "smtp");
            props.setProperty("mail.smtp.host", localMailServer.getSendHost());
            props.setProperty("mail.smtp.port", localMailServer.getSendPort());
            if ("ssl".equals(localMailServer.getSendTs())) {
                Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
                props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.setProperty("mail.smtp.socketFactory.fallback", "false");
                props.setProperty("mail.smtp.socketFactory.port", localMailServer.getSendPort());
            }
            if (loginName != null && !"".equals(loginName.trim()) && password != null && !"".equals(password.trim())) {
                props.put("mail.smtp.auth", "true");
                session = Session.getInstance(props, new AuthUser(loginName, password));
            } else {
                props.put("mail.smtp.auth", "false");
                session = Session.getInstance(props);
            }

            if (logger.isTraceEnabled())
                logger.trace("properties:" + gson.toJson(props));

            transport = session.getTransport();
            transport.connect();
            try {
                Message msg = createMsg(to, from, cc, bcc, message, subject, session);
                transport.sendMessage(msg, msg.getAllRecipients());
            } catch (Exception e) {
                logger.error("sendMail/MailValidateImpl/Exception: " + "[" + to + "]" + e);
            }
        } catch (Exception e) {
            logger.warn("sendMail/MailValidateImpl/Exception: " + e);
        } finally {
            try {
                if (transport != null && transport.isConnected()) {
                    transport.close();
                }
            } catch (Exception e) {
                logger.warn("sendMail/MailValidateImpl/Exception: [closing transport failure]", e);
            }
        }
    }

    private static MimeMessage createMsg(String to, String from, String cc, String bcc, String message, String subject, Session session) throws Exception {

        if (logger.isTraceEnabled())
            logger.trace(String.format("createMsg(to=%s, from=%s, cc=%s, bcc=%s, message=%s, subject=%s)", String.valueOf(to),
                    String.valueOf(from), String.valueOf(cc), String.valueOf(bcc), String.valueOf(message), String.valueOf(subject)));

        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(tranFromAddr(from));
        if (to != null && !"".equals(to.trim())) {
            msg.setRecipients(Message.RecipientType.TO, UtilsTools.tranAddr(to.trim()));
        }
        if (cc != null && !"".equals(cc.trim())) {
            msg.setRecipients(Message.RecipientType.CC, UtilsTools.tranAddr(cc.trim()));
        }
        if (bcc != null && !"".equals(bcc.trim())) {
            msg.setRecipients(Message.RecipientType.BCC, UtilsTools.tranAddr(bcc.trim()));
        }
        msg.setSubject(subject);
        msg.setText(message);

        msg.saveChanges();
        return msg;
    }

    private static InternetAddress tranFromAddr(String addr) throws Exception {
        InternetAddress address = new InternetAddress();
        address.setAddress(addr.trim());
        return address;
    }

}
