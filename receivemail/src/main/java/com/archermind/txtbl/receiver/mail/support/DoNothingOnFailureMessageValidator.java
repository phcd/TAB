package com.archermind.txtbl.receiver.mail.support;

import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.mail.store.MessageStoreException;

public abstract class DoNothingOnFailureMessageValidator extends MessageValidator {
    public void handleFailure(Account account, String messageStoreBucket, String messageId) throws MessageStoreException {
        //do nothing
    }
}
