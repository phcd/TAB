package com.archermind.txtbl.receiver.mail.support;

import com.archermind.txtbl.utils.ReceiverUtilsTools;
import com.webalgorithm.exchange.dto.Attachment;
import org.jboss.logging.Logger;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.UnsupportedEncodingException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;

public class MSExchangeMessage extends MimeMessage {

    private static final Logger log = Logger.getLogger(MSExchangeMessage.class);
	
	private com.webalgorithm.exchange.dto.Message message;

	public MSExchangeMessage(Session session, com.webalgorithm.exchange.dto.Message message) {
		super(session);
		setMessage(message, false);
	}

    public void setMessage(com.webalgorithm.exchange.dto.Message message, boolean loadAttachments) {
        this.message = message;

        try {
            String body = message.getBody();
            if (body == null) {
                body = "";
            }

            super.content = body.getBytes("UTF-8");

            Multipart multipart = new MimeMultipart("multipart");

            BodyPart bodyPart = new MimeBodyPart();
            String messageBody = message.getBody() != null ? message.getBody() : "";
            bodyPart.setContent(messageBody, "text/plain;charset=utf-8");
            multipart.addBodyPart(bodyPart);

            if (loadAttachments && message.isHasAttachment()) {
                Collection<Attachment> attachments = message.getAttachments();
                log.info("There are " + attachments.size() + " attachments to process for message " + getId());
                for (Attachment attachment : attachments) {
                    byte[] data = attachment.getData();

                    log.info("\tAttachment " + attachment.getFileName() + " contains " + (data != null ? data.length : 0) + " bytes");
                    if (data == null) {
                        data = "".getBytes("UTF-8");
                    }

                    ByteArrayDataSource byteArrayDataSource = new ByteArrayDataSource(data, "application/octet-stream");
                    MimeBodyPart attachPart = new MimeBodyPart();
                    attachPart.setDataHandler(new DataHandler(byteArrayDataSource));
                    attachPart.setFileName(attachment.getFileName());
                    multipart.addBodyPart(attachPart);
                }
            }

            super.setContent(multipart);

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Error encoding content as UTF-8", e);
        } catch (MessagingException e) {
            throw new RuntimeException("Error creating MSExchangeMessage", e);
        }
    }

	@Override
	public synchronized Flags getFlags() throws MessagingException {
		return new Flags(Flag.RECENT);
	}
	
    public String getId() {
        return message.getId();
    }

    public String getUid() {
        return message.getUid();
    }

    @Override
    protected void parse(InputStream is) throws MessagingException
    {
        super.parse(is);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
	public Address[] getFrom() throws MessagingException {
		return new InternetAddress[]{new InternetAddress(message.getFromName())};
	}

	@Override
	public Date getReceivedDate() throws MessagingException {
		return message.getReceivedDate();
	}

	@Override
	public Address[] getRecipients(Message.RecipientType type)
			throws MessagingException {
		if (Message.RecipientType.TO.equals(type)) {
			return new InternetAddress[]{new InternetAddress(message.getTo())};
		} else if (Message.RecipientType.CC.equals(type)) {
			return new InternetAddress[]{new InternetAddress(message.getCc())};
		}  else {
			return new InternetAddress[]{new InternetAddress(message.getBcc())};
		}
	}

	@Override
	public Address[] getReplyTo() throws MessagingException {
		return new InternetAddress[]{new InternetAddress(message.getReplyTo())};
	}

	@Override
	public Address getSender() throws MessagingException {
		return new InternetAddress(message.getFromEmail());
	}

	@Override
	public Date getSentDate() throws MessagingException {
		return message.getSentDate();
	}

	@Override
	public int getSize() throws MessagingException {
		return message.getBody() != null ? message.getBody().length() : 0;
	}

    @Override
	public String getSubject() throws MessagingException {
		return message.getSubject();
	}

    /**
	 * Return all the headers from this Message as an enumeration of Header
	 * objects.
	 * <p>
	 * 
	 * Note that certain headers may be encoded as per RFC 2047 if they contain
	 * non US-ASCII characters and these should be decoded.
	 * <p>
	 * 
	 * @return array of header objects
	 * @exception MessagingException
	 * @see javax.mail.internet.MimeUtility
	 */
	@Override
	public Enumeration getAllHeaders() throws MessagingException {
		loadHeaders();
		return headers.getAllHeaders();
	}

	/**
	 * Return matching headers from this Message as an Enumeration of Header
	 * objects.
	 * 
	 * @exception MessagingException
	 */
	@Override
	public Enumeration getMatchingHeaders(String[] names)
			throws MessagingException {
		loadHeaders();
		return headers.getMatchingHeaders(names);
	}

	/**
	 * Return non-matching headers from this Message as an Enumeration of Header
	 * objects.
	 * 
	 * @exception MessagingException
	 */
	@Override
	public Enumeration getNonMatchingHeaders(String[] names)
			throws MessagingException {
		loadHeaders();
		return headers.getNonMatchingHeaders(names);
	}

	/**
	 * Get all header lines as an Enumeration of Strings. A Header line is a raw
	 * RFC822 header-line, containing both the "name" and "value" field.
	 * 
	 * @exception MessagingException
	 */
	@Override
	public Enumeration getAllHeaderLines() throws MessagingException {
		loadHeaders();
		return headers.getAllHeaderLines();
	}

	/**
	 * Get matching header lines as an Enumeration of Strings. A Header line is
	 * a raw RFC822 header-line, containing both the "name" and "value" field.
	 * 
	 * @exception MessagingException
	 */
	@Override
	public Enumeration getMatchingHeaderLines(String[] names)
			throws MessagingException {
		loadHeaders();
		return headers.getMatchingHeaderLines(names);
	}

	/**
	 * Get non-matching header lines as an Enumeration of Strings. A Header line
	 * is a raw RFC822 header-line, containing both the "name" and "value"
	 * field.
	 * 
	 * @exception MessagingException
	 */
	@Override
	public Enumeration getNonMatchingHeaderLines(String[] names)
			throws MessagingException {
		loadHeaders();
		return headers.getNonMatchingHeaderLines(names);
	}

	/**
	 * Load the headers for this message into the InternetHeaders object. The
	 * headers are fetched using the POP3 TOP command.
	 */
	private void loadHeaders() throws MessagingException {
		
	    synchronized (this) {
			if (headers == null) {
				headers = new InternetHeaders();
			}
						
			headers.addHeader("Date", ReceiverUtilsTools.dateToStr(message.getSentDate()));
			headers.addHeader("From", message.getFromName());
			headers.addHeader("Sender", message.getFromName());
			headers.addHeader("Reply-To", message.getReplyTo());
			headers.addHeader("To", message.getTo());
			headers.addHeader("Cc", message.getCc());
			headers.addHeader("Bcc", message.getBcc());
			headers.addHeader("Message-Id", message.getId());
			headers.addHeader("Subject", message.getSubject());

//			headers.addHeader("Return-Path", null);
//			headers.addHeader("Received", null);
//			headers.addHeader("Resent-Date", null);
//			headers.addHeader("Resent-From", null);
//			headers.addHeader("Resent-Sender", null);
//			headers.addHeader("Resent-To", null);
//			headers.addHeader("Resent-Cc", null);
//			headers.addHeader("Resent-Bcc", null);
//			headers.addHeader("Resent-Message-Id", null);
//			headers.addHeader("In-Reply-To", null);
//			headers.addHeader("References", null);
//			headers.addHeader("Comments", null);
//			headers.addHeader("Keywords", null);
//			headers.addHeader("Errors-To", null);
//			headers.addHeader("MIME-Version", null);
//			headers.addHeader("Content-Type", null);
//			headers.addHeader("Content-Transfer-Encoding", null);
//			headers.addHeader("Content-MD5", null);
//			headers.addHeader(":", null);
//			headers.addHeader("Content-Length", null);
//			headers.addHeader("Status", null);
	    }
	}
}
