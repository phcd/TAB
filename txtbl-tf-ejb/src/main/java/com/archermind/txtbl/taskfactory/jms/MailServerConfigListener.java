package com.archermind.txtbl.taskfactory.jms;

import com.archermind.txtbl.domain.Server;
import com.archermind.txtbl.taskfactory.TaskFactoryEngineImp;
import com.archermind.txtbl.taskfactory.common.MacroDefine;
import org.jboss.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

@MessageDriven(activationConfig = {@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/" + MacroDefine.JNDI.CONST_MAILSERVERCONFIG_JNDI + "")})
public class MailServerConfigListener implements MessageListener {

    private static final Logger logger = Logger.getLogger(MailServerConfigListener.class);

    public void onMessage(Message msg) {
        ObjectMessage obj = (ObjectMessage) msg;
        try {
            Server server = (Server) obj.getObject();
            TaskFactoryEngineImp.getInstance().updateMSConfig(server);
        } catch (JMSException e) {
            logger.error("error when onmessage!", e);
        }
    }
}
