package com.archermind.txtbl.validate.mailbox.abst;

import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.utils.StringUtils;
import com.archermind.txtbl.utils.UtilsTools;
import com.sun.mail.iap.Argument;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.BASE64MailboxDecoder;
import com.sun.mail.imap.protocol.IMAPProtocol;
import org.jboss.logging.Logger;

import javax.mail.*;
import java.util.*;

public class AllMailsFolderProvider extends FolderProvider {
    private static final Logger logger = Logger.getLogger(AllMailsFolderProvider.class);

    public List<FolderName> getFolderNames(Account account, Store store) {
        List<FolderName> folderNames = new ArrayList<FolderName>();
        FolderName allMailsFolderName = getAllMailsFolder(getFolderTags(account, store));
        if (allMailsFolderName != null) {
            folderNames.add(allMailsFolderName);
        }
        folderNames.add(new FolderName(Account.GMAIL_ALL_MAILS_FOLDER, Account.GMAIL_DRAFTS_FOLDER));
        folderNames.add(new FolderName(Account.GOOGLEMAIL_ALL_MAILS_FOLDER, Account.GOOGLEMAIL_DRAFTS_FOLDER));
        folderNames.add(FolderName.DEFAULT_FOLDER_NAME);
        return folderNames;
    }

    private List<String> getFolderTags(Account account, Store store) {
        final List<String> folderTags = new ArrayList<String>();
        try {
            ((IMAPFolder) store.getDefaultFolder()).doCommand(new IMAPFolder.ProtocolCommand() {
                public Object doCommand(IMAPProtocol imapProtocol) throws ProtocolException {
                    Argument args = new Argument();
                    Argument args1 = new Argument();
                    args1.writeString("");
                    args.append(args1);
                    args.writeString("*");
                    Response[] responses = imapProtocol.command("XLIST", args);
                    for (Response response : responses) {
                        folderTags.add(response.getRest());
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            logger.error("Error getting folder tags for : " + account.getName(), e);
        }
        return folderTags;
    }

    FolderName getAllMailsFolder(List<String> folderTags) {
        if(UtilsTools.isEmpty(folderTags)) {
            return null;
        }
        String allMailsFolder = null;
        String excludeFolder = null;
        for (String folderTag : folderTags) {
            if(folderTag.contains("\\AllMail")) {
                allMailsFolder = getFolderName(folderTag);
            }
            if(folderTag.contains("\\Drafts"))  {
                excludeFolder = getFolderName(folderTag);
            }
        }
        logger.info("Found All Mail folder : " + allMailsFolder + " exclude folder : " + excludeFolder);
        if(!StringUtils.isEmpty(allMailsFolder)) {
            return new FolderName(BASE64MailboxDecoder.decode(allMailsFolder), BASE64MailboxDecoder.decode(excludeFolder));
        }
        return null;
    }

    private String getFolderName(String folderTag) {
        String[] parts = folderTag.split("\"");
        return parts[parts.length - 1];
    }
}
