package com.archermind.txtbl.pushmail.mdb;

import com.archermind.txtbl.dal.business.impl.UserService;
import com.archermind.txtbl.domain.User;
import com.archermind.txtbl.features.Features;
import com.archermind.txtbl.features.FeaturesPropertiesParser;
import com.archermind.txtbl.pushmail.cache.TopicMessageCache;
import com.archermind.txtbl.pushmail.cache.UUIDIPMappingCache;
import com.archermind.txtbl.pushmail.redis.NewPushManager;
import com.archermind.txtbl.pushmail.timer.BytePacket;
import com.archermind.txtbl.pushmail.timer.ProcessMonitor;
import com.archermind.txtbl.pushmail.udp.UDPProtocol;
import com.archermind.txtbl.pushmail.utility.Common;
import com.archermind.txtbl.pushmail.utility.TopicInfo;
import com.archermind.txtbl.pushmail.utility.UUIDIpMapping;
import com.archermind.txtbl.utils.SysConfigManager;
import com.archermind.txtbl.utils.UtilsTools;
import org.apache.commons.lang.StringUtils;
import org.jboss.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "topic/topicpushmail")})
@TransactionManagement(TransactionManagementType.BEAN)
public class TopicPushMailMDB implements MessageListener {

    private static final Logger log = Logger.getLogger(TopicPushMailMDB.class);

    private static final boolean useMasterForNewMails;
    private static final int messageRetrieveLimit;

    private static final NewPushManager pushManager;

    static {

        pushManager = new NewPushManager();
        useMasterForNewMails = Boolean.parseBoolean(SysConfigManager.instance().getValue("useMasterForNewMails", "false"));
        messageRetrieveLimit = Integer.valueOf(SysConfigManager.instance().getValue("xobniMessageRetrieveLimit", "100"));

        boolean shouldStartProcessMonitor = !Boolean.parseBoolean(System.getProperty("xobni"));
        log.info("shouldStartProcessMonitor : " + shouldStartProcessMonitor);
        if (shouldStartProcessMonitor) {
            ProcessMonitor monitor = new ProcessMonitor();
            monitor.start();
        }

    }

    public TopicPushMailMDB() {
        super();
        log.info("create TopicPushMailMDB...");
    }

    /**
     * @param msg
     * @see Message Drive Bean
     */
    public void onMessage(Message msg) {
        log.info("onMessage...");

        UtilsTools.logSystemStats(log);
        TopicInfo topicInfo = null;

        try {
            ObjectMessage objMsg = (ObjectMessage) msg;

            topicInfo = (TopicInfo) objMsg.getObject();

            log.info("topicInfo=" + String.valueOf(topicInfo));

            if (StringUtils.isNotEmpty(topicInfo.getUuid())) {
                handleMessage(topicInfo);
            }
        } catch (Throwable e) {
            log.fatal(String.format("unable to handle message %s", topicInfo), e);
        }
    }


    /* This method processes a new topic message from a receiver or the Web*/

    private void handleMessage(TopicInfo topicInfo) {
        if (log.isTraceEnabled())
            log.trace(String.format("handleMessage(topicInfo=%s)", String.valueOf(topicInfo)));

        String uuid = topicInfo.getUuid();
        String ip = topicInfo.getIp();
        String flag = topicInfo.getFlag();
        if (flag.equals(Common.RENEW) && ip != null) {
            // message resulting from relevant client activity (typically from Web node)

            if (UUIDIPMappingCache.getMappings() != null) {
                log.info(String.format("renewing uuid-ip mapping %s", String.valueOf(topicInfo)));

                UUIDIpMapping renewedMapping = new UUIDIpMapping();
                renewedMapping.setAvaible(true);
                renewedMapping.setIp(ip);
                renewedMapping.setUuid(uuid);

                UUIDIpMapping existingMapping = UUIDIPMappingCache.contains(uuid) ? UUIDIPMappingCache.getMapping(uuid) : null;

                if (existingMapping == null) {
                    existingMapping = new UUIDIpMapping();
                    existingMapping.setAvaible(true);
                    existingMapping.setTimes(0);
                    existingMapping.setIp(ip);
                    existingMapping.setUuid(uuid);

                    UUIDIPMappingCache.addOrUpdate(existingMapping);
                } else if (!existingMapping.isAvaible()) {
                    // Ticket #3572 - heartbeat required when client goes from unavailable to available state
                    renewedMapping.setTimes(1);
                    renewedMapping.setLastHeartbeat(new Date());

                    log.info(String.format("client %s at %s is now available, sending first heartbeat right away", uuid, ip));

                    // TODO: not sure why we need to pass in this value to the heartbeat
                    int port = Integer.parseInt(SysConfigManager.instance().getValue("UDPport"));

                    String pushDeviceTime = "";

                    try {
                        pushDeviceTime = SysConfigManager.instance().getValue("push.device.time");
                    } catch (Exception e) {
                        log.error("Unable to retrieve configuration property 'push.device.time', please make sure it is available in sys config", e);
                    }

                    UDPProtocol.topicNotify(ip, port, BytePacket.getHeartbeatByte(pushDeviceTime));

                    if (existingMapping.getNextNotification() != null) {
                        log.info(String.format("mapping became unavailable for uid=%s and we have an unsent new email notification", existingMapping.getUuid()));

                        UDPProtocol.topicNotify(ip, port, BytePacket.getNotifyByte());

                        existingMapping.setNextNotification(null);
                        existingMapping.setLastNotification(new Date());
                    }

                } else {
                    renewedMapping.setTimes(0);
                    renewedMapping.setLastHeartbeat(null);
                }

                renewedMapping.setLastNotification(existingMapping.getLastNotification());
                renewedMapping.setNextNotification(existingMapping.getNextNotification());

                UUIDIPMappingCache.addOrUpdate(renewedMapping);

                TopicMessageCache.removeAll(uuid);
            } else {
                log.info("uuid-ip mapping is empty");
            }

        } else if (flag.equals(Common.NOTIFY)) {
            // new message has arrived; this topic message typically added by receivers;
            try {
                if (pushManager.isNewClient(uuid)) {
                    pushManager.sendChar(uuid, BytePacket.NOTIFY);
                    return;
                }
            } catch (Exception e) {
                log.error("New client check and add failed: " + UtilsTools.getExceptionStackTrace(e));
            }

            boolean isCon = UUIDIPMappingCache.contains(uuid);
            if (log.isTraceEnabled())
                log.trace(String.format("isCon=" + (isCon ? "true" : "false")));

            if (isCon) {
                final UUIDIpMapping map = UUIDIPMappingCache.getMapping(uuid);

                log.info(String.format("is connected, mapping is %s", map));

                if (map.isAvaible()) {
                    sendNewEmailNotification(map, topicInfo);
                } else {
                    log.info(String.format("uuid-ip mapping for uid=%s is not available, this notification will need to be sent as soon as it becomes available again", uuid));

                    final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

                    map.setNextNotification(scheduler.schedule(new Runnable() {
                        public void run() {
                            // do nothing, we just need to record the fact that notification needs to be send
                            log.info(String.format("schedule new email notification for unavailable mapping uid=%s, ip=%s is triggered, sending notification", map.getUuid(), map.getIp()));

                            scheduler.shutdown();
                        }
                    }, 1, TimeUnit.MILLISECONDS));


                    /*
                      Unfortunately we have a bit of history here. Original implementation is all timer based. So there is a bit of
                      conflict. Basically what this does is this: while timer is retrying an event we may get new email for an unavailable
                      user. In this we will create a place holder schedule and no longer need to retry notification
                     */
                    TopicMessageCache.removeTopicInfo(map.getUuid(), BytePacket.NOTIFY);
                }
            } else {
                log.info("uuid-ip mapping does not exist for " + uuid);
            }
        }

    }

    public void sendNewEmailNotification(UUIDIpMapping map, TopicInfo topicInfo) {
        User user = new UserService().getPeekAccountIdByID(map.getUuid());
        if (user == null) {
            log.error(String.format("Could not retrieve user for uid %s", topicInfo.getUuid()));
            return;
        }


        String features = user.getComment();

        Features f = FeaturesPropertiesParser.parse(features);

        String pushFlag = f.get(Features.CRM_FEATURES_push_email);

        log.info(String.format("push flag is set to %s for uid %s", pushFlag, topicInfo.getUuid()));

        if ("on".equals(pushFlag)) {
            finalSendNotify(map, topicInfo);
        } else if ("off".equals(pushFlag)) {
            if (canNotify(map)) {
                finalSendNotify(map, topicInfo);
            }
        }
    }

    private boolean canNotify(final UUIDIpMapping map) {
        long intervalForOff = 0;

        try {
            intervalForOff = Long.parseLong(SysConfigManager.instance().getValue("intervalForPushOff")) * 1000l;
        } catch (Exception e) {
            log.error("Unable to retrieve configuration value for 'intervalForPushOff' please ensure they exists in sys config");
        }

        if (map.getLastNotification() == null) {
            log.info(String.format("last notification is not present for uid=%s, ip=%s, thus we can send new email notification", map.getUuid(), map.getIp()));
            //if we haven't sent notification yet - send one now
            return true;
        } else {
            long secondsSince = (System.currentTimeMillis() - map.getLastNotification().getTime()) / 1000l;

            if ((System.currentTimeMillis() - map.getLastNotification().getTime()) > intervalForOff) {
                log.info(String.format("last notification for uid=%s, ip=%s was sent a long time ago (%s seconds), thus we can send new email notification", secondsSince, map.getUuid(), map.getIp()));

                return true;
            } else if (map.getNextNotification() == null) {
                long timeToNextPossibleSend = (intervalForOff - (System.currentTimeMillis() - map.getLastNotification().getTime())) / 1000l;

                log.info(String.format("last notification for uid=%s, ip=%s was has been recently sent (%s seconds), thus we can't send new email notification now, but we will schedule it to run in (%s seconds)", secondsSince, map.getUuid(), map.getIp(), timeToNextPossibleSend));

                // this schedules the next notification

                if (timeToNextPossibleSend < 0l) {
                    timeToNextPossibleSend = 2l;
                }

                final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

                map.setNextNotification(scheduler.schedule(new Runnable() {
                    public void run() {
                        log.info(String.format("schedule new email notification for uid=%s, ip=%s is triggered, sending notification", map.getUuid(), map.getIp()));

                        int port = Integer.parseInt(SysConfigManager.instance().getValue("UDPport"));

                        if (map.isAvaible()) {
                            // if the client is still availalbe
                            UDPProtocol.topicNotify(map.getIp(), port, BytePacket.getNotifyByte());
                            map.setNextNotification(null);
                            map.setLastNotification(new Date());
                        }

                        scheduler.shutdown();
                    }
                }, timeToNextPossibleSend, TimeUnit.SECONDS));

                return false;
            } else {
                log.info(String.format("we already have a schedule notification, ignoring this new notification for uid=%s", map.getUuid()));

                return false;
            }
        }

    }

    private void finalSendNotify(UUIDIpMapping mapping, TopicInfo topicInfo) {
        log.info("sending new email received notification to " + topicInfo);

        topicInfo.setIp(mapping.getIp());
        topicInfo.setDate(new Date());
        topicInfo.setUdpPacket(BytePacket.getNotifyByte());
        TopicMessageCache.addTopicInfo(topicInfo);
        //log.info("add Cache4Topic | uuid: " + topicInfo.getUuid() + " ip:" + map.getIp());
        // for test
        int port = 0;
        try {
            port = Integer.parseInt(SysConfigManager.instance().getValue("UDPport"));
        } catch (Exception e) {
            log.error("Unable to retrieve configuration value for 'UDPport', please ensure it exists in sys config");
        }

        UDPProtocol.topicNotify(mapping.getIp(), port, topicInfo.getUdpPacket());

        if (topicInfo.getFlag().equals(Common.NOTIFY)) {
            // let's remember last time we sent a notification to this client
            mapping.setLastNotification(new Date());
        }

    }


}
