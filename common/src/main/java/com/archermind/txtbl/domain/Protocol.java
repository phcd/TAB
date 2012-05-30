package com.archermind.txtbl.domain;

public class Protocol {
    public static String EXCHANGE = "newexchange";
    public static String IMAP_IDLE = "newimapidle";
    public static String GMAIL_POP3 = "newgmailpop3";
    public static String GMAIL_IMAP = "newgmailimap";
    public static String YAHOO_IMAP = "newyahooimap";
    public static String POP3 = "newpop3";
    public static String IMAP = "newimap";
    public static String MSP_NEW = "newmsp";
    public static String SMTP = "smtp";
    public static String GOOGLE_VOICE = "googlevoice";
    public static String ACTIVE_SYNC = "activesync";

    public static String XOBNI_IMAP_IDLE = "xobniimapidle";
    public static String XOBNI_OAUTH_IDLE = "xobnioauthidle";
    public static String XOBNI_YAHOO_IMAP = "xobniyahooimap";
    public static String XOBNI_IMAP = "xobniimap";
    public static String XOBNI_OAUTH = "xobnioauth";
    public static String XOBNI_POP3 = "xobnipop3";
    public static String XOBNI_MSP = "xobnimsp";

    public static boolean isIMapIdle(String protocol) {
        return Protocol.IMAP_IDLE.equals(protocol);
    }

    public static boolean isGmailPop3(String protocol) {
        return Protocol.GMAIL_POP3.equals(protocol);
    }

    public static boolean isGmailImap(String protocol) {
        return Protocol.GMAIL_IMAP.equals(protocol);
    }

    public static boolean isGmailHosted(String protocol) {
        return isGmailPop3(protocol) || isGmailImap(protocol) || isIMapIdle(protocol);
    }

    public static boolean isYahooHosted(String protocol, String name) {
        return protocol.toLowerCase().contains("yahoo") || name.contains("@yahoo.");
    }

    public static boolean isExchange(String protocol) {
        return Protocol.EXCHANGE.equals(protocol);
    }

    public static boolean isMSP(String protocol) {
        return Protocol.MSP_NEW.equals(protocol);
    }

    public static boolean isXobniOauthIdle(String protocolType) {
        return XOBNI_OAUTH_IDLE.equals(protocolType);
    }

    public static boolean isXobniImapIdle(String protocolType) {
        return XOBNI_IMAP_IDLE.equals(protocolType);
    }

    public static boolean isXobni(String protocolType) {
        return isXobniImapIdle(protocolType) || isXobniOauthIdle(protocolType) || isXobniYahooImap(protocolType) || isXobniMSP(protocolType) || isXobniPOP3(protocolType) || isXobniIMap(protocolType) || isXobniOauth(protocolType); 
    }

    public static boolean isXobniYahooImap(String protocolType) {
        return XOBNI_YAHOO_IMAP.equals(protocolType);
    }

    private static boolean isXobniMSP(String protocolType) {
        return XOBNI_MSP.equals(protocolType);
    }

    private static boolean isXobniPOP3(String protocolType) {
        return XOBNI_POP3.equals(protocolType);
    }

    public static boolean isXobniIMap(String protocolType) {
        return XOBNI_IMAP.equals(protocolType);
    }

    public static boolean isXobniOauth(String protocolType) {
        return XOBNI_OAUTH.equals(protocolType);
    }

    public static String getXobniProtocol(String protocolType) {
        if (isGmailHosted(protocolType)) {
            return XOBNI_IMAP_IDLE;
        }
        if (isYahooHosted(protocolType, "")) {
            return XOBNI_YAHOO_IMAP;
        }
        if (isMSP(protocolType)) {
            return XOBNI_MSP;
        }
        if (isPop3(protocolType)) {
            return XOBNI_POP3;
        }
        if (isIMap(protocolType)) {
            return XOBNI_IMAP;
        }
        return null;
    }

    private static boolean isIMap(String protocolType) {
        return IMAP.equals(protocolType);
    }

    public static boolean isPop3(String protocolType) {
        return POP3.equals(protocolType);
    }

    public static boolean isIdle(String protocolType) {
        return isIMapIdle(protocolType) || isXobniImapIdle(protocolType) || isXobniOauthIdle(protocolType);
    }
}