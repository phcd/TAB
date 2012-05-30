package com.archermind.txtbl.sender.mail.abst.impl;

import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.Email;
import com.archermind.txtbl.domain.EmailPojo;
import com.archermind.txtbl.sender.mail.abst.Operator;
import com.archermind.txtbl.utils.SysConfigManager;
import com.techventus.server.voice.Voice;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.util.List;

public class GoogleVoiceOperator extends Operator {

    private static final Logger log = Logger.getLogger(GoogleVoiceOperator.class);


    private static String pokeApiKey;
    private static String googleVoiceFailedFrom;
    private static String googleVoiceFailedFromAlias;
    private static String googleVoiceFailedText;

    static {
        pokeApiKey = SysConfigManager.instance().getValue("pokeApiKey", "");
        googleVoiceFailedFrom = SysConfigManager.instance().getValue("googleVoiceFailedFrom", "feedback@getpeek.com");
        googleVoiceFailedFromAlias = SysConfigManager.instance().getValue("googleVoiceFailedFromAlias", "Peek");
        googleVoiceFailedText = SysConfigManager.instance().getValue("googleVoiceFailedText", "We were unable to deliver your SMS via GoogleVoice. Please ensure that the number is valid and correct.");
    }

	/**
	 * @param list
	 */
	public String sendMail(List<EmailPojo> list) {

		Account account = null;
		String failureId = null;

		try {
            account = list.get(0).getAccount();

            Voice voice = new Voice(account.getLoginName() + "@gmail.com",account.getPassword());

            boolean success = true;

			for (EmailPojo emailPojo : list) {
				try {

                    doSend(voice, emailPojo);

					log.info("[" + account.getName() + "] [sending success] [" + emailPojo.getEmail().getMailid() + "]");
					updateMailFlag(emailPojo.getEmail().getMailid());
				} catch (Exception e) {
                    success = false;

                    Email email = emailPojo.getEmail();
					failureId = failureId == null ? String.valueOf(email.getMailid()) : failureId + "," + email.getMailid();
                    log.error("[" + account.getName() + "] [sending failed] [" + failureId + "] " + e.getMessage());
				} finally {
                    
                }
			}
		} catch (Exception e) {
            log.error("[" + account.getName() + "] [sending failed - google voice authentication error] " + e.getMessage());
            for (EmailPojo emailPojo : list) {
                Email email = emailPojo.getEmail();
                failureId = failureId == null ? String.valueOf(email.getMailid()) : failureId + "," + email.getMailid();
            }
		}

		return failureId;
	}

    private void doSend(Voice voice, EmailPojo emailPojo) throws IOException {
        String mailType = emailPojo.getEmail().getEmail_type().toUpperCase();

        StringBuffer sb = new StringBuffer();
        sb.append(emailPojo.getEmail().getSubject() + "\n");
        sb.append(new String(emailPojo.getBody().getData()));

        voice.sendSMS(emailPojo.getEmail().getTo(),sb.toString());


    }




    
}