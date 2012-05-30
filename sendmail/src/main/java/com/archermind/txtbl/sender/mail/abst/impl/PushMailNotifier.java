package com.archermind.txtbl.sender.mail.abst.impl;

import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.pushmail.utility.Common;
import com.archermind.txtbl.pushmail.utility.TopicInfo;
import com.archermind.txtbl.utils.SendTopicMessageClient;
import com.archermind.txtbl.utils.SysConfigManager;
import org.jboss.logging.Logger;

import java.util.Date;

public class PushMailNotifier {
    private static final Logger log = Logger.getLogger(PushMailNotifier.class);

    private static final SysConfigManager sysConfig = SysConfigManager.instance();



    public static String sendPushMailNotification(Account account, int total, String context) {
        if (log.isTraceEnabled())
            log.trace(String.format("sendPushMailNotification(account=%s, total=%d, context=%s)", account, total, context));
        String notifyName = sysConfig.getValue("pushmail.topic.jndi");
        if (log.isTraceEnabled())
            log.trace("notifyName=" + notifyName);

        String notifyIP = sysConfig.getValue("pushmail.topic.url", account.getCountry());
        if (log.isTraceEnabled())
            log.trace("notifyIP=" + notifyIP);

        try {
            if (total > 0) {
                // create a notify packet message, it will be sent to the client to inform of new messages

                TopicInfo tif = new TopicInfo(account.getUser_id(), Common.NOTIFY, new Date(), new byte[]{'Y', 0x00});
                if (log.isTraceEnabled())
                    log.trace("topicInfo=" + String.valueOf(tif));

                log.info(String.format("pushing notification of new messages for %s", context));
                log.debug(String.format("send topic info is %s %s",notifyName,notifyIP));
                SendTopicMessageClient.getInstance(notifyName, notifyIP, notifyName).send(tif);

                return String.format("[notify success total=%s] ", total);
            } else {
                log.info(String.format("not pushing notification of new messages to %s, total is %s", context, total));

                return String.format("[notify success total=%s] ", total);
            }

        }
        catch (Throwable e) {
            log.error(String.format("failed to push notification of %d new message to %s", total, context), e);

            return "[notify failure] ";
        }
    }
}
