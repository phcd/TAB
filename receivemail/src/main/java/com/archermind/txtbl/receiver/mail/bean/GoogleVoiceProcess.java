package com.archermind.txtbl.receiver.mail.bean;

import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.utils.SysConfigManager;
import com.techventus.server.voice.Voice;
import com.archermind.txtbl.utils.StringUtils;
import org.jboss.logging.Logger;

import java.io.IOException;

public class GoogleVoiceProcess {

    private static final Logger log = Logger.getLogger(GoogleVoiceProcess.class);

    private long latestProcessStartTime;
    private Voice voice=null;
    Account account=null;


    public GoogleVoiceProcess(long latestProcessStartTime, Account account) {
        this.latestProcessStartTime = latestProcessStartTime;
        this.account = account;
    }


    //Connects to a googlevoice instance
    public boolean connect() throws IOException {

        String context = String.format("[%s] account=%s, uid=%s, email=%s, login=%s", this.hashCode(), account.getId(), account.getUser_id(), account.getName(),account.getLoginName());

        if (voice==null){
            voice = new Voice(account.getLoginName() + "@gmail.com", account.getPassword(),"GoogleVoiceJava",true,Voice.GOOGLE);
        }
        log.info(String.format("attempting to connect to google voice for %s", context));
        voice.isLoggedIn();

        return true;
    }
    

    public void disconnect() {
        voice = null;

    }

    public boolean needsKilling(){
        return (((System.currentTimeMillis() - latestProcessStartTime)) >
                1000l*60*getGoogleVoiceConnectionTTLInMinutes());
    }


    private long getGoogleVoiceConnectionTTLInMinutes() {
        String googleVoiceConnectionTtl = SysConfigManager.instance().getValue("googleVoiceConnectionTtl");

        if (StringUtils.isEmpty(googleVoiceConnectionTtl)) {
            log.warn("default connection time to live (googleVoiceConnectionTtl) is not defined in configuration using default of 60 minutes");
            return 60l;
        } else {
            return Long.valueOf(googleVoiceConnectionTtl);
        }

    }


    public Voice getVoice() {
        return voice;
    }
}
