package com.archermind.txtbl.receiver.mail.support;


import org.jboss.logging.Logger;

import javax.mail.*;
import javax.mail.Flags.Flag;
import javax.mail.internet.*;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Enumeration;

public class GoogleVoiceMessage extends MimeMessage {

    private static final Logger log = Logger.getLogger(GoogleVoiceMessage.class);

    private com.techventus.server.voice.datatypes.records.Message message;
    private com.techventus.server.voice.Folder folder;

    public GoogleVoiceMessage(Session session,com.techventus.server.voice.Folder folder, com.techventus.server.voice.datatypes.records.Message message) {
        super(session);
        setMessage(folder,message);
    }

    public void setMessage(com.techventus.server.voice.Folder folder){

        setMessage(folder,this.message);

    }

    public void setMessage(com.techventus.server.voice.Folder folder,
                           com.techventus.server.voice.datatypes.records.Message message) {
        this.message = message;
        this.folder = folder;
        try {
            com.techventus.server.voice.datatypes.records.Message loadedMsg = folder.fetchFullyLoaded(message.getId());
            String body = loadedMsg.getBody();
            if (body == null) {
                body = "";
            }

            super.content = body.getBytes("UTF-8");

            Multipart multipart = new MimeMultipart("multipart");

            BodyPart bodyPart = new MimeBodyPart();
            String messageBody = body != null ? body : "";
            bodyPart.setContent(messageBody, "text/plain;charset=utf-8");
            multipart.addBodyPart(bodyPart);

            super.setContent(multipart);

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Error encoding content as UTF-8", e);

        } catch (Exception e) {
            throw new RuntimeException("Error creating GoogleVoiceMessage", e);
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
        return message.getId();
    }

    @Override
    protected void parse(InputStream is) throws MessagingException
    {
        super.parse(is);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public Address[] getFrom() throws MessagingException {
        return new InternetAddress[]{new InternetAddress(message.getPhoneNumber())};
    }

    @Override
    public Date getReceivedDate() throws MessagingException {
        return message.getDate();
    }

    @Override
    public Address[] getRecipients(javax.mail.Message.RecipientType type)
            throws MessagingException {
        if (javax.mail.Message.RecipientType.TO.equals(type)) {
            return new InternetAddress[]{new InternetAddress(message.getPhoneNumber())};
        } else if (javax.mail.Message.RecipientType.CC.equals(type)) {
            return null;
        }  else {
            return null;
        }
    }

    @Override
    public Address[] getReplyTo() throws MessagingException {
        return new InternetAddress[]{new InternetAddress(message.getPhoneNumber())};
    }

    @Override
    public Address getSender() throws MessagingException {
        return new InternetAddress(message.getPhoneNumber());
    }

    @Override
    public Date getSentDate() throws MessagingException {
        return message.getDate();
    }

    @Override
    public int getSize() throws MessagingException {
        return message.getTitle() != null ? message.getTitle().length() : 0;
    }

    @Override
    public String getMessageID() throws MessagingException
    {
        return super.getMessageID();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public String getSubject() throws MessagingException {
        return message.getTitle();
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
     * @exception javax.mail.MessagingException
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
     * @exception javax.mail.MessagingException
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
     * @exception javax.mail.MessagingException
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
     * @exception javax.mail.MessagingException
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
     * @exception javax.mail.MessagingException
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
     * @exception javax.mail.MessagingException
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

            headers.addHeader("Date", message.getDisplayDate());
            headers.addHeader("From", message.getPhoneNumber());
            headers.addHeader("Sender", message.getPhoneNumber());
            headers.addHeader("Reply-To", message.getPhoneNumber());
            headers.addHeader("To", message.getPhoneNumber());
            headers.addHeader("Cc", "");
            headers.addHeader("Bcc", "");
            headers.addHeader("Message-Id", message.getId());
            headers.addHeader("Subject", message.getTitle());

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