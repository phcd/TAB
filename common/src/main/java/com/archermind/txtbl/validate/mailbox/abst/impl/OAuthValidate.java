package com.archermind.txtbl.validate.mailbox.abst.impl;

import com.archermind.txtbl.authenticator.Authenticator;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.validate.mailbox.abst.FolderProvider;
import com.archermind.txtbl.validate.mailbox.abst.Validate;

public class OAuthValidate extends Validate {
    private Authenticator authenticator;
    private FolderProvider folderProvider;

    public OAuthValidate(Authenticator authenticator, FolderProvider folderProvider) {
        this.authenticator = authenticator;
        this.folderProvider = folderProvider;
    }

    public void validate(Account account) throws Exception {
        validate(account, authenticator, folderProvider);
    }
}
