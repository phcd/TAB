package com.archermind.txtbl.receiver.mail.bean;


import org.jboss.logging.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class IdleProcessMap {

    private static final Logger log = Logger.getLogger(IdleProcessMap.class);

    public static final String DELETE_COMMAND = "2";

    private static final Map<String, IdleProcess> processMap = new ConcurrentHashMap<String, IdleProcess>();

    public Map<String, IdleProcess> getProcessMap() {
        return processMap;
    }



    public boolean handleCommand(String command, String idleProcessKey) {

        if (DELETE_COMMAND.equals(command)){
            log.info("IdleProcessMap received delete command");
            return delete(idleProcessKey);
        }

        return true;



    }

    private Boolean delete(String idleProcessKey){
            log.debug("idle process map received delete command");
            IdleProcess process = processMap.remove(idleProcessKey);
            if(process==null){
                  log.warn("received delete command for account that does not exist " + idleProcessKey);
                  return false;
            }else{
                process.disconnect();
                return false;
            }



    }


}
