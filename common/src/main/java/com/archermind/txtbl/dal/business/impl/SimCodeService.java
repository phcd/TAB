package com.archermind.txtbl.dal.business.impl;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.dal.business.ISimCodeService;
import com.archermind.txtbl.dal.orm.SimCodeORMap;
import com.archermind.txtbl.domain.SimCode;

import com.archermind.txtbl.utils.ErrorCode;
import com.archermind.txtbl.utils.SysConfigManager;
import org.jboss.logging.Logger;

public class SimCodeService implements ISimCodeService {
    private static final Logger logger = Logger.getLogger(SimCodeService.class);    


    private static boolean useMasterForSimCode;

    static {
        useMasterForSimCode = Boolean.parseBoolean(SysConfigManager.instance().getValue("useMasterForSimCode",  "false"));
    }

	public List<SimCode> viewDeviceSimcode(String simCode) throws DALException {
        try {
            HashMap<String, Object> hsmap = new HashMap<String, Object>();
			if (simCode != null) {
				hsmap.put("simCode", simCode);
			}
			return new SimCodeORMap(useMasterForSimCode).loadAllSimCode(hsmap);
		} catch (SQLException e) {
			logger.error("View Device Simcode error!", e);
			throw new DALException(ErrorCode.CODE_DAL_, e);
		}
	}

}
