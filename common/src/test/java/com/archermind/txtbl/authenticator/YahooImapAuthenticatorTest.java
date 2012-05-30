package com.archermind.txtbl.authenticator;

import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.utils.StopWatch;
import org.junit.Assert;
import org.junit.Test;

import javax.mail.Folder;

public class YahooImapAuthenticatorTest {
    @Test
    public void yahooConnect() throws Exception {
        Account account = new Account();
        account.setName("peektestabc@yahoo.com");
        account.setLoginName("peektestabc@yahoo.com");
        account.setPassword("mailster");
        account.setReceiveHost("imap.n.mail.yahoo.com,imap.mail.yahoo.com,imap.next.mail.yahoo.com");
        account.setReceivePort("143");
        Folder folder = new YahooImapAuthenticator().connect(account, null, new StopWatch(), new DoNothingLoginFailureHandler(), account.getFolderNameToConnect());
        Assert.assertNotNull(folder);
    }
    
}
