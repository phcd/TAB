package com.archermind.txtbl.pushmail.cache;


import com.archermind.txtbl.pushmail.utility.UUIDIpMapping;
import org.jboss.logging.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UUIDIPMappingCache {
    private static Logger log = Logger.getLogger(UUIDIPMappingCache.class);

    private static Map<String, UUIDIpMapping> mappings = new ConcurrentHashMap<String, UUIDIpMapping>();

    public static Map<String, UUIDIpMapping> getMappings() {
        return mappings;
    }

    public static void addOrUpdate(UUIDIpMapping map) {
        if(log.isTraceEnabled())
            log.trace(String.format("addOrUpdate(map=%s)", String.valueOf(map)));
        if (mappings.containsKey(map.getUuid())) {
            UUIDIpMapping mapping = mappings.remove(map.getUuid());

            cancelNotifications(mapping);
        }

        mappings.put(map.getUuid(), map);

    }

    private static void cancelNotifications(UUIDIpMapping mapping) {
        if (mapping.getNextNotification() != null) {
            try {
                mapping.getNextNotification().cancel(true);
                mapping.setNextNotification(null);
            }
            catch (Throwable t) {
                log.fatal(String.format("Unable to cancel next notification for mapping %s", mapping), t);
            }
        }
    }

    public static UUIDIpMapping getMapping(String key) {
        if (log.isTraceEnabled())
            log.trace(String.format("getMapping(key=%s)", key));
        return mappings.get(key);
    }

    public static boolean contains(String key) {
        if (log.isTraceEnabled())
            log.trace(String.format("contains(key=%s)", key));
        return mappings.containsKey(key);
    }


}
