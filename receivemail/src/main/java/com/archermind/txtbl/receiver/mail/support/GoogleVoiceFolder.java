package com.archermind.txtbl.receiver.mail.support;

import com.techventus.server.voice.Voice;
import org.jboss.logging.Logger;

import javax.mail.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class GoogleVoiceFolder extends Folder {
    private static final Logger log = Logger.getLogger(GoogleVoiceFolder.class);

	private static final String FOLDER_NAME = "Inbox/";

	private List<Message> messages = new ArrayList<Message>();

    private Session session;

    private Voice voice;
    private com.techventus.server.voice.Folder folder;

	public GoogleVoiceFolder(Session session, Voice voice) throws NoSuchProviderException,IOException {
		super(session.getStore("pop3"));
        this.session = session;
        this.voice = voice;
        try{
            this.folder = com.techventus.server.voice.Folder.getFolder(voice);
        }catch (Exception e){

        }

		loadMessages();
	}

	public synchronized String getUID(Message message) {
		return ((GoogleVoiceMessage)message).getId();
	}

    public synchronized TreeMap<String,String>  getAllUID() throws MessagingException {
    	TreeMap<String,String> map = new TreeMap<String, String>();

    	for (int i = 1; i <= getMessageCount(); i++) {
    		map.put(getUID(getMessage(i)), String.valueOf(i));
    	}

    	return map;
    }

	@Override
	public void appendMessages(Message[] msgs) throws MessagingException {
		for (int i = 0; i < msgs.length; i++) {
			messages.add(msgs[i]);
		}
	}

	@Override
	public void close(boolean expunge) throws MessagingException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean create(int type) throws MessagingException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean delete(boolean recurse) throws MessagingException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean exists() throws MessagingException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Message[] expunge() throws MessagingException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Folder getFolder(String name) throws MessagingException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFullName() {
		return FOLDER_NAME;
	}

    /**
     * Get the Message object corresponding to the given message
     * number.  A Message object's message number is the relative
     * position of this Message in its Folder. Messages are numbered
     * starting at 1 through the total number of message in the folder.
     * Note that the message number for a particular Message can change
     * during a session if other messages in the Folder are deleted and
     * the Folder is expunged. <p>
     *
     * Message objects are light-weight references to the actual message
     * that get filled up on demand. Hence Folder implementations are
     * expected to provide light-weight Message objects. <p>
     *
     * Unlike Folder objects, repeated calls to getMessage with the
     * same message number will return the same Message object, as
     * long as no messages in this folder have been expunged. <p>
     *
     * Since message numbers can change within a session if the folder
     * is expunged , clients are advised not to use message numbers as
     * references to messages. Use Message objects instead.
     *
     * @param msgnum	the message number
     * @return 		the Message object
     * @see		#getMessageCount
     * @see		#fetch
     * @exception	javax.mail.FolderNotFoundException if this folder does
     *			not exist.
     * @exception	IllegalStateException if this folder is not opened
     * @exception	IndexOutOfBoundsException if the message number
     *			is out of range.
     * @exception 	javax.mail.MessagingException
     */
	@Override
	public Message getMessage(int msgnum) throws MessagingException {
        if(log.isTraceEnabled())
            log.trace(String.format("getMessage(msgnum=%s)", String.valueOf(msgnum)));
		return messages.get(msgnum - 1);
	}

	@Override
	public int getMessageCount() throws MessagingException {
		return messages.size();
	}

	@Override
	public String getName() {
		return FOLDER_NAME;
	}

	@Override
	public Folder getParent() throws MessagingException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Flags getPermanentFlags() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public char getSeparator() throws MessagingException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getType() throws MessagingException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean hasNewMessages() throws MessagingException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isOpen() {
		return true;
	}

	@Override
	public Folder[] list(String pattern) throws MessagingException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void open(int mode) throws MessagingException {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean renameTo(Folder f) throws MessagingException {
		// TODO Auto-generated method stub
		return false;
	}

    private void loadMessages() throws IOException {

        com.techventus.server.voice.datatypes.records.Message[] messages = folder.getMessages();

        for (int i=0;i<messages.length;i++) {

            Message mimeMessage = new GoogleVoiceMessage(session, folder,messages[i]);

            appendMessage(mimeMessage);

            log.debug("Loading message " + messages[i].toString());
        }
    }

    public void loadFully(GoogleVoiceMessage msg) throws Exception{
           
        msg.setMessage(folder);

    }

	private void appendMessage(Message msg) {
		messages.add(msg);
	}

}