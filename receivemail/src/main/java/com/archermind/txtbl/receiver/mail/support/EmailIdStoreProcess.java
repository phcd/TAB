package com.archermind.txtbl.receiver.mail.support;

import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.mail.store.MessageStoreException;
import com.archermind.txtbl.mail.store.MessageStoreFactory;
import com.archermind.txtbl.utils.StopWatch;
import com.archermind.txtbl.utils.StopWatchUtils;
import org.jboss.logging.Logger;

import javax.mail.Message;
import java.util.Set;

public class EmailIdStoreProcess extends EmailProcess {
    private static final Logger log = Logger.getLogger(EmailIdStoreProcess.class);

    private Account account;
    private Set<String> storeMessageIds;
    private String storeBucket;
    private String context;
    private StopWatch watch;
    private EmailProcess nextProcess;

    public EmailIdStoreProcess(Account account, Set<String> storeMessageIds, String storeBucket, String context, StopWatch watch, EmailProcess nextProcess) {
        this.account = account;
        this.storeMessageIds = storeMessageIds;
        this.storeBucket = storeBucket;
        this.context = context;
        this.watch = watch;
        this.nextProcess = nextProcess;
    }

    public boolean process(Message message, int messageNumber, String messageId) throws Exception {
        log.info(String.format("msgNum=%d, messageId=%s, message is new for %s", messageNumber, messageId, context));
        if (addToStore(messageNumber, messageId)) {
            try {
                StopWatchUtils.newTask(watch, String.format("msgNum=%s, processor=%s", messageNumber, nextProcess.getClass()), context, log);
                nextProcess.process(message, messageNumber, messageId);
            } catch (Throwable e) {
                log.fatal(String.format("msgNum=%d, messageId=%s, subject=%s message %s failed for %s", messageNumber, messageId, nextProcess.getClass(), message.getSubject(), context), e);
                deleteFromStore(messageNumber, messageId);
            }
            return true;
        }
        return false;
    }

    private void deleteFromStore(int messageNumber, String messageId) throws MessageStoreException {
        StopWatchUtils.newTask(watch, String.format("msgNum=%s, removeFromStore", messageNumber), context, log);
        MessageStoreFactory.getStore().deleteMessage(account.getId(), storeBucket, messageId, account.getCountry());
        storeMessageIds.remove(messageId);
    }

    private boolean addToStore(int messageNumber, String messageId) throws MessageStoreException {
        StopWatchUtils.newTask(watch, String.format("msgNum=%s, addToStore", messageNumber), context, log);
        storeMessageIds.add(messageId);
        return MessageStoreFactory.getStore().addMessage(account.getId(), storeBucket, messageId, account.getCountry());
    }

}
