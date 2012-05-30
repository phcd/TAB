package com.archermind.txtbl.validate.mailbox.abst.impl;

import com.archermind.txtbl.authenticator.Authenticator;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.validate.mailbox.abst.FolderProvider;
import com.archermind.txtbl.validate.mailbox.abst.Validate;

public class NewImapValidate extends Validate {

    private Authenticator authenticator;
    private FolderProvider folderProvider;

    public NewImapValidate(Authenticator authenticator, FolderProvider folderProvider) {
        this.authenticator = authenticator;
        this.folderProvider = folderProvider;
    }

    public void validate(Account account) throws Exception {
        validate(account, authenticator, folderProvider);
    }

    public static void main(String[] args) throws Exception {
        Account account = new Account();
        account.setReceiveHost("imap.next.mail.yahoo.com");
        account.setReceivePort("143");
        account.setName("peektestabc@yahoo.com");
        account.setLoginName("peektestabc@yahoo.com");
        account.setPassword("mailster");
        account.setReceiveTs("");
        new NewImapValidate(null, null).validate(account);
    }

}