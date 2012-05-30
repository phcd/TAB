package com.archermind.txtbl.sender.mail.abst.impl;

import java.util.List;
import java.util.Date;

import javax.mail.internet.InternetAddress;

import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.EmailPojo;
import com.archermind.txtbl.domain.Attachment;
import com.archermind.txtbl.sender.mail.abst.Operator;
import com.archermind.txtbl.utils.MSExchangeUtil;
import com.webalgorithm.exchange.ConnectionPick;
import com.webalgorithm.exchange.ExchangeClient;
import org.jboss.logging.Logger;


public class MSExchangeOperator extends Operator {


    private static final Logger log = Logger.getLogger(MSExchangeOperator.class);

	@Override
	public String sendMail(List<EmailPojo> list) {

		Account account = null;
		String failrueId = null;
		ConnectionPick cp = null;
		
		try {
			account = list.get(0).getAccount();

            cp = MSExchangeUtil.pickConnection(account.getSendHost(), account.getSendPort(), account.getSendTs(), account.getName(), account.getLoginName(), account.getPassword(), account.getSendHostPrefix(), account.getSendHostFbaPath());
            
            ExchangeClient exchangeClient = cp.getExchangeClient();

			for (EmailPojo emailPojo : list) {
				try {

					exchangeClient.sendMessage(createExchangeMessage(emailPojo));
					
					log.info("[" + account.getName() + "] [sending success] [" + emailPojo.getEmail().getMailid() + "]");
					updateMailFlag(emailPojo.getEmail().getMailid());
				} catch (Exception e) {
					if (e.getMessage() != null) {
						if (account.getName().indexOf("@aol.com") != -1 && e.getMessage().indexOf("COMPLETE IMAGE PUZZLE BEFORE SENDING") != -1) {
							saveErrorMsg(e,account);
						} 
					}
					if (alarm(e, account.getName())) {
						log.error("sendMail/MSExchangeOperator/Exception: " + "[" + account.getName() + "]" , e);
					} else if (exceStyle(e, account.getName())) {
						log.error("sendMail/MSExchangeOperator/Exception: " + "[" + account.getName() + "]" , e);
					} else {
						log.error("sendMail/MSExchangeOperator/Exception: " + "[" + account.getName() + "]", e);
					}
					failrueId = failrueId == null ? String.valueOf(emailPojo.getEmail().getMailid()) : failrueId + "," + emailPojo.getEmail().getMailid();
				}
			}
		} catch (Exception e) {
			saveErrorMsg(e,account);
			if (alarm(e, account.getName())) {
				log.error("sendMail/MSExchangeOperator/Exception: " + "[" + account.getName() + "] [login failure]" + e);
			} else if (exceStyle(e, account.getName())) {
				log.error("sendMail/MSExchangeOperator/Exception: " + "[" + account.getName() + "] [login failure]" + e);
			} else {
				log.error("sendMail/MSExchangeOperator/Exception: " + "[" + account.getName() + "] [login failure]", e);
			}
			try {
				for (EmailPojo emailPojo : list) {
					failrueId = failrueId == null ? String.valueOf(emailPojo.getEmail().getMailid()) : failrueId + "," + emailPojo.getEmail().getMailid();
				}
			} catch (Exception ex) {
				log.error("sendMail/MSExchangeOperator/Exception: " + "[" + account.getName() + "] [losing mail]", e);
			}
		} finally {
			if (cp != null) {
				cp.getExchangeClient().close();
			}
		}
		return failrueId;
	}

    private com.webalgorithm.exchange.dto.Message createExchangeMessage(EmailPojo emailPojo) throws Exception
    {

        com.webalgorithm.exchange.dto.Message exchangeMessage = new com.webalgorithm.exchange.dto.Message();

        InternetAddress fromAddr = tranFromAddr(emailPojo);
        exchangeMessage.setFromEmail(fromAddr.getAddress());
        exchangeMessage.setFromName(fromAddr.getPersonal());

        exchangeMessage.setSentDate(new Date());

        String to = emailPojo.getEmail().getTo();
        if (to != null && !"".equals(to.trim()))
        {
            exchangeMessage.setTo(to.trim());
        }
        String cc = emailPojo.getEmail().getCc();
        if (cc != null && !"".equals(cc.trim()))
        {
            exchangeMessage.setCc(cc);
        }

        String bcc = emailPojo.getEmail().getBcc();
        if (bcc != null && !"".equals(bcc.trim()))
        {
            exchangeMessage.setBcc(bcc);
        }

        String subject = emailPojo.getEmail().getSubject();
        if (subject != null)
        {
            if (subject.length() > 250)
            {
                subject = subject.substring(0, 245) + "...";
            }
        }
        exchangeMessage.setSubject(subject);

        exchangeMessage.setBody(transformData(emailPojo));

        if (emailPojo.getAttachement() != null && emailPojo.getAttachement().size() > 0)
        {
            for (Attachment attachment : emailPojo.getAttachement())
            {
                com.webalgorithm.exchange.dto.Attachment exchangeAttachment = new com.webalgorithm.exchange.dto.Attachment();
                exchangeAttachment.setData(attachment.getData());
                exchangeAttachment.setFileName(attachment.getName());

                exchangeMessage.addAttachment(exchangeAttachment);
            }
        }

        return exchangeMessage;
    }

}
