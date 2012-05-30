package com.archermind.txtbl.taskfactory;

import javax.jms.Message;

import com.archermind.txtbl.domain.Account;

public interface TaskFactoryEngine {
	public void start();
	
	public ReceiveNoticer updateAccount(Account account);
}
