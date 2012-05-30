package com.archermind.txtbl.receiver.mail.support;

import com.archermind.txtbl.utils.FinalizationUtils;

import javax.mail.Folder;
import javax.mail.MessagingException;

public abstract class SingleFolderHelper implements FolderHelper {
    protected Folder folder;
    protected Folder currentFolder = null;

    public SingleFolderHelper(Folder folder) {
        this.folder = folder;
    }

    public Folder next() {
        if(currentFolder == null) {
            currentFolder = folder;
            return currentFolder;
        } else {
            currentFolder = null;
            return null;
        }
    }

    public int getTotalMessageCount() throws MessagingException {
        return folder.getMessageCount();
    }

    public void close() {
        FinalizationUtils.close(folder);
    }
}
