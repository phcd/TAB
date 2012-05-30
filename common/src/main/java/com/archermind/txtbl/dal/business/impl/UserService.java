package com.archermind.txtbl.dal.business.impl;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.dal.business.IUserService;
import com.archermind.txtbl.dal.orm.*;
import com.archermind.txtbl.domain.*;
import com.archermind.txtbl.mail.store.MessageStore;
import com.archermind.txtbl.mail.store.MessageStoreException;
import com.archermind.txtbl.mail.store.MessageStoreFactory;
import com.archermind.txtbl.utils.ErrorCode;
import com.archermind.txtbl.utils.StringUtils;
import com.archermind.txtbl.utils.SysConfigManager;
import com.archermind.txtbl.utils.UtilsTools;
import com.webalgorithm.exchange.ExchangeConnectionMode;
import org.jboss.logging.Logger;
import twitter4j.http.AccessToken;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

public class UserService implements IUserService {
    private static final Logger logger = Logger.getLogger(UserService.class);
    private boolean useMasterForUser;
    private static boolean useMasterForUserDefault;
    public static final MessageStore store = MessageStoreFactory.getStore();

    static {
        useMasterForUserDefault = Boolean.parseBoolean(SysConfigManager.instance().getValue("useMasterForUser", "false"));
    }

    public UserService() {
        this(useMasterForUserDefault);
    }

    public UserService(boolean useMasterForUser) {
        this.useMasterForUser = useMasterForUser;
    }

    /*
    * (non-Javadoc)
    *
    * @see com.archermind.txtbl.dal.business.IUserService#registerUser(com.archermind.txtbl.domain.UserPojo)
    */
    public int registerUser_new(UserPojo pojo, XobniAccount xobniAccount) {
        boolean flag = true;
        UserORMap orm = new UserORMap();
        try {
            orm.startTransaction();

            if (pojo.getUser() != null) {
                flag = (orm.addUser(pojo.getUser()) == 1);
            }

            String user_id = (orm.getUserMaxId() != null) ? Integer.parseInt(orm.getUserMaxId().toString()) + "" : "1";

            if (flag && (pojo.getDevice() != null)) {
                DeviceORMap dorm = new DeviceORMap(orm);
                pojo.getDevice().setUser_id(user_id);
                flag = (dorm.addDevice(pojo.getDevice()) == 1);
            }

            AccountORMap accountORMap = new AccountORMap(orm);
            if (flag && (pojo.getAccount() != null)) {
                for (int i = 0; i < pojo.getAccount().size(); i++) {
                    pojo.getAccount().get(i).setUser_id(user_id);
                    if (accountORMap.addAccount(pojo.getAccount().get(i)) == 0) {
                        flag = false;
                        break;
                    }
                }
            }

            if (flag && (pojo.getContact() != null)) {
                ContactORMap corm = new ContactORMap(orm);

                for (int i = 0; i < pojo.getContact().size(); i++) {
                    pojo.getContact().get(i).setUserid(user_id);
                    if (corm.addContact(pojo.getContact().get(i)) == 0) {
                        flag = false;
                        break;
                    }
                }
            }

            if (flag && (xobniAccount != null)) {
                flag = accountORMap.addXobniAccount(xobniAccount) >= 1;
            }

            if (flag) {
                orm.commitTransaction();
            }
        } catch (SQLException e) {
            logger.error("Register user error", e);
        } finally {
            try {
                orm.endTransaction();
            } catch (SQLException e) {
                logger.error("Register user error!", e);
            }
        }
        return flag ? 1 : 0;
    }

    public int modifyStatus(String user_id, String status) {
        UserORMap userORMap = new UserORMap();
        try {
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("id", user_id);
            params.put("status", status);
            userORMap.startTransaction();
            int ret = userORMap.modifyStatus(params);
            userORMap.commitTransaction();
            return ret;
        } catch (SQLException e) {
            logger.error("Modify status error!", e);
            return 0;
        } finally {
            try {
                userORMap.endTransaction();
            } catch (SQLException e) {
                logger.error("Modify status error!", e);
            }

        }
    }

    public int addAccount(Account account) {
        AccountORMap orMap = new AccountORMap();
        try {
            orMap.startTransaction();
            int ret = orMap.addAccount(account);
            orMap.commitTransaction();
            return ret;
        } catch (SQLException e) {
            logger.error("Add account error!", e);
            return 0;
        } finally {
            try {
                orMap.endTransaction();
            } catch (SQLException e) {
                logger.error("Add account error!", e);
            }
        }
    }

    public Account getAccount(String user_id, String name) {
        if (logger.isTraceEnabled())
            logger.trace(String.format("getAccount(user_id=%s, name=%s)", String.valueOf(user_id), String.valueOf(name)));
        try {
            HashMap<String, String> param = new HashMap<String, String>();
            if (user_id != null) {
                param.put("user_id", user_id);
            }
            param.put("name", name);
            return new AccountORMap(useMasterForUser).getAccount(param);
        } catch (SQLException e) {
            logger.error(String.format("Get account error user_id=%s, name=%s", user_id, name), e);
            return null;
        }
    }

    public Account getAccount(long account_id) {
        try {
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("id", account_id);
            return new AccountORMap(useMasterForUser).getAccount(params);
        } catch (SQLException e) {
            logger.error("Get account error!", e);
            return null;
        }
    }

    public Account getAccount(String emailAddress) {
        try {

            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("name", emailAddress);
            return new AccountORMap(useMasterForUser).getAccount(params);
        } catch (SQLException e) {
            logger.error("Get account error!", e);
            return null;
        }
    }

    public Map getAccountData(String name) {
        try {
            return new AccountORMap(useMasterForUser).getAccountData(name);
        } catch (SQLException e) {
            logger.error("Get account error for " + name, e);
            return null;
        }
    }

    public Date getAccountSubscribeAlertDate(String name) {
        try {
            return new AccountORMap(useMasterForUser).getAccountSubscribeAlertDate(name);
        } catch (SQLException e) {
            logger.error("Get account error for " + name, e);
            return null;
        }
    }

    public XobniAccount getXobniAccountByUserID(String userid) {
        try {
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("id", userid);
            return new AccountORMap(useMasterForUser).getXobniAccountByUserID(params);
        } catch (SQLException e) {
            logger.error("Get Xobni account failed", e);
            return null;
        }
    }


    public boolean xobniAccountExists(String name) {
        try {
            return new AccountORMap(useMasterForUser).getXobniAccountCount(name) > 0;
        } catch (SQLException e) {
            logger.error("Xobni account existence check failed", e);
            return false;
        }
    }

    public boolean updateXobniDBVersion(String accountName, String dbVersion) {
        AccountORMap accountORMap = new AccountORMap();
        try {
            logger.debug("XobniAccount: " + accountName + " set to: " + dbVersion);
            if (accountORMap.updateXobniDBVersion(accountName, dbVersion) > 0) {
                return true;
            }
            logger.warn("Xobni AccountMap dbversion updated successfully but no rows were affected.");
            return false;
        } catch (DALException e) {
            logger.error("Xobni account DBVersion update failed for: " + accountName +
                    "dbVersionUpdate" + dbVersion);
            return false;
        }
    }

    public List<Account> getAccounts(String user_id) {
        try {
            HashMap<String, String> param = new HashMap<String, String>();
            param.put("user_id", user_id);
            param.put("status", "1");
            return new AccountORMap(useMasterForUser).getAccounts(param);
        } catch (SQLException e) {
            logger.error("Get accounts error!", e);
            return null;
        }
    }

    public int modifyAccount(Account account) {
        AccountORMap orMap = new AccountORMap();
        try {
            orMap.startTransaction();
            int ret = orMap.modifyAccount(account);
            orMap.commitTransaction();
            return ret;
        } catch (SQLException e) {
            logger.error("Modify account error!", e);
            return 0;
        } finally {
            try {
                orMap.endTransaction();
            } catch (SQLException e) {
                logger.error("Modify account error!", e);
            }
        }
    }

    public boolean removeEmailAccount(Account account) {
        boolean success = new TxtblWebServiceORMap().deactivateSubscriberEmailAccount(account);
        if (success) {
            cleanupMessageStore(account);
        }
        return success;
    }


    public boolean resetEmailAccount(Account account) {
        boolean success = new TxtblWebServiceORMap().resetEmailAccount(account);
        if (success) {
            cleanupMessageStore(account);
        }
        return success;
    }

    private boolean cleanupMessageStore(Account account) {
        try {
            store.deleteAllMessages(account.getId(), account.getCountry());
        } catch (MessageStoreException ex) {
            logger.warn(String.format("Unable to cleanup message store for account=%s, uid=%s, name=%s", account.getId(), account.getUser_id(), account.getName()), ex);
        }

        return true;
    }

    public int addContacts(List<Contact> list) {
        ContactORMap orm = new ContactORMap();
        try {
            int ret = 0;
            orm.startTransaction();
            for (Contact aList : list) {
                ret = orm.addContact(aList);
                if (ret == 0) {
                    break;
                }
            }
            if (ret == 1) {
                orm.commitTransaction();
            }
            return ret;
        } catch (SQLException e) {
            logger.error("Add contacts error!", e);
            return 0;
        } finally {
            try {
                orm.endTransaction();
            } catch (SQLException e) {
                logger.error("Add contacts error!", e);
            }
        }
    }

    public List<Contact> getPagedContacts(String user_id, int page_no, int page_size, String type) {
        try {
            HashMap<String, Object> param = new HashMap<String, Object>();
            param.put("userid", user_id);
            param.put("row_no", page_no * page_size);
            param.put("page_size", page_size);
            param.put("type", type);

            return new ContactORMap(useMasterForUser).getPagedContacts(param);
        } catch (SQLException e) {
            logger.error("Get paged contacts error!", e);
            return null;
        }
    }

    /*
      * (non-Javadoc)
      *
      * @see com.archermind.txtbl.dal.business.IUserService#modifyContacts(java.util.List)
      */
    public int modifyContacts(List<Contact> list) {
        ContactORMap orm = new ContactORMap();
        try {
            int ret = 0;
            orm.startTransaction();
            for (Contact aList : list) {
                if ((ret = orm.modifyContact(aList)) == 0) {
                    break;
                }
            }
            if (ret == 1) {
                orm.commitTransaction();
            }
            return ret;
        } catch (SQLException e) {
            logger.error("Modify contacts error!", e);
            return 0;
        } finally {
            try {
                orm.endTransaction();
            } catch (SQLException e) {
                logger.error("Modify contacts error!", e);
            }
        }
    }

    /*
      * (non-Javadoc)
      *
      * @see com.archermind.txtbl.dal.business.IUserService#removeContact(java.lang.String,
      *      java.lang.String)
      */
    public int removeContact(String user_id, String email, String type) {
        ContactORMap orm = new ContactORMap();

        try {
            int ret;
            HashMap<String, String> param = new HashMap<String, String>();
            param.put("userid", user_id);
            if (email != null) {
                param.put("email", email);
            }
            if (type != null) {
                param.put("type", type);
            }

            orm.startTransaction();
            if ((ret = orm.removeContact(param)) >= 1) {
                orm.commitTransaction();
            }
            return ret;
        } catch (SQLException e) {
            logger.error("Remove contact error!", e);
            return 0;
        } finally {
            try {
                orm.endTransaction();
            } catch (SQLException e) {
                logger.error("Remove contact error!", e);
            }
        }
    }

    public int removeContacts(List<Contact> list) {
        int ret = 0;
        ContactORMap orm = new ContactORMap();
        try {
            orm.startTransaction();

            for (Contact aList : list) {
                HashMap<String, String> param = new HashMap<String, String>();
                param.put("userid", aList.getUserid());
                param.put("email", aList.getEmail());
                if ((ret = orm.removeContact(param)) == 0) {
                    break;
                }
            }

            if (ret != 0) {
                orm.commitTransaction();
                ret = 1;
            }
        } catch (SQLException e) {
            ret = 0;
            logger.error("Remove contacts error!", e);
        } finally {
            try {
                orm.endTransaction();
            } catch (SQLException e) {
                logger.error("Remove contacts error!", e);
            }
        }

        return ret;
    }

    public int addDevices(Device device) {
        DeviceORMap orm = new DeviceORMap();
        int li_ret = 0;

        try {
            orm.startTransaction();
            List<Device> list = orm.selectDevice(device.getUser_id());

            if (list.size() > 0) {
                for (Device aList : list) {
                    device.setId(aList.getId());
                    orm.modifyIdDevices(device);
                }
            }

            li_ret = orm.addDevice(device);

            if (li_ret == 1) {
                orm.commitTransaction();
            }
        } catch (SQLException e) {
            li_ret = 0;
            logger.error("add devices error!", e);
        } finally {
            try {
                orm.endTransaction();
            } catch (SQLException e) {
                logger.error("add devices error!", e);
            }
        }
        return li_ret;
    }

    public String getDeviceCode(String user_id) {
        try {
            HashMap<String, String> param = new HashMap<String, String>();
            param.put("user_id", user_id);
            return new DeviceORMap(useMasterForUser).getDeviceCode(param);
        } catch (SQLException e) {
            logger.error("Get device code error!", e);
            return null;
        }
    }

    public int modifyDevice(Device device) {
        DeviceORMap orMap = new DeviceORMap();
        try {
            orMap.startTransaction();
            int ret = orMap.modifyDevice(device);
            orMap.commitTransaction();
            return ret;
        } catch (SQLException e) {
            logger.error("modify device  fail!", e);
            return 0;
        } finally {
            try {
                orMap.endTransaction();
            } catch (SQLException e) {
                logger.error("modify device  fail!", e);
            }
        }

    }

    public Integer getUserId(String name) {
        try {
            return new AccountORMap(useMasterForUser).getUserId(name);
        } catch (SQLException e) {
            logger.error("get user id fail!", e);
            return null;
        }
    }

    public Device getDeviceById(String id) {
        try {
            return new DeviceORMap(useMasterForUser).getDeviceByid(id);
        } catch (SQLException e) {
            logger.error("get device by id   fail!", e);
            return null;
        }
    }

    public Device getDeviceByUserId(String user_id) {
        if (logger.isTraceEnabled())
            logger.trace(String.format("getDeviceByUserId(userId=%s)", user_id));
        try {
            return new DeviceORMap(useMasterForUser).getDeviceByUserid(user_id);
        } catch (SQLException e) {
            logger.error("Get   device by userid  fail!", e);
            return null;
        }
    }

    public boolean removeUser(String userid) {
        UserORMap orMap = new UserORMap();
        try {
            orMap.startTransaction();
            boolean ret = (orMap.removeUser(userid) == 1);
            orMap.commitTransaction();
            return ret;
        } catch (SQLException e) {
            logger.error("remove peek error!", e);
            return false;
        } finally {
            try {
                orMap.endTransaction();
            } catch (SQLException e) {
                logger.error("remove peek error!", e);
            }
        }

    }

    public User getPeekAccountIdByID(String id) {
        try {
            return new UserORMap(useMasterForUser).getPeekAccountIdByID(id);
        } catch (SQLException e) {
            logger.error("get peek  account id by id error!", e);
            return null;
        }
    }

    public User getPeekAccountIdByPeek_account(String PeekAccount) {
        try {
            return new UserORMap(useMasterForUser).getPeekAccountIdByPeek_account(PeekAccount);
        } catch (SQLException e) {
            logger.error("get peek  account id by peek account error!", e);
            return null;
        }
    }

    public User getPeekUserIdByEmailAndImei(String email, String imei) {
        try {
            HashMap<String, String> param = new HashMap<String, String>();
            param.put("email", email);
            param.put("imei", imei);
            return new UserORMap(useMasterForUser).getPeekUserIdByEmailAndImei(param);
        } catch (SQLException e) {
            logger.error("get peek  account id by peek account error!", e);
            return null;
        }
    }

    public int getAccountFolderAndMessagingId(Account account) {
        try {
            UserORMap orm = new UserORMap();
            List<Account> list = orm.getAccountFolderAndMessagingId(account);
            if (list.size() > 0) {
                account.setFolder_id(list.get(0).getFolder_id());
                account.setMessaging_id(list.get(0).getMessaging_id());
            }
            return 1;
        } catch (SQLException e) {
            logger.error("Get Account Folder and MessagingId error " + account.getId(), e);
            return 0;
        }
    }


    public List<Account> getAllAccounts(String userId) {
        if (logger.isTraceEnabled())
            logger.trace(String.format("getAllAccounts(userId=%s)", String.valueOf(userId)));
        try {
            HashMap<String, String> param = new HashMap<String, String>();
            param.put("user_id", userId);
            return new UserORMap().getAccount(param);
        } catch (SQLException e) {
            logger.error("getAllAcounts for " + userId + " not found", e);
        }
        return new ArrayList<Account>();
    }

    public boolean resetPeekAccount(String userId, List<Account> accounts) {
        boolean result = true;
        UserORMap orm = new UserORMap();
        ApiKeyORMap apiKeyORMap = new ApiKeyORMap();

        try {
            orm.startTransaction();

            if (accounts.size() > 0) {
                for (Account account : accounts) {
                    // save account history
                    orm.setBackUpAccount(account);
                    String day = "delete" + UtilsTools.normalDateToStr(new Date());
                    account.setComment(day);

                    if (account.isXobniAccount()) {
                        deleteXobniAccount(account.getName());
                    } else if (account.isTwitterAccount()) {
                        deleteTwitterAccount(account.getName());
                    }
                    cleanupMessageStore(account);
                }
            }

            HashMap<String, String> hs = new HashMap<String, String>();
            hs.put("user_id", userId);
            //delete msptoken
            new MspTokenORMap(orm).removeMspToken(hs);
            // delete txtbl_email_account
            orm.deleteAccounts(userId);
            // delete txtbl_contact
            orm.removePeek02(userId);
            // delete txtbl_device
            orm.removePeek09(userId);
            // save user_history delete user
            orm.removePeek13(userId);

            orm.commitTransaction();


        } catch (SQLException e) {
            result = false;
            logger.error("Reset Peek Account error", e);
        } finally {
            try {
                orm.endTransaction();
                apiKeyORMap.deleteApiKeyByUserId(userId);
            } catch (SQLException e) {
                result = false;
                logger.error("Reset Peek Account error!", e);
            }
        }
        return result;
    }

    public ExchangeConnectionMode getExchangeMode(Account account) {
        try {
            String exchangeModeString = new UserORMap().getAccountExchangeMode(account);
            if (!StringUtils.isEmpty(exchangeModeString)) {
                String[] tmpMode = exchangeModeString.split(";");
                if (tmpMode.length == 3) {
                    return new ExchangeConnectionMode(tmpMode[0], Boolean.parseBoolean(tmpMode[1]), Boolean.parseBoolean(tmpMode[2]));
                }
            }
            return null;
        } catch (SQLException e) {
            logger.error("Get exchange mode error for " + account.getId(), e);
            return null;
        }
    }

    public int updateExchangeMode(Account account, String mode) {
        UserORMap orMap = new UserORMap();
        try {

            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("id", account.getId());
            params.put("exchange_conn_mode", mode);
            orMap.startTransaction();
            int ret = orMap.modifyAccountExchangeMode(params);
            orMap.commitTransaction();
            return ret;
        } catch (SQLException e) {
            logger.error("Update exchange mode error for " + account.getId(), e);
            return 0;
        } finally {
            try {
                orMap.endTransaction();
            } catch (SQLException e) {
                logger.error("Update exchange mode error for " + account.getId(), e);
            }
        }

    }

    public List<Device> getDeviceByIDvcIDSim(String devicecode, String simcode) {
        if (logger.isTraceEnabled())
            logger.trace(String.format("getDeviceByIDvcIDSim(deviceCode=%s, simCode=%s)", String.valueOf(devicecode), String.valueOf(simcode)));
        try {
            return new DeviceORMap(useMasterForUser).getDeviceByIDvcIDSim(devicecode, simcode);
        } catch (SQLException e) {
            logger.error("get Device By  Sim  error", e);
            return null;
        }
    }

    public String getUserIdByDeviceCode(String devicecode) {
        try {
            logger.debug("GETUSER1:"+devicecode);
            //HashMap<String, String> param = new HashMap<String, String>();
            logger.debug("GETUSER2:"+devicecode);
            //param.put("devicecode", devicecode);
            logger.debug("GETUSER3:"+devicecode);
            return (String) new DeviceORMap(useMasterForUser).getUserIdByDeviceCode(devicecode);
        } catch (SQLException e) {
            logger.error("Get user id by device code error!", e);
            return null;
        }
    }

    public int clearAccountExceptionMessages(String user_id) {
        AccountORMap orMap = new AccountORMap();
        try {
            orMap.startTransaction();
            int ret = orMap.clearAccountExceptionMessages(user_id);
            orMap.commitTransaction();
            return ret;
        } catch (Exception e) {
            logger.error("clear Account Exception Messages  error", e);
            return 0;
        } finally {
            try {
                orMap.endTransaction();
            } catch (SQLException e) {
                logger.error("clear Account Exception Messages  error", e);
            }
        }
    }

    public int updateAccountMessages(String message, String name, String loginStatus) {
        AccountORMap orMap = new AccountORMap();
        try {
            HashMap<String, String> param = new HashMap<String, String>();
            param.put("message", message);
            param.put("name", name);
            param.put("login_status", loginStatus);
            orMap.startTransaction();
            int ret = orMap.updateAccountMessages(param);
            orMap.commitTransaction();
            return ret;
        } catch (Exception e) {
            logger.error("update Account Messages  error", e);
            return 0;
        } finally {
            try {
                orMap.endTransaction();
            } catch (SQLException e) {
                logger.error("update Account Messages  error", e);
            }
        }
    }

    public int saveEmailGuess(String name) {
        AccountORMap accountORMap = new AccountORMap();

        try {
            Guess guess = new Guess();
            guess.setName(name);
            guess.setCommnet("");
            accountORMap.startTransaction();
            int ret = accountORMap.saveEmailGuess(guess);
            accountORMap.commitTransaction();
            return ret;
        } catch (Exception e) {
            logger.error("save Email Guess error", e);
            return 0;
        } finally {
            try {
                accountORMap.endTransaction();
            } catch (SQLException e) {
                logger.error("save Email Guess error", e);
            }
        }
    }

    public int modifyChangeFlag(String id, String change_flag) {
        UserORMap orm = new UserORMap();
        try {
            HashMap<String, String> param = new HashMap<String, String>();
            param.put("id", id);
            param.put("change_flag", change_flag);

            orm.startTransaction();
            int ret = orm.modifyChangeFlag(param);
            orm.commitTransaction();
            return ret;
        } catch (Exception e) {
            logger.error("modify Change Flag error", e);
            return 0;
        } finally {
            try {
                orm.endTransaction();
            } catch (SQLException e) {
                logger.error("modify Change Flag error", e);
            }
        }
    }

    public List<Account> getAccountToTaskfactory(String sRvcPtlType) throws DALException {
        if (logger.isTraceEnabled())
            logger.trace(String.format("getAccountToTaskfactory(sRvcPtlType=%s)", sRvcPtlType));

        try {
            long start = System.currentTimeMillis();
            List<Account> list = new UserORMap(useMasterForUser).getAccountToTaskfactory(sRvcPtlType);
            logger.info(String.format("loaded %s task factory accounts for %s, in %s millis", (list != null ? list.size() : 0), sRvcPtlType, System.currentTimeMillis() - start));
            return list;
        } catch (Throwable e) {
            throw new DALException(String.format("Unable to load accounts for %s, error code=%s", sRvcPtlType, ErrorCode.CODE_DAL_), e);
        }
    }

    public int addContactTrack(ContactTrack contactTrack) throws DALException {
        ContactTrackORMap orMap = new ContactTrackORMap();
        try {
            orMap.startTransaction();
            int ret = orMap.addContactTrack(contactTrack);
            orMap.commitTransaction();
            return ret;
        } catch (Exception e) {
            logger.error("addContactTrack error", e);
            return 0;
        } finally {
            try {
                orMap.endTransaction();
            } catch (SQLException e) {
                logger.error("addContactTrack error", e);
            }
        }
    }

    public ContactTrack getContactTrack(String userId, String name) throws DALException {
        if (logger.isTraceEnabled())
            logger.trace(String.format("getContactTrack(userId=%s, name=%s)", userId, name));

        ContactTrackORMap contactTrackORMap = new ContactTrackORMap(useMasterForUser);
        try {
            HashMap<String, String> hs = new HashMap<String, String>();
            hs.put("user_id", userId);
            hs.put("name", name);
            return contactTrackORMap.getContactTrack(hs);
        } catch (Exception e) {
            logger.error("getContactTrack error", e);
            return null;
        }
    }

    public int modifyContactTrack(ContactTrack contactTrack) throws DALException {
        ContactTrackORMap orMap = new ContactTrackORMap();

        try {
            orMap.startTransaction();
            int ret = orMap.modifyContactTrack(contactTrack);
            orMap.commitTransaction();
            return ret;
        } catch (Exception e) {
            logger.error("modifyContactTrack error", e);
            return 0;
        } finally {
            try {
                orMap.endTransaction();
            } catch (SQLException e) {
                logger.error("modifyContactTrack error", e);
            }
        }
    }

    public int removeContactTrack(String userId, String name) throws DALException {
        ContactTrackORMap orMap = new ContactTrackORMap();
        try {
            HashMap<String, String> hs = new HashMap<String, String>();
            hs.put("user_id", userId);
            hs.put("name", name);
            orMap.startTransaction();
            int ret = orMap.removeContactTrack(hs);
            orMap.commitTransaction();
            return ret;
        } catch (Exception e) {
            logger.error("removeContactTrack error", e);
            return 0;
        } finally {
            try {
                orMap.endTransaction();
            } catch (SQLException e) {
                logger.error("removeContactTrack error", e);
            }
        }
    }

    public List<Account> getReceiveAccount(String sTransctionId) throws DALException {
        try {
            return new AccountORMap(useMasterForUser).getReceiveAccount(sTransctionId);
        } catch (Exception e) {
            logger.error("getReceiveAccount error", e);
            return new ArrayList<Account>();
        }
    }

    public int modifyDevicePin(String userId, String sPin) throws DALException {
        DeviceORMap orMap = new DeviceORMap();
        try {
            HashMap<String, String> hs = new HashMap<String, String>();
            hs.put("user_id", userId);
            hs.put("pin", sPin);
            orMap.startTransaction();
            int ret = orMap.modifyDevicePin(hs);
            orMap.commitTransaction();
            return ret;
        } catch (Exception e) {
            logger.error("modifyDevicePin error", e);
            return 0;
        } finally {
            try {
                orMap.endTransaction();
            } catch (SQLException e) {
                logger.error("modifyDevicePin error", e);
            }
        }
    }

    public int modifyUserFeatures(String userId, String sFeaturs)
            throws DALException {
        UserORMap orMap = new UserORMap();
        try {
            HashMap<String, String> hs = new HashMap<String, String>();
            hs.put("id", userId);
            hs.put("featurs", sFeaturs);
            orMap.startTransaction();
            int ret = orMap.modifyUserFeatures(hs);
            orMap.commitTransaction();
            return ret;
        } catch (Exception e) {
            logger.error("modifyUserFeatures error", e);
            return 0;
        } finally {
            try {
                orMap.endTransaction();
            } catch (SQLException e) {
                logger.error("modifyUserFeatures error", e);
            }
        }
    }


    public void updateAccountReceiveInfo(Account account) throws DALException {
        new AccountORMap().updateAccountReceiveInfo(account);
    }

    public void updateAccountReceiveInfoAndReceivedDate(Account account) throws DALException {
        Date lastReceivedDate = account.getLast_received_date();
        if (lastReceivedDate != null && !isTimeStampValid(lastReceivedDate)) {
            logger.info(String.format("setting invalid date %s to current date for %s", lastReceivedDate, account.getName()));
            account.setLast_received_date(new Date());
        }
        new AccountORMap().updateAccountReceiveInfoAndReceivedDate(account);
    }


    public void updateAccountSubscribeRequestDate(Integer accountId, java.util.Date date) throws DALException {
        AccountORMap aorm = new AccountORMap();
        aorm.updateAccountSubscribeRequestDate(accountId, date);
    }

    public ExchangeLogin getExchangeLogin(String accountName) {
        try {
            return new ExchangeLoginORMap(useMasterForUser).getExchangeLogin(accountName);
        } catch (SQLException e) {
            logger.info("No entry found in txtbl_exchange_login for account %s: " + accountName);
            return null;
        }
    }

    public void resetLoginFailureCount(String accountName) throws DALException {
        new AccountORMap().resetLoginFailureCount(accountName);
    }

    public void resetCalendarDate(String accountName, Date date) throws DALException {
        new AccountORMap().resetCalendarDate(accountName, date);
    }

    public void resetContactsSyncKey(String accountName, String syncKey) throws DALException {
        new AccountORMap().resetCcontactsSyncKey(accountName, syncKey);
    }

    public Country getCountry(String userId) {
        try {
            String countryString = new UserORMap().getCountry(userId);
            if (!StringUtils.isEmpty(countryString)) {
                return Country.valueOf(countryString);
            }
        } catch (Exception e) {
            logger.error("No valid country found for user %s: " + userId, e);
        }
        return null;
    }

    public int updateAccountFolderAndMessagingId(Account account) {
        UserORMap orm = new UserORMap();
        try {
            orm.startTransaction();
            int ret = orm.modifyAccountFolderAndMessagingId(account);
            orm.commitTransaction();
            return ret;
        } catch (SQLException e) {
            logger.error("Update Account Folder and MessagingId error " + account.getId(), e);
            return 0;
        } finally {
            try {
                orm.endTransaction();
            } catch (SQLException e) {
                logger.error("Update Account Folder and MessagingId error " + account.getId(), e);
            }
        }
    }

    public void addOrUpdateLogin(Account account) {
        new AccountORMap().updateLoginName(account);
    }

    boolean isTimeStampValid(Date time) {
        Timestamp startRange = Timestamp.valueOf("1970-01-01 00:00:01");
        Timestamp endRange = Timestamp.valueOf("2038-01-19 03:14:07");
        return !time.before(startRange) && !time.after(endRange);
    }

    public boolean deleteXobniAccount(String email) {
        AccountORMap orMap = new AccountORMap();
        try {
            return orMap.deleteXobniAccount(email) >= 1;
        } catch (SQLException e) {
            logger.error("Remove account error!", e);
            return false;
        }
    }

    public boolean deleteTwitterAccount(String email) {

        AccountORMap orMap = new AccountORMap();
        try {
            orMap.deleteTwitterAccount(email);
            return true;
        } catch (SQLException e) {
            logger.error("Remove account error!", e);
            return false;
        }
    }

    public AccessToken fetchTwitterToken(String email) {
        try {
            return new AccountORMap().fetchTwitterToken(email);
        } catch (SQLException e) {
            logger.error("Fetch Twitter Account error!", e);
        }
        return null;
    }

    public boolean deactivateEmailAccount(Account account) {
        return updateStatus(account, "0");
    }

    public boolean activateEmailAccount(Account account) {
        return updateStatus(account, "1");
    }

    private boolean updateStatus(Account account, String status) {
        AccountORMap accountORMap = new AccountORMap();
        try {
            boolean success = accountORMap.updateStatus(account.getId(), status) == 1;
            if (success) {
                account.setStatus(status);
            }
            return success;
        } catch (SQLException e) {
            logger.error("unable to activateAccount", e);
            return false;
        }
    }

    public String getUserId(Device device) {
        try {
            return new DeviceORMap(useMasterForUser).getUserId(device);
        } catch (SQLException e) {
            logger.error("get user id fail!", e);
            return null;
        }
    }

    public int getAccountsCountByProvider(String provider) {
        try {
            return new AccountORMap(useMasterForUser).getAccountsCountByProvider(provider);
        } catch (SQLException e) {
            logger.error("getAccountsCountByProvider failed", e);
        }
        return 0;
    }

    public boolean isAccountUsingRestApi(Account account) {
        // TODO: update RequestProcesserServlet.supportsPushMail to use this
        String userId = account.getUser_id();

        if (userId == null || "".equals(userId)) {
            return false;
        }
        User user = getPeekAccountIdByID(userId);
        Device device = getDeviceByUserId(userId);

        return !(user == null || device == null) && UtilsTools.isDeviceUsingRestApi(user, device);
    }

    public int getStalledXobniAccountsCount() {
        try {
            return new AccountORMap(useMasterForUser).getStalledXobniAccountsCount();
        } catch (SQLException e) {
            logger.error("getStalledXobniAccountsCount failed", e);
        }
        return 0;
    }

    public List<Integer> getSyncDisabledXobniAccounts(Date date) {
        try {
            return new AccountORMap(useMasterForUser).getSyncDisabledXobniAccounts(date);
        } catch (Exception e) {
            logger.error("getReceiveAccount error", e);
        }
        return new ArrayList<Integer>();
    }

    public boolean updateXobniAccount(XobniAccount xobniAccount) {
        try {
            AccountORMap accountORMap = new AccountORMap();
            accountORMap.updateXobniAccount(xobniAccount);
            return true;
        } catch (SQLException e) {
            logger.error("unable to update xobni account " + xobniAccount.getName(), e);
            return false;
        }
    }

    //TODO - Paul - xobni - to go
    public boolean disableXobniAccount(String name) {
        AccountORMap accountORMap = new AccountORMap();
        try {
            return accountORMap.updateXobniStatus(name, 0) > 0;
        } catch (SQLException e) {
            logger.error("unable to update status", e);
            return false;
        }
    }

}
