package com.archermind.txtbl.receiver.mail.support;

import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.Protocol;
import com.archermind.txtbl.utils.SendQueueMessageClient;
import com.archermind.txtbl.utils.StringUtils;
import com.archermind.txtbl.utils.SysConfigManager;
import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.Map;

public class XobniFirstTimeHandler {
    private static final Logger log = Logger.getLogger(XobniFirstTimeHandler.class);

    public static final XobniFirstTimeHandler INSTANCE = new XobniFirstTimeHandler();

    private static final Map<Integer, Long> xobniFirstTimeFetchAccounts = new HashMap<Integer, Long>();

    private static final boolean xobniSeparateInitialFetch = Boolean.valueOf(SysConfigManager.instance().getValue("xobni.separate.initial.fetch", "false"));
    private static final String xobniInitialFetchUrl = SysConfigManager.instance().getValue("xobni.initial.fetch.url");
    private static final long xobniInitialFetchMaxTime = Long.valueOf(SysConfigManager.instance().getValue("xobni.initial.fetch.max.time", "60")) * 1000l * 60l;

    public boolean handleFirstTime(Account account, NewProviderSupport support, String context) {
        if (shouldSeparateInitialFetchForXobni(account)) {
            int id = account.getId();
            boolean inFirstTimeFetchMap = xobniFirstTimeFetchAccounts.containsKey(id);
            if (support.isFirstTime(account)) {
                if (inFirstTimeFetchMap) {
                    Long firstTimeFetchStartTime = xobniFirstTimeFetchAccounts.get(id);
                    if ((System.currentTimeMillis() - firstTimeFetchStartTime) > xobniInitialFetchMaxTime) {
                        log.info(String.format("initial fetch taken more than %s milliseconds for account %s - retrying", xobniInitialFetchMaxTime, context));
                        xobniFirstTimeFetchAccounts.remove(id);
                    } else {
                        log.info(String.format("initial fetch happening for account %s - nothing to do", context));
                        return true;
                    }

                }
                xobniFirstTimeFetchAccounts.put(id, System.currentTimeMillis());
                sendXobniAccountForFirstTimeFetch(account);
                return true;
            }

            removeFromXobniInitialFetchMap(account);
        }
        return false;
    }

    private boolean shouldSeparateInitialFetchForXobni(Account account) {
        return xobniSeparateInitialFetch
                && !StringUtils.isEmpty(xobniInitialFetchUrl)
                && (account.isXobniImapIdle() || account.isXobniOauthIdle() || (account.isXobniYahooImap() && !account.hasBeenDispatchedForFirstTimeFetch()));
    }

    public void removeFromXobniInitialFetchMap(Account account) {
        int id = account.getId();
        if (xobniFirstTimeFetchAccounts.containsKey(id)) {
            log.info("removing account from xobniFirstTimeFetchAccount if xobni account : " + account.getName() + " userId : " + account.getUser_id() + " id : " + id);
            xobniFirstTimeFetchAccounts.remove(id);
        }
    }

    private void sendXobniAccountForFirstTimeFetch(Account account) {
        account.setDispatchForFirstTimeFetch();
        account.setFolderName(account.getFolderNameToConnect());
        if (account.isXobniOauthIdle()) {
            account.setReceiveProtocolType(Protocol.XOBNI_OAUTH);
        }

        if (account.isXobniImapIdle()) {
            account.setReceiveProtocolType(Protocol.XOBNI_IMAP);
        }

        String destinationName = "xobniFirstTime";
        String receiverJndi = SysConfigManager.instance().getValue("recivemail-jndi");

        long start = System.nanoTime();

        log.info(String.format("%s (%s), send xobni account for first time fetch %s", destinationName, xobniInitialFetchUrl, account));

        try {
            SendQueueMessageClient.getInstance(destinationName, xobniInitialFetchUrl, receiverJndi).send(account);

            log.info(String.format("%s, sent copy of account uid=%s, name=%s to receiver url=%s, jndi=%s in %d millis", destinationName, account.getUser_id(), account.getName(), xobniInitialFetchUrl, receiverJndi, (System.nanoTime() - start) / 1000000));
        } catch (Throwable e) {
            log.error(String.format("%s, sending copy of account uid=%s, name=%s to receiver url=%s, jndi=%s failed", destinationName, account.getUser_id(), account.getName(), xobniInitialFetchUrl, receiverJndi), e);
        }
    }

}
