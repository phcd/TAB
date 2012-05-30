package com.archermind.txtbl.validate.mailbox.inte;

import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.Server;
import com.archermind.txtbl.domain.Country;

public interface MailValidateInte {

	public abstract String validateSendConfig(Account account); //GOOD

	public abstract String validateReceiveConfig(Account account); //GOOD

	public abstract String surmiseMailboxConfig(Account account, Country country);
	
    public abstract String validateMailboxConfig(Server server, Account account);

    public abstract Server getLocalSendServer();

    //TODO - Paul - temporarily made public
    public abstract String createServersIfNecessary(Account account, Country country);
}
