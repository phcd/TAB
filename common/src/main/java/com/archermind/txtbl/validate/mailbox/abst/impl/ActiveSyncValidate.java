package com.archermind.txtbl.validate.mailbox.abst.impl;

import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.validate.mailbox.abst.Validate;
import com.zynku.sync.activesync.context.ActiveSyncContext;
import com.zynku.sync.activesync.control.ActiveSyncController;
import org.jboss.logging.Logger;

import java.net.URL;

public class ActiveSyncValidate extends Validate {
    private static final Logger logger = Logger.getLogger(ActiveSyncValidate.class);

    public void validate(Account account) throws Exception {
        if (logger.isTraceEnabled())
            logger.trace(String.format("validate(account=%s)", String.valueOf(account)));
        ActiveSyncContext activeSyncContext = new ActiveSyncContext();
        activeSyncContext.setDeviceId("Appl9C808MH40JW");
        activeSyncContext.setDeviceType("PEEK");
        logger.info(String.format("Doing ActiveSync validation for %s, %s, %s, %s", account.getLoginName(), account.getPassword(), account.getReceiveTs(), account.getReceiveHost()));

        String userName = account.getLoginName();
        int backslashPosition = userName.indexOf("\\");
        if (-1 != backslashPosition) {
            activeSyncContext.setUserName(userName.substring(backslashPosition+1, userName.length()));
            activeSyncContext.setDomain(userName.substring(0, backslashPosition));
        } else {
            activeSyncContext.setUserName(userName);
        }

        //activeSyncContext.setUserName(account.getLoginName());
        activeSyncContext.setPassword(account.getPassword());
        activeSyncContext.setServerURL(new URL(("ssl".equals(account.getReceiveTs()) ? "https" : "http") + "://" + account.getReceiveHost()));

        final ActiveSyncController controller = new ActiveSyncController(activeSyncContext, 230000);
        String policyKey = controller.getPolicyKey();
        logger.debug(String.format("policyKey=%s", (null != policyKey ? policyKey : "null")));
        if(null != policyKey)
            activeSyncContext.setPolicyKey(policyKey);

        try {
            controller.initialFolderSync();

            logger.info(String.format("initialFolderSync succeeded account %s serverUrl %s", account.getLoginName(), activeSyncContext.getServerURL()));
        }
        catch (Throwable t) {
            logger.warn(String.format("unable to validate account %s due to %s", account.getLoginName(), t.getMessage()));
            t.printStackTrace(System.out);

            throw new Exception("Error during ActiveSync validation + " + t.getMessage());
        }
    }

}
