package com.archermind.txtbl.receiver.mail.abst.impl;

import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.Protocol;
import com.sun.mail.imap.IMAPStore;
import org.springframework.core.io.ClassPathResource;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.naming.NamingException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Security;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

public class MailBoxHelper
{

    private static String BIG_FILE = "/email/CDB3.pdf";


    enum SimpleMessage
    {
        DAILY_REPORT("Hi all, today " + new Date() + "I created tests and fixed bugs,\n Thank you, ")
        , CURRENT_STATUS("Hi all, today " + new Date() + "I created tests and fixed bugs, \n Thank you,")
        , PLANNING("Hi all, " + new Date() + "I'm planning to created tests and fixed bugs. \n Thank you, ")
        , CHECKIN_CODE("Hi all, " + new Date() + "I chekin code. please update code. \n Thank you, ")
        , BUG("Hi all, " + new Date() + "I found bug. \n Thank you, ");

        private String body;

        private SimpleMessage(String body)
        {
            this.body = body;
        }

        public String getBody()
        {
            return body;
        }
    }

    private static String[] emails = {"newimap@webalgorithm.com"
            , "sender@webalgorithm.com"
            , "newpop3@webalgorithm.com"
            , "fregat123@gmail.com"
            , "fregat456@gmail.com"
            , "newimapp@yahoo.com"
            , "newpop3p@hotmail.com"
            , "newimapp@aol.com"
    };



    public static int removeEmails(Account account, String protocol) throws MessagingException, UnknownHostException
    {
        int count = 0;
        Session session = Session.getInstance(Protocol.POP3.equals(protocol) ? getPop3MailProperties(account) : getImapMailProperties(account));
        Folder inbox;

//        store = session.getStore("imap");
        if (account.getLoginName().contains("@yahoo."))
        {
            IMAPStore imapstore = (IMAPStore) session.getStore(protocol);
            yahooConnect(account, imapstore, true);
            inbox = imapstore.getFolder("INBOX");
        }
        else
        {
            Store  store = session.getStore(protocol);
            store.connect(account.getReceiveHost(), account.getLoginName(), account.getPassword());
            inbox = store.getFolder("INBOX");
        }


        inbox.open(Folder.READ_WRITE);

        count = inbox.getMessageCount();
        for (Message message : inbox.getMessages())
        {
            message.setFlag(Flags.Flag.DELETED, true);
        }
        inbox.close(true);
        return count;
    }

    private static void sendSimpleEmails(String toUserName, int count) throws MessagingException, InterruptedException, IOException
    {

        Session session = Session.getInstance(getEmailProperties());

        Random random = new Random();
        int emailSize = emails.length;
        for (int i = 1; i <= count; i++)
        {
            Thread.sleep(10000);

            MimeMessage msg = new MimeMessage(session);

            InternetAddress from =
                    new InternetAddress(emails[random.nextInt(emailSize)]);

            InternetAddress to =
                    new InternetAddress(toUserName);

            msg.setFrom(from);
            msg.addRecipient(Message.RecipientType.TO, to);
            msg.setSentDate(new Date());

            SimpleMessage randomMessage = SimpleMessage.values()[random.nextInt(SimpleMessage.values().length)];
            msg.setSubject(randomMessage.name());
            msg.setText(randomMessage.getBody());
            Transport.send(msg);
        }
    }

    private static void sendBigEmail(String toUserName) throws MessagingException, IOException
    {
        Session session = Session.getInstance(getEmailProperties());

        Random random = new Random();
        int emailSize = emails.length;
        MimeMessage msg = new MimeMessage(session);

        InternetAddress from =
                new InternetAddress(emails[random.nextInt(emailSize)]);

        InternetAddress to =
                new InternetAddress(toUserName);

        msg.setFrom(from);
        msg.addRecipient(Message.RecipientType.TO, to);

        msg.setSubject("New book  " + UUID.randomUUID().toString());

        // create and fill the first message part
        MimeBodyPart mbp1 = new MimeBodyPart();
        mbp1.setText("Hi, all \n I sent you book.\n  Thank you, ");

        // create the second message part
        MimeBodyPart mbp2 = new MimeBodyPart();

        // attach the file to the message
        FileDataSource fds = new FileDataSource(new ClassPathResource(BIG_FILE).getFile());
        mbp2.setDataHandler(new DataHandler(fds));
        mbp2.setFileName(fds.getName());

        // create the Multipart and add its parts to it
        Multipart mp = new MimeMultipart();
        mp.addBodyPart(mbp1);
        mp.addBodyPart(mbp2);



        // add the Multipart to the message
        msg.setContent(mp);

        // set the Date: header
        msg.setSentDate(new Date());
//                    msg.setText(String.format(TEXT, name));

        //send message
        Transport.send(msg);

    }

    public static void main(String[] args) throws MessagingException, NamingException, IOException, InterruptedException
    {

        Account account = new Account();


//        account.setLoginName("newimapp@aol.com");
//        account.setPassword("p123456");
//        account.setReceiveHost("imap.aol.com");
//        account.setReceivePort("143");
//        removeEmails(account, "imap");


//        account.setLoginName("newpop3p@hotmail.com");
//        account.setPassword("123456");
//        account.setReceiveHost("pop3.live.com");
//        account.setReceiveTs("ssl");
//        account.setReceivePort("995");
//
//        removeEmails(account, "pop3");

        account.setLoginName("newimapp@yahoo.com");
        account.setPassword("123456");
        account.setReceiveHost("imap.mail.yahoo.com");
        account.setReceivePort("143");
        removeEmails(account, "imap");

//        account.setLoginName("fregat456@gmail.com");
//        account.setPassword("passw0rd");
//        account.setReceiveHost("pop.gmail.com");
//        account.setReceiveTs("ssl");
//        account.setReceivePort("995");
//        removeEmails(account, "pop3");

//        account.setLoginName("fregat123@gmail.com");
//        account.setPassword("passw0rd");
//        account.setReceiveHost("imap.gmail.com");
//        account.setReceiveTs("ssl");
//        account.setReceivePort("993");
//        removeEmails(account, "imap");


//        account.setLoginName("newimap@webalgorithm.com");
//        account.setPassword("123456");
//        account.setReceiveHost("pop.emailsrvr.com");
//        account.setReceivePort("110");
//        removeEmails(account, "pop3");

//        account.setLoginName("newpop3@webalgorithm.com");
//        account.setPassword("123456");
//        account.setReceiveHost("pop.emailsrvr.com");
//        account.setReceivePort("110");
//        removeEmails(account, "pop3");


        sendBigEmail(account.getLoginName());
        Thread.sleep(60000);
        sendSimpleEmails(account.getLoginName(), 3);
        sendSimpleEmails(account.getLoginName(), 5);

    }

    private static Properties getImapMailProperties(Account account)
    {
        Properties props = new Properties();

        if (account.getReceiveProtocolType().contains("gmail"))
        {
            props.put("mail.imap.host", "imap.gmail.com");
            props.put("mail.imap.port", "143");
            props.put("mail.imap.auth", "true");
            props.put("mail.store.protocol", "imap");
            props.put("mail.imap.starttls.enable", "true");
            props.put("mail.imap.socketFactory.port", "993");
            props.put("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.iamp.socketFactory.fallback", "false");
        }
        else
        {

            props.setProperty("mail.imap.port", account.getReceivePort());
            props.setProperty("mail.imap.connectiontimeout", "30000");
            if ("ssl".equals(account.getReceiveTs()))
            {
                Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
                props.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.setProperty("mail.imap.socketFactory.fallback", "false");
                props.setProperty("mail.imap.socketFactory.port", account.getReceivePort());
            }
            else if ("tls".equals(account.getReceiveTs()))
            {
                props.setProperty("mail.imap.starttls.enable", "true");
                java.security.Security.setProperty("ssl.SocketFactory.provider", "com.archermind.txtbl.mail.DummySSLSocketFactory");
            }
        }


        return props;
    }

    private static Properties getPop3MailProperties(Account account)
    {
        Properties props = new Properties();
        props.setProperty("mail.pop3.port", account.getReceivePort());
        props.setProperty("mail.pop3.connectiontimeout", "30000");
        if ("ssl".equals(account.getReceiveTs()))
        {
            Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
            props.setProperty("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.setProperty("mail.pop3.socketFactory.fallback", "false");
            props.setProperty("mail.pop3.socketFactory.port", account.getReceivePort());
        }
        else if ("tls".equals(account.getReceiveTs()))
        {
            props.setProperty("mail.pop3.starttls.enable", "true");
            java.security.Security.setProperty("ssl.SocketFactory.provider", "com.archermind.txtbl.mail.DummySSLSocketFactory");
        }
        return props;
    }

      /**
     * Special Yahoo connect routines. We resolve hostname IP address and try each one until connected
     *
     * @param account
     * @param store
     * @return
     * @throws java.net.UnknownHostException
     */
    private static int yahooConnect(Account account, IMAPStore store, boolean specialYahooConnectEnabled) throws UnknownHostException, MessagingException
    {
        int connectionAttempts = 0;
        store.SetIDCommand("ID (\"vendor\" \"Zimbra\" \"os\" \"Windows XP\" \"os-version\" \"5.1\" \"guid\" \"4062-5711-9195-4050\")");

        if (specialYahooConnectEnabled)
        {
            InetAddress[] addresses = InetAddress.getAllByName(account.getReceiveHost());

            for (InetAddress address : addresses)
            {
                try
                {
                    store.connect(address.getHostAddress(), account.getLoginName(), account.getPassword());

                    connectionAttempts++;


                    break;
                }
                catch (Throwable t)
                {
                }
            }
        }
        else
        {
            store.connect(account.getReceiveHost(), account.getLoginName(), account.getPassword());

            connectionAttempts++;
        }

        return connectionAttempts;
    }


    private static Properties getEmailProperties() throws IOException
    {
        Properties props = new Properties();
        props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("email/mail.properties"));
        return props;
    }

}
