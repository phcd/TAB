package com.archermind.txtbl.validate.mailbox.abst;

import com.archermind.txtbl.authenticator.Authenticator;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.utils.FinalizationUtils;
import com.archermind.txtbl.utils.StopWatch;
import org.jboss.logging.Logger;

import javax.mail.FolderNotFoundException;
import javax.mail.Store;
import java.util.List;

public abstract class Validate {

	private static final Logger logger = Logger.getLogger(Validate.class);

    public abstract void validate(Account account) throws Exception;

    protected void validate(Account account, Authenticator authenticator, FolderProvider folderProvider) throws Exception {
        boolean success = false;
        String context = account.getName();
        Store store = null;
        try {
            StopWatch watch = new StopWatch();
            store = authenticator.getStore(account, context, watch);
            if(store != null) {
                success = getFolder(account, authenticator, folderProvider, context, store, watch);
            }
        } catch (Exception e) {
            logger.error("Error validating : " + context, e);
            success = false;
        } finally {
            FinalizationUtils.close(store);
        }

        if (!success) {
            throw new Exception("Unable to validate " + context);
        }
    }

    private boolean getFolder(Account account, Authenticator authenticator, FolderProvider folderProvider, String context, Store store, StopWatch watch) throws Exception {
        List<FolderName> folderNames = folderProvider.getFolderNames(account, store);
        for (FolderName folderName : folderNames) {
            try {
                if(authenticator.getFolder(store, context, watch, folderName.name) != null) {
                    if(!folderName.name.equals(account.getFolderNameToConnect())) {
                        account.setFolderName(folderName.name);
                        if(folderName.excludeFolder != null) {
                            try {
                                if(authenticator.getFolder(store, context, watch, folderName.excludeFolder) != null) {
                                    account.setExcludeFolder(folderName.excludeFolder);
                                }
                            } catch (Exception e) {
                                logger.error("Error while fetching exclude folder : " + folderName.excludeFolder);
                            }
                        }
                    }
                    return true;
                }
            } catch (FolderNotFoundException ignored) {}
        }
        return false;
    }

}
