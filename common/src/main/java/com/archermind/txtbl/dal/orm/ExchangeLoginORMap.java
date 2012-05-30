package com.archermind.txtbl.dal.orm;

import com.archermind.txtbl.domain.ExchangeLogin;

import java.sql.SQLException;
import java.util.HashMap;

public class ExchangeLoginORMap extends BaseORMap {

    public ExchangeLoginORMap(boolean useMaster) {
        super(useMaster);
    }

    public ExchangeLoginORMap() {}

    public int addOrUpdateExchangeLogin(ExchangeLogin exchangeLogin) throws SQLException {
        try {
            ExchangeLogin el = getExchangeLogin(exchangeLogin.getAccountName());
            if (el == null) {
                return addExchangeLogin(exchangeLogin);
            } else {
                return updateExchangeLogin(exchangeLogin);
            }
        } catch (Exception ex) {
            return addExchangeLogin(exchangeLogin);
        }
    }

    public int addExchangeLogin(ExchangeLogin exchangeLogin) throws SQLException {
        return this.sqlMapClient.update("ExchangeLogin.addExchangeLogin", exchangeLogin);
    }

    public int updateExchangeLogin(ExchangeLogin exchangeLogin) throws SQLException {
        return this.sqlMapClient.update("ExchangeLogin.updateExchangeLogin", exchangeLogin);
    }

	public ExchangeLogin getExchangeLogin(String accountName) throws SQLException {
		HashMap<String, String> param = new HashMap<String, String>();
		param.put("accountName", accountName);
		return (ExchangeLogin) this.sqlMapClient.queryForObject("ExchangeLogin.getExchangeLogin", param);
	}

}
