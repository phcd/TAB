package com.archermind.txtbl.receiver.mail.support;

import com.archermind.txtbl.utils.UtilsTools;
import com.sun.mail.imap.IMAPFolder;

import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

public class HeaderMessageIdsUtil implements IdsUtil {
    public static final String MESSAGE_ID = "Message-ID";

    public static final HeaderMessageIdsUtil INSTANCE = new HeaderMessageIdsUtil();

    private FetchProfile fetchProfile;

    private HeaderMessageIdsUtil() {
        fetchProfile = new FetchProfile();
        fetchProfile.add(MESSAGE_ID);
    }

    public void fetch(Folder draftsFolder, Message[] messages) throws MessagingException {
        draftsFolder.fetch(messages, fetchProfile);
    }

    public String getId(IMAPFolder folder, Message message) throws MessagingException {
        return getId(message);
    }

    public String getId(Message message) throws MessagingException {
        String[] header = message.getHeader(MESSAGE_ID);
        if(UtilsTools.isEmpty(header)) {
            return null;
        }
        return header[0];
    }

}
