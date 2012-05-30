package com.archermind.txtbl.taskfactory.jms;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import com.archermind.txtbl.domain.SysConfig;
import com.archermind.txtbl.taskfactory.TaskFactoryEngineImp;
import com.archermind.txtbl.taskfactory.common.MacroDefine;
import org.jboss.logging.Logger;

@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "topic/"
				+ MacroDefine.JNDI.CONST_RELOADTASKFACTORYCONFIG_JNDI + "") })
@TransactionManagement(TransactionManagementType.BEAN)
public class TaskFactoryConfigListener implements MessageListener {

	private static final Logger log = Logger.getLogger(TaskFactoryConfigListener.class);

	/**
	 * @param msg
	 * @see Message Drive Bean
	 */
	public void onMessage(Message msg) {
		ObjectMessage obj = (ObjectMessage) msg;
		try {
			SysConfig sysConfig = (SysConfig)obj.getObject();
			TaskFactoryEngineImp.getInstance().updateConfig(sysConfig);
		} catch (JMSException e) {
			log.error("error when onmessage!",e);
		}
	}
}
