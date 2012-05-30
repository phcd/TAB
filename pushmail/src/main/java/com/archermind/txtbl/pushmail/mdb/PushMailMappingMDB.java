package com.archermind.txtbl.pushmail.mdb;


import com.archermind.txtbl.pushmail.cache.UUIDIPMappingCache;
import com.archermind.txtbl.pushmail.utility.UUIDIpMapping;
import org.jboss.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;


@MessageDriven(activationConfig = {@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"), @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/pushmailmapping")})
@TransactionManagement(TransactionManagementType.BEAN)
public class PushMailMappingMDB implements MessageListener {

    private static final Logger log = Logger.getLogger(PushMailMappingMDB.class);
    

    public PushMailMappingMDB() {
        super();
        log.info("create PushMailMappingMDB...");
    }

    /**
     * @param msg
     * @see Message Drive Bean
     */
    public void onMessage(Message msg) {
        if (log.isTraceEnabled())
            log.trace(String.valueOf("onMessage..."));

        log.info("push mailling mapping has received a message");

        try {
            ObjectMessage objMsg = (ObjectMessage) msg;

            UUIDIpMapping mapping = (UUIDIpMapping) objMsg.getObject();
            if (log.isTraceEnabled())
                log.trace("mapping=" + String.valueOf(mapping));

            String uuid = mapping.getUuid();
            if (uuid != null) {

                log.info(String.format("adding mapping %s to mapping cache", mapping));

                UUIDIpMapping existingMapping = UUIDIPMappingCache.getMapping(uuid);

                if (existingMapping != null) {
                    existingMapping.setAvaible(true);
                    mapping.setTimes(0);
                } else {
                    mapping.setAvaible(true);
                    mapping.setTimes(0);
                    UUIDIPMappingCache.addOrUpdate(mapping);
                }
            }

        } catch (Throwable e) {
            log.error("Unable to process pushmail message " + msg, e);
        }

        log.info(String.format("we now have %d items in uuid-ip mapping cache", UUIDIPMappingCache.getMappings().size()));
    }


}
