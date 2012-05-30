package com.archermind.txtbl.dal.business.impl;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.dal.business.IMspTokenService;
import com.archermind.txtbl.dal.orm.MspTokenORMap;
import com.archermind.txtbl.domain.MspToken;
import com.archermind.txtbl.utils.SysConfigManager;
import org.jboss.logging.Logger;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public class MspTokenService implements IMspTokenService {
    private static final Logger logger = Logger.getLogger(MspTokenService.class);

    private static boolean useMasterForMSP;

    static {
        useMasterForMSP = Boolean.parseBoolean(SysConfigManager.instance().getValue("useMasterForMSP",  "false"));
    }

	public MspToken getMspToken(String user_id, String sName) throws DALException {
		try {
            HashMap<String, Object> hs = new HashMap<String, Object>();
            hs.put("user_id", user_id);
            hs.put("name", sName);
            return new MspTokenORMap(useMasterForMSP).getMspToken(hs);
		} catch (SQLException e) {
			logger.error("getMspToken error!", e);
		}
		return null;
	}

	public int setMspToken(MspToken mspToken) throws DALException {
        if(mspToken == null) {
            return 0;
        }
        MspTokenORMap mspTokenORMap = new MspTokenORMap();
		try {
			mspTokenORMap.startTransaction();
            HashMap<String, Object> hs = new HashMap<String, Object>();
            hs.put("user_id", mspToken.getUser_id());
            hs.put("name", mspToken.getName());
            MspToken msp = mspTokenORMap.getMspToken(hs);
            int li_ret;
            if (msp != null) {
                li_ret = mspTokenORMap.modifyMspToken(mspToken);
            } else {
                li_ret = mspTokenORMap.addMspToken(mspToken);
            }
			mspTokenORMap.commitTransaction();
            return li_ret;
		} catch (SQLException e) {
			logger.error("setMspToken error!", e);

		} finally {
			try {
				mspTokenORMap.endTransaction();
			} catch (SQLException e) {
				logger.error("setMspToken error!", e);
			}
		}
		return 0;
	}

	public List<MspToken> getAllMspToken() throws DALException {
        try {
			return new MspTokenORMap(useMasterForMSP).getAllMspToken();
		} catch (SQLException e) {
			logger.error("getAllMspToken error!", e);
		}
		return null;
	}
}
