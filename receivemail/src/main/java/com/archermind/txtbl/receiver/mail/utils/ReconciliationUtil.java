package com.archermind.txtbl.receiver.mail.utils;

import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.reconsvc.ReconciliationRequest;
import com.archermind.txtbl.utils.SendQueueMessageClient;
import com.archermind.txtbl.utils.StringUtils;
import com.archermind.txtbl.utils.SysConfigManager;
import org.jboss.logging.Logger;

import java.io.Serializable;
import java.util.Set;

public class ReconciliationUtil {
    private static final Logger log = Logger.getLogger(ReconciliationUtil.class);

    public static void sendReconciliationRequest(Account account, Set<String> messageIds, String context) {
        try {
            Serializable message = new ReconciliationRequest(account.getId(), messageIds, context);
            if (log.isDebugEnabled())
                log.debug(String.format("sending request to reconciliation service, message=%s", message));

            String jndi = SysConfigManager.instance().getValue("reconsvc.queue.jndi");
            String url = SysConfigManager.instance().getValue("reconsvc.queue.url");

            if (StringUtils.isNotEmpty(jndi) && StringUtils.isNotEmpty(url)) {
                if (log.isDebugEnabled())
                    log.debug(String.format("sending reconciliation service message %s to %s on %s", message, jndi, url));

                SendQueueMessageClient.getInstance(jndi, url, jndi).send(message);
            } else {
                log.warn(String.format("reconciliation service is missing connection parameters. reconsvc.queue.jndi=%s, reconsvc.queue.url=%s", jndi, url));
            }

        } catch (Throwable t) {
            log.fatal("Unable to determine message id list to send to reconciliation service. Reconciliation will be skipped for " + context, t);
        }
    }

}
