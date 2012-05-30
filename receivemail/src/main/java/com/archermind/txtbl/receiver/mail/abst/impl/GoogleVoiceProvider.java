package com.archermind.txtbl.receiver.mail.abst.impl;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.exception.SystemException;
import com.archermind.txtbl.receiver.mail.abst.Provider;
import com.archermind.txtbl.receiver.mail.bean.GoogleVoiceProcess;
import com.archermind.txtbl.receiver.mail.bean.GoogleVoiceProcessMap;
import com.archermind.txtbl.receiver.mail.dal.DALDominator;
import com.archermind.txtbl.receiver.mail.support.FolderHelper;
import com.archermind.txtbl.receiver.mail.support.FolderHelperFactory;
import com.archermind.txtbl.receiver.mail.support.GoogleVoiceFolder;
import com.archermind.txtbl.receiver.mail.support.GoogleVoiceProviderSupport;
import com.archermind.txtbl.receiver.mail.utils.ProviderStatistics;
import com.archermind.txtbl.utils.FinalizationUtils;
import com.archermind.txtbl.utils.ReceiverUtilsTools;
import com.archermind.txtbl.utils.StopWatch;
import com.archermind.txtbl.utils.StopWatchUtils;
import org.jboss.logging.Logger;

import javax.mail.Message;
import javax.mail.Store;
import java.io.IOException;
import java.util.*;

public class GoogleVoiceProvider implements Provider {

    protected GoogleVoiceProviderSupport support;

    private static final Logger log = Logger.getLogger(GoogleVoiceProvider.class);

    private static final GoogleVoiceProcessMap accountProcessMap = new GoogleVoiceProcessMap();
    private static ProviderStatistics statistics = new ProviderStatistics();


    public GoogleVoiceProvider(GoogleVoiceProviderSupport support) {
        this.support = support;
    }


    public int receiveMail(Account account) {

        String processKey = account.getUser_id() + "_" + account.getName();

        String context = String.format("account=%s, uid=%s, email=%s", account.getId(), account.getUser_id(), account.getName());

        StopWatch watch = new StopWatch("mailcheck " + context);

        log.info(String.format("receiving email %s", context));

        //Before we do anything, lets check if this person's device is on and being used
        //if it has been off for a long time we won't check it
        if (!support.isAccountBeingUsed(account)) {
            log.info(String.format("Skipping mailcheck, this device is not being used for account %s", context));
            return 0;
        }

        if (!DALDominator.loadAccountLatestReceiveInfo(account)) {
            log.warn(String.format("account has been removed by email checks continue for " + context));
            // this means that we can't find account anymore - probably to do with deletion
            return 0;
        }
        GoogleVoiceProcess process;
        //Double check this account has a current password/credentials
        if (support.exceededMaximumLoginFailures(account)) {
            log.warn(String.format("exceeded maximum login failures with %d attempts for %s", account.getLogin_failures(), context));
            process = accountProcessMap.getProcessMap().remove(processKey);
            if (process != null) process.disconnect();
            return 0;
        }

        //This gets us an existing connection, or creates a new one
        //it also manages time to live, so old connections are refreshed periodically
        try {
            process = GoogleVoiceProcessMap.getProcess(account);
        } catch (IOException e) {
            log.warn(String.format("Unable to get process, likely due to auth failure for %s", context));
            try {
                support.handleLoginFailures(context, account);
            } catch (DALException ignored) {

            }
            return 0;
        }


        int newMessages = 0;
        int folderDepth = 0;
        int examined = 0;
        Date lastMessageReceivedDate = null;

        GoogleVoiceFolder inbox = null;

        Store store = null;
        try {
            inbox = support.connect(account, context, watch, process);


            StopWatchUtils.newTask(watch, "Getting email count in folder", context, log);
            FolderHelper folderHelper = FolderHelperFactory.getFolderHelper(account, inbox);
            folderDepth = folderHelper.getFolderDepth(); // this is purely stats, if it proves to be expensive we shold nix it

            StopWatchUtils.newTask(watch, "Getting messageStore bucket", context, log);
            String messageStoreBucket = support.getMessageIdStore().getBucket(account.getId()); // calculate once

            if (account.getLast_received_date() == null) {
                StopWatchUtils.newTask(watch, "Start first time receive", context, log);
                newMessages = handleFirstTime(account, inbox, messageStoreBucket, folderDepth, context, watch, folderHelper);

            } else {
                StopWatchUtils.newTask(watch, "getMessages", context, log);
                Message[] messages = inbox.getMessages();
                examined = messages.length;

                StopWatchUtils.newTask(watch, "getStoreMessages", context, log);
                Set<String> storedMessageIds = support.getMessageIdStore().getMessages(account.getId(), account.getCountry(), context, watch);
                if (log.isDebugEnabled())
                    log.debug(String.format("store bucket is %s for %s", messageStoreBucket, context));

                StopWatchUtils.newTask(watch, "processMessages", context, log);

                for (Message message : messages) {
                    StopWatchUtils.newTask(watch, String.format("msgNum=%s, getUID", message.getMessageNumber()), context, log);

                    if (support.processMessage(account, message, message.getMessageNumber(), String.valueOf(inbox.getUID(message)), inbox, storedMessageIds, messageStoreBucket, context, watch, folderHelper)) {
                        if (lastMessageReceivedDate == null || lastMessageReceivedDate.before(message.getReceivedDate())) {
                            lastMessageReceivedDate = message.getSentDate();
                            if (log.isDebugEnabled())
                                log.debug(String.format("Setting received date to %s for subject %s ", lastMessageReceivedDate, message.getSubject()));
                        }

                        newMessages++;

                        if (newMessages > support.getMaximumMessagesToProcess()) {
                            break;
                        }
                    }
                }

                StopWatchUtils.newTask(watch, "updateAccount", context, log);
                support.updateAccount(account, null, newMessages, folderDepth, lastMessageReceivedDate);
            }

        } catch (Throwable e) {
            throw new SystemException(String.format("unexpected failure during receive mail for %s", context), e);
        }
        finally {
            StopWatchUtils.newTask(watch, String.format("closeConnection (%d new mails, %d in folder, %d examined)", newMessages, folderDepth, examined), context, log);

            FinalizationUtils.close(inbox);
            FinalizationUtils.close(store);

            watch.stop();

            log.info(ReceiverUtilsTools.printWatch(watch, folderDepth, newMessages, false));

            String summary = statistics.enterStats(GoogleVoiceProvider.class.getName(), folderDepth, newMessages, watch.getTotalTimeMillis());

            log.info(String.format("completed mailcheck for %s, folderDepth=%d, newMessages=%d, examined=%d, time=%sms [%s]", context, folderDepth, newMessages, examined, watch.getTotalTimeMillis(), summary));
        }

        return newMessages;


    }


    /**
     * @param account
     * @param inbox
     * @param messageStoreBucket
     * @param context
     * @param watch
     * @return
     * @throws Exception
     */
    private int handleFirstTime(Account account, GoogleVoiceFolder inbox, String messageStoreBucket, int folderDepth, String context, StopWatch watch, FolderHelper folderHelper) throws Exception {
        log.info(String.format("handling first mailcheck for %s", context));

        Collection<Integer> messages = support.getFirstTimeMessages(inbox, context);
        if(log.isTraceEnabled())
            log.trace(String.format("messages size=%s", (null != messages ? messages.size() : "null")));

        if(messages == null) {
            return 0;
        }

        int newMessages = 0;

        if (messages.size() > 0)
        {
            Set<String> storedMessageIds = new HashSet<String>();

            Date lastMessageReceivedDate = null;

            for (Integer messageNumber : messages)
            {
                Message message = inbox.getMessage(messageNumber);

                if (lastMessageReceivedDate == null || lastMessageReceivedDate.before(message.getReceivedDate())) {

                    lastMessageReceivedDate = message.getSentDate();
                    if (log.isDebugEnabled())
                        log.debug(String.format("Setting received date to %s for subject %s ", lastMessageReceivedDate, message.getSubject()));
                }

                String messageId = String.valueOf(inbox.getUID(message));

                if (support.processMessage(account, message, messageNumber, messageId, inbox, storedMessageIds, messageStoreBucket, context, watch, folderHelper)) {
                    newMessages++;
                }
            }

            support.updateAccount(account, null, 0, folderDepth, lastMessageReceivedDate);

        }
        return newMessages;
    }


}
