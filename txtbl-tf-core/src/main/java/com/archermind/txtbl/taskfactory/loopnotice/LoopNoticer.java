package com.archermind.txtbl.taskfactory.loopnotice;

import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.Server;
import com.archermind.txtbl.domain.SysConfig;
import com.archermind.txtbl.taskfactory.ReceiveNoticer;
import com.archermind.txtbl.taskfactory.common.MacroDefine;
import com.archermind.txtbl.taskfactory.common.TFConfigHelp;
import com.archermind.txtbl.taskfactory.common.TFConfigHelpIMP;
import com.archermind.txtbl.utils.SysConfigManager;
import com.archermind.txtbl.utils.UtilsTools;
import org.jboss.logging.Logger;

import java.util.*;
import java.util.Map.Entry;

public class LoopNoticer extends ReceiveNoticer {
    private static Logger logger = Logger.getLogger(LoopNoticer.class);

    private List<SysConfig> destinationGroup = null;//
    private String groupOfProtocols = null;//
    private String[] allProtocols = null;//
    private String[] subscribeArray = null;//

    public static HashMap<String, DestinationInstance> destinationsMap = new HashMap<String, DestinationInstance>();
    public static HashMap<String, String[]> protocolsMap = new HashMap<String, String[]>();

    private static int accountIdIncrement = Integer.valueOf(SysConfigManager.instance().getValue("account.id.increment", "2"));

    private String defaultConfig = null;

    private TFConfigHelp tfConfigHelp = new TFConfigHelpIMP();

    @Override
    public void start() {
        loadParameters();
        init();
    }

    private void loadParameters() {
        if(logger.isTraceEnabled())
            logger.trace("loadParameters");
        defaultConfig = getDefaultConfig(destinationGroup);
        if(logger.isTraceEnabled())
               logger.trace("defaultConfig:"+defaultConfig);
    }

    /**
     * ����������¼destination.target.group.default
     *
     * @param destinationGroup
     * @return
     */
    private String getDefaultConfig(List<SysConfig> destinationGroup) {
        if (logger.isTraceEnabled())
            logger.trace(String.format("getDefaultConfig(destinationGroup=%s)", org.apache.commons.lang.StringUtils
                    .join(destinationGroup, ",")));
        String defaultConfig = null;
        if (destinationGroup == null || destinationGroup.size() == 0) {
            logger
                    .error("can not start task factory, because receiver groups are null");
        } else {
            for (SysConfig s : destinationGroup)
                if (s.getName().indexOf(
                        MacroDefine.SYSCONFIG.CONST_DEFAULT_DESTINATION_NAME) > -1) {
                    defaultConfig = s.getValue();
                }
        }

        return defaultConfig;
    }

    @Override
    public DestinationInstance updateAccount(Account account) {
        logger.info(String.format("updating account %s", account));

        DestinationInstance accountDestination = null;

        if (protocolsMap == null) {
            logger.error("protocolsStub is null!maybe taskfactory has not started!!!");
            return null;
        }

        Set<String> keySet = protocolsMap.keySet();
        String[] name = keySet.toArray(new String[keySet.size()]);

        for (String aName : name) {
            if (Arrays.asList(protocolsMap.get(aName)).contains(account.getReceiveProtocolType())) {
                DestinationInstance destination = destinationsMap.get(aName);

                logger.info(String.format("delegating update account for uid=%s to destination %s", account.getUser_id(), destination));
                destination.updateAccount(account);
                accountDestination = destination;
            }
        }
        if (accountDestination == null)
            logger
                    .error("the receive protocol ["
                            + account.getReceiveProtocolType()
                            + "] of account ["
                            + account.getName()
                            + "] is not exist in loopnoticer,so can not update the account!!!!!!!!!");
        return accountDestination;
    }

    private void init() {
        logger.info("initializing loop noticer...");

        List<String> nameOfProtocols = getNameOfProtocols(allProtocols, subscribeArray);

        String[][] groups = getGroups(groupOfProtocols, subscribeArray);

        String[][] groupsNameValue = getGroupsNameValue(groups, destinationGroup);

        createDestination(nameOfProtocols, groups, groupsNameValue, defaultConfig, destinationsMap, protocolsMap);
    }

    private void createDestination(List<String> nameOfProtocols, String[][] groups, String[][] groupsNameValue,
                                   String defaultConfig,
                                   HashMap<String, DestinationInstance> instanceStub,
                                   HashMap<String, String[]> protocolsStub) {
        if (groupsNameValue != null && groups != null && nameOfProtocols != null && groups.length > 0) {
            for (int i = 0; i < groups.length; i++) {
                String protocol = groupsNameValue[i][0];
                String destinationConfiguration = groupsNameValue[i][1];
                if (protocol != null && protocol.trim().length() != 0
                        && destinationConfiguration != null && destinationConfiguration.trim().length() != 0) {
                    String[] protocols = groups[i];
                    logger.info("start " + protocol + " destination instance....");
                    DestinationInstance destinationInstance = getNewDestinationInstance(destinationConfiguration, protocols, protocol);
                    instanceStub.put(protocol, destinationInstance);
                    protocolsStub.put(protocol, protocols);
                    nameOfProtocols.removeAll(Arrays.asList(protocols));
                    instanceStub.get(protocol).start();
                }
            }

            logger.info("start default destination instance....");

            instanceStub.put(MacroDefine.SYSCONFIG.CONST_DEFAULT_DESTINATION_NAME, getNewDestinationInstance(defaultConfig, nameOfProtocols.toArray(new String[nameOfProtocols.size()]), MacroDefine.SYSCONFIG.CONST_DEFAULT_DESTINATION_NAME));
            protocolsStub.put(MacroDefine.SYSCONFIG.CONST_DEFAULT_DESTINATION_NAME, nameOfProtocols.toArray(new String[nameOfProtocols.size()]));
            instanceStub.get(MacroDefine.SYSCONFIG.CONST_DEFAULT_DESTINATION_NAME).start();
        } else {
            logger.error("Error when create destinations,there are some varible is null!!");
        }
    }

    private DestinationInstance getNewDestinationInstance(String destinationConfiguration, String[] protocols, String protocol) {
        if(UtilsTools.containsIdleProtocol(protocols)) {
            return new IdleDestinationInstance(destinationConfiguration, protocols, protocol, accountIdIncrement);
        }
        return new DestinationInstance(destinationConfiguration, protocols, protocol);
    }

    /**
     * @param groups
     * @param destinationGroup
     * @return
     */
    private String[][] getGroupsNameValue(String[][] groups, List<SysConfig> destinationGroup) {
        String[][] groupsNameValue = null;

        if (groups != null && groups.length > 0 && destinationGroup != null) {
            groupsNameValue = new String[groups.length][2];
            for (SysConfig s : destinationGroup) {
                if (s.getName().indexOf(MacroDefine.SYSCONFIG.CONST_DEFAULT_DESTINATION_NAME) > -1)
                    continue;

                for (int i = 0; i < groups.length; i++) {
                    if (Arrays.asList(groups[i]).contains(s.getName().split("\\.")[3])) {
                        groupsNameValue[i][0] = s.getName().split("\\.")[3];
                        groupsNameValue[i][1] = s.getValue();
                    }
                }
            }
        }

        return groupsNameValue;
    }

    /**
     * @return
     */
    private String[][] getGroups(String groupOfProtocols, String[] subscribeArray) {
        String[][] groups = null;

        if (groupOfProtocols != null) {
            String[] tmp = groupOfProtocols.split(";");
            groups = new String[tmp.length][];
            for (int i = 0; i < tmp.length; i++) {
                groups[i] = tmp[i].split(",");
            }
            groups = filtGroups(groups, subscribeArray);
        }
        return groups;
    }

    private List<String> getNameOfProtocols(String[] allProtocols, String[] subscribeArray) {
        if (logger.isTraceEnabled())
            logger.trace(String.format("getNameOfProtocols(allProtocols=%s, subscribeArray=%s)", org.apache.commons.lang.StringUtils
                    .join(allProtocols, ","), org.apache.commons.lang.StringUtils.join(subscribeArray, ",")
            ));
        List<String> nameOfProtocols = null;
        if (allProtocols != null) {
            nameOfProtocols = new ArrayList<String>();
            int i = 0;
            for (String s : allProtocols) {
                logger.info("all protocls " + i + " [" + s + "]");
                nameOfProtocols.add(s);
                i++;
            }
            removeSubscribeProtocols(nameOfProtocols, subscribeArray);
        } else {
            logger.error("allprotocols is null!!!");
        }

        return nameOfProtocols;
    }

    private void removeSubscribeProtocols(List<String> nameOfProtocols, String[] subscribeArray) {
        if (subscribeArray == null)
            return;

        List<String> subscribeProtocols = tfConfigHelp.getSubscribeProtocols(subscribeArray);

        nameOfProtocols.removeAll(subscribeProtocols);
    }

    private String[][] filtGroups(String[][] groups, String[] subscribeArray) {
        String[][] result = null;
        List<String> protocols = tfConfigHelp.getSubscribeProtocols(subscribeArray);
        List<String[]> resultList = new ArrayList<String[]>();
        List<String> nullString = new ArrayList<String>();
        nullString.add("");

        if (groups != null) {
            for (String[] group : groups) {
                List<String> tmp = new ArrayList<String>();
                tmp.addAll(Arrays.asList(group));
                tmp.removeAll(protocols);
                tmp.removeAll(nullString);
                if (tmp.size() > 0) {
                    resultList.add(tmp.toArray(new String[tmp.size()]));
                }
            }
            result = resultList.toArray(new String[resultList.size()][0]);
        }

        return result;
    }

    @Override
    public void updateMSConfig(Server server) {
        try {
            String command = server.getCommand();
            logger.info("Mail server,status: " + command + " ID: " + server.getName() + "_" + server.getId());
            // String id = String.valueOf(server.getId());

            if (command.equals(MacroDefine.OTHER.CONST_DELETETASK)
                    || command.equals(MacroDefine.OTHER.CONST_ADDTASK)) {
                // TaskFactoryEngineImp.mailServerMap.remove(id);
            } else {
                Set<String> keySet = protocolsMap.keySet();
                String[] name = keySet.toArray(new String[keySet.size()]);
                for (String aName : name) {
                    if (Arrays.asList(protocolsMap.get(aName)).contains(server.getReceiveProtocolType())) {
                        destinationsMap.get(aName).reloadAccount();
                    }
                }
                // TaskFactoryEngineImp.mailServerMap.put(id, server);
            }

        } catch (Exception e) {
            logger.error("Receive mail server config message failed", e);
        }

    }

    @Override
    public void updateConfig(SysConfig sysConfig) {
    }

    @Override
    public void pop3CollectionClear() {
        logger.info("pop3 collection clear!!!");
        for (Entry<String, DestinationInstance> stringDestinationInstanceEntry : destinationsMap.entrySet()) {
            stringDestinationInstanceEntry.getValue().pop3CollectionClear();
        }
    }

    public void setDestinationGroup(List<SysConfig> destinationGroup) {
        this.destinationGroup = destinationGroup;
    }

    public void setGroupOfProtocols(String groupOfProtocols) {
        this.groupOfProtocols = groupOfProtocols;
    }

    public void setAllProtocols(String[] allProtocols) {
        this.allProtocols = allProtocols;
    }

    public void setSubscribeArray(String[] subscribeArray) {
        this.subscribeArray = subscribeArray;
    }

}
