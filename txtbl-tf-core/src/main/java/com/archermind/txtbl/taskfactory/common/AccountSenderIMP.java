package com.archermind.txtbl.taskfactory.common;

import java.util.Arrays;
import java.util.List;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.dal.business.impl.SysConfigService;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.SysConfig;
import com.archermind.txtbl.utils.SendQueueMessageClient;
import org.jboss.logging.Logger;

public class AccountSenderIMP implements AccountSender{
	private static Logger logger = Logger.getLogger(AccountSenderIMP.class);
	private static AccountSenderIMP accountSenderIMP=null;
	
	private static final String GROUP_ID_Prefix = "destination.target.group";
	private static final String GROUP_ID = "destination.protocol.group";
	private static final String defaultGroup = "default";
	private static final String JNDI_RECEIVER = "receivemail";

	private static SysConfigService syscs =null ;
	private static List<SysConfig> destinationGroup = null;
	private static String groupOfProtocols = null;
	private static String[][] groupsNameValue = null;
	private static String defaultDestination = "";

	public AccountSenderIMP(){
		loadParam();
	}
	
	public static AccountSenderIMP getInstance(){
		synchronized (AccountSenderIMP.class) {
			if (accountSenderIMP != null)
				return accountSenderIMP;
			else{
				accountSenderIMP=new AccountSenderIMP();
				return accountSenderIMP;
			}
		}
	}
	
	/**
	 * load parameters
	 */
	public void loadParam() {
		syscs=new SysConfigService();
        logger.info("loading parameters.......");
		loadSysConfig();
		getOtherPara();
	}

	/**
	 * get other needed parameters
	 */
	private void getOtherPara() {
		if (destinationGroup != null && groupOfProtocols != null) {
			String[] groups = groupOfProtocols.split(";");
			groupsNameValue = new String[groups.length][2];
			for (SysConfig s : destinationGroup) {
				if (s.getName().indexOf(defaultGroup) > -1) {
					String[] defaultArray = s.getValue().split(";");
					if (defaultArray.length == 3) {
						defaultDestination = defaultArray[1];
					} else {
						logger.error("error when get default destination!");
					}
					continue;
				}
				for (int i = 0; i < groups.length; i++) {
					if (Arrays.asList(groups[i].split(",")).contains(
							s.getName().split("\\.")[3])) {
						String[] destinationArray = s.getValue().split(";");
						if (destinationArray.length == 3) {
							groupsNameValue[i][0] = s.getName().split("\\.")[3];
							groupsNameValue[i][1] = destinationArray[1];
						} else {
							logger.error("error when get default destination!");
                        }
					}
				}
			}
		} else {
			groupsNameValue = null;
		}
	}

	/**
	 * load system configure from database
	 */
	private void loadSysConfig() {
		try {
			destinationGroup = syscs.getMailboxConfig(GROUP_ID_Prefix + "%");
            for (SysConfig aDestinationGroup : destinationGroup) {
                logger.info("destinationGroup [" + aDestinationGroup.getName() + ":" + aDestinationGroup.getValue() + "]");
            }
		} catch (DALException e) {
			logger.error(
					"load destination target failed,so can not send message!",
					e);
		}

		try {
			groupOfProtocols = syscs.getTaskFactoryParameter(GROUP_ID)
					.getValue();
			logger.info("groupOfProtocols [" + groupOfProtocols + "]");
		} catch (DALException e) {
			logger
					.error(
							"get receiver group failed, so conside all to default group",
							e);
		}
	}
	
	public void SendAccount(Account account)
    {
        logger.info(String.format("sending account=%s, uid=%s, protocol=%s", account.getName(), account.getUser_id(), account.getReceiveProtocolType()));

		String protocol = account.getReceiveProtocolType();

		int groupsNameValueId = getGroupsNameValueId(protocol);

		String destinationName;
		String destinationUrl;

		if (groupsNameValueId < 0)
        {
			destinationName = defaultGroup;
			destinationUrl = defaultDestination;
		}
        else
        {
			destinationName = groupsNameValue[groupsNameValueId][0];
			destinationUrl = groupsNameValue[groupsNameValueId][1];
		}
		try
        {
            logger.info(String.format("sending account to destination=%s, url=%s, account=%s, uid=%s", destinationName, destinationUrl, account.getName(), account.getUser_id()));

			SendQueueMessageClient.getInstance(destinationName, destinationUrl, JNDI_RECEIVER).send(account);
		}
        catch (Throwable e)
        {
            logger.fatal(String.format("failed to send the account to destination:=%s, url=%s, account=%s, uid=%s", destinationName, destinationUrl, account.getName(), account.getUser_id()), e);
		}
	}
	
	/**
	 * get the id of the receivers which this protocol will be send to
	 * 
	 * @param protocol
	 * @return
	 */
	private int getGroupsNameValueId(String protocol)
    {
		if (groupOfProtocols == null)
			return -1;
		String[] protocolArray = groupOfProtocols.split(";");

			for (int id = 0; id < protocolArray.length; id++)
            {
			    if (Arrays.asList(protocolArray[id].split(",")).contains(protocol))
                {
			    	return id;
		    	}
    		}

		return -1;
	}

}
