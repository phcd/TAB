package com.archermind.txtbl.sender.mail.dal;

import EDU.oswego.cs.dl.util.concurrent.TimeoutException;
import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.dal.business.impl.EmailRecievedService;
import com.archermind.txtbl.dal.business.impl.UserService;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.EmailPojo;
import com.archermind.txtbl.domain.OriginalReceivedAttachment;
import com.archermind.txtbl.domain.OriginalReceivedEmail;
import org.jboss.logging.Logger;
import twitter4j.http.AccessToken;

import java.util.ArrayList;

public class DALDominator {

    private static final Logger log = Logger.getLogger(DALDominator.class);

	/**
	 * @param emailPojo
	 * @return boolean
	 * @throws DALException
	 * @throws TimeoutException
	 */
	public static boolean notifierClient(EmailPojo emailPojo) throws DALException {
		boolean succFlag = false;
		OriginalReceivedEmail original = new OriginalReceivedEmail();
		original.setAttachList(new ArrayList<OriginalReceivedAttachment>());
		if (new EmailRecievedService().saveEmail(emailPojo, original) != 0) {
			succFlag = true;
		} else {
			log.error("notifierClient/DALDominator/Exception: [" + emailPojo.getEmail().getMailid() + "]" + " [notifier failure]");
		}
		return succFlag;
	}

    public static AccessToken fetchTwitterToken(Account account) {
        return new UserService().fetchTwitterToken(account.getName());

    }
}
