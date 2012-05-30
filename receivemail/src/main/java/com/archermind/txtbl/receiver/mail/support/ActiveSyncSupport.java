package com.archermind.txtbl.receiver.mail.support;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.dal.business.impl.UserService;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.OriginalReceivedAttachment;
import com.archermind.txtbl.exception.SystemException;
import com.archermind.txtbl.mail.store.MessageStoreException;
import com.archermind.txtbl.utils.*;
import com.zynku.sync.activesync.context.ActiveSyncContext;
import com.zynku.sync.activesync.control.ActiveSyncController;
import com.zynku.sync.activesync.control.handler.HandlerException;
import com.zynku.sync.activesync.model.AirSyncAttachment;
import com.zynku.sync.activesync.model.ApplicationData;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.OptionsMethod;
import org.jboss.logging.Logger;

import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ActiveSyncSupport extends NewProviderSupport {
    protected static final Logger log = Logger.getLogger(ActiveSyncSupport.class);


    protected static Long timeCorrection;
    protected boolean isTransliterateEmail = Boolean.parseBoolean(SysConfigManager.instance().getValue("transliterateEmail", "true"));
    protected boolean isTransliterateEmailPeekvetica = Boolean.parseBoolean(SysConfigManager.instance().getValue("transliterateEmailPeekvetica", "true"));
    protected String canntGetAttachmentBodyNotif;
    public static final String UNABLE_GET_ATTACHMENT_BODY = "Unable to process attachment. Please visit your account online.";


    public ActiveSyncController getController(Account account, String deviceId, String deviceType) throws IOException, HandlerException {
        if(log.isTraceEnabled())
            log.trace(String.format("getController(account=%s, deviceId=%s, deviceType=%s)", String.valueOf(account), deviceId, deviceType));
        ActiveSyncContext activeSyncContext = new ActiveSyncContext();
        activeSyncContext.setDeviceId(deviceId);
        activeSyncContext.setDeviceType(deviceType);
        activeSyncContext.setUserName(account.getLoginName());
        activeSyncContext.setPassword(account.getPassword());
        activeSyncContext.setServerURL(new URL(("ssl".equals(account.getReceiveTs()) ? "https" : "http") + "://" + account.getReceiveHost()));
        ActiveSyncController controller = new ActiveSyncController(activeSyncContext, 230000);
        if(null != controller.getPolicyKey())
            activeSyncContext.setPolicyKey(controller.getPolicyKey());

        return controller;
    }

    public String getMessageId(ApplicationData email) {
        String received = email.get("DateReceived");
        String subject = email.get("Subject");
        String from = email.get("From");

        try {
            return HashGenerator.genHash(received + "_" + subject + "_" + from);
        }
        catch (Throwable e) {
            throw new SystemException("unable to generate message id has for " + email, e);
        }
    }


    public enum SortOrder {
        ASC, DESC
    }

    public ActiveSyncSupport() {
        this.canntGetAttachmentBodyNotif = SysConfigManager.instance().getValue("unableToGetAttachMessageBody", UNABLE_GET_ATTACHMENT_BODY);
    }

    public int getMaxEmailCount(){
        return Integer.parseInt(SysConfigManager.instance().getValue("get.mail.count.first.time", "5"));
    }



    /**
     * Utils method to convert date from string
     *
     * @param strDate string representation
     * @return result date if can't parse string then return null
     */
    public Date getDateFromString(String strDate) {
        Date result = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            result = sdf.parse(strDate);
        }
        catch (ParseException e) {
            log.fatal(String.format("unable to parse date %s expected format is like 2010-06-31T12:34:35", strDate), e);
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

                        if (processMessage(account, messageNumber, storeMessageIds, messageStoreBucket, email, messageId, context, watch, true, controller)) {
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


    /**
     * Util method to convert ApplicationData email object into javax.mail.Message object from
     *
     * @param email           ApplicationData instance
     * @param attachmentsList attachments list
     * @return result value
     */
    protected Message getMessage(Account account, ApplicationData email, List<OriginalReceivedAttachment> attachmentsList, String context) {

        try {
            Session session = Session.getDefaultInstance(new Properties(), null);
            MimeMessage msg = new MimeMessage(session);
            msg.setSubject(email.get("Subject"));
            String from = email.get("From");
            msg.setFrom(new InternetAddress(from));
            Address[] recepients = tranAddr(email.get("To"));
            for (Address recepient : recepients) {
                msg.addRecipient(Message.RecipientType.TO, recepient);
            }

            recepients = tranAddr(email.get("CC"));
            for (Address recepient : recepients) {
                msg.addRecipient(Message.RecipientType.CC, recepient);
            }

            recepients = tranAddr(email.get("BCC"));
            for (Address recepient : recepients) {
                msg.addRecipient(Message.RecipientType.BCC, recepient);
            }

            int mailSize = email.get("Data") != null ? email.get("Data").length() : 0;

            if (attachmentsList != null) {
                for (OriginalReceivedAttachment attach : attachmentsList) {
                    mailSize += attach.getSize();
                }
            } else if ((attachmentsList == null) && (email.getAttachments().size() > 0)) {
                mailSize += maximumMessageSize + 1;
            }
            if (mailSize > maximumMessageSize) {
                msg.setContent(getEmailDroppedMessage(account, getMessageDate(msg), mailSize, from), "html");
                return msg;
            }


            String content = MailUtils.clean(ReceiverUtilsTools.htmToTxt(email.get("Data")), isTransliterateEmail, isTransliterateEmailPeekvetica);

            msg.setContent(content, "text/plain");

            if (timeCorrection != null) {
                msg.setSentDate(new Date(getDateFromString(email.get("DateReceived")).getTime() + timeCorrection));

                log.debug("Date received is " + email.get("DateReceived") + " time correction is " + timeCorrection + " sent date is " + msg.getSentDate());
            } else {
                msg.setSentDate(getDateFromString(email.get("DateReceived")));
                log.debug("Time correction is null sent date is " + msg.getSentDate());
            }

            if ((attachmentsList != null) && (attachmentsList.size() > 0)) {
                MimeMultipart mp = new MimeMultipart("multipart");
                for (OriginalReceivedAttachment attach : attachmentsList) {
                    BodyPart tmpAttachBody = new MimeBodyPart();
                    tmpAttachBody.setFileName(attach.getName());
                    tmpAttachBody.setDataHandler(new DataHandler(new ByteArrayDataSource(attach.getData(), "application/octet-stream")));
                    mp.addBodyPart(tmpAttachBody);
                }
                BodyPart bodyPart = new MimeBodyPart();
                if (email.get("Data") != null) {
                    bodyPart.setText(content);
                } else {
                    bodyPart.setText("");
                }
                mp.addBodyPart(bodyPart);
                msg.setContent(mp);
                msg.setHeader("Content-Type", "multipart/*");
            }

            return msg;
        }
        catch (Throwable e) {
            log.fatal(String.format("unable to get message from %s with subject %s for %s", email.get("From"), email.get("Subject"), context), e);

            return null;
        }

    }

    /**
     * Util method for getting know when is time to reconcile mesage id's storage
     *
     * @param account account instance
     * @return result value
     */
    public boolean isTimeToReconcile(Account account) {
        UserService userService = new UserService();
        Date accountResult = userService.getAccountSubscribeAlertDate(account.getName());
        return accountResult == null || (System.currentTimeMillis() - accountResult.getTime()) > 1000l * 60 * 60 * 24 * Long.valueOf(SysConfigManager.instance().getValue("subscribeAlertIntervalInDays", "15"));
    }


    public boolean processMessage(Account account, Message message, int messageNumber, String messageId, Folder folder, Set<String> storeMessageIds, String storeBucket, String context, StopWatch watch, FolderHelper folderHelper) throws Exception {
        return false;
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
                                  boolean isFirstTime, ActiveSyncController controller) {
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

                        getEmailProcess(account, storeMessageIds, storeBucket, context, watch).process(message, messageNumber, messageId);

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
     * Method detects sort order for given emails list
     *
     * @param emailsList emails list
     * @return order value of last or first email's receive date is null then returns null
     */
    public SortOrder getSortOrder(List<ApplicationData> emailsList) {
        SortOrder result = null;
        if (emailsList.size() > 0) {
            Date firstMailReceived = getDateFromString(emailsList.get(0).get("DateReceived"));
            Date lastMailReceived = getDateFromString(emailsList.get(emailsList.size() - 1).get("DateReceived"));
            if ((emailsList.size() > 0) && (firstMailReceived != null) && (lastMailReceived != null)) {
                if (firstMailReceived.getTime() < lastMailReceived.getTime()) {
                    result = SortOrder.ASC;
                } else {
                    result = SortOrder.DESC;
                }
            }
        }
        return result;
    }

    /**
     * Method for getting next email depending on sort order (ASC or DESC)
     *
     * @param direction   sort direction
     * @param emailNumber email number
     * @param emailsList  emails list
     * @return result value if emailNumber > email list size then return null
     */
    public ApplicationData getNextEmail(SortOrder direction, int emailNumber, List<ApplicationData> emailsList) {
        ApplicationData result = null;

        if (emailsList.size() >= emailNumber) {
            result = (direction == SortOrder.ASC) ? emailsList.get(emailsList.size() - emailNumber) : emailsList.get(emailNumber - 1);
        }
        return result;
    }


    /**
     * Util method returns java.util.Map from string syncKey representation
     *
     * @param syncKeys string value
     * @return reuslt Map
     */
    public Map<String, String[]> getSyncKeys(String syncKeys) {
        if(log.isTraceEnabled())
            log.trace(String.format("getSyncKeys(syncKeys=%s)", syncKeys));
        Map<String, String[]> result = new HashMap<String, String[]>();
        if (!StringUtils.isEmpty(syncKeys)) {
            String[] folderKeys = syncKeys.split(";");
            for (String key : folderKeys) {
                if (key.indexOf('=') > -1) {
                    String[] keyValuePair = key.split("=");
                    if (keyValuePair[1].indexOf(',') > 0) {
                        result.put(keyValuePair[0], keyValuePair[1].split(","));
                    } else {
                        result.put(keyValuePair[0], new String[]{keyValuePair[1]});
                    }
                }
            }
        }
        return result;
    }

    /**
     * Util method that converts java.util.Map to string syncKey representation
     *
     * @param syncKeys Map
     * @return string result
     */
    public String getSyncKey(Map<String, String[]> syncKeys) {
        String result = "";
        for (Map.Entry<String, String[]> entry : syncKeys.entrySet()) {
            String syncKeysStr = "";
            for (String key : entry.getValue()) {
                syncKeysStr += ("".equals(syncKeysStr) ? "" : ",") + key;
            }
            result += (StringUtils.isEmpty(result) ? "" : ";") + entry.getKey() + '=' + syncKeysStr;
        }
        return result;
    }

    /**
     * Makes saving folder depth, last message receive date and syncKeys into database
     *
     * @param folderDepth             folder depth
     * @param lastMessageReceivedDate last message received
     * @param account                 user account
     * @throws DALException raised exception
     */
    public void updateAccountData(int folderDepth, int newMessages, Date lastMessageReceivedDate, Account account) {
        try {
            updateAccount(account, null, newMessages, folderDepth, lastMessageReceivedDate);
        }
        catch (DALException e) {
            throw new SystemException("Unable to update account " + account, e);
        }
    }

    /**
     * Makes saving folder depth, last message receive date and syncKeys into database
     *
     * @param calendarDate calendar date value to be updated
     * @param account      user account
     * @throws DALException raised exception
     */
    public void updateCalendarDate(Date calendarDate, Account account) throws DALException {
        account.setLast_calendar(calendarDate);
        UserService userService = new UserService();
        userService.resetCalendarDate(account.getName(), calendarDate);

    }

    /**
     * util method for getting keys value for given folder from syncKeys map. it there is no values return default value
     *
     * @param syncKey sync key array
     * @param folder  active sync folder instance
     * @return keys array
     */
    public List<ApplicationData> getEmails(String[] syncKey, com.zynku.sync.activesync.model.Folder folder, String context, ActiveSyncController controller) throws IOException {
        if (log.isDebugEnabled())
            log.debug(String.format("getEmails(syncKey=%s, folder=%s, context=%s)", syncKey[0], null != folder ? folder.getServerId() : "null", context));

        List<ApplicationData> result = null;

        try {
            if(folder != null) {
                result = controller.getEmails(folder.getServerId(), syncKey);
            }
        }
        catch (HandlerException e) {
            if (e.toString().toLowerCase().contains("invalid synchronization key")) {
                log.warn(String.format("looks like we have an invalid sync key %s for %s due to %s", syncKey[0], context, e.toString()));
            } else {
                throw new SystemException(String.format("error occured while retrieving emails for %s using server %s and sync key %s", context, folder.getServerId(), syncKey[0]), e);
            }

        }
        return result;
    }

    /**
     * Get Default sync key - gmail is a bit different
     *
     * @param emailName
     * @return
     */
    public String[] getDefaultSyncKey(String emailName) {
        String defaultSyncKeyValue = "0";

        if (emailName.indexOf("gmail.com") > 0) {
            defaultSyncKeyValue = "1";
        }

        return new String[]{defaultSyncKeyValue};
    }

    /**
     * Util method for getting attachments for current email
     *
     * @param email      for that getting attachments
     * @param controller active sync controller
     * @return attacments list
     */
    public List<OriginalReceivedAttachment> getAttachments(ApplicationData email, ActiveSyncController controller, String context) {
        List<OriginalReceivedAttachment> attachments = new ArrayList<OriginalReceivedAttachment>();
        if (email.getAttachments().size() > 0) {
            int mailSize = email.get("Data") != null ? email.get("Data").length() : 0;

            for (AirSyncAttachment attach : email.getAttachments()) {
                mailSize += attach.getEstimatedDataSize();
            }
            if (mailSize > maximumMessageSize) {
                return null;
            }
            for (AirSyncAttachment attachment : email.getAttachments()) {
                OriginalReceivedAttachment currentAttachment = new OriginalReceivedAttachment();
                currentAttachment.setName(attachment.getDisplayName());
                attachments.add(currentAttachment);
                try {
                    if (log.isDebugEnabled())
                        log.debug("ActiveSync file reference " + attachment.getFileReference());
                    currentAttachment.setData(controller.getAttachment(attachment.getFileReference()));
                }
                catch (Throwable e) {
                    String errMessage = "Unable to download attachment due to " + e.getClass() + " for " + context + " using file reference " + attachment.getFileReference();

                    if (e instanceof HandlerException) {
                        if (e.getMessage().contains("The object was not found or access denied") || (e.getMessage().contains("The request timed out."))) {
                            log.warn(String.format("%s [%s]", errMessage, e.getMessage()));
                        } else {
                            log.error(errMessage, e);
                        }
                    } else {
                        log.error(errMessage, e);
                    }


                    currentAttachment.setData(canntGetAttachmentBodyNotif.getBytes());
                }
            }
        }
        return attachments;
    }

    protected  Address[] tranAddr(String addr) {
        if (StringUtils.isEmpty(addr)) {
            return new InternetAddress[0];
        }
        String[] tempAddr;
        if (addr.indexOf(',') > -1) {
            tempAddr = addr.trim().split(",");
        } else if (addr.indexOf(';') > -1) {
            tempAddr = addr.trim().split(";");
        } else {
            tempAddr = new String[]{addr};
        }
        InternetAddress[] address = null;
        if (tempAddr != null) {
            address = new InternetAddress[tempAddr.length];
            for (int i = 0; i < tempAddr.length; i++) {
                address[i] = new InternetAddress();
                String addressStr = tempAddr[i].indexOf('<') > -1 ?
                        tempAddr[i].substring(tempAddr[i].indexOf('<') + 1, tempAddr[i].indexOf('>')) : tempAddr[i];
                address[i].setAddress(addressStr);
            }
        }

        return address == null ? new InternetAddress[0] : address;
    }

    public Long getTimeCorrection(Account account) {
        Long resultCorrection = null;
        HttpClient httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());

        String serverUrl = ("ssl".equals(account.getReceiveTs()) ? "https" : "http") + "://" + account.getReceiveHost();
        final OptionsMethod method = new OptionsMethod(serverUrl);

        final String auth = "Basic " + new String(Base64.encodeBase64(new StringBuilder(account.getLoginName() + ":" + account.getPassword()).toString().getBytes()));
        method.addRequestHeader("Authorization", auth);
        try {
            int result = httpClient.executeMethod(method);
            if (result == 200)

            {
                for (org.apache.commons.httpclient.Header header : method.getResponseHeaders()) {
                    if ("Date".equals(header.getName())) {
                        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
                        resultCorrection = System.currentTimeMillis() - sdf.parse(header.getValue()).getTime();
                        break;
                    }
                }
            }
        }
        catch (Exception e) {
            log.warn(String.format("can't get current server time for account %s", account.getName()));
        }
        return resultCorrection;
    }

    public boolean isMessageTooOld(Account account, Date messageSentDate, String context) throws MessagingException {
        if (messageSentDate == null) {
            log.warn(String.format("we have a message with no sent date for %s, allowing message", context));
            return false;
        } else if (account.getRegister_time() == null) {
            log.warn(String.format("we are process an account with no register time. this behavior is not understood yet %s, we will accept this message", context));
            return false;
        } else {
            return (System.currentTimeMillis() - (messageSentDate.getTime() + timeCorrection)) > 1000l * 60 * 60 * 24 * emailDaysCutoff;
        }
    }

    public List<ApplicationData> getEmails(StopWatch watch, String context, Map<String, String[]> syncKeys,
                                           com.zynku.sync.activesync.model.Folder folder,
                                           ActiveSyncController controller)
    {
        if(log.isTraceEnabled())
            log.trace(String.format("getEmails(context=%s ...)", context));

        try
        {
            List<ApplicationData> emails = null;

            StopWatchUtils.newTask(watch, "getEmails", context, log);

            if (syncKeys.get(folder.getServerId()) == null || syncKeys.get(folder.getServerId()).length == 0) {
                String[] syncKey = getDefaultSyncKey(context);

                emails = getEmails(syncKey, folder, context, controller);

                if (emails != null) {
                    syncKeys.put(folder.getServerId(), syncKey);
                }
            } else {
                String[] syncKey = syncKeys.get(folder.getServerId());

                if (syncKey.length == 1) {
                    if (log.isDebugEnabled())
                        log.debug(String.format("we only have 1 sync key for %s", context));

                    String firstSyncKey = "" + syncKey[0];

                    emails = getEmails(syncKey, folder, context, controller);

                    if (log.isDebugEnabled())
                        log.debug(String.format("after getEmails synckey is %s for %s", syncKey[0], context));

                    if (emails != null) {
                        if (!firstSyncKey.equals(syncKey[0])) {
                            syncKeys.put(folder.getServerId(), new String[]{firstSyncKey, syncKey[0]});
                        } else {
                            if (log.isDebugEnabled())
                                log.debug(String.format("synckey %s is not updated, it is the same as the first in the list for %s", syncKey[0], context));
                        }
                    } else {
                        log.warn(String.format("sync key %s is no longer valid for %s, time to reset back to 0", syncKey[0], context));

                        syncKeys.put(folder.getServerId(), getDefaultSyncKey(context));

                        emails = getEmails(syncKeys.get(folder.getServerId()), folder, context, controller);
                    }
                } else if (syncKey.length > 1) {
                    // we have more then 1 sync key, will try to use them.
                    if (log.isDebugEnabled())
                        log.debug(String.format("we have %d sync keys for %s", syncKey.length, context));

                    String[] firstSyncKey = {"-1"};
                    String[] syncKeyCurrent = {"-1"};

                    for (String key : syncKey) {
                        syncKeyCurrent = new String[]{"" + key};
                        firstSyncKey = new String[]{"" + key};

                        emails = getEmails(syncKeyCurrent, folder, context, controller);

                        if (emails != null) {
                            break;
                        }
                    }

                    if (emails != null) {
                        if (!firstSyncKey[0].equals(syncKeyCurrent[0])) {
                            String[] aggregatedSyncKeys = new String[3];
                            aggregatedSyncKeys[0] = syncKeyCurrent[0];
                            aggregatedSyncKeys[1] = syncKeys.get(folder.getServerId())[0];
                            aggregatedSyncKeys[2] = syncKeys.get(folder.getServerId())[1];
                            syncKeys.put(folder.getServerId(), aggregatedSyncKeys);
                        }
                    } else {
                        log.info(String.format("sync pool has no valid keys for " + context + " and serverId " + folder.getServerId() + " therefore reset it's sync key"));
                        syncKeys.put(folder.getServerId(), getDefaultSyncKey(context));
                        emails = getEmails(syncKeys.get(folder.getServerId()), folder, context, controller);
                    }
                }
            }
            return emails;
        }
        catch (Throwable t) {
            throw new SystemException("Unable to retrieve emails for " + context, t);
        }
    }

}