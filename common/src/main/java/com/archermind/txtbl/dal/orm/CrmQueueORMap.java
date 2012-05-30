package com.archermind.txtbl.dal.orm;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import com.archermind.txtbl.domain.CrmQueue;
import org.jboss.logging.Logger;

public class CrmQueueORMap extends BaseORMap {

    private static final Logger logger = Logger.getLogger(CrmQueueORMap.class);

    public CrmQueueORMap() {}

    public int addCrmQueue(CrmQueue crmQueue) {
		int iRet;

		try {
			iRet = 1;
			this.sqlMapClient.insert("CrmQueue.addCrmQueue", crmQueue);
		} catch (SQLException e) {
			iRet = 0;
			logger.error("addCrmQueue error!", e);
		}
		return iRet;
	}

	public int delDeviceSimcode(HashMap hs) throws SQLException {
		return this.sqlMapClient.delete("CrmQueue.removeCrmQueue", hs);
	}

    @SuppressWarnings("unchecked")
	public List<CrmQueue> getCrmQueue(HashMap hs) throws SQLException {
		return this.sqlMapClient.queryForList("CrmQueue.getCrmQueue", hs);
	}
}
