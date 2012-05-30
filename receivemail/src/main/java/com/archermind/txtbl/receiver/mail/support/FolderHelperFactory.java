package com.archermind.txtbl.receiver.mail.support;

import com.archermind.txtbl.authenticator.Authenticator;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.utils.StopWatch;
import com.sun.mail.imap.IMAPFolder;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Store;

public class FolderHelperFactory {
    public static FolderHelper getFolderHelper(Account account, Folder folder) throws MessagingException {
        if(account.isXobniAccount()) {
            return new AllMailsFolderHelper(account, (IMAPFolder)folder);
        }
        
        return new DefaultFolderHelper(folder);
    }

    public static FolderHelper getFolderHelper(Authenticator authenticator, Account account, Store store, String context, StopWatch watch) throws Exception {
        if(store != null) {
            if(account.isXobniYahooImap()) {
                return new YahooMultipleFolderHelper(store.getDefaultFolder());
            }

            Folder folder = authenticator.getFolder(store, context, watch, account.getFolderNameToConnect());
            return getFolderHelper(account, folder);
        }
        return null;
    }

}
