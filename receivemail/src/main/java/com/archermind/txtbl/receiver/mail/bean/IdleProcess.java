package com.archermind.txtbl.receiver.mail.bean;

import com.archermind.txtbl.authenticator.Authenticator;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.receiver.mail.support.NewProviderSupport;
import com.archermind.txtbl.utils.StopWatch;
import com.archermind.txtbl.utils.StringUtils;
import com.archermind.txtbl.utils.SysConfigManager;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPProtocol;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jboss.logging.Logger;

import javax.mail.*;
import javax.mail.event.MessageCountListener;
import java.util.concurrent.*;

/**
 * This class is used for idle status management.
 */
public class IdleProcess {


    private static final Logger log = Logger.getLogger(IdleProcess.class);

    private long latestProcessStartTime;
    private long lastRefreshTime;
    private IMAPFolder imapFolder;
    Account account;


    public IdleProcess(long latestProcessStartTime, Account account) {
        this.latestProcessStartTime = latestProcessStartTime;
        this.account = account;
        this.lastRefreshTime = latestProcessStartTime;
    }


    //Connects to a

    public boolean connect(Authenticator authenticator, NewProviderSupport support) {

        String context = String.format("[%s] account=%s, uid=%s, email=%s", this.hashCode(), account.getId(), account.getUser_id(), account.getName());

        StopWatch watch = new StopWatch("idle connect " + context);

        try {
            log.info("attempting to connect to idle");
            imapFolder = (IMAPFolder) authenticator.connect(account, context, watch, support, account.getFolderNameToConnect());
            return imapFolder != null;
        }
        catch (Exception e) {
            log.error(String.format("Unable to connect : %s with exception %s", context, e.getMessage()));
            return false;
        }

    }

    public void disconnect() {

        log.info("attempting to kill idle mail connection");
        try {
            Store store = imapFolder.getStore();
            imapFolder.close(false);
            store.close();
        } catch (Exception e) {
            log.warn(String.format("Unable to disconnect with exception %s", e.getMessage()));
        }
    }


    public boolean needsRefreshing() {

        return ((System.currentTimeMillis() - lastRefreshTime) >
                (1000l * 60 * getIdleConnectionRefreshIntervalInMinutes()));

    }

    public boolean needsKilling() {
        return (((System.currentTimeMillis() - latestProcessStartTime)) >
                1000l * 60 * getIdleConnectionTTLInMinutes());
    }


    /**
     * Refresh imap idle connection by issuing a "no op" command
     * .
     *
     * @return True is refresh was succesfull
     */
    public boolean refreshImapConnection() {
        final String context = String.format("[%s] account=%s, uid=%s, email=%s", this.hashCode(), account.getId(), account.getUser_id(), account.getName());

        ExecutorService pool = Executors.newSingleThreadExecutor();

        Future<Integer> future = null;

        final IMAPFolder currentImapFolder = imapFolder;


        try {
            if (currentImapFolder == null) {
                log.warn(String.format("imap folder is not available for %s, most likely due to invalid credentials earlier", context));
            } else {
                future = pool.submit(new Callable<Integer>() {
                    public Integer call() throws Exception {
                        try {
                            if (currentImapFolder.isOpen()) {
                                currentImapFolder.doCommand(new IMAPFolder.ProtocolCommand() {
                                    public Object doCommand(IMAPProtocol p) throws ProtocolException {
                                        p.simpleCommand("NOOP", null);
                                        return null;
                                    }
                                });
                            } else {
                                log.warn(String.format("imap folder is closed for %s ", context));
                            }
                        }
                        catch (Throwable t) {
                            log.error("Unale to refresh imap connection for " + context, t);
                        }

                        return null;
                    }
                });
            }
        }
        catch (Throwable t) {
            log.error(String.format("unable to schedule refresh connection command for %s", context), t);

            disconnect();
        }

        try {
            if (future != null) {
                future.get(10, TimeUnit.SECONDS);

                log.info(String.format("refresh connection command for %s completed", context));
                pool.shutdown();
                lastRefreshTime = System.currentTimeMillis();
                return true; // success
            } else {
                disconnect(); //folder is closed or could not refresh, disconnect and try again
            }
        }
        catch (TimeoutException e) {
            log.error(String.format("refresh connection command timed out for %s", context));

            disconnect();
        }
        catch (Throwable e) {
            log.error(String.format("refresh connection command was interrupted for %s", context), e);

            disconnect();
        }
        finally {
            if (log.isDebugEnabled())
                log.debug(String.format("invoking pool shutdown for %s", context));
            pool.shutdown();
        }

        return false;
    }


    private long getIdleConnectionTTLInMinutes() {
        String idleConnectionTtl = SysConfigManager.instance().getValue("idleConnectionTtl");

        if (StringUtils.isEmpty(idleConnectionTtl)) {
            log.warn("default idle connection time to live (idleConnectionTtl) is not defined in configuration using default of 60 minutes");
            return 60l;
        } else {
            return Long.valueOf(idleConnectionTtl);
        }

    }

    private long getIdleConnectionRefreshIntervalInMinutes() {
        String idleConnectionRefreshInterval = SysConfigManager.instance().getValue("idleConnectionRefreshInterval");

        if (StringUtils.isEmpty(idleConnectionRefreshInterval)) {
            log.warn("default idle connection time to live (idleConnectionTtl) is not defined in configuration using default of 60 minutes");
            return 10l;
        } else {
            return Long.valueOf(idleConnectionRefreshInterval);
        }
    }


    public long getLatestProcessStartTime() {
        return latestProcessStartTime;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(latestProcessStartTime).append(account.getName()).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IdleProcess) {
            IdleProcess saved = (IdleProcess) obj;

            return new EqualsBuilder().append(saved.getLatestProcessStartTime(), latestProcessStartTime).isEquals();
        } else {
            return false;
        }

    }

    public boolean isConnected() {
        if (imapFolder == null) {
            return false;
        }

        Store store = imapFolder.getStore();
        return store != null && store.isConnected();
    }

    public void idle(String context) throws MessagingException {
        if (!imapFolder.isOpen()) {
            imapFolder.open(Folder.READ_ONLY); /* throws MessagingException */
        }

        log.info(String.format("entering idling state for %s", context));
        imapFolder.idle();
        log.info(String.format("completed idle iteration for %s", context));

    }

    public void removeMessageCountListener(MessageCountListener messageCountListener) {
        if (imapFolder != null) {
            imapFolder.removeMessageCountListener(messageCountListener);
        }
    }

    public void addMessageCountListener(MessageCountListener messageCountListener) {
        if (imapFolder != null) {
            imapFolder.addMessageCountListener(messageCountListener);
        }
    }

    public long getUID(Message message) throws MessagingException {
        return imapFolder.getUID(message);
    }

    public Folder getImapFolder() {
        return imapFolder;
    }
}
