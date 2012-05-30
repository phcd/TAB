package com.archermind.txtbl.receiver.mail.support;

import java.util.*;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderNotFoundException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;

import com.webalgorithm.exchange.ExchangeClient;
import com.webalgorithm.exchange.ExchangeClientException;
import com.webalgorithm.exchange.dto.Attachment;

public class MSExchangeFolder extends javax.mail.Folder {

    private static final org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(MSExchangeFolder.class);


	private static final String FOLDER_NAME = "Inbox/";
	
	private List<Message> messages = new ArrayList<Message>();

    private Session session;

    private ExchangeClient connector;
	
	public MSExchangeFolder(Session session, ExchangeClient connector) throws NoSuchProviderException {
		super(session.getStore("pop3"));
        this.session = session;
        this.connector = connector;
        
		loadMessages();
	}
	
	public synchronized String getUID(Message message) {
		return ((MSExchangeMessage)message).getId();
	}
	
	@Override
	public void appendMessages(Message[] msgs) throws MessagingException {
        messages.addAll(Arrays.asList(msgs));
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
     * @exception	FolderNotFoundException if this folder does 
     *			not exist.
     * @exception	IllegalStateException if this folder is not opened
     * @exception	IndexOutOfBoundsException if the message number
     *			is out of range.
     * @exception 	MessagingException
     */	
	@Override
	public Message getMessage(int msgnum) throws MessagingException {
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
    
    private void loadMessages() {

        //Collection<com.webalgorithm.exchange.dto.Message> msExchangeMessages = connector.getMessages(FOLDER_NAME);
        Collection<com.webalgorithm.exchange.dto.Message> msExchangeMessages = connector.getMessagesHeaders(FOLDER_NAME);

        for (com.webalgorithm.exchange.dto.Message message : msExchangeMessages) {

            Message mimeMessage = new MSExchangeMessage(session, message);

            appendMessage(mimeMessage);

            log.debug("Loading message " + message);
        }
    }

    public void loadFully(MSExchangeMessage msg) {

        try {
            com.webalgorithm.exchange.dto.Message message = connector.getMessageByUid(FOLDER_NAME, msg.getUid());

            if (message != null) {
                if (message.isHasAttachment()) {
                    Collection<Attachment> attachments = connector.getAttachments(message.getHref());
                    String currentAttachmentHref = null;

                    log.info("Retrieved " + attachments.size() + " attachments from server for message " + msg.getId());

                    try {
                        for (Attachment attachment : attachments) {
                            currentAttachmentHref = attachment.getHref();

                            log.info("Loading attachment data from " + currentAttachmentHref + " for message " + message.getHref());

                            attachment.setData(connector.getAttachmentData(currentAttachmentHref));
                        }
                    } catch (ExchangeClientException e) {
                        log.error("Error loading attachment data from " + currentAttachmentHref + " for message " + message.getHref() + ". " + e.getMessage());
                    }

                    message.setAttachments(attachments);
                } else {
                    log.info("Message contains no attachments:  " + msg.getId());
                }

                msg.setMessage(message, true);
            } else {
                log.error("Error loading message using UID " + msg.getUid());
            }

        } catch (ExchangeClientException e) {
            log.error("Error loading message " + msg.getId() + ". " + e.getMessage());
        }
    }

	private void appendMessage(Message msg) {
		messages.add(msg);
	}

}