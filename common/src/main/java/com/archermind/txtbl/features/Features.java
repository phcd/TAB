package com.archermind.txtbl.features;

import java.util.*;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

public class Features {

	/**
	 * - <features> <texting>on</texting> <roaming>on</roaming> <accounts>5</accounts>
	 * <push_email>on</push_email> <attachments>on</attachments>
	 * <carrier>T-Mobile</carrier> </features>
	 */

	Map<String, String> prop = new HashMap<String, String>();

	public static String CRM_FEATURES_TEXTING = "texting";
	public static String CRM_FEATURES_accounts = "accounts";
	public static String CRM_FEATURES_push_email = "push_email";
	public static String CRM_FEATURES_attachments = "attachments";

	public void add(String key, String value) {
		prop.put(key, value);

	}

	public String get(String key) {
		return prop.get(key);

	}

	public Map<String, String> list() {
		 return  prop ;	
	}

	public String toKeyValuePair() {
		StringBuffer keyvalue = new StringBuffer();

        Iterator<String> keySet = prop.keySet().iterator();
        while (keySet.hasNext()) {
			String key = keySet.next();
			String value = prop.get(key);
			if (keySet.hasNext())
				keyvalue.append(key).append("=").append(value).append(",");
			else
				keyvalue.append(key).append("=").append(value);
		}

		return keyvalue.toString();
	}
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}
}
