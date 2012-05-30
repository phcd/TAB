package com.archermind.txtbl.authenticator;

import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.utils.FinalizationUtils;
import com.archermind.txtbl.utils.StopWatch;
import com.archermind.txtbl.utils.StopWatchUtils;
import org.jboss.logging.Logger;

import javax.mail.*;

public abstract class Authenticator {
    private static final Logger log = Logger.getLogger(ImapAuthenticator.class);

    public abstract Store getStore(Account account, String context, StopWatch watch) throws Exception;

    public Store getStore(Account account, String context, StopWatch watch, LoginFailureHandler loginFailureHandler) throws Exception {
        Store store = null;
        try {
            return getStore(account, context, watch);
        } catch (AuthenticationFailedException e) {
            if (LoginUtil.INSTANCE.isLoginFailure(e)) {
                loginFailureHandler.handleLoginFailures(context, account);
                log.warn(String.format("aborting connecting for %s due to authentication problem", context));
            } else {
                throw e;
            }
        } catch (NoSuchProviderException e) {
            log.warn(String.format("Unable to get imap store as imap provide is not available due to %s for %s", e.getMessage(), context));
        } catch (MessagingException e) {
            if (e.getMessage().contains("Invalid Credentials")) {
                try {
                    loginFailureHandler.handleLoginFailures(context, account);
                }
                catch (Exception dalException) {
                    log.error(String.format("unexpected failure while trying to update login failures account %s %s ", account, dalException));
                }
            }
            else {
                throw e;
            }
            log.error(String.format("unexpected messaging exception %s", account), e);
        }
        FinalizationUtils.close(store);
        return null;
    }

    public Folder getFolder(Store store, String context, StopWatch watch, String folderName) throws Exception {
        StopWatchUtils.newTask(watch, "getInbox", context, log);
        Folder inbox = store.getFolder(folderName);
        StopWatchUtils.newTask(watch, "openFolder " + folderName, context, log);
        inbox.open(Folder.READ_ONLY);
        return inbox;
    }

    public Folder connect(Account account, String context, StopWatch watch, LoginFailureHandler loginFailureHandler, String folderName) throws Exception {
        Store store = getStore(account, context, watch, loginFailureHandler);
        if(store != null) {
            return getFolder(store, context, watch, folderName);
        }
        FinalizationUtils.close(store);
        return null;
    }
}
