package com.archermind.txtbl.pushmail.cache;

import com.archermind.txtbl.pushmail.timer.BytePacket;
import com.archermind.txtbl.pushmail.utility.TopicInfo;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TopicMessageCache
{

    private static Logger log = Logger.getLogger(TopicMessageCache.class);

    private static Map<String, TopicInfo> topicCache = new ConcurrentHashMap<String, TopicInfo>();

    public static Map<String, TopicInfo> getCache()
    {
        return topicCache;
    }

    public static void addTopicInfo(TopicInfo topic)
    {
        String id = String.valueOf(new Date().getTime()) + topic.getUuid();

        if (topicCache.containsKey(id))
        {
            topicCache.remove(id);
        }

        topicCache.put(id, topic);

        log.info("add cache4Topic | eventid: " + id + "  uuid: " + topic.getUuid());
    }

    public static void removeAll(String key)
    {
        removeTopicInfo(key, BytePacket.RESPONSE);
        removeTopicInfo(key, BytePacket.NOTIFY);
    }

    public static void removeTopicInfo(String key, char packet)
    {
        Set<String> ls = topicCache.keySet();

        List<String> listKey = new ArrayList<String>();

        if (ls == null || ls.size() == 0)
        {
            return;
        }

        for (String item : ls) {
            TopicInfo topic = topicCache.get(item);

            if (topic != null) {
                if (topic.getUuid().equals(key) && topic.getUdpPacket()[0] == packet) {
                    listKey.add(item);

                }
            } else {
                topicCache.remove(item);
            }
        }

        for (String strKey : listKey) {
            topicCache.remove(strKey);

            log.info("remove cache4Topic | uuid: " + key + " eventid: " + strKey);
        }
    }


}
