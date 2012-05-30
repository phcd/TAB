package com.archermind.txtbl.authenticator;

import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.utils.StopWatch;
import com.archermind.txtbl.utils.StopWatchUtils;
import com.sun.mail.imap.IMAPStore;
import org.jboss.logging.Logger;

import javax.mail.*;
import java.security.Security;
import java.util.Properties;

public class ImapAuthenticator extends Authenticator {
    private static final Logger log = Logger.getLogger(ImapAuthenticator.class);

    @Override
    public Store getStore(Account account, String context, StopWatch watch) throws Exception {
        IMAPStore store = null;
        try {
            StopWatchUtils.newTask(watch, "getSession", context, log);
            Session session = Session.getInstance(getMailProperties(account));

            StopWatchUtils.newTask(watch, "getStore", context, log);
            store = (IMAPStore) session.getStore("imap");

            StopWatchUtils.newTask(watch, "connect", context, log);
            store.connect(account.getReceiveHost(), account.getLoginName(), account.getPassword());
            return store;
        } catch (AuthenticationFailedException e) {
            if (e.getMessage().contains("Your account is not enabled for IMAP use")) {
                log.info(String.format("enabling imap for %s", context));
                enableIdle(account, context);
                log.info(String.format("attempting to connect after enabling imap for %s", context));
                store.connect(account.getReceiveHost(), account.getLoginName(), account.getPassword());
            }
        }
        return null;
    }

    private Properties getMailProperties(Account account) {
        Properties props = new Properties();

        if (account.isGmailHosted() || account.isXobniImapIdle()) {
            props.put("mail.imap.host", "imap.gmail.com");
            props.put("mail.imap.port", "143");
            props.put("mail.imap.auth", "true");
            props.put("mail.store.protocol", "imap");
            props.put("mail.imap.starttls.enable", "true");
            props.put("mail.imap.socketFactory.port", "993");
            props.put("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.iamp.socketFactory.fallback", "false");
        }
        else {

            props.setProperty("mail.imap.port", account.getReceivePort());
            props.setProperty("mail.imap.connectiontimeout", "180000");
            if ("ssl".equals(account.getReceiveTs())) {
                Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
                props.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.setProperty("mail.imap.socketFactory.fallback", "false");
                props.setProperty("mail.imap.socketFactory.port", account.getReceivePort());
            }
            else if ("tls".equals(account.getReceiveTs())) {
                props.setProperty("mail.imap.starttls.enable", "true");
                Security.setProperty("ssl.SocketFactory.provider", "com.archermind.txtbl.mail.DummySSLSocketFactory");
            }
        }


        return props;
    }

    public void enableIdle(Account account, String context) throws GmailImapEnablerException {
        log.debug(String.format("enabling imap for %s", context));

        GmailImapEnabler imapEnabler = new GmailImapEnabler();

        if (account.getLoginName().startsWith("recent:")) {
            String login = account.getLoginName().substring(account.getLoginName().indexOf(':') + 1);

            imapEnabler.enableImap(login, account.getPassword());
        }
        else {
            imapEnabler.enableImap(account.getLoginName(), account.getPassword());
        }

    }
}
