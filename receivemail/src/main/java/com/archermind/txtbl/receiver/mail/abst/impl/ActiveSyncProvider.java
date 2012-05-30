package com.archermind.txtbl.receiver.mail.abst.impl;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.Body;
import com.archermind.txtbl.domain.Email;
import com.archermind.txtbl.exception.SystemException;
import com.archermind.txtbl.mail.store.MessageStore;
import com.archermind.txtbl.mail.store.MessageStoreException;
import com.archermind.txtbl.mail.store.MessageStoreFactory;
import com.archermind.txtbl.receiver.mail.abst.Provider;
import com.archermind.txtbl.receiver.mail.dal.DALDominator;
import com.archermind.txtbl.receiver.mail.support.ActiveSyncSupport;
import com.archermind.txtbl.receiver.mail.utils.ProviderStatistics;
import com.archermind.txtbl.utils.*;
import com.zynku.sync.activesync.control.ActiveSyncController;
import com.zynku.sync.activesync.control.handler.HandlerException;
import com.zynku.sync.activesync.model.ApplicationData;
import com.zynku.sync.activesync.wbxml.codepage.CalendarCodePageField;
import com.zynku.sync.util.CalendarUtils;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ActiveSyncProvider implements Provider {
    private static final Logger log = Logger.getLogger(ActiveSyncProvider.class);


    private static ProviderStatistics statistics = new ProviderStatistics();

    // to define start time or frequency for calendarProcessingTime set it's value in next format "start=5:00 AM;frequency=1h"
    private static String calendarProcessingTime = SysConfigManager.instance().getValue("sendCalendarTime", "disabled");

    protected ActiveSyncSupport support;

    private MessageStore messageStore = MessageStoreFactory.getStore();

    private String deviceType;
    private String deviceId;

    public ActiveSyncProvider(ActiveSyncSupport support, String deviceType, String deviceId) {
        this.support = support;
        this.deviceType = deviceType;
        this.deviceId = deviceId;
    }

    /**
     * Corresponding provider's method implementation
     *
     * @param account account instance
     * @return received new messages number
     */
    public int receiveMail(Account account) {
        if (log.isTraceEnabled())
            log.trace(String.format("receiveMail(account=%s)", String.valueOf(account)));

        String context = String.format("account=%s, uid=%s, email=%s", account.getId(), account.getUser_id(), account.getName());

        log.info("receiving mail for " + context);

        StopWatch watch = new StopWatch("mailcheck " + context);

        StopWatchUtils.newTask(watch, "loadAccountLatestReceiveInfo", context, log);

        if (!DALDominator.loadAccountLatestReceiveInfo(account)) {
            log.warn("account has been removed by email checks continue for " + context);
            // this means that we can't find account anymore - probably to do with deletion
            return 0;
        }

        int newMessages = 0;
        int folderDepth = 0;

        try {

            if (log.isTraceEnabled())
                log.trace("getMessages for context=" + context);
            StopWatchUtils.newTask(watch, "getMessageIds", context, log);
            Set<String> storeMessageIds = messageStore.getMessages(account.getId(), account.getCountry(), context, watch);

            if (log.isTraceEnabled())
                log.trace("getBucket for context=" + context);
            String messageStoreBucket = messageStore.getBucket(account.getId());

            if (log.isTraceEnabled())
                log.trace("initialFolderSync for context=" + context);
            StopWatchUtils.newTask(watch, "initialFolderSync", context, log);
            ActiveSyncController controller = support.getController(account, deviceId, deviceType);
            controller.initialFolderSync();

            if (log.isTraceEnabled())
                log.trace("getSyncKeys for context=" + context);
            StopWatchUtils.newTask(watch, "getSyncKeys", context, log);
            Map<String, String[]> syncKeys = support.getSyncKeys(account.getActive_sync_key());

            if (log.isTraceEnabled())
                log.trace("getFolders for context=" + context);
            StopWatchUtils.newTask(watch, "getFolders", context, log);
            List<com.zynku.sync.activesync.model.Folder> folders = controller.getContext().getFolders(com.zynku.sync.activesync.model.FolderType.DEFAULT_INBOX);

            if (log.isTraceEnabled())
                log.trace("processCalendarUpdates for context=" + context);
            processCalendarUpdates(account, controller, watch, context);

            boolean bIsFirstTime = isFirstTime(account);
            if (log.isTraceEnabled())
                log.trace("bIsFirstTime=" + (bIsFirstTime ? "true" : "false"));

            if (bIsFirstTime) {
                int[] result = support.getMailFirstTime(folders, account, syncKeys, watch, context, messageStoreBucket, controller, storeMessageIds);
                newMessages = result[0];
                folderDepth = result[1];
                if (log.isDebugEnabled())
                    log.debug(String.format("first time registration for %s resulted in %s new messages", context, newMessages));
            } else if (isMigration(account)) {
                handleMigration(folders, account, syncKeys, watch, context, messageStoreBucket, controller);
            } else {
                int[] result = getMail(folders, account, syncKeys, watch, context, messageStoreBucket, controller, storeMessageIds);
                newMessages = result[0];
                folderDepth = result[1];
            }
        }
        catch (Throwable e) {
            throw new SystemException(String.format("unexpected failure during receive mail for %s", context), e);

        }
        finally {
            StopWatchUtils.newTask(watch, String.format("closeConnection (%d new mails, %d in folder)", newMessages, folderDepth), context, log);

            StopWatchUtils.newTask(watch, "logMailCheckEvent", context, log);
            
            watch.stop();

            log.info(ReceiverUtilsTools.printWatch(watch, folderDepth, newMessages, false));

            String summary = statistics.enterStats(ActiveSyncProvider.class.getName(), folderDepth, newMessages, watch.getTotalTimeMillis());

            log.info(String.format("completed mailcheck for %s, folderDepth=%d, newMessages=%s, time=%sms [%s]", context, folderDepth, newMessages, watch.getTotalTimeMillis(), summary));

        }

        return newMessages;
    }

    private boolean isMigration(Account account) {
        return account.getLast_mailcheck() != null && StringUtils.isEmpty(account.getActive_sync_key());
    }

    private boolean isFirstTime(Account account) {
        return account.getLast_received_date() == null && account.getLast_mailcheck() == null;
    }


    private void processCalendarUpdates(Account account, ActiveSyncController controller, StopWatch watch, String context) throws DALException, IOException, HandlerException {

        if (log.isTraceEnabled())
            log.trace(String.format("processCalendarUpdates(account=%s, context=%s ...)", String.valueOf(account), String.valueOf(context)));
        Calendar now = new GregorianCalendar();

        Calendar calendarProcessDate = new GregorianCalendar();

        Map<String, String> calendarSchedule = new HashMap<String, String>();

        if (!"disabled".equalsIgnoreCase(calendarProcessingTime)) {
            String[] calendarParams = calendarProcessingTime.split(";");

            for (String calendarParam : calendarParams) {
                String[] tmpParams = calendarParam.split("=");
                if (calendarParam.length() > 1) {
                    calendarSchedule.put(tmpParams[0], tmpParams[1]);
                }
            }

            if (account.getLast_calendar() != null) {
                String frequencyProcessingTime = calendarSchedule.get("frequency");
                if (frequencyProcessingTime == null) {
                    calendarProcessDate.setTime(account.getLast_calendar());
                    calendarProcessDate.add(Calendar.DATE, 1);
                } else {
                    int hourNumber = Integer.parseInt(frequencyProcessingTime.substring(0, frequencyProcessingTime.indexOf('h')));
                    calendarProcessDate.setTime(account.getLast_calendar());
                    calendarProcessDate.add(Calendar.HOUR, hourNumber);
                }
            } else {
                String startProcessingTime = calendarSchedule.get("start");
                calendarProcessDate.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startProcessingTime.substring(0, startProcessingTime.indexOf(':'))));
                calendarProcessDate.set(Calendar.MINUTE, Integer.parseInt(startProcessingTime.substring(startProcessingTime.indexOf(':') + 1, startProcessingTime.indexOf(' '))));
            }

            if (calendarProcessDate.before(now)) {
                support.updateCalendarDate(now.getTime(), account);

                StopWatchUtils.newTask(watch, "Start calendar processing", context, log);
                SimpleDateFormat sdfTime = new SimpleDateFormat("h:mm a");
                log.info(String.format("Send event notification to %s", account.getName()));
                StopWatchUtils.newTask(watch, "Get folders", context, log);
                List<com.zynku.sync.activesync.model.Folder> foldersCalendar = controller.getContext().getFolders(com.zynku.sync.activesync.model.FolderType.DEFAULT_CALENDAR);
                for (com.zynku.sync.activesync.model.Folder folderCalendar : foldersCalendar) {
                    StopWatchUtils.newTask(watch, "Get calendars", context, log);
                    List<ApplicationData> calendars = controller.getCalendars(folderCalendar.getServerId(), new Date()); //receive all calendars  for current time
                    // verify each calendar object
                    SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd, yyyy");
                    StopWatchUtils.newTask(watch, "Processing calendars", context, log);
                    if (calendars.size() > 0) {
                        Body emailBody = new Body();
                        StringBuffer mailBody = new StringBuffer(account.getName()).append(" here is your schedule for: ")
                                .append(sdf.format(new Date())).append("\n\r\n\r");
                        TreeMap<Date, ApplicationData> events = new TreeMap<Date, ApplicationData>();
                        for (ApplicationData _calendar : calendars) {
                            events.put(CalendarUtils.getStartTime(_calendar).getTime(), _calendar);
                        }
                        for (Map.Entry<Date, ApplicationData> event : events.entrySet()) {
                            String text = event.getValue().get(CalendarCodePageField.EndTime.name());
                            String ID = event.getValue().get(CalendarCodePageField.TimeZone.name());
                            mailBody.append(sdfTime.format(event.getKey())).append(" - ")
                                    .append(sdfTime.format(CalendarUtils.getTime(text, ID).getTime())).append("    ")
                                    .append(event.getValue().get("Subject")).append("\n\r");
                        }
                        Email email = new Email();
                        email.setSubject("Daily Agenda as of " + sdfTime.format(new Date(System.currentTimeMillis())));
                        email.setFrom("Peek Calendar");
                        email.setTo(account.getName());
                        email.setUserId(account.getUser_id());
                        email.setStatus("0");
                        emailBody.setData(mailBody.toString().getBytes());
                        DALDominator.newSaveMail(account, email, emailBody);
                    }
                }

            }
        }


    }


    private boolean handleMigration(List<com.zynku.sync.activesync.model.Folder> folders,
                                    Account account, Map<String, String[]> syncKeys,
                                    StopWatch watch,
                                    String context,
                                    String messageStoreBucket,
                                    ActiveSyncController controller) throws MessageStoreException, DALException {
        if(log.isTraceEnabled())
            log.trace(String.format("handleMigration(account=%s, context=%s, messageStoreBucket=%s ... )", String.valueOf(account), context, messageStoreBucket));

        StopWatchUtils.newTask(watch, "handleMigration", context, log);

        int folderDepth = 0;

        List<String> ids = new ArrayList<String>();

        for (com.zynku.sync.activesync.model.Folder folder : folders) {
            List<ApplicationData> emails = support.getEmails(watch, context, syncKeys, folder, controller);

            if (emails != null) {
                folderDepth += emails.size();
                ActiveSyncSupport.SortOrder sortOrder = support.getSortOrder(emails);

                for (int messageNumber = 1; messageNumber <= emails.size(); messageNumber++) {
                    ApplicationData email = support.getNextEmail(sortOrder, messageNumber, emails);

                    Date mailSentDate = support.getDateFromString(email.get("DateReceived"));

                    if (mailSentDate != null) {
                        String messageId = "" + mailSentDate.getTime();
                        ids.add(messageId + " " + messageId);
                    }

                }
            }
        }

        StopWatchUtils.newTask(watch, "addMessageInBulk", context, log);
        support.getMessageIdStore().addMessageInBulk(account.getId(), messageStoreBucket, IdUtil.encodeMessageIds(ids), account.getCountry());

        StopWatchUtils.newTask(watch, "updateAccount", context, log);
        account.setActive_sync_key(support.getSyncKey(syncKeys));
        support.updateAccount(account, null, 0, folderDepth, null);
        //support.updateAccountData(folderDepth, null, syncKeys, account);

        return true;
    }

    /**
     * This method purposed for getting mail after first time mail cheking
     *
     * @param folders            folders list to check
     * @param account            email account
     * @param syncKeys           sync keys map
     * @param watch              stop watch instance
     * @param context            context message
     * @param messageStoreBucket message id's store bucket
     * @param controller         email controller instance
     * @return received new messages number
     * @throws Exception raised exception
     */
    private int[] getMail(List<com.zynku.sync.activesync.model.Folder> folders,
                          Account account,
                          Map<String, String[]> syncKeys,
                          StopWatch watch,
                          String context,
                          String messageStoreBucket,
                          ActiveSyncController controller,
                          Set<String> storeMessageIds) {

        if(log.isTraceEnabled())
            log.trace(String.format("getMail(account=%s, context=%s, messageStoreBucket=%s ...)", String.valueOf(account), context, messageStoreBucket));

        Date lastMessageReceivedDate = account.getLast_received_date();


        int newMessages = 0;
        int folderDepth = 0;

        for (com.zynku.sync.activesync.model.Folder folder : folders)
        {
            List<ApplicationData> emails = support.getEmails(watch, context, syncKeys, folder, controller);

            if (emails != null) {
                folderDepth += emails.size();

                if (log.isDebugEnabled())
                    log.debug(String.format("seems like we have %d new emails, for %s", emails.size(), context));

                for (int messageNumber = 1; messageNumber <= emails.size(); messageNumber++) {
                    ApplicationData email = emails.get(messageNumber - 1);

                    Date mailSentDate = support.getDateFromString(email.get("DateReceived"));

                    String messageId = support.getMessageId(email);

                    if (support.processMessage(account, messageNumber, storeMessageIds, messageStoreBucket, email, messageId, context, watch, false, controller)) {
                        if (messageNumber == 1) {
                            lastMessageReceivedDate = mailSentDate;
                        }

                        newMessages++;
                    }
                }

                StopWatchUtils.newTask(watch, "updateAccount", context, log);
                String syncKey = support.getSyncKey(syncKeys);
                if (log.isDebugEnabled())
                    log.debug(String.format("updating sync key from %s to %s for %s", account.getActive_sync_key(), syncKey, context));
                account.setActive_sync_key(support.getSyncKey(syncKeys));
                support.updateAccountData(folderDepth, newMessages, lastMessageReceivedDate, account);
            }
        }
        return new int[]{newMessages, folderDepth};
    }



}
