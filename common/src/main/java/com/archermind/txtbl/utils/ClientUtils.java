package com.archermind.txtbl.utils;

import com.archermind.txtbl.dal.business.impl.UserService;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.Device;
import com.archermind.txtbl.domain.PartnerCode;
import com.archermind.txtbl.domain.User;
import org.jboss.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: nick
 * Date: 11-10-31
 * Time: 5:40 PM
 */
public class ClientUtils {
    private static UserService us = new UserService();
    private static final Logger logger = Logger.getLogger(ClientUtils.class);

    private static String processAttachmentClients = null;
    private static String peekClientSwStrings = "Ex:01.08.03, Ex:01.08.04, Ex:01.08.08, Ex:01.08.10, Ex:01.08.11, Ex:01.09.10.5, Ex:01.09.14, Ex:01.09.15, Ex:01.09.15_LED_build, " +
            "Ex:01.09.16, Ex:01.09.18, Ex:01.09.18-091207, Ex:01.09.18-100830, Ex:01.09.19, Ex:01.09.20, Ex:01.09.x1, Ex:01.09.xx, Ex:01.10.00, Ex:01.10.50-BF1117, " +
            "Ex:01.20.00, Ex:01.30.00, Ex:01.50.00, Ex:1.09.15_0702, Ex:11_XF_20090323, Ex:15_AM_20090420, Ex:V1.09.15_0608E, Alpine, TwitterPeek";
    
    static {
        processAttachmentClients =  SysConfigManager.instance().getValue("attachment.no-raw.clientsw.version", peekClientSwStrings);
    }

    public static boolean doesAccountSupportRawAttachments(Account account, PartnerCode partnerCode) {
        Device device = us.getDeviceByUserId(account.getUser_id());
        return doesDeviceSupportRawAttachments(device, partnerCode);
    }

    public static boolean doesDeviceSupportRawAttachments(Device device, PartnerCode code) {
        if (code == null)
            return false;

        String clientSw = device.getClientsw();

        if (StringUtils.isVersionSupported(clientSw, processAttachmentClients))
            return false;

        logger.debug(String.format("Checking RAW attach support for partnerCode=%s, clientSw=%s", code.toString(), clientSw));
        if (clientSw == null || "".equals(clientSw)) {
            if (code.equals(PartnerCode.qcom)
                    || code.equals(PartnerCode.peekint)
                    || code.equals(PartnerCode.sprd)
                    || code.equals(PartnerCode.mmax))
                return true;
        } else if (code.equals(PartnerCode.sprd)) {
            return false;
        } else if (code.equals(PartnerCode.mmax) && !clientSw.equals("MTK:01.60.00")) {
            return true;
        } else if (code.equals(PartnerCode.sym))
            return true;
        return false;
    }

    public static boolean doesDeviceSupportRawAttachments(Device device, String emailAddress) {
        Account account = us.getAccount(emailAddress);
        return doesDeviceSupportRawAttachments(device, account.getPartnerCode());
    }

    public static boolean isDeviceUsingRestApi(User user, Device device) {
        return isDeviceUsingRestApi(user.getPartnerCode(), device.getClientsw());
    }

    public static boolean isDeviceUsingRestApi(PartnerCode code, String clientSw) {
        // if device has no client string and is partnerCode 'qcom', 'peekint', 'sprd', 'mmax', it is using rest
        // if mmax and has client string MMAX_Android:01.01.00 it's rest

        if (code == null)
            return false;

        logger.debug(String.format("Checking API usage for partnerCode=%s, clientSw=%s", code.toString(), clientSw));
        if (clientSw == null || "".equals(clientSw)) {
            if (code.equals(PartnerCode.qcom)
                    || code.equals(PartnerCode.peekint)
                    || code.equals(PartnerCode.sprd)
                    || code.equals(PartnerCode.mmax))
                return true;
        } else if (code.equals(PartnerCode.sprd)) {
            return true;
        } else if (code.equals(PartnerCode.mmax) && !clientSw.startsWith("MTK")) {
            return true;
        }

        return false;
    }

    public static boolean doesDeviceAcceptUTF8(PartnerCode code, String clientSw) {
        // if device has no client string and is partnerCode 'qcom', 'peekint', 'sprd', 'mmax', it is using rest
        // if mmax and has client string MMAX_Android:01.01.00 it's rest

        if (code == null)
            return false;

        logger.debug(String.format("Checking UTF8 support for partnerCode=%s, clientSw=%s", code.toString(), clientSw));
        if (clientSw == null || "".equals(clientSw)) {
            if (code.equals(PartnerCode.qcom)
                    || code.equals(PartnerCode.peekint)
                    || code.equals(PartnerCode.sprd)
                    || code.equals(PartnerCode.mmax))
                return true;
        } else if (code.equals(PartnerCode.sprd)) {
            return true;
        } else if (code.equals(PartnerCode.mmax)) {  //&& !clientSw.startsWith("MTK")) { // removing this for now
            return true;
        } else if (code.equals(PartnerCode.sym))
            return true;
        else if (code.equals(PartnerCode.nex))
            return true;

        return false;
    }

    public static boolean isDeviceAPeek(Device device) {
        if (device == null)
            return false;

        if (device.getClientsw() == null)
            return false;

        return StringUtils.isVersionSupported(device.getClientsw(), peekClientSwStrings)
                || ((device.getClientsw().equals("") && (device.getDeviceCode().startsWith("35444")))); // this is for old peeks with no client string -- check for IMEI instead
    }
}
