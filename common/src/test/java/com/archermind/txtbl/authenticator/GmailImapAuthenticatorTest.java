package com.archermind.txtbl.authenticator;

import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.utils.StopWatch;
import org.junit.Assert;
import org.junit.Test;

import javax.mail.Folder;

public class GmailImapAuthenticatorTest {
    @Test
    public void gmailConnect() throws Exception {
        Account account = new Account();
        account.setName("dan@getpeek.com");
        account.setLoginName("dan@getpeek.com");
        account.setPassword("txtbl123");
        account.setReceiveHost("imap.gmail.com");
        Folder folder = new ImapSSLAuthenticator().connect(account, null, new StopWatch(), new DoNothingLoginFailureHandler(), account.getFolderNameToConnect());
        Assert.assertNotNull(folder);
    }
    
}
