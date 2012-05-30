package com.archermind.txtbl.dal.business.impl;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.dal.business.ITxtblWebService;
import com.archermind.txtbl.dal.orm.AccountORMap;
import com.archermind.txtbl.dal.orm.TxtblWebServiceORMap;
import com.archermind.txtbl.domain.*;

import com.archermind.txtbl.utils.ErrorCode;
import com.archermind.txtbl.utils.StringUtils;
import com.archermind.txtbl.utils.SysConfigManager;
import org.jboss.logging.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//TODO - Paul - move all functionality to either databasesupport or this class
public class TxtblWebService implements ITxtblWebService {
	private static final Logger logger = Logger.getLogger(TxtblWebService.class);

	private static boolean useMasterForWebService;

    static {
        useMasterForWebService = Boolean.parseBoolean(SysConfigManager.instance().getValue("useMasterForWebService",  "false"));
    }

	@SuppressWarnings({"ThrowFromFinallyBlock"})
    public int addPop3Server(Server server) throws DALException {

        TxtblWebServiceORMap ws = new TxtblWebServiceORMap();
        try {
            int li_ret = 1;
            ws.startTransaction();
            int lastId = 0;
            int lastId_sent = 0;
			if (!StringUtils.isEmpty(server.getReceiveProtocolType())) {
				li_ret = ws.addPop3Server(server);
                lastId = server.getId() > 0 ? server.getId() : (int) (Long.parseLong(ws.getMaxServerId().toString()));
			}
			if ((li_ret > 0) &&  !StringUtils.isEmpty(server.getSendProtocolType())) {
                li_ret = ws.addSentServer(server);
                lastId_sent = server.getSent_id() > 0 ? server.getSent_id() : (int) (Long.parseLong(ws.getMaxServerId().toString()));
			}

			if (li_ret != 0) {
				ws.commitTransaction();
				server.setId(lastId);
				server.setSent_id(lastId_sent);
			}
            return li_ret;
		} catch (SQLException e) {
			logger.error("add pop3 server error!", e);
			throw new DALException(ErrorCode.CODE_DAL_, e);
		} finally {
			try {
				ws.endTransaction();
			} catch (SQLException e) {
				logger.error("add pop3 server error!", e);
			}
		}
	}

	public List<Account> activateNewExpiryPeriod(Device device) throws DALException {
        return new TxtblWebServiceORMap().activateNewExpiryPeriod(device);
	}

	public List<Account> SuspendPeekAccount(Device device) throws DALException {
        return new TxtblWebServiceORMap().SuspendPeekAccount(device);
	}

	public List<TxtblCookie> queryTxtblCookieByAccountName(String accountName) throws DALException {
        return new TxtblWebServiceORMap(useMasterForWebService).queryCookieByAccountName(accountName);
	}

	public int saveOrUpdateTxtblCookies(List<TxtblCookie> txtblCookies) throws DALException {
        return new TxtblWebServiceORMap().saveOrUpdateTxtblCookies(txtblCookies);
	}

	public int getIdByPeekAccount(String peekAccountId) throws DALException {
        return new TxtblWebServiceORMap(useMasterForWebService).getIdByPeekAccount(peekAccountId);
	}

	public List getAccountExArray(String userId, String status) throws DALException {
		try {
            HashMap<String, String> parama = new HashMap<String, String>();
            parama.put("user_id", userId);
            parama.put("status", status);
            List<Account> list = new AccountORMap(useMasterForWebService).getAccountExArray(parama);
            for (Account account : list) {
                try {
                    int totalReceived = Integer.parseInt(account.getComment());
                    account.setTotalReceived(totalReceived);
                }
                catch (NumberFormatException e) {
                    logger.warn("account received count not a valid number" + account.getComment());
                }
                try {
                    int totalSent = Integer.parseInt(account.getNeedAuth());
                    account.setTotalSent(totalSent);
                }
                catch (NumberFormatException e) {
                    logger.warn("account sent count not a valid number" + account.getComment());
                }
            }
            return list;
        } catch (SQLException e) {
            logger.error("getAccountExArray error!", e);
            throw new DALException(ErrorCode.CODE_DAL_, e);
        }
	}

	public List getHistoryAccountExArray(String userId) throws DALException {
		try {
            HashMap<String, String> parama = new HashMap<String, String>();
            parama.put("user_id", userId);
            parama.put("status", "1");
            return new AccountORMap(useMasterForWebService).getAccountExArrayNoReceive(parama);
		} catch (SQLException e) {
			logger.error("get History Account error!", e);
			throw new DALException(ErrorCode.CODE_DAL_, e);
		}
	}

	public int transfer(String accountNumber, String fromDeviceIMEI, String fromSIM, String toDeviceIMEI, String toSIM) throws DALException {
		try {
			TxtblWebServiceORMap ws = new TxtblWebServiceORMap();
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("peek_account", accountNumber);
            params.put("device_code", fromDeviceIMEI);
            params.put("sim_code", fromSIM);
            List<Device> devices = ws.transferquery(params);
			if (devices.size() <= 0) {
				return 2; // no datas
            }
            for (Device device : devices) {
                HashMap<String, String> parama1 = new HashMap<String, String>();
                parama1.put("user_id", device.getUser_id());
                parama1.put("device_code", fromDeviceIMEI);
                parama1.put("sim_code", fromSIM);
                parama1.put("toDeviceIMEI", toDeviceIMEI);
                parama1.put("toSIM", toSIM);
                if (ws.transferupdate(parama1) == 0) {
                    return 3; // update fial!
                }
            }
            return 1;
		} catch (Exception e) {
			logger.info("transfer error!", e);
			throw new DALException(ErrorCode.CODE_DAL_, e);
		}
	}

	public List getReceiveList(String original_account, String user_id, int limit) throws DALException {
        try {
            return new TxtblWebServiceORMap(useMasterForWebService).getReceiveList(original_account, Integer.parseInt(user_id), limit);
        } catch (NumberFormatException e) {
            throw new DALException(ErrorCode.CODE_DAL_, "Invalid user id");
        }
    }

	public List getSentList(String original_account, String user_id, int limit) throws DALException {

        try {
            HashMap<String, Object> hs = new HashMap<String, Object>();
            hs.put("original_account", original_account);
            hs.put("user_id", user_id);
            if(limit != 0){
                hs.put("limit", limit);
            }
			return new TxtblWebServiceORMap(useMasterForWebService).getSentList(hs);
		} catch (Exception e) {
			logger.error("get Sent List error!", e);
		}
		return new ArrayList();
	}

	public List getReceiveAttachmentList(String emailId) throws DALException {
        try {
			return new TxtblWebServiceORMap(useMasterForWebService).getReceiveAttachmentList(emailId);
		} catch (Exception e) {
			logger.error("get Receive Attachment List error!",e);
            return new ArrayList();
		}
	}

	public List getSentAttachmentList(String emailId) throws DALException {
        try {
			return new TxtblWebServiceORMap(useMasterForWebService).getSentAttachmentList(emailId);
		} catch (Exception e) {
			logger.error("get Sent Attachment List error!", e);
            return new ArrayList();
		}
	}

	public int indiaTransfer(String accountNumber, String fromDeviceIMEI, String fromSIM, String toDeviceIMEI, String toSIM, int ppflag, String phoneNumber) throws DALException {
		try {
			TxtblWebServiceORMap ws = new TxtblWebServiceORMap();
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("peek_account", accountNumber);
            params.put("device_code", fromDeviceIMEI);
            params.put("sim_code", fromSIM);
			List<Device> list = ws.transferquery(params);
			if (list.size() <= 0) {
				return 2; // no datas
            }
            for (Device device : list) {
                HashMap<String, Object> parama1 = new HashMap<String, Object>();
                parama1.put("user_id", device.getUser_id());
                parama1.put("device_code", fromDeviceIMEI);
                parama1.put("sim_code", fromSIM);
                parama1.put("toDeviceIMEI", toDeviceIMEI);
                parama1.put("toSIM", toSIM);
                parama1.put("pp_flag", ppflag);
                parama1.put("msisdn", phoneNumber);
                if (ws.indiaTransferupdate(parama1) == 0) {
                    return 3; // update fial!
                }
            }
            return 1;
		} catch (Exception e) {
			logger.info("indiaTransfer error!", e);
			throw new DALException(ErrorCode.CODE_DAL_, e);
		}
	}

}
