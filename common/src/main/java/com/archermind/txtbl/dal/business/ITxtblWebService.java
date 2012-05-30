package com.archermind.txtbl.dal.business;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.domain.*;

import java.util.List;

public interface ITxtblWebService {

	/** Activate the QWERT account with a new expiry period */
	public List<Account> activateNewExpiryPeriod(Device device) throws DALException;

	/** Deactivate a QWERT account */
	public List<Account> SuspendPeekAccount(Device device) throws DALException;

	public int addPop3Server(Server server) throws DALException;

	public List<TxtblCookie> queryTxtblCookieByAccountName(String accountName) throws DALException;

	public int saveOrUpdateTxtblCookies(List<TxtblCookie> txtblCookies) throws DALException;

	public int getIdByPeekAccount(String peekAccountId) throws DALException;

	public List getAccountExArray(String userId, String status) throws DALException;

	public int transfer(String accountNumber, String fromDeviceIMEI, String fromSIM, String toDeviceIMEI, String toSIM) throws DALException;

	public List getReceiveList(String original_account, String userId, int limit) throws DALException;

	public List getSentList(String original_account, String userId, int limit) throws DALException;

	public List getReceiveAttachmentList(String emailId) throws DALException;

	public List getSentAttachmentList(String emailId) throws DALException;

	public List getHistoryAccountExArray(String userId) throws DALException;

	/**
	 * 
	 * @param accountNumber
	 * @param fromDeviceIMEI
	 * @param fromSIM
	 * @param toDeviceIMEI
	 * @param toSIM
	 * @param ppflag
	 * @param phoneNumber
	 * @return  2; // no datas ,1; // sucess!,3; // update fial!,0; // execute fial!
	 * @throws DALException
	 */
	public int indiaTransfer(String accountNumber, String fromDeviceIMEI, String fromSIM, String toDeviceIMEI, String toSIM, int ppflag, String phoneNumber) throws DALException;
}
