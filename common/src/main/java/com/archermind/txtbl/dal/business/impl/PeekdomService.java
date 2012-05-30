package com.archermind.txtbl.dal.business.impl;

import com.archermind.txtbl.dal.orm.PeekdomORMap;
import com.archermind.txtbl.domain.PeekdomUser;
import com.archermind.txtbl.utils.StringUtils;
import com.archermind.txtbl.utils.SysConfigManager;
import org.jboss.logging.Logger;

import java.sql.SQLException;
import java.util.HashMap;

public class PeekdomService {
    private static final Logger logger = Logger.getLogger(PeekdomService.class);
    private static boolean useMasterForUser;

    static {
        useMasterForUser = Boolean.parseBoolean(SysConfigManager.instance().getValue("useMasterForUser", "false"));
    }

    public PeekdomUser getUser(String userName, String password) {
        try {
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("userName", userName);
            if(StringUtils.isNotEmpty(password)) {
                params.put("password", password);
            }
            return new PeekdomORMap(useMasterForUser).getUser(params);
        } catch (SQLException e) {
            logger.error(String.format("Get peekdom user error userName=%s", userName), e);
            return null;
        }

    }
}
