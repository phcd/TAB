package com.archermind.txtbl.features;

import com.archermind.txtbl.utils.StringUtils;

public class  FeaturesPropertiesParser  {

	private final static Features DEFAULT_FEATURES = FeaturesPropertiesParser.parse("texting=off,roaming=off,accounts=3,push_email=on,attachments=on,carrier=T-Mobile,texting=on,twitter=on");

	public static Features parse(String formatStr) {
        Features features = new Features();

        if (StringUtils.isEmpty(formatStr)) {
            return DEFAULT_FEATURES;
        } else {
            String[] item = formatStr.split(",");
            for (String anItem : item) {
                String[] keyvalue = anItem.split("=");
                features.add(keyvalue[0], keyvalue[1]);
            }
            return features;
        }
	}
}
