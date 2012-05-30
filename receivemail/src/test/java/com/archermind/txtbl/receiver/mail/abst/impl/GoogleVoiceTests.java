package com.archermind.txtbl.receiver.mail.abst.impl;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.Protocol;
import com.archermind.txtbl.mail.store.ApacheS3MessageStore;
import com.archermind.txtbl.mail.store.MessageStoreException;
import com.archermind.txtbl.receiver.mail.abst.Provider;
import com.archermind.txtbl.receiver.mail.bean.GoogleVoiceProcess;
import com.archermind.txtbl.receiver.mail.bean.GoogleVoiceProcessMap;
import com.archermind.txtbl.receiver.mail.support.GoogleVoiceFolder;
import com.archermind.txtbl.receiver.mail.support.GoogleVoiceMessage;
import com.archermind.txtbl.receiver.mail.support.GoogleVoiceProviderSupport;
import com.archermind.txtbl.utils.StopWatch;
import com.techventus.server.voice.Voice;
import junit.framework.Assert;
import org.junit.Test;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import java.io.IOException;
import java.util.Properties;

public class GoogleVoiceTests extends AbstractProviderTest{

    public static final String PROTOCOL = Protocol.GOOGLE_VOICE;
    private static final String GOOGLEVOICE = "googlevoice.com";
    private static final GoogleVoiceProcessMap accountProcessMap = new GoogleVoiceProcessMap();

    @Test
    public void testReceive() throws DALException, MessagingException, InterruptedException, MessageStoreException
    {
        Account account = getAccount(PROTOCOL, GOOGLEVOICE);
        GoogleVoiceProvider provider = new GoogleVoiceProvider(new GoogleVoiceProviderSupport());
        testReceiveEmail(account, provider);
    }


   @Test
   public void testFolderFetch() throws IOException, MessagingException
   {

        Voice voice = new Voice("dan.morel@gmail.com","txtbl123");
        voice.login();
        Properties props = new Properties();
        props.setProperty("mail.pop3.port", "995");
        props.setProperty("mail.pop3.connectiontimeout", "30000");
        Session session = Session.getInstance(props);

        GoogleVoiceFolder inbox = new GoogleVoiceFolder(session,voice);

        Message[] messages = inbox.getMessages();
        for (Message message : messages) {

           System.out.println(printMessage((GoogleVoiceMessage) message));
        }

   }

   @Test
   public void testGoogleVoiceProcess() throws DALException,IOException, MessagingException
   {

        Account account = getAccount(PROTOCOL, GOOGLEVOICE);
        System.out.println(account.toString());
        long now = System.currentTimeMillis();
        GoogleVoiceProcess process = new GoogleVoiceProcess(now,account);
        Assert.assertTrue(process.connect());


   }

   @Test
   public void testGoogleVoiceProcessMap() throws DALException,IOException, MessagingException
   {

        Account account = getAccount(PROTOCOL, GOOGLEVOICE);
        System.out.println(account.toString());
        GoogleVoiceProcess process1 = accountProcessMap.getProcess(account);
        System.out.println(process1.toString());
        System.out.println("Process map size: " + accountProcessMap.getProcessMap().size());
        GoogleVoiceProcess process2 = accountProcessMap.getProcess(account);
        System.out.println("Process map size: " + accountProcessMap.getProcessMap().size());
        Assert.assertTrue(accountProcessMap.getProcessMap().size()==1);
        Assert.assertTrue(process2.connect());


   }

    @Test
    public void testGoogleVoiceProcessMap2() throws DALException,IOException, MessagingException
    {
        Account account = new Account();
        account.setName("dan.morel@googlevoice.com");
        account.setAlias_name("Dan Morel");
        account.setLoginName("dan.morel");
        account.setPassword("txtbl123");
        account.setUser_id("1111");
        handleProcess(account);

        Account account2 = new Account();
        account2.setName("asarva@googlevoice.com");
        account2.setAlias_name("Amol Sarva");
        account2.setLoginName("asarva");
        account2.setPassword("peekster");
        account2.setUser_id("1112");
        handleProcess(account2);


        Account account3 = new Account();
        account3.setName("halliday@googlevoice.com");
        account3.setAlias_name("Derek Halliday");
        account3.setLoginName("halliday");
        account3.setPassword("txtbl123");
        account3.setUser_id("1113");
        handleProcess(account3);



    }


    private void handleProcess(Account account) throws DALException,IOException, MessagingException{

        GoogleVoiceProcess process1 = GoogleVoiceProcessMap.getProcess(account);
        System.out.println("process: " + process1);
        Properties props = new Properties();
        props.setProperty("mail.pop3.port", "995");
        props.setProperty("mail.pop3.connectiontimeout", "30000");
        Session session = Session.getInstance(props);
        GoogleVoiceProviderSupport support = new GoogleVoiceProviderSupport();
        StopWatch watch = new StopWatch("mailcheck " + "");

        GoogleVoiceFolder inbox = support.connect(account,"",watch,process1);

        Message[] messages = inbox.getMessages();
        for (Message message : messages) {
            System.out.println(printMessage((GoogleVoiceMessage) message));
        }


    }


    private String printMessage(GoogleVoiceMessage msg) throws MessagingException, IOException {

        StringBuffer sb = new StringBuffer();
        sb.append("Subject: " + msg.getSubject());
        sb.append("\nFrom: " + msg.getFrom());
        sb.append("\nMessage ID: " + msg.getMessageID());
        sb.append("\nUID: " + msg.getUid());
        sb.append("\nReceived Data: " + msg.getReceivedDate().toString());
        sb.append("\nBody: " + msg.getContent());

        return sb.toString();

    }

    @Test
    public void testTimeStampCompare() throws DALException,IOException, MessagingException{

        Account account = getAccount(PROTOCOL, GOOGLEVOICE);
        System.out.println(account.toString());
        Voice voice = new Voice("dan.morel@gmail.com","txtbl123");
        voice.login();
        Properties props = new Properties();
        props.setProperty("mail.pop3.port", "995");
        props.setProperty("mail.pop3.connectiontimeout", "30000");
        Session session = Session.getInstance(props);

        GoogleVoiceFolder inbox = new GoogleVoiceFolder(session,voice);
        GoogleVoiceProviderSupport support = new GoogleVoiceProviderSupport();

        Message[] messages = inbox.getMessages();

        for(int i=0;i<messages.length;i++){
            if(account.getLast_received_date()==null){
                account.setLast_received_date(messages[i].getSentDate());
                Assert.assertTrue(support.isOlder(messages[i].getSentDate(),account.getLast_received_date()));

            }else{
                Assert.assertFalse(support.isOlder(messages[i].getSentDate(),account.getLast_received_date()));           
            }


        }

    }

    private void testReceiveEmail(Account account, Provider provider) throws DALException, MessagingException, InterruptedException, MessageStoreException
    {
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
        Assert.assertTrue(containsDroppedEmail(account));
    }


}
