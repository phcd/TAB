package com.archermind.txtbl.receiver.mail.support;

import com.archermind.txtbl.domain.Account;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

public class NewMSExchangeProviderSupport extends NewProviderSupport
{
    protected Message getFullyLoad(Message message, Folder folder, String context) throws MessagingException
    {
        ((MSExchangeFolder) folder).loadFully((MSExchangeMessage) message);
        Message wrappedMessage = new MimeMessage((MimeMessage) message);
        wrappedMessage.setSentDate(message.getSentDate());
        return wrappedMessage;
    }

    public boolean isSentMessage(Account account, Message message) throws Exception
    {
        return false;
    }
}
