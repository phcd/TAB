package com.archermind.txtbl.dal.orm;

import com.archermind.txtbl.attachment.format.support.PDFHelper;
import com.archermind.txtbl.attachmentsvc.msg.AttachmentSvcMessage;
import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.domain.*;
import com.archermind.txtbl.utils.*;
import org.apache.commons.lang.ArrayUtils;
import org.jboss.logging.Logger;
import org.jets3t.service.S3ServiceException;

import java.io.Serializable;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ReceivedORMap extends BaseORMap {
    private static final Logger log = Logger.getLogger(ReceivedORMap.class);

    public ReceivedORMap(boolean useMaster) {
        super(useMaster);
    }

    public ReceivedORMap() {}

    public int saveEmail(EmailPojo ep, OriginalReceivedEmail ore) throws DALException {
        MySQLEncoder.encode(ep.getEmail());

        int result = 0;
        int lastAttachId = 0;
        long ll_time = System.currentTimeMillis();
        try {

            // default the received time to now if it's not already set
            // TODO - this should really be done upstream
            if (StringUtils.isEmpty(ep.getEmail().getReceivedTime())) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                ep.getEmail().setReceivedTime(formatter.format(new Date()));
            }

            log.debug("receive email startTransaction!");
            sqlMapClient.startTransaction();
            log.debug("receive email startTransaction! " + (System.currentTimeMillis() - ll_time));

            result = sqlMapClient.update("ReceivedEmail.addEmail", ep.getEmail());

            int lastId = (int) (Long.parseLong(((sqlMapClient.queryForObject("Global.getLastInsertId")).toString())));

            log.debug("receive email! " + (System.currentTimeMillis() - ll_time));
            ll_time = System.currentTimeMillis();

            if ((ep.getBody() != null) && (result == 1)) {
                ep.getBody().setEmailid(lastId);
                result = sqlMapClient.update("ReceivedEmailBody.addBody", ep.getBody());
            }

            log.debug("receive email body! "+ (System.currentTimeMillis() - ll_time));
            ll_time = System.currentTimeMillis();

            if ((ep.getAttachement() != null) && (result == 1)) {

                AttachmentORMap attachmentORMap = new AttachmentORMap();

                Iterator<Attachment> it = ep.getAttachement().iterator();
                while ((it.hasNext()) && (result == 1)) {
                    Attachment attach = it.next();
                    attach.setEmailId(lastId);

                    log.debug("receive name:" + ep.getEmail().getOriginal_account() + "  attach file  : " + attach.getName() + " msg id :" + ore.getMail_type());

                    String sAttachName = attach.getName();
                    if (sAttachName.startsWith(".")) {
                        attach.setName(sAttachName.substring(1) + "." + lastAttachId);
                    }

                    log.debug(" Calling: new AttachmentORMap(null).addReceivedAttachment(attach);");
                    log.debug("  attach: " + attach);

                    attachmentORMap.addReceivedAttachment(ep.getEmail().getReceivedDateAsDate(), attach);

                    log.debug(" ....addReceivedAttachment() returned " + result);

                    if (!sAttachName.startsWith(".")) {
                        lastAttachId = (int) (Long.parseLong(((sqlMapClient.queryForObject("Global.getLastInsertId")).toString())));
                    }
                }
            }

            log.debug("receive email attachment! " + (System.currentTimeMillis() - ll_time));
            ll_time = System.currentTimeMillis();

            log.debug("receive email pop3! " + (System.currentTimeMillis() - ll_time));
            ll_time = System.currentTimeMillis();
            if (result == 1) {
                for (OriginalReceivedAttachment tmp : ore.getAttachList()) {
                    tmp.setEmailId(lastId);

                    log.debug( " Calling: saveOriginalAttachment(tmp);");
                    log.debug("     tmp: " + tmp);

                    result = saveOriginalAttachment(ep.getEmail().getReceivedDateAsDate(), tmp);

                    log.debug(" ....saveOriginalAttachment() returned " + result);
                    //result = sqlMapClient.update("ReceivedEmail.saveOriginalAttachment", tmp);
                }
            }
            log.debug("receive email OriginalAttachment! " + (System.currentTimeMillis() - ll_time));
            ll_time = System.currentTimeMillis();
            if (result != 0) {
                sqlMapClient.commitTransaction();
                result = 1;
            }
            log.debug("receive email commitTransaction! " + (System.currentTimeMillis() - ll_time));

        } catch (Exception e) {
            result = 0;
            log.fatal("Save email error! mailname = " + ep.getEmail().getOriginal_account(), e);
            throw new DALException(ErrorCode.CODE_DAL_, e);
        } finally {
            try {
                ll_time = System.currentTimeMillis();
                sqlMapClient.endTransaction();
                log.debug("receive email endTransaction! " + (System.currentTimeMillis() - ll_time));
            } catch (SQLException e) {
                log.fatal("Save email error! mailname = " + ep.getEmail().getOriginal_account(), e);
            }
        }
        return result;
    }

    public int deleteReceivedAttachment(String date) throws Exception {
        return sqlMapClient.delete("ReceivedEmail.deleteReceivedAttachment", date);
    }

    public int deleteOriginalAttachment(String date) throws Exception {
        return sqlMapClient.delete("ReceivedEmail.deleteOriginalAttachment", date);
    }

    //TODO - Paul - need to use during user reset
    @SuppressWarnings("unused")
    public int deleteReceivedEmailByUserId(String userid) {
        try {
            sqlMapClient.delete("User.removePeek06", userid);
            sqlMapClient.delete("User.removePeek07", userid);
            sqlMapClient.delete("User.removePeek08", userid);
            return 1;
        } catch (SQLException e) {
            log.fatal("deleteReceivedEmailByUserId error!",e);
        }
        return 0;
    }

    public int deleteReceivedBody(String id) throws SQLException {
        return sqlMapClient.delete("ReceivedEmail.deleteReceivedBodyEmailId", id);
    }

    public int deleteReceivedEmail(String id) {
        try {
            sqlMapClient.delete("ReceivedEmail.deleteReceivedAttachment", id);
            sqlMapClient.delete("ReceivedEmail.deleteReceivedBodyEmailId", id);
            sqlMapClient.delete("ReceivedEmail.deleteReceivedHeadEmailId", id);
            return 1;
        } catch (SQLException e) {
            log.fatal("deleteReceivedEmail error!", e);
        }
        return 0;
    }

    public int deleteReceivedHead(String id) throws SQLException {
        return sqlMapClient.delete("ReceivedEmail.deleteReceivedHeadEmailId", id);
    }

    public Email getReceivedEmail(HashMap paras) {
        try {
            return MySQLEncoder.decode(((Email) sqlMapClient.queryForObject("ReceivedEmail.getReceivedEmail", paras)));
        } catch (SQLException e) {
            log.fatal("Get receive email error!", e);
        }
        return null;
    }

    public Email getReceivedEmailByMessageId(HashMap paras) {
        try {
            return MySQLEncoder.decode(((Email) sqlMapClient.queryForObject("ReceivedEmail.getReceivedEmailByMessageId", paras)));
        } catch (SQLException e) {
            log.fatal("Get receive email error!", e);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<Email> selectReceivedEmailDate(String date) throws SQLException {
        return MySQLEncoder.decode(sqlMapClient.queryForList("ReceivedEmail.selectReceivedEmailDate", date));
    }

    @SuppressWarnings("unchecked")
    public List<Email> selectReceivedAttachmentDate(String date) throws SQLException {
        return MySQLEncoder.decode(sqlMapClient.queryForList("ReceivedEmail.selectReceivedAttachmentDate", date));
    }

    @SuppressWarnings("unchecked")
    public List<Email> selectReceivedAttachmentMinDate(String date) throws SQLException {
        return MySQLEncoder.decode(sqlMapClient.queryForList("ReceivedEmail.selectReceivedAttachmentMinDate", date));
    }

    @SuppressWarnings("unchecked")
    public List<Email> selectReceivedAttachmentMaxDate(String date) throws SQLException {
        return MySQLEncoder.decode(sqlMapClient.queryForList("ReceivedEmail.selectReceivedAttachmentMaxDate", date));
    }

    @SuppressWarnings("unchecked")
    public List<Email> selectOriginalAttachmentDate(String date) throws SQLException {
        return MySQLEncoder.decode(sqlMapClient.queryForList("ReceivedEmail.selectOriginalAttachmentDate", date));
    }

    @SuppressWarnings("unchecked")
    public List<Email> selectOriginalAttachmentMinDate(String date) throws SQLException {
        return MySQLEncoder.decode(sqlMapClient.queryForList("ReceivedEmail.selectOriginalAttachmentMinDate", date));
    }

    @SuppressWarnings("unchecked")
    public List<Email> selectOriginalAttachmentMaxDate(String date) throws SQLException {
        return MySQLEncoder.decode(sqlMapClient.queryForList("ReceivedEmail.selectOriginalAttachmentMaxDate", date));
    }

    public int getNewEmailCount(String userId) throws SQLException {
        return Integer.parseInt( String.valueOf(sqlMapClient.queryForObject("ReceivedEmail.getNewEmailCount", userId)));
    }

    public int getNewEmailCountForAccount(String name) throws SQLException {
        return Integer.parseInt( String.valueOf(sqlMapClient.queryForObject("ReceivedEmail.getNewEmailCountForAccount", name)));
    }

    public int getDeliveredEmailCount(String userId) throws SQLException {
        return Integer.parseInt( String.valueOf(sqlMapClient.queryForObject("ReceivedEmail.getDeliveredEmailCount", userId)));
    }

    private int saveOriginalAttachment(Date mailDate, OriginalReceivedAttachment attachment) throws SQLException {
        sqlMapClient.insert("ReceivedEmail.saveOriginalAttachment", attachment);

        try
        {
            AmazonS3.getInstance().saveAttachment(mailDate, attachment);
        }
        catch (S3ServiceException e)
        {
            log.fatal("Error saving original attachment: " + e.getMessage());
        }

        return attachment.getId();
    }

    /**
     * New original attachment persistence call. TODO: Add attachment service API call
     * @param attachment
     * @return
     * @throws SQLException
     */
    private int newSaveOriginalAttachment(String userId, Account account, Date mailDate, OriginalReceivedAttachment attachment, Integer receivedAttachmentId, Collection<Integer> extraReceivedAttachmentIds) throws SQLException {

        sqlMapClient.insert("ReceivedEmail.saveOriginalAttachment", attachment);

        try {
            // Note: two side-effects of "saveAttachment" is that attachment.location, and attachment.savedOnDate are updated.
            if (! ArrayUtils.isEmpty(attachment.getData())) {
                AmazonS3.getInstance().saveAttachment(mailDate, attachment);
                log.debug("attachment saved to location: " + attachment.getLocation());
                sqlMapClient.update("ReceivedEmail.updateAttachmentLocation", attachment);
                notifyAttachmentService(userId, account, mailDate, attachment.getEmailId(), attachment.getName(), attachment.getId(), receivedAttachmentId, attachment.getData().length, extraReceivedAttachmentIds);
            }
        } catch (S3ServiceException e) {
            log.fatal("Error saving original attachment: " + e.getMessage());
        }

        return attachment.getId();
    }

    public void updateProcessedOnDate(int attachmentId, Date processedOnDate)  throws SQLException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", attachmentId);
        params.put("processedOnDate", processedOnDate);

        sqlMapClient.update("ReceivedEmail.updateProcessedOnDate", params);
    }

    @SuppressWarnings("unchecked,unused")
    public List<OriginalReceivedAttachment> getOriginalAttachments(int emailId) throws SQLException {
        List<OriginalReceivedAttachment> attachments;
        boolean dataFoundInDB = true;

        // first check DB
        attachments = sqlMapClient.queryForList("ReceivedEmail.queryOriginalAttachment", emailId);
        for (OriginalReceivedAttachment attachment : attachments) {
            if (attachment.getData() == null || attachment.getData().length == 0) {
                dataFoundInDB = false;
                break;
            }
        }

        // now check S3
        if (!dataFoundInDB) {
            try {
                attachments = AmazonS3.getInstance().retrieveOriginalAttachments(emailId);
            } catch (S3ServiceException e) {
                log.fatal("Error retrieving original attachments for email ID " + emailId + ": " + e.getMessage());
            }
        }

        return attachments;
    }

    public int newSaveEmailTwitter(EmailPojo emailPojo, OriginalReceivedEmail original) throws DALException {
        persistEmail(emailPojo);
        Email email = emailPojo.getEmail();
        try {
            if (emailPojo.getAttachement() != null) {
                long start = System.currentTimeMillis();
                int lastAttachId = 0;
                for (int attachmentIndex=0; attachmentIndex<emailPojo.getAttachement().size(); attachmentIndex++) {
                    Attachment attach = emailPojo.getAttachement().get(attachmentIndex);
                    attach.setEmailId(email.getId());
                    log.debug(String.format("processing attachment, account=%s, file=%s, mailtype=%s", email.getOriginal_account(), attach.getName(), original.getMail_type()));
                    if (attach.getName().startsWith(".")) {
                        attach.setName(attach.getName().substring(1) + "." + lastAttachId);
                    }
                    if(log.isDebugEnabled())
                        log.debug(String.format("before saving received attachment %s", attach));

                    // save to db, not send to s3
                    int attachmentId = new AttachmentORMap().newAddReceivedAttachment(attach);

                    log.debug(String.format("after saving received attachment %s", attach));

                    if (! attach.getName().startsWith(".")) {
                        // no idea what this is about but afraid to remove since it might be realated to how these are interpreted on the device
                        lastAttachId = attachmentId;
                    }

                    if (attach.getName().endsWith(".tweet")) {
                        if(log.isDebugEnabled())
                            log.debug(String.format("we have a twitter attachment, need to save to s3, emailId=%s, name=%s", email.getId(), attach.getName()));
                        attach.setName(renameToJpg(attach.getName()));
                        AmazonS3.getInstance().saveAttachment(email.getReceivedDateAsDate(), attach, AmazonS3.AttachmentType.RECEIVED);
                    }
                }
                if(log.isDebugEnabled())
                    log.debug(String.format("saved attachments in %d millis", System.currentTimeMillis() - start));
            }
        } catch (Throwable e) {
            // note that this exception is not propagating out, this is tomake sure that ID of the message does not get removed as we have already saved the email
            String message = String.format("unable to save email attachments for account=%s, uid=%s", email.getOriginal_account(), email.getUserId());

            log.fatal(message, e);
        }

        return emailPojo.getEmail().getId();
    }

    public int newSaveEmail(EmailPojo emailPojo, OriginalReceivedEmail original, Account account) throws DALException {
        Email email = emailPojo.getEmail();
        persistEmail(emailPojo);
        try {
            Map<Integer, Collection<Integer>> extraReceivedAttachments = new HashMap<Integer, Collection<Integer>>();

            if (emailPojo.getAttachement() != null) {
                long start = System.currentTimeMillis();
                int lastAttachId = 0;
                int pdfAttachmentIndex = 1;
                for (int attachmentIndex=0; attachmentIndex<emailPojo.getAttachement().size(); attachmentIndex++) {
                    Attachment attach = emailPojo.getAttachement().get(attachmentIndex);
                    attach.setEmailId(email.getId());

                    if(log.isDebugEnabled())
                        log.debug(String.format("processing attachment, account=%s, file=%s, mailtype=%s", email.getOriginal_account(), attach.getName(), original.getMail_type()));

                    if (attach.getName().startsWith(".")) {
                        attach.setName(attach.getName().substring(1) + "." + lastAttachId);
                    }

                    log.debug( String.format("before saving received attachment %s", attach));

                    // save to db, not send to s3
                    int attachmentId = new AttachmentORMap().newAddReceivedAttachment(attach);

                    log.debug( String.format("after saving received attachment %s", attach));

                    if (! attach.getName().startsWith(".")) {
                        // no idea what this is about but afraid to remove since it might be realated to how these are interpreted on the device
                        lastAttachId = attachmentId;
                    }

                    Collection<Integer> extraAttachments = new ArrayList<Integer>();

                    if (attach.getName().toUpperCase().endsWith("PDF")) {
                        // pdf, we need to count pages and create extra received attachment place holders
                        int pageCount = PDFHelper.getPageCount(original.getAttachList().get(attachmentIndex).getData());

                        log.debug( String.format("we have %d pages in this pdf document", pageCount));

                        if (pageCount > 0) {
                            int maxPdfPagesCount = PDFHelper.getMaxPdfPagesCount();

                            for (int pageIndex = 1; pageIndex < pageCount + 1 && pageIndex <= maxPdfPagesCount ; pageIndex++) {
                                Attachment jpgAttachment = new Attachment();
                                jpgAttachment.setName(PDFHelper.getImageName(attach.getName(),pdfAttachmentIndex, pageIndex)) ;
                                jpgAttachment.setEmailId(email.getId());

                                int jpgAttachmentId = new AttachmentORMap().newAddReceivedAttachment(jpgAttachment);

                                extraAttachments.add(jpgAttachmentId);

                                log.debug( String.format("created extra received attachment %d", jpgAttachmentId));

                            }
                            pdfAttachmentIndex++;
                        }
                    }

                    extraReceivedAttachments.put(attachmentId, extraAttachments);
                }

                log.debug(String.format("saved attachments in %d millis", System.currentTimeMillis() - start));
            }

            if (original.getAttachList() != null && original.getAttachList().size() > 0) {
                long start = System.currentTimeMillis();
                int index = 0;
                for (OriginalReceivedAttachment origReceivedAttachment : original.getAttachList()) {
                    origReceivedAttachment.setEmailId(email.getId());
                    // save to db, s3 and call service
                    int receivedAttachmentId = emailPojo.getAttachement().get(index).getId();
                    newSaveOriginalAttachment(email.getUserId(), account, email.getReceivedDateAsDate(), origReceivedAttachment, receivedAttachmentId, extraReceivedAttachments.get(receivedAttachmentId));
                    index++;
                }
                log.debug(String.format("saved %d original attachments in %d millis", original.getAttachList().size(), System.currentTimeMillis() - start));
            }
        } catch (Throwable e) {
            // note that this exception is not propagating out, this is tomake sure that ID of the message does not get removed as we have already saved the email
            String message = String.format("unable to save email attachments for account=%s, uid=%s", email.getOriginal_account(), email.getUserId());
            log.fatal(message, e);
        }

        return emailPojo.getEmail().getId();
    }

    private void persistEmail(EmailPojo emailPojo) throws DALException {
        Email email = emailPojo.getEmail();
        MySQLEncoder.encode(email);
        long start = System.currentTimeMillis();
        try {
            // default the received time to now if it's not already set
            // TODO - this should really be done upstream
            if (StringUtils.isEmpty(email.getReceivedTime())) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                email.setReceivedTime(formatter.format(new Date()));
            }

            log.debug( "receive email startTransaction!");

            sqlMapClient.startTransaction();

            log.debug( String.format("receive email started transaction in %s millis ", System.currentTimeMillis() - start));

            sqlMapClient.insert("ReceivedEmail.addEmail", email);

            // via some weird events original.getMail_type() actually contains a message id
            log.debug(String.format( "receive email saved in %d millis, mailId=%s", System.currentTimeMillis() - start, email.getId()));

            start = System.currentTimeMillis();

            if (emailPojo.getBody() != null) {
                emailPojo.getBody().setEmailid(email.getId());

                sqlMapClient.insert("ReceivedEmailBody.addBody", emailPojo.getBody());
            }

            log.debug( String.format("receive email body saved in %d millis", System.currentTimeMillis() - start));

            start = System.currentTimeMillis();

            sqlMapClient.commitTransaction();

            log.debug(String.format("receive email commitTransaction in %d millis", System.currentTimeMillis() - start));
        } catch (Throwable e) {
            String message = String.format("unable to save email header and/or composite for account=%s, uid=%s", email.getOriginal_account(), email.getUserId());
            throw new DALException(message, e);
        } finally {
            endTransaction(sqlMapClient);
        }
    }

    private void notifyAttachmentService(String userId, Account account, Date mailDate, Integer emailId, String originalAttachmentName, Integer originalAttachmentId, Integer receivedAttachmentId, int size, Collection<Integer> extraReceivedAttachmentIds) {
        log.debug( String.format("notifying Attachment Service for userId=%s, emailId=%s, originalAttachmentId=%s, accountName=%s", userId, emailId, originalAttachmentId, account.getName()));
        Serializable message = new AttachmentSvcMessage(account, mailDate, emailId, originalAttachmentName, originalAttachmentId, receivedAttachmentId, extraReceivedAttachmentIds, size);
        log.debug( String.format("Sending attachment info to Attachment Service, message=%s", message));

        String attachmentSvcQueueJNDI = SysConfigManager.instance().getValue("attachmentsvc.queue.jndi");
        String attachmentSvcQueueURL = getAttachmentServiceQueueURL(account);

        if (StringUtils.isNotEmpty(attachmentSvcQueueJNDI) && StringUtils.isNotEmpty(attachmentSvcQueueURL)) {
            log.debug( String.format("Sending Attachment Service message %s to %s on %s", message, attachmentSvcQueueJNDI, attachmentSvcQueueURL));
            String targetIdentifier = attachmentSvcQueueJNDI + ":" + attachmentSvcQueueURL;
            SendQueueMessageClient.getInstance(targetIdentifier, attachmentSvcQueueURL, attachmentSvcQueueJNDI).send(message);
        } else {
            log.warn(String.format("Attachment Service is missing connection parameters. attachmentsvc.queue.jndi=%s, attachmentsvc.queue.url=%s", attachmentSvcQueueJNDI, attachmentSvcQueueURL));
        }
    }

    public int newSaveEmail(Account account, Email email, Body body) throws DALException {
        MySQLEncoder.encode(email);
        long start = System.currentTimeMillis();

        try {
            log.debug( "receive email startTransaction!");
            sqlMapClient.startTransaction();
            log.debug( String.format("receive email started transaction in %s millis ", System.currentTimeMillis() - start));
            sqlMapClient.insert("ReceivedEmail.addEmail", email);

            // via some weird events original.getMail_type() actually contains a message id
            log.debug( String.format("receive email saved in %d millis, messageId=%s", System.currentTimeMillis() - start, email.getId()));
            start = System.currentTimeMillis();
            if (body != null) {
                body.setEmailid(email.getId());
                sqlMapClient.insert("ReceivedEmailBody.addBody", body);
            }

            log.debug( String.format("receive email body saved in %d millis", System.currentTimeMillis() - start));

            start = System.currentTimeMillis();

            sqlMapClient.commitTransaction();

            log.debug( String.format("receive email commitTransaction in %d millis", System.currentTimeMillis() - start));
        } catch (Throwable e) {
            String message = String.format("unable to save email header and/or composite for account=%s, uid=%s", account.getName(), account.getUser_id());
            throw new DALException(message, e);
        } finally {
            endTransaction(sqlMapClient);
        }

        return email.getId();
    }


    /**
     * Returns the Attachment Service Queue URL. We first check for sys config attachmentsvc.queue.url<.protocol> and
     * then fall back to the default attachmentsvc.queue.url
     *
     * Note: "protocol" refers to the account's receive protocol, for example: pop3, gmailpop3, twitter, etc.
     *
     * @param account The user's account, used to determine the receive protocol.
     * @return  The Attachment Service Queue URL
     */
    private String getAttachmentServiceQueueURL(final Account account) {
        final String defaultKey = "attachmentsvc.queue.url";
        String attachmentSvcQueueURL = SysConfigManager.instance().getValue(defaultKey + "." + account.getReceiveProtocolType());
        if (attachmentSvcQueueURL == null) {
            attachmentSvcQueueURL = SysConfigManager.instance().getValue(defaultKey);
        }
        return attachmentSvcQueueURL;
    }

    public void skipMesagesBefore(int lastMailId, int user_id) throws DALException {
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("mail_id", lastMailId);
            params.put("user_id", user_id);
            params.put("limit", Integer.parseInt(SysConfigManager.instance().getValue("skipMessagesBeforeMailId_Max", "1000")));

            sqlMapClient.update("ReceivedEmail.skipMessagesBeforeMailId", params);
        } catch (Throwable t) {
            throw new DALException("Unable to skip messages before mail_id=" + lastMailId + " for user_id=" + user_id, t);
        }
    }

    //TODO - Paul - duplicate code in attachmentorMap
    private static String renameToJpg(String attachmentFileName) {
        String file = attachmentFileName.toUpperCase();
        StringBuilder sb = new StringBuilder(attachmentFileName);
        if (file.endsWith("BMP") || file.endsWith("PNG") || file.endsWith("GIF")) {
            int index = file.lastIndexOf(".");
            if (index > 0) {
                sb.replace(index, index + 4, ".jpg");
            }
        }

        return sb.toString();
    }
}