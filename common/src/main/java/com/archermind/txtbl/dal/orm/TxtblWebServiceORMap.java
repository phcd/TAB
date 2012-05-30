package com.archermind.txtbl.dal.orm;

import com.archermind.txtbl.domain.*;
import com.archermind.txtbl.utils.UtilsTools;
import org.jboss.logging.Logger;
import org.springframework.util.CollectionUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TxtblWebServiceORMap extends BaseORMap {

    private static final Logger log = Logger.getLogger(TxtblWebServiceORMap.class);

    public TxtblWebServiceORMap(boolean useMaster) {
        super(useMaster);
    }

    public TxtblWebServiceORMap() {}

    public boolean deactivateSubscriberEmailAccount(Account account) {
        try {
            this.sqlMapClient.startTransaction();
            this.sqlMapClient.insert("TxtblWebService.backupDeactivateSubscriberEmailAccount", account);

            String day = "delete" + UtilsTools.normalDateToStr(new Date());
            account.setComment(day);

            // delete from msp token data
            HashMap<String, String> hs = new HashMap<String, String>();
            hs.put("user_id", account.getUser_id());
            hs.put("name", account.getName());

            this.sqlMapClient.delete("MspToken.removeMspToken", hs);

            this.sqlMapClient.delete("Account.deleteAccount", account);

            deleteEmails(account);

            this.sqlMapClient.commitTransaction();

            return true;
        } catch (SQLException e) {
            log.error("Deactivate subscriber email account fail!", e);
            return false;
        } finally {
            try {
                this.sqlMapClient.endTransaction();
            } catch (SQLException e) {
                log.error("Deactivate subscriber email account fail!", e);
            }
        }
    }

    //TODO - Paul - this seems very in-efficient
    @SuppressWarnings("unchecked")
    private void deleteEmails(Account account) throws SQLException {
        List<Email> list = this.sqlMapClient.queryForList("TxtblWebService.selectReceivedEmail", account);

        if (list.size() > 0) {
            this.sqlMapClient.startBatch();
            for (Email aEmail : list) {
                this.sqlMapClient.delete("TxtblWebService.delReceivedAttachment", String.valueOf(aEmail.getMailid()));
                this.sqlMapClient.delete("TxtblWebService.delOriginalAttachment", String.valueOf(aEmail.getMailid()));
                this.sqlMapClient.delete("TxtblWebService.delReceivedBody", String.valueOf(aEmail.getMailid()));
            }
            this.sqlMapClient.executeBatch();
        }

        this.sqlMapClient.delete("TxtblWebService.delRecieved", account);
    }

    public boolean resetEmailAccount(Account account) {
        try {
            this.sqlMapClient.startTransaction();

            this.sqlMapClient.update("Account.resetAccount", account);
            deleteEmails(account);

            this.sqlMapClient.commitTransaction();

            return true;
        } catch (SQLException e) {
            log.error("Deactivate subscriber email account fail!", e);
            return false;
        } finally {
            try {
                this.sqlMapClient.endTransaction();
            } catch (SQLException e) {
                log.error("Deactivate subscriber email account fail!", e);
            }
        }

    }

    @SuppressWarnings("unchecked")
    public List<Account> activateNewExpiryPeriod(Device device) {
        try {
            this.sqlMapClient.update("TxtblWebService.activateNewExpiryPeriod_step1_active", device);
            return this.sqlMapClient.queryForList("TxtblWebService.activateOrDeactiveNewExpiryPeriod_step2_select", device);
        } catch (SQLException e) {
            log.error("Activate new expiry period fail!", e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public List<Account> SuspendPeekAccount(Device device) {
        try {
            int rowsImpacted = this.sqlMapClient.update("TxtblWebService.SuspendPeekAccount_step1_active", device);
            //TODO - Paul - just suspend account and the getaccounts at the caller of this method.
            List<Account> accounts = this.sqlMapClient.queryForList("TxtblWebService.activateOrDeactiveNewExpiryPeriod_step2_select", device);

            /*
                TODO: I am soooo sorry for doing this! but i need to work around this mess :) Here is a scoop:

                CommandBox.suspendPeekAccount wants to suspend account and relies on account.size to be able to tell is
                suspend work, but there maybe no accounts left due to deletions.

                The ONLY error type condition here is that user was not found update rowsImpacted == 0, we will return null in that case
             */
            if (CollectionUtils.isEmpty(accounts)) {
                if (rowsImpacted > 0) {
                    return new ArrayList<Account>();
                } else {
                    return null;
                }
            } else {
                return accounts;
            }

        } catch (SQLException e) {
            log.error("Suspend peek account fail!", e);
            return null;
        }
    }

    public int addPop3Server(Server server) {
        try {
            this.sqlMapClient.insert("TxtblWebService.addPop3Server", server);
            return 1;
        } catch (SQLException e) {

            log.error("Add pop3 server fail!", e);
            return 0;
        }
    }

    @SuppressWarnings("unchecked")
    public List<TxtblCookie> queryCookieByAccountName(String accountName) {
        try {
            return this.sqlMapClient.queryForList("TxtblWebService.queryCookieByAccount", accountName);
        } catch (SQLException e) {
            log.error("Query cookie by account fail!", e);
            return null;
        }
    }

    public int deleteCookiesByAccountName(String accountName) {
        try {
            return this.sqlMapClient.delete("TxtblWebService.deleteCookiesByAccountName", accountName);
        } catch (SQLException e) {
            log.error("Delete cookies by account name fail!", e);
            return 0;
        }
    }

    public int saveOrUpdateTxtblCookies(List<TxtblCookie> txtblCookies) {
        try {
            List<TxtblCookie> tcs = queryCookieByAccountName(txtblCookies
                    .get(0).getEmailAccount());
            if (tcs != null && tcs.size() > 0) {
                deleteCookiesByAccountName(txtblCookies.get(0)
                        .getEmailAccount());
            }
            for (TxtblCookie tmp : txtblCookies) {
                this.sqlMapClient.insert("TxtblWebService.saveTxtblCookies", tmp);
            }
            return 1;
        } catch (SQLException e) {
            log.error("Save or update TxtblCookies fail!", e);
            return 0;
        }
    }

    public int getIdByPeekAccount(String peekAccount) {
        int user_id = -1;
        Object object = null;
        try {
            object = this.sqlMapClient.queryForObject("TxtblWebService.getUserIdByPeekAccountId", peekAccount);
        } catch (SQLException e) {
            log.error("get Id By Peek Account fail!", e);
        }
        if (object != null)
            user_id = (Integer) object;
        return user_id;
    }

    @SuppressWarnings("unchecked")
    public List<Device> transferquery(HashMap hs) {
        try {
            return this.sqlMapClient.queryForList("TxtblWebService.transferquery", hs);
        } catch (SQLException e) {
            log.error("transferquery fail!", e);
            return null;
        }
    }

    public int indiaTransferupdate(HashMap hs) {
        try {
            return this.sqlMapClient.update("TxtblWebService.indiaTransferupdate", hs);
        } catch (SQLException e) {
            log.error("indiaTransferupdate fail!", e);
            return 0;
        }
    }

    public int transferupdate(HashMap hs) {
        try {
            return this.sqlMapClient.update("TxtblWebService.transferupdate", hs);
        } catch (SQLException e) {
            log.error("transferupdate fail!", e);
            return 0;
        }
    }

    public List getReceiveList(String original_account, int user_id, int limit) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("original_account", original_account);
        map.put("user_id", user_id);
        if (limit != 0) {
            map.put("limit", limit);
        }

        try {
            return this.sqlMapClient.queryForList("TxtblWebService.getReceiveList",
                    map);
        } catch (SQLException e) {
            log.error("get Receive List fail!", e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public List<Email> getSentList(HashMap hs) throws SQLException {
        return this.sqlMapClient.queryForList("TxtblWebService.getSentList", hs);
    }

    @SuppressWarnings("unchecked")
    public List<Attachment> getReceiveAttachmentList(String emailId) throws SQLException {
        return this.sqlMapClient.queryForList("TxtblWebService.getReceiveAttachmentList", emailId);
    }

    @SuppressWarnings("unchecked")
    public List<Attachment> getSentAttachmentList(String emailId) throws SQLException {
        return this.sqlMapClient.queryForList("TxtblWebService.getSentAttachmentList", emailId);
    }

    public int addSentServer(Server server) {
        try {
            this.sqlMapClient.insert("MailServer.addSentServer", server);
            return 1;
        } catch (SQLException e) {
            log.error("addSentServerr fail!", e);
            return 0;
        }
    }

    public Object getMaxServerId() throws SQLException {
        return this.sqlMapClient.queryForObject("Global.getLastInsertId");
	}

}
