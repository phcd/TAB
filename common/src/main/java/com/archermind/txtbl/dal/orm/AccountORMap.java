package com.archermind.txtbl.dal.orm;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.domain.*;
import org.jboss.logging.Logger;
import twitter4j.http.AccessToken;

import java.sql.SQLException;
import java.util.*;

public class AccountORMap extends BaseORMap {
    private static final Logger logger = Logger.getLogger(AccountORMap.class);

    public AccountORMap(BaseORMap another) {
        super(another);
    }

    public AccountORMap(boolean useMaster) {
        super(useMaster);
    }

    public AccountORMap() {
    }

    /**
	 * save new account info
	 *
	 * @param account
	 * @return 1-successful, 0-failed
	 * @throws SQLException
	 */
	public int addAccount(Account account) throws SQLException {
		return this.sqlMapClient.update("Account.addAccount", account);
	}

	public Integer getUserId(String name) throws SQLException {
		return (Integer) this.sqlMapClient.queryForObject("Account.getUserId", name);
	}

    public String getUserIdByAccId(String accountId) throws SQLException {
		return (String) this.sqlMapClient.queryForObject("Account.getUserIdByAccId", accountId);
	}

    public XobniAccount getXobniAccountByUserID(HashMap param) throws SQLException {
        return (XobniAccount) this.sqlMapClient.queryForObject("Account.getXobniAccountData", param);
    }


    public int getXobniAccountCount(String name) throws SQLException {
        return (Integer) this.sqlMapClient.queryForObject("Account.getXobniAccountCount", name);
    }


    public AccessToken fetchTwitterToken(String email) throws SQLException {
        TwitterAccount account = (TwitterAccount)this.sqlMapClient.queryForObject("Account.getTwitterToken",email);
        return new AccessToken(account.getoAuthToken(),account.getoAuthTokenSecret());
    }
    
    @SuppressWarnings("unchecked")
	public List<Account> getAccounts(HashMap param) throws SQLException {
		List<Account> accounts = this.sqlMapClient.queryForList("Account.getAccount", param);
        updateLoginName(accounts);
        return accounts;
	}

    @SuppressWarnings("unchecked")
	public List<Account> getAccountExArray(HashMap param) throws SQLException {
		List<Account> accounts = this.sqlMapClient.queryForList("Account.getAccountExArray", param);
        updateLoginName(accounts);
        return accounts;
	}

    @SuppressWarnings("unchecked")
	public List<Account> getAccountExArrayNoReceive(HashMap param) throws SQLException {
		List<Account> accounts = this.sqlMapClient.queryForList("Account.getAccountExArrayNoReceive", param);
        updateLoginName(accounts);
        return accounts;
	}

	public Account getAccount(HashMap param) throws SQLException {
		Account account = (Account) this.sqlMapClient.queryForObject("Account.getAccount", param);
        updateLoginName(account);
        return account;
	}

    public HashMap getAccountData(String name) throws SQLException {
		return (HashMap)this.sqlMapClient.queryForObject("Account.getAccountData", name);
	}

    public java.util.Date getAccountSubscribeAlertDate(String name) throws SQLException {
		return (java.util.Date)this.sqlMapClient.queryForObject("Account.getAccountSubscribeAlertDate", name);
	}

	public int modifyAccount(Account account) throws SQLException {
		return this.sqlMapClient.update("Account.modifyAccount", account);
	}

	public int clearAccountExceptionMessages(String user_id) throws SQLException {
		return this.sqlMapClient.update("Account.clearAccountExceptionMessages", user_id);
	}

	public int updateAccountMessages(HashMap param) throws SQLException {
		return this.sqlMapClient.update("Account.updateAccountMessages", param);
	}

	public int saveEmailGuess(Guess guess) throws SQLException {
		return this.sqlMapClient.update("Account.saveEmailGuess", guess);
	}

    @SuppressWarnings("unchecked")
	public List<Account> getReceiveAccount(String sTransctionId) throws SQLException {
		List<Account> accounts = this.sqlMapClient.queryForList("Account.getReceiveAccount", sTransctionId);
        updateLoginName(accounts);
        return accounts;
	}

    private void updateLoginName(Collection<Account> accounts) {
        for (Account account : accounts) {
            updateLoginName(account);
        }
    }

    public void updateLoginName(Account account) {
        if (account != null) {
            ExchangeLoginORMap exchangeLoginORMap = new ExchangeLoginORMap();
            try {
                ExchangeLogin exchangeLogin = exchangeLoginORMap.getExchangeLogin(account.getName());
                if (exchangeLogin != null) {
                    account.setLoginName(exchangeLogin.getLoginName());
                }
            } catch (SQLException ex) {
                logger.info("No entry found in txtbl_exchange_login for account %s: " + account.getName());
            }
        }
    }

    public void updateAccountReceiveInfo(Account account) throws DALException {
        try {
            this.sqlMapClient.update("Account.updateAccountReceiveInfo", account);
        } catch (SQLException e) {
            throw new DALException("Unable to update account " + account, e);
        }
    }

    public int updateXobniDBVersion(String name, String dbVersion) throws DALException {
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("name", name);
            params.put("dbVersion", dbVersion);

            return this.sqlMapClient.update("Account.updateXobniDBVersion", params);
        } catch (SQLException e) {
            throw new DALException("Unable to update xobni account " + name, e);
        }
    }

    public void updateAccountReceiveInfoAndReceivedDate(Account account) throws DALException {
        try {
            this.sqlMapClient.update("Account.updateAccountReceiveInfoAndReceivedDate", account);
        } catch (SQLException e) {
            throw new DALException("Unable to update account " + account, e);
        }
    }


    public void updateAccountSubscribeRequestDate(Integer accountId, java.util.Date date) throws DALException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("accountId", accountId);
        params.put("date", date);

        try {
            this.sqlMapClient.update("Account.updateAccountSubscribeRequestDate", params);
        } catch (SQLException e) {
            throw new DALException("Unable to update subscribe request date for account " + accountId + ", to " + date, e);
        }
    }

    public void resetLoginFailureCount(String accountName) throws DALException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("accountName", accountName);

        try {
            this.sqlMapClient.update("Account.resetLoginFailureCount", params);
        } catch (SQLException e) {
            throw new DALException("Unable to reset login failure count " + accountName, e);
        }
    }

    public void resetCalendarDate(String accountName, java.util.Date date) throws DALException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("accountName", accountName);
        params.put("date", date);

        try {
            this.sqlMapClient.update("Account.resetCalendarDate", params);
        } catch (SQLException e) {
            throw new DALException("Unable to reset last_calendar failure count " + accountName, e);
        }
    }

    public void resetCcontactsSyncKey(String accountName, String syncKey) throws DALException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("accountName", accountName);
        params.put("contacts_sync_key", syncKey);

        try {
            this.sqlMapClient.update("Account.resetContactsKey", params);
        } catch (SQLException e) {
            throw new DALException("Unable to reset contacts sync key " + accountName, e);
        }
    }

    public int deleteXobniAccount(String email) throws SQLException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", email);
        return this.sqlMapClient.update("Account.deleteXobniAccount", params);
    }

    public int addXobniAccount(XobniAccount xobniAccount) throws SQLException {
        return this.sqlMapClient.update("Account.addXobniAccount", xobniAccount);
    }

    public int deleteTwitterAccount(String email) throws SQLException{
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", email);
        return this.sqlMapClient.update("Account.deleteTwitterAccount", params);
    }

    public int updateStatus(int id, String status) throws SQLException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);
        params.put("status", status);
        return this.sqlMapClient.update("Account.updateStatus", params);
    }

    public int updateXobniAccount(XobniAccount xobniAccount) throws SQLException {
        return this.sqlMapClient.update("Account.updateXobniAccount", xobniAccount);
    }


    //TODO - Paul - xobni - to go
    public int updateXobniStatus(String name, int status) throws SQLException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", name);
        params.put("status", status);
        return this.sqlMapClient.update("Account.updateXobniStatus", params);
    }

    public int getAccountsCountByProvider(String provider) throws SQLException {
        return (Integer) this.sqlMapClient.queryForObject("Account.getAccountsCountByProvider", provider);
    }

    public int getStalledXobniAccountsCount() throws SQLException {
        return (Integer) this.sqlMapClient.queryForObject("Account.getStalledXobniAccountsCount");
    }

    @SuppressWarnings("unchecked")
    public List<Integer> getSyncDisabledXobniAccounts(Date date) throws SQLException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("country", Country.Xobni);
        params.put("date", date);
        return this.sqlMapClient.queryForList("Account.getSyncDisabledXobniAccounts", params);
    }
}