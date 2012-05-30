package com.archermind.txtbl.dal.orm;

import com.archermind.txtbl.domain.SysConfig;
import org.jboss.logging.Logger;

import java.sql.SQLException;
import java.util.List;

public class SysConfigORMap extends BaseORMap {

	private static final Logger logger = Logger.getLogger(SysConfigORMap.class);

    public SysConfigORMap() {}

    @SuppressWarnings("unchecked")
    public List<SysConfig> getConfig() throws SQLException {
		return this.sqlMapClient.queryForList("SystemConfig.getConfig");
	}

	public int updateSystemParameters(SysConfig sysconfig) {
		try {
			this.sqlMapClient.update("SystemConfig.updateSystemParameters",
					sysconfig);
			return 1;
		} catch (Exception e) {

			logger.error("update System Parameters fail!", e);
			return 0;
		}
	}

    @SuppressWarnings("unchecked")
	public List<SysConfig> getPeekConfig() throws SQLException {
		return this.sqlMapClient.queryForList("SystemConfig.getPeekConfig");
	}

    @SuppressWarnings("unchecked")
	public List<SysConfig> getMailboxConfig(String sLikeKey) throws SQLException {
		return this.sqlMapClient.queryForList("SystemConfig.getMailboxConfig", sLikeKey);
	}

	public SysConfig getTaskFactoryParameter(String sName) throws SQLException {
		return (SysConfig) this.sqlMapClient.queryForObject("SystemConfig.getTaskFactoryParameter", sName);
	}
}
