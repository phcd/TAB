package com.archermind.txtbl.dal.orm;

import com.archermind.txtbl.domain.MspToken;
import org.jboss.logging.Logger;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public class MspTokenORMap extends BaseORMap {

    private static final Logger logger = Logger.getLogger(MspTokenORMap.class);

    public MspTokenORMap(BaseORMap another) {
        super(another);
    }

    public MspTokenORMap(boolean useMaster) {
        super(useMaster);
    }

    public MspTokenORMap() {}

	public int addMspToken(MspToken mspToken) {
		int iRet;

		try {
			iRet = 1;
			getSqlMapClient().insert("MspToken.addMspToken", mspToken);
		} catch (SQLException e) {
			iRet = 0;
			logger.error("addMspToken error!", e);
		}
		return iRet;
	}

	public MspToken getMspToken(HashMap hs) throws SQLException {
		return (MspToken) getSqlMapClient().queryForObject("MspToken.getMspToken", hs);
	}

	public int modifyMspToken(MspToken mspToken) throws SQLException {
		return getSqlMapClient().update("MspToken.modifyMspToken", mspToken);
	}

	public int removeMspToken(HashMap hs) throws SQLException {
		return getSqlMapClient().delete("MspToken.removeMspToken", hs);
	}

    @SuppressWarnings("unchecked")
	public List<MspToken> getAllMspToken() throws SQLException {
		return getSqlMapClient().queryForList("MspToken.getMspToken");
	}
}
