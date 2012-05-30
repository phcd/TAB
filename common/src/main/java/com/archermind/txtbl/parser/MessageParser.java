package com.archermind.txtbl.parser;

import com.archermind.txtbl.domain.OriginalReceivedAttachment;
import com.archermind.txtbl.utils.EncodeHandler;
import com.archermind.txtbl.utils.ReceiverUtilsTools;
import com.archermind.txtbl.utils.StringUtils;
import com.archermind.txtbl.utils.SysConfigManager;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.jboss.logging.Logger;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.ParseException;
import javax.mail.util.SharedByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageParser {
    private static final Logger log = Logger.getLogger(MessageParser.class);

    private static final String IMG_ATTACHMENT_EXTENSION = ".jpg";
    private static final String TXT_ATTACHMENT_EXTENSION = ".txt";
    private static final int MAX_ATTACHMENTS_PER_EMAIL = Integer.valueOf(SysConfigManager.instance().getValue("maxAttachmentsPerEmail", "5"));
    private static final int MAX_ATTACHMENT_NAME_LENGTH = Integer.valueOf(SysConfigManager.instance().getValue("maxAttachmentNameLength", "10"));
    private static final Pattern LINK_PATTERN = Pattern.compile("(http[s]*://[a-zA-Z_0-9$_.+!*'()\\-/\\?:@=&]+)", Pattern.CASE_INSENSITIVE);
    private static final String UTF_8_ENCODING = "UTF-8";
    private static final String WINMAIL_DAT = "winmail.dat";

    public String parseMsgSubject(Message msg)  {
        String subject = "";
        try {
            subject = msg.getSubject() == null ? "" : MimeUtility.decodeText(msg.getSubject());
        } catch (Exception e) {
            try{
                if (msg.getSubject().toLowerCase().contains("utf-7")) {
                    subject = new EncodeHandler().parseMsgSubject(msg);
                } else {
                    if (e.getMessage().startsWith("Unknown encoding")) {
                        msg.removeHeader("content-transfer-encoding");
                        subject = msg.getSubject() == null ? "" : MimeUtility.decodeText(msg.getSubject());
                    }
                }
            }catch(Exception lastEx){
                subject = "";
            }

        }


        return subject;
    }

    public String parseMsgAddress(Message msg, String type, boolean includeAlias) {
        InternetAddress[] internetAddress = null;
        try {
            if ("FROM".equals(type)) {
                internetAddress = (InternetAddress[]) msg.getFrom();
            } else if ("TO".equals(type)) {
                internetAddress = (InternetAddress[]) msg.getRecipients(Message.RecipientType.TO);
            } else if ("CC".equals(type)) {
                internetAddress = (InternetAddress[]) msg.getRecipients(Message.RecipientType.CC);
            } else if ("BCC".equals(type)) {
                internetAddress = (InternetAddress[]) msg.getRecipients(Message.RecipientType.BCC);
            }
            if (internetAddress != null && internetAddress.length > 0) {
                List<String> addresses = new ArrayList<String>();
                for (InternetAddress internetAddres : internetAddress) {
                    String address = includeAlias ? internetAddres.toUnicodeString(): internetAddres.getAddress();
                    addresses.add(address);
                }
                return org.apache.commons.lang.StringUtils.join(addresses, ";");
            }
        }
        catch (AddressException e)
        {
            log.warn(String.format("Can't parse recipients value for type %s, callers should have defaulting logic [%s]", type, ExceptionUtils.getMessage(e)));
        } catch (MessagingException msgEx){
            log.warn(String.format("Can't parse recipients value for type %s, exception [%s]", type, ExceptionUtils.getMessage(msgEx)));

        }
        return "";
    }

    public String parseReplyTo(Message msg) {
        String addressStr = "";

        Address[] addresses;
        try {
            addresses = msg.getReplyTo();
        } catch (MessagingException e){
            log.warn(String.format("Can't parse reply to, exception [%s]", ExceptionUtils.getMessage(e)));
            return "";
        }

        if (addresses != null && addresses.length > 0) {
            for (Address address : addresses) {
                String addressText = address.toString();

                if (addressText.contains("<") && addressText.contains(">") && addressText.indexOf("<") < addressText.indexOf(">")) {
                    addressText = addressText.substring(addressText.indexOf("<")+1, addressText.indexOf(">"));
                }

                if ("".equals(addressStr)) {
                    addressStr = addressText;
                } else {
                    addressStr += ";" + addressText;
                }
            }
        }

        return addressStr;
    }

    public String parseMsgAddressAlias(Message msg, String type) {
        String addressAlias = "";
        InternetAddress[] internetAddress;
        try{
            if ("FROM".equals(type)) {
                internetAddress = (InternetAddress[]) msg.getFrom();
            } else {
                if ("TO".equals(type)) {
                    internetAddress = (InternetAddress[]) msg.getRecipients(Message.RecipientType.TO);
                } else {
                    if ("CC".equals(type)) {
                        internetAddress = (InternetAddress[]) msg.getRecipients(Message.RecipientType.CC);
                    } else {
                        internetAddress = (InternetAddress[]) msg.getRecipients(Message.RecipientType.BCC);
                    }
                }
            }

            if (internetAddress != null && internetAddress.length > 0) {
                for (InternetAddress internetAddres : internetAddress) {
                    if ("".equals(addressAlias)) {
                        addressAlias = internetAddres.getPersonal();
                    } else {
                        addressAlias += ";" + internetAddres.getPersonal();
                    }
                }
            }

        } catch (MessagingException e){
            log.warn(String.format("Can't parse alias for type %s, exception %s", type,ExceptionUtils.getMessage(e)));
            addressAlias="";
        }
        return addressAlias;
    }

    public String parseMsgContent(Message msg, boolean convertHtml) throws Exception {
        String content = "";

        if (msg.isMimeType("multipart/*")) {
            if (msg.isMimeType("multipart/alternative")) {
                content = parseMsgContent((Multipart) msg.getContent(), convertHtml);
            } else {
                Multipart multipart = (Multipart) msg.getContent();
                int bodyPartCount = multipart.getCount();
                for (int i = 0; i < bodyPartCount; i++) {
                    content += parseMsgContent(multipart.getBodyPart(i), convertHtml);
                }
            }
        } else {
            if (msg.isMimeType("text/plain")) {
                content = parseMsgContent(msg, "plain", true, convertHtml);
            } else {
                if (msg.isMimeType("text/html")) {
                    content = ReceiverUtilsTools.replace(parseMsgContent(msg, "html", true, convertHtml));
                }
            }
        }
        return content;
    }

    public List<OriginalReceivedAttachment> parseMsgAttach(Message msg, String mailbox, String msgID) throws Exception {
        return parseMsgAttach(msg, mailbox, msgID, true);    
    }

    public List<OriginalReceivedAttachment> parseMsgAttach(Message msg, String mailbox, String msgID, boolean fetchFullAttachment) throws Exception {
        List<OriginalReceivedAttachment> list = new ArrayList<OriginalReceivedAttachment>();
        if (msg.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) msg.getContent();
            int bodyPartCount = multipart.getCount();
            for (int i = 0; i < bodyPartCount; i++) {
                parseMsgAttach(multipart.getBodyPart(i), list, mailbox, msgID, fetchFullAttachment);
            }
        }
        return list;
    }


    public List<OriginalReceivedAttachment> createLinkAttachments(String message) throws Exception {
        List<OriginalReceivedAttachment> attachments = new ArrayList<OriginalReceivedAttachment>();
        Matcher matcher = LINK_PATTERN.matcher(message);
        int index = 0;
        while (matcher.find(index) && attachments.size() < MAX_ATTACHMENTS_PER_EMAIL) {
            String url = matcher.group(1);
            OriginalReceivedAttachment attachment = new OriginalReceivedAttachment();
            String extension = isImageResource(url) ? IMG_ATTACHMENT_EXTENSION : TXT_ATTACHMENT_EXTENSION;
            String attachName = StringUtils.getNameFromLink(url, MAX_ATTACHMENT_NAME_LENGTH);
            attachment.setName(attachName + "_" + index + extension);
            attachment.setData(("LINK:" + url).getBytes(UTF_8_ENCODING));
            attachment.setSize(attachment.getData() == null ? 0 : attachment.getData().length);
            attachments.add(attachment);
            index = matcher.end();
        }

        return attachments;
    }


    private boolean isImageResource(String url) {
        boolean isImage = false;

        try {
            if (url.contains("twitpic.com")) {
                isImage = true;
            } else if (hasImageExtension(url)) {
                isImage = true;
            }
        } catch (Throwable t) {
            log.warn(String.format("Unable to determine if url %s points to an image resource", url), t);
        }

        return isImage;
    }

    private boolean hasImageExtension(String rsc) {
        rsc = rsc.toLowerCase();
        return rsc != null && (rsc.endsWith(".jpg") || rsc.endsWith(".jpeg") || rsc.endsWith(".gif") || rsc.endsWith(".png"));
    }

    private String parseMsgContent(BodyPart bodyPart, boolean convertHtml) throws Exception {
        String content = "";
        if (bodyPart.isMimeType("text/plain") && bodyPart.getFileName() == null && !Part.ATTACHMENT.equals(bodyPart.getDisposition())) {
            content = parseMsgContent(bodyPart, "plain", true, convertHtml);
        } else {
            if (bodyPart.isMimeType("text/html") && bodyPart.getFileName() == null && !Part.ATTACHMENT.equals(bodyPart.getDisposition())) {
                content = ReceiverUtilsTools.replace(parseMsgContent(bodyPart, "html", true, convertHtml));
            } else {
                if (bodyPart.isMimeType("multipart/*")) {
                    if (bodyPart.isMimeType("multipart/alternative")) {
                        content = parseMsgContent((Multipart) bodyPart.getContent(), convertHtml);
                    } else {
                        Multipart multipart = (Multipart) bodyPart.getContent();
                        int bodyPartCount = multipart.getCount();
                        for (int i = 0; i < bodyPartCount; i++) {
                            content += parseMsgContent(multipart.getBodyPart(i), convertHtml);
                        }
                    }
                }
            }
        }
        return content;
    }

    private String parseMsgContent(Multipart multipart, boolean convertHtml) throws Exception {
        String content = "";
        String contentHtml = null;
        String contentPlain = null;
        int bodyPartCount = multipart.getCount();
        for (int i = 0; i < bodyPartCount; i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                contentPlain = parseMsgContent(bodyPart, "plain", true, convertHtml);
            } else if (bodyPart.isMimeType("text/html")) {
                contentHtml = ReceiverUtilsTools.replace(parseMsgContent(bodyPart, "html", true, convertHtml));
                /** multipart/alternative or multipart/related */
            } else if (bodyPart.isMimeType("multipart/*")) {
                content = parseMsgContent(bodyPart, convertHtml);
            }
        }
        if (contentPlain != null) {
            content += contentPlain;
        } else if (contentHtml != null) {
            content += contentHtml;
        }
        return content;
    }

    private void parseMsgAttach(BodyPart bodyPart, List<OriginalReceivedAttachment> list, String mailbox, String msgID, boolean fetchFullAttachment) throws MessagingException, IOException {
        if (bodyPart.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) bodyPart.getContent();
            int bodyPartCount = multipart.getCount();
            for (int i = 0; i < bodyPartCount; i++) {
                parseMsgAttach(multipart.getBodyPart(i), list, mailbox, msgID, fetchFullAttachment);
            }
        } else {
            try {
                if (bodyPart.getFileName() != null && !bodyPart.isMimeType("application/ms-tnef") && !bodyPart.getFileName().equalsIgnoreCase(WINMAIL_DAT)) {
                    createAttach(bodyPart, list, mailbox, msgID, fetchFullAttachment);
                }
            } catch (ParseException ex) {
                log.warn("unable to prase attachment name " + ex.toString(), ex);

                Enumeration enumer = bodyPart.getAllHeaders();

                while (enumer.hasMoreElements()) {
                    Header header = (Header)enumer.nextElement();
                    log.warn(String.format("unable to prase attachment name, suspected header %s=%s", header.getName(), header.getValue()));
                }

            }

        }
    }

    private void createAttach(BodyPart bodyPart, List<OriginalReceivedAttachment> list, String mailbox, String msgID, boolean fetchFullAttachment) {
        try {
            OriginalReceivedAttachment bean = new OriginalReceivedAttachment();

            log.info("...creating attachment");

            String filename = bodyPart.getFileName() == null ? "" : MimeUtility.decodeText(bodyPart.getFileName());

            if(fetchFullAttachment) {
                int attachmentFileSizeLimit = Integer.valueOf(SysConfigManager.instance().getValue("attachmentFileSizeLimit", "30"));
                filename = StringUtils.getShortFileName(filename, attachmentFileSizeLimit);
            }

            log.info("\t...fileName=" + filename);
            bean.setName(filename);

            int fileSize = bodyPart.getSize();

            if(fetchFullAttachment) {
                int attachSize = Integer.parseInt(SysConfigManager.instance().getValue("attachSize","10485760"));
                byte[] tempData = new byte[attachSize + 1];

                fileSize = bodyPart.getInputStream().read(tempData, 0, attachSize + 1);
                if (fileSize < 0) {
                    fileSize = 0;
                }
                if (fileSize <= attachSize) {
                    byte[] fileByte = new byte[fileSize];
                    System.arraycopy(tempData, 0, fileByte, 0, fileSize);
                    bean.setData(fileByte);
                    bean.setSize(fileByte.length);
                }
            }

            log.info("\t...fileSize=" + fileSize);
            bean.setId(fileSize);

            list.add(bean);

            log.info("Successfully created attachment for [" + mailbox + "]" + " [" + msgID + "]: " + bean);
        } catch (Exception e) {
            log.error("createAttach/MessageParser/Exception: [" + mailbox + "]" + " [" + msgID + "]", e);
        }
    }

    private String parseMsgContent(Message msg, String contentType, boolean recursionFlag, boolean convertHtml) throws Exception {
        String content = "";
        try {
            content = getContent(msg.getContent(), contentType, convertHtml);
        } catch (Exception e) {
            if (msg.getContentType().toLowerCase().contains("utf-7")) {
                content = getContent(msg, contentType, convertHtml);
            } else {
                if (recursionFlag) {
                    if (e.getMessage() != null && e.getMessage().startsWith("Unknown encoding")) {
                        msg.removeHeader("content-transfer-encoding");
                        content = parseMsgContent(msg, contentType, false, convertHtml);
                    }
                } else {
                    throw e;
                }
            }
        }
        return content;
    }

    private String parseMsgContent(BodyPart bodyPart, String contentType, boolean recursionFlag, boolean convertHtml) throws Exception {
        String content = "";
        try {
            content = getContent(bodyPart.getContent(), contentType, convertHtml);
        } catch (Exception e) {
            if (bodyPart.getContentType().toLowerCase().contains("utf-7")) {
                content = getContent(bodyPart, contentType, convertHtml);
            } else {
                if (recursionFlag) {
                    if (e.getMessage() != null && e.getMessage().startsWith("Unknown encoding")) {
                        bodyPart.removeHeader("content-transfer-encoding");
                        content = parseMsgContent(bodyPart, contentType, false, convertHtml);
                    }
                } else {
                    throw e;
                }
            }
        }
        return content;
    }

    private String getContent(Object contentObj, String contentType, boolean convertHtml) throws Exception {
        if ("html".equals(contentType) && convertHtml) {
            if(contentObj instanceof BodyPart) {
                return ReceiverUtilsTools.htmToTxt(new EncodeHandler().parseMsgContent((BodyPart) contentObj));
            }

            if(contentObj instanceof Message) {
                return ReceiverUtilsTools.htmToTxt(new EncodeHandler().parseMsgContent((Message) contentObj));
            }

            if (contentObj instanceof SharedByteArrayInputStream) {
                return ReceiverUtilsTools.htmToTxt(new String(inputStreamToBytes((SharedByteArrayInputStream) contentObj)));
            }

            return ReceiverUtilsTools.htmToTxt((String) contentObj);
        } else {
            if(contentObj instanceof BodyPart) {
                return new EncodeHandler().parseMsgContent((BodyPart) contentObj);
            }

            if(contentObj instanceof Message) {
                return new EncodeHandler().parseMsgContent((Message) contentObj);
            }

            if (contentObj instanceof SharedByteArrayInputStream) {
                return new String(inputStreamToBytes((SharedByteArrayInputStream) contentObj));
            }

            return (String) contentObj;
        }
    }

    private byte[] inputStreamToBytes(InputStream is) throws Exception {
        byte[] readed = null;
        int totallength = 0;
        byte[] readedtemp = new byte[1024];
        int truelen;
        while ((truelen = is.read(readedtemp, 0, readedtemp.length)) != -1) {
            totallength += truelen;
            if (totallength - truelen > 0) {
                byte[] newReaded = new byte[totallength];
                System.arraycopy(readed, 0, newReaded, 0, totallength - truelen);
                readed = newReaded;
            } else
                readed = new byte[truelen];
            System.arraycopy(readedtemp, 0, readed, totallength - truelen, truelen);
        }
        return readed;
    }
}
