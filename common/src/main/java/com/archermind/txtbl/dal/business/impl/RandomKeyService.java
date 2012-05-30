package com.archermind.txtbl.dal.business.impl;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.dal.business.IRandomKeyService;
import com.archermind.txtbl.dal.mode.SqlMapFactory;
import com.archermind.txtbl.dal.orm.RandomKeyORMap;
import com.archermind.txtbl.domain.RandomKey;
import com.archermind.txtbl.utils.SysConfigManager;
import com.ibatis.sqlmap.client.SqlMapClient;
import org.jboss.logging.Logger;

import java.sql.SQLException;
import java.util.HashMap;

public class RandomKeyService implements IRandomKeyService {

    private static final Logger logger = Logger.getLogger(RandomKeyService.class);

    private static HashMap<Integer, RandomKey> randomKeys = new HashMap<Integer, RandomKey>();

    private static boolean useMasterForRandomKey;

    static {
        useMasterForRandomKey = Boolean.parseBoolean(SysConfigManager.instance().getValue("useMasterForRandomKey",  "false"));
    }
    
	public RandomKey getRandomKey(int iKeyId) throws DALException {
        if(randomKeys.containsKey(iKeyId)) {
            return randomKeys.get(iKeyId);
        } else {
            try {
                SqlMapClient sqlMapClient = useMasterForRandomKey ? SqlMapFactory.getMasterSqlMapClient() : SqlMapFactory.getSlaveSqlMapClient();
                RandomKey randomKey = new RandomKeyORMap().getRandomKey(iKeyId, sqlMapClient);
                randomKeys.put(iKeyId, randomKey);
                return randomKey;
            } catch (SQLException e) {
                logger.error("getRandomKey error!", e);
            }
            return null;
        }
	}

	public RandomKey getMaxRandomKey() throws DALException {
		try {
            SqlMapClient sqlMapClient = useMasterForRandomKey ? SqlMapFactory.getMasterSqlMapClient() : SqlMapFactory.getSlaveSqlMapClient();
            return new RandomKeyORMap().getMaxRandomKey(sqlMapClient);
		} catch (SQLException e) {
			logger.error("getMaxRandomKey error!", e);
		}
		return null;
	}

}
