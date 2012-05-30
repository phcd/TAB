package com.archermind.txtbl.taskfactory.subscribe;

import com.archermind.txtbl.domain.Account;

public interface Subscribe {
	void receivedMail(Account account);
}
