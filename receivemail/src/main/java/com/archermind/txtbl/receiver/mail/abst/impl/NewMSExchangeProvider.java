package com.archermind.txtbl.receiver.mail.abst.impl;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.dal.business.impl.UserService;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.exception.SystemException;
import com.archermind.txtbl.mail.store.MessageStoreException;
import com.archermind.txtbl.receiver.mail.abst.Provider;
import com.archermind.txtbl.receiver.mail.dal.DALDominator;
import com.archermind.txtbl.receiver.mail.support.*;
import com.archermind.txtbl.receiver.mail.utils.ProviderStatistics;
import com.archermind.txtbl.utils.*;
import com.webalgorithm.exchange.ConnectionPick;
import com.webalgorithm.exchange.ExchangeClient;
import com.webalgorithm.exchange.ExchangeClientException;
import com.webalgorithm.exchange.ExchangeConnectionMode;
import org.jboss.logging.Logger;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import java.security.Security;
import java.util.*;

public class NewMSExchangeProvider implements Provider
{

    private static final Logger logger = Logger.getLogger(NewMSExchangeProvider.class);

    private static ProviderStatistics statistics = new ProviderStatistics();

    protected NewProviderSupport support;

    private static HashMap<String, ExchangeConnectionMode> connectionModesList = new HashMap<String, ExchangeConnectionMode>();

    /**
     * Constructs new pop3 provider, initializes emails cutoff - how far back to consider messages and max message size - the biggest possible message size we should consider
     */
    public NewMSExchangeProvider(NewMSExchangeProviderSupport support) {
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
        String context = String.format("Receiving mail for  Exchange account host - %s port - %s ssl - %s email - %s login - %s fbapath - %s prefix - %s", account.getReceiveHost(), account.getReceivePort(),account.getReceiveTs(), account.getName(), account.getLoginName(), account.getReceiveHostFbaPath(), account.getReceiveHostPrefix());

        StopWatch watch = new StopWatch("mailcheck " + context);

        logger.info(String.format("receiving email %s", context));

        int newMessages = 0;
        int folderDepth = 0;
        int examined = 0;
        Date lastMessageReceivedDate = null;

        MSExchangeFolder inbox = null;

        StopWatchUtils.newTask(watch, "Get session", context, logger);
        Session session = Session.getInstance(getMailProperties(account));

        Store store = null;

        try
        {
            StopWatchUtils.newTask(watch, "Load latest receive info for account", context, logger);
            if (!DALDominator.loadAccountLatestReceiveInfo(account))
            {
                logger.warn(String.format("account has been removed by email checks continue for %s", context));
                // this means that we can't find account anymore - probably to do with deletion
                return 0;
            }

            if (support.exceededMaximumLoginFailures(account)) {
                logger.warn(String.format("exceeded maximum login failures with %d attempts for %s", account.getLogin_failures(), context));
                return 0;
            }

            StopWatchUtils.newTask(watch, "Get store", context, logger);
            store = session.getStore("pop3");

            StopWatchUtils.newTask(watch, "Try to connect", context, logger);

            ExchangeClient exchangeClient;
            ExchangeConnectionMode currentExchConnMode = getExchangeConnectionMode(account);
            if (currentExchConnMode != null)
            {
                connectionModesList.put(account.getName(),currentExchConnMode);
            }
            if (connectionModesList.get(account.getName()) != null)
            {
                exchangeClient = MSExchangeUtil.getExchangeClient(connectionModesList.get(account.getName()),account.getReceiveHost(),
                        account.getReceivePort(),account.getReceiveTs(),account.getName(),account.getLoginName(),
                        account.getPassword(),account.getReceiveHostPrefix(),account.getReceiveHostFbaPath());
            }
            else
            {
                    ConnectionPick cp = MSExchangeUtil.pickConnection(account.getReceiveHost(), account.getReceivePort(),
                        account.getReceiveTs(), account.getName(), account.getLoginName(), account.getPassword(),
                        account.getReceiveHostPrefix(), account.getReceiveHostFbaPath());
                    saveExchangeMode(account, cp.getConnectionMode().getSaveModeToDb());
                    if (cp != null)
                    {
                        exchangeClient = cp.getExchangeClient();
                        connectionModesList.put(account.getName(),cp.getConnectionMode());
                    }
                    else
                    {
                        logger.info(String.format("login for %s is failes with message %s", context, ""));
                        support.handleLoginFailures(context, account);
                        throw new RuntimeException(String.format("Unable to pick exchange connection for %s", account.getLoginName()));
                    }
            }


            StopWatchUtils.newTask(watch, "Get folder", context, logger);
            inbox = new MSExchangeFolder(session, exchangeClient);

            StopWatchUtils.newTask(watch, "Open folder", context, logger);
            inbox.open(Folder.READ_ONLY);

            StopWatchUtils.newTask(watch, "Getting email count in folder", context, logger);
            FolderHelper folderHelper = FolderHelperFactory.getFolderHelper(account, inbox);
            folderDepth = folderHelper.getFolderDepth();

            StopWatchUtils.newTask(watch, "Getting messageStore bucket", context, logger);
            String messageStoreBucket = support.getMessageIdStore().getBucket(account.getId()); // calculate once

            if (account.getLast_received_date() == null)
            {
                if (account.getLast_mailcheck() != null)
                {

                    StopWatchUtils.newTask(watch, "Handle migrated account", context, logger);
                    handleMigratedAccount(account, inbox, folderDepth, messageStoreBucket, context);
                }
                else
                {
                    StopWatchUtils.newTask(watch, "Start first time receive", context, logger);
                    newMessages = handleFirstTime(account, inbox, messageStoreBucket, folderDepth, messageStoreBucket, context, watch, folderHelper);
                }
            }
            else
            {
                StopWatchUtils.newTask(watch, "getMessages", context, logger);
                Message[] messages = inbox.getMessages();
                examined = messages.length;

                StopWatchUtils.newTask(watch, "getStoreMessages", context, logger);
                Set<String> storedMessageIds = support.getMessageIdStore().getMessages(account.getId(), account.getCountry(), context, watch);

                if(logger.isDebugEnabled())
                    logger.debug(String.format("store bucket is %s for %s", messageStoreBucket, context));

                StopWatchUtils.newTask(watch, "processMessages", context, logger);

                for (Message message : messages)
                {
                    StopWatchUtils.newTask(watch, String.format("msgNum=%s, getUID", message.getMessageNumber()), context, logger);

                    if (support.processMessage(account, message, message.getMessageNumber(), String.valueOf(inbox.getUID(message)), inbox, storedMessageIds, messageStoreBucket, context, watch, folderHelper))
                    {
                        if (lastMessageReceivedDate == null || lastMessageReceivedDate.before(message.getReceivedDate()))
                        {
                            lastMessageReceivedDate = message.getReceivedDate();
                        }

                        newMessages++;

                        if (newMessages > support.getMaximumMessagesToProcess())
                        {
                            break;
                        }
                    }
                }

                StopWatchUtils.newTask(watch, "updateAccount", context, logger);
                support.updateAccount(account, null, newMessages, folderDepth, lastMessageReceivedDate);
            }
        }
        catch (ExchangeClientException e)
        {
            //increment login failures number and save it
            try {
                if(logger.isDebugEnabled())
                    logger.debug(String.format("login failed for %s ", context));
                support.handleLoginFailures(context, account);
            } catch (DALException ex) {
                logger.info(String.format("update loginFailures for %s is failed with message %s", context, ex.getMessage()));
            }
//            throw new SystemException(String.format("unexpected failure during receive mail for %s", context), e);
        }
        catch (Throwable e)
        {
            throw new SystemException(String.format("unexpected failure during receive mail for %s", context), e);
        }
        finally
        {
            StopWatchUtils.newTask(watch, String.format("closeConnection (%d new mails, %d in folder, %d examined)", newMessages, folderDepth, examined), context, logger);

            FinalizationUtils.close(inbox);
            FinalizationUtils.close(store);

            StopWatchUtils.newTask(watch, "logMailCheckEvent", context, logger);


            watch.stop();

            logger.info(ReceiverUtilsTools.printWatch(watch, folderDepth, newMessages, false));

            String summary = statistics.enterStats(NewPOP3Provider.class.getName(), folderDepth, newMessages, watch.getTotalTimeMillis());

            logger.info(String.format("completed mailcheck for %s, folderDepth=%d, newMessages=%d, examined=%d, time=%sms [%s]", context, folderDepth, newMessages, examined, watch.getTotalTimeMillis(), summary));
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
    private int handleFirstTime(Account account, MSExchangeFolder inbox, String messageStoreBucket, int folderDepth, String bucket, String context, StopWatch watch, FolderHelper folderHelper) throws Exception
    {
        logger.info(String.format("handling first mailcheck for %s", context));

        Collection<Integer> messages = support.getFirstTimeMessages(inbox, context);

        int newMessages = 0;

        if (messages.size() > 0)
        {
            Set<String> storedMessageIds = new HashSet<String>();

            Date lastMessageReceivedDate = null;

            for (Integer messageNumber : messages)
            {
                Message message = inbox.getMessage(messageNumber);

                if (lastMessageReceivedDate == null || lastMessageReceivedDate.before(message.getReceivedDate()))
                {
                    lastMessageReceivedDate = message.getReceivedDate();
                }

                String messageId = String.valueOf(inbox.getUID(message));

                if (support.processMessage(account, message, messageNumber, messageId, inbox, storedMessageIds, messageStoreBucket, context, watch, folderHelper))
                {
                    newMessages++;
                }
            }

            handleMigratedAccount(account, inbox, folderDepth, bucket, context);
        }

        return newMessages;
    }

    /**
     * @param inbox
     * @param account
     * @param bucket
     * @throws MessagingException
     * @throws MessageStoreException
     */
    private void handleMigratedAccount(Account account, MSExchangeFolder inbox, int folderDepth, String bucket, String context) throws MessagingException, MessageStoreException, DALException
    {
        Message[] messages = inbox.getMessages();

        logger.info(String.format("found %d messages to add to id store as part of migration for %s", messages.length, context));

        // all we have to do is to store them now
        List<String> ids = new ArrayList<String>();

        Date lastMessageReceivedDate = null;

        if (messages.length == 0)
        {
            lastMessageReceivedDate = new Date(System.currentTimeMillis());
        }
        else
        {
            for (Message message : messages) {
                if (lastMessageReceivedDate == null || lastMessageReceivedDate.before(message.getReceivedDate())) {
                    lastMessageReceivedDate = message.getReceivedDate();
                }

                // the formatting below is necessary to stay consistent with POP bulk imports
                ids.add(message.getMessageNumber() + " " + inbox.getUID(message));
            }
        }

        support.getMessageIdStore().addMessageInBulk(account.getId(), bucket, IdUtil.encodeMessageIds(ids), account.getCountry());

        logger.info(String.format("migrated messages with last receive date of %s for %s", lastMessageReceivedDate, context));

        support.updateAccount(account, null, 0, folderDepth, lastMessageReceivedDate);
        // migration case


    }

    //TODO: need to be refactored. Can do away with.
    private Properties getMailProperties(Account account)
    {
        Properties props = new Properties();
        props.setProperty("mail.pop3.port", account.getReceivePort());
        props.setProperty("mail.pop3.connectiontimeout", "30000");
        if ("ssl".equals(account.getReceiveTs()))
        {
            Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
            props.setProperty("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
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

    private int saveExchangeMode(Account account, String mode)
    {
        UserService service = new UserService();
        return service.updateExchangeMode(account, mode);
    }

    private ExchangeConnectionMode getExchangeConnectionMode(Account account) {
        UserService service = new UserService();
        return service.getExchangeMode(account);
    }
}
