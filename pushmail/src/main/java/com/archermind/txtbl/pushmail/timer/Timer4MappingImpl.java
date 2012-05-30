package com.archermind.txtbl.pushmail.timer;

import com.archermind.txtbl.pushmail.cache.TopicMessageCache;
import com.archermind.txtbl.pushmail.cache.UUIDIPMappingCache;
import com.archermind.txtbl.pushmail.udp.UDPProtocol;
import com.archermind.txtbl.pushmail.utility.Common;
import com.archermind.txtbl.pushmail.utility.TopicInfo;
import com.archermind.txtbl.pushmail.utility.UUIDIpMapping;
import com.archermind.txtbl.utils.SysConfigManager;
import org.jboss.logging.Logger;

import java.util.Date;
import java.util.Map;
import java.util.TimerTask;
import java.util.Iterator;

public class Timer4MappingImpl extends TimerTask
{

    private static Logger log = Logger.getLogger(Timer4MappingImpl.class);

    public void run()
    {
        try
        {
            long start = System.currentTimeMillis();

            log.info("uuid-ip mapping timed task is starting...");

            int port = 7;

            try
            {
                port = Integer.parseInt(SysConfigManager.instance().getValue("UDPport"));
            }
            catch (Exception e)
            {
                log.error("Unable to retrieve configuration for 'UDPport', please make sure it exists in sys config", e);
            }

            Map<String, UUIDIpMapping> map = UUIDIPMappingCache.getMappings();

            //Set<String> uuids = new HashSet(map.keySet());

            log.debug(String.format("we have %d uuid-ip mappings to consider", map.size()));

            for (Iterator<String> iter = map.keySet().iterator(); iter.hasNext();)
            {
                String uid = iter.next();
                
                UUIDIpMapping mapping = map.get(uid);

                log.debug(String.format("processing mapping %s", mapping));

                if (mapping.isAvaible())
                {
                    poll(mapping, port);
                }
                else if (!mapping.isAvaible())
                {
                    // check overtimes mapping and delete it
                    log.info(String.format("uuid-ip mapping [%s:%s] is not available!", mapping.getUuid(), mapping.getIp()));

                    long overtime = mapping.getLastHeartbeat().getTime();
                    long nowtime = new Date().getTime();

                    // test
                    long discard = 0;

                    try
                    {
                        discard = Long.parseLong(SysConfigManager.instance().getValue("intervalToDiscard"));
                    }
                    catch (Exception e)
                    {
                        log.error("Unable to retrieve configuration for 'intervalToDiscard', please make sure it exists in sys config", e);
                    }

                    if (nowtime - overtime >= discard)
                    {
                        log.info("discarding mapping " + mapping);

                        //map.remove(uid);
                        iter.remove();
                    }

                }
            }

            log.info(String.format("topic cache size is: %s, uuid-ip mapping cache size is: %s, task completed in %d millis", TopicMessageCache.getCache().size(), UUIDIPMappingCache.getMappings().size(), System.currentTimeMillis() - start));
        }
        catch (Throwable t)
        {
            log.fatal("timed task failed", t);
        }
    }

    private void poll(UUIDIpMapping mapping, int port)
    {
        if(log.isTraceEnabled())
            log.trace(String.format("poll(mapping=%s, port=%s)", String.valueOf(mapping), String.valueOf(port)));
        
        int i = mapping.getTimes();

        int heartBeatCount = 4;

        try
        {
            heartBeatCount = Integer.parseInt(SysConfigManager.instance().getValue("push.heartbeat.count"));
        }
        catch (Exception e)
        {
            log.error("Unable to retrieve configuration for 'pushmail.heartbeat.count', please make sure it exists in sys config", e);
        }

        if ((i + 1) % heartBeatCount == 0)
        {
            // add to cache2

            TopicInfo topicInfo = new TopicInfo();
            topicInfo.setDate(new Date());
            topicInfo.setFlag(Common.TIMER);
            topicInfo.setIp(mapping.getIp());
            topicInfo.setUuid(mapping.getUuid());
            topicInfo.setUdpPacket(BytePacket.getResponseByte());
            TopicMessageCache.addTopicInfo(topicInfo);

            UDPProtocol.topicNotify(topicInfo.getIp(), port, BytePacket.getResponseByte());

            log.info(String.format("ping count %d, sending response byte to %s", (i + 1), mapping));

            UUIDIPMappingCache.addOrUpdate(mapping);

            mapping.setTimes(0);
        }
        else
        {

            mapping.setTimes(mapping.getTimes() + 1);

            UUIDIPMappingCache.addOrUpdate(mapping);

            String sInterval = "";

            try
            {
                sInterval = SysConfigManager.instance().getValue("push.device.time");
            }
            catch (Exception e)
            {
                log.error("Unable to retrieve configuration property 'push.device.time', please make sure it is available in sys config", e);
            }

            log.info(String.format("ping count %d, sending heartbeat packet to %s", (i + 1), mapping));

            long start = System.currentTimeMillis();

            UDPProtocol.topicNotify(mapping.getIp(), port, BytePacket.getHeartbeatByte(sInterval));

            log.info(String.format("ping count %d, heartbeat packet is sent to %s in %s millis", (i + 1), mapping, System.currentTimeMillis() - start));

        }


    }

}
