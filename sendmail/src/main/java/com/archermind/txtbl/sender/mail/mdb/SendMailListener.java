package com.archermind.txtbl.sender.mail.mdb;

import java.util.*;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.naming.InitialContext;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

import com.archermind.txtbl.dal.business.IEMailSentService;
import com.archermind.txtbl.dal.business.impl.EmailSentService;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.EmailPojo;
import com.archermind.txtbl.sender.mail.abst.Operator;
import com.archermind.txtbl.sender.mail.config.SenderConfig;
import com.archermind.txtbl.sender.mail.relation.FailureNotifier;

@MessageDriven(activationConfig = {@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"), @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/sendmail")})
@TransactionManagement(TransactionManagementType.BEAN)
public class SendMailListener implements MessageListener {


    private static final Logger log = Logger.getLogger(SendMailListener.class);
    private static final Logger logger = Logger.getLogger("snmpTrap");


	private static final HashMap<String, Integer> hashMap = new HashMap<String, Integer>();

	protected static final Hashtable<String, Integer> hashtable = new Hashtable<String, Integer>();

	private static BeanFactory beanFactory = null;

	/** initialize spring bean factory */
	static {
		try {
			hashtable.put("total", 0);
			hashtable.put("failure", 0);
			SenderConfig.initServer(null);
			SenderConfig.initSysConfig(false);
			beanFactory = new XmlBeanFactory(new ClassPathResource("com/archermind/txtbl/sender/mail/xml/sendOperatorsBeanFactory.xml"));
			log.info("[initialization sending config and beanFactory]");
		} catch (Exception e) {
			log.error("static module of SendMailListener class throw Exception: ", e);
		}
	}

	/**
	 * @param msg
	 * @see Message Drive Bean(send mail)
	 */

	public void onMessage(Message msg) {
		String getFailureId = null;
        String mailId = null;
		try {
			ObjectMessage objMsg = (ObjectMessage) msg;
			mailId = (String) objMsg.getObject();
			log.info("[sending mailID] [" + mailId + "]");
			if (hashtable.get("total") >= Integer.parseInt(SenderConfig.getProp("senderEndTotal"))) {
				hashtable.put("total", 0);
				hashtable.put("failure", 0);
			}
			hashtable.put("total", hashtable.get("total") + mailId.split(",+").length);
			if (hashtable.get("total") >= Integer.parseInt(SenderConfig.getProp("senderBeginTotal"))) {
				if (((float) hashtable.get("failure").intValue() / hashtable.get("total")) > Float.parseFloat(SenderConfig.getProp("senderLimit"))) {
					log.error("sender of mail alarm");
				}
			}
			HashMap<String, List<EmailPojo>> hashMapMail = sortByMailbox(mailId);
			try {
				getFailureId = sendMail(hashMapMail, false);

                // apply retry logic only for non-twitter accounts
                if (!isTwitterAccount(hashMapMail) && !isPiqueAccount(hashMapMail)) {
                    if (getFailureId != null) {
                        alarmStat(getFailureId, false);
                        getFailureId = sendMail(hashMapMail, true);
                    }
                    if (getFailureId != null) {
                        alarmStat(getFailureId, true);
                    }
                }

			} catch (Exception e) {
				log.error("onMessage/SendMailListener/Exception: [filter getFailureId failure] [" + getFailureId + "]", e);
			}
		} catch (Exception e) {
			log.error("onMessage/SendMailListener/Exception: [initialization failure] [" + mailId + "]", e);
		}

	}

	/**
	 * @param mailId
	 * @return HashMap<String, List<EmailPojo>>
	 * @throws Exception
	 */
	private HashMap<String, List<EmailPojo>> sortByMailbox(String mailId) {
		HashMap<String, List<EmailPojo>> hashMapMail = new HashMap<String, List<EmailPojo>>();
        for (String tempId : mailId.split(",+")) {
            try {
                IEMailSentService emailSentService = new EmailSentService();
                EmailPojo emailPojo = emailSentService.getEmailPojo(tempId);
                List<EmailPojo> tempList;
				if ("9".equals(emailPojo.getEmail().getStatus())) {
					SenderConfig.initLocalSMTPConfig(emailPojo.getAccount());
					if (hashMapMail.containsKey("localsmtp")) {
						hashMapMail.get("localsmtp").add(emailPojo);
					} else {
						tempList = new ArrayList<EmailPojo>();
						tempList.add(emailPojo);
						hashMapMail.put("localsmtp", tempList);
					}
				}else{
					SenderConfig.initAccount(emailPojo.getAccount());
					if (hashMapMail.containsKey(emailPojo.getAccount().getName())) {
						hashMapMail.get(emailPojo.getAccount().getName()).add(emailPojo);
					} else {
						tempList = new ArrayList<EmailPojo>();
						tempList.add(emailPojo);
						hashMapMail.put(emailPojo.getAccount().getName(), tempList);
					}
				}
			} catch (Exception e) {
				log.error("sortByMailbox/SendMailListener/Exception: [lose mailID] [" + tempId + "]", e);
			}
		}
		return hashMapMail;
	}

	/**
	 * @param localFlag
	 * @return String
	 */
	private String sendMail(HashMap<String, List<EmailPojo>> hashMapMail, boolean localFlag) {
		String failrueId = null;
		String accountName = null;
        String getfailrueId;
        try {
            for (String key : hashMapMail.keySet()) {
                List<EmailPojo> mailPojoList = hashMapMail.get(key);
                Account account = mailPojoList.get(0).getAccount();
                accountName = account.getName();
                Operator operator;
                if (localFlag) {
                    operator = (Operator) beanFactory.getBean("localsmtp");
                } else {
                    if (beanFactory.containsBean(account.getSendProtocolType())) {
                        operator = (Operator) beanFactory.getBean(account.getSendProtocolType());
                    } else {
                        operator = (Operator) beanFactory.getBean("localsmtp");
                    }
                }
                getfailrueId = operator.sendMail(mailPojoList);
                if (failrueId == null) {
                    if (getfailrueId != null) {
                        failrueId = getfailrueId;
                    }
                } else {
                    if (getfailrueId != null) {
                        failrueId += "," + getfailrueId;
                    }
                }
            }
		} catch (Exception e) {
			log.error("sendMail/SendMailListener/Exception: " + "[" + accountName + "]", e);
		}
		return failrueId;
	}

    /**
     * Returns true if the sendProtocolType is set to "twitter" for the account.
     * @param hashMapMail
     * @return true if this is a twitter account, false otherwise
     */
    private boolean isTwitterAccount(HashMap<String, List<EmailPojo>> hashMapMail) {
        return "twitter".equals(getSendProtocolType(hashMapMail));
    }

    /**
     * Returns true if the sendProtocolType is set to "twitter" for the account.
     * @param hashMapMail
     * @return true if this is a twitter account, false otherwise
     */
    private boolean isPiqueAccount(HashMap<String, List<EmailPojo>> hashMapMail) {
        return "pique".equals(getSendProtocolType(hashMapMail));
    }

    /**
     * Returns the protocol that will be used to send the email.
     *
     * @param hashMapMail
     * @return
     */
    private String getSendProtocolType(HashMap<String, List<EmailPojo>> hashMapMail) {
        String sendProtocolType = null;

        for (String key : hashMapMail.keySet()) {
            List<EmailPojo> mailPojoList = hashMapMail.get(key);
            Account account = mailPojoList.get(0).getAccount();
            sendProtocolType = account.getSendProtocolType();

            break;
        }

        return sendProtocolType;
    }

	/**
	 */
	protected void alarm(String loseMailId) {

		if (hashtable.get("total") >= Integer.parseInt(SenderConfig.getProp("senderEndTotal"))) {
			hashtable.put("total", 0);
			hashtable.put("failure", 0);
		}
		hashtable.put("failure", hashtable.get("failure") + loseMailId.split(",+").length);
		if (hashtable.get("total") >= Integer.parseInt(SenderConfig.getProp("senderBeginTotal"))) {
			if (((float) hashtable.get("failure").intValue() / hashtable.get("total")) > Float.parseFloat(SenderConfig.getProp("senderLimit"))) {
				log.error("sender of mail alarm !");
				logger.fatal("sender of mail alarm !");
			} else if (((float) hashtable.get("failure").intValue() / hashtable.get("total")) > 0.01) {
				logger.warn("sender of mail alarm !");
			}
		}
	}

	/**
	 * @param getFailureId
	 */
	public void alarmStat(String getFailureId, boolean callBackFlag) {

		String failureId = null;
		String loseMailId = null;
		for (String tempId : getFailureId.split(",+")) {
			if (hashMap.containsKey(tempId)) {
				if (hashMap.get(tempId) + 1 >= Integer.parseInt(SenderConfig.getProp("sendFailureTimes"))) {
					loseMailId = loseMailId == null ? tempId : loseMailId + "," + tempId;
					hashMap.remove(tempId);
					if (callBackFlag) {
						FailureNotifier.notifier(tempId);
						log.error("[lose mailID]" + " [" + tempId + "]");
					}
				} else {
					failureId = failureId == null ? tempId : failureId + "," + tempId;
					hashMap.put(tempId, hashMap.get(tempId) + 1);
				}
			} else {
				failureId = failureId == null ? tempId : failureId + "," + tempId;
				hashMap.put(tempId, 1);
			}
		}
		if (failureId != null && callBackFlag) {
			log.info("[wait for sending again]" + " [" + failureId + "]");
			callBack(failureId);
		}
		if (loseMailId != null) {
			alarm(loseMailId);
		}
	}

	/**
	 * @param failedMailId
	 */
	public void callBack(String failedMailId) {
		try {
			Properties props = new Properties();
			props.setProperty("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
			props.setProperty("java.naming.provider.url", SenderConfig.getProp("callbackUrl"));
			props.setProperty("java.naming.factory.url.pkgs", "org.jboss.naming");
			InitialContext ctx = new InitialContext(props);
			QueueConnectionFactory factory = (QueueConnectionFactory) ctx.lookup("ConnectionFactory");
			QueueConnection queueConnection = factory.createQueueConnection();
			QueueSession queueSession = queueConnection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
			Queue queue = (Queue) ctx.lookup("queue/" + SenderConfig.getProp("callbackJNDI"));
			ObjectMessage objMsg = queueSession.createObjectMessage();
			objMsg.setObject(failedMailId);
			QueueSender queueSender = queueSession.createSender(queue);
			queueSender.send(objMsg);
			queueSession.close();
			queueConnection.close();
		} catch (Exception e) {
			log.error("sendMail/SendMailListener/Exception: [" + failedMailId + "]", e);
		}
	}
}
