package com.archermind.txtbl.receiver.mail.support;

import com.archermind.txtbl.domain.*;
import com.archermind.txtbl.utils.ReceiverUtilsTools;
import com.archermind.txtbl.utils.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class EmailPojoMapper {
    private static final Logger log = Logger.getLogger(EmailPojoMapper.class);

    private static EmailPojoMapper instance = new EmailPojoMapper();

    private EmailPojoMapper() {}

    public static EmailPojoMapper getInstance() {
        return instance;
    }

    public EmailPojo getEmailPojo(OriginalReceivedEmail original, String mailbox, String fromAlias) throws Exception {
        if (mailbox.endsWith("hotmail.com") || mailbox.endsWith("msn.com") || mailbox.endsWith("live.com")) {
            byte[] bodyContent = original.getBody();
            original.setBody(ReceiverUtilsTools.bodyFilter(bodyContent).getBytes("UTF-8"));
        }

        Email email = new Email();
        email.setSubject(original.getSubject());
        email.setFrom(original.getEmailFrom());
        email.setTo(ReceiverUtilsTools.subAddress(original.getEmailTo()));
        email.setCc(ReceiverUtilsTools.subAddress(original.getCc()));
        email.setBcc(ReceiverUtilsTools.subAddress(original.getBcc()));
        email.setMaildate(original.getMailTime());
        email.setStatus("0");
        email.setUserId(original.getUserId());
        email.setMessage_type("EMAIL");
        email.setReply(ReceiverUtilsTools.subAddress(original.getReply()));
        email.setSent(original.isSent());
        email.setMessageId(original.getUid());

        String fromAddr = original.getEmailFrom();

        if (StringUtils.isNotEmpty(fromAlias)) {
            if (fromAlias.length() > 120) {
                fromAlias = fromAlias.substring(120) + "...";
            }
            email.setFrom_alias(fromAlias);
        } else if (fromAddr.contains("@")) {
            email.setFrom_alias(fromAddr.split("@")[0]);
        } else {
            email.setFrom_alias(fromAddr);
        }

        email.setOriginal_account(mailbox);

        EmailPojo emailPojo = new EmailPojo();
        emailPojo.setEmail(email);
        emailPojo.setMessageSize(original.getMessageSize());


        List<Attachment> list = new ArrayList<Attachment>();

        List<OriginalReceivedAttachment> allAttachList = original.getAttachList();

        for (OriginalReceivedAttachment attach : allAttachList) {
            if (log.isDebugEnabled()) log.debug(String.format("processing attachment %s", attach));

            Attachment attachment = new Attachment();
            attachment.setName(attach.getName());
            attachment.setSize(attach.getId());
            attachment.setData(attach.getData());

            list.add(attachment);
        }

        emailPojo.setAttachement(list);


        if (original.getBody() != null) {
            Body body = new Body();
            body.setData(original.getBody());

            email.setBodySize(body.getData().length);
            emailPojo.setBody(body);
            emailPojo.setAttachement(list);
        }


        return emailPojo;
    }

}
