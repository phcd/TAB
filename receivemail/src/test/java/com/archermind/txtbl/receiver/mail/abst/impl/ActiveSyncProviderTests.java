package com.archermind.txtbl.receiver.mail.abst.impl;

import javax.mail.MessagingException;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.mail.store.MessageStoreException;
import com.archermind.txtbl.mail.store.ApacheS3MessageStore;
import com.archermind.txtbl.receiver.mail.support.NewProviderSupport;
import com.archermind.txtbl.receiver.mail.support.ActiveSyncSupport;
import com.archermind.txtbl.receiver.mail.abst.Provider;
import com.archermind.txtbl.validate.mailbox.abst.impl.ActiveSyncValidate;
import com.zynku.sync.activesync.control.ActiveSyncController;
import com.zynku.sync.activesync.model.Folder;
import com.zynku.sync.activesync.model.ApplicationData;
import com.zynku.sync.activesync.context.ActiveSyncContext;
import org.junit.Test;
import org.apache.log4j.BasicConfigurator;
import junit.framework.Assert;

import java.util.List;
import java.util.GregorianCalendar;
import java.net.URL;

public class ActiveSyncProviderTests extends AbstractProviderTest
{

    private static final String PROTOCOL_ACTIVE_SYNC = "activeSync";

    private static final String HOTMAIL = "hotmail";
    private static final String GMAIL = "gmail.com";

    private static final String STRYKER = "stryker.com";

    @Test
    public void testReceiveStrykerMail() throws Exception
    {
        Account account = getAccount(PROTOCOL_ACTIVE_SYNC, STRYKER);
        account.setReceiveHost("cas.stryker.com/Microsoft-Server-ActiveSync");
        account.setLoginName("jill.peektest@stryker.com");
        account.setPassword("Tester1");
        account.setReceiveTs("ssl");

        ActiveSyncProvider provider = new ActiveSyncProvider(new ActiveSyncSupport(),"iPod","Appl9C808MH40JW");

        testRecieveEmail(account, provider);

    }

    @Test
    public void testReceiveMail() throws DALException, MessagingException, InterruptedException, MessageStoreException
    {
        Account account = getAccount(PROTOCOL_ACTIVE_SYNC, STRYKER);

        account.setLoginName("k2tex1");
        account.setPassword("aircel@666");
        account.setName("K2testEx1@aircel.co.in");
        account.setReceiveHost("mail.aircel.co.in/Microsoft-Server-ActiveSync");
        account.setReceiveTs("ssl");
        ActiveSyncProvider provider = new ActiveSyncProvider(new ActiveSyncSupport(),"iPod","Appl9C808MH40JW");
        testRecieveEmail(account, provider);

    }

    @Test
    public void testReceiveMailWithMigration() throws DALException, MessagingException, InterruptedException, MessageStoreException
    {
        Account account = new Account();
        NewPOP3Provider newPop3Provider = new NewPOP3Provider(new NewProviderSupport());
        account.setLoginName("schybko@gmail.com");
        account.setPassword("vsh181175");
//        account.setReceiveHost("pop.gmail.com");
        int i = newPop3Provider.receiveMail(account);
        Assert.assertTrue(i > 0 && i < 6);
        ActiveSyncProvider activeSyncProvider = new ActiveSyncProvider(new ActiveSyncSupport(),"iPod","Appl9C808MH40JW");
        testRecieveEmail(account, activeSyncProvider);

    }

    @Test
    public void testGmail() throws DALException, MessagingException, InterruptedException, MessageStoreException
    {
        Account account = getAccount(PROTOCOL_ACTIVE_SYNC, GMAIL);
        ActiveSyncProvider provider = new ActiveSyncProvider(new ActiveSyncSupport(),"iPod","Appl9C808MH40JW");


        testRecieveEmail(account, provider);//TODO rewrite test because as message.getContent() folderDepth decreases.
        ApacheS3MessageStore messageStore = new ApacheS3MessageStore();
//        ApacheMessageStore messageStore = new ApacheMessageStore();
        //clean apache and apacheS3
        messageStore.deleteAllMessages(account.getId(), account.getCountry());

        System.out.println(account.getName());


    }

    @Test
    public void testHotmail() throws DALException, MessagingException, InterruptedException, MessageStoreException
    {
        Account account = getAccount(PROTOCOL_ACTIVE_SYNC, HOTMAIL);
        NewPOP3Provider provider = new NewPOP3Provider(new NewProviderSupport());
        testRecieveEmail(account, provider);

    }




    private void testRecieveEmail(Account account, Provider provider) throws DALException, MessagingException, InterruptedException, MessageStoreException
    {
        ApacheS3MessageStore messageStore = new ApacheS3MessageStore();
//        ApacheMessageStore messageStore = new ApacheMessageStore();
        //clean apache and apacheS3
//        messageStore.deleteAllMessages(account.getId());

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

    public void testGetCalendars() throws Exception {
            BasicConfigurator.configure();

        ActiveSyncContext activeSyncContext = new ActiveSyncContext();
        activeSyncContext.setDeviceId("Appl9C808MH40JW");
        activeSyncContext.setDeviceType("iPod");
        activeSyncContext.setUserName("air004");
        activeSyncContext.setPassword("WLT@123");
        activeSyncContext.setServerURL(new URL("https://mail.aircel.co.in/Microsoft-Server-ActiveSync"));
        ActiveSyncController controller = new ActiveSyncController(activeSyncContext, 30000);

        // this loads folder information
        controller.initialFolderSync();


        List<Folder> folders = activeSyncContext.getFolders(com.zynku.sync.activesync.model.FolderType.DEFAULT_CALENDAR);

        Assert.assertTrue(folders.size() > 0);



        for (Folder folder : folders) {
            GregorianCalendar calendar = new GregorianCalendar(2010,5,30);
            List<ApplicationData> calendars = controller.getCalendars(folder.getServerId(), calendar.getTime()); //receive all calendars  for current time
             // verify each calendar object
            for (ApplicationData _calendar : calendars) {
                _calendar.get("StartTime");
                _calendar.get("EndTime");
                _calendar.get("TimeZone");
            }
        }
    }

    /*public static void main (String [] argv) {
        String syncKeys = "Mail:DEFAULT=1277398577406";
        if (!StringUtils.isEmpty(syncKeys))
        {
            String[] folderKeys = syncKeys.split(";");
            if (syncKeys.indexOf('=') > -1) {
                for (String key : folderKeys)
                {
                    if (key.indexOf('=') > -1)
                    {
                        String[] keyValuePair = key.split("=");
                        System.out.println(keyValuePair[0] + new String[]{keyValuePair[1]});
                    }
                }
            }
        }
    } */
    @Test
    public void testValidate() throws Exception
    {
        Account account = getAccount(PROTOCOL_ACTIVE_SYNC, GMAIL);
        ActiveSyncValidate validation = new ActiveSyncValidate();
        validation.validate(account);
    }

}