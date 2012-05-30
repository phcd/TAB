package com.archermind.txtbl.validate.mailbox.abst.impl;

import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.validate.mailbox.abst.Validate;
import com.techventus.server.voice.Voice;
import org.jboss.logging.Logger;

public class GoogleVoiceValidate extends Validate {

    private static final Logger log = Logger.getLogger(GoogleVoiceValidate.class);

    public void validate(Account account) throws Exception{

        String googleVoiceLogin = account.getLoginName();
        if(!googleVoiceLogin.contains("@gmail.com")) {
            googleVoiceLogin=account.getLoginName() + "@gmail.com";
        }
        if(log.isInfoEnabled())
         log.info("handling validation request for [" + googleVoiceLogin + "]");

        //Throws IOException if auth fails
        new Voice(googleVoiceLogin,account.getPassword());

    }

}
