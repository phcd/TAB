package com.archermind.txtbl.receiver.mail.support;


import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.OriginalReceivedAttachment;
import com.archermind.txtbl.exception.SystemException;
import com.archermind.txtbl.mail.store.MessageStoreException;
import com.archermind.txtbl.utils.StopWatch;
import com.archermind.txtbl.utils.StopWatchUtils;
import com.archermind.txtbl.utils.SysConfigManager;
import com.zynku.sync.activesync.control.ActiveSyncController;
import com.zynku.sync.activesync.model.ApplicationData;

import javax.mail.Message;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class XobniActiveSyncSupport extends ActiveSyncSupport {

     public int getMaxEmailCount(){
        return 50000;
    }


    /**
     * Method for processing email message
     *
     * @param account         account instance
     * @param messageNumber   current message number
     * @param storeMessageIds message id's storage
     * @param storeBucket     message id's store bucket
     * @param email           email feilds collection
     * @param messageId       message id
     * @param context         context
     * @param watch           watch instance
     * @param isFirstTime     boolean flag for detect receive mail in first time
     * @return result status value. if true then message was stored if false then either exception was occurs
     *         or message was processed before
     * @throws Exception raised exception
     */
    public boolean processMessage(Account account, int messageNumber, Set<String> storeMessageIds, String storeBucket,
                                  ApplicationData email, String messageId, String context, StopWatch watch,
                                  boolean isFirstTime, ActiveSyncController controller,XobniSaveProcess xobniSaveProcess) {
        boolean result = false;

        long start = System.nanoTime();

        try {
            if (timeCorrection == null) {
                timeCorrection = getTimeCorrection(account);
                if (log.isDebugEnabled())
                    log.debug(String.format("time zone correction is %s for %s", timeCorrection, context));
            }

            StopWatchUtils.newTask(watch, String.format("msgNum=%s, storeCheck", messageNumber), context, log);

            if (log.isDebugEnabled())
                log.debug(String.format("msgNum=%d, checking if messageId=%s is known for %s", messageNumber, messageId, context));

            // note that we will first check in he loaded list and only query the store when it is not there. this will force the message to be pulled from backing store if we are out of synch
            if (messageId == null || (!storeMessageIds.contains(messageId) && !messageIdStore.hasMessage(account.getId(), storeBucket, messageId, account.getCountry()))) {
                Date mailSentDate = getDateFromString(email.get("DateReceived"));
                if (!isFirstTime && isMessageTooOld(account, mailSentDate, context)) {
                    log.warn(String.format("msgNum=%d, messageId=%s, message is too old, sentDate=%s, discarding, for %s", messageNumber, messageId, mailSentDate, context));
                    // this message is too old and needs to be ignored
                    messageIdStore.addMessage(account.getId(), storeBucket, messageId, account.getCountry());
                } else {
                    if (log.isDebugEnabled())
                        log.debug(String.format("msgNum=%d, messageId=%s, message is new for %s", messageNumber, messageId, context));
                    try {
                        StopWatchUtils.newTask(watch, String.format("msgNum=%s, getAttachments", messageNumber), context, log);

                        List<OriginalReceivedAttachment> attachments = getAttachments(email, controller, context);

                        StopWatchUtils.newTask(watch, String.format("msgNum=%s, getMessage", messageNumber), context, log);

                        Message message = getMessage(account, email, attachments, context);

                        StopWatchUtils.newTask(watch, String.format("msgNum=%s, saveMessage", messageNumber), context, log);

                        xobniSaveProcess.process(message, messageNumber, messageId);

                        //process if we have 1000 emails there to process
                        if(xobniSaveProcess.shouldComplete()){
                            xobniSaveProcess.complete();
                        }

                    } catch (Throwable e) {
                        log.fatal(String.format("msgNum=%d, messageId=%s, message saving failed for %s", messageNumber, messageId, context), e);
                    }
                    result = true;
                }
            } else {
                if (log.isDebugEnabled())
                    log.debug(String.format("msgNum=%d, messageId=%s is known for %s, skipping message [received=%s, subject=%s]", messageNumber, messageId, context, email.get("DateReceived"), email.get("Subject")));
            }


        }
        catch (Throwable t) {
            throw new SystemException(String.format("Unable to process message %s with id %s for %s", messageNumber, messageId, context), t);
        }
        finally {
            if (log.isDebugEnabled())
                log.debug(String.format("msgNum=%d, checked if messageId=%s is known in %dms for %s", messageNumber, messageId, (System.nanoTime() - start) / 1000000, context));
        }

        return result;
    }

        /**
     * First time mail check method.
     *
     * @param folders            folders list to check
     * @param account            account instance
     * @param syncKeys           sync keys map
     * @param watch              stop watch instance
     * @param context            context string
     * @param messageStoreBucket message id's store bucket
     * @return received new messages number
     * @throws Exception raised exception
     */
    public int[] getMailFirstTime(List<com.zynku.sync.activesync.model.Folder> folders,
                                   Account account,
                                   Map<String, String[]> syncKeys,
                                   StopWatch watch,
                                   String context,
                                   String messageStoreBucket,
                                   ActiveSyncController controller,
                                   Set<String> storeMessageIds) throws MessageStoreException, DALException {
        if (log.isTraceEnabled())
            log.trace(String.format("getMailFirstTime(account=%s, context=%s ... )", String.valueOf(account), String.valueOf(context)));

        StopWatchUtils.newTask(watch, "getMailFirstTime", context, log);
        Date lastMessageReceivedDate = account.getLast_received_date();
        int newMessages = 0;
        int folderDepth = 0;
        int maxEmailCount = getMaxEmailCount();
        XobniSaveProcess xobniSaveProcess = new XobniSaveProcess(account,messageStoreBucket,watch,context,false);
        for (com.zynku.sync.activesync.model.Folder folder : folders) {
            List<ApplicationData> emails = getEmails(watch, context, syncKeys, folder, controller);

            if (emails != null) {
                folderDepth += emails.size();

                ActiveSyncSupport.SortOrder sortOrder = getSortOrder(emails);

                for (int messageNumber = 1; messageNumber <= emails.size(); messageNumber++) {
                    StopWatchUtils.newTask(watch, "Processing email #" + messageNumber, context, log);

                    ApplicationData email = getNextEmail(sortOrder, messageNumber, emails);

                    Date mailSentDate = getDateFromString(email.get("DateReceived"));
                    if (maxEmailCount > 0) {
                        String messageId = getMessageId(email);

                        if (processMessage(account, messageNumber, storeMessageIds, messageStoreBucket, email, messageId, context, watch, true, controller,xobniSaveProcess)) {
                            if (messageNumber == 1) {
                                lastMessageReceivedDate = mailSentDate;
                            }
                            newMessages++;
                        }

                        maxEmailCount--;
                    } else {
                        getMessageIdStore().addMessage(account.getId(), messageStoreBucket, "" + (mailSentDate != null ? mailSentDate.getTime() : 0), account.getCountry());
                    }
                }
                StopWatchUtils.newTask(watch, "updateAccount", context, log);
                account.setActive_sync_key(getSyncKey(syncKeys));
                updateAccountData(folderDepth, newMessages, lastMessageReceivedDate, account);
            }
        }
        return new int[]{newMessages, folderDepth};
    }

}
