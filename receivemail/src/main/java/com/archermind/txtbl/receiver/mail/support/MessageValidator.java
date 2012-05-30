package com.archermind.txtbl.receiver.mail.support;

import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.mail.store.MessageStoreException;
import com.archermind.txtbl.mail.store.MessageStoreFactory;

import javax.mail.Message;

public abstract class MessageValidator {
    public abstract Message validate(Message message, int messageNumber, String messageId) throws Exception;

    public void handleFailure(Account account, String messageStoreBucket, String messageId) throws MessageStoreException {
        MessageStoreFactory.getStore().addMessage(account.getId(), messageStoreBucket, messageId, account.getCountry());
    }
}
