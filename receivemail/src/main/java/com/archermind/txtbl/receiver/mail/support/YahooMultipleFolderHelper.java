package com.archermind.txtbl.receiver.mail.support;

import com.archermind.txtbl.utils.FinalizationUtils;

import javax.mail.Folder;
import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class YahooMultipleFolderHelper implements FolderHelper {
    List<Folder> folders;
    int currentFolderIndex = -1;
    int folderDepth = 0;

    public YahooMultipleFolderHelper(Folder defaultFolder) throws MessagingException {
        this.folders = sortAndExcludeFolders(defaultFolder.list());
        Folder folder;
        while ((folder = next()) != null) {
            folderDepth += folder.getMessageCount();
        }
    }

    public int getFolderDepth() throws MessagingException {
        return folderDepth;
    }

    public int getTotalMessageCount() throws MessagingException {
        return folderDepth;
    }

    public void addMessageValidators(List<MessageValidator> messageValidators) {
        //do nothing
    }

    public Folder next() throws MessagingException {
        if (currentFolderIndex > -1) {
            folders.get(currentFolderIndex).close(false);
        }
        currentFolderIndex += 1;
        if(currentFolderIndex == folders.size()) {
            reset();
            return null;
        }
        Folder folder = folders.get(currentFolderIndex);
        folder.open(Folder.READ_ONLY);
        return folder;
    }

    private void reset() {
        currentFolderIndex = -1;
    }

    public void close() {
        for (Folder folder : folders) {
            if (folder.isOpen()) {
                FinalizationUtils.close(folder);
            }
        }
    }

    private static List<Folder> sortAndExcludeFolders(Folder[] folders) {
        List<String> excludeFolders = Arrays.asList("Bulk Mail", "Y! Conversations", "Draft", "Trash");
        List<Folder> sortedFolders = new ArrayList<Folder>();
        Folder sentFolder = null;
        for (Folder folder : folders) {
            String name = folder.getName();
            if (!excludeFolders.contains(name)) {
                if ("inbox".equalsIgnoreCase(name)) {
                    sortedFolders.add(0, folder);
                } else if ("sent".equalsIgnoreCase(name)) {
                    sentFolder = folder;
                } else {
                    sortedFolders.add(folder);
                }
            }
        }
        if (sentFolder != null) {
            sortedFolders.add(sentFolder);
        }
        return sortedFolders;
    }

}
