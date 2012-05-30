package com.archermind.txtbl.receiver.mail.support;

import javax.activation.DataHandler;
import javax.mail.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Enumeration;

public class EmptyMessage extends Message {
    @Override
    public Address[] getFrom() throws MessagingException {
        return new Address[0];
    }

    @Override
    public void setFrom() throws MessagingException {
    }

    @Override
    public void setFrom(Address address) throws MessagingException {
    }

    @Override
    public void addFrom(Address[] addresses) throws MessagingException {
    }

    @Override
    public Address[] getRecipients(RecipientType recipientType) throws MessagingException {
        return new Address[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setRecipients(RecipientType recipientType, Address[] addresses) throws MessagingException {
    }

    @Override
    public void addRecipients(RecipientType recipientType, Address[] addresses) throws MessagingException {
    }

    @Override
    public String getSubject() throws MessagingException {
        return null;
    }

    @Override
    public void setSubject(String s) throws MessagingException {
    }

    @Override
    public Date getSentDate() throws MessagingException {
        return null;
    }

    @Override
    public void setSentDate(Date date) throws MessagingException {
    }

    @Override
    public Date getReceivedDate() throws MessagingException {
        return null;
    }

    @Override
    public Flags getFlags() throws MessagingException {
        return null;
    }

    @Override
    public void setFlags(Flags flags, boolean b) throws MessagingException {
    }

    @Override
    public Message reply(boolean b) throws MessagingException {
        return null;
    }

    @Override
    public void saveChanges() throws MessagingException {
    }

    public int getSize() throws MessagingException {
        return 0;
    }

    public int getLineCount() throws MessagingException {
        return 0;
    }

    public String getContentType() throws MessagingException {
        return null;
    }

    public boolean isMimeType(String s) throws MessagingException {
        return false;
    }

    public String getDisposition() throws MessagingException {
        return null;
    }

    public void setDisposition(String s) throws MessagingException {
    }

    public String getDescription() throws MessagingException {
        return null;
    }

    public void setDescription(String s) throws MessagingException {
    }

    public String getFileName() throws MessagingException {
        return null;
    }

    public void setFileName(String s) throws MessagingException {
    }

    public InputStream getInputStream() throws IOException, MessagingException {
        return null;
    }

    public DataHandler getDataHandler() throws MessagingException {
        return null;
    }

    public Object getContent() throws IOException, MessagingException {
        return null;
    }

    public void setDataHandler(DataHandler dataHandler) throws MessagingException {
    }

    public void setContent(Object o, String s) throws MessagingException {
    }

    public void setText(String s) throws MessagingException {
    }

    public void setContent(Multipart multipart) throws MessagingException {
    }

    public void writeTo(OutputStream outputStream) throws IOException, MessagingException {
    }

    public String[] getHeader(String s) throws MessagingException {
        return new String[0];
    }

    public void setHeader(String s, String s1) throws MessagingException {
    }

    public void addHeader(String s, String s1) throws MessagingException {
    }

    public void removeHeader(String s) throws MessagingException {
    }

    public Enumeration getAllHeaders() throws MessagingException {
        return null;
    }

    public Enumeration getMatchingHeaders(String[] strings) throws MessagingException {
        return null;
    }

    public Enumeration getNonMatchingHeaders(String[] strings) throws MessagingException {
        return null;
    }
}
