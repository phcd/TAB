package com.archermind.txtbl.receiver.mail.support;

import com.sun.mail.imap.IMAPFolder;

import javax.mail.*;

public class XobniYahooIdsUtil extends UIDIdsUtil {

    public static final XobniYahooIdsUtil INSTANCE = new XobniYahooIdsUtil();

    private XobniYahooIdsUtil() {
        super();
    }

    public void fetch(Folder folder, Message[] messages) throws MessagingException {
        folder.fetch(messages, fetchProfile);
    }

    public String getId(IMAPFolder folder, Message message) throws MessagingException {
        return folder.getName().replace(" ", "~") + "-" + String.valueOf(folder.getUID(message));
    }
}
