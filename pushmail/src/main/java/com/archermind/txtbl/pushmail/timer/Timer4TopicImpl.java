package com.archermind.txtbl.pushmail.timer;

import com.archermind.txtbl.pushmail.cache.TopicMessageCache;
import com.archermind.txtbl.pushmail.cache.UUIDIPMappingCache;
import com.archermind.txtbl.pushmail.udp.UDPProtocol;
import com.archermind.txtbl.pushmail.utility.TopicInfo;
import com.archermind.txtbl.pushmail.utility.UUIDIpMapping;
import com.archermind.txtbl.utils.SysConfigManager;
import org.jboss.logging.Logger;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;

public class Timer4TopicImpl extends TimerTask {

    private static Logger log = Logger.getLogger(Timer4TopicImpl.class);

    public void run() {

        log.info("starting sent package response mointor");

        int port = 0;
        long interval = 0;
        int times = 0;
        // test
        try {
            port = Integer.parseInt(SysConfigManager.instance().getValue("UDPport"));
            if (log.isTraceEnabled())
                log.trace("UDPPort=" + port);

            interval = Long.parseLong(SysConfigManager.instance().getValue("intervalForRetry")) * 1000;
            if (log.isTraceEnabled())
                log.trace("interval=" + interval);

            times = Integer.parseInt(SysConfigManager.instance().getValue("timesForRetry"));
            if (log.isTraceEnabled())
                log.trace("timesForRetry=" + times);
        }
        catch (Exception e) {
            log.error("Unable to retrieve configuration please verify existence of the following numeric properties: UDPport, intervalForRetry and timesForRetry in sys config", e);
        }

        log.info(String.format("udpport=%s, interval=%s, times=%s", port, interval, times));

        Map<String, TopicInfo> cache = TopicMessageCache.getCache();
        if (log.isTraceEnabled())
            log.trace("cache=" + String.valueOf(cache));

        Set<String> ls = cache.keySet();
        if (log.isTraceEnabled())
            log.trace("ls=" + String.valueOf(ls));

        for (String key : ls) {
            if (cache.size() > 0 && cache.containsKey(key)) {
                TopicInfo topic = cache.get(key);
                if (log.isTraceEnabled())
                    log.trace("topic=" + String.valueOf(topic));

                long difference = new Date().getTime() - topic.getDate().getTime();
                if (log.isTraceEnabled())
                    log.trace("difference=" + difference);

                boolean isResponsePacket = topic.getUdpPacket()[0] == BytePacket.RESPONSE;
                if (log.isTraceEnabled())
                    log.trace("isResponsePacket=" + (isResponsePacket ? "true" : "false"));

                boolean isNotifyPacket = topic.getUdpPacket()[0] == BytePacket.NOTIFY;
                if (log.isTraceEnabled())
                    log.trace("isNotifyPacket=" + (isNotifyPacket ? "true" : "false"));

                if (isNotifyPacket && difference >= interval && difference <= 10 * interval) {
                    // only retrying for the notify packet
                    log.info(String.format("Retrying notify for event %s to %s", key, topic));

                    UDPProtocol.topicNotify(topic.getIp(), port, topic.getUdpPacket());
                }

                if ((isNotifyPacket && difference > times * interval) || (isResponsePacket && difference > interval)) {
                    if (isResponsePacket) {
                        log.info(String.format("have not received response packet %s answer, will not attempt to resend to %s", key, topic.getUuid()));
                        TopicMessageCache.removeTopicInfo(topic.getUuid(), BytePacket.RESPONSE);
                    } else {
                        log.info(String.format("exceeded maximum number of retries for notify packaget, will not attempt to resend again to %s", topic));
                        TopicMessageCache.removeTopicInfo(topic.getUuid(), BytePacket.NOTIFY);
                    }

                    UUIDIpMapping map = null;

                    if (UUIDIPMappingCache.contains(topic.getUuid())) {
                        map = UUIDIPMappingCache.getMapping(topic.getUuid());
                    }

                    if (map != null) {
                        if (map.isAvaible()) {
                            map.setLastHeartbeat(new Date());
                            map.setAvaible(false);

                            UUIDIPMappingCache.addOrUpdate(map);

                            log.info(String.format("making mapping unavailable %s", topic));
                        }
                    } else {
                        log.warn(String.format("Unable to find mapping for uuid=%s in cache", topic.getUuid()));
                    }

                }
            }

        }

    }

}
