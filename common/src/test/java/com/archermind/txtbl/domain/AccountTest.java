package com.archermind.txtbl.domain;

import org.junit.Test;
import org.junit.Assert;

public class AccountTest {
    @Test
    public void advanceConfigSameAsRecvServerConfig(){
        Server server = new Server();
        server.setReceiveHost(null);
        Account account = new Account();
        Assert.assertTrue(account.advanceConfigSameAsRecvServerConfig(server));
    }

    @Test
    public void getLoginFailureNotificationMessage(){
        Account account = new Account();
        account.setName("paul@getpeek.com");
        account.setOauthToken("oauthtoken");
        account.setOauthTokenSecret("oauthtokenSecret");
        account.setReceiveProtocolType(Protocol.XOBNI_OAUTH_IDLE);
        Assert.assertEquals("Credentials invalid for paul@getpeek.com\n" + "Oauth token : oauthtoken\n" + "Oauth secret token: oauthtokenSecret\n" + "consumer key: \n" + "consumer secret: \n", account.getLoginFailureNotificationMessage());
        account.setReceiveProtocolType(Protocol.XOBNI_IMAP);
        Assert.assertEquals("Credentials invalid for paul@getpeek.com\n", account.getLoginFailureNotificationMessage());
    }

    @Test
    public void getFoldersToValidateAgainst() {
        Account account = new Account();
        account.setReceiveProtocolType(Protocol.XOBNI_IMAP_IDLE);
        Assert.assertEquals(3, account.getFoldersToValidateAgainst().length);
        account.setReceiveProtocolType(Protocol.XOBNI_OAUTH_IDLE);
        Assert.assertEquals(3, account.getFoldersToValidateAgainst().length);
        account.setReceiveProtocolType(Protocol.IMAP_IDLE);
        Assert.assertEquals(1, account.getFoldersToValidateAgainst().length);
    }
}
