package com.archermind.txtbl.receiver.mail.support;

import com.archermind.txtbl.dal.business.impl.UserService;
import com.archermind.txtbl.domain.*;
import com.archermind.txtbl.parser.MessageParser;
import com.archermind.txtbl.utils.*;
import org.apache.log4j.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OriginalReceivedEmailMapper {

    private static OriginalReceivedEmailMapper originalReceivedEmailMapper =null;
    private MessageParser parser = null;
    private LinkProcessor linkProcessor = null;

    private static final int subjectSize = Integer.valueOf(SysConfigManager.instance().getValue("receiver.mail.subject.size", "500"));
    private static final boolean isTransliterateEmail = Boolean.parseBoolean(SysConfigManager.instance().getValue("transliterateEmail", "true"));
    private static final boolean isTransliterateEmailPeekvetica = Boolean.parseBoolean(SysConfigManager.instance().getValue("transliterateEmailPeekvetica", "true"));
    private static final Logger log = Logger.getLogger(OriginalReceivedEmailMapper.class);
    private static final int bodySize = Integer.valueOf(SysConfigManager.instance().getValue("receiver.mail.body.size", "524288"));


    public OriginalReceivedEmailMapper(){
        parser = new MessageParser();
        linkProcessor = LinkProcessor.getInstance();

    }

    public static OriginalReceivedEmailMapper getInstance() {
        if (originalReceivedEmailMapper ==null) {
            originalReceivedEmailMapper = new OriginalReceivedEmailMapper();
        }
        return originalReceivedEmailMapper;
    }

    /*********************************
     *
     * Provides an email with header, body, attachments, and links
     *
     */
    public OriginalReceivedEmail getFullyLoaded(Account account,Message message,String messageId,boolean saveBodies, boolean fetchAttachments, String context){


        Device device = new UserService().getDeviceById(account.getUser_id());

        OriginalReceivedEmail original = getHeader(account, device, message,messageId, fetchAttachments, context);
        if(saveBodies){
            byte[] body = new byte[0];
            try {
                PartnerCode partnerCode = account.getPartnerCode();

                String encoding = account.isXobniAccount() || ClientUtils.doesDeviceAcceptUTF8(partnerCode, device.getClientsw()) ? "UTF-8" : "ISO-8859-1";
                body = MailUtils.getBody(message, account, device).getBytes(encoding);
                log.info(String.format("loading mail of body size %d and encoding %s", body.length, encoding));
                if (body.length > bodySize) {
                    log.warn(String.format("Body content length is greater than %s. Message will not be saved for %s", bodySize, context));
                    body = "".getBytes(encoding);
                }
            } catch (Exception e) {
                log.error(String.format("Exception in processing body %s with exception %s", context, e.getMessage()));
            }
            original.setBody(body);
            original.getAttachList().addAll(getLinksAsAttachments(body.toString()));
        }

        return original;


    }


    public List<OriginalReceivedAttachment> getAttachments(Message message, Account account, String messageId,String context) {

        List<OriginalReceivedAttachment> attachmentsList;
        try{
            attachmentsList = parser.parseMsgAttach(message,account.getName(), messageId, !account.isXobniAccount());
        }catch(Exception e){
            //return empty list
            attachmentsList = new ArrayList<OriginalReceivedAttachment>();
            log.error(String.format("Exception in processing attachments %s with exception %s",context,e.getMessage()));

        }

        return attachmentsList;

    }


    public List<OriginalReceivedAttachment> getLinksAsAttachments(String messageBody){

        List<OriginalReceivedAttachment> linksList;
        try{
            linksList = linkProcessor.createLinkAttachments(messageBody);
        }catch(Exception e){
            //On exception return empty list
            linksList = new ArrayList<OriginalReceivedAttachment>();
        }

        return linksList;

    }

    public  OriginalReceivedEmail getHeader(Account account, Device device, Message message, String messageId, boolean fetchAttachments, String context) {


        OriginalReceivedEmail original = new OriginalReceivedEmail();

        original.setMail_type(messageId);
        original.setCc(parser.parseMsgAddress(message, "CC", true));
        original.setBcc(parser.parseMsgAddress(message, "BCC", true));
        original.setSent(isSentMessage(account, message));
        original.setMessageSize(getMessageSize(message, messageId, context));


        String to = parser.parseMsgAddress(message, "TO", true);
        to = StringUtils.isEmpty(to) ? account.getName() : to;
        original.setEmailTo(to);

        boolean isTransliterateEmail = !account.isXobniAccount() && ClientUtils.isDeviceAPeek(device) && OriginalReceivedEmailMapper.isTransliterateEmail;
        boolean isTransliterateEmailPeekvetica = !account.isXobniAccount()  && ClientUtils.isDeviceAPeek(device) && OriginalReceivedEmailMapper.isTransliterateEmailPeekvetica;

        log.debug(String.format("isTransliterateEmail=%b, isTransliterateEmailPeekvetica=%b, clientsw=%s", isTransliterateEmail, isTransliterateEmailPeekvetica, device.getClientsw()));
        
        original.setEmailFrom(parser.parseMsgAddress(message, "FROM", false));
        original.setEmailFromAlias(MailUtils.clean(parser.parseMsgAddressAlias(message, "FROM"), isTransliterateEmail, isTransliterateEmailPeekvetica));
        original.setReply(parser.parseReplyTo(message));

        // database can't handle more then 128 and altering table is very difficult. temporary workaround.
        if (StringUtils.isNotEmpty(original.getReply()) && original.getReply().length() > 128) {
            original.setReply("");
        }

        original.setUid(messageId);

        original.setUserId(account.getUser_id());
        original.setMailTime(ReceiverUtilsTools.dateToStr(getSentDate(message)));
        original.setSubject(MailUtils.clean(ReceiverUtilsTools.subSubject(parser.parseMsgSubject(message), subjectSize), isTransliterateEmail, isTransliterateEmailPeekvetica));

        if(fetchAttachments) {
            original.setAttachList(getAttachments(message,account,messageId,context));
        }

        return original;
    }

    private int getMessageSize(Message message, String messageId, String context) {
        try {
            return message.getSize();
        } catch (MessagingException e) {
            log.error("Error while getting message size for " + context + " messageId: " + messageId,  e );
            return 0;
        }
    }

    protected Date getSentDate(Message message){

        Date date;
        try {
            date = message.getSentDate();
        } catch (MessagingException e) {
            date = new Date();
        }

        return date;
    }

    protected boolean isSentMessage(Account account, Message message) {

        String from = parser.parseMsgAddress(message, "FROM", false);

        if (from.contains(account.getName())) {
            String to = parser.parseMsgAddress(message, "TO", true);
            String cc = parser.parseMsgAddress(message, "CC", true);

            if (!to.contains(account.getName()) && !cc.contains(account.getName())) {
                return true;
            }
        }

        return false;
    }

}
