package com.archermind.txtbl.sender.mail.mdb;

import com.archermind.txtbl.domain.Server;
import com.archermind.txtbl.domain.SysConfig;
import com.archermind.txtbl.sender.mail.config.SenderConfig;
import org.jboss.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

@MessageDriven(activationConfig = {@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"), @ActivationConfigProperty(propertyName = "destination", propertyValue = "topic/reloadsendconfig")})
@TransactionManagement(TransactionManagementType.BEAN)
public class SendMailConfigListener implements MessageListener {

    private static final Logger log = Logger.getLogger(SendMailConfigListener.class);

    /**
     * @param msg
     * @see Message Drive Bean
     */
    public void onMessage(Message msg) {
        try {
            log.info("[reload sending config]");
            ObjectMessage objMsg = (ObjectMessage) msg;
            Object obj = objMsg.getObject();
            if (obj instanceof SysConfig) {
                SenderConfig.initSysConfig(true);
            } else if (obj instanceof Server) {
                SenderConfig.initServer((Server) obj);
            }
        } catch (JMSException e) {
            log.error("onMessage/ReloadConfigMDB/JMSException: ", e);
        } catch (Exception e) {
            log.error("onMessage/ReloadConfigMDB/Exception: ", e);
        }
    }
}
