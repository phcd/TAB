package com.archermind.txtbl.validate.mailbox.abst.impl;

import com.archermind.txtbl.authenticator.Authenticator;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.validate.mailbox.abst.FolderProvider;
import com.archermind.txtbl.validate.mailbox.abst.Validate;

public class YahooIMAP4Validate extends Validate {
    private Authenticator authenticator;
    private FolderProvider folderProvider;

    public YahooIMAP4Validate(Authenticator authenticator, FolderProvider folderProvider) {
        this.authenticator = authenticator;
        this.folderProvider = folderProvider;
    }

	public void validate(Account account) throws Exception {
        validate(account, authenticator, folderProvider);
	}
}
