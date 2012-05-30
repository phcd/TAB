package com.archermind.txtbl.authenticator;

import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.utils.StopWatch;
import com.archermind.txtbl.utils.StringUtils;
import com.archermind.txtbl.utils.XoauthSaslClientFactory;
import com.sun.mail.imap.IMAPSSLStore;
import org.jboss.logging.Logger;

import javax.mail.*;
import java.security.Security;
import java.util.Properties;

public class OauthAuthenticator extends ImapAuthenticator {
    private static final Logger log = Logger.getLogger(OauthAuthenticator.class);

    static {
        Security.addProvider(new XoauthProvider());
    }

    public static final class XoauthProvider extends java.security.Provider {
        public XoauthProvider() {
            super("Google Xoauth Provider", 1.0, "Provides the Xoauth experimental SASL Mechanism");
            put("SaslClientFactory.XOAUTH", "com.archermind.txtbl.utils.XoauthSaslClientFactory");
        }
    }

    @Override
    public Store getStore(Account account, String context, StopWatch watch) throws Exception {
        log.info(String.format( "trying to connect for %s", context));

        String oauthToken = account.getOauthToken();
        String oauthTokenSecret = account.getOauthTokenSecret();
        String consumerKey = account.getConsumerKey();
        String consumerSecret = account.getConsumerSecret();

        if(StringUtils.isEmpty(consumerKey)) {
            consumerKey = "anonymous";
        }

        if(StringUtils.isEmpty(consumerSecret)) {
            consumerSecret = "anonymous";
        }

        Properties props = new Properties();
        props.put("mail.imaps.sasl.enable", "true");
        props.put("mail.imaps.sasl.mechanisms", "XOAUTH");
        props.put(XoauthSaslClientFactory.OAUTH_TOKEN_PROP, oauthToken);
        props.put(XoauthSaslClientFactory.OAUTH_TOKEN_SECRET_PROP, oauthTokenSecret);
        props.put(XoauthSaslClientFactory.CONSUMER_KEY_PROP, consumerKey);
        props.put(XoauthSaslClientFactory.CONSUMER_SECRET_PROP, consumerSecret);
        Session session = Session.getInstance(props);

        log.info(String.format( "got session for %s", context));

        final URLName unusedUrlName = null;
        IMAPSSLStore imapStore = new IMAPSSLStore(session, unusedUrlName);
        log.info(String.format( "got store for %s", context));
        final String emptyPassword = "";
        int port = 993;

        try {
            port = Integer.parseInt(account.getReceivePort());
        } catch (NumberFormatException e) {
            log.error(String.format("receive port not valid %s", account.getReceivePort()), e);
        }

        imapStore.connect(account.getReceiveHost(), port, account.getLoginName(), emptyPassword);
        log.info(String.format( "connected to store for %s", context));
        return imapStore;
    }

    public static void main(String[] args) throws Exception {
        Account account = new Account();
        account.setOauthToken("1/J6EFYuxEd4hhB_86YMdu3BxqXOhFqxEilSXW1DlkjpE");
        account.setOauthTokenSecret("Wc1P1VhqJsbmPIvsrrM1zob");
        account.setLoginName("jmpak80@gmail.com");
        account.setReceiveHost("imap.googlemail.com");
        new OauthAuthenticator().connect(account, "", new StopWatch(), new DoNothingLoginFailureHandler(), account.getFolderNameToConnect());
    }

}
