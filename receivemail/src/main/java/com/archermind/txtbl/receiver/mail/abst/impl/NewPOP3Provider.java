package com.archermind.txtbl.receiver.mail.abst.impl;

import com.archermind.txtbl.authenticator.Authenticator;
import com.archermind.txtbl.authenticator.LoginUtil;
import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.exception.SystemException;
import com.archermind.txtbl.mail.store.MessageStore;
import com.archermind.txtbl.mail.store.MessageStoreException;
import com.archermind.txtbl.mail.store.MessageStoreFactory;
import com.archermind.txtbl.receiver.mail.abst.Provider;
import com.archermind.txtbl.receiver.mail.dal.DALDominator;
import com.archermind.txtbl.receiver.mail.support.FolderHelper;
import com.archermind.txtbl.receiver.mail.support.FolderHelperFactory;
import com.archermind.txtbl.receiver.mail.support.NewProviderSupport;
import com.archermind.txtbl.receiver.mail.utils.ProviderStatistics;
import com.archermind.txtbl.receiver.mail.utils.ReconciliationUtil;
import com.archermind.txtbl.utils.*;
import com.sun.mail.pop3.POP3Folder;
import org.jboss.logging.Logger;

import javax.mail.*;
import java.io.UnsupportedEncodingException;
import java.security.Security;
import java.util.*;

// TODO: need refactoring badly

public class NewPOP3Provider implements Provider
{

    private static final Logger log = Logger.getLogger(NewPOP3Provider.class);

    private static ProviderStatistics statistics = new ProviderStatistics();

    private MessageStore messageStore = MessageStoreFactory.getStore();

    protected NewProviderSupport support;

    /**
     * Constructs new pop3 provider, initializes emails cutoff - how far back to consider messages and max message size - the biggest possible message size we should consider
     */
    public NewPOP3Provider(NewProviderSupport support)
    {
        this.support = support;
    }

    /**
     * Processes a mailcheck for a given account
     *
     * @param account
     * @return Number of new messages
     */
    public int receiveMail(final Account account)
    {
        String context = String.format("account=%s, uid=%s, email=%s", account.getId(), account.getUser_id(), account.getName());

        StopWatch watch = new StopWatch("mailcheck " + context);

        log.info(String.format("receiving email %s", context));

        //Before we do anything, lets check if this person's device is on and being used
        //if it has been off for a long time we won't check it
        if(!support.isAccountBeingUsed(account)){
            log.info(String.format("Skipping mailcheck, this device is not being used for account %s", context));
            return 0;
        }


        int newMessages = 0;
        int folderDepth = 0;
        boolean folderHashMatch = false;
        Date lastMessageReceivedDate = null;
        POP3Folder inbox = null;

        StopWatchUtils.newTask(watch, "getSession", context, log);
        Session session = Session.getInstance(getMailProperties(account));

        Store store = null;

        try
        {
            StopWatchUtils.newTask(watch, "loadAccountLatestReceiveInfo", context, log);
            if (!DALDominator.loadAccountLatestReceiveInfo(account))
            {
                log.warn(String.format( "account has been removed by email checks continue for %s", context));
                // this means that we can't find account anymore - probably to do with deletion
                return 0;
            }

            StopWatchUtils.newTask(watch, "getStore", context, log);
            store = session.getStore("pop3");

            StopWatchUtils.newTask(watch, "connect", context, log);
            if (!connect(account, store, context)) {
                return 0;
            }

            StopWatchUtils.newTask(watch, "getFolder", context, log);
            inbox = (POP3Folder) store.getFolder("INBOX");

            StopWatchUtils.newTask(watch, "openFolder", context, log);
            inbox.open(Folder.READ_ONLY);

            StopWatchUtils.newTask(watch, "uidl", context, log);
            String uidl = inbox.uidl();

            StopWatchUtils.newTask(watch, "genHash", context, log);
            String newFolderHash = HashGenerator.genHash(uidl);

            StopWatchUtils.newTask(watch, "getMessageCount", context, log);
            FolderHelper folderHelper = FolderHelperFactory.getFolderHelper(account, inbox);
            folderDepth = folderHelper.getFolderDepth();

            StopWatchUtils.newTask(watch, "compareFolderHash", context, log);

            folderHashMatch = newFolderHash.equals(account.getFolder_hash());

            if (folderHashMatch)
            {
                log.info(String.format("Folder hash hasn't changed. Skipping mailcheck for %s", context));
            }
            else
            {
                // we can load ids from store while we get messages
                StopWatchUtils.newTask(watch, "getMessageIds", context, log);
                String[] messageIds = getMessageIds(uidl);

                //Set<String> storeMessageIds = new HashSet<String>();// = getStoredMessagesTask.get(30, TimeUnit.SECONDS);

                StopWatchUtils.newTask(watch, "getStoreMessages", context, log);
                Set<String> storeMessageIds = messageStore.getMessages(account.getId(), account.getCountry(), context, watch);
                log.info(String.format( "we have %d stored messages for %s", storeMessageIds.size(), context));

                String messageStoreBucket = messageStore.getBucket(account.getId()); // calculate once
                log.info(String.format( "store bucket is %s for %s", messageStoreBucket, context));

                if (folderDepth > 0)
                {
                    boolean isFirstTime = isFirstTime(account);
                    boolean migratedAccount = isMigratedAccount(account);

                    if (isFirstTime)
                    {
                        StopWatchUtils.newTask(watch, "handleFirstTime", context, log);
                        newMessages = handleFirstTime(account, messageIds, inbox, folderHelper, messageStoreBucket, context, watch); // too ugly to include here

                        StopWatchUtils.newTask(watch, "storeIds", context, log);
                        storeIds(account, newFolderHash, newMessages, inbox, uidl, messageStoreBucket, context);
                    }
                    else
                    {
                        if (migratedAccount)
                        {
                            StopWatchUtils.newTask(watch, "handleMigratedAccount-StoreIds", context, log);
                            storeIds(account, newFolderHash, 1, inbox, uidl, messageStoreBucket, context);
                        }
                        else
                        {

                            // get anythinng new
                            for (int i = 0; i < inbox.getMessageCount() && newMessages < support.getMaximumMessagesToProcess(); i++)
                            //for (int messageNumber = inbox.getMessageCount()-1; messageNumber >=0 && newMessages < maximumMessagesToProcess; messageNumber--)
                            {
                                Integer messageNumber = i + 1;

                                String messageId = messageIds[i];

                                try{
                                    Message message = inbox.getMessage(messageNumber);

                                    if (support.processMessage(account, message, messageNumber, messageId, inbox, storeMessageIds, messageStoreBucket, context, watch, folderHelper))
                                    {
                                        Date messageDate = message.getSentDate();
                                        if(message.getSentDate()==null){
                                            if(message.getReceivedDate()!=null){
                                                messageDate = message.getReceivedDate();
                                                
                                            }
                                        }
                                        
                                        if (lastMessageReceivedDate == null || (messageDate!=null && lastMessageReceivedDate.before(messageDate)))
                                        {
                                            lastMessageReceivedDate = messageDate;
                                        }

                                        newMessages++;
                                    }



                                }catch(MessageRemovedException msgRemoved){
                                    //we want to catch this exception up here so we can store the msg id and move on
                                    messageStore.addMessage(account.getId(),messageStoreBucket,messageId, account.getCountry());
                                }

                            }

                            if (support.isTimeToReconcile(account))
                            {
                                StopWatchUtils.newTask(watch, "sendReconciliationRequest", context, log);

                                storeMessageIds.removeAll(new HashSet<String>(Arrays.asList(messageIds)));

                                ReconciliationUtil.sendReconciliationRequest(account, storeMessageIds, context);

                                account.setLast_reconciliation(new Date(System.currentTimeMillis()));
                            }

                        }
                    }
                }
            }

            // store account folder hash and number of receivin messages
            StopWatchUtils.newTask(watch, "updateAccount", context, log);
            // looks like we might have stopped short of going through the entire mailbox, don't update folder hash
            String folderHash = (newMessages == support.getMaximumMessagesToProcess()) ? account.getFolder_hash() : newFolderHash;
            support.updateAccount(account, folderHash, newMessages, folderDepth, lastMessageReceivedDate);
        } catch (Throwable e) {
            throw new SystemException(String.format("unexpected failure during receive mail for %s", context), e);
        } finally {
            StopWatchUtils.newTask(watch, String.format("closeConnection (%d new mails, %d in folder)", newMessages, folderDepth), context, log);

            FinalizationUtils.close(inbox);
            FinalizationUtils.close(store);

            StopWatchUtils.newTask(watch, "logMailCheckEvent", context, log);

            watch.stop();

            log.info(ReceiverUtilsTools.printWatch(watch, folderDepth, newMessages, !folderHashMatch));

            String summary = statistics.enterStats(NewPOP3Provider.class.getName(), folderDepth, newMessages, watch.getTotalTimeMillis());

            log.info(String.format("completed mailcheck for %s, folderDepth=%d, newMessages=%s, time=%sms [%s]", context, folderDepth, newMessages, watch.getTotalTimeMillis(), summary));

        }

        return newMessages;
    }

    private void storeIds(Account account, String newFolderHash, int newMessages, POP3Folder inbox, String messageIds, String messageStoreBucket, String context) throws DALException, MessagingException, MessageStoreException {
        long start = System.nanoTime();

        messageStore.addMessageInBulk(account.getId(), messageStoreBucket, IdUtil.enodePop3MessageIdString(messageIds), account.getCountry());

        log.debug(String.format("added %s messages to store as part of first mailcheck in %dms for %s", inbox.getMessageCount(), (System.nanoTime() - start) / 1000000, context));

        // now we need to add all message ids to this user store so that next time we won't process those messages as new
        support.updateAccount(account, newFolderHash, newMessages, inbox.getMessageCount(), null);
    }

    private String[] getMessageIds(String uidl) throws UnsupportedEncodingException {
        String[] list = uidl.split("\r\n");
        for (int i = 0; i < list.length; i++) {
            list[i] = list[i].substring(list[i].indexOf(" ") + 1);
        }
        return list;
    }


    /**
     * Handles first time receipt of messages
     *
     * @param account
     * @param inbox
     * @param messageStoreBucket
     * @throws Exception
     */
    private int handleFirstTime(Account account, String[] messageIds, POP3Folder inbox, FolderHelper folderHelper, String messageStoreBucket, String context, StopWatch watch) throws Exception
    {
        log.debug(String.format( "handling first mailcheck for %s", context));


        Collection<Integer> messages = support.getFirstTimeMessages(inbox, context);
        int newMessages = 0;

        if (messages.size() > 0)
        {
            Set<String> storedMessageIds = new HashSet<String>();

            for (Integer messageNumber : messages)
            {
                Message message = inbox.getMessage(messageNumber);

                if (support.processMessage(account, message, messageNumber, messageIds[messageNumber - 1], inbox, storedMessageIds, messageStoreBucket, context, watch, folderHelper))
                {
                    newMessages++;
                }
            }
        }

        return newMessages;
    }

    private boolean isMigratedAccount(Account account)
    {
        return account.getLast_mailcheck() != null && account.getMessage_count() == 0;
    }

    private boolean isFirstTime(Account account)
    {
        return account.getLast_mailcheck() == null && account.getMessage_count() == 0;
    }



    private boolean connect(Account account, Store store, String context) throws MessagingException, DALException {
        try {
            if (support.exceededMaximumLoginFailures(account)) {
                log.warn(String.format( "exceeded maximum login failures with %d attempts for %s", account.getLogin_failures(), context));
                return false;
            } else {
                store.connect(account.getReceiveHost(), account.getLoginName(), account.getPassword());
                return true;
            }
        } catch (AuthenticationFailedException e) {
            //TODO: - need to create pop3 authenticator
            if (LoginUtil.INSTANCE.isLoginFailure(e)) {
                support.handleLoginFailures(context, account);
                log.warn(String.format( "aborting receiving at processing of status messages for %s due to authentication problem", context));
                return false;
            } else {
                throw e;
            }
        }
    }
    private Properties getMailProperties(Account account)
    {
        Properties props = new Properties();
        props.setProperty("mail.pop3.port", account.getReceivePort());
        props.setProperty("mail.pop3.connectiontimeout", "60000");
        if ("ssl".equals(account.getReceiveTs()))
        {
            Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
            props.put("mail.pop3.socketFactory.class", "com.archermind.txtbl.mail.DummySSLSocketFactory");
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

}
