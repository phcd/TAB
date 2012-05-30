package com.archermind.txtbl.receiver.mail.abst.impl;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.dal.business.IMspTokenService;
import com.archermind.txtbl.dal.business.impl.MspTokenService;
import com.archermind.txtbl.dal.business.impl.UserService;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.MspToken;
import com.archermind.txtbl.mail.store.MessageStoreException;
import com.archermind.txtbl.receiver.mail.abst.Provider;
import com.archermind.txtbl.receiver.mail.bean.MSPEmailHeaders;
import com.archermind.txtbl.receiver.mail.bean.MSPFullFolderListResponse;
import com.archermind.txtbl.receiver.mail.bean.MSPLoginBean;
import com.archermind.txtbl.receiver.mail.bean.MSPMessageHeader;
import com.archermind.txtbl.receiver.mail.bean.MSPSubscribeBean;
import com.archermind.txtbl.receiver.mail.dal.DALDominator;
import com.archermind.txtbl.receiver.mail.filter.MSGFilter;
import com.archermind.txtbl.receiver.mail.support.EmailIdStoreProcess;
import com.archermind.txtbl.receiver.mail.support.EmailSaveProcess;
import com.archermind.txtbl.receiver.mail.support.NewMSPSupport;
import com.archermind.txtbl.utils.*;
import org.apache.commons.lang.StringUtils;
import org.jboss.logging.Logger;

import javax.mail.AuthenticationFailedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import java.text.SimpleDateFormat;
import java.util.*;

public class NewMSPProvider implements Provider
{

    private static final Logger log = Logger.getLogger(NewMSPProvider.class);


    private NewMSPSupport mspSupport;
    public MSGFilter msgFilter;


    private enum MailAction {
        FIRST_TIME,
        MIGRATION,
        GET_MAIL
    }

    public NewMSPProvider(NewMSPSupport support) {
        mspSupport = support;
    }

    public int receiveMail(Account account)
    {
        String context = String.format("account=%s, uid=%s, email=%s", account.getId(), account.getUser_id(), account.getName());

        log.info(String.format("receiving mail for %s", context));

        StopWatch watch = new StopWatch("mailcheck " + context);

        StopWatchUtils.newTask(watch, "Getting message id's from store", context, log);

        String messageStoreBucket = mspSupport.getMessageIdStore().getBucket(account.getId()); // calculate once

        try
        {
            StopWatchUtils.newTask(watch, "loadAccountLatestReceiveInfo", context, log);
            if (!DALDominator.loadAccountLatestReceiveInfo(account))
            {
                log.warn(String.format("account has been removed by email checks continue for %s", context));
                // this means that we can't find account anymore - probably to do with deletion
                return 0;
            }
            
            if (account.getLast_received_date() == null)
            {
                if (account.getLast_mailcheck() != null)
                {
                    return getMailWithToken(account, messageStoreBucket, MailAction.MIGRATION, watch, context);
                }
                else
                {
                    return getMailWithToken(account, messageStoreBucket, MailAction.FIRST_TIME, watch, context);
                }
            }
            else
            {
                return getMailWithToken(account, messageStoreBucket, MailAction.GET_MAIL, watch, context);
            }
        }
        catch (Throwable e)
        {
            log.error(String.format("unable to receive mail account=%s, uid=%s", account.getName(), account.getUser_id()), e);
        }

        return 0;
    }

    private String getToken(Account account) throws Exception
    {
        IMspTokenService service = new MspTokenService();

        MspToken mspToken = service.getMspToken(account.getUser_id(), account.getName());


        if (mspToken != null)
        {
            byte[] token = service.getMspToken(account.getUser_id(), account.getName()).getToken_id();

            if (token != null)
            {
                return new String(token, "utf-8");
            }
        }


        return "";
    }


    /**
     * @param account
     * @param bucket
     * @throws javax.mail.MessagingException
     * @throws com.archermind.txtbl.mail.store.MessageStoreException
     */
    private int getMailWithToken(Account account, String bucket, MailAction action, StopWatch watch, String context) throws MessagingException, MessageStoreException, DALException {
        if (mspSupport.exceededMaximumLoginFailures(account)) {
            log.warn(String.format("exceeded maximum login failures with %d attempts for %s", account.getLogin_failures(), context));
            return 0;
        }

        int folderDepth = 0;
        int newMessages = 0;

        try {

            MSPLoginBean credentials = getCredentials(account);
            List<MSPMessageHeader> messageHeaders = getMessageHeaders(account, watch, context, credentials);

            if(messageHeaders == null) {
                return 0;
            }

            StopWatchUtils.newTask(watch, "Process message headers ", context, log);
            folderDepth = messageHeaders.size();
            if (messageHeaders.size() > 0) {
                log.info(String.format("we have %d messages to process for %s with action %s", messageHeaders.size(), context, action));

                StopWatchUtils.newTask(watch, "getStoreMessages", context, log);
                Set<String> storedMessageIds = mspSupport.getMessageIdStore().getMessages(account.getId(), account.getCountry(), context, watch);

                switch (action) {
                  case FIRST_TIME: {
                      newMessages = mspSupport.handleFirstTime(account, bucket, watch, context, folderDepth, newMessages, credentials, messageHeaders, storedMessageIds);
                      break;
                  }

                  case MIGRATION: {
                      StopWatchUtils.newTask(watch, "Processing migration", context, log);
                      mspSupport.handleMigratedAccount(messageHeaders, account, bucket, folderDepth, new Date());
                      break;
                  }

                  case GET_MAIL: {
                      EmailIdStoreProcess emailProcess = mspSupport.getEmailProcess(account, bucket, watch, context, storedMessageIds);
                      Date lastMessageReceivedDate = null;
                      StopWatchUtils.newTask(watch, "Start mail receiving", context, log);
                      for (int messageNumber = 0; messageNumber < messageHeaders.size(); messageNumber++){

                          MSPMessageHeader currentMessageHeader = messageHeaders.get(messageNumber);
                          String messageId = currentMessageHeader.getMessageId();
                          //Processing current mail
                          StopWatchUtils.newTask(watch, "Processing email", context, log);
                          Message savedMessage = mspSupport.processMessage(account, messageNumber + 1, storedMessageIds, bucket, messageId, currentMessageHeader.getHasAttachments(), credentials, currentMessageHeader.getSize(), emailProcess, context, watch);
                          if (savedMessage != null) {
                              if (lastMessageReceivedDate == null || (savedMessage.getSentDate() != null && lastMessageReceivedDate.before(savedMessage.getSentDate()))) {
                                  lastMessageReceivedDate = savedMessage.getSentDate();
                              }

                              log.info(String.format("identified new message %s for %s, extracting", messageId, context));
                              newMessages++;
                          }
                      }
                      emailProcess.complete();
                      //TODO - Paul - reconcile and stuff
                      // Mail was received successfully. Now update last_received_date in email account
                      StopWatchUtils.newTask(watch, "Updating account", context, log);
                      mspSupport.updateAccount(account, null, 0, folderDepth, lastMessageReceivedDate);
                  }
                }
            } else {
                log.info(String.format("no new messages for %s, extracting", context));
                mspSupport.updateAccount(account, null, 0, folderDepth, null);
            }

        } catch (Throwable e) {
            log.error(String.format("unable to receive mail %s", context), e);
        }
        finally
        {
            StopWatchUtils.newTask(watch, "Logging mailChecked event", context, log);
            watch.stop();
            log.info(ReceiverUtilsTools.printWatch(watch, folderDepth, newMessages, true));
        }

        return newMessages;
    }





    private List<MSPMessageHeader> getMessageHeaders(Account account, StopWatch watch, String context, MSPLoginBean credentials) throws Exception {

        String subscribeUrl = SysConfigManager.instance().getValue("subscribe.url");

        getAccountFolderAndMessagingId(account);
        String fullMessageListResponse = mspSupport.getFullMessageList(account.getMessaging_id(), account.getFolder_id(), credentials, watch, context);

        // log.info("-----------------------fullMessageList-------------------------------");
        // log.info(fullMessageList);
        // log.info("---------------------------------------------------------------------\n");

        MSPEmailHeaders mailHeaders = mspSupport.getMailHeaders(fullMessageListResponse, watch, context);
        boolean reLoggedin = false;
        if (mailHeaders.getError() != null)
        {
            MSPFullFolderListResponse fullFolderResponse = mspSupport.getFullFolderList(credentials, "", watch, context);

            String error = fullFolderResponse.getError();

            if (mspSupport.hasFatalFailure(error))
            {
                String warning = String.format("Windows Live Hotmail is temporarily unavailable (unable to process %s)", context);
                log.warn(warning);
                log.error(warning);
            }

            if (mspSupport.hasRecoverableFailure(error))
            {
                log.info(String.format("sign in failed, might be because the token expired %s, getting new token", context));

                if (StringUtils.isNotEmpty(account.getPassword()))
                {
                    StopWatchUtils.newTask(watch, "Trying to login", context, log);
                    credentials = mspSupport.login(account.getName(), account.getPassword());
                    //Check if login was successful
                    if (StringUtils.isNotEmpty(credentials.getError()))
                    { //increment login failures number and save it
                        log.info(String.format("login for %s is failed with message %s", context, credentials.getError()));
                        mspSupport.handleLoginFailures(context, account);
                        throw new AuthenticationFailedException(credentials.getError());
                    }
                    else
                    {
                        mspSupport.updateAccount(account, null, 0, account.getFolder_depth(), null);

                        StopWatchUtils.newTask(watch, "List Folders", context, log);
                        fullFolderResponse = mspSupport.getFullFolderList(credentials, "", watch, context);
                        account.setMessaging_id(fullFolderResponse.getMessagingId());
                        account.setFolder_id(fullFolderResponse.getInboxFolderId());
                        reLoggedin = true;
                    }

                }
                else
                {
                    log.warn(String.format("missing password for %s", context));
                }
            }
            else
            {
                account.setMessaging_id(fullFolderResponse.getMessagingId());
                account.setFolder_id(fullFolderResponse.getInboxFolderId());
            }

            saveAccountFolderAndMessagingId(account);

            fullMessageListResponse = mspSupport.getFullMessageList(account.getMessaging_id(), account.getFolder_id(), credentials, watch, context);
            mailHeaders = mspSupport.getMailHeaders(fullMessageListResponse, watch, context);
        }

        if (reLoggedin || mspSupport.isTimeToReconcile(account)) {
            log.info(String.format("attempting to subscribe for msp alerts %s", context));

            StopWatchUtils.newTask(watch, "Trying to subscribe for email alert", context, log);
            int expiresInDays = Integer.parseInt(SysConfigManager.instance().getValue("subscribeAlertIntervalInDays", "15"));
            Calendar expireOn = new GregorianCalendar();
            expireOn.add(Calendar.DAY_OF_YEAR,expiresInDays);
            int timeZoneOffset = expireOn.get(Calendar.ZONE_OFFSET)/3600000;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'"+(timeZoneOffset > 0?"+":"")+timeZoneOffset+":00'");
            MSPSubscribeBean subscribeBean = mspSupport.subcribeAlert(account.getMessaging_id(), credentials, subscribeUrl, watch, context, sdf.format(expireOn.getTime()));
            String transactionID = subscribeBean.getTransactionID();

            String webServiceCustomerID = subscribeBean.getWebServiceCustomerID();

            StopWatchUtils.newTask(watch, "Save token", context, log);
            saveToken(account, credentials.getCreatedTime(), credentials.getToken(), String.format("%s,%s", transactionID, webServiceCustomerID));

            UserService userService = new UserService();
            userService.updateAccountSubscribeRequestDate(account.getId(),new java.util.Date());
            StopWatchUtils.newTask(watch, "subscribeAlertLogging", context, log);
            if (StringUtils.isNotEmpty(transactionID))
            {
                log.info(String.format("subscribe success for %s, transaction=%s, webServiceCustomerId=%s", context, transactionID, webServiceCustomerID));
            } else {
                log.info(String.format("failed to subscribe %s, response was %s", context, subscribeBean.getResponse()));
                return null;
            }

        }

        return mailHeaders.getHeaders();
    }

    private MSPLoginBean getCredentials(Account account) throws Exception {
        return new MSPLoginBean(NewMSPSupport.getCreatedTime(), getToken(account));
    }

    private int saveToken(Account account, String createdTime, String token, String transactionID) throws DALException
    {
        IMspTokenService service = new MspTokenService();
        MspToken mspToken = new MspToken();
        mspToken.setName(account.getName());
        mspToken.setUser_id(account.getUser_id());
        mspToken.setCreate_number(new Date().getTime());
        mspToken.setToken_id(token.getBytes());
        mspToken.setTransaction_id(transactionID);
        mspToken.setComment(createdTime);
        return service.setMspToken(mspToken);
    }

    private int saveAccountFolderAndMessagingId(Account account)
    {
        UserService service = new UserService();
        return service.updateAccountFolderAndMessagingId(account);
    }

    private void getAccountFolderAndMessagingId(Account account)
    {
        UserService service = new UserService();
        service.getAccountFolderAndMessagingId(account);
    }

    public void setMsgFilter(MSGFilter msgFilter) {
        this.msgFilter = msgFilter;
    }

}