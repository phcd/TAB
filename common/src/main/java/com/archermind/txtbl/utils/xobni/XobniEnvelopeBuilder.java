package com.archermind.txtbl.utils.xobni;

import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.EmailPojo;
import com.archermind.txtbl.utils.StringUtils;
import org.jboss.logging.Logger;
import org.json.simple.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class XobniEnvelopeBuilder {

    private static Logger log = Logger.getLogger(XobniEnvelopeBuilder.class);

    @SuppressWarnings("unchecked")
    public JSONObject getEnvelope(Account account, String folder, List<EmailPojo> emails, Boolean finalBatch, Integer emailsInInbox, Integer totalEmailsUploadedSoFar) {
        List xobMessages = new LinkedList();
        for (EmailPojo emailPojo : emails) {
            XobniEmailMessage xobSync = new XobniEmailMessage(emailPojo);
            xobMessages.add(xobSync.getXobniMessageObject());
        }

        JSONObject returnObj = new JSONObject();
        log.debug("Adding receiver for account : " + account.getName() + " with receive protocol type : " + account.getReceiveProtocolType());
        setReceiver(account, returnObj);
        returnObj.put("folder", folder);
        if(finalBatch != null) {
            returnObj.put("initial_upload", finalBatch ? "complete" : "partial");
        }

        if(emailsInInbox != null) {
            returnObj.put("emailsInInbox", emailsInInbox);
        }

        if(totalEmailsUploadedSoFar != null) {
            returnObj.put("totalEmailsUploaded", totalEmailsUploadedSoFar + emails.size());
        }
        returnObj.put("emails", xobMessages);

        return returnObj;
    }

    @SuppressWarnings("unchecked")
    private void setReceiver(Account account, JSONObject returnObj) {
        String receiver = XobniUtil.getReceiver(account);
        log.debug("Adding receiver for account: " + account.getName() + " receiver : " + receiver);
        if(!StringUtils.isEmpty(receiver)) {
            returnObj.put("receiver", receiver);
        }
    }
}
