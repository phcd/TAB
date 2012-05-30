package com.archermind.txtbl.utils;

import com.archermind.txtbl.domain.Country;

public interface ISysConfigManager {
    String getValue(String key);
    String getValue(String key, String defaultValue);
    String getValue(String key, String defaultValue, Country country);
    String getValue(String key, Country country);
}
