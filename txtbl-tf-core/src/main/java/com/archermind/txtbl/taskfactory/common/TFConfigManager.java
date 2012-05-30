package com.archermind.txtbl.taskfactory.common;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.dal.business.impl.EmailServerService;
import com.archermind.txtbl.dal.business.impl.SysConfigService;
import com.archermind.txtbl.domain.SysConfig;
import org.jboss.logging.Logger;

import java.util.List;

public class TFConfigManager {
    private static Logger logger = Logger
            .getLogger(TFConfigManager.class);
    private static TFConfigManager tfConfigManager = null;

    List<SysConfig> destinationGroup = null;
    String groupOfProtocols = null;
    SysConfig subscribesConfig = null;
    String TASKFACTORY_ADDRESS = "127.0.0.1:1099";
    SysConfig defaultGroupDestination = null;
    SysConfig taskfactoryURLConfig = null;
    String webNoticeURLConfig = "127.0.0.1:1099";
    String webNoticeJndiConfig = "reloadwebconfig";
    String port = "1099";
    String JNDI_RECEIVER = "receivemail";
    long subscribeTimeOut = 30;   // minutes
    int pop3CollectionClearTime = 0;
    String allProtocols[] = null;

    private TFConfigManager() {

        SysConfigService syscs = new SysConfigService();

        try {
            destinationGroup = syscs.getMailboxConfig(MacroDefine.SYSCONFIG.CONST_GROUP_ID_Prefix + "%");
            if(logger.isTraceEnabled())
                 logger.trace("destinationGroup["+MacroDefine.SYSCONFIG.CONST_GROUP_ID_Prefix+":"+org.apache.commons.lang.StringUtils.join(destinationGroup,",")+"]");
        }
        catch (DALException e) {
            logger.error("unable to load destination groups", e);
        }

        try {
            groupOfProtocols = syscs.getTaskFactoryParameter(MacroDefine.SYSCONFIG.CONST_GROUP_ID).getValue();
            if(logger.isTraceEnabled())
                  logger.trace("groupOfProtocols["+MacroDefine.SYSCONFIG.CONST_GROUP_ID+":"+groupOfProtocols+"]");
         }
        catch (DALException e) {
            logger.error("unable to load protocols", e);
        }

        try {
            subscribesConfig = syscs.getTaskFactoryParameter(MacroDefine.SYSCONFIG.CONST_TASKFACTORY_SUBSCRIBE_GROUP);
            if (logger.isDebugEnabled())
                logger.debug("subscribesConfig=" + subscribesConfig);

        }
        catch (DALException e) {
            logger.error("unable to load subscribers", e);
        }

        try {
            taskfactoryURLConfig = syscs
                    .getTaskFactoryParameter(MacroDefine.SYSCONFIG.CONST_TASKFACTORY_PROVIDER_URL_NAME);
            if (logger.isDebugEnabled())
                logger.debug("taskFactoryConfig=" + String.valueOf(syscs));

            if (taskfactoryURLConfig != null) {
                TASKFACTORY_ADDRESS = taskfactoryURLConfig.getValue();
            }

            if (TASKFACTORY_ADDRESS != null)
                port = TASKFACTORY_ADDRESS.split(":")[1];

            if (logger.isDebugEnabled())
                logger.debug(String.format("taskFactoryAddress=%s taskfactoryPort=%s", TASKFACTORY_ADDRESS, port));

        } catch (DALException e) {
            logger.error("error when get TASKFACTORY_ADDRESS!!!!", e);
        }

        try {
            defaultGroupDestination = syscs
                    .getTaskFactoryParameter(MacroDefine.SYSCONFIG.CONST_GROUP_ID_Prefix
                            + "."
                            + MacroDefine.SYSCONFIG.CONST_DEFAULT_DESTINATION_NAME);
        } catch (DALException e) {
            logger.error("error when get defaultGroupDestination!!!!", e);
        }

        try {
            webNoticeURLConfig = syscs.getTaskFactoryParameter(
                    MacroDefine.SYSCONFIG.CONST_WEB_RELOAD_URL_KEY).getValue();
        } catch (DALException e) {
            logger.error("error when get webNoticeURLConfig!!!!", e);
        }

        try {
            webNoticeJndiConfig = syscs.getTaskFactoryParameter(
                    MacroDefine.SYSCONFIG.CONST_WEB_RELOAD_JNDI_KEY).getValue();
        } catch (DALException e) {
            logger.error("error when get webNoticeJndiConfig!!!!", e);
        }

        try {
            JNDI_RECEIVER = syscs.getTaskFactoryParameter(
                    MacroDefine.SYSCONFIG.CONST_JNDI_RECEIVER_KEY).getValue();
        } catch (DALException e) {
            logger.error("error when get JNDI_RECEIVER!!!!", e);
        }

        try {
            SysConfig subscribeTimeOutConfig = syscs
                    .getTaskFactoryParameter(MacroDefine.SYSCONFIG.CONST_SUBSCRIBE_TIMEOUT_KEY);
            if (subscribeTimeOutConfig != null) {
                String subscribeTimeOutStr = subscribeTimeOutConfig.getValue();
                if (subscribeTimeOutStr != null)
                    subscribeTimeOut = Long.valueOf(subscribeTimeOutStr);
                else
                    logger.warn("the record ["
                            + MacroDefine.SYSCONFIG.CONST_SUBSCRIBE_TIMEOUT_KEY
                            + "] in database is null!!! use default timeout="
                            + subscribeTimeOut);
            } else {
                logger.warn("there is no record ["
                        + MacroDefine.SYSCONFIG.CONST_SUBSCRIBE_TIMEOUT_KEY
                        + "] in database!!! use default timeout="
                        + subscribeTimeOut);
            }
        } catch (DALException e) {
            logger.error("error when get "
                    + MacroDefine.SYSCONFIG.CONST_SUBSCRIBE_TIMEOUT_KEY
                    + " from database", e);
        }

        try {
            String pop3CollectionClearTimeStr = syscs.getTaskFactoryParameter(
                    MacroDefine.SYSCONFIG.CONST_Pop3_Collection_Clear_Time)
                    .getValue();
            if (pop3CollectionClearTimeStr != null
                    && !pop3CollectionClearTimeStr.equals("")) {
                pop3CollectionClearTime = Integer
                        .parseInt(pop3CollectionClearTimeStr);
            }
        } catch (DALException e) {
            logger.error("error when get JNDI_RECEIVER!!!!", e);
        }

        try {
            allProtocols = new EmailServerService().getAllReceiveProtocolType();
        } catch (DALException e) {
            logger.error("Error when get all receive protocols!!!!", e);
        }
    }

    public static TFConfigManager getInstance() {
        synchronized (TFConfigManager.class) {
            if (tfConfigManager != null)
                return tfConfigManager;
            else {
                tfConfigManager = new TFConfigManager();
                return tfConfigManager;
            }
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {

    }

    public List<SysConfig> getDestinationGroup() {
        return destinationGroup;
    }

    public String getGroupOfProtocols() {
        return groupOfProtocols;
    }

    public SysConfig getSubscribesConfig() {
        return subscribesConfig;
    }

    public String getTASKFACTORY_ADDRESS() {
        return TASKFACTORY_ADDRESS;
    }

    public SysConfig getDefaultGroupDestination() {
        return defaultGroupDestination;
    }

    public SysConfig getTaskfactoryURLConfig() {
        return taskfactoryURLConfig;
    }

    public String getWebNoticeURLConfig() {
        return webNoticeURLConfig;
    }

    public String getWebNoticeJndiConfig() {
        return webNoticeJndiConfig;
    }

    public String getJNDI_RECEIVER() {
        return JNDI_RECEIVER;
    }

    public long getSubscribeTimeOut() {
        return subscribeTimeOut;
    }

    public int getPop3CollectionClearTime() {
        return pop3CollectionClearTime;
    }

    public String[] getAllProtocols() {
        return allProtocols;
    }
}
