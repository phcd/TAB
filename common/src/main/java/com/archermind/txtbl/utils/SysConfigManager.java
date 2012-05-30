package com.archermind.txtbl.utils;

import com.archermind.txtbl.dal.business.ISysConfigService;
import com.archermind.txtbl.dal.business.impl.SysConfigService;
import com.archermind.txtbl.domain.Country;
import com.archermind.txtbl.domain.PartnerCode;
import com.archermind.txtbl.domain.SysConfig;
import org.jboss.logging.Logger;

import java.util.HashMap;

public class SysConfigManager implements ISysConfigManager {
    private static final Logger logger = Logger.getLogger(SysConfigManager.class);

	private ISysConfigService sysConfigNanager = new SysConfigService();

    private HashMap keyValue = new HashMap();

    private static SysConfigManager sysConfigManager = new SysConfigManager();

	public static SysConfigManager instance() {
        return sysConfigManager;
	}
    
	private SysConfigManager () {
		load();
	}
	
	public synchronized void load() {
		try {
			keyValue = sysConfigNanager.loadConfig();
		} catch (Throwable e) {
			logger.error("Unable to load configuration",e);
		}
	}
	
	public SysConfig get(String key) {
		return (SysConfig)keyValue.get(key);
	}
	
	public HashMap list() {
		return keyValue;
	}

    public String getValue(String key, Country country) {
        String value = getValue(getCountrySpecificKey(key, country));
        if(value !=  null){
            return value;
        }
        return getValue(key);
    }

    public String getValue(String key, PartnerCode partnerCode) {
        String value = getValue(getPartnerCodeSpecificKey(key, partnerCode));
        if(value !=  null){
            return value;
        }
        return getValue(key);
    }

    private String getPartnerCodeSpecificKey(String key, PartnerCode partnerCode) {
        if(partnerCode != null) {
            return partnerCode.toString() + "." + key;
        }
        return key;
    }

    public static String getCountrySpecificKey(String key, Country country) {
        if(country != null) {
            return country.toString() + "." + key;
        }
        return key;
    }

    public String getValue(String key) {
		SysConfig cfg = get( key) ;
		if(cfg!=null)
			return cfg.getValue() ;
    	return null;
	}

    public String getValue(String key, String defaultValue) {
        return defaultIfNull(getValue(key), defaultValue);
    }

    public String getValue(String key, String defaultValue, Country country) {
        return defaultIfNull(getValue(key, country), defaultValue);
    }

    public String getValue(String key, String defaultValue, PartnerCode partnerCode) {
        return defaultIfNull(getValue(key, partnerCode), defaultValue);
    }

    private String defaultIfNull(String value, String defaultValue) {
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        } else {
            return value;
        }
    }
}
