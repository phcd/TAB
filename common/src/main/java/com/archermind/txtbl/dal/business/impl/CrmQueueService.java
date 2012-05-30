package com.archermind.txtbl.dal.business.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.dal.business.ICrmQueueService;
import com.archermind.txtbl.dal.orm.CrmQueueORMap;
import com.archermind.txtbl.domain.CrmQueue;

import org.jboss.logging.Logger;

public class CrmQueueService implements ICrmQueueService {
    private static final Logger logger = Logger.getLogger(CrmQueueService.class);

	public int addCrmQueue(CrmQueue crmQueue) throws DALException {
		return new CrmQueueORMap().addCrmQueue(crmQueue);
	}

	public int delDeviceSimcode(String user_id, String sName) throws DALException {
		CrmQueueORMap crmQueueORMap = new CrmQueueORMap();
		HashMap<String, String> hs = new HashMap<String, String>();
		hs.put("user_id", user_id);
		hs.put("name", sName);
		int li_ret;
		try {
			li_ret = crmQueueORMap.delDeviceSimcode(hs);
		} catch (SQLException e) {
			li_ret = 0;
			logger.error("delDeviceSimcode error!", e);
		}
		return li_ret;
	}

	public List<CrmQueue> getQueueMessage(String user_id, String sName)
			throws DALException {
		CrmQueueORMap crmQueueORMap = new CrmQueueORMap();
		HashMap<String, String> hs = new HashMap<String, String>();
		hs.put("user_id", user_id);
		hs.put("name", sName);
		try {
			return crmQueueORMap.getCrmQueue(hs);
		} catch (SQLException e) {
			logger.error("getQueueMessage error!", e);
            return new ArrayList<CrmQueue>();
		}
	}

}
