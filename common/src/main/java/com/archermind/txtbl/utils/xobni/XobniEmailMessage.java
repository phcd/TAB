package com.archermind.txtbl.utils.xobni;

import com.archermind.txtbl.domain.Attachment;
import com.archermind.txtbl.domain.Email;
import com.archermind.txtbl.domain.EmailPojo;
import com.archermind.txtbl.utils.StringUtils;
import com.archermind.txtbl.utils.UtilsTools;
import org.jboss.logging.Logger;
import org.json.simple.JSONObject;

import java.util.*;

//TODO - Paul - move to gson
public class XobniEmailMessage {


    private JSONObject xobMsg;
    private static Logger log = Logger.getLogger(XobniEmailMessage.class);

    @SuppressWarnings("unchecked")
    public XobniEmailMessage(EmailPojo emailPojo)
    {
        xobMsg = new JSONObject();

        Email inputEmail = emailPojo.getEmail();

        //desired:  "from":{"name" : "Oron Nadiv", "smtp":"oron.nadiv@xobni.net"}
        LinkedHashMap fromMap = new LinkedHashMap();
        xobMsg.put("from", fromMap);
        fromMap.put("name", inputEmail.getFrom_alias());
        fromMap.put("smtp", inputEmail.getFrom());

        //desired:  "to": [{ "name" : "Oron Nadiv","smtp" : "oron.nadiv@xobni.com"},
        //                 { "name" : "Frank Cort","smtp" : "frank.cort@xobni.com")]
        xobMsg.put("to", getRecipientList(inputEmail.getTo()));

        //desired:  "cc": [{ "name" : "Oron Nadiv","smtp" : "oron.nadiv@xobni.com"},
        //                 { "name" : "Frank Cort","smtp" : "frank.cort@xobni.com")]
        String cc = inputEmail.getCc();
        if (!StringUtils.isEmpty(cc)) {
            xobMsg.put("cc", getRecipientList(cc));
        }
        //bcc is not available on incoming mails
        xobMsg.put("type", 0); //xobni Constant for incoming mails

        //epoch in UTC
        Date mailDateAsDate = new Date();
        try {
            mailDateAsDate = inputEmail.getMailDateAsDate();
        } catch (Exception e) {
            log.warn(String.format("unable to convert maildate for mail : %s with maildate %s", inputEmail.getId(), inputEmail.getMaildate()));
        }
        xobMsg.put("delivery-time", mailDateAsDate.getTime() / 1000);

        xobMsg.put("message-size", emailPojo.getMessageSize());
        xobMsg.put("subject", inputEmail.getSubject());
        xobMsg.put("account", inputEmail.getOriginal_account());
        xobMsg.put("message-id", inputEmail.getMessageId());

        //String bodyAsString = body != null ? new String(body.getData()) : null;
        //xobMsg.put("body", bodyAsString);

        List<JSONObject> attachments = getAttachmentList(emailPojo.getAttachement());
        if (!UtilsTools.isEmpty(attachments)) {
            xobMsg.put("attachments", attachments);
        }

    }

    @SuppressWarnings("unchecked")
    private List<JSONObject> getAttachmentList(List<Attachment> attachments) {
        List<JSONObject> attachmentList = new ArrayList<JSONObject>();
        if (!UtilsTools.isEmpty(attachments)) {
            for (Attachment attachment : attachments) {
                String name = attachment.getName();
                int size = attachment.getSize();
                JSONObject attachmentObject = new JSONObject();
                attachmentObject.put("name", name);
                attachmentObject.put("size", size);
                attachmentList.add(attachmentObject);
            }
        }
        return attachmentList;
    }

    @SuppressWarnings("unchecked")
    private List<JSONObject> getRecipientList(String recipients) {
        String[] recipientStrings = recipients.split(";");
        List<JSONObject> recipientList = new ArrayList<JSONObject>();
        for (String recipientString : recipientStrings) {
            JSONObject recipient = new JSONObject();
            Map<String, String> aliasAndAddressMap = StringUtils.splitAliasAndAddress(recipientString);
            String address = aliasAndAddressMap.get("address");
            if (address == null) {
                address = recipientString;
            }
            recipient.put("smtp", address);

            String alias = aliasAndAddressMap.get("alias");
            if (alias != null) {
                recipient.put("name", alias);
            }
            recipientList.add(recipient);
        }
        return recipientList;
    }

    public JSONObject getXobniMessageObject() {
        return xobMsg;
    }

}
