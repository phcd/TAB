package com.archermind.txtbl.receiver.mail.support;

import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.utils.EmailUtil;
import com.archermind.txtbl.utils.FinalizationUtils;
import com.archermind.txtbl.utils.StringUtils;
import com.archermind.txtbl.utils.UtilsTools;
import com.sun.mail.imap.IMAPFolder;
import org.jboss.logging.Logger;

import javax.mail.*;
import java.util.ArrayList;
import java.util.List;

public class AllMailsFolderHelper extends SingleFolderHelper {
    protected static final Logger log = Logger.getLogger(AllMailsFolderHelper.class);

    private Account account;
    private List<String> draftMessageIds;

    public AllMailsFolderHelper(Account account, IMAPFolder folder) throws MessagingException {
        super(folder);
        this.account = account;
        this.draftMessageIds = getDraftMessageIds(account, folder);
    }


    public int getFolderDepth() throws MessagingException {
        int folderDepth = folder.getMessageCount();
        return folderDepth - draftMessageIds.size();
    }

    public void addMessageValidators(List<MessageValidator> messageValidators) {
        messageValidators.add(new MessageValidator() {
            public Message validate(Message message, int messageNumber, String messageId) throws Exception {
                if (isDraftMessage(message, draftMessageIds)) {
                    log.info("Ignoring draft message with messageNumber " + messageId + " for account " + account.getName());
                    return null;
                }
                return message;
            }
        });
    }

    private List<String> getDraftMessageIds(Account account, IMAPFolder folder) throws MessagingException {
        List<String> draftMessageIds = new ArrayList<String>();
        String draftsFolderName = account.getDraftsFolder();
        if (!StringUtils.isEmpty(draftsFolderName)) {
            Folder draftsFolder = null;
            try {
                draftsFolder = folder.getStore().getFolder(draftsFolderName);
                draftsFolder.open(Folder.READ_ONLY);

                Message[] messages = draftsFolder.getMessages();

                HeaderMessageIdsUtil.INSTANCE.fetch(draftsFolder, messages);

                for (Message message : messages) {
                    draftMessageIds.add(HeaderMessageIdsUtil.INSTANCE.getId(folder, message));
                }
            } catch (FolderNotFoundException e) {
                log.info("Drafts folder not found for " + account.getName());
            } finally {
                FinalizationUtils.close(draftsFolder);
            }
        }
        return draftMessageIds;
    }

    public static boolean isDraftMessage(Message message, List<String> draftMessageIds) throws MessagingException {
        if(!UtilsTools.isEmpty(draftMessageIds)) {
            String headerMessageId = HeaderMessageIdsUtil.INSTANCE.getId(message);
            return headerMessageId != null && draftMessageIds.contains(headerMessageId);
        }
        return false;
    }

}
