package com.archermind.txtbl.utils;

import com.webalgorithm.exchange.ConnectionPick;
import com.webalgorithm.exchange.ExchangeClient;
import com.webalgorithm.exchange.ExchangeClientFactory;
import com.webalgorithm.exchange.dto.Attachment;
import com.webalgorithm.exchange.dto.Contact;
import com.webalgorithm.exchange.dto.Event;
import com.webalgorithm.exchange.dto.Folder;
import com.webalgorithm.exchange.dto.Message;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

public class MailTests
{
    private static final String DEFAULT_FBA_PATH = "/owa/auth/owaauth.dll";
    private static final String DEFAULT_PREFIX = "exchange";

    @Test
    public void sendEmail() throws Exception
    {
        String host = "mex07a.mailtrust.com";
        String accountName = "exchange@webalgorithm.com";
        String password = "passw0rd!";
        String prefix = "exchange";
        String mailboxName = "exchange@webalgorithm.com";
        boolean useSSL = true;
        int port = 443;

        String fbaPath = "/owa/auth/owaauth.dll";

        ExchangeClientFactory factory = new ExchangeClientFactory();
        ExchangeClient connector = factory.getExchangeClient(host, port, useSSL, accountName, password, mailboxName, prefix, fbaPath);

        Message message = new Message();
        message.setTo("jmpak80@gmail.com");
        message.setBody("Text body");
        message.setFromName("exchange@webalgorithm.com");
        message.setFromEmail("exchange@webalgorithm.com");
        message.setSubject("Test subject");

        // Attachment 1
        /*
         * File file = new File("frontphone.gif"); byte[] buffer = new
         * byte[(int)file.length()]; InputStream in = new FileInputStream(file);
         * in.read(buffer); in.close();
         *
         * Attachment attachment = new Attachment();
         * attachment.setFileName("frontphone.gif"); attachment.setData(buffer);
         *
         * message.addAttachment(attachment);
         *
         * // Attachment 2 file = new File("1.txt"); buffer = new
         * byte[(int)file.length()]; in = new FileInputStream(file);
         * in.read(buffer); in.close();
         *
         * attachment = new Attachment(); attachment.setFileName("1.txt");
         * attachment.setData(buffer);
         *
         * message.addAttachment(attachment);
         */

        connector.sendMessage(message);
    }

    @Test
    public void addContact() throws Exception
    {
        String host = "mex07a.mailtrust.com";
        String accountName = "exchange@webalgorithm.com";
        String password = "passw0rd!";
        String prefix = "exchange";
        String mailboxName = "exchange@webalgorithm.com";
        boolean useSSL = true;
        int port = 443;
        String fbaPath = "/owa/auth/owaauth.dll";

        ExchangeClientFactory factory = new ExchangeClientFactory();
        ExchangeClient connector = factory.getExchangeClient(host, port, useSSL, accountName, password, mailboxName, prefix, fbaPath);

        Contact contact = new Contact();
        contact.setName("Test");
        contact.setSecondName("Test");
        contact.setMiddleName("Test");
        contact.setMobile("234234234234");

        System.out.println("adding...");
        connector.addContact(contact);

    }

    @Test
    public void getContacts() throws Exception
    {
        String host = "mex07a.mailtrust.com";
        String accountName = "exchange@webalgorithm.com";
        String password = "passw0rd!";
        String prefix = "exchange";
        String mailboxName = "exchange@webalgorithm.com";
        boolean useSSL = true;
        int port = 443;

        String fbaPath = "/owa/auth/owaauth.dll";

        System.out.println("connecting...");

        ExchangeClientFactory factory = new ExchangeClientFactory();

        ExchangeClient connector = factory.getExchangeClient(host, port, useSSL, accountName, password, mailboxName, prefix, fbaPath);
        System.out.println("getting contacts...");

        Collection<Contact> contacts = connector.getContacts();

        System.out.printf("got back %d contacts\n", contacts.size());
    }

    @Test
    public void getMessages() throws Exception
    {
        String host = "mex07a.mailtrust.com";
        String accountName = "exchange@webalgorithm.com";
        String password = "passw0rd!";
        String prefix = "exchange";
        String mailboxName = "exchange@webalgorithm.com";
        boolean useSSL = true;
        int port = 443;

        String fbaPath = "/owa/auth/owaauth.dll";

        System.out.println("connecting...");

        ExchangeClientFactory factory = new ExchangeClientFactory();

        ExchangeClient connector = factory.getExchangeClient(host, port, useSSL, accountName, password, mailboxName, prefix, fbaPath);

        System.out.println("getting messages...");

        Collection<Message> messages = connector.getMessages(null);

        System.out.printf("got back %d messages\n", messages.size());
    }

    @Test
    public void getFolders() throws Exception
    {
        String host = "mex07a.mailtrust.com";
        String accountName = "exchange@webalgorithm.com";
        String password = "passw0rd!";
        String prefix = "exchange";
        String mailboxName = "exchange@webalgorithm.com";
        boolean useSSL = true;
        int port = 443;

        String fbaPath = "/owa/auth/owaauth.dll";

        System.out.println("connecting...");

        ExchangeClientFactory factory = new ExchangeClientFactory();

        ExchangeClient connector = factory.getExchangeClient(host, port, useSSL, accountName, password, mailboxName, prefix, fbaPath);

        System.out.println("getting folders...");

        Collection<Folder> folders = connector.getFolders();

        System.out.printf("got back %d messages\n", folders.size());
    }

    @Test
    public void getMesagesWithAttachments() throws Exception
    {
        String host = "mex07a.mailtrust.com";
        String accountName = "exchange@webalgorithm.com";
        String password = "passw0rd!";
        String prefix = "exchange";
        String mailboxName = "exchange@webalgorithm.com";
        boolean useSSL = true;
        int port = 443;

        String fbaPath = "/owa/auth/owaauth.dll";

        System.out.println("connecting...");

        ExchangeClientFactory factory = new ExchangeClientFactory();

        ExchangeClient connector = factory.getExchangeClient(host, port, useSSL, accountName, password, mailboxName, prefix, fbaPath);

        System.out.println("getting mails...");

        Collection<Message> messages = connector.getMessages("Inbox/");

        for (Message message : messages)
        {
            if (message.isHasAttachment())
            {
                Collection<Attachment> attachments = connector.getAttachments(message.getHref());

                for (Attachment attachment : attachments)
                {
                    attachment.setData(connector.getAttachmentData(attachment.getHref()));
                    if (attachment.getData() != null)
                    {
                        OutputStream out = new FileOutputStream(attachment.getFileName());
                        out.write(attachment.getData());
                        out.close();
                    }
                }
            }
        }
    }

    @Test
    public void getMesagesWithAttachments2() throws Exception
    {
        String host = "getmail.energyadvantage.com";
        String password = "3test3";
        String mailboxName = "test3";

        boolean useSSL = false;
        int port = 80;

        System.out.println("connecting...");

        ConnectionPick pick = ExchangeClientFactory.pickConnection(host, port, useSSL, mailboxName, mailboxName, password, DEFAULT_PREFIX,
                DEFAULT_FBA_PATH);
        pick.getExchangeClient().close();
        ExchangeClient client = pick.getConnectionMode().getExchangeClient(host, port, useSSL, mailboxName, mailboxName, password,
                DEFAULT_PREFIX, DEFAULT_FBA_PATH);

        System.out.println("getting mails...");

        Collection<Message> messages = client.getMessagesHeaders("Inbox/");

        for (Message message : messages)
        {
            if (message.isHasAttachment())
            {
                Collection<Attachment> attachments = client.getAttachments(message.getHref());

                for (Attachment attachment : attachments)
                {
                    attachment.setData(client.getAttachmentData(attachment.getHref()));
                    if (attachment.getData() != null)
                    {
                        OutputStream out = new FileOutputStream(attachment.getFileName());
                        out.write(attachment.getData());
                        out.close();
                    }
                }
            }
        }
    }

    @Test
    public void getMesagesWithAttachments3() throws Exception
    {
        String host = "getmail.energyadvantage.com";
        String password = "3test3";
        String mailboxName = "test3";

        boolean useSSL = false;
        int port = 80;

        System.out.println("connecting...");

        ConnectionPick pick = ExchangeClientFactory.pickConnection(host, port, useSSL, mailboxName, mailboxName, password, DEFAULT_PREFIX,
                DEFAULT_FBA_PATH);
        pick.getExchangeClient().close();
        ExchangeClient client = pick.getConnectionMode().getExchangeClient(host, port, useSSL, mailboxName, mailboxName, password,
                DEFAULT_PREFIX, DEFAULT_FBA_PATH);

        System.out.println("getting mails...");

        Collection<Message> messages = client.getMessages("Inbox/");

        for (Message message : messages)
        {
            if (message.isHasAttachment())
            {
                Collection<Attachment> attachments = client.getAttachments(message.getHref());

                for (Attachment attachment : attachments)
                {
                    attachment.setData(client.getAttachmentData(attachment.getHref()));
                    if (attachment.getData() != null)
                    {
                        OutputStream out = new FileOutputStream(attachment.getFileName());
                        out.write(attachment.getData());
                        out.close();
                    }
                }
            }
        }
    }

    @Test
    public void getMesagesWithAttachments4() throws Exception
    {

        String host = "mail.americanpcsolutions.com";
        String password = "Passw0RD";
        String mailboxName = "testpeek";
        String loginName = "testpeek@americanpcsolutions.com";

        String prefix = "exchange";
        boolean useSSL = true;
        int port = 443;
        String fbaPath = "/exchweb/bin/auth/owaauth.dll";

        System.out.println("connecting...");

        ConnectionPick pick = ExchangeClientFactory.pickConnection(host, port, useSSL, mailboxName, loginName, password, prefix, fbaPath);
        pick.getExchangeClient().close();
        ExchangeClient client = pick.getConnectionMode().getExchangeClient(host, port, useSSL, mailboxName, mailboxName, password, prefix,
                fbaPath);

        System.out.println("getting mails...");

        Collection<Message> messages = client.getMessages("Inbox/");

        for (Message message : messages)
        {
            if (message.isHasAttachment())
            {
                Collection<Attachment> attachments = client.getAttachments(message.getHref());

                for (Attachment attachment : attachments)
                {
                    attachment.setData(client.getAttachmentData(attachment.getHref()));
                    if (attachment.getData() != null)
                    {
                        OutputStream out = new FileOutputStream(attachment.getFileName());
                        out.write(attachment.getData());
                        out.close();
                    }
                }
            }
        }
    }

    @Test
    public void getMesagesWithIhostexchange() throws Exception
    {

        String host = "webmail.ihostexchange.net";
        String password = "txtbl123";
        String mailboxName = "peek_exchange2007@getpeek.com";

        String prefix = "exchange";
        boolean useSSL = true;
        int port = 443;
        String fbaPath = "/owa/auth/owaauth.dll";

        System.out.println("connecting...");

        ConnectionPick pick = ExchangeClientFactory.pickConnection(host, port, useSSL, mailboxName, mailboxName, password, prefix, fbaPath);
        pick.getExchangeClient().close();
        ExchangeClient client = pick.getConnectionMode().getExchangeClient(host, port, useSSL, mailboxName, mailboxName, password, prefix,
                fbaPath);

        System.out.println("getting mails...");

        Collection<Message> messages = client.getMessages("Inbox/");

        System.out.println("have we have " + messages.size() + " messages...");

        for (Message message : messages)
        {
            if (message.isHasAttachment())
            {
                Collection<Attachment> attachments = client.getAttachments(message.getHref());

                for (Attachment attachment : attachments)
                {
                    attachment.setData(client.getAttachmentData(attachment.getHref()));
                    if (attachment.getData() != null)
                    {
                        OutputStream out = new FileOutputStream(attachment.getFileName());
                        out.write(attachment.getData());
                        out.close();
                    }
                }

            }

        }
    }

    @Test
    public void getMesagesWithBerkley() throws Exception
    {

        String host = "mail.haas.berkeley.edu";
        String password = "NewPass4me";
        String mailboxName = "mandar_shinde@haas.berkeley.edu";
        String loginName = "mandar_shinde";

        String prefix = "exchange";
        boolean useSSL = true;
        int port = 443;
        String fbaPath = "/owa/auth/owaauth.dll";

        System.out.println("connecting...");

        ConnectionPick pick = ExchangeClientFactory.pickConnection(host, port, useSSL, mailboxName, loginName, password, prefix, fbaPath);
        pick.getExchangeClient().close();
        ExchangeClient client = pick.getConnectionMode().getExchangeClient(host, port, useSSL, mailboxName, mailboxName, password, prefix,
                fbaPath);

        System.out.println("getting mails...");

        Collection<Message> messages = client.getMessages("Inbox/", 0, 1000);

        System.out.println("have we have " + messages.size() + " messages...");

        for (Message message : messages)
        {
            if (message.isHasAttachment())
            {
                Collection<Attachment> attachments = client.getAttachments(message.getHref());

                for (Attachment attachment : attachments)
                {
                    attachment.setData(client.getAttachmentData(attachment.getHref()));
                    if (attachment.getData() != null)
                    {
                        OutputStream out = new FileOutputStream(attachment.getFileName());
                        out.write(attachment.getData());
                        out.close();
                    }
                }

            }

        }
    }

    @Test
    public void getMesagesWithMail2Web() throws Exception
    {

        String host = "exchange.mail2web.com";
        String password = "txtbl123";
        String mailboxName = "peek_exchange2003@mail2web.com";

        String prefix = "exchange";
        boolean useSSL = true;
        int port = 443;
        String fbaPath = "/owa/auth/owaauth.dll";

        System.out.println("connecting...");

        ConnectionPick pick = ExchangeClientFactory.pickConnection(host, port, useSSL, mailboxName, mailboxName, password, prefix, fbaPath);
        pick.getExchangeClient().close();
        ExchangeClient client = pick.getConnectionMode().getExchangeClient(host, port, useSSL, mailboxName, mailboxName, password, prefix,
                fbaPath);

        System.out.println("getting 1000 mails...");

        Collection<Message> messages = client.getMessages("Inbox/", 0, 1000);

        System.out.println("have we have " + messages.size() + " messages...");

        for (Message message : messages)
        {
            if (message.isHasAttachment())
            {
                System.out.println("getting attachments for " + message.getHref());

                Collection<Attachment> attachments = client.getAttachments(message.getHref());

                for (Attachment attachment : attachments)
                {
                    attachment.setData(client.getAttachmentData(attachment.getHref()));
                    if (attachment.getData() != null)
                    {
                        OutputStream out = new FileOutputStream(attachment.getFileName());
                        out.write(attachment.getData());
                        out.close();
                    }
                }

            }

        }
    }

    @Test
    public void justTest() throws Exception
    {
        String host = "mex07a.mailtrust.com";
        String accountName = "exchange@webalgorithm.com";
        String password = "passw0rd!";
        String prefix = "exchange";
        String mailboxName = "exchange@webalgorithm.com";
        boolean useSSL = true;
        int port = 443;
        String fbaPath = "/owa/auth/owaauth.dll";

        System.out.println("connecting...");

        ExchangeClientFactory factory = new ExchangeClientFactory();

        ExchangeClient connector = factory.getExchangeClient(host, port, useSSL, accountName, password, mailboxName, prefix, fbaPath);

        System.out.println("getting mails...");

        Collection<Message> messages = connector.getMessages("Inbox/");

        for (Message message : messages)
        {
            if (message.isHasAttachment())
            {
                Collection<Attachment> attachments = connector.getAttachments(message.getHref());

                for (Attachment attachment : attachments)
                {
                    attachment.setData(connector.getAttachmentData(attachment.getHref()));
                    if (attachment.getData() != null)
                    {
                        OutputStream out = new FileOutputStream(attachment.getFileName());
                        out.write(attachment.getData());
                        out.close();
                    }
                }
            }
        }
    }

    @Test
    public void addEvent() throws Exception
    {
        String host = "mex07a.mailtrust.com";
        String accountName = "exchange@webalgorithm.com";
        String password = "passw0rd!";
        String prefix = "exchange";
        String mailboxName = "exchange@webalgorithm.com";
        boolean useSSL = true;
        int port = 443;
        String fbaPath = "/owa/auth/owaauth.dll";

        System.out.println("connecting...");

        ExchangeClientFactory factory = new ExchangeClientFactory();

        ExchangeClient connector = factory.getExchangeClient(host, port, useSSL, accountName, password, mailboxName, prefix, fbaPath);

        Event event = new Event();
        event.setSubject("test event 3 ");
        event.setDescription("my test event 3");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date sinceDate = dateFormat.parse("2009-01-13 15:30:00");
        Date tillDate = dateFormat.parse("2009-01-13 15:55:00");
        event.setStart(sinceDate);
        event.setEnd(tillDate);

        event.setReminderBeforeStart(15);
        event.setReminderSet(true);

        event.setImportance(2);
        event.setLocation("test location 3");

        // Attachment
        File file = new File("1.txt");
        byte[] buffer = new byte[(int) file.length()];
        InputStream in = new FileInputStream(file);
        in.read(buffer);
        in.close();

        Attachment attachment = new Attachment();
        attachment.setFileName("1.txt");
        attachment.setData(buffer);

        event.addAttachment(attachment);

        file = new File("2.jpg");
        buffer = new byte[(int) file.length()];
        in = new FileInputStream(file);
        in.read(buffer);
        in.close();

        attachment = new Attachment();
        attachment.setFileName("2.jpg");
        attachment.setData(buffer);

        event.addAttachment(attachment);

        connector.addEvent(event);
    }

    @Test
    public void getEvents() throws Exception
    {
        String host = "mex07a.mailtrust.com";
        String accountName = "exchange@webalgorithm.com";
        String password = "passw0rd!";
        String prefix = "exchange";
        String mailboxName = "exchange@webalgorithm.com";
        boolean useSSL = true;
        int port = 443;
        String fbaPath = "/owa/auth/owaauth.dll";

        System.out.println("connecting...");

        ExchangeClientFactory factory = new ExchangeClientFactory();

        ExchangeClient connector = factory.getExchangeClient(host, port, useSSL, accountName, password, mailboxName, prefix, fbaPath);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date sinceDate = dateFormat.parse("2008-01-09 16:30:00");
        Date tillDate = dateFormat.parse("2010-01-09 16:55:00");

        Collection<Event> events = connector.getEvents(null, sinceDate, tillDate);

        System.out.printf("got back %d messages\n", events.size());

        for (Event event : events)
        {
            if (event.isHasAttachment())
            {
                Collection<Attachment> attachments = connector.getAttachments(event.getHref());

                for (Attachment attachment : attachments)
                {
                    attachment.setData(connector.getAttachmentData(attachment.getHref()));
                    if (attachment.getData() != null)
                    {
                        OutputStream out = new FileOutputStream(attachment.getFileName());
                        out.write(attachment.getData());
                        out.close();
                    }
                }
            }
        }
    }

    @Test
    public void getContactsNew() throws Exception
    {
        String host = "getmail.energyadvantage.com";
        String password = "1test1";
        String mailboxName = "test1";

        System.out.println("connecting...");

        int port = 80;
        boolean useSSL = false;
        ConnectionPick pick = ExchangeClientFactory.pickConnection(host, port, useSSL, mailboxName, mailboxName, password, DEFAULT_PREFIX,
                DEFAULT_FBA_PATH);

        pick.getExchangeClient().close();

        ExchangeClient client = pick.getConnectionMode().getExchangeClient(host, port, useSSL, mailboxName, mailboxName, password,
                DEFAULT_PREFIX, DEFAULT_FBA_PATH);

        Collection<Contact> contacts = client.getContacts();

        System.out.printf("got back %d contacts\n", contacts.size());
    }

    @Test
    public void getMessagesNew() throws Exception
    {
        String host = "getmail.energyadvantage.com";
        String password = "1test1";
        String mailboxName = "test1";

        System.out.println("connecting...");
        int port = 80;
        boolean useSSL = false;
        ConnectionPick pick = ExchangeClientFactory.pickConnection(host, port, useSSL, mailboxName, mailboxName, password, DEFAULT_PREFIX,
                DEFAULT_FBA_PATH);
        pick.getExchangeClient().close();
        ExchangeClient client = pick.getConnectionMode().getExchangeClient(host, port, useSSL, mailboxName, mailboxName, password,
                DEFAULT_PREFIX, DEFAULT_FBA_PATH);

        System.out.println("getting messages...");

        Collection<Message> messages = client.getMessages(null);

        System.out.printf("got back %d messages\n", messages.size());
    }

    @Test
    public void getFoldersNew() throws Exception
    {
        String host = "mail.haas.berkeley.edu";
        String password = "NewPass4me";
        String mailboxName = "mandar_shinde@haas.berkeley.edu";
        String loginName = "mandar_shinde";

        System.out.println("connecting...");
        int port = 443;
        boolean useSSL = true;
        ConnectionPick pick = ExchangeClientFactory.pickConnection(host, port, useSSL, mailboxName, loginName, password, DEFAULT_PREFIX,
                DEFAULT_FBA_PATH);
        pick.getExchangeClient().close();
        ExchangeClient client = pick.getConnectionMode().getExchangeClient(host, port, useSSL, mailboxName, mailboxName, password,
                DEFAULT_PREFIX, DEFAULT_FBA_PATH);

        Collection<Folder> folders = client.getFolders();

        System.out.printf("got back %d messages\n", folders.size());
    }

    @Test
    public void dateFormatTest() throws Exception
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date = sdf.parse("2009-04-07T14:53:36.093Z");
        date.toString();
    }

    @Test
    public void getMessagesHeaders() throws Exception
    {
        String host = "getmail.energyadvantage.com";
        String password = "3test3";
        String mailboxName = "test3";

        System.out.println("connecting...");
        int port = 80;
        boolean useSSL = false;
        ConnectionPick pick = ExchangeClientFactory.pickConnection(host, port, useSSL, mailboxName, mailboxName, password, DEFAULT_PREFIX,
                DEFAULT_FBA_PATH);
        pick.getExchangeClient().close();
        ExchangeClient client = pick.getConnectionMode().getExchangeClient(host, port, useSSL, mailboxName, mailboxName, password,
                DEFAULT_PREFIX, DEFAULT_FBA_PATH);

        System.out.println("getting messages...");

        Collection<Message> messages = client.getMessagesHeaders(null);

        System.out.printf("got back %d messages\n", messages.size());
    }

    @Test
    public void getMessage() throws Exception
    {
        String host = "getmail.energyadvantage.com";
        String password = "1test1";
        String mailboxName = "test1";

        System.out.println("connecting...");
        int port = 80;
        boolean useSSL = false;
        ConnectionPick pick = ExchangeClientFactory.pickConnection(host, port, useSSL, mailboxName, mailboxName, password, DEFAULT_PREFIX,
                DEFAULT_FBA_PATH);
        pick.getExchangeClient().close();
        ExchangeClient client = pick.getConnectionMode().getExchangeClient(host, port, useSSL, mailboxName, mailboxName, password,
                DEFAULT_PREFIX, DEFAULT_FBA_PATH);

        System.out.println("getting messages...");

        client.getMessageByUid(null, "AQEAAAACLAMYAAAAAAAAAAAAAAAA");
    }

    @Test
    public void getFoldersNew2() throws Exception
    {
        String host = "webmail.ihostexchange.net";
        String password = "txtbl123";
        String mailboxName = "peek_exchange2007@getpeek.com";

        System.out.println("connecting...");
        int port = 443;
        boolean useSSL = true;
        ConnectionPick pick = ExchangeClientFactory.pickConnection(host, port, useSSL, mailboxName, mailboxName, password, DEFAULT_PREFIX,
                DEFAULT_FBA_PATH);
        pick.getExchangeClient().close();
        ExchangeClient client = pick.getConnectionMode().getExchangeClient(host, port, useSSL, mailboxName, mailboxName, password,
                DEFAULT_PREFIX, DEFAULT_FBA_PATH);

        Collection<Folder> folders = client.getFolders();

        System.out.printf("got back %d messages\n", folders.size());
    }

    private void doValidate(String exchangeServer, int port, boolean useSSL, String emailAccount, String loginName, String password,
            String fbaPath, String prefix) throws Exception
    {
        try
        {
            ConnectionPick cp = ExchangeClientFactory.pickConnection(exchangeServer, port, useSSL, emailAccount, loginName, password, prefix, fbaPath);

            if (cp != null)
            {
                ExchangeClient exchangeClient = cp.getExchangeClient();

                Collection<com.webalgorithm.exchange.dto.Message> msExchangeMessages = exchangeClient.getMessagesHeaders("Inbox");
                System.out.println(msExchangeMessages.size());
                System.out.println(String.format("%s %s succeeded with mode %s", emailAccount, loginName, cp.getConnectionMode()));
                exchangeClient.close();
            }
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            System.out.println(String.format("%s %s failed", emailAccount, loginName));
        }
    }

    public static void main(String args[]) throws Exception
    {
        MailTests validate = new MailTests();
//        validate.doValidate("exchange.getpeek.in", 443, true, "paul@exchange.getpeek.in", "paul@exchange.getpeek.in", "peek_123",
//                "/exchweb/bin/auth/owaauth.dll", "exchange");

//        validate.doValidate("mex07a.mailtrust.com", 443, true, "exchange@webalgorithm.com", "exchange@webalgorithm.com", "passw0rd!",
//                "/owa/auth/owaauth.dll", "exchange");
//
//        validate.doValidate("getmail.energyadvantage.com", 80, false, "test3", "test3", "3test3", "/owa/auth/owaauth.dll", "exchange");
//
//        validate.doValidate("getmail.energyadvantage.com", 80, false, "test3@getmail.energyadvantage.com",
//                "test3@getmail.energyadvantage.com", "3test3", "/owa/auth/owaauth.dll", "exchange");
//
//        validate.doValidate("mail.americanpcsolutions.com", 443, true, "testpeek@americanpcsolutions.com", "testpeek", "Passw0RD",
//                "/exchweb/bin/auth/owaauth.dll", "exchange");
//
//        validate.doValidate("mail.americanpcsolutions.com", 443, true, "testpeek@americanpcsolutions.com",
//                "testpeek@americanpcsolutions.com", "Passw0RD", "/exchweb/bin/auth/owaauth.dll", "exchange");
//
//        validate.doValidate("webmail.ihostexchange.net", 443, true, "peek_exchange2007@getpeek.com", "peek_exchange2007@getpeek.com",
//                "txtbl123", "/owa/auth/owaauth.dll", "exchange");
//
//        validate.doValidate("mail.haas.berkeley.edu", 443, true, "mandar_shinde@haas.berkeley.edu", "mandar_shinde", "NewPass4me",
//                "/owa/auth/owaauth.dll", "exchange");
//
//        validate.doValidate("mail.haas.berkeley.edu", 443, true, "mandar_shinde@haas.berkeley.edu", "mandar_shinde@haas.berkeley.edu",
//                "NewPass4me", "/owa/auth/owaauth.dll", "exchange");
//
//        validate.doValidate("exchange.mail2web.com", 443, true, "peek_exchange2003@mail2web.com", "peek_exchange2003@mail2web.com",
//                "txtbl123", "/owa/auth/owaauth.dll", "exchange");
//
//        validate.doValidate("mail.mse22.exchange.ms", 443, true, "mbharadwaj@peekindia.mymailstreet.com", "mbharadwaj", "mailster",
//                "/owa/auth/owaauth.dll", "owa");
//
//        validate.doValidate("mail.mse22.exchange.ms", 443, true, "mbharadwaj@peekindia.mymailstreet.com",
//                "mbharadwaj@peekindia.mymailstreet.com", "mailster", "/owa/auth/owaauth.dll", "owa");

        validate.doValidate("221.134.83.1", 443, true, "pmanuel@meyer.co.in", "paul", "paulpeek", null, "exchange");

    }


}
