package com.archermind.txtbl.receiver.mail.abst.impl;

import com.archermind.txtbl.authenticator.Authenticator;
import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.exception.SystemException;
import com.archermind.txtbl.mail.store.MessageStoreException;
import com.archermind.txtbl.receiver.mail.abst.Provider;
import com.archermind.txtbl.receiver.mail.dal.DALDominator;
import com.archermind.txtbl.receiver.mail.support.*;
import com.archermind.txtbl.receiver.mail.utils.ProviderStatistics;
import com.archermind.txtbl.utils.*;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import org.jboss.logging.Logger;

import javax.mail.*;
import javax.mail.search.AndTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;
import java.util.*;

public class NewImapProvider implements Provider {
    private static final Logger log = Logger.getLogger(NewImapProvider.class);
    protected static ProviderStatistics statistics = new ProviderStatistics();

    private Map<String, Integer> hoursToGoBackInSearch = new HashMap<String, Integer>();

    NewProviderSupport support;
    Authenticator authenticator;

    private static boolean shouldReconcileIds = Boolean.valueOf(SysConfigManager.instance().getValue("shouldReconcileIds", "false"));
    private static Long reconciliationIntervalInDays = Long.valueOf(SysConfigManager.instance().getValue("reconciliationIntervalInDays", "6"));

    public NewImapProvider(NewProviderSupport support, Authenticator authenticator) {
        this.support = support;
        this.authenticator = authenticator;
    }

    public int receiveMail(final Account account) {
        String context = String.format("account=%s, uid=%s, email=%s", account.getId(), account.getUser_id(), account.getName());
        StopWatch watch = new StopWatch("mailcheck " + context);
        log.info(String.format("receiving email %s", context));

        int newMessages = 0;
        int folderDepth = 0;

        Store store = null;
        FolderHelper folderHelper = null;

        try {
            StopWatchUtils.newTask(watch, "loadAccountLatestReceiveInfo", context, log);
            if (!DALDominator.loadAccountLatestReceiveInfo(account)) {
                log.warn(String.format("account has been removed by email checks continue for %s", context));
                // this means that we can't find account anymore - probably to do with deletion
                return 0;
            }

            if(XobniFirstTimeHandler.INSTANCE.handleFirstTime(account, support, context)) {
                return 0;
            }

            if (support.exceededMaximumLoginFailures(account)) {
                log.info(String.format("exceeded maximum login failures with %d attempts for %s", account.getLogin_failures(), context));
                return 0;
            }

            StopWatchUtils.newTask(watch, "getStoreMessages", context, log);
            Set<String> storedMessageIds = support.getMessageIdStore().getMessages(account.getId(), account.getCountry(), context, watch);

            store = authenticator.getStore(account, context, watch, support);
            folderHelper = FolderHelperFactory.getFolderHelper(authenticator, account, store, context, watch);
            if(folderHelper == null) {
                log.info(String.format("unable to connect for %s", context));
                return 0;
            }

            folderDepth = folderHelper.getFolderDepth();
            log.info(String.format("folder depth old %d new %d folder name %s %s",account.getFolder_depth(),folderDepth,account.getFolderNameToConnect(),context));
            account.setFolder_depth(folderDepth);

            StopWatchUtils.newTask(watch, "getMessageStoreBucket", context, log);
            String messageStoreBucket = support.getMessageIdStore().getBucket(account.getId()); // calculate once

            boolean isFirstTime = isFirstTime(account);
            boolean migratedAccount = isMigratedAccount(account);

            if(isFirstTime) {
                StopWatchUtils.newTask(watch, "handleFirstTime", context, log);
                newMessages = handleFirstTime(account, messageStoreBucket, storedMessageIds, folderHelper, folderDepth, context, watch);
            } else if(migratedAccount) {
                StopWatchUtils.newTask(watch, "handleMigratedAccount", context, log);
                handleMigratedAccount(account, folderHelper, folderDepth, messageStoreBucket, context);
            } else {
                StopWatchUtils.newTask(watch, "getMessages", context, log);
                newMessages = getMessages(account, messageStoreBucket, storedMessageIds, folderHelper, folderDepth, context, watch);
            }
        } catch (Throwable e) {
            throw new SystemException(String.format("unexpected failure during receive mail for %s", context), e);
        } finally {
            StopWatchUtils.newTask(watch, String.format("closeConnection (%d new mails, %d in folder)", newMessages, folderDepth), context, log);

            //TODO: refactor into authenticator
            if(folderHelper != null) {
                folderHelper.close();
            }
            FinalizationUtils.close(store);

            StopWatchUtils.newTask(watch, "logMailCheckEvent", context, log);

            watch.stop();

            log.info(ReceiverUtilsTools.printWatch(watch, folderDepth, newMessages, false));

            String summary = statistics.enterStats(NewImapProvider.class.getName(), folderDepth, newMessages, watch.getTotalTimeMillis());
            log.info(String.format("completed mailcheck for %s, folderDepth=%d, newMessages=%d time=%sms [%s]", context, folderDepth, newMessages, watch.getTotalTimeMillis(), summary));
        }

        return newMessages;
    }

    private boolean isMigratedAccount(Account account) {
        return support.isMigratedAccount(account);
    }

    protected boolean isFirstTime(Account account) {
        return support.isFirstTime(account);
    }

    private int getMessages(Account account, String messageStoreBucket, Set<String> storedMessageIds, FolderHelper folderHelper, int folderDepth, String context, StopWatch watch) throws Throwable {
        int newMessages = 0;
        AndTerm andTerm = new AndTerm(new SearchTerm[]{new ReceivedDateTerm(ComparisonTerm.GT, getDateOffset(account))});
        try {
            Date lastMessageReceivedDate = null;
            StopWatchUtils.newTask(watch, "getEmailProcess", context, log);
            EmailProcess emailProcess = support.getEmailProcess(account, storedMessageIds, messageStoreBucket, context, watch);
            if(!emailProcess.shouldProcess()) {
                log.info("Should Process is false for " + context);
                return 0;            
            }
            List<String> messageIds = new ArrayList<String>();
            int examined = 0;

            Folder folder;
            while((folder = folderHelper.next()) != null) {
                String folderContext = context + " ,folderName=" + folder.getFullName();
                IMAPFolder imapFolder = (IMAPFolder) folder;

                log.info(String.format("searching with search term %s for %s", getDateOffset(account), folderContext));
                Message[] messages = imapFolder.search(andTerm);
                examined += messages.length;

                StopWatchUtils.newTask(watch, "getMessageUIDs", folderContext, log);
                support.fetchIds(account, imapFolder, messages);

                if(log.isDebugEnabled())
                    log.debug(String.format("store bucket is %s for %s", messageStoreBucket, folderContext));

                StopWatchUtils.newTask(watch, "processMessages", folderContext, log);

                for (Message message : messages) {
                    String messageId = String.valueOf(imapFolder.getUID(message));
                    try {
                        messageIds.add(messageId);

                        Date messageDate = support.getMessageDate(message);

                        if (support.processMessage(account, message, message.getMessageNumber(), messageId, imapFolder, storedMessageIds, messageStoreBucket, folderContext, watch, folderHelper, emailProcess)) {
                            if (lastMessageReceivedDate == null || lastMessageReceivedDate.before(messageDate)) {
                                lastMessageReceivedDate = messageDate;
                            }

                            newMessages++;

                            if (newMessages > support.getMaximumMessagesToProcess()) {
                                break;
                            }
                        }
                        //Added by Dan to clear high memory/caching by Javamail
                        ((IMAPMessage) message).invalidateHeaders();
                    } catch (MessageRemovedException e) {
                        log.warn("message removed - " + messageId + " : " + folderContext);
                    }
                }
            }

            emailProcess.complete();

            if(!emailProcess.shouldStopProcess()) {

                if (shouldReconcileIds && isTimeToReconcile(account)) {
                    StopWatchUtils.newTask(watch, "reconcile", context, log);

                    boolean reconciliationSuccess = false;
                    try {
                        reconciliationSuccess = support.getMessageIdStore().reconcileIds(account.getId(), messageStoreBucket, IdUtil.encodeMessageIds(messageIds), account.getCountry());
                    } catch (Exception e) {
                        log.error("reconciliation for " + context, e);
                    }

                    account.setLast_reconciliation(new Date(System.currentTimeMillis()));

                    log.info(String.format("reconcilation - result - %s for %s", reconciliationSuccess, context));
                }

                StopWatchUtils.newTask(watch, "updateAccount", context, log);
                support.updateAccount(account, null, newMessages, folderDepth, lastMessageReceivedDate);
                log.info(String.format("message stats for %s, folderDepth=%d, newMessages=%d, examined=%d", context, folderDepth, newMessages, examined));
            }

        } catch (Throwable e) {
            //TODO - Paul - need to move out
            //check for search error so we can get log of it
            String message = e != null ? e.getMessage() : null;
            if (message != null && message.contains("A5 BAD SEARCH")) {
                log.error(String.format("error while searching with search term %s for %s", andTerm.toString(), context));

            }
            throw e;
        }
        return newMessages;
    }

    private boolean isTimeToReconcile(Account account) {
        return account.getLast_reconciliation() == null || (System.currentTimeMillis() - account.getLast_reconciliation().getTime()) > 1000l * 60 * 60 * 24 * reconciliationIntervalInDays;
    }

    /**
     * @param account
     * @param messageStoreBucket
     * @param storedMessageIds
     *@param context
     * @param watch   @return   @throws Exception
     */
    private int handleFirstTime(Account account, String messageStoreBucket, Set<String> storedMessageIds, FolderHelper folderHelper, int folderDepth, String context, StopWatch watch) throws Exception {
        int newMessages = support.handleFirstTime(account, folderHelper, folderDepth, messageStoreBucket, storedMessageIds, context, watch);

        if(support.shouldProcessMigrated()){
            // store last 1 day worth of IDs
            handleMigratedAccount(account, folderHelper, folderDepth, messageStoreBucket, context);
        }

        log.info(String.format("handled first mailcheck for %s - %s messages", context, newMessages));

        return newMessages;
    }

    /**
     * @param folder
     * @param account
     * @param bucket
     * @throws MessagingException
     * @throws MessageStoreException
     */
    private void handleMigratedAccount(Account account, FolderHelper folderHelper, int folderDepth, String bucket, String context) throws MessagingException, MessageStoreException, DALException {
        Date lastMessageReceivedDate = null;
        List<String> ids = new ArrayList<String>();

        Folder folder;
        while((folder = folderHelper.next()) != null) {
            String folderContext = context + " ,folderName=" + folder.getFullName();
            IMAPFolder imapFolder = (IMAPFolder) folder;

            int daysToGoBackOnMigration = Integer.parseInt(SysConfigManager.instance().getValue("imapMigrationDaysToGoBack", "1"));

            Message[] messages = imapFolder.search(new AndTerm(new SearchTerm[]{new ReceivedDateTerm(ComparisonTerm.GT, new Date(System.currentTimeMillis() - 1000l * 60 * 60 * 24 * daysToGoBackOnMigration))}));

            if(log.isDebugEnabled())
                log.debug(String.format("found %d messages to add to id store as part of migration for %s", messages.length, folderContext));

            support.fetchIds(account, imapFolder, messages);

            if(log.isDebugEnabled())
                log.debug(String.format("fetched ids for migration eligible messages for %s", folderContext));

            if (messages.length == 0) {
                lastMessageReceivedDate = new Date(System.currentTimeMillis());
            } else {
                for (Message message : messages) {
                    if (lastMessageReceivedDate == null || lastMessageReceivedDate.before(message.getReceivedDate())) {
                        lastMessageReceivedDate = message.getReceivedDate();
                    }

                    // the formatting below is necessary to stay consistent with POP bulk imports
                    ids.add(message.getMessageNumber() + " " + support.getId(account, imapFolder, message));
                }
            }
        }

        support.getMessageIdStore().addMessageInBulk(account.getId(), bucket, IdUtil.encodeMessageIds(ids), account.getCountry());

        if(log.isDebugEnabled())
            log.debug(String.format("migrated messages with last receive date of %s for %s", lastMessageReceivedDate, context));

        support.updateAccount(account, null, 0, folderDepth, lastMessageReceivedDate);
    }

    /**
     * Determines date offset of how far back to go in search
     *
     * @param account
     * @return
     */
    public Date getDateOffset(Account account)
    {
        if (account.getLast_received_date() != null)
        {
            if (!hoursToGoBackInSearch.containsKey(account.getReceiveProtocolType()))
            {
                hoursToGoBackInSearch.put(account.getReceiveProtocolType(), Integer.valueOf(SysConfigManager.instance().getValue(account.getReceiveProtocolType() + ".hoursToBackInSearch", "4")));
            }

            int hours = hoursToGoBackInSearch.get(account.getReceiveProtocolType());

            if(log.isDebugEnabled())
                log.debug(String.format("imapHoursToBackInSearch=%d for protocol %s", hours, account.getReceiveProtocolType()));

            return new Date(System.currentTimeMillis() - 1000l * 60 * 60 * hours);
        }
        else
        {
            return account.getRegister_time();
        }
    }


}
