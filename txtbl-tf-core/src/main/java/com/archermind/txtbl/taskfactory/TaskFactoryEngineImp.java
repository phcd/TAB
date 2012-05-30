package com.archermind.txtbl.taskfactory;

import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;

import javax.jms.JMSException;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.dal.business.impl.SysConfigService;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.Server;
import com.archermind.txtbl.domain.SysConfig;
import com.archermind.txtbl.taskfactory.common.FactoryTools;
import com.archermind.txtbl.taskfactory.common.MacroDefine;
import com.archermind.txtbl.taskfactory.common.TFConfigManager;
import com.archermind.txtbl.taskfactory.msp.IsMSPAccount;
import com.archermind.txtbl.taskfactory.msp.IsMSPAccountIMP;
import com.archermind.txtbl.taskfactory.strategys.MasterLoopStrategy;
import com.archermind.txtbl.taskfactory.strategys.Pop3CollectionTimer;
import com.archermind.txtbl.taskfactory.subscribe.Subscribe;
import com.archermind.txtbl.taskfactory.loopnotice.LoopNoticer;
import com.archermind.txtbl.taskfactory.loopnotice.DestinationInstance;
import com.archermind.txtbl.utils.SendTopicMessageClient;
import org.jboss.logging.Logger;


public class TaskFactoryEngineImp implements TaskFactoryEngine {
    private static Logger logger = Logger.getLogger(TaskFactoryEngineImp.class);

    private static int instanceCount = 0;

    private String port = "1099";

	private static final String LOOP_NOTICER_KEY = "loop_noticer";

	private static final String SUBSCRIBER_NOTICER_KEY = "subscriber_noticer";

	private SysConfigService sysConfigService = new SysConfigService();

	private MasterLoopStrategy startStrategy = new MasterLoopStrategy();

	private Pop3CollectionTimer pop3CollectionTimer;

	private HashMap<String, ReceiveNoticer> receiverNoticers = new HashMap<String, ReceiveNoticer>();

	private static TaskFactoryEngineImp taskFactoryEngine = new TaskFactoryEngineImp();

	private IsMSPAccount isMSPAccount = new IsMSPAccountIMP(FactoryTools.getSubscribeArray());

    public static TaskFactoryEngineImp getInstance() {
        return taskFactoryEngine;
	}

	private TaskFactoryEngineImp() {
        instanceCount ++;
		pop3CollectionTimer = new Pop3CollectionTimer(this);
	}

	public void start() {
		logger.info("starting taskfactory engine..........................");
		if (!startStrategy.isMaster())
			return;
		initTaskFactory();
		afterInit();
		pop3CollectionTimer.startTimer();
		logger.info("taskfactory engine has started!!!!!!!!!");
	}

	private void afterInit() {
		try {
            String localIP = FactoryTools.getLocalIP();

			// update jms provider url and notify other modules
			SysConfig tmp = TFConfigManager.getInstance().getTaskfactoryURLConfig();
			tmp.setValue(localIP + ":" + port);

			sysConfigService.updateSystemParameters(tmp);
            
			logger.info("update " + MacroDefine.SYSCONFIG.CONST_TASKFACTORY_PROVIDER_URL_NAME + " with value:" + localIP + ":" + port);

			noticeJMS();
		} catch (DALException e) {
			logger.error("update flag into database failed, but still successfully initialize this task factory instance!!! ", e);
		} catch (UnknownHostException e) {
			logger.error("can not get local ip,so can not update the local ip the database!!!!!!!!!!!", e);
		}
	}

	/**
	 * notice the correlative module with jms
	 * 
	 * @throws JMSException
	 * @throws Exception
	 */
	private void noticeJMS() {
		logger.info("start to notice other moduls!");
		String webNoticeURLConfig = TFConfigManager.getInstance().getWebNoticeURLConfig();

		logger.info("webNoticeURLConfig:" + webNoticeURLConfig);
        String webNoticeJndiConfig = TFConfigManager.getInstance().getWebNoticeJndiConfig();

		logger.info("webNoticeJndiConfig:" + webNoticeJndiConfig);
		if (webNoticeURLConfig == null) {
			logger.error("the webNoticeURLConfig is null,so web module may not know this taskfactory!!!");
			return;
		}

		if (webNoticeJndiConfig == null) {
			logger.error("the webNoticeJndiConfig is null,so web module may not know this taskfactory!!!");
			return;
		}

		try {
			SendTopicMessageClient sender = SendTopicMessageClient.getInstance("", webNoticeURLConfig, webNoticeJndiConfig);
			sender.send(new SysConfig());
			logger.info("send message to " + webNoticeURLConfig + ":" + webNoticeJndiConfig);
		} catch (JMSException e) {
			logger.error("notice other moduls failed, so web or receivemail may not know this taskfactory!!! ", e);
		} catch (Exception e) {
			logger.error("notice other moduls failed, so web or receivemail may not know this taskfactory!!! ", e);
		}
	}

	private void initTaskFactory() {
		logger.info("init subscribe noticer.......");
		initSubscribeNoticer();

		logger.info("init receive noticer.......");
		initReceiveNoticer();

	}

	private void initReceiveNoticer() {
		LoopNoticer loopNoticer = (LoopNoticer)ReceiveNoticerFactory.create("loop");

        logger.info("storing loop noticer " + loopNoticer);

		receiverNoticers.put(LOOP_NOTICER_KEY, loopNoticer);

        logger.info(String.format("we (%s) have loop noticers: %s", this.hashCode(), receiverNoticers.containsKey(LOOP_NOTICER_KEY)));
        
		loopNoticer.start();
	}

	private void initSubscribeNoticer() {
		ReceiveNoticer subscribeNoticer = ReceiveNoticerFactory.create("subscribe");

		receiverNoticers.put(SUBSCRIBER_NOTICER_KEY, subscribeNoticer);
        
		subscribeNoticer.start();
	}

    public void updateAccountAndSend(Account account) {
        ReceiveNoticer noticer = updateAccount(account);

        if (noticer instanceof DestinationInstance) {
            ((DestinationInstance)noticer).sendAccount(account);
        }
    }

    //TODO - Paul - remove duplicate code
	public ReceiveNoticer updateAccount(Account account) {
		logger.info("starting to update account " + account);
        
		if (account.getReceiveProtocolType() == null || account.getReceiveProtocolType().equals("")) {
			logger.info("can not get protocoltype of account [" + account.getName() + "], so can not update the account!!!");
			return null;
		}

		if (isMSPAccount.isMSP(account)) {
			logger.info(String.format("account %s is an msp account", account.getUser_id()));

			ReceiveNoticer receiverNoticer = receiverNoticers.get(SUBSCRIBER_NOTICER_KEY);

			if (receiverNoticer != null) {
				return receiverNoticer.updateAccount(account);
			} else {
				logger.error("there is no subscribe noticer in map 'receiverNoticers'!!!!!!!");
			}
		} else {
			logger.info(String.format("account %s is NOT an msp account, receiver noticer present %s", account.getUser_id(), receiverNoticers.containsKey(LOOP_NOTICER_KEY)));

            ReceiveNoticer receiverNoticer = receiverNoticers.get(LOOP_NOTICER_KEY);

            if (receiverNoticer != null) {
				return receiverNoticer.updateAccount(account);
			} else {
				logger.error(String.format("there is no loop noticer in map 'receiverNoticers', we (%s) have %d task factory engine instances", this.hashCode(), instanceCount));
			}
		}

        return null;
	}

	public void updateConfig(SysConfig sysConfig) {
		Collection<ReceiveNoticer> clc = receiverNoticers.values();
		if (clc != null) {
            for (ReceiveNoticer aClc : clc) {
                aClc.updateConfig(sysConfig);
            }
		}
	}

	public void updateMSConfig(Server server) {
		Collection<ReceiveNoticer> clc = receiverNoticers.values();
		if (clc != null) {
            for (ReceiveNoticer aClc : clc) {
                aClc.updateMSConfig(server);
            }
		}
	}


	public void subscribeReceivedMail(Account account) {
		logger.info("subscribe account [" + account.getName() + "] has received an receivemail message!!");
		Subscribe subscribe = (Subscribe) receiverNoticers.get(SUBSCRIBER_NOTICER_KEY);
		subscribe.receivedMail(account);
	}

	public void pop3CollectionClear() {
		logger.info("pop3 collection clear!!!");

		for (ReceiveNoticer noticer : receiverNoticers.values()) {
			noticer.pop3CollectionClear();
		}
	}
}
