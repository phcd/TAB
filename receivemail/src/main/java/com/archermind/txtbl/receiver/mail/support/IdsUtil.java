package com.archermind.txtbl.receiver.mail.support;

import com.sun.mail.imap.IMAPFolder;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

public interface IdsUtil {
    void fetch(Folder folder, Message[] messages) throws MessagingException;
    String getId(IMAPFolder folder, Message message) throws MessagingException;
}
