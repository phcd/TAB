package com.archermind.txtbl.receiver.mail.support;

import com.archermind.txtbl.dal.business.impl.UserService;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.mail.store.MessageStoreException;
import com.archermind.txtbl.receiver.mail.utils.ProviderStatistics;
import com.archermind.txtbl.utils.*;
import com.archermind.txtbl.utils.xobni.XobniSyncUtil;
import com.archermind.txtbl.utils.xobni.XobniUtil;
import com.sun.mail.imap.IMAPFolder;
import org.jboss.logging.Logger;

import javax.mail.*;
import java.util.*;

public class XobniProviderSupport extends NewProviderSupport {
    protected static final Logger log = Logger.getLogger(XobniProviderSupport.class);
    private static final int maxMessagesToProcess = Integer.valueOf(SysConfigManager.instance().getValue("xobni.max.messages", "50000"));
    private static final int xobniBatchSize = Integer.valueOf(SysConfigManager.instance().getValue("xobni.batch.size","1000"));

    private static final String xobniAuthFailFrom = SysConfigManager.instance().getValue("xobni.auth.fail.from", "ops@getpeek.com");
    private static final String xobniAuthFailTo = SysConfigManager.instance().getValue("xobni.auth.fail.to", "webdev-alerts@xobni.com");

    private static final boolean xobniEnableAttachments = Boolean.valueOf(SysConfigManager.instance().getValue("xobni.enable.attachments", "false"));
    private static final int XOBNI_SYNC_ERROR = -1;

    protected static ProviderStatistics statistics = new ProviderStatistics();

    public int handleFirstTime(final Account account, FolderHelper folderHelper, int folderDepth, String messageStoreBucket, final Set<String> storedMessageIds, final String context, StopWatch watch) throws Exception{
        int totalMessagesProcessed = 0;
        int previousMessageCount = account.getMessage_count();
        int totalMessagesUploadedSoFar = previousMessageCount;

        StopWatch firstTimeWatch = new StopWatch("first time download " + context);

        XobniSaveProcess emailProcess = getEmailProcess(account, messageStoreBucket, context, watch);
        if(!emailProcess.shouldProcess()) {
            log.info("Should Process is false for " + context);
            return 0;
        }

        List<MessageValidator> messageValidators = new ArrayList<MessageValidator>();
        messageValidators.add(new DoNothingOnFailureMessageValidator() {
            public Message validate(Message message, int messageNumber, String messageId) throws Exception {
                if(storedMessageIds.contains(messageId)) {
                    log.debug("message with messageId " + messageId + " already present for account " + context);
                    return null;
                }
                return message;
            }
        });
        folderHelper.addMessageValidators(messageValidators);

        int totalMessageCount = folderHelper.getTotalMessageCount();
        int totalNumberToProcess = maxMessagesToProcess < totalMessageCount ? maxMessagesToProcess : totalMessageCount;

        Folder folder;
        while(((folder = folderHelper.next()) != null) && (totalMessagesProcessed < totalNumberToProcess)) {
            String folderContext = context + " ,folderName=" + folder.getFullName();
            IMAPFolder imapFolder = (IMAPFolder) folder;

            int messagesInInbox = imapFolder.getMessageCount();

            if(messagesInInbox == 0) {
                updateAccount(account, null, 0, folderDepth, null);
                log.info(String.format("zero messages in %s folder for %s", imapFolder.getName(), folderContext));
                continue;
            }

            int totalNumberLeftToProcess = totalNumberToProcess - totalMessagesProcessed;
            int numberToProcess = getNumberToProcess(messagesInInbox, totalNumberLeftToProcess, totalNumberToProcess, totalMessageCount, true);

            int newMessages = 0;
            int length = 0;
            int messagesProcessedForFolder = 0;

            boolean reverseOrder =  areMessagesInReverseOrder(imapFolder);
            FetchProfile fp = getFetchProfile();
            while(messagesProcessedForFolder < numberToProcess){

                try{
                    if(!emailProcess.shouldProcess()) {
                        break;
                    }

                    int batchSize = XobniBatchUtil.INSTANCE.getBatchSize(xobniBatchSize, messagesProcessedForFolder, messagesInInbox);
                    int startIndex = XobniBatchUtil.INSTANCE.getStartIndex(messagesProcessedForFolder, messagesInInbox, reverseOrder);
                    int endIndex = XobniBatchUtil.INSTANCE.getEndIndex(messagesProcessedForFolder, batchSize, messagesInInbox, reverseOrder);
                    if(startIndex > endIndex) {
                        int tempIndex = startIndex;
                        startIndex = endIndex;
                        endIndex = tempIndex;
                    }
                    log.info(String.format("about to fetch batch for start index %d end index %d batch size %d messages in inbox %d order %b for %s",startIndex,endIndex,batchSize,messagesInInbox,reverseOrder,context));
                    StopWatchUtils.newTask(firstTimeWatch, "get messages", folderContext, log);
                    Message[] batch = imapFolder.getMessages(startIndex,endIndex);
                    length = batch.length;

                    log.info(String.format("total number of emails in this batch %d total messages %d order %b %s", batch.length, messagesProcessedForFolder, reverseOrder, context));

                    if(totalMessagesProcessed < previousMessageCount) {
                        batch = filterMessagesAlreadySaved(account, imapFolder, batch, storedMessageIds);
                    }

                    if(batch.length == 0) {
                        log.info("no. of emails after filtering is 0 : " + folderContext);
                    } else {
                        StopWatchUtils.newTask(firstTimeWatch, "fetch messages", folderContext, log);
                        imapFolder.fetch(batch, fp);

                        StopWatchUtils.newTask(firstTimeWatch,"Process mails", folderContext, log);
                        newMessages = processMessageFirstTime(account, messageStoreBucket, batch, imapFolder, folderDepth, folderContext, firstTimeWatch, messageValidators, emailProcess, (totalMessagesProcessed + length) >= totalNumberToProcess, totalMessagesUploadedSoFar);
                        if(newMessages == XOBNI_SYNC_ERROR) {
                            log.info("Stopping fetch due to error during sync for " + context);
                            break;
                        }
                    }
                } catch(Exception e) {
                    log.error(String.format("errors in first time downloading %s %s",folderContext,e.toString()));
                    updateAccount(account, null, length, folderDepth, null);

                } finally {
                    if(firstTimeWatch.isRunning()) {
                        firstTimeWatch.stop();
                    }
                    String summary = statistics.enterStats(XobniProviderSupport.class.getName(), folderDepth, messagesProcessedForFolder, firstTimeWatch.getTotalTimeMillis());
                    messagesProcessedForFolder += length;
                    totalMessagesProcessed += length;
                    totalMessagesUploadedSoFar += newMessages;
                    log.info(String.format( "completed mailcheck for %s, folderDepth=%d, newMessages=%d time=%sms [%s]", folderContext, folderDepth, newMessages, firstTimeWatch.getTotalTimeMillis(), summary));
                    log.info(firstTimeWatch.prettyPrint());
                }
            }
        }
        return totalMessagesProcessed;
    }

    private int getNumberToProcess(int messagesInInbox, int totalNumberLeftToProcess, int totalNumberToProcess, int totalMessageCount, boolean prorate) {
        if(prorate) {
            return (int) Math.ceil(((float)messagesInInbox  * totalNumberToProcess)/totalMessageCount);
        }
        return (messagesInInbox < totalNumberLeftToProcess) ? messagesInInbox : totalNumberLeftToProcess;
    }

    private FetchProfile getFetchProfile() {
        FetchProfile fp = new FetchProfile();
        fp.add(UIDFolder.FetchProfileItem.UID);
        fp.add(FetchProfile.Item.ENVELOPE);
        if(xobniEnableAttachments) {
            fp.add(FetchProfile.Item.CONTENT_INFO);
        }
        fp.add(HeaderMessageIdsUtil.MESSAGE_ID);
        return fp;
    }


    private Message[] filterMessagesAlreadySaved(Account account, IMAPFolder folder, Message[] messages, Set<String> storedIds) throws MessagingException {
        fetchIds(account, folder, messages);

        List<Message> filteredMessages = new ArrayList<Message>();
        for (Message message : messages) {
            String uid = getId(account, folder, message);
            if(!storedIds.contains(uid)) {
                filteredMessages.add(message);
            }
        }
        return filteredMessages.toArray(new Message[filteredMessages.size()]);
    }

    public int processMessageFirstTime(Account account, String messageStoreBucket, Message[] messages, IMAPFolder inbox, int folderDepth, String context, StopWatch watch, List<MessageValidator> messageValidators, XobniSaveProcess xobniSaveProcess, boolean finalBatch, int totalMessagesUploadedSoFar)  throws Exception{
        log.info(String.format("Messages count including drafts %d %s", messages.length, context));

        StopWatchUtils.newTask(watch, "rip out draft messages and convert to pojo", context, log);
        Date lastMessageReceivedDate = null;
        int skipMessages = 0;
        for(int i=0; i < messages.length; i++) {
            Message message = messages[i];
            String messageId = getId(account, inbox, message);
            message = valiateAndPrepMessage(account, messageStoreBucket, message, message.getMessageNumber(), messageId, messageValidators);
            if (message != null) {
                Date messageDate = getMessageDate(message);
                if (lastMessageReceivedDate == null || lastMessageReceivedDate.before(messageDate)) {
                    lastMessageReceivedDate = message.getReceivedDate();
                }
                xobniSaveProcess.process(message, message.getMessageNumber(), messageId);
            } else {
                messages[i] = null;
                skipMessages++;
            }
        }

        int newMessages = messages.length-skipMessages;

        if(!xobniSaveProcess.complete(finalBatch, folderDepth, totalMessagesUploadedSoFar)) {
            return XOBNI_SYNC_ERROR;
        }

        log.info(String.format("Messages count without drafts %d %s", messages.length - skipMessages, context));
        updateAccount(account, null, newMessages, folderDepth, lastMessageReceivedDate);

        return newMessages;
    }

    public boolean shouldProcessMigrated() {
        return false;
    }

    public void notifyAccountLock(Account account, String context) {
        boolean succ = XobniSyncUtil.INSTANCE.notifyAccountLock(account, new UserService().getXobniAccountByUserID(account.getUser_id()));
        log.info("Notifying xobni for " + context + " : " + succ);
    }

    protected void sendAccountLockedNotificationEm(Account account) {
        String message = account.getLoginFailureNotificationMessage();
        String subject = "Credentials invalid for " + account.getName();
        Mailer.getInstance().sendMail(xobniAuthFailFrom, xobniAuthFailTo, null, null, message, subject);
    }

    public boolean isFirstTime(Account account) {
        return super.isFirstTime(account) || XobniUtil.isInitialFetchInProgress(account, maxMessagesToProcess, xobniBatchSize);
    }


    protected List<MessageValidator> getMessageValidators(final Account account, final Folder folder, final Set<String> storeMessageIds, final String storeBucket, final String context) {
        ArrayList<MessageValidator> messageValidators = new ArrayList<MessageValidator>();
        messageValidators.add(new DoNothingOnFailureMessageValidator() {
            public Message validate(Message message, int messageNumber, String messageId) throws Exception {
                if (isMessageAlreadyProcessed(messageId, account, storeBucket, storeMessageIds)) {
                    return null;
                }
                return message;
            }

            @Override
            public void handleFailure(Account account, String messageStoreBucket, String messageId) throws MessageStoreException {
                //do nothing
            }
        });
        return messageValidators;
    }

    @Override
    public EmailProcess getEmailProcess(Account account, Set<String> storedMessageIds, String messageStoreBucket, String context, StopWatch watch) {
        return getEmailProcess(account, messageStoreBucket, context, watch);
    }

    private XobniSaveProcess getEmailProcess(Account account, String messageStoreBucket, String context, StopWatch watch) {
        return new XobniSaveProcess(account, messageStoreBucket, watch, context, xobniEnableAttachments);
    }
}
