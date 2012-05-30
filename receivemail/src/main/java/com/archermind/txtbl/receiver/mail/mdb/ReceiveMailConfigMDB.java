package com.archermind.txtbl.receiver.mail.mdb;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import com.archermind.txtbl.domain.SysConfig;
import com.archermind.txtbl.receiver.mail.config.ReceiverConfig;
import org.jboss.logging.Logger;

@MessageDriven(activationConfig = { @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "topic/reloadreceiveconfig") })
@TransactionManagement(TransactionManagementType.BEAN)
public class ReceiveMailConfigMDB implements MessageListener {

	private static final Logger log = Logger.getLogger(ReceiveMailConfigMDB.class);

	public void onMessage(Message msg) {
		try {
			log.info("[reload receiving config]");
			ObjectMessage objMsg = (ObjectMessage) msg;
			Object obj = objMsg.getObject();
			if (obj instanceof SysConfig) {
				new ReceiverConfig(true);
			} else {
                log.warn("log rebuilding is disabled, shouldn't be getting this message");
			}
		} catch (JMSException e) {
			log.error("onMessage/ReloadConfigMDB/JMSException: ", e);
		} catch (Exception e) {
			log.error("onMessage/ReloadConfigMDB/Exception: ", e);
		}
	}
}
