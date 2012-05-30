package com.archermind.txtbl.receiver.mail.support;

import javax.mail.Folder;
import javax.mail.MessagingException;
import java.util.List;

public class DefaultFolderHelper extends SingleFolderHelper {
    public DefaultFolderHelper(Folder folder) {
        super(folder);
    }

    public int getFolderDepth() throws MessagingException {
        return folder.getMessageCount();
    }

    public void addMessageValidators(List<MessageValidator> messageValidators) {
        //do nothing
    }
}
