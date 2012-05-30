package com.archermind.txtbl.receiver.mail.abst.impl;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.Protocol;
import com.archermind.txtbl.mail.store.ApacheS3MessageStore;
import com.archermind.txtbl.mail.store.MessageStoreException;
import com.archermind.txtbl.receiver.mail.abst.Provider;
import com.archermind.txtbl.receiver.mail.support.NewProviderSupport;
import junit.framework.Assert;
import org.junit.Test;

import javax.mail.MessagingException;

public class NewPop3ProviderTests extends AbstractProviderTest
{

    private static final String PROTOCOL = Protocol.POP3;

    private static final String HOTMAIL = "hotmail";
    private static final String GMAIL = "gmail.com";
    private static final String WEBALGORITHM = "webalgorithm.com";


    @Test
    public void testWebalgorithm() throws DALException, MessagingException, InterruptedException, MessageStoreException
    {
        Account account = getAccount(PROTOCOL, WEBALGORITHM);
        NewPOP3Provider provider = new NewPOP3Provider(new NewProviderSupport());
        testRecieveEmail(account, provider);

    }

    @Test
    public void testGmail() throws DALException, MessagingException, InterruptedException, MessageStoreException
    {
        Account account = getAccount(PROTOCOL, GMAIL);
        NewPOP3Provider provider = new NewPOP3Provider(new NewProviderSupport());
//        testRecieveEmail(account, provider);//TODO rewrite test because as message.getContent() folderDepth decreases.
        ApacheS3MessageStore messageStore = new ApacheS3MessageStore();
//        ApacheMessageStore messageStore = new ApacheMessageStore();
        //clean apache and apacheS3
        messageStore.deleteAllMessages(account.getId(), account.getCountry());

        System.out.println(account.getName());


    }

    @Test
    public void testHotmail() throws DALException, MessagingException, InterruptedException, MessageStoreException
    {
        Account account = getAccount(PROTOCOL, HOTMAIL);
        NewPOP3Provider provider = new NewPOP3Provider(new NewProviderSupport());
        testRecieveEmail(account, provider);

    }

    private void testRecieveEmail(Account account, Provider provider) throws DALException, MessagingException, InterruptedException, MessageStoreException
    {
        ApacheS3MessageStore messageStore = new ApacheS3MessageStore();
//        ApacheMessageStore messageStore = new ApacheMessageStore();
        //clean apache and apacheS3
        messageStore.deleteAllMessages(account.getId(), account.getCountry());

        System.out.println(account.getName());
        //registration
        //receives last 5 emails
        int i = provider.receiveMail(account);
        Assert.assertTrue(i > 0 && i < 6);
        // nothing receive
        Assert.assertEquals(0, provider.receiveMail(account));

        // change uid
        updateHash(account);
        messageStore.deleteAllMessages(account.getId(), account.getCountry());
        //receives all emails
        Assert.assertTrue(provider.receiveMail(account) > 0);

        Assert.assertTrue(containsDroppedEmail(account));
    }

}
