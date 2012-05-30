package com.archermind.txtbl.dal.business.impl;

import com.archermind.txtbl.dal.business.IClientFeatureService;
import com.archermind.txtbl.dal.orm.ClientFeatureORMap;
import com.archermind.txtbl.dal.orm.ClientORMap;
import com.archermind.txtbl.domain.ClientFeature;
import com.archermind.txtbl.mail.store.MessageStore;
import com.archermind.txtbl.mail.store.MessageStoreFactory;
import com.archermind.txtbl.utils.SysConfigManager;
import org.jboss.logging.Logger;

import java.sql.SQLException;

public class ClientFeatureService implements IClientFeatureService{
    private static final Logger logger = Logger.getLogger(UserService.class);
    private boolean useMasterForUser;
    private static boolean useMasterForUserDefault;
    public static final MessageStore store = MessageStoreFactory.getStore();

    static {
        useMasterForUserDefault = Boolean.parseBoolean(SysConfigManager.instance().getValue("useMasterForUser", "false"));
    }

    public ClientFeatureService() {
        this(useMasterForUserDefault);
    }

    public ClientFeatureService(boolean useMasterForUser) {
        this.useMasterForUser = useMasterForUser;
    }

    public ClientFeature getClientFeatureById(String id) {
        try {
            return new ClientFeatureORMap(useMasterForUser).getClientFeatureById(id);
        } catch (SQLException e) {
            logger.error("get client by id error!", e);
            return null;
        }
    }
}
