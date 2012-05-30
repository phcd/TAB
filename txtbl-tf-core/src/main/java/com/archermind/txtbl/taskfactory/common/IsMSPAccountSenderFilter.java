package com.archermind.txtbl.taskfactory.common;

import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.taskfactory.msp.IsMSPAccount;
import com.archermind.txtbl.taskfactory.msp.IsMSPAccountIMP;
import org.jboss.logging.Logger;

public class IsMSPAccountSenderFilter implements AccountSender {
	private static Logger logger = Logger
			.getLogger(IsMSPAccountSenderFilter.class);
	private AccountSender accountSender = null;
	private IsMSPAccount isMSPAccount = new IsMSPAccountIMP(FactoryTools
			.getSubscribeArray());

	public IsMSPAccountSenderFilter(AccountSender accountSender) {
		this.accountSender = accountSender;
	}

	public void SendAccount(Account account)
    {
		if (isMSPAccount(account)) {
			accountSender.SendAccount(account);
		} else {
			logger.info("the account[" + account.getName()
					+ "] is not belong to MSP");
		}
	}

	private boolean isMSPAccount(Account account) {
		return isMSPAccount.isMSP(account);
	}

}
