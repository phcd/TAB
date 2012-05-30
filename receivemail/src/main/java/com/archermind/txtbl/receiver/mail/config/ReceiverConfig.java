package com.archermind.txtbl.receiver.mail.config;

import com.archermind.txtbl.TxtblConfiguration;
import com.archermind.txtbl.domain.Country;
import com.archermind.txtbl.utils.SysConfigManager;
import com.archermind.txtbl.utils.StringUtils;
import org.jboss.logging.Logger;

import java.util.HashMap;

public class ReceiverConfig extends TxtblConfiguration {

	private static HashMap<String, String> hashMap = new HashMap<String, String>();

	private static Logger log = Logger.getLogger(ReceiverConfig.class);

	public ReceiverConfig(boolean reloadFlag) {
		init(reloadFlag);
	}

	/**
	 */
	public void init(boolean reloadFlag) {
		try {
			SysConfigManager sysConfig = SysConfigManager.instance();
			if (reloadFlag) {
				sysConfig.load();
			}
			String temp = sysConfig.getValue("receiver.mail.body.size");
			if (StringUtils.isEmpty(temp)) {
				log.error("init/ReceiverConfig/ConfigException: receiver.mail.body.size is null or \"\"");
			} else {
				hashMap.put("bodySize", temp.trim());
			}
			temp = sysConfig.getValue("receiver.mail.attach.type");
			if (StringUtils.isEmpty(temp)) {
				log.error("init/ReceiverConfig/ConfigException: receiver.mail.attach.type is null or \"\"");
			} else {
				hashMap.put("attachType", temp.trim());
			}

			temp = sysConfig.getValue("receiver.mail.attach.size");
			if (StringUtils.isEmpty(temp)) {
				log.error("init/ReceiverConfig/ConfigException: receiver.mail.attach.size is null or \"\"");
			} else {
				hashMap.put("attachSize", temp.trim());
			}

			temp = sysConfig.getValue("receiver.mail.subject.size");
			if (StringUtils.isEmpty(temp)) {
				log.error("init/ReceiverConfig/ConfigException: receiver.mail.subject.size is null or \"\"");
			} else {
				hashMap.put("subjectSize", temp.trim());
			}

			temp = sysConfig.getValue("receiver.mail.image.height");
			if (StringUtils.isEmpty(temp)) {
				log.error("init/ReceiverConfig/ConfigException: receiver.mail.image.height is null or \"\"");
			} else {
				hashMap.put("imageHeight", temp.trim());
			}

			temp = sysConfig.getValue("receiver.mail.image.width");
			if (StringUtils.isEmpty(temp)) {
				log.error("init/ReceiverConfig/ConfigException: receiver.mail.image.width is null or \"\"");
			} else {
				hashMap.put("imageWidth", temp.trim());
			}
			
			temp = sysConfig.getValue("receiver.search.new.mail.size");
			if (StringUtils.isEmpty(temp)) {
				log.error("init/ReceiverConfig/ConfigException: receiver.search.new.mail.size is null or \"\"");
			} else {
				hashMap.put("searchSize", temp.trim());
			}			
			
			temp = sysConfig.getValue("receiver.mail.filter.size");
			if (StringUtils.isEmpty(temp)) {
				log.error("init/ReceiverConfig/ConfigException: receiver.mail.filter.size is null or \"\"");
			} else {
				hashMap.put("filterSize", temp.trim());
			}		
			
			
			temp = sysConfig.getValue("receiver.mail.parse.error.replace.body");
			if (StringUtils.isEmpty(temp)) {
				log.error("init/ReceiverConfig/ConfigException: receiver.mail.parse.error.replace.body is null or \"\"");
			} else {
				hashMap.put("replaceBody", temp.trim());
			}

			/** ********************************************************************** */
			temp = sysConfig.getValue("receiver.mail.thread.timeout");
			if (StringUtils.isEmpty(temp)) {
				log.error("init/ReceiverConfig/ConfigException: receiver.mail.thread.timeout is null or \"\"");
			} else {
				hashMap.put("shortTimeOut", temp.trim());
			}

			temp = sysConfig.getValue("receiver.large.mail.size");
			if (StringUtils.isEmpty(temp)) {
				log.error("init/ReceiverConfig/ConfigException: receiver.large.mail.size is null or \"\"");
			} else {
				hashMap.put("largeMailSize", temp.trim());
			}
			
			temp = sysConfig.getValue("receiver.mail.thread.max.pool.size");
			if (StringUtils.isEmpty(temp)) {
				log.error("init/ReceiverConfig/ConfigException: receiver.mail.thread.max.pool.size is null or \"\"");
			} else {
				hashMap.put("maxPoolSize", temp.trim());
			}
			
			temp = sysConfig.getValue("receiver.mail.thread.min.pool.size");
			if (StringUtils.isEmpty(temp)) {
				log.error("init/ReceiverConfig/ConfigException: receiver.mail.thread.min.pool.size is null or \"\"");
			} else {
				hashMap.put("minPoolSize", temp.trim());
			}
			
			temp = sysConfig.getValue("receiver.mail.thread.create.size");
			if (StringUtils.isEmpty(temp)) {
				log.error("init/ReceiverConfig/ConfigException: receiver.mail.thread.create.size is null or \"\"");
			} else {
				hashMap.put("createSize", temp.trim());
			}
			
			temp = sysConfig.getValue("receiver.mail.thread.keep.alive.time");
			if (StringUtils.isEmpty(temp)) {
				log.error("init/ReceiverConfig/ConfigException: receiver.mail.thread.keep.alive.time is null or \"\"");
			} else {
				hashMap.put("keepAliveTime", temp.trim());
			}
			
			temp = sysConfig.getValue("receiver.mail.repeat.login.switch");
			if (StringUtils.isEmpty(temp)) {
				log.error("init/ReceiverConfig/ConfigException: receiver.mail.repeat.login.switch is null or \"\"");
			} else {
				hashMap.put("switch", temp.trim());
			}	
			
			temp = sysConfig.getValue("receiver.mail.version");
			if (StringUtils.isEmpty(temp)) {
				log.error("init/ReceiverConfig/ConfigException: receiver.mail.repeat.login.switch is null or \"\"");
			} else {
				hashMap.put("version", temp.trim());
			}
			
			if ("US".equalsIgnoreCase(ReceiverConfig.getProp("version"))) {
				temp = sysConfig.getValue("pushmail.topic.jndi");
				if (StringUtils.isEmpty(temp)) {
					log.error("init/ReceiverConfig/ConfigException: pushmail.topic.jndi is null or \"\"");
				} else {
					hashMap.put("notifyName", temp.trim());
				}

                for (Country country : Country.values())
                {
                    temp = sysConfig.getValue("pushmail.topic.url", country);
                    if(!StringUtils.isEmpty(temp)) {
                        hashMap.put(country.toString() + "." + "notifyIP", temp.trim());
                    } else {
                        log.error("init/ReceiverConfig/ConfigException: pushmail.topic.url is null or \"\"");                                                
                    }
                }
			}
			
			/** ********************************************************************** */
			
			temp = sysConfig.getValue("first.waring.time.msp");
			if (StringUtils.isEmpty(temp)) {
				log.error("init/ReceiverConfig/ConfigException: first.waring.time.msp is null or \"\"");
			} else {
				hashMap.put("first.waring.time.msp", temp.trim());
			}
			temp = sysConfig.getValue("first.waring.subject.msp");
			if (StringUtils.isEmpty(temp)) {
				log.error("init/ReceiverConfig/ConfigException: first.waring.subject.msp is null or \"\"");
			} else {
				hashMap.put("first.waring.subject.msp", temp.trim());
			}
			temp = sysConfig.getValue("first.waring.content.msp");
			if (StringUtils.isEmpty(temp)) {
				log.error("init/ReceiverConfig/ConfigException: first.waring.content.msp is null or \"\"");
			} else {
				hashMap.put("first.waring.content.msp", temp.trim());
			}
			
			
			temp = sysConfig.getValue("second.waring.time.msp");
			if (StringUtils.isEmpty(temp)) {
				log.error("init/ReceiverConfig/ConfigException: second.waring.time.msp is null or \"\"");
			} else {
				hashMap.put("second.waring.time.msp", temp.trim());
			}
			temp = sysConfig.getValue("second.waring.subject.msp");
			if (StringUtils.isEmpty(temp)) {
				log.error("init/ReceiverConfig/ConfigException: second.waring.subject.msp is null or \"\"");
			} else {
				hashMap.put("second.waring.subject.msp", temp.trim());
			}
			temp = sysConfig.getValue("second.waring.content.msp");
			if (StringUtils.isEmpty(temp)) {
				log.error("init/ReceiverConfig/ConfigException: second.waring.content.msp is null or \"\"");
			} else {
				hashMap.put("second.waring.content.msp", temp.trim());
			}
			
			
			temp = sysConfig.getValue("third.waring.time.msp");
			if (StringUtils.isEmpty(temp)) {
				log.error("init/ReceiverConfig/ConfigException: third.waring.time.msp is null or \"\"");
			} else {
				hashMap.put("third.waring.time.msp", temp.trim());
			}
			temp = sysConfig.getValue("third.waring.subject.msp");
			if (StringUtils.isEmpty(temp)) {
				log.error("init/ReceiverConfig/ConfigException: third.waring.subject.msp is null or \"\"");
			} else {
				hashMap.put("third.waring.subject.msp", temp.trim());
			}
			temp = sysConfig.getValue("third.waring.content.msp");
			if (StringUtils.isEmpty(temp)) {
				log.error("init/ReceiverConfig/ConfigException: third.waring.content.msp is null or \"\"");
			} else {
				hashMap.put("third.waring.content.msp", temp.trim());
			}
			
			temp = sysConfig.getValue("after.invalid.msp.subject");
			if (StringUtils.isEmpty(temp)) {
				log.error("init/ReceiverConfig/ConfigException: after.invalid.msp.subject is null or \"\"");
			} else {
				hashMap.put("after.invalid.msp.subject", temp.trim());
			}
			temp = sysConfig.getValue("after.invalid.msp.content");
			if (StringUtils.isEmpty(temp)) {
				log.error("init/ReceiverConfig/ConfigException: after.invalid.msp.content is null or \"\"");
			} else {
				hashMap.put("after.invalid.msp.content", temp.trim());
			}
			
			temp = sysConfig.getValue("subscribe.url");
			if (StringUtils.isEmpty(temp)) {
				log.error("init/ReceiverConfig/ConfigException: subscribe.url is null or \"\"");
			} else {
				hashMap.put("subscribe.url", temp.trim());
			}
			/** ********************************************************************** */
			log.info(" [initialization receiving configuration success]");
		} catch (Exception e) {
			log.error("init/ReceiverConfig/Exception: ", e);
		}
	}

	public static String getProp(String key) {
		return hashMap.get(key);
	}

    public static String getProp(String key, Country country)
    {
        String countrySpecificKey = SysConfigManager.getCountrySpecificKey(key, country);
        String value = hashMap.get(countrySpecificKey);
        if(!StringUtils.isEmpty(value))
        {
            return value;
        }
        return getProp(key);
    }
}
