package com.archermind.txtbl.taskfactory.strategys;

import com.archermind.txtbl.taskfactory.common.FactoryTools;
import com.archermind.txtbl.taskfactory.common.TFConfigManager;
import org.jboss.logging.Logger;

import java.net.UnknownHostException;

public class MasterLoopStrategy {
    private static String localIP = "";
    private static String taskFactoryAddress = "";

    private static Logger log = Logger.getLogger(MasterLoopStrategy.class);

    public boolean isMaster() {
        try {
            localIP = FactoryTools.getLocalIP();
            if (log.isDebugEnabled())
                log.debug("localIp=" + localIP);

            taskFactoryAddress = TFConfigManager.getInstance().getTASKFACTORY_ADDRESS();
            if (log.isDebugEnabled())
                log.debug("taskFactoryAddress=" + taskFactoryAddress);

            if (taskFactoryAddress != null && taskFactoryAddress.trim().length() > 0) {
                taskFactoryAddress = taskFactoryAddress.split(":")[0];
            }
            
            log.info(String.format("taskFactoryLocalIp=%s, taskFactoryAddress=%s", localIP, taskFactoryAddress));
        } catch (UnknownHostException e) {
            if (localIP == null || localIP.trim().length() < 7) {
                log.error("This machine does not have a ip address, so taskfactory can not be initialize");
                return false;
            }
        }

        // can not use 127.0.0.1
        if (localIP != null && taskFactoryAddress.startsWith(localIP)) {
            log.info(String.format("this is the master, local ip %s is the same as task factory address %s", localIP, taskFactoryAddress));
            return true;
        }
        return false;
    }

}
