package com.archermind.txtbl.receiver.mail.bean;

import com.archermind.txtbl.domain.Account;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GoogleVoiceProcessMap {

    private static final Logger log = Logger.getLogger(GoogleVoiceProcessMap.class);

    private static final Map<String, GoogleVoiceProcess> processMap = new ConcurrentHashMap<String, GoogleVoiceProcess>();

    public Map<String, GoogleVoiceProcess> getProcessMap() {
        return processMap;
    }

    public static GoogleVoiceProcess getProcess(Account account) throws IOException {
        String processKey = account.getUser_id() + "_" + account.getName();
        GoogleVoiceProcess process;
        log.info("attempting to retrieve process for " + account.getName() + " with key " + processKey);
        if (processMap.containsKey(processKey)) {
            process = processMap.get(processKey);
            if (process.needsKilling()) {
                log.info(String.format("process needs killing for %s", account.getName()));
                process = processMap.remove(processKey);
                if(log.isDebugEnabled())
                log.debug(String.format("removing map entry, total: %s, removed: %s",processMap.size(),account.getLoginName()));
                if(process!=null) process.disconnect();
                return createProcess(account);  //we do this so we'll reconnect again in this thread, instead of waiting until x minutes later to reconnect
            }

        } else {

            return createProcess(account);

        }

        return process;

    }

    private static GoogleVoiceProcess createProcess(Account account) throws IOException{


            String processKey = account.getUser_id() + "_" + account.getName();
            log.info("creating new process for " + account.getName() + " with key " + processKey);
            long currentProcessStartTime = System.currentTimeMillis();
            GoogleVoiceProcess process = new GoogleVoiceProcess(currentProcessStartTime,account);
            process.connect();
            processMap.put(processKey,process);
            return process;

    }

}
