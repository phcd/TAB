package com.archermind.txtbl.receiver.mail.dal;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.dal.business.impl.EmailRecievedService;
import com.archermind.txtbl.dal.business.impl.UserService;
import com.archermind.txtbl.domain.*;
import org.jboss.logging.Logger;
import twitter4j.http.AccessToken;

import java.sql.Timestamp;
import java.util.Map;

public class DALDominator {


    private static final Logger log = Logger.getLogger(DALDominator.class);


    public static int newSaveMailTwitter(EmailPojo emailPojo, OriginalReceivedEmail original) throws DALException {
        return new EmailRecievedService().newSaveEmailTwitter(emailPojo, original);
    }

    public static int newSaveMail(EmailPojo emailPojo, OriginalReceivedEmail original, Account account) throws DALException {
        return new EmailRecievedService().newSaveEmail(emailPojo, original, account);
    }


    public static int newSaveMail(Account account, Email email, Body body) {
        try {
            return new EmailRecievedService().newSaveEmail(account, email, body);
        } catch (Throwable e) {
            if (e.getMessage() != null && e.getMessage().contains("Interrupted while requesting permit")) {
                try {
                    return new EmailRecievedService().newSaveEmail(account, email, body);
                } catch (Exception ex) {
                    log.error(String.format("unable to save mail for account=%s, uid=%s", account.getName(), email.getUserId()), ex);
                }
            } else {
                log.error(String.format("unexpected error, unable to save mail for account=%s, uid=%s", email.getOriginal_account(), email.getUserId()), e);
            }
        }

        return 0;
    }

    public static boolean loadAccountLatestReceiveInfo(Account account) {
        Map savedAccount = new UserService().getAccountData(account.getName());

        if (savedAccount == null) {
            return false;
        } else {
            account.setId((Integer) savedAccount.get("id"));
            account.setMessage_count((Integer) savedAccount.get("message_count"));
            account.setFolder_hash((String) savedAccount.get("folder_hash"));
            account.setFolder_depth((Integer) savedAccount.get("folder_depth"));
            account.setLast_reconciliation((Timestamp) savedAccount.get("last_reconciliation"));
            account.setLast_mailcheck((Timestamp) savedAccount.get("last_mailcheck"));
            account.setRegister_time((Timestamp) savedAccount.get("register_time"));
            account.setLast_received_date((Timestamp) savedAccount.get("last_received_date"));
            account.setLast_received_tweet_id((Long) savedAccount.get("last_received_tweet_id"));
            account.setLast_received_dm_id((Long) savedAccount.get("last_received_dm_id"));
            account.setLast_sent_dm_id((Long) savedAccount.get("last_sent_dm_id"));
            account.setLast_received_mention_id((Long) savedAccount.get("last_received_mention_id"));
            account.setLast_login_failure((Timestamp) savedAccount.get("last_login_failure"));
            account.setLogin_failures((Integer) savedAccount.get("login_failures"));
            account.setLast_calendar((Timestamp) savedAccount.get("last_calendar"));
            account.setActive_sync_key((String) savedAccount.get("active_sync_key"));
            account.setActive_sync_device_type((String) savedAccount.get("active_sync_device_type"));
            account.setActive_sync_device_id((String) savedAccount.get("active_sync_device_id"));


            return true;
        }

    }

    public static void updateAccountReceiveInfo(Account account) throws DALException {
        new UserService().updateAccountReceiveInfo(account);
    }

    public static void updateAccountReceiveInfoAndReceivedDate(Account account) throws DALException {
        new UserService().updateAccountReceiveInfoAndReceivedDate(account);
    }

    public static AccessToken fetchTwitterToken(String email) {
        return new UserService().fetchTwitterToken(email);

    }


}
