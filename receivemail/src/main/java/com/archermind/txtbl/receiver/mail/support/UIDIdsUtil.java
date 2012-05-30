package com.archermind.txtbl.receiver.mail.support;

import com.sun.mail.imap.IMAPFolder;

import javax.mail.*;

public class UIDIdsUtil implements IdsUtil {
    public static final String MESSAGE_ID = "Message-ID";

    public static final UIDIdsUtil INSTANCE = new UIDIdsUtil();

    protected FetchProfile fetchProfile;

    protected UIDIdsUtil() {
        fetchProfile = new FetchProfile();
        fetchProfile.add(UIDFolder.FetchProfileItem.UID);
        fetchProfile.add(MESSAGE_ID);
        fetchProfile.add(FetchProfile.Item.ENVELOPE);
        fetchProfile.add(FetchProfile.Item.CONTENT_INFO);


    }

    public void fetch(Folder folder, Message[] messages) throws MessagingException {
        folder.fetch(messages, fetchProfile);
    }

    public String getId(IMAPFolder folder, Message message) throws MessagingException {
        return String.valueOf(folder.getUID(message));
    }
}
