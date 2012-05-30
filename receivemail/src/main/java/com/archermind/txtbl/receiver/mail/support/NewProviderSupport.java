package com.archermind.txtbl.receiver.mail.support;

import com.archermind.txtbl.authenticator.*;
import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.dal.business.IPeekLocationService;
import com.archermind.txtbl.dal.business.impl.EmailRecievedService;
import com.archermind.txtbl.dal.business.impl.PeekLocationService;
import com.archermind.txtbl.domain.*;
import com.archermind.txtbl.mail.store.MessageStore;
import com.archermind.txtbl.mail.store.MessageStoreException;
import com.archermind.txtbl.mail.store.MessageStoreFactory;
import com.archermind.txtbl.parser.MessageParser;
import com.archermind.txtbl.receiver.mail.dal.DALDominator;
import com.archermind.txtbl.sync.IMAPSyncUtil;
import com.archermind.txtbl.utils.*;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.pop3.POP3Folder;
import org.jboss.logging.Logger;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class NewProviderSupport implements LoginFailureHandler {

    private static final Logger log = Logger.getLogger(NewProviderSupport.class);

    public static final int DEFAULT_MAXIMUM_MESSAGE_SIZE = 20485760;

    public static final String EMAIL_DROPPED_MESSAGE = "Dear Peekster,\n\nPlease pardon the inconvenience, but we wanted to make sure you know that we were unable to deliver the following email to your Peek due to its size:\n\nReceived Time: %s\nReceived Date: %s\nReceived From: %s\nMessage Size: %d\n\nWe're working hard on this limitation and hope to have it resolved soon, but for the time being we're not able to deliver messages to your Peek that exceed 20 MB in size. Keeping in mind that we greatly compress the size of your emails, you should only encounter this issue infrequently. If you believe you've received this mail in error, please reply to this email and we'll be happy to help you out.\n\nHappy Peeking,\n\nThe Peek Team\nwww.getpeek.com";

    protected int emailDaysCutoff = 0;

    protected int maximumMessageSize = 0;

    private int maximumMessagesToProcess = 15;

    protected MessageStore messageIdStore = MessageStoreFactory.getStore();

    private final IMAPSyncUtil syncUtilUtil = new IMAPSyncUtil();

    private static final String LOCKED_OUT_MESSAGE_BODY = "Sorry, it looks like your email account password has changed. Please go to Peek Manager->Email Accounts->Account and update your password to resume Peek service.";

    private static final String LOCKED_OUT_MESSAGE_SUBJECT = "Action required: Please update your password!";

    private static final String LOCKED_OUT_MESSAGE_FROM = "care@getpeek.com";

    private static final String LOCKED_OUT_MESSAGE_ALIAS = "Peek";

    private int maximumLoginFailures;

    private static final Long mailcheckDisableIntervalInHours;
    private static final Long dayMultiplier;

    static {
         mailcheckDisableIntervalInHours = Long.valueOf(SysConfigManager.instance().getValue("mailcheckDisableIntervalInHours", "720"));
                 dayMultiplier = 1000l * 60 * 60;
    }

    public NewProviderSupport() {
        this.emailDaysCutoff = getEmailDaysCutoff();
        this.maximumMessageSize = getMaxMessageSize();
        this.maximumMessagesToProcess = Integer.valueOf(SysConfigManager.instance().getValue("maximumMessages", "50"));

        this.maximumLoginFailures = Integer.valueOf(SysConfigManager.instance().getValue("maximumLoginFailures", "3"));
    }

    public boolean syncEmails(Account account, Folder folder, String context) {
        try {
            return syncUtilUtil.syncEmails(account, folder, context);
        } catch (DALException e) {
            log.error(e);
        }
        return false;
    }

    public Message processMessage(Account account, String messageStoreBucket, Message message, int messageNumber, String messageId, String context, StopWatch watch, List<MessageValidator> validators, EmailProcess emailProcess) throws Exception {
        long start = System.nanoTime();

        try {
            StopWatchUtils.newTask(watch, String.format("msgNum=%s, storeCheck", messageNumber), context, log);
            log.debug(String.format("msgNum=%d, checking if messageId=%s is known for %s", messageNumber, messageId, context));
            message = valiateAndPrepMessage(account, messageStoreBucket, message, messageNumber, messageId, validators);
            if (message != null) {
                log.info(String.format("msgNum=%d, messageId=%s, message is new for %s", messageNumber, messageId, context));
                boolean result = emailProcess.process(message, messageNumber, messageId);
                if(result) {
                    return message;
                }
            }
        } finally {
            if (log.isDebugEnabled())
                log.debug(String.format("msgNum=%d, checked if messageId=%s is known in %dms for %s", messageNumber, messageId, (System.nanoTime() - start) / 1000000, context));
        }

        return null;
    }

    public boolean processMessage(Account account, Message message, int messageNumber, String messageId, Folder folder, Set<String> storeMessageIds, String storeBucket, String context, StopWatch watch, FolderHelper folderHelper, EmailProcess emailProcess) throws Exception {
        List<MessageValidator> messageValidators = getMessageValidators(account, folder, storeMessageIds, storeBucket, context);
        folderHelper.addMessageValidators(messageValidators);
        return processMessage(account, storeBucket, message, messageNumber, messageId, context, watch, messageValidators, emailProcess) != null;
    }

    public boolean processMessage(Account account, Message message, int messageNumber, String messageId, Folder folder, Set<String> storeMessageIds, String storeBucket, String context, StopWatch watch, FolderHelper folderHelper) throws Exception {
        return processMessage(account, message, messageNumber, messageId, folder, storeMessageIds, storeBucket, context, watch, folderHelper, getEmailProcess(account, storeMessageIds, storeBucket, context, watch));
    }

    protected Message valiateAndPrepMessage(Account account, String messageStoreBucket, Message message, int messageNumber, String messageId, List<MessageValidator> validators) throws Exception {
        for (MessageValidator validator : validators) {
            message = validator.validate(message, messageNumber, messageId);
            if (message == null) {
                validator.handleFailure(account, messageStoreBucket, messageId);
                return null;
            }
        }
        return message;
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
        });
        messageValidators.add(new MessageValidator() {
            public Message validate(Message message, int messageNumber, String messageId) throws Exception {
                if (isSentMessage(account, message)) {
                    return null;
                }
                return message;
            }
        });
        messageValidators.add(new MessageValidator() {
            public Message validate(Message message, int messageNumber, String messageId) throws Exception {
                if (isMessageTooOld(account, message, context)) {
                    return null;
                }
                return message;
            }
        });
        messageValidators.add(new MessageValidator() {
            public Message validate(Message message, int messageNumber, String messageId) throws Exception {
                message = getFullyLoad(message, folder, context);
                if (isMessageTooBig(folder, account, message, messageNumber)) {
                    return null;
                }
                return message;
            }
        });
        return messageValidators;
    }

    protected boolean isMessageAlreadyProcessed(String messageId, Account account, String storeBucket, Set<String> storeMessageIds) throws MessageStoreException {
        return messageId != null && (storeMessageIds.contains(messageId) || messageIdStore.hasMessage(account.getId(), storeBucket, messageId, account.getCountry()));
    }

    boolean isMessageTooBig(Folder folder, Account account, Message message, int messageNumber) throws Exception {
        int messageSize = (folder instanceof POP3Folder) ? ((POP3Folder) folder).getMessageSize(messageNumber) : getMessageSize(message, account);
        boolean messageTooBig = messageSize > maximumMessageSize;
        if (messageTooBig) {
            log.warn(String.format("msgNum=%d, message is too big %d bytes, discarding for %s", messageNumber, messageSize, account.getName()));
            saveMessageDroppedNotification(account, messageNumber, message, messageSize);
        }
        return messageTooBig;
    }

    /**
     * It so happens Gmail sends us sent messages as well as received once
     *
     * @param account
     * @param message
     * @return
     * @throws Exception
     */
    protected boolean isSentMessage(Account account, Message message) throws Exception {
        MessageParser parser = new MessageParser();

        if (EmailUtil.isFromSameAsAccount(account, message)) {
            String to = parser.parseMsgAddress(message, "TO", true);
            String cc = parser.parseMsgAddress(message, "CC", true);

            if (!to.contains(account.getName()) && !cc.contains(account.getName())) {
                log.warn(String.format("msgNum=%d, message is 'sent' not 'received' discarding, for %s", message.getMessageNumber(), account.getName()));
                return true;
            }
        }

        return false;
    }

    public boolean isMessageTooOld(Account account, Message message, String context) throws MessagingException {
        if (message.getSentDate() == null) {
            log.warn(String.format("we have a message with no sent date for %s, allowing message", context));
            return false;
        } else if (account.getRegister_time() == null) {
            log.warn(String.format("we are process an account with no register time. this behavior is not understood yet %s, we will accept this message", context));
            return false;
        } else {
            boolean messageTooOld = (System.currentTimeMillis() - message.getSentDate().getTime()) > 1000l * 60 * 60 * 24 * emailDaysCutoff;
            if (messageTooOld) {
                log.warn(String.format("msgNum=%d, message is too old, sentDate=%s, discarding, for %s", message.getMessageNumber(), message.getSentDate(), context));
            }
            return messageTooOld;
        }

    }

    public boolean isTimeToReconcile(Account account) {
        return account.getLast_reconciliation() == null || (System.currentTimeMillis() - account.getLast_reconciliation().getTime()) > 1000l * 60 * Long.valueOf(SysConfigManager.instance().getValue("reconciliationIntervalInMinutes", "60"));
    }

    private int getEmailDaysCutoff() {
        String emailDaysCutoff = SysConfigManager.instance().getValue("emailDaysCutoff");

        if (StringUtils.isEmpty(emailDaysCutoff)) {
            return 30;
        } else {
            return Integer.valueOf(emailDaysCutoff);
        }

    }

    private int getMaxMessageSize() {
        try {
            String config = SysConfigManager.instance().getValue("maxMessageSize");

            if (StringUtils.isEmpty(config)) {
                return DEFAULT_MAXIMUM_MESSAGE_SIZE;
            } else {
                return Integer.parseInt(config);
            }
        } catch (Throwable t) {
            log.warn(String.format("Unable to determined maximum message size, defaulting to %d", DEFAULT_MAXIMUM_MESSAGE_SIZE), t);
            return DEFAULT_MAXIMUM_MESSAGE_SIZE;
        }
    }


    //Doesn't peform mailcheck unless device has been used in past 30 days
    //Or if device has been off for > 48 hours, only check their mail every 15 minutes

    public boolean isAccountBeingUsed(Account account) {
        if (log.isDebugEnabled())
            log.debug(String.format("Checking account %s, user_id %s for last registration with location service", account.getId(), account.getUser_id()));

        IPeekLocationService locationService = new PeekLocationService();
        Date lastUpdated = null;
        try {
            lastUpdated = locationService.getLastUpdatedTimestamp(account.getUser_id());
        } catch (DALException e) {
            log.error(UtilsTools.getExceptionStackTrace(e));
        }
        if (lastUpdated == null) {
            log.debug("Last updated value not found, so we should still check this account");
            return true;
        }
        Date now = new Date();
        Long interval = dayMultiplier * mailcheckDisableIntervalInHours;
        Long sinceLastConnection = now.getTime() - lastUpdated.getTime();
        if (sinceLastConnection > interval) {
            if (log.isDebugEnabled())
                log.debug(String.format("Last mail check was %s hours ago, > %s hour interval , we will not check this account", sinceLastConnection / dayMultiplier, interval / dayMultiplier));
            return false;
        }

        return true;
    }


    /**
     * Deliveres notification to the user that an email has been dropped. Typically due to size restriction
     *
     * @param account
     * @param messageNumber
     * @param message
     * @throws Exception
     */
    public void saveMessageDroppedNotification(Account account, int messageNumber, Message message, int reportedSize) throws Exception {
        MessageParser parser = new MessageParser();

        Email email = new Email();
        email.setSubject("Your email");//ReceiverUtilsTools.subSubject(parser.parseMsgSubject(message), subjectSize));
        email.setFrom(parser.parseMsgAddress(message, "FROM", false));
        email.setTo(ReceiverUtilsTools.subAddress(parser.parseMsgAddress(message, "TO", true)));
        email.setCc(ReceiverUtilsTools.subAddress(parser.parseMsgAddress(message, "CC", true)));
        email.setBcc(ReceiverUtilsTools.subAddress(parser.parseMsgAddress(message, "BCC", true)));
        email.setMaildate(ReceiverUtilsTools.dateToStr(message.getSentDate()));
        email.setStatus("0");
        email.setUserId(account.getUser_id());
        email.setMessage_type("EMAIL");

        Body body = new Body();

        String droppedMessage = getEmailDroppedMessage(account, getMessageDate(message), reportedSize, email.getFrom());

        body.setData(droppedMessage.getBytes());
        email.setBodySize(droppedMessage.length());

        int saveStatus = DALDominator.newSaveMail(account, email, body);

        if (log.isDebugEnabled())
            log.debug(String.format("[%s] msgNum=%d, saving completed for dropped message with status %s", account.getName(), messageNumber, saveStatus));
    }

    protected String getEmailDroppedMessage(Account account, Date sentDate, int reportedSize, String from) throws MessagingException {
        String time = new SimpleDateFormat("hh:mm a").format(sentDate);
        String date = new SimpleDateFormat("MMMMM dd, yyyy").format(sentDate);

        String emailDroppedMessage = SysConfigManager.instance().getValue("emailDroppedMessage", EMAIL_DROPPED_MESSAGE, account.getPartnerCode());

        return String.format(emailDroppedMessage, time, date, from, reportedSize);
    }

    public Collection<Integer> getFirstTimeMessages(Folder inbox, String context) throws Exception {
        log.debug(String.format("handling first mailcheck for %s", context));

        Collection<Integer> messages = new ArrayList<Integer>();

        // we need to get 5 most recent messages in this case
        if (inbox.getMessageCount() > 0) {
            if (areMessagesInReverseOrder(inbox)) {
                log.debug(String.format("order is reverse chronological for %s", context));
                for (int i = 0; i < 5; i++) {
                    messages.add(i + 1);
                }
            } else {
                log.debug(String.format("order is chronological for %s", context));
                for (int messageNumber = inbox.getMessageCount(); messageNumber > 0 && messageNumber > inbox.getMessageCount() - 5; messageNumber--) {
                    messages.add(messageNumber);
                }
            }
        }

        return messages;
    }

    protected boolean areMessagesInReverseOrder(Folder inbox) throws MessagingException {
        try {
            Message msgFirst = inbox.getMessage(1);
            Message msgLast = inbox.getMessage(inbox.getMessageCount());

            Date firstMessageDate = getDateToDetermineOrder(msgFirst);
            Date lastMessageDate = getDateToDetermineOrder(msgLast);
            return firstMessageDate != null && lastMessageDate != null && firstMessageDate.after(lastMessageDate);
        } catch (Exception e) {
            return false;
        }
    }

    private Date getDateToDetermineOrder(Message message) throws MessagingException {
        return message instanceof IMAPMessage ? message.getReceivedDate() : message.getSentDate();
    }

    public MessageStore getMessageIdStore() {
        return messageIdStore;
    }

    public int getMaximumMessagesToProcess() {
        return maximumMessagesToProcess;
    }

    public void updateAccount(Account account, String newFolderHash, int newMessages, int folderDepth, Date lastMessageReceivedDate) throws DALException {
        account.setFolder_hash(newFolderHash);
        //reset login failures - since update account happens on successful mail fetch
        account.setLogin_failures(0);
        account.setLast_login_failure(null);

        account.setMessage_count(account.getMessage_count() + newMessages);

        account.setLast_mailcheck(new Date(System.currentTimeMillis()));

        account.setFolder_depth(folderDepth);

        if (lastMessageReceivedDate == null) {
            DALDominator.updateAccountReceiveInfo(account);
        } else {
            account.setLast_received_date(lastMessageReceivedDate);
            DALDominator.updateAccountReceiveInfoAndReceivedDate(account);
        }
    }

    public void fetchIds(Account account, IMAPFolder folder, Message[] messages) throws MessagingException {
        IdsUtilFactory.getIdsUtil(account).fetch(folder, messages);
    }

    public String getId(Account account, IMAPFolder inbox, Message message) throws MessagingException {
        return IdsUtilFactory.getIdsUtil(account).getId(inbox, message);
    }

    protected int getMessageSize(Message message, Account account) throws IOException, MessagingException {
        int count = 0;
        if (account.getLoginName().contains("aol.com") && message.getContent() instanceof Multipart) {
            Multipart multipart = (Multipart) message.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                count += multipart.getBodyPart(i).getSize();
            }
        } else {
            count = message.getSize();
        }
        return count;
    }

    // This method is overrided for Exchange protocol

    protected Message getFullyLoad(Message message, Folder folder, String context) throws MessagingException {

        //use the copy constructor to try and copy the email over and work with it locally
        boolean read = isRead(message, context);

        MimeMessage msg = new MimeMessage((MimeMessage) message);
        //BUG: was marking IMAP emails as read: test account of aircel to verify

        try {
            if (!read && (folder instanceof IMAPFolder)) {
                message.setFlag(Flags.Flag.SEEN, false);
            }
        } catch (MessagingException me) {
            //Not all providers allow for setting of flags
            log.debug(String.format("failed to SEEN flag %s", context), me);
        }
        return msg;
    }

    private boolean isRead(Message msg, String context) {
        boolean readFlag = false;
        try {
            if (msg.isExpunged()) {
                readFlag = true;
            } else {
                Flags flags = msg.getFlags();
                Flags.Flag[] flag = flags.getSystemFlags();
                for (Flags.Flag aFlag : flag) {
                    if (aFlag == Flags.Flag.SEEN) {
                        readFlag = true;
                        break;
                    }
                }
            }
        } catch (MessagingException e) {
            log.debug(String.format("failed to get SEEN FLAG for %s", context), e);
        }

        return readFlag;
    }

    public void handleLoginFailures(String context, Account account) throws DALException {
        account.setLast_mailcheck(new Date(System.currentTimeMillis()));

        if (account.getLogin_failures() == null) {
            account.setLogin_failures(1);
        } else {
            account.setLogin_failures(account.getLogin_failures() + 1);
        }

        account.setLast_login_failure(new Date(System.currentTimeMillis()));

        DALDominator.updateAccountReceiveInfo(account);

        if (shouldSendAccountLockedNotification(account)) {
            notifyAccountLock(account, context);
            sendAccountLockedNotificationEm(account);
        }

    }

    private boolean shouldSendAccountLockedNotification(Account account) {
        return account.getLogin_failures() != null && (account.getLogin_failures() == maximumLoginFailures + 1);
    }

    protected void sendAccountLockedNotificationEm(Account account) {
        //do nothing
    }

    public boolean shouldProcessMigrated() {
        return true;
    }

    public void notifyAccountLock(Account account, String context) {
        PartnerCode partnerCode = account.getPartnerCode();
        String lockedOutMessageBody = SysConfigManager.instance().getValue("lockedOutMessageBody", LOCKED_OUT_MESSAGE_BODY, partnerCode);
        String lockedOutMessageSubject = SysConfigManager.instance().getValue("lockedOutMessageSubject", LOCKED_OUT_MESSAGE_SUBJECT, partnerCode);
        String lockedOutMessageFrom = SysConfigManager.instance().getValue("lockedOutMessageFrom", LOCKED_OUT_MESSAGE_FROM, partnerCode);
        String lockedOutMessageAlias = SysConfigManager.instance().getValue("lockedOutMessageAlias", LOCKED_OUT_MESSAGE_ALIAS, partnerCode);

        try {
            Email email = new Email();
            email.setSubject(lockedOutMessageSubject);

            email.setStatus("0");
            email.setUserId(account.getUser_id());
            email.setMessage_type("EMAIL");

            email.setFrom(lockedOutMessageFrom);
            email.setFrom_alias(lockedOutMessageAlias);
            email.setTo(account.getLoginName());
            email.setBodySize(lockedOutMessageBody.length());

            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            email.setMaildate(dateFormat.format(new Date(System.currentTimeMillis()))); // ugh!


            email.setOriginal_account(account.getLoginName());

            // create the pojgo
            EmailPojo emailPojo = new EmailPojo();
            emailPojo.setEmail(email);

            Body body = new Body();
            body.setData(lockedOutMessageBody.getBytes());

            // note, the email will be encoded to prevent sql-injection. since this email is destined directly for the
            // device, we need to reverse the encoding after the call to newSaveEmail
            new EmailRecievedService().newSaveEmail(account, email, body);

            //Added by Dan so we stop checking accounts that have incorrect passwords
            account.setStatus("0");

        } catch (Throwable t) {
            log.warn(String.format("failed to save locked out message for %s", context), t);
        }
    }

    public boolean exceededMaximumLoginFailures(Account account) {
        return account.getLogin_failures() != null && account.getLogin_failures() > maximumLoginFailures;
    }

    public int handleFirstTime(Account account, FolderHelper folderHelper, int folderDepth, String messageStoreBucket, Set<String> storedMessageIds, String context, StopWatch watch) throws Exception {
        int newMessages = 0;
        Date lastMessageReceivedDate = null;
        log.info(String.format("handling first mailcheck for %s", context));

        Folder folder;
        while ((folder = folderHelper.next()) != null) {
            String folderContext = context + " ,folderName=" + folder.getFullName();
            IMAPFolder imapFolder = (IMAPFolder) folder;

            Collection<Integer> messages = getFirstTimeMessages(imapFolder, folderContext);

            if (messages.size() > 0) {
                storedMessageIds = new HashSet<String>();

                for (Integer messageNumber : messages) {
                    Message message = imapFolder.getMessage(messageNumber);

                    if (lastMessageReceivedDate == null || lastMessageReceivedDate.before(message.getReceivedDate())) {
                        lastMessageReceivedDate = message.getReceivedDate();
                    }

                    String messageId = getId(account, imapFolder, message);

                    if (processMessage(account, message, messageNumber, messageId, imapFolder, storedMessageIds, messageStoreBucket, folderContext, watch, folderHelper)) {
                        newMessages++;
                    }
                }
            }

        }
        updateAccount(account, null, 0, folderDepth, lastMessageReceivedDate);
        return newMessages;

    }

    public boolean isMigratedAccount(Account account) {
        return account.getLast_received_date() == null && account.getLast_mailcheck() != null;
    }

    public boolean isFirstTime(Account account) {
        return account.getLast_received_date() == null && account.getLast_mailcheck() == null;
    }


    public EmailProcess getEmailProcess(Account account, Set<String> storedMessageIds, String messageStoreBucket, String context, StopWatch watch) {
        EmailSaveProcess saveProcess = new EmailSaveProcess(account, watch, context);
        return new EmailIdStoreProcess(account, storedMessageIds, messageStoreBucket, context, watch, saveProcess);
    }

    public Date getMessageDate(Message message) throws MessagingException {
        Date messageDate = message.getReceivedDate();
        if (message.getReceivedDate() == null) {
            if (message.getSentDate() != null) {
                messageDate = message.getSentDate();
            }
        }
        return messageDate;
    }
}
