package com.archermind.txtbl.dal.orm;

import com.archermind.txtbl.domain.Server;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerORMap extends BaseORMap {
    public ServerORMap(boolean useMaster) {
        super(useMaster);
    }

    public ServerORMap() { }

	public Server getConfig(HashMap param) throws SQLException {
		try {
			return (Server) this.sqlMapClient.queryForObject("MailServer.getConfig", param);
		} catch (Exception e) {
			return null;
		}
	}

    @SuppressWarnings("unchecked")
	public List<Server> getConfigs(HashMap param) throws SQLException {
		return this.sqlMapClient.queryForList("MailServer.getConfig", param);
	}

    @SuppressWarnings("unchecked")
	public List<Server> getSentConfigs(HashMap param) throws SQLException {
		return this.sqlMapClient.queryForList("MailServer.getSentConfig", param);
	}

    @SuppressWarnings("unchecked")
	public List<Server> getAllReceiveProtocolType() throws SQLException {
		return this.sqlMapClient.queryForList("MailServer.getAllReceiveProtocolType");
	}

    @SuppressWarnings("unchecked")
	public List<Server> getSentServerConfig(String sName) throws SQLException {
		return this.sqlMapClient.queryForList("MailServer.getSentServerConfig", sName);
	}

    @SuppressWarnings("unchecked")
	public List<Server> getMailServerConfig(String sName) throws SQLException {
		return this.sqlMapClient.queryForList("MailServer.getMailServerConfig", sName);
	}

    @SuppressWarnings("unchecked")
    public List<String> getDomainNames(Map params) throws SQLException {
        return this.sqlMapClient.queryForList("MailServer.getDomainNames", params);
    }

}