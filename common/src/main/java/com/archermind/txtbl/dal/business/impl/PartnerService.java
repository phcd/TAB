package com.archermind.txtbl.dal.business.impl;

import com.archermind.txtbl.dal.business.IPartnerService;
import com.archermind.txtbl.dal.business.IUserService;
import com.archermind.txtbl.dal.orm.PartnerORMap;
import com.archermind.txtbl.dal.orm.UserORMap;
import com.archermind.txtbl.domain.Partner;
import com.archermind.txtbl.mail.store.MessageStore;
import com.archermind.txtbl.mail.store.MessageStoreFactory;
import com.archermind.txtbl.utils.IdUtil;
import com.archermind.txtbl.utils.SysConfigManager;

import java.sql.SQLException;

import org.jboss.logging.Logger;

public class PartnerService implements IPartnerService {

    private static final Logger logger = Logger.getLogger(UserService.class);
    private boolean useMasterForUser;
    private static boolean useMasterForUserDefault;
    public static final MessageStore store = MessageStoreFactory.getStore();

    static {
        useMasterForUserDefault = Boolean.parseBoolean(SysConfigManager.instance().getValue("useMasterForUser", "false"));
    }

    public PartnerService() {
        this(useMasterForUserDefault);
    }

    public PartnerService(boolean useMasterForUser) {
        this.useMasterForUser = useMasterForUser;
    }

    public Partner getPartnerById(String id) {
        try {
            return new PartnerORMap(useMasterForUser).getPartnerById(id);
        } catch (SQLException e) {
            logger.error("get partner by ID error!", e);
            return null;
        }
    }
}