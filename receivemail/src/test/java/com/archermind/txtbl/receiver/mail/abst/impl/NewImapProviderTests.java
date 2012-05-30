package com.archermind.txtbl.receiver.mail.abst.impl;

import com.archermind.txtbl.authenticator.Authenticator;
import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.Protocol;
import com.archermind.txtbl.mail.store.ApacheS3MessageStore;
import com.archermind.txtbl.mail.store.MessageStoreException;
import com.archermind.txtbl.receiver.mail.abst.Provider;
import com.archermind.txtbl.receiver.mail.support.NewProviderSupport;
import com.archermind.txtbl.utils.StopWatch;
import junit.framework.Assert;
import org.junit.Test;

import javax.mail.MessagingException;
import javax.mail.Store;

public class NewImapProviderTests extends AbstractProviderTest {

    public static final String PROTOCOL = Protocol.IMAP;
    private static final String GMAIL = "gmail.com";
    private static final String WEBALGORITHM = "webalgorithm.com";
    private static final String YAHOO = "yahoo.com";
    private static final String AOL = "aol.com";

    @Test
    public void testWebalgorithm() throws DALException, MessagingException, InterruptedException, MessageStoreException {
        Account account = getAccount(PROTOCOL, WEBALGORITHM);
        NewImapProvider provider = new NewImapProvider(new NewProviderSupport(), getAuthenticator());
        testRecieveEmail(account, provider);
    }

    private Authenticator getAuthenticator() {
        return new Authenticator() {
            @Override
            public Store getStore(Account account, String context, StopWatch watch) throws Exception {
                return null;
            }
        };
    }

    @Test
    public void testGmail() throws DALException, MessagingException, InterruptedException, MessageStoreException {
        Account account = getAccount(PROTOCOL, GMAIL);
        NewImapProvider provider = new NewImapProvider(new NewProviderSupport(), getAuthenticator());
        testRecieveEmail(account, provider);
    }

    @Test
    public void testYahoo() throws DALException, MessagingException, InterruptedException, MessageStoreException {
        Account account = getAccount(PROTOCOL, YAHOO);
        NewImapProvider provider = new NewImapProvider(new NewProviderSupport(), getAuthenticator());
        testRecieveEmail(account, provider);
    }

    @Test
    public void testAol() throws DALException, MessagingException, InterruptedException, MessageStoreException {
        Account account = getAccount(PROTOCOL, AOL);
        NewImapProvider provider = new NewImapProvider(new NewProviderSupport(), getAuthenticator());
        testRecieveEmail(account, provider);
    }


    private void testRecieveEmail(Account account, Provider provider) throws DALException, MessagingException, InterruptedException, MessageStoreException {
        ApacheS3MessageStore messageStore = new ApacheS3MessageStore();
        //clean apache and apacheS3
        messageStore.deleteAllMessages(account.getId(), account.getCountry());

        System.out.println(account.getName());
        //registration
        //receives last 5 emails
        int i = provider.receiveMail(account);
        Assert.assertTrue(i > 0 && i < 6);
        // nothing receive
        Assert.assertEquals(0, provider.receiveMail(account));

        messageStore.deleteAllMessages(account.getId(), account.getCountry());
        //receives all emails
        Assert.assertTrue(provider.receiveMail(account) > 0);
//        if (!account.getLoginName().contains(AOL))
//        {
        Assert.assertTrue(containsDroppedEmail(account));
//        }

    }


}
