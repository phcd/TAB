package com.archermind.txtbl.sender.mail.config;

import java.util.HashMap;
import java.util.List;

import com.archermind.txtbl.TxtblConfiguration;
import com.archermind.txtbl.dal.business.impl.EmailServerService;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.Country;
import com.archermind.txtbl.domain.Server;
import com.archermind.txtbl.utils.StringUtils;
import com.archermind.txtbl.utils.SysConfigManager;
import com.archermind.txtbl.utils.UtilsTools;
import org.jboss.logging.Logger;

public class SenderConfig extends TxtblConfiguration {

    private static final Logger log = Logger.getLogger(SenderConfig.class);

	private static HashMap<String, String> hashMap = new HashMap<String, String>();
	
	private static HashMap<Integer, Server> mailServer = new HashMap<Integer, Server>();

	private static Server localMailServer = null;
	
	public static void initSysConfig(boolean reloadFlag) {
		try {
			SysConfigManager sysConfig = SysConfigManager.instance();
			if (reloadFlag) {
				sysConfig.load();
			}
			String temp = sysConfig.getValue("sendFailureTimes");
			if (temp == null || "".equals(temp.trim())) {
				log.error("init/SenderConfig/ConfigException: sendFailureTimes is null or \"\"");
			} else {
				hashMap.put("sendFailureTimes", temp.trim());
			}
			temp = sysConfig.getValue("senderBeginTotal");
			if (temp == null || "".equals(temp.trim())) {
				log.error("init/SenderConfig/ConfigException: senderBeginTotal is null or \"\"");
			} else {
				hashMap.put("senderBeginTotal", temp.trim());
			}

			temp = sysConfig.getValue("senderEndTotal");
			if (temp == null || "".equals(temp.trim())) {
				log.error("init/SenderConfig/ConfigException: senderEndTotal is null or \"\"");
			} else {
				hashMap.put("senderEndTotal", temp.trim());
			}

			temp = sysConfig.getValue("senderLimit");
			if (temp == null || "".equals(temp.trim())) {
				log.error("init/SenderConfig/ConfigException: senderLimit is null or \"\"");
			} else {
				hashMap.put("senderLimit", temp.trim());
			}

			// temp = sysConfig.getValue("callbackUrl");
			// if (temp == null || "".equals(temp.trim())) {
			// log.error("init/SenderConfig/ConfigException: callbackUrl is null or \"\"");
			// } else {
			hashMap.put("callbackUrl", "127.0.0.1:1099");
			// }

			temp = sysConfig.getValue("sending-jndi");
			if (temp == null || "".equals(temp.trim())) {
				log.error("init/SenderConfig/ConfigException: sending-jndi is null or \"\"");
			} else {
				hashMap.put("callbackJNDI", temp.trim());
			}

			temp = sysConfig.getValue("loginName");
			if (temp == null || "".equals(temp.trim())) {
				log.warn("init/SenderConfig/ConfigException: loginName.provider.url is null or \"\"");
			} else {
				hashMap.put("loginName", temp.trim());
			}
			temp = sysConfig.getValue("loginPassword");
			if (temp == null || "".equals(temp.trim())) {
				log.warn("init/SenderConfig/ConfigException: loginPassword is null or \"\"");
			} else {
				hashMap.put("loginPassword", temp.trim());
			}
			temp = sysConfig.getValue("mailFrom");
			if (temp == null || "".equals(temp.trim())) {
				log.warn("init/SenderConfig/ConfigException: mailFrom is null or \"\"");
			} else {
				hashMap.put("mailFrom", temp.trim());
			}

            if ("US".equalsIgnoreCase(SenderConfig.getProp("version"))) {
				temp = sysConfig.getValue("pushmail.topic.jndi");
				if (temp == null || "".equals(temp.trim())) {
					log.error("init/SenderConfig/ConfigException: pushmail.topic.jndi is null or \"\"");
				} else {
					hashMap.put("notifyName", temp.trim());
				}

                for (Country country : Country.values())
                {
                    temp = sysConfig.getValue("pushmail.topic.url", country);
                    if(!StringUtils.isEmpty(temp)) {
                        hashMap.put(country.toString() + "." + "notifyIP", temp.trim());
                    } else {
                        log.error("init/SenderConfig/ConfigException: pushmail.topic.url is null or \"\"");
                    }
                }
			}


		} catch (Exception e) {
			log.error("init/SenderConfig/Exception: ", e);
		}
	}


	public static void initServer(Server updateServer) {
		try {
			if (updateServer == null) {
				EmailServerService emailServerService = new EmailServerService();
				List<Server> list = emailServerService.getSentServers(null);
				localMailServer = emailServerService.getSentServerConfig("localsmtp.com");
				for (Server server : list) {
					mailServer.put(server.getSent_id(), server);
				}
			} else {
				mailServer.put(updateServer.getSent_id(), updateServer);
				if ("localsmtp.com".equals(updateServer.getName())) {
					localMailServer = updateServer;
				}
			}
		} catch (Exception e) {
			log.error("initServer/SenderConfig/Exception: ", e);
		}
	}
	public static String getProp(String key) {
		return hashMap.get(key);
	}
	
	public static void initAccount(Account account) {
		if (!mailServer.containsKey(account.getSent_id())) {
			initServer(null);
		}
		Server server = mailServer.get(account.getSent_id());

        if (server == null)
        {
            log.error(String.format("Unable to identify a server for uid=%s, account=%s, sent_id=%s", account.getUser_id(), account.getName(), account.getSent_id()));
        }
        else
        {
            UtilsTools.mapSendServerDetails(account, server);
        }
	}
	
	public static void initLocalSMTPConfig(Account account) {
        UtilsTools.mapServerDetails(account, localMailServer);
		String loginName = getProp("loginName");
		String password = getProp("loginPassword");
		if (loginName != null && !"".equals(loginName.trim()) && password != null && !"".equals(password.trim())) {
			account.setNeedAuth("1");
			account.setLoginName(loginName);
			account.setPassword(password);
		} else {
			account.setNeedAuth("0");
		}
	}

}
