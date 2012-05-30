package com.archermind.txtbl.utils.xobni;

import com.archermind.txtbl.domain.XobniAccount;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;

public class XobniValidator {
    private static final Logger log = Logger.getLogger(XobniValidator.class);

    public static  List<String> validateSessionIdAndSyncUrl(XobniAccount xobniAccount) {
        ArrayList<String> errors = new ArrayList<String>();
        XobniResponse xobniResponse = XobniSyncUtil.INSTANCE.getExpectedDBVersion(xobniAccount);
        int returnCode = xobniResponse.getReturnCode();
        if(returnCode != XobniSyncUtil.XOBNI_200_OK) {
            log.error("valiation syncUrl and sessionId for " + xobniAccount.getName()  + " failed with error: " + returnCode);
        }

        if(returnCode == XobniSyncUtil.XOBNI_AUTH_SESSION_ID_ERROR_401 || returnCode == XobniSyncUtil.XOBNI_AUTH_SESSION_ID_ERROR_403) {
            errors.add("Invalid session Id");
        } else if(returnCode != XobniSyncUtil.XOBNI_200_OK) {
            errors.add("Invalid sync url");
        }
        return errors;
    }


}
