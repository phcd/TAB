package com.archermind.txtbl.dal.orm;

import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.User;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public class UserORMap extends BaseORMap {

    public UserORMap(boolean useMaster) {
        super(useMaster);
    }

    public UserORMap() {
    }

    public int addUser(User user) throws SQLException {
        return this.sqlMapClient.update("User.addUser", user);
    }

    public int modifyStatus(HashMap param) throws SQLException {
        return this.sqlMapClient.update("User.modifyStatus", param);
    }

    public Object getUserMaxId() throws SQLException {
        return this.sqlMapClient.queryForObject("Global.getLastInsertId");
    }

    public int removeUser(String userid) throws SQLException {
        return this.sqlMapClient.delete("User.removeUser", userid);
    }

    public int setBackUpAccount(Account account) throws SQLException {
        return this.sqlMapClient.update("TxtblWebService.backupDeactivateSubscriberEmailAccount",account);
    }

    @SuppressWarnings("unchecked")
    public List<Account> getAccount(HashMap param) throws SQLException {
        return this.sqlMapClient.queryForList("Account.getAccount", param);
    }

    @SuppressWarnings("unchecked")
    public List<Account> getAccountFolderAndMessagingId(Account account) throws SQLException {
        return this.sqlMapClient.queryForList("Account.getAccountFolderIdAndMessagingId", account);
    }

    public int modifyAccountFolderAndMessagingId(Account account) throws SQLException {
        return this.sqlMapClient.update("Account.updateAccountFolderIdAndMessagingId", account);
    }

    public String getAccountExchangeMode(Account account) throws SQLException {
        return (String) this.sqlMapClient.queryForObject("Account.getAccountExchangeConnectionMode", account);
    }

    public int modifyAccountExchangeMode(HashMap params) throws SQLException {
        return this.sqlMapClient.update("Account.updateAccountExchangeConnectionMode", params);
    }

    public int deleteAccounts(String userid) throws SQLException {
        return this.sqlMapClient.delete("Account.deleteAccounts", userid);
    }

    public int removePeek02(String userid) throws SQLException {
        return this.sqlMapClient.delete("User.removePeek02", userid);
    }

    public List selectSentEmail(String userid) throws SQLException {
        return this.sqlMapClient.queryForList("User.selectSentEmail", userid);
    }

    public int removePeekSentBcc(String userid) throws SQLException {
        return this.sqlMapClient.delete("User.removePeekSentBcc", userid);
    }

    public int removePeek03(String userid) throws SQLException {
        return this.sqlMapClient.delete("User.removePeek03", userid);
    }

    public int removePeek04(String userid) throws SQLException {
        return this.sqlMapClient.delete("User.removePeek04", userid);
    }

    public int removePeek05(String id) throws SQLException {
        // return this.sqlMapClient.delete("User.removePeek05", userid); by yong.hu
        // 2008-9-26
        return this.sqlMapClient.delete("SentEmail.deleteSentHeadEmailId", id);
    }

    public List selectReceivedEmail(String userid) throws SQLException {
        return this.sqlMapClient.queryForList("User.selectReceivedEmail", userid);
    }

    public int removePeek06(String userid) throws SQLException {
        return this.sqlMapClient.delete("User.removePeek06", userid);
    }

    public int removePeek07(String userid) throws SQLException {
        return this.sqlMapClient.delete("User.removePeek07", userid);
    }

    public int removePeek08(String id) throws SQLException {
        return this.sqlMapClient.delete(
                "ReceivedEmail.deleteReceivedHeadEmailId", id);
    }

    public int removePeek09(String userid) throws SQLException {
        return this.sqlMapClient.delete("User.removePeek09", userid);
    }

    public int removePeek11(String userid) throws SQLException {
        return this.sqlMapClient.delete("User.removePeek11", userid);
    }

    public int removePeek13(String userid) throws SQLException {
        this.sqlMapClient.insert("TxtblWebService.backupDeactivatePeekAccount", userid);
        return removeUser(userid);
    }

    public User getPeekAccountIdByID(String id) throws SQLException {
        return (User) this.sqlMapClient.queryForObject("User.getPeekAccountIdByID",id);
    }

    public User getPeekAccountIdByPeek_account(String PeekAccount) throws SQLException {
        return (User) this.sqlMapClient.queryForObject("User.getPeekAccountIdByPeek_account", PeekAccount);
    }

    public User getPeekUserIdByEmailAndImei(HashMap param) throws SQLException {
        return (User) this.sqlMapClient.queryForObject("User.getPeekUserIdByEmailAndImei", param);
    }

    public int modifyChangeFlag(HashMap param) throws SQLException {
        return this.sqlMapClient.update("User.modifyChangeFlag", param);
    }

    @SuppressWarnings("unchecked")
    public List<User> getCurDateHistoyUserid(HashMap param) throws SQLException {
        return this.sqlMapClient.queryForList("User.getCurDateHistoyUserid", param);
    }

    @SuppressWarnings("unchecked")
    public List<Account> getAccountToTaskfactory(String sRvcPtlType) throws SQLException {
        return this.sqlMapClient.queryForList("Account.getAccountToTaskfactory", sRvcPtlType);
    }

    public int modifyUserFeatures(HashMap param) throws SQLException {
        return this.sqlMapClient.update("User.modifyUserFeatures", param);
    }


    public String getCountry(String userId) throws SQLException {
        return (String) this.sqlMapClient.queryForObject("User.getCountry", userId);
    }
}
