package com.archermind.txtbl.utils.xobni;

import com.archermind.txtbl.dal.business.impl.UserService;
import com.archermind.txtbl.domain.*;
import com.archermind.txtbl.utils.*;
import org.jboss.logging.Logger;
import org.json.simple.JSONObject;

import java.util.*;


public class XobniUserHandler {

    private static Logger log = Logger.getLogger(XobniUserHandler.class);
    private static final String xobniAuthFailFrom = SysConfigManager.instance().getValue("xobni.auth.fail.from", "ops@getpeek.com");
    private static final String xobniAuthFailTo = SysConfigManager.instance().getValue("xobni.auth.fail.to", "webdev-alerts@xobni.com");
    private static final int maxSyncRetriesBeforeDisablingSync = Integer.parseInt(SysConfigManager.instance().getValue("Xobni.maxSyncRetriesBeforeDisablingSync", "3"));
    private static final int maxSyncRetries = Integer.parseInt(SysConfigManager.instance().getValue("Xobni.maxSyncRetries", "5"));
    private static final int delayBetweenSyncRetriesInSeconds = Integer.parseInt(SysConfigManager.instance().getValue("Xobni.delayBetweenSyncRetries", "30"));

    private UserService userService;
    private XobniAccount xobniAccount;
    private Account account;
    private boolean accountReset = false;

    public XobniUserHandler(Account account) {
        this.account = account;
        userService = new UserService();
        xobniAccount = userService.getXobniAccountByUserID(account.getUser_id());
        if(xobniAccount == null) {
            String errorMessage = "Xobni account for " + account.getName() + " not found";
            log.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }
    }


    private boolean updateDBVersion(XobniAccount xAccount) {
        String newDBVersion = getDbVersion(xAccount);
        if (newDBVersion != null) {
            boolean success = userService.updateXobniDBVersion(xAccount.getName(), newDBVersion);
            if (success) {
                xAccount.setDbVersion(newDBVersion);
            }
            return success;
        } else {
            log.error("new DB Version for Xobni Account " + xAccount.getName() + " is empty");
            return false;
        }
    }

    private String getDbVersion(XobniAccount xAccount) {
        XobniResponse xobniResponse = XobniSyncUtil.INSTANCE.getExpectedDBVersion(xAccount);
        return xobniResponse != null ? xobniResponse.getResponse() : null;
    }

    //TODO - Paul - handle reset of account from within receiver
    private void resetAccount(XobniAccount xAccount, Account account) {
        String newDBVersion = getDbVersion(xAccount);
        if (!StringUtils.isEmpty(newDBVersion)) {
            String currentDbVersion = xAccount.getDbVersion();
            if(newDBVersion.equals(currentDbVersion)) {
                log.error("Account: " + account.getName() + "new DB Version same as the current one for Xobni Account " + xAccount.getName());
                return;
            }
            WebServiceClient.resetAccount(account);
        } else {
            log.error("Account: " + account.getName() + "  new DB Version for Xobni Account " + xAccount.getName() + " is empty");
        }
    }


    boolean buildAndDispatch(String folder, List<EmailPojo> emails, Boolean finalBatch, Integer emailsInInbox, Integer totalEmailsUploadedSoFar) {
        log.debug(String.format("Building JSON message to server for Account: %s, folder: %s, for %d messages", account.getName(), folder, emails.size()));

        XobniEnvelopeBuilder xobEnvelopeBuilder = new XobniEnvelopeBuilder();
        JSONObject xobEnvelope = xobEnvelopeBuilder.getEnvelope(account, folder, emails, finalBatch, emailsInInbox, totalEmailsUploadedSoFar);

        log.debug(String.format("Completed JSON msg for account: %s, folder: %s, SessionID: %s, JSON: %s", account.getName(), folder, xobniAccount.getSessionId(), xobEnvelope.toString()));

        //handle null or empty dbVersion
        if (StringUtils.isEmpty(xobniAccount.getDbVersion())) {
            log.info("UUID: " + account.getUser_id() + " Xobni Account " + xobniAccount.getName() + " has no DBVersion. Initializing first time");
            if (updateDBVersion(xobniAccount)) {
                log.debug("Updated local Xobni Account " + xobniAccount.getName() + " has dbVersion " + xobniAccount.getDbVersion());
            } else {
                log.error("Xobni Account " + xobniAccount.getName() + "  Initialization of Xobni DB Version failed.  Aborting Xobni sync.");
                return false;
            }
        }

        int returnCode = -1;
        for (int i = 0; i < maxSyncRetries; i++) {
            returnCode = XobniSyncUtil.INSTANCE.sync(xobEnvelope, xobniAccount);
            if(returnCode == XobniSyncUtil.XOBNI_200_OK || returnCode == XobniSyncUtil.XOBNI_DB_VERSION_ERROR) {
                break;
            } else {
                int nextIndex = i + 1;
                int delay = nextIndex * delayBetweenSyncRetriesInSeconds * 1000;
                log.warn("Xobni Account : " + xobniAccount.getName() + " sync failed with return code : " + returnCode + ", retry count : " + nextIndex);
                if(nextIndex < maxSyncRetries) {
                    try {
                        log.info("Xobni Account : " + xobniAccount.getName() + " waiting for " + delay + " milliseconds before retrying");
                        Thread.sleep(delay);
                    } catch (InterruptedException ignored) {}
                }
            }
        }

        switch (returnCode) {
            case XobniSyncUtil.XOBNI_200_OK:
                resetSyncErrorIfNecessary(xobniAccount);
                return true;

            case XobniSyncUtil.XOBNI_DB_VERSION_ERROR:
                //first attempt; update db version and try again
                log.info("Xobni Account " + xobniAccount.getName() + " has bad DB version; attempting update");
                resetAccount(xobniAccount, account);
                accountReset = true;
                return false;

            case XobniSyncUtil.XOBNI_AUTH_SESSION_ID_ERROR_401:
            case XobniSyncUtil.XOBNI_AUTH_SESSION_ID_ERROR_403:
                log.warn("Authentication session ID error from Xobni, synch failed for " + xobniAccount.getName());
                updateSyncError(xobniAccount, Integer.toString(returnCode));
                return false;

            case XobniSyncUtil.XOBNI_UNKNOWHOST_ERROR:
                log.info("Account: " + account.getName() + "  Unknown host error from Xobni, synch failed SessionId : " + xobniAccount.getSessionId());
                updateSyncError(xobniAccount, "Unknown Host");
                return false;

            default:
                log.warn("Account: " + account.getName() + "  Unexpected/Unhandled return code from Xobni sync method");
                return false;
        }
    }

    private void resetSyncErrorIfNecessary(XobniAccount xobniAccount) {
        if(xobniAccount.hasSyncError()) {
            xobniAccount.resetSyncErrors();
            boolean success = userService.updateXobniAccount(xobniAccount);
            log.info("Resetting Sync errors for " + xobniAccount.getName() + " Sucess : " + success);
        }
    }

    private void updateSyncError(XobniAccount xobniAccount, String error) {
        if(UtilsTools.isEmpty(XobniValidator.validateSessionIdAndSyncUrl(xobniAccount))) {
            return;
        }
        xobniAccount.setSyncError(error, maxSyncRetriesBeforeDisablingSync);
        boolean success = userService.updateXobniAccount(xobniAccount);
        if (success && !xobniAccount.isActive()) {
            String message = getNotificationMessage(xobniAccount, error);
            String subject = "Email sync disabled for " + xobniAccount.getName();
            Mailer.getInstance().sendMail(xobniAuthFailFrom, xobniAuthFailTo, null, null, message, subject);
        }
    }

    private String getNotificationMessage(XobniAccount xobniAccount, String errorMessage) {
        String message = "Email sync disabled for " + xobniAccount.getName() + "\n";
        message += "Sync Url: " + xobniAccount.getSyncUrl() + "\n";
        message += "SessionId: " + xobniAccount.getSessionId() + "\n";
        message += "Error: " + errorMessage + "\n";
        return message;
    }

    private boolean sendAllEmails(String folder, List<EmailPojo> emailPojos, Boolean finalBatch, Integer emailsInInbox, Integer totalEmailsUploadedSoFar) {
        if (UtilsTools.isEmpty(emailPojos)) {
            log.debug("Account: " + account.getName() + " folder: " + folder + " returned email object from emailreceived service is emoty");
            return true;
        }
        boolean success = buildAndDispatch(folder, emailPojos, finalBatch, emailsInInbox, totalEmailsUploadedSoFar);
        if (!success) {
            log.error("Account: " + account.getName() + " folder: " + folder + "  Dispatch to Xobni failed.");
        } else {
            log.info("Account: " + account.getName() + " folder: " + folder + "  Xobni synced " + emailPojos.size() + " messages");
        }
        return success;
    }

    public boolean syncXobniUser(String folder, List<EmailPojo> emailPojos, Boolean finalBatch, Integer emailsInInbox, Integer totalEmailsUploadedSoFar) {
        log.debug("Synching xobniUser: " + account.getName() + " folder: " + folder +  " final batch of the initial fetch : " + finalBatch);
        return sendAllEmails(folder, emailPojos, finalBatch, emailsInInbox, totalEmailsUploadedSoFar);
    }

    public boolean shouldProcess() {
        return xobniAccount.isActive();
    }

    public boolean accountReset() {
        return accountReset;
    }
}
