package com.archermind.txtbl.dal.business.impl;

import com.archermind.txtbl.dal.business.IApiKeyService;
import com.archermind.txtbl.dal.orm.ApiKeyORMap;
import com.archermind.txtbl.dal.orm.ClientFeatureORMap;
import com.archermind.txtbl.domain.ApiKey;
import com.archermind.txtbl.domain.ClientFeature;
import com.archermind.txtbl.mail.store.MessageStore;
import com.archermind.txtbl.mail.store.MessageStoreFactory;
import com.archermind.txtbl.utils.SysConfigManager;
import org.jboss.logging.Logger;

import java.sql.SQLException;

public class ApiKeyService implements IApiKeyService{

    private static final Logger logger = Logger.getLogger(UserService.class);
    private boolean useMasterForUser;
    private static boolean useMasterForUserDefault;
    public static final MessageStore store = MessageStoreFactory.getStore();

    static {
        useMasterForUserDefault = Boolean.parseBoolean(SysConfigManager.instance().getValue("useMasterForUser", "false"));
    }

    public ApiKeyService() {
        this(useMasterForUserDefault);
    }

    public ApiKeyService(boolean useMasterForUser) {
        this.useMasterForUser = useMasterForUser;
    }

    public ApiKey getApiKeyById(String id) {
        try {
            return new ApiKeyORMap(useMasterForUser).getApiKeyById(id);
        } catch (SQLException e) {
            logger.error("get ApiKey by id error!", e);
            return null;
        }
    }

        public ApiKey getApiKeyByAccessKey(String accessKey) {
        try {
            return new ApiKeyORMap(useMasterForUser).getApiKeyById(accessKey);
        } catch (SQLException e) {
            logger.error("get ApiKey by id error!", e);
            return null;
        }
    }
}
