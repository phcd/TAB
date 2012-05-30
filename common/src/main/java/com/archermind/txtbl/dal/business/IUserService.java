package com.archermind.txtbl.dal.business;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.domain.*;
import twitter4j.http.AccessToken;

import java.util.Date;
import java.util.List;

public interface IUserService {

	/**
	 * @return 1-successful, 0-failed
	 */
	public abstract int modifyStatus(String user_id, String status) throws DALException;

	public abstract int addAccount(Account account);

	public abstract Account getAccount(String user_id, String name);

	public abstract Account getAccount(long account_id);

	public abstract List<Account> getAccounts(String user_id) throws DALException;

	public abstract int modifyAccount(Account account);

	public abstract int addContacts(List<Contact> list) throws DALException;

	public abstract List<Contact> getPagedContacts(String user_id, int page_no, int page_size, String type) throws DALException;

	public abstract int modifyContacts(List<Contact> list) throws DALException;

	public abstract int removeContact(String user_id, String email, String type) throws DALException;

	public abstract int removeContacts(List<Contact> list) throws DALException;

	public abstract int addDevices(Device device);

	public abstract String getDeviceCode(String user_id) throws DALException;

	public List getDeviceByIDvcIDSim(String devicecode, String simcode) throws DALException;

	public int clearAccountExceptionMessages(String user_id) throws DALException;

	public int updateAccountMessages(String message, String name, String sLoginStatus) throws DALException;

	public int saveEmailGuess(String name);

	public int modifyChangeFlag(String id, String change_flag) throws DALException;

	public List<Account> getAccountToTaskfactory(String sRvcPtlType) throws DALException;

	public int addContactTrack(ContactTrack contactTrack) throws DALException;

	public ContactTrack getContactTrack(String userId, String name) throws DALException;

	public int modifyContactTrack(ContactTrack contactTrack) throws DALException;

	public int removeContactTrack(String userId, String name) throws DALException;

	public List<Account> getReceiveAccount(String sTransctionId) throws DALException;

	public int modifyDevicePin(String userId, String sPin) throws DALException;

	public int modifyUserFeatures(String userId, String sFeaturs) throws DALException;

    public List<Account> getAllAccounts(String userId);

    public boolean resetPeekAccount(String id, List<Account> accounts);

    Country getCountry(String userId);

    public abstract AccessToken fetchTwitterToken(String email);

    public boolean deleteXobniAccount(String email);

    public boolean removeUser(String userid);

    public boolean removeEmailAccount(Account account);

    public int registerUser_new(UserPojo pojo, XobniAccount xobniAccount);

    public boolean resetEmailAccount(Account account);

    boolean deactivateEmailAccount(Account account);

    boolean activateEmailAccount(Account account);

    public boolean deleteTwitterAccount(String email);

    public XobniAccount getXobniAccountByUserID(String userid);

    public boolean updateXobniDBVersion(String accountName, String dbVersion);

    int getAccountsCountByProvider(String provider);

    int getStalledXobniAccountsCount();

    List<Integer> getSyncDisabledXobniAccounts(Date date);
}
