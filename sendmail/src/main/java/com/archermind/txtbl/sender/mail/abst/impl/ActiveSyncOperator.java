package com.archermind.txtbl.sender.mail.abst.impl;

import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.Attachment;
import com.archermind.txtbl.domain.Email;
import com.archermind.txtbl.domain.EmailPojo;
import com.archermind.txtbl.sender.mail.abst.Operator;
import com.zynku.sync.activesync.context.ActiveSyncContext;
import com.zynku.sync.activesync.control.ActiveSyncController;
import org.bouncycastle.util.encoders.Base64;
import org.jboss.logging.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class ActiveSyncOperator extends Operator {

    private static final Logger log = Logger.getLogger(ActiveSyncOperator.class);

    private final static String LINE = "\n";
    private final static String BOUNDARY = "--frontier";


    private String deviceId;
    private String deviceType;


    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    @Override
    public String sendMail(List<EmailPojo> list) {
        Account account = null;
        String failureId = null;
        try {
            account = list.get(0).getAccount();
            ActiveSyncController controller = getController(account, deviceId, deviceType);
            for (EmailPojo emailPojo : list) {
                try {
                    controller.send(getEmailMessage(emailPojo));
                    log.info("[" + account.getName() + "] [sending success] [" + emailPojo.getEmail().getMailid() + "]");
                    updateMailFlag(emailPojo.getEmail().getMailid());
                }
                catch (Exception e) {
                    Email email = emailPojo.getEmail();
                    failureId = failureId == null ? String.valueOf(email.getMailid()) : failureId + "," + email.getMailid();
                    log.error("[" + account.getName() + "] [sending failed] [" + failureId + "] " + e.getMessage());
                }
            }
        }
        catch (Exception e) {
            log.error("[" + account.getName() + "] [sending failed] " + e.getMessage());
            for (EmailPojo emailPojo : list) {
                Email email = emailPojo.getEmail();
                failureId = failureId == null ? String.valueOf(email.getMailid()) : failureId + "," + email.getMailid();
            }
        }

        return failureId;
    }

    private String getEmailMessage(EmailPojo emailPojo) {

        StringBuffer buffer = new StringBuffer("From: " + LINE);

        if (emailPojo.getEmail().getTo() != null && !"".equals(emailPojo.getEmail().getTo().trim())) {
            buffer.append("To: ").append(emailPojo.getEmail().getTo().trim()).append(LINE);
        }
        if (emailPojo.getEmail().getCc() != null && !"".equals(emailPojo.getEmail().getCc().trim())) {
            buffer.append("Cc: ").append(emailPojo.getEmail().getCc().trim()).append(LINE);
        }
        if (emailPojo.getEmail().getBcc() != null && !"".equals(emailPojo.getEmail().getBcc().trim())) {
            buffer.append("Bcc: ").append(emailPojo.getEmail().getBcc().trim()).append(LINE);
        }

        if (emailPojo.getEmail().getReply() != null && !"".equals(emailPojo.getEmail().getReply().trim())) {
            buffer.append("Reply-To: ").append(emailPojo.getEmail().getBcc().trim()).append(LINE);
        }

        String subject = emailPojo.getEmail().getSubject();
        //log.debug("Sending email " + bean.getEmail().getId() + " with subject: " + subject);
        if (subject != null) {
            if (subject.length() > 250) {
                log.warn("Subject length > 250 characters, subject will be lost: " + emailPojo.getEmail().getId()
                        + " with length: " + subject.length() + " with subject: " + subject);
                subject = subject.substring(0, 245) + "...";
            }
        }
        buffer.append("Subject: ").append(subject).append(LINE);
        buffer.append("Content-Type: multipart/mixed; boundary=\"frontier\"").append(LINE);
        // fill in text
        buffer.append(BOUNDARY).append(LINE).append(LINE);
        buffer.append(new String(emailPojo.getBody().getData())).append(LINE);
        buffer.append(BOUNDARY).append(LINE);


        if (emailPojo.getAttachement() != null) {
            for (Attachment attachment : emailPojo.getAttachement()) {

                buffer.append("Content-Type: application/octet-stream; name=").append(attachment.getName()).append(LINE);
                buffer.append("Content-Transfer-Encoding: base64").append(LINE).append(LINE);
                buffer.append(new String(Base64.encode(attachment.getData()))).append(LINE);
                buffer.append(BOUNDARY).append(LINE);
            }
        }

        return buffer.toString();
    }

    private ActiveSyncController getController(Account account, String deviceId, String deviceType) throws MalformedURLException {
        if (log.isTraceEnabled())
            log.trace(String.format("getController(account=%s, deviceId=%s, deviceType=%s)", String.valueOf(account), deviceId, deviceType));

        ActiveSyncContext activeSyncContext = new ActiveSyncContext();
        activeSyncContext.setDeviceId(deviceId);
        activeSyncContext.setDeviceType(deviceType);

        //String loginName = account.getLoginName().indexOf('@') > 0 ? account.getLoginName().substring(0, account.getLoginName().indexOf('@')) : account.getLoginName();
        //activeSyncContext.setUserName(loginName);

        String userName = account.getLoginName();
        if (userName.indexOf('@') < 0) {
            int backslashPosition = userName.indexOf("\\");
            if (-1 != backslashPosition) {
                activeSyncContext.setUserName(userName.substring(backslashPosition+1, userName.length()));
                activeSyncContext.setDomain(userName.substring(0, backslashPosition));
            } else {
                activeSyncContext.setUserName(userName);
            }
        } else {
            activeSyncContext.setUserName(account.getLoginName().substring(0, account.getLoginName().indexOf('@')));
        }


        activeSyncContext.setPassword(account.getPassword());
        activeSyncContext.setServerURL(new URL(("ssl".equals(account.getSendTs()) ? "https" : "http") + "://" + account.getSendHost()));

        return new ActiveSyncController(activeSyncContext, 230000);
    }

}
