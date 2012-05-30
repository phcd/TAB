package com.archermind.txtbl.dal.business.impl;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.dal.business.IEmailReceivedService;
import com.archermind.txtbl.dal.orm.AttachmentORMap;
import com.archermind.txtbl.dal.orm.BodyORMap;
import com.archermind.txtbl.dal.orm.EmailORMap;
import com.archermind.txtbl.dal.orm.ReceivedORMap;
import com.archermind.txtbl.domain.*;
import com.archermind.txtbl.utils.Constants;
import com.archermind.txtbl.utils.ErrorCode;
import com.archermind.txtbl.utils.SysConfigManager;
import org.jboss.logging.Logger;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


public class EmailRecievedService implements IEmailReceivedService {
    private static final Logger logger = Logger.getLogger(EmailRecievedService.class);

    private static boolean userMasterForNewMailCheck;
    private static boolean useMasterForGetBody;
    private static boolean userMasterForAttachment;
    private static boolean useMasterForReceivedMail;

    static {
        userMasterForNewMailCheck = Boolean.parseBoolean(SysConfigManager.instance().getValue("userMasterForNewMailCheck", "false"));
        useMasterForGetBody = Boolean.parseBoolean(SysConfigManager.instance().getValue("useMasterForGetBody", "false"));
        userMasterForAttachment = Boolean.parseBoolean(SysConfigManager.instance().getValue("userMasterForAttachment", "false"));
        useMasterForReceivedMail = Boolean.parseBoolean(SysConfigManager.instance().getValue("useMasterForReceivedMail", "false"));
    }


    /*
    * TODO: make these methods not need to handle SQLExceptions (this should be done inside the map, SQL should be abstracted away here).
    * TODO: make these methods pass parameters individually instead of Hashmaps (also breaks abstraction)
    * TODO: clean up method names
    */
    public Date getRecievedMailReceivedDate(int mailid) throws DALException {
        try {
            return new EmailORMap().getReceivedEmailReceivedDate(mailid);
        } catch (SQLException e) {
            throw new DALException("Unable to extract received_date for mailid=" + mailid, e);
        }
    }

    public int saveEmail(EmailPojo emailPojo) throws DALException {
        OriginalReceivedEmail receivedEmail = new OriginalReceivedEmail();
        receivedEmail.setMail_type("EMAIL");
        return saveEmail(emailPojo, receivedEmail);
    }

    public Email getEmail(int emailid) throws DALException {
        EmailORMap emailORMap = new EmailORMap(useMasterForReceivedMail);
        HashMap<String, Object> param = new HashMap<String, Object>();
        param.put("mailid", emailid);

        try {
            List<Email> receivedEmails = emailORMap.getReceivedEmail(param);
            if (receivedEmails == null || receivedEmails.size() == 0) {
                throw new DALException(ErrorCode.CODE_DAL_, String.format("email with id = %s not found", emailid));
            }
            return receivedEmails.get(0);
        } catch (SQLException e) {
            throw new DALException(ErrorCode.CODE_DAL_, String.format("Error while retrieving email with id = %s", emailid), e);
        }
    }


    public boolean exists(Integer emailId, String accountName) throws DALException {
        try {
            EmailORMap emailORMap = new EmailORMap(useMasterForReceivedMail);
            HashMap<String, Object> param = new HashMap<String, Object>();
            param.put("id", emailId);
            param.put("original_account", accountName);
            return emailORMap.getEmailCount(param) > 0;
        } catch (SQLException e) {
            throw new DALException(ErrorCode.CODE_DAL_, String.format("Error while retrieving email count with id = %s and %s", emailId, accountName), e);
        }
    }

    public boolean exists(Integer emailId, Integer userId) throws DALException {
        try {
            EmailORMap emailORMap = new EmailORMap(useMasterForReceivedMail);
            HashMap<String, Object> param = new HashMap<String, Object>();
            param.put("id", emailId);
            param.put("userId", userId);
            return emailORMap.getEmailCountByUserId(param) > 0;
        } catch (SQLException e) {
            throw new DALException(ErrorCode.CODE_DAL_, String.format("Error while retrieving email count with id = %d and user_id %d", emailId, userId), e);
        }
    }


    public List<Email> getNewEmail(String userId, String status, Integer limit, boolean useMaster) throws DALException {
        try {
            EmailORMap emailORMap = new EmailORMap(useMaster);
            HashMap<String, Object> param = new HashMap<String, Object>();
            param.put("userId", userId);
            param.put("status", status);
            if (limit != null) {
                param.put("limit", limit);
            }
            return emailORMap.getReceivedEmail(param);
        } catch (SQLException e) {
            throw new DALException(ErrorCode.CODE_DAL_, String.format("Error while retrieving messages with status %s for user %s, limit %s, useMaster=%s", status, userId, limit, useMaster), e);
        }
    }

    public List<Email> getNewEmail(String accountName, String status, boolean useMaster) throws DALException {
        EmailORMap emailORMap = new EmailORMap(useMaster);
        HashMap<String, Object> param = new HashMap<String, Object>();
        param.put("original_account", accountName);
        param.put("status", status);

        try {
            return emailORMap.getReceivedEmail(param);
        } catch (SQLException e) {
            throw new DALException(ErrorCode.CODE_DAL_, String.format("Error while retrieving messages with status %s for account %s, useMaster=%s", status, accountName, useMaster), e);
        }
    }

    public List<Email> getIMAPDirtyEmail(String userId) throws DALException {
        try {
            EmailORMap emailORMap = new EmailORMap(useMasterForReceivedMail);
            HashMap<String, Object> param = new HashMap<String, Object>();
            param.put("uuid", userId);
            return emailORMap.getIMAPDirtyEmail(param);
        } catch (SQLException e) {
            throw new DALException(ErrorCode.CODE_DAL_, String.format("Error while retrieving dirty imap messages for user %s, useMaster=%s", userId, useMasterForReceivedMail), e);
        }
    }

    public void clearImapDirtyStatusFlagsBulk(String userId, int[] emailIds, int imapStatus) throws DALException {
        EmailORMap emailORMap = new EmailORMap(useMasterForReceivedMail);
        HashMap<String, Object> param = new HashMap<String, Object>();
        param.put("imapStatus", imapStatus);
        param.put("userId", userId);
        param.put("mailids", emailIds);

        try {
            emailORMap.startTransaction();
            emailORMap.clearImapDirtyStatusFlagsBulk(param);
        } catch (SQLException e) {
            logger.error("Clearing imap dirty status flags error!", e);
            throw new DALException(ErrorCode.CODE_DAL_, e);
        } finally {
            try {
                emailORMap.endTransaction();
            } catch (SQLException e) {
                logger.error("Clearing imap dirty status flags error!", e);
            }
        }
    }


    public void clearImapDirtyStatusFlags(String userId, Email email, int imapStatus) throws DALException {
        try {
            EmailORMap emailORMap = new EmailORMap(useMasterForReceivedMail);
            HashMap<String, Object> param = new HashMap<String, Object>();
            param.put("userId", userId);
            param.put("messageId", email.getMessageId());
            param.put("imapStatus", imapStatus);
            emailORMap.clearImapDirtyStatusFlags(param);
        } catch (SQLException e) {
            throw new DALException(ErrorCode.CODE_DAL_, String.format("Error while clearing dirty flag bits user %s, messageID %s, useMaster=%s", userId, email.getMessageId(), useMasterForReceivedMail), e);
        }
    }

    /*
      * (non-Javadoc)
      *
      * @see com.archermind.txtbl.dal.business.IEmailReceivedService#getBody(java.lang.String)
      */
    public Body getBody(String uuid, String emailId, boolean useCannedMessage) throws DALException {
        HashMap<String, Object> param = new HashMap<String, Object>();
        param.put("emailid", emailId);
        param.put("uuid", uuid);
        BodyORMap bodyORMap = new BodyORMap(useMasterForGetBody);
        try {
            Body body = bodyORMap.getReceivedBody(param);
            if (body == null && useCannedMessage) {
                body = new Body();
                body.setData(SysConfigManager.instance().getValue("noEmailBodyFoundMessage", "").getBytes());
            }
            return body;
        } catch (SQLException e) {
            logger.error("Get body error! emailId" + emailId, e);
            throw new DALException(ErrorCode.CODE_DAL_, e);
        }
    }


    public List<Attachment> getAttachmentListNoData(int emailId, boolean preferOriginals) throws DALException {

        try {
            AttachmentORMap map = new AttachmentORMap(userMasterForAttachment);
            List<Attachment> receivedAttachments = map.getReceivedAttachmentsNoData(emailId);

            if (preferOriginals) {

                if (receivedAttachments != null && receivedAttachments.size() > 0) {
                    // try for originals based on the data received in the last query

                    logger.debug(String.format("found %d attachments, trying to retrieve originals", receivedAttachments.size()));
                    List<Attachment> origAttachments = new LinkedList<Attachment>();

                    for (Attachment att : receivedAttachments) {
                        logger.debug(String.format("trying to get originals with emailId=%s, attId=%d, name=%s", emailId, att.getId(), att.getName()));
                        OriginalReceivedAttachment origAttachment = getOriginalAttachmentFromReceivedAttachment(emailId, att.getComment()); // comment has the original filename

                        if (origAttachment != null) {
                            origAttachment.setSize(att.getSize()); // workaround
                            logger.debug(String.format("found original attachment for emailId=%d, recId=%d, origId=%d, name=%s",
                                    emailId, att.getId(), origAttachment.getId(), origAttachment.getName()));
                            Attachment temp = new Attachment();
                            //BeanUtils.copyProperties(origAttachment, temp);
                            temp.setId(origAttachment.getId());
                            temp.setName(origAttachment.getName());
                            temp.setEmailId(origAttachment.getEmailId());
                            temp.setSavedOnDate(origAttachment.getSavedOnDate());
                            temp.setLocation(origAttachment.getLocation());
                            temp.setSize((int) origAttachment.getSize());
                            origAttachments.add(temp);
                        } else {
                            //origAttachments.add(att);  // use the received if we have to
                        }
                    }
                    return origAttachments;
                } else {
                    logger.debug("no original attachments found");
                    return receivedAttachments;
                }
            } else {
                logger.debug("returning received attachments since original was not preferred");
                return receivedAttachments;
            }
        } catch (SQLException e) {
            logger.error("Get attachment list error! emailId=" + emailId, e);
            throw new DALException(ErrorCode.CODE_DAL_, e);
        }
    }


    /*

    * This method is to support unprocessed attachment retrieval. It should disappear when we get rid of original_attachment table.

    */

    public OriginalReceivedAttachment getOriginalAttachmentFromReceivedAttachment(int emailId, String name) throws DALException {

        return new AttachmentORMap(userMasterForAttachment).getOriginalAttachmentFromReceivedAttachment(emailId, name);
    }

    /**
     * @param uuid
     * @param emailId
     * @param name
     * @param id
     * @param mailDate
     * @return NULL if the attachment is not found
     * @throws DALException
     */
    public Attachment getAttachment(String uuid, int emailId, String name, Integer id, Date mailDate) throws DALException {
        try {
            HashMap<String, Object> param = new HashMap<String, Object>();
            param.put("emailId", emailId);
            param.put("name", name);
            param.put("uuid", uuid);
            param.put("id", id);
            param.put("mailDate", mailDate);
            return new AttachmentORMap(userMasterForAttachment).getReceivedAttachment(param);
        } catch (SQLException e) {
            logger.error("Get attachment error! emailId" + emailId, e);
            throw new DALException(ErrorCode.CODE_DAL_, e);
        }
    }

    public int updateStatusSingle(String uuid, int emailid, String newStatus) throws DALException {
        int result = 0;

        EmailORMap emailORMap = new EmailORMap();
        try {
            emailORMap.startTransaction();
            HashMap<String, Object> param = new HashMap<String, Object>();
            param.put("uuid", uuid);
            param.put("emailid", emailid);
            param.put("newStatus", newStatus);
            result = emailORMap.updateReceivedUuidStatusByEmailID(param);
            logger.debug("Updated email status for " + emailid + " to new status " + newStatus + "=" + result + " rows updated,  by " + uuid);

            if (result != 0) {
                emailORMap.commitTransaction();
                result = 1;
            }

        } catch (SQLException e) {
            result = 0;
            logger.error("updateStatusSingle error! uuid: " + uuid, e);
            throw new DALException(ErrorCode.CODE_DAL_, e);

        } finally {
            try {
                emailORMap.endTransaction();
            } catch (SQLException e) {
                result = 0;
                logger.error("updateStatusSingle error!", e);
            }
        }
        return result;

    }

    public List<Attachment> getAttachmentHeaders(String userId, String emailStatus, int startEmailId, int endEmailId) throws DALException {
        return new AttachmentORMap(userMasterForAttachment).getAttachmentHeaders(userId, emailStatus, startEmailId, endEmailId);
    }

    public int updateStatus(String uuid, String newStatus, String oldStatus) throws DALException {
        int result = 0;
        EmailORMap emailORMap = new EmailORMap();
        try {
            emailORMap.startTransaction();

            HashMap<String, Object> param = new HashMap<String, Object>();
            param.put("oldStatus", oldStatus);
            param.put("newStatus", newStatus);
            param.put("uuid", uuid);

            result = emailORMap.updateReceivedUuidStatus(param);
            logger.debug("Updated all email status from " + oldStatus + " to new status " + newStatus + "=" + result + " rows updated,  by " + uuid);

            if (result != 0) {
                emailORMap.commitTransaction();
                result = 1;
            }

        } catch (SQLException e) {
            result = 0;
            logger.error("Update status error! uuid" + uuid, e);
            throw new DALException(ErrorCode.CODE_DAL_, e);
        } finally {
            try {
                emailORMap.endTransaction();
            } catch (SQLException e) {
                result = 0;
                logger.error("Update status error!", e);
            }
        }
        return result;

    }

    /*
      * (non-Javadoc)
      *
      * @see com.archermind.txtbl.dal.business.IEmailReceivedService#updateStatus(java.lang.String,
      *      int[])
      */
    public int updateStatusBulk(String status, int[] mailId) throws DALException {

        int result = 0;
        EmailORMap emailORMap = new EmailORMap();
        try {
            emailORMap.startTransaction();
            HashMap<String, Object> param = new HashMap<String, Object>();
            param.put("status", status);
            param.put("mailids", mailId);
            result = emailORMap.updateStatusBulk(param);
        } catch (SQLException e) {
            result = 0;
            logger.error("Update status error!", e);
            throw new DALException(ErrorCode.CODE_DAL_, e);
        } finally {
            try {
                emailORMap.endTransaction();
            } catch (SQLException e) {
                result = 0;
                logger.error("Update status error!", e);
            }
        }
        return result;


    }

    public int saveEmail(EmailPojo ep, OriginalReceivedEmail ore)
            throws DALException {
        ReceivedORMap ro = new ReceivedORMap();
        return ro.saveEmail(ep, ore);
    }

    public int deleteReceivedAttachment(String date) throws DALException {
        try {
            long ll_time = System.currentTimeMillis();
            ReceivedORMap ro = new ReceivedORMap();
            List list = ro.selectReceivedAttachmentDate(date);

            if (list.size() > 0) {
                Email email = (Email) list.get(0);

                List listMin = ro.selectReceivedAttachmentMinDate(String.valueOf(email.getMailid()));
                List listMax = ro.selectReceivedAttachmentMaxDate(String.valueOf(email.getMailid()));
                if ((listMin.size() > 0) && (listMax.size() > 0)) {
                    Email emailMax = (Email) listMax.get(0);
                    long ll_max = emailMax.getId();
                    Email emailMin = (Email) listMin.get(0);
                    long ll_min = emailMin.getId();

                    while (ll_min < ll_max) {

                        ll_min = ll_min + 1000;
                        if (ll_min > ll_max)
                            ll_min = ll_max;
                        logger.info("deleteReceivedAttachment number "
                                + ll_min);
                        ro.deleteReceivedAttachment(String.valueOf(ll_min));
                    }
                }
            }
            logger.info("deleteReceivedAttachment  time " + (System.currentTimeMillis() - ll_time));
            return 1;
        } catch (Exception e) {
            logger.error("Delete Received attachment error!", e);
            throw new DALException(ErrorCode.CODE_DAL_, e);
        }
    }

    public int deletedOriginalAttachment(String date) throws DALException {

        try {
            long ll_time = System.currentTimeMillis();
            ReceivedORMap ro = new ReceivedORMap();
            List list = ro.selectOriginalAttachmentDate(date);
            if (list.size() > 0) {

                Email email = (Email) list.get(0);

                List listMax = ro.selectOriginalAttachmentMaxDate(String
                        .valueOf(email.getMailid()));

                List listMin = ro.selectOriginalAttachmentMinDate(String
                        .valueOf(email.getMailid()));

                if ((listMin.size() > 0) && (listMax.size() > 0)) {
                    Email emailMax = (Email) listMax.get(0);
                    long ll_max = emailMax.getId();
                    Email emailMin = (Email) listMin.get(0);
                    long ll_min = emailMin.getId();
                    while (ll_min < ll_max) {
                        ll_min = ll_min + 1000;
                        if (ll_min > ll_max)
                            ll_min = ll_max;
                        logger.info("deleteOriginalAttachment number "
                                + ll_min);
                        ro.deleteOriginalAttachment(String.valueOf(ll_min));
                    }
                }
            }
            logger.info("deleteOriginalAttachment  time " + (System.currentTimeMillis() - ll_time));
            return 1;
        } catch (Exception e) {
            logger.error("Delete Original Attachment error!", e);
            throw new DALException(ErrorCode.CODE_DAL_, e);
        }
    }

    public int deleteReceivedEmail(String id) throws DALException {
        try {
            return new ReceivedORMap().deleteReceivedEmail(id);
        } catch (Exception e) {
            logger.error("Delete Receive Email error!", e);
            throw new DALException(ErrorCode.CODE_DAL_, e);
        }
    }

    public Email getReceivedEmail(String mailid, String uuid) throws DALException {
        HashMap<String, String> paras = new HashMap<String, String>();
        paras.put("mailid", mailid);
        paras.put("uuid", uuid);
        return new ReceivedORMap(useMasterForReceivedMail).getReceivedEmail(paras);
    }

    public Email getReceivedEmailByMessageId(String messageId, String uuid) throws DALException {
        HashMap<String, String> paras = new HashMap<String, String>();
        paras.put("messageId", messageId);
        paras.put("uuid", uuid);
        return new ReceivedORMap(useMasterForReceivedMail).getReceivedEmailByMessageId(paras);
    }


    public int delReceiveEmail(String date) throws DALException {
        try {
            logger.info("Receive Email input date " + date);
            ReceivedORMap ro = new ReceivedORMap();
            List list = ro.selectReceivedEmailDate(date);
            if (list.size() > 0) {
                for (Object aList : list) {
                    Email email = (Email) aList;

                    ro.deleteReceivedAttachment(String.valueOf(email.getMailid()));

                    ro.deleteOriginalAttachment(String.valueOf(email.getMailid()));

                    ro.deleteReceivedBody(String.valueOf(email.getMailid()));

                    ro.deleteReceivedHead(String.valueOf(email.getMailid()));
                }
            }
            logger.info("Receive Email end ");
            return 1;
        } catch (Exception e) {
            logger.error("del Receive Email error!", e);
            throw new DALException(ErrorCode.CODE_DAL_, e);
        }
    }

    public List<Attachment> getAttachmentDataList(String emailId, Date mailDateAsDate) throws DALException {
        try {
            HashMap<String, Object> param = new HashMap<String, Object>();
            param.put("emailId", emailId);
            param.put("mailDateAsDate", mailDateAsDate);

            return new AttachmentORMap(userMasterForAttachment).getReceivedAttachmentData(param);
        } catch (SQLException e) {
            logger.error("getAttachmentDataList error!", e);
            throw new DALException(ErrorCode.CODE_DAL_, e);
        }
    }

    public int getNewEmailCount(String userId) throws DALException {
        if (logger.isDebugEnabled())
            logger.debug(String.format("getNewEmailCount(userId=%s)", userId));
        try {
            ReceivedORMap ro = new ReceivedORMap(userMasterForNewMailCheck);
            logger.info("new email count for web module push mail  user_id  = " + userId);
            int li_ret = ro.getNewEmailCount(userId);
            logger.info("new email count for web module push mail  number  = " + li_ret);
            return li_ret;
        } catch (SQLException e) {
            logger.error("getNewEmailCount error!", e);
            throw new DALException(ErrorCode.CODE_DAL_, e);
        }
    }


    public int getNewEmailCountForAccount(String name) throws DALException {
        try {
            ReceivedORMap ro = new ReceivedORMap(userMasterForNewMailCheck);
            int count = ro.getNewEmailCountForAccount(name);
            logger.info("new email count for " + name + "  = " + count);
            return count;
        } catch (SQLException e) {
            logger.error("getNewEmailCountForAccount error!", e);
            throw new DALException(ErrorCode.CODE_DAL_, e);
        }
    }

    public int getDeliveredEmailCount(String userId) throws DALException {
        if (logger.isDebugEnabled())
            logger.debug(String.format("getDeliveredEmailCount(userId=%s)", userId));
        ReceivedORMap ro = new ReceivedORMap(userMasterForNewMailCheck);
        try {
            return ro.getDeliveredEmailCount(userId);
        } catch (SQLException e) {
            logger.error("getDeliveredEmailCount error!", e);
            throw new DALException(ErrorCode.CODE_DAL_, e);
        }
    }

    public int newSaveEmailTwitter(EmailPojo emailPojo, OriginalReceivedEmail original) throws DALException {
        ReceivedORMap ro = new ReceivedORMap();
        return ro.newSaveEmailTwitter(emailPojo, original);
    }

    public int newSaveEmail(EmailPojo emailPojo, OriginalReceivedEmail original, Account account) throws DALException {
        ReceivedORMap ro = new ReceivedORMap();
        return ro.newSaveEmail(emailPojo, original, account);
    }

    public int newSaveEmail(Account account, Email email, Body body) throws DALException {
        ReceivedORMap ro = new ReceivedORMap();
        return ro.newSaveEmail(account, email, body);
    }

    public void skipMesagesBefore(int lastMailId, int user_id) throws DALException {
        ReceivedORMap ro = new ReceivedORMap();
        ro.skipMesagesBefore(lastMailId, user_id);

    }

    public String getAttachmentName(int attachmentid) throws DALException {
        AttachmentORMap attachORMap = new AttachmentORMap(userMasterForAttachment);
        return attachORMap.getAttachmentName(attachmentid);
    }

    // returns # of mails truncated
    public int truncateUnreadList(String uuid, int count) throws DALException {
        EmailORMap emailORMap = new EmailORMap();
        HashMap<String, Object> param = new HashMap<String, Object>();
        param.put("uuid", uuid); // these params have to be in the order that they are used!
        param.put("old_status", Constants.EMAIL_LIST_STATUS_INIT);
        param.put("cutoff", count);
        param.put("new_status", Constants.EMAIL_LIST_STATUS_VIEWED);
        try {
            return emailORMap.updateOldestMailsAfterCutoffHavingStatus(param);
        } catch (SQLException e) {
            logger.error("truncateUnreadList error!", e);
            throw new DALException(ErrorCode.CODE_DAL_, e);
        }

    }


}
