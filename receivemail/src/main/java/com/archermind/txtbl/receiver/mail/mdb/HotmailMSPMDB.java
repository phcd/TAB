package com.archermind.txtbl.receiver.mail.mdb;

import com.archermind.txtbl.receiver.mail.support.MspExpired;
import org.jboss.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;

@MessageDriven(activationConfig = {@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"), @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/msp")})
public class HotmailMSPMDB implements MessageListener {


    private static final Logger log = Logger.getLogger(HotmailMSPMDB.class);

    //TODO - Paul - can remove?
    public void onMessage(Message msg) {
        try {
            log.info("MessageListener: ");
            MspExpired.notifytoClient();
            // TODO
        } catch (Exception e) {
            log.error("Receive msp message failed", e);
        }
    }
}
