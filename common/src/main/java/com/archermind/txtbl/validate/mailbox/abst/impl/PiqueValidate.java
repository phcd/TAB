package com.archermind.txtbl.validate.mailbox.abst.impl;

import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.validate.mailbox.abst.Validate;
import org.jboss.logging.Logger;

public class PiqueValidate extends Validate {

    private static final Logger log = Logger.getLogger(PiqueValidate.class);

    public void validate(Account account) throws Exception{

        String piqueLogin = account.getLoginName();
        if(!piqueLogin.contains("@gmail.com")) {
            piqueLogin=account.getLoginName() + "@gmail.com";
        }
        if(log.isInfoEnabled())
         log.info("handling validation request for [" + piqueLogin+ "]");


    }

}
