package com.archermind.txtbl.authenticator;

import com.archermind.txtbl.domain.Protocol;

public class AuthenticatorFactory {
    public static Authenticator getAuthenticator(String protocolType) {
        if(Protocol.XOBNI_IMAP.equals(protocolType) || Protocol.XOBNI_IMAP_IDLE.equals(protocolType)
                || Protocol.GMAIL_IMAP.equals(protocolType) || Protocol.IMAP.equals(protocolType) || Protocol.IMAP_IDLE.equals(protocolType)) {
            return new ImapAuthenticator();            
        }
        if(Protocol.XOBNI_YAHOO_IMAP.equals(protocolType) || (Protocol.YAHOO_IMAP.equals(protocolType))) {
            return new YahooImapAuthenticator();
        }
        if(Protocol.XOBNI_OAUTH_IDLE.equals(protocolType)) {
            return new OauthAuthenticator();
        }
        return null;
    }
}
