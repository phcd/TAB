package com.archermind.txtbl.authenticator;

import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.utils.StopWatch;
import com.archermind.txtbl.utils.StopWatchUtils;
import com.archermind.txtbl.utils.SysConfigManager;
import com.sun.mail.imap.IMAPStore;
import org.jboss.logging.Logger;

import javax.mail.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Security;
import java.util.Properties;

public class YahooImapAuthenticator extends ImapAuthenticator {
    private static final Logger log = Logger.getLogger(Authenticator.class);

    private boolean specialYahooConnectEnabled;

    public YahooImapAuthenticator() {
        this.specialYahooConnectEnabled = Boolean.parseBoolean(SysConfigManager.instance().getValue("specialYahooImapConnect", "true"));
    }

    @Override
    public Store getStore(Account account, String context, StopWatch watch) throws Exception {
        return yahooConnect(account, specialYahooConnectEnabled, context, watch);
    }

    private Properties getMailProperties(Account account) {
        Properties props = new Properties();

        props.setProperty("mail.imap.port", account.getReceivePort());
        props.setProperty("mail.imap.connectiontimeout", "180000");
        if ("ssl".equals(account.getReceiveTs())) {
            Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
            props.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.setProperty("mail.imap.socketFactory.fallback", "false");
            props.setProperty("mail.imap.socketFactory.port", account.getReceivePort());
        } else if ("tls".equals(account.getReceiveTs())) {
            props.setProperty("mail.imap.starttls.enable", "true");
            Security.setProperty("ssl.SocketFactory.provider", "com.archermind.txtbl.mail.DummySSLSocketFactory");
        }

        return props;
    }

    public IMAPStore yahooConnect(Account account, boolean specialYahooConnectEnabled, String context, StopWatch watch) throws UnknownHostException, MessagingException {
        int connectionAttempts = 0;

        StopWatchUtils.newTask(watch, "getSession", context, log);
        Session session = Session.getInstance(getMailProperties(account));
        session.setDebug(false);

        StopWatchUtils.newTask(watch, "getStore", context, log);
        IMAPStore store = (IMAPStore) session.getStore("imap");

        StopWatchUtils.newTask(watch, "setIDCommand", context, log);

        store.SetIDCommand("ID (\"vendor\" \"Zimbra\" \"os\" \"Windows XP\" \"os-version\" \"5.1\" \"guid\" \"4062-5711-9195-4050\")");

        StopWatchUtils.newTask(watch, "connect", context, log);

        boolean succ = false;
        if (specialYahooConnectEnabled) {
            String[] tempHost = account.getReceiveHost().split(",");
            for (String host : tempHost) {
                try {
                    InetAddress[] addresses = InetAddress.getAllByName(host);
                    for (InetAddress address : addresses) {
                        try {
                            store.connect(address.getHostAddress(), account.getLoginName(), account.getPassword());
                            connectionAttempts++;
                            if(log.isDebugEnabled())
                            log.debug(String.format("connected to %s for host %s for %s", address.getHostAddress(), account.getReceiveHost(), context));
                            succ = true;
                            break;
                        } catch (AuthenticationFailedException e) {
                            throw e;
                        } catch (Throwable t) {
                            log.warn(String.format("connection attempt to %s failed for %s", address.getHostAddress(), context), t);
                        }
                    }
                } catch (Exception e) {
                    if(log.isDebugEnabled())
                    log.debug(String.format("validate/YahooIMAP4Provider/Exception: [" + account.getName() + "]" + e));
                }
                if(succ) {
                    break;
                }
            }
        } else {
            store.connect(account.getReceiveHost(), account.getLoginName(), account.getPassword());
            connectionAttempts++;
            succ = true;
        }

        StopWatchUtils.newTask(watch, String.format("connection established after %d connects)", connectionAttempts), context, log);
        if(succ) {
            return store;
        }

        return null;
    }

}