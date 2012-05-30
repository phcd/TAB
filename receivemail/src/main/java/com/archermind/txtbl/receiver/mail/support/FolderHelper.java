package com.archermind.txtbl.receiver.mail.support;

import javax.mail.Folder;
import javax.mail.MessagingException;
import java.util.List;

public interface FolderHelper {
    int getFolderDepth() throws MessagingException;
    int getTotalMessageCount() throws MessagingException;
    void addMessageValidators(List<MessageValidator> messageValidators);
    Folder next() throws MessagingException;
    void close();
}
