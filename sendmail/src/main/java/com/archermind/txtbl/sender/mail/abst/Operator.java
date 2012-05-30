package com.archermind.txtbl.sender.mail.abst;

import com.archermind.txtbl.dal.business.impl.EmailSentService;
import com.archermind.txtbl.dal.business.impl.UserService;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.EmailPojo;
import org.jboss.logging.Logger;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.util.Date;
import java.util.List;

public abstract class Operator {

    private static final Logger log = Logger.getLogger(Operator.class);

    private static final Logger logger = Logger.getLogger("snmpTrap");


	/**
	 * @param list
	 * @return String
	 */
	public abstract String sendMail(List<EmailPojo> list);

	/**
	 * @param e
	 * @param mailbox
	 * @return boolean
	 * @see Exception Alarm
	 */
	protected boolean alarm(Exception e, String mailbox) {
		boolean alarmFlag = false;
		try {
			if (e.getMessage() != null) {
				if (mailbox.indexOf("@aol.com") != -1 && e.getMessage().indexOf("COMPLETE IMAGE PUZZLE BEFORE SENDING") != -1) {
					alarmFlag = true;
					logger.fatal("alarm/Operator/Exception: [" + mailbox + "]", e);
				} else if (e.getMessage().indexOf("Connect failed") != -1) {
					alarmFlag = true;
					logger.fatal("alarm/Operator/Exception: [" + mailbox + "]", e);
				}
			}
		} catch (Exception ex) {
			log.error("alarm/Operator/Exception: [" + mailbox + "]", ex);
		}
		return alarmFlag;
	}

	/**
	 * @param e
	 * @param mailbox
	 * @return boolean
	 */
	protected boolean exceStyle(Exception e, String mailbox) {
		boolean styleFlag = true;
		try {
			if (e.getMessage() != null) {
				if (mailbox.indexOf("@gmail.com") != -1 && e.getMessage().indexOf("Temporary system problem") != -1) {
					styleFlag = true;
				} else if (mailbox.indexOf("@gmail.com") != -1 && e.getMessage().indexOf("Username and password not accepted") != -1) {
					styleFlag = true;
				} else if (mailbox.indexOf("@gmail.com") != -1 && e.getMessage().indexOf("EOF on socket") != -1) {
					styleFlag = true;
				}
			}
		} catch (Exception ex) {
			log.error("exceStyle/Operator/Exception: [" + mailbox + "]", ex);
		}
		return styleFlag;
	}

	/**
	 * @param e
	 * @param account
	 */
	public void saveErrorMsg(Exception e, Account account) {
		if (e.getMessage() != null) {
			if (e.getMessage().indexOf("Temporary system problem") == -1 && e.getMessage().indexOf("EOF on socket") == -1 && e.getMessage().indexOf("Connect failed") == -1) {
				UserService userService = new UserService();
				try {
					userService.modifyChangeFlag(account.getUser_id(), "1");
					userService.updateAccountMessages(e.getMessage(), account.getName(), "1");
				} catch (Exception e1) {
					log.error("saveErrorMsg/Operator/Exception: [" + account.getName() + "]", e1);
				}
			}
		}
	}

	/**
	 * @param mailId
	 */
	protected void updateMailFlag(int mailId) {
		try {
			new EmailSentService().updateStatus("1", new int[] { mailId });
		} catch (Exception e) {
			log.error("updateMailFlag/Operator/Exception: [" + mailId + "]", e);
		}
	}

	/**
	 * @param bean
	 * @param session
	 * @return MimeMessage
	 * @throws Exception
	 */
	protected MimeMessage createMsg(EmailPojo bean, Session session) throws Exception {
		MimeMessage msg = new MimeMessage(session);
		/** Set the FROM,TO,CC,BCC and subject fields */
		msg.setFrom(tranFromAddr(bean));
		msg.setSentDate(new Date());
		if (bean.getEmail().getTo() != null && !"".equals(bean.getEmail().getTo().trim())) {
			msg.setRecipients(Message.RecipientType.TO, tranAddr(bean.getEmail().getTo().trim()));
		}
		if (bean.getEmail().getCc() != null && !"".equals(bean.getEmail().getCc().trim())) {
			msg.setRecipients(Message.RecipientType.CC, tranAddr(bean.getEmail().getCc().trim()));
		}
		if (bean.getEmail().getBcc() != null && !"".equals(bean.getEmail().getBcc().trim())) {
			msg.setRecipients(Message.RecipientType.BCC, tranAddr(bean.getEmail().getBcc().trim()));
		}
		if (bean.getEmail().getReply() != null && !"".equals(bean.getEmail().getReply().trim())) {
			msg.setReplyTo(tranAddr(bean.getEmail().getReply().trim()));
		}
		String subject = bean.getEmail().getSubject();
        //log.debug("Sending email " + bean.getEmail().getId() + " with subject: " + subject);
		if (subject != null) {
			if (subject.length() > 250) {
                log.warn("Subject length > 250 characters, subject will be lost: " + bean.getEmail().getId()
                        + " with length: " + subject.length() + " with subject: " + subject);
				subject = subject.substring(0, 245) + "...";
			}
		}
		msg.setSubject(subject, "UTF-8");
        //log.debug("Checking MIME msg subject for email " + bean.getEmail().getId() + " with subject: "+ msg.getSubject());
		if (bean.getAttachement() == null || bean.getAttachement().size() < 1) {
			msg = createMsg(msg, bean);
		} else {
			msg = createMsgAttach(msg, bean);
		}
		msg.saveChanges();
        log.debug("Checking MIME msg subject AFTER msg.saveChange for email " + bean.getEmail().getId() + " with subject: "+ msg.getSubject());
		return msg;
	}

	protected MimeMessage createMsg(MimeMessage msg, EmailPojo bean) throws Exception {

        logger.info(String.format("regular create msg is called for %s", bean.getEmail().getId()));

		/** send text type */
		if ("0".equals(bean.getEmail().getDataType().trim())) {
			msg.setContent(transformData(bean, "plain"), "text/plain;charset=utf-8");
			/** send html type */
		} else if ("1".equals(bean.getEmail().getDataType().trim())) {
			msg.setContent(transformData(bean, "html"), "text/html;charset=utf-8");
		} else {
			/** send alternative type */
			MimeMultipart multipart = new MimeMultipart("alternative");
			MimeBodyPart textBodyPart = new MimeBodyPart();
			textBodyPart.setContent(transformData(bean, "plain"), "text/plain;charset=utf-8");
			MimeBodyPart htmlBodyPart = new MimeBodyPart();
			htmlBodyPart.setContent(transformData(bean, "html"), "text/html;charset=utf-8");
			multipart.addBodyPart(textBodyPart);
			multipart.addBodyPart(htmlBodyPart);
			msg.setContent(multipart);
		}
		return msg;
	}

	protected MimeMessage createMsgAttach(MimeMessage msg, EmailPojo bean) throws Exception {
		MimeMultipart allMultipart = new MimeMultipart("mixed");
		/** send text type */
		if ("0".equals(bean.getEmail().getDataType().trim())) {
			MimeBodyPart textBodyPart = new MimeBodyPart();
			textBodyPart.setContent(transformData(bean, "plain"), "text/plain;charset=utf-8");
			allMultipart.addBodyPart(textBodyPart);
		} else {
			/** send html type */
			if ("1".equals(bean.getEmail().getDataType().trim())) {
				MimeBodyPart htmlBodyPart = new MimeBodyPart();
				htmlBodyPart.setContent(transformData(bean, "html"), "text/html;charset=utf-8");
				allMultipart.addBodyPart(htmlBodyPart);
			} else {
				/** send alternative type */
				MimeBodyPart contentPart = new MimeBodyPart();
				MimeMultipart contentMultipart = new MimeMultipart("alternative");
				MimeBodyPart textBodyPart = new MimeBodyPart();
				textBodyPart.setContent(transformData(bean, "plain"), "text/plain;charset=utf-8");
				MimeBodyPart htmlBodyPart = new MimeBodyPart();
				htmlBodyPart.setContent(transformData(bean, "html"), "text/html;charset=utf-8");
				contentMultipart.addBodyPart(textBodyPart);
				contentMultipart.addBodyPart(htmlBodyPart);
				contentPart.setContent(contentMultipart);
				allMultipart.addBodyPart(contentPart);
			}
		}

		if (bean.getAttachement() != null && bean.getAttachement().size() > 0) {
			for (int i = 0; i < bean.getAttachement().size(); i++) {
				byte[] attach = bean.getAttachement().get(i).getData();
				if (attach == null) {
					attach = "".getBytes("UTF-8");
				}
				ByteArrayDataSource byteArrayDataSource = new ByteArrayDataSource(attach, "application/octet-stream");
				MimeBodyPart attachPart = new MimeBodyPart();
				attachPart.setDataHandler(new DataHandler(byteArrayDataSource));
				attachPart.setFileName(bean.getAttachement().get(i).getName());
				allMultipart.addBodyPart(attachPart);
			}
		}

		msg.setContent(allMultipart);
		return msg;
	}
    
	private InternetAddress[] tranAddr(String addr) throws Exception {
		String[] tempAddr = addr.trim().replaceAll("\\s+", ";").replaceAll(",+", ";").split(";+");
		InternetAddress address[] = new InternetAddress[tempAddr.length];
		for (int i = 0; i < tempAddr.length; i++) {
			address[i] = new InternetAddress();
			address[i].setAddress(tempAddr[i].trim());
		}
		return address;
	}

	protected InternetAddress tranFromAddr(EmailPojo bean) throws Exception {
		InternetAddress address = new InternetAddress();
		address.setAddress(bean.getEmail().getFrom().trim());
		if (bean.getAccount().getAlias_name() != null && !"".equals(bean.getAccount().getAlias_name().trim())) {
			bean.getAccount().setAlias_name(bean.getAccount().getAlias_name().replaceAll(";", " "));
			address.setPersonal(bean.getAccount().getAlias_name().trim());
		}
		return address;
	}

	protected String transformData(EmailPojo bean, String MIMEType) throws Exception {
		String bodyStr = new String(bean.getBody().getData(), "UTF-8");
		if (!"plain".equals(MIMEType)) {
			bodyStr = bodyStr.replaceAll("\r\n", "<br/>");
			bodyStr = bodyStr.replaceAll("\n", "<br/>");
			bodyStr = bodyStr.replaceAll("\r", "<br/>");
			bodyStr = bodyStr.replaceAll(" ", "&nbsp;");
		}
		return bodyStr;
	}

    protected String transformData(EmailPojo bean) throws Exception {
		return transformData(bean, "plain");
	}
}
