package com.archermind.txtbl.sender.mail.relation;

import java.util.ArrayList;

import com.archermind.txtbl.dal.business.IEMailSentService;
import com.archermind.txtbl.dal.business.impl.EmailSentService;
import com.archermind.txtbl.domain.Attachment;
import com.archermind.txtbl.domain.Body;
import com.archermind.txtbl.domain.Email;
import com.archermind.txtbl.domain.EmailPojo;
import com.archermind.txtbl.sender.mail.config.SenderConfig;
import com.archermind.txtbl.sender.mail.dal.DALDominator;
import com.archermind.txtbl.utils.UtilsTools;
import org.jboss.logging.Logger;

public class FailureNotifier {

    private static final Logger log = Logger.getLogger(FailureNotifier.class);

	public static boolean notifier(String mailID) {
		try {
			IEMailSentService emailSentService = new EmailSentService();
			for (String tempID : mailID.split(",")) {
				try {
					EmailPojo saveEmail = new EmailPojo();
					EmailPojo originalEmail = emailSentService.getEmailPojo(tempID);
					Email email = new Email();
					email.setMaildate(null);
					String loginName = SenderConfig.getProp("loginName");
					email.setFrom(loginName);
					if (loginName.indexOf("@") != -1) {
						email.setFrom_alias(UtilsTools.stripDomainName(loginName));
					} else {
						email.setFrom_alias(loginName);
					}
					email.setTo(originalEmail.getAccount().getName());
					email.setSubject("Non-delivery notification");
					email.setUserId(originalEmail.getAccount().getUser_id());
					email.setOriginal_account(originalEmail.getAccount().getName());
					email.setStatus("0");
					String address = "";
					if (originalEmail.getEmail().getTo() != null) {
						address += originalEmail.getEmail().getTo() + "\n";
					}
					if (originalEmail.getEmail().getCc() != null) {
						address += originalEmail.getEmail().getCc() + "\n";
					}
					if (originalEmail.getEmail().getBcc() != null) {
						address += originalEmail.getEmail().getBcc();
					}
					Body body = new Body();
					String content = "Your message: \nSubject: " + originalEmail.getEmail().getSubject() + "\nSent: " + originalEmail.getEmail().getSentTime()
							+ "did not reach the following recipient(s):\n" + address;
					body.setData(content.getBytes("UTF-8"));
					email.setBodySize(body.getData().length);
					saveEmail.setEmail(email);
					saveEmail.setBody(body);
					saveEmail.setAttachement(new ArrayList<Attachment>());
					DALDominator.notifierClient(saveEmail);
				} catch (Exception e) {
					log.error("notifier/FailureNotifier/Exception: [" + tempID + "]" + " [notifier failure]");
				}
			}
			return true;
		} catch (Exception e) {
			log.error("notifier/FailureNotifier/Exception: [" + mailID + "]" + " [notifier failure]");
		}
		return false;
	}

	public static void main(String[] args) {
		notifier("5800");
		System.out.println("SUCCESS");
	}
}
