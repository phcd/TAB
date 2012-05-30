package com.archermind.txtbl.dal.business.impl;

import com.archermind.txtbl.dal.business.IClientService;
import com.archermind.txtbl.dal.orm.ClientORMap;
import com.archermind.txtbl.domain.Client;
import com.archermind.txtbl.mail.store.MessageStore;
import com.archermind.txtbl.mail.store.MessageStoreFactory;
import com.archermind.txtbl.utils.SysConfigManager;
import org.jboss.logging.Logger;

import java.sql.SQLException;

public class ClientService implements IClientService {


    private static final Logger logger = Logger.getLogger(UserService.class);
    private boolean useMasterForUser;
    private static boolean useMasterForUserDefault;
    public static final MessageStore store = MessageStoreFactory.getStore();

    static {
        useMasterForUserDefault = Boolean.parseBoolean(SysConfigManager.instance().getValue("useMasterForUser", "false"));
    }

    public ClientService() {
        this(useMasterForUserDefault);
    }

    public ClientService(boolean useMasterForUser) {
        this.useMasterForUser = useMasterForUser;
    }

    public Client getClientById(String id) {
        try {
            return new ClientORMap(useMasterForUser).getClientById(id);
        } catch (SQLException e) {
            logger.error("get client by id error!", e);
            return null;
        }
    }
}