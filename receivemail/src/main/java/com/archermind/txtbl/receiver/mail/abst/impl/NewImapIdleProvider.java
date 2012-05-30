package com.archermind.txtbl.receiver.mail.abst.impl;

import com.archermind.txtbl.authenticator.Authenticator;
import com.archermind.txtbl.dal.business.impl.UserService;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.XobniAccount;
import com.archermind.txtbl.receiver.mail.bean.IdleProcess;
import com.archermind.txtbl.receiver.mail.bean.IdleProcessMap;
import com.archermind.txtbl.receiver.mail.dal.DALDominator;
import com.archermind.txtbl.receiver.mail.support.*;
import com.archermind.txtbl.utils.*;
import com.sun.mail.imap.IMAPMessage;
import org.jboss.logging.Logger;

import javax.mail.*;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.event.StoreEvent;
import java.util.*;


public class NewImapIdleProvider extends NewImapProvider implements MessageCountListener {
    private static final Logger log4j = Logger.getLogger(NewImapIdleProvider.class);

    private static final IdleProcessMap accountIdleProcessMap = new IdleProcessMap();

    Account account;

    String context;

    private IdleProcess idleProcess;

    public NewImapIdleProvider(NewProviderSupport support, Authenticator authenticator) {
        super(support, authenticator);
    }

    /**
     * Invoked by the receive mail mdb every time there is a message from task factory
     *
     * @param account PEEK email account
     * @return number of new emails received
     */
    @Override
    public int receiveMail(final Account account) {
        this.account = account;

        context = String.format("[%s] account=%s, uid=%s, email=%s", this.hashCode(), account.getId(), account.getUser_id(), account.getName());

        String idleProcessKey = getIdleProcessKey(account);

        if (!DALDominator.loadAccountLatestReceiveInfo(account)) {
            log4j.warn(String.format("account has been removed by email checks continue for %s", context));
            // this means that we can't find account anymore - probably to do with deletion
            return 0;
        }

        if(account.isXobniAccount()) {
            XobniAccount xobniAccount = new UserService().getXobniAccountByUserID(account.getUser_id());
            if(!xobniAccount.isActive()) {
                log4j.info("Xobni account is not active - " + context);
                removeImapIdleProcess(account);
            }
        }

        if(XobniFirstTimeHandler.INSTANCE.handleFirstTime(account, support, context)) {
            return 0;
        }

        //Check to see if we should tear down or refresh the current idle connection
        if (accountIdleProcessMap.getProcessMap().containsKey(idleProcessKey)) {
            idleProcess = accountIdleProcessMap.getProcessMap().get(idleProcessKey);

            if (idleProcess.needsKilling()) {
                log4j.info(String.format("idle process needs killing for %s", context));
                removeImapIdleProcess(account);
                idleProcess=null;  //we do this so we'll reconnect again in this thread, instead of waiting until x minutes later to reconnect
            } else if (idleProcess.needsRefreshing()) {
                log4j.info(String.format( "idle process needs refreshing for %s", context));
                if (idleProcess.refreshImapConnection()) {
                    log4j.info(String.format( "refreshed account %s and now we are exiting", context));
                    return 0;
                }
            } else {
                log4j.info(String.format( "we don't need to do anything with this account %s", context));
                return 0;
            }

        }

        //Before we do anything, lets check if this person's device is on and being used
        //if it has been off for a long time we won't check it
        //if it has been off for a bit of time, we will check it less often
        if(!support.isAccountBeingUsed(account)){
            log4j.info(String.format( "Skipping mailcheck, this device is not being used for account %s", context));
            return 0;

        }

        //Double check this account has a current password/credentials
        if (support.exceededMaximumLoginFailures(account)) {
            log4j.info(String.format("exceeded maximum login failures with %d attempts for %s", account.getLogin_failures(), context));
            removeImapIdleProcess(account);
            return 0;
        }

        long currentIdlingProcessStartTime = System.currentTimeMillis();

        //Try to connect and create the IdleProcess
        log4j.info(String.format( "attempting to establish idling connection to account %s", context));
        if(idleProcess ==null){
            idleProcess = createIdleProcess(account, currentIdlingProcessStartTime);
            log4j.info(String.format( "idle adding a new idle account %s", context));
            accountIdleProcessMap.getProcessMap().put(idleProcessKey, idleProcess);
        }

        if (idleProcess.connect(authenticator, support)) {
            log4j.info(String.format( "idle connected to account %s and now setting up listener", context));
            idleProcess.addMessageCountListener(this);
        } else {
            log4j.error(String.format("could not connect for idle to account %s", context));
            removeImapIdleProcess(account);
            return 0;
        }

        //Lets make sure we haven't missed any emails
        receiveAnyMissedEmails();

        try {

            while (true) {
                idleProcess = accountIdleProcessMap.getProcessMap().get(idleProcessKey);

                if (idleProcess == null || currentIdlingProcessStartTime != idleProcess.getLatestProcessStartTime()) {
                    log4j.info(String.format( "idler for %s has been replaced, stopping this instance", context));
                    break;
                }

                try {
                    if (idleProcess.isConnected()) {
                        idleProcess.idle(context);
                    } else {
                        log4j.info(String.format( "reconnecting idle process for %s", context));
                        //disconnect so we ensure no memory leaks
                        idleProcess.disconnect();
                        
                        //for some reason we weren't connected
                        //this will try to reconnect, and if it does, it will catchup on old emails
                        if (idleProcess.connect(authenticator, support)) {
                            receiveAnyMissedEmails();
                        }else{
                            log4j.warn(String.format("could not reconnect for %s,", context));
                            break;
                        }

                    }

                } catch (FolderClosedException e) {
                    log4j.warn(String.format("folder is closed, can't idle anymore, quit and reconnect during the next cycle for %s [%s]", account, e.getMessage()));
                    idleProcess.disconnect();  //try to disconnect so it will reconnect on next iteration

                } catch (MessagingException e) {
                    if (e.getMessage().contains("Invalid credentials")) {
                        try {
                            support.handleLoginFailures(context, account);
                        } catch (Exception dalException) {
                            log4j.error(String.format("unexpected failure while trying to update login failures account %s %s ", account, dalException));
                        }
                    }
                    log4j.error(String.format("unexpected messaging exception %s", account), e);
                    break;

                }  catch (IllegalStateException e) {
                    log4j.warn(String.format("folder is closed or in Illegal state, can't idle anymore, quit and reconnect during the next cycle for %s [%s]", account, e.getMessage()));
                    idleProcess.disconnect();  //try to disconnect so it will reconnect on next iteration
                }


            }   //while
        } catch (Throwable t) {
            log4j.error(String.format("unexpected general exception %s", account), t);
        } finally {
            log4j.info(String.format( "removing message count listener, closing folder and mail store for %s", context));
            if (idleProcess!=null) {
                // imapFolder is null if this process hasn't gotten this far - which is the case for health checks invocations etc.
                idleProcess.removeMessageCountListener(this);
                try {
                    idleProcess.disconnect();
                    accountIdleProcessMap.getProcessMap().remove(idleProcessKey);
                    Thread.currentThread().interrupt();
                } catch (Throwable t) {
                    log4j.warn( "Unable to stop idling thread for " + context, t);
                }
            }
        }

        log4j.info(String.format( "receive mail complete for %s", context));

        return 0;
    }

    protected IdleProcess createIdleProcess(Account account, long currentIdlingProcessStartTime) {
        return new IdleProcess(currentIdlingProcessStartTime, account);
    }

    /**
     * This method takes advantage of existing Gmail Pop3 implementation and brings in any messages
     * that might have been missed during server restarts
     */
    void receiveAnyMissedEmails() {
        log4j.info(String.format( "catching up on any messages we might have missed for %s", context));

        try {
            // let's gmail imap provider process any missed email
            int result = super.receiveMail(account);

            if (result < 0) {
                log4j.error(String.format("unable to receive missed email using gmail imap implementation for %s, return code is %s", account, result));


            } else {
                log4j.info(String.format( "received %s missed emails for %s", result, context));

                PushMailNotifier.sendPushMailNotification(account, result, context);
            }
        }
        catch (Throwable t) {
            log4j.error("unable to receive missed email using gmail imap implementation for " + account, t);

        }
    }

    public void messagesAdded(MessageCountEvent event) {
        String context = String.format("account=%s, uid=%s, email=%s", account.getId(), account.getUser_id(), account.getName());
        StopWatch watch = new StopWatch("idle receive " + context);
        try {
            String messageStoreBucket = support.getMessageIdStore().getBucket(account.getId());
            Set<String> storedMessageIds = new HashSet<String>();

            EmailProcess emailProcess = support.getEmailProcess(account, storedMessageIds, messageStoreBucket, context, watch);
            if(!emailProcess.shouldProcess()) {
                log4j.info("Should Process is false for " + context);
                removeImapIdleProcess(account);
                return;
            }

            Message[] messages = event.getMessages();

            log4j.info(String.format( "%d possibly new messages have been identified for %s", messages.length, context));

            if (messages.length > 0) {
                Folder inbox = messages[0].getFolder();
                FolderHelper folderHelper = FolderHelperFactory.getFolderHelper(account, inbox);
                int folderDepth = folderHelper.getFolderDepth();

                if (!DALDominator.loadAccountLatestReceiveInfo(account)) {
                    log4j.warn(String.format("account has been removed by email checks continue for %s", context));
                    removeImapIdleProcess(account);
                    return;
                }
                int total = 0;
                Date lastMessageReceivedDate = null;
                try {

                    for (Message message : messages) {
                        String messageId =  null;
                        try {
                            try {
                                StopWatchUtils.newTask(watch, "process message", context, log4j);
                                messageId = String.valueOf(idleProcess.getUID(message));
                                if (support.processMessage(account, message, message.getMessageNumber(), messageId, idleProcess.getImapFolder(), storedMessageIds, messageStoreBucket, context, watch, folderHelper, emailProcess)) {
                                    total++;
                                }
                            } catch (MessageRemovedException e) {
                                log4j.warn("Message " + message + " was removed for " + context);
                            }
                            //Used later to set lastReceivedDate properly for the account
                            if (lastMessageReceivedDate == null || lastMessageReceivedDate.before(message.getReceivedDate())) {
                                lastMessageReceivedDate = message.getReceivedDate();
                            }

                            //Added by Dan to clear high memory/caching by Javamail
                            StopWatchUtils.newTask(watch, "invalidate headers", context, log4j);
                            ((IMAPMessage) message).invalidateHeaders();
                        } catch (MessageRemovedException e) {
                            log4j.warn("message removed - " + messageId + " : " + context);
                        }
                    }

                    emailProcess.complete();
                    if(emailProcess.shouldStopProcess()) {
                        log4j.info("stop process is true for " + context);
                        removeImapIdleProcess(account);
                        return;
                    }
                } catch (Throwable e) {
                    log4j.error("Unable to process imap idle messages for " + account, e);
                } finally {
                    if(!emailProcess.shouldStopProcess()) {
                        StopWatchUtils.newTask(watch, "updateAccount", context, log4j);
                        support.updateAccount(account, account.getFolder_hash(), total, folderDepth, lastMessageReceivedDate);
                    }
                }

                PushMailNotifier.sendPushMailNotification(account, total, context);
            }
        } catch (IllegalStateException e) {
            if(isIdlingForAccount(account)){

            }
        } catch (Throwable e) {
            String message = e.getMessage();
            if(e instanceof IllegalStateException && message!= null && message.contains("Not connected")) {
                log4j.error("lost connection for " + account, e);
                removeImapIdleProcess(account);
            } else {
                log4j.error(String.format("Unexpected error while trying to process new messages for %s: %s", account, e.getMessage()), e);
            }
        } finally {
            watch.stop();
            log4j.info(watch.prettyPrint());
        }
    }

    public void messagesRemoved(MessageCountEvent event) {
        // can't be used since are unable to identify the message. only message number is available, which is meaningless
    }

    /**
     * Added by DM
     * Allows us to capture disconnections from the remote mail server
     * Should help reduce incidences of
     *
     * @param arg0
     */
    public void notification(StoreEvent arg0) {
        // these are, typically, store disconnection events, due to IDLE inactivity

        log4j.info(String.format( "Received a  notification from Idling mail server for %s ", account));
        if (idleProcess.isConnected()) {
            idleProcess.disconnect();
        }  // forces a re-connect attempt
    }


    public static boolean isIdlingForAccount(Account account) {
        String idleProcessKey = account.getUser_id() + "_" + account.getName();
        return accountIdleProcessMap.getProcessMap().containsKey(idleProcessKey);
    }


    public static void removeImapIdleProcess(Account account) {
        XobniFirstTimeHandler.INSTANCE.removeFromXobniInitialFetchMap(account);
        log4j.debug(String.format("removing map entry, total: %s, removed: %s",accountIdleProcessMap.getProcessMap().size(),account.getLoginName()));
        String idleProcessKey = getIdleProcessKey(account);
        IdleProcess idleProcess = accountIdleProcessMap.getProcessMap().remove(idleProcessKey);
        if(idleProcess!=null) idleProcess.disconnect();
    }

    private static String getIdleProcessKey(Account account) {
        return account.getUser_id() + "_" + account.getName();
    }
}

