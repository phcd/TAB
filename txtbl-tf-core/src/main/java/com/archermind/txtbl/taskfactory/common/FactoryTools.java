package com.archermind.txtbl.taskfactory.common;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.dal.business.impl.UserService;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.SysConfig;
import org.apache.commons.lang.StringUtils;
import org.jboss.logging.Logger;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;


public class FactoryTools {
    private static Logger logger = Logger.getLogger(FactoryTools.class);

    public static String[] getSubscribeArray() {
        if(logger.isTraceEnabled())
            logger.trace("getSubscribeArray()");

        SysConfig subscribesConfig;

        subscribesConfig = TFConfigManager.getInstance().getSubscribesConfig();

        if (subscribesConfig == null) {
            logger.error("there is no subscribe protocols in the database!!!");
            return null;
        }

        String subscribesValue = subscribesConfig.getValue();
        if(logger.isTraceEnabled())
            logger.trace("subscribesValue="+subscribesValue);

        if (subscribesValue == null) {
            logger.error("the value of subscribe protocols is null!!!");
            return null;
        }

        return subscribesValue.split(";");
    }

    public static String getLocalIP() throws UnknownHostException {
        InetAddress localhost = InetAddress.getLocalHost();
        return localhost.getHostAddress();
    }

    public static List<Account> loadAccounts(String[] protocols) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadAccounts(protocols=%s)", org.apache.commons.lang.StringUtils
                    .join(protocols, ",")));
        
        List<Account> source = new ArrayList<Account>();

        for (String protocol : protocols) {
            try {
                source.addAll(new UserService().getAccountToTaskfactory(protocol));
                logger.info(String.format("loaded %s accounts for protocols: %s", source.size(), StringUtils.join(protocols, ",")));
            }
            catch (DALException e) {
                logger.error(String.format("load accounts for %s failed", protocol), e);
                break;
            }
        }

        return source;
    }

    public static Serializable copyObject(Object object) {
        Serializable newObject;
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream out;
            out = new ObjectOutputStream(byteOut);
            out.writeObject(object);

            ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut
                    .toByteArray());
            ObjectInputStream in = new ObjectInputStream(byteIn);
            newObject = (Serializable) in.readObject();
            return newObject;
        }
        catch (IOException e) {
            logger.error("error when copy account", e);
            return null;
        }
        catch (ClassNotFoundException e) {
            logger.error("error when copy account", e);
            return null;
        }
    }
}
