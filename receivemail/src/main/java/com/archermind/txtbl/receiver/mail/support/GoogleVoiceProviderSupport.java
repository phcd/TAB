package com.archermind.txtbl.receiver.mail.support;

import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.receiver.mail.bean.GoogleVoiceProcess;
import com.archermind.txtbl.utils.StopWatch;
import com.archermind.txtbl.utils.StopWatchUtils;
import com.sun.mail.pop3.POP3Folder;
import org.jboss.logging.Logger;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.security.Security;
import java.util.*;

public class GoogleVoiceProviderSupport extends NewProviderSupport {

    private static final Logger log = Logger.getLogger(GoogleVoiceProviderSupport.class);


    protected Message getFullyLoad(Message message, Folder folder, String context) throws MessagingException {
        try {
            ((GoogleVoiceFolder) folder).loadFully((GoogleVoiceMessage) message);
        } catch (Exception e) {
            throw new MessagingException("Exception getting googlevoice message body " + e.getMessage());
        }
        Message wrappedMessage = new MimeMessage((MimeMessage) message);
        wrappedMessage.setSentDate(message.getSentDate());
        return wrappedMessage;
    }

    /**
     * checks message sent date againsts a predefined email days cutoff
     *
     * @param message
     * @param migratedAccount
     * @return
     * @throws javax.mail.MessagingException
     */
    public boolean isMessageTooOld(Account account, Message message, String context) throws MessagingException {

        if (message.getSentDate() == null) {
            log.warn(String.format("we have a message with no sent date for %s, allowing message", context));
            return false;
        }

        //This should handle the first login case, we want to take up to x days worth of old emails and not be rejected
        //by the Too Old check
        if (account.getLast_received_date() == null) {
            log.warn(String.format("we are process an account with no register time. this behavior is not understood yet %s, we will accept this message", context));
            return (System.currentTimeMillis() - message.getSentDate().getTime()) > 1000l * 60 * 60 * 24 * emailDaysCutoff;
        }

        return isOlder(message.getSentDate(), account.getLast_received_date());

    }


    //GoogleVoice needs its own processMessage since it does not use the messagestore to determine which new
    //messages to get, it uses last received data

    public boolean processMessage(Account account, Message message, int messageNumber, String messageId, Folder folder, Set<String> storeMessageIds, String storeBucket, String context, StopWatch watch, FolderHelper folderHelper) throws Exception {
        boolean result = false;

        long start = System.nanoTime();

        try {
            StopWatchUtils.newTask(watch, String.format("msgNum=%s, storeCheck", messageNumber), context, log);

            //Log.debug(log4j, "msgNum=%d, checking if messageId=%s is known for %s", messageNumber, messageId, context);

            //Log.debug(log4j, "msgNum=%d, checking if messageId=%s is too old or too big or sent for %s", messageNumber, messageId, context);

            StopWatchUtils.newTask(watch, String.format("msgNum=%s, tooOld-tooBig-check", messageNumber), context, log);

            if (isMessageTooOld(account, message, context)) {
                log.warn(String.format("msgNum=%d, messageId=%s, message is too old, sentDate=%s, discarding, for %s", messageNumber, messageId, message.getSentDate(), context));

                // this message is too old and needs to be ignored
            } else {
                message = getFullyLoad(message, folder, context);
                int messageSize = (folder instanceof POP3Folder) ? ((POP3Folder) folder).getMessageSize(messageNumber) : getMessageSize(message, account);

                if (messageSize > maximumMessageSize) {
                    log.warn(String.format("msgNum=%d, messageId=%s, message is too big %d bytes, discarding for %s", messageNumber, messageId, messageSize, context));
                    saveMessageDroppedNotification(account, messageNumber, message, messageSize);
                } else {
                    if (log.isDebugEnabled())
                        log.debug(String.format("msgNum=%d, messageId=%s, message is new for %s", messageNumber, messageId, context));

                    try {
                        new EmailSaveProcess(account, watch, context).process(message, messageNumber, messageId);
                    }
                    catch (Throwable e) {
                        log.fatal(String.format("msgNum=%d, messageId=%s, subject=%s message saving failed for %s", messageNumber, messageId, message.getSubject(), context), e);

                        StopWatchUtils.newTask(watch, String.format("msgNum=%s, removeFromStore", messageNumber), context, log);

                    }

                    result = true;
                }
            }
        }
        finally {
            if (log.isDebugEnabled())
                log.debug(String.format("msgNum=%d, checked if messageId=%s is known in %dms for %s", messageNumber, messageId, (System.nanoTime() - start) / 1000000, context));
        }

        return result;
    }


    public boolean isOlder(Date receivedDate, Date lastReceivedDate) {

        //now we check to see if the timestamp of this message is newer than that of
        //the last received date, so we are getting new google voice messages only
        Calendar newMsgReceivedDate = Calendar.getInstance(TimeZone.getDefault());
        Calendar oldReceivedDate = Calendar.getInstance(TimeZone.getDefault());
        newMsgReceivedDate.setTime(receivedDate);
        oldReceivedDate.setTime(lastReceivedDate);
        if (log.isDebugEnabled())
            log.debug(String.format("Calendar Message age check.  Sent date %s %s, received date %s %s", newMsgReceivedDate.getTime().getTime(), newMsgReceivedDate.getTime(), oldReceivedDate.getTime().getTime(), oldReceivedDate.getTime()));


        if (newMsgReceivedDate.getTime().compareTo(oldReceivedDate.getTime()) <= 0) return true;

        if (log.isDebugEnabled())
            log.debug(String.format("Calendar explicit equal check:  Seconds %s %s \n, minutes %s %s \n, hours %s %s",
                    newMsgReceivedDate.get(Calendar.SECOND), oldReceivedDate.get(Calendar.SECOND),
                    newMsgReceivedDate.get(Calendar.MINUTE), oldReceivedDate.get(Calendar.MINUTE),
                    newMsgReceivedDate.get(Calendar.HOUR), oldReceivedDate.get(Calendar.HOUR)));

        //need to an explicit check if two dates are equal
        return newMsgReceivedDate.get(Calendar.SECOND) == oldReceivedDate.get(Calendar.SECOND) && newMsgReceivedDate.get(Calendar.MINUTE) == oldReceivedDate.get(Calendar.MINUTE) && newMsgReceivedDate.get(Calendar.HOUR) == oldReceivedDate.get(Calendar.HOUR) && newMsgReceivedDate.get(Calendar.DAY_OF_YEAR) == oldReceivedDate.get(Calendar.DAY_OF_YEAR) && newMsgReceivedDate.get(Calendar.YEAR) == oldReceivedDate.get(Calendar.YEAR);
    }

    public GoogleVoiceFolder connect(Account account, String context, StopWatch watch, GoogleVoiceProcess process) throws IOException, MessagingException {

        StopWatchUtils.newTask(watch, "Get session", context, log);
        Session session = Session.getInstance(getMailProperties(account));
        StopWatchUtils.newTask(watch, "Get store", context, log);
        Store store = session.getStore("pop3");
        GoogleVoiceFolder inbox = new GoogleVoiceFolder(session, process.getVoice());

        StopWatchUtils.newTask(watch, "Open folder", context, log);
        inbox.open(Folder.READ_ONLY);

        return inbox;

    }


    private Properties getMailProperties(Account account) {
        Properties props = new Properties();
        props.setProperty("mail.pop3.port", "995");
        props.setProperty("mail.pop3.connectiontimeout", "30000");
        if ("ssl".equals(account.getReceiveTs())) {
            Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
            props.setProperty("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.setProperty("mail.pop3.socketFactory.fallback", "false");
            props.setProperty("mail.pop3.socketFactory.port", account.getReceivePort());
        } else if ("tls".equals(account.getReceiveTs())) {
            props.setProperty("mail.pop3.starttls.enable", "true");
            java.security.Security.setProperty("ssl.SocketFactory.provider", "com.archermind.txtbl.mail.DummySSLSocketFactory");
        }
        return props;
    }


}
