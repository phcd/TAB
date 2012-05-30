package com.archermind.txtbl.validate.mailbox.abst.impl;

import com.archermind.txtbl.domain.Account;
import org.junit.Before;
import org.junit.Test;


public class ActiveSyncValidateTest{

    private Account account;
    private Account intermedia;

    @Before
    public void setup(){

        account = new Account();
        account.setName("mcmulkin@co.pueblo.co.us");
        account.setLoginName("mcmulkin@pueblo.cnty");
        account.setPassword("Ladybug6");
        account.setReceiveHost("owa.co.pueblo.co.us/Microsoft-Server-ActiveSync");
        account.setReceivePort("443");
        account.setReceiveTs("ssl");

        intermedia = new Account();
        intermedia.setName("peek_intermedia@peek.hostpilot.com");
        intermedia.setLoginName("peek_intermedia@peek.hostpilot.com");
        intermedia.setPassword("3xch@ng3");
        intermedia.setReceiveHost("EAST.EXCH022.serverdata.net/Microsoft-Server-ActiveSync");
        intermedia.setReceivePort("443");
        intermedia.setReceiveTs("ssl");


    }

    @Test
    public void testActiveSync(){


        ActiveSyncValidate validator = new ActiveSyncValidate();
        try{
            validator.validate(intermedia);
        }catch(Exception e){
            System.out.println("EXCEPTION - " + e.getMessage());
        }



    }

}
