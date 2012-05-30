package com.archermind.txtbl.taskfactory.subscribe;

import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.Server;
import com.archermind.txtbl.domain.SysConfig;
import com.archermind.txtbl.taskfactory.ReceiveNoticer;
import org.jboss.logging.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class SubscribeNoticer extends ReceiveNoticer implements Subscribe {
    private static Logger logger = Logger.getLogger(SubscribeNoticer.class);

    private static HashMap<String, SubscriberImp> instanceStub = new HashMap<String, SubscriberImp>();
    private static HashMap<String, String[]> protocolsStub = new HashMap<String, String[]>();

    private static long defaultInterval = 1;
    private static long defaultThreadSleep = 10;
    private String[] subscribeArray = null;

    @Override
    public void start() {
        if (logger.isDebugEnabled())
            logger.debug("start()");

        String subscribesValue = null;

        String[] subscribeProtocols = null;

        long lReloadInterval;
        long lThreadSleepMinutes;

        if (logger.isTraceEnabled())
            logger.trace("subscribeArray=" + org.apache.commons.lang.StringUtils.join(subscribeArray, ","));

        if (subscribeArray != null) {
            for (int i = 0; i < subscribeArray.length; i++) {
                logger.info("start subscribe [" + subscribeArray[i] + "]");

                subscribesValue = subscribeArray[i];

                String[] subscrivesValueArray = subscribesValue.split(":");

                if (logger.isTraceEnabled())
                    logger.trace("subscrivesValueArray=" + org.apache.commons.lang.StringUtils.join(subscrivesValueArray, ","));

                String subscribeProtocolsStr = null;
                if (subscrivesValueArray.length > 0) {
                    subscribeProtocolsStr = subscrivesValueArray[0];
                }

                if (subscribeProtocolsStr == null || subscribeProtocolsStr.equals("")) {
                    logger.error("there is no subscribe protocols in database of this subscribe,so can not start this subscribe service!!!");
                    continue;
                }
                subscribeProtocols = subscribeProtocolsStr.split(",");

                String interval = null;
                if (subscrivesValueArray.length > 1) {
                    interval = subscrivesValueArray[1];
                }

                if (interval == null || interval.equals("")) {
                    logger.info("there is no interval time in the database of this subscribe,so use the default interval time [" + defaultInterval + "]");
                    lReloadInterval = defaultInterval;
                } else {
                    lReloadInterval = Long.parseLong(interval) * 1000l * 60l;
                }

                String threadSleep = null;
                if (subscrivesValueArray.length > 2) {
                    threadSleep = subscrivesValueArray[2];
                }

                if (null == threadSleep || threadSleep.equals("")) {
                    logger.info("there is no thread sleep time in the database for this subscribe protocol, so use the default interval time [" + defaultThreadSleep + "]");
                    lThreadSleepMinutes = defaultThreadSleep;
                } else {
                    lThreadSleepMinutes = Long.parseLong(threadSleep);
                }


                SubscriberImp subscriberImp = new SubscriberImp(subscribeProtocols, lReloadInterval, lThreadSleepMinutes);

                instanceStub.put(i + "", subscriberImp);

                protocolsStub.put(i + "", subscribeProtocols);

                subscriberImp.start();
            }
        } else {
            logger.error("there no subscribe in the database!!!!!");
        }
    }

    @Override
    public ReceiveNoticer updateAccount(Account account) {
        if (logger.isDebugEnabled())
            logger.info(String.format("updateAccount(account=%s)", account));

        boolean isSuccess = false;
        if (protocolsStub == null) {
            logger.error("protocolsStub is null!maybe taskfactory has not started!!!");
            return null;
        }

        String[] name = protocolsStub.keySet().toArray(new String[]{});
        for (int i = 0; i < name.length; i++) {
            if (Arrays.asList(protocolsStub.get(name[i])).contains(account.getReceiveProtocolType())) {
                ReceiveNoticer noticer = instanceStub.get(name[i]);

                noticer.updateAccount(account);

                return noticer;
            }
        }

        return null;
    }

    @Override
    public void updateConfig(SysConfig sysConfig) {

    }

    @Override
    public void updateMSConfig(Server server) {

    }

    public void receivedMail(Account account) {
        logger.info("get receive mail message of account[" + account.getName()
                + "]");
        String[] name = protocolsStub.keySet().toArray(new String[]{});
        for (int i = 0; i < name.length; i++) {
            if (Arrays.asList(protocolsStub.get(name[i])).contains(
                    account.getReceiveProtocolType())) {
                instanceStub.get(name[i]).receivedMail(account);
            }
        }
    }

    @Override
    public void pop3CollectionClear() {
        logger.info("pop3 collection clear!!!");
        Iterator<Entry<String, SubscriberImp>> it = instanceStub.entrySet()
                .iterator();
        while (it.hasNext()) {
            it.next().getValue().pop3CollectionClear();
        }
    }

    public String[] getSubscribeArray() {
        return subscribeArray;
    }

    public void setSubscribeArray(String[] subscribeArray) {
        this.subscribeArray = subscribeArray;
    }

}
