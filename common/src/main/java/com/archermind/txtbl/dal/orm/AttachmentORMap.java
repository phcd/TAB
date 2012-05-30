package com.archermind.txtbl.dal.orm;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.domain.Attachment;
import com.archermind.txtbl.domain.OriginalReceivedAttachment;

import com.archermind.txtbl.utils.AmazonS3;
import org.jboss.logging.Logger;
import org.jets3t.service.S3ServiceException;

import java.sql.SQLException;
import java.util.*;

/* TODO: clean this class up so that:
   - all methods build their own hashmaps
   - all methods catch SQLException and throw DALException
*/
public class AttachmentORMap extends BaseORMap {

    private static final Logger logger = Logger.getLogger(AttachmentORMap.class);

    public AttachmentORMap(boolean useMaster) {
        super(useMaster);
    }

    public AttachmentORMap() {
    }

    public AttachmentORMap(BaseORMap another) {
        super(another);
    }

    public int addSentAttachment(Attachment attachment) throws SQLException {
        int attachmentId = sqlMapClient.update("SentAttachment.addAttachment", attachment);
        attachment.setId(attachmentId);

        try {
            AmazonS3.getInstance().saveAttachment(attachment, AmazonS3.AttachmentType.SENT);
        } catch (S3ServiceException e) {
            logger.error("Error saving attachment: " + e.getMessage());
        }

        return attachmentId;
    }

    public int addReceivedAttachment(Date mailDate, Attachment attachment) throws SQLException {
        byte[] data = attachment.getData();

        attachment.setData(null);
        // if the attachment is an image, it will be renamed to <name>.jpg since the peek only likes jpeg files
        attachment.setComment(attachment.getName());
        attachment.setName(renameToJpg(attachment.getName()));

        sqlMapClient.insert("ReceivedAttachment.addAttachment", attachment);

        try {
            attachment.setData(data);
            AmazonS3.getInstance().saveAttachment(mailDate, attachment, AmazonS3.AttachmentType.RECEIVED);
        } catch (S3ServiceException e) {
            logger.error("S3 Service error while saving attachment: " + e.getMessage());
        } catch (Throwable t) {
            logger.error("Error saving attachment: " + t.getMessage());
        }

        return attachment.getId();
    }

    public int newAddReceivedAttachment(Attachment attachment) throws SQLException {
        byte[] data = attachment.getData();

        attachment.setData(null);
        // if the attachment is an image, it will be renamed to <name>.jpg since the peek only likes jpeg files
        attachment.setComment(attachment.getName());
        String name = renameToJpg(attachment.getName());
        //changes extension for WORD 2007
        name = name.replaceFirst("\\.docx$", ".doc");  //TODO should be removed when device can proccess .docx
        attachment.setName(name);

        sqlMapClient.insert("ReceivedAttachment.addAttachment", attachment);

        attachment.setData(data);

        return attachment.getId();
    }

    @SuppressWarnings("unchecked")
    public List<Attachment> getSentAttachment(HashMap param) throws SQLException {
        boolean dataFoundInDB = true;
        int emailId = Integer.valueOf(param.get("emailid").toString());

        // first check DB
        List<Attachment> attachments = sqlMapClient.queryForList("SentAttachment.getAttachment", param);
        for (Attachment attachment : attachments) {
            if (attachment == null || isNullOrEmpty(attachment.getData())) {
                logger.info("Found no attachment data in DB for mailId: " + emailId);
                dataFoundInDB = false;
                break;
            }
        }

        // now check S3
        if (!dataFoundInDB) {
            logger.info("Checking S3 for attachments for mailId: " + emailId);
            try {
                attachments = AmazonS3.getInstance().retrieveSentAttachments(emailId);
                logger.info(attachments.size() + " attachments in S3 for mailId: " + emailId);
            } catch (S3ServiceException e) {
                logger.error("Error retrieving attachments for email ID " + emailId + ": " + e.getMessage());
            }
        }

        return attachments;
    }

    public Attachment getReceivedAttachment(HashMap param) throws SQLException {
        // Check DB
        Attachment attachment = (Attachment) sqlMapClient.queryForObject("ReceivedAttachment.getAttachment", param);

        if (logger.isTraceEnabled())
            logger.trace(String.format("found attachment=%s", String.valueOf(attachment)));

        if (attachment == null || isNullOrEmpty(attachment.getData())) {
            int emailId = Integer.valueOf(param.get("emailId").toString());
            int attachmentId = Integer.valueOf(param.get("id").toString());
            java.util.Date mailDate = (java.util.Date) param.get("mailDate");

            logger.info("Found no attachment data in DB for mailId: " + emailId);

            // Check S3
            try {
                attachment = null;

                List<Attachment> attachments = AmazonS3.getInstance().retrieveReceivedAttachments(mailDate, emailId);
                logger.info(attachments.size() + " attachments in S3 for mailId: " + emailId);

                for (Attachment a : attachments) {
                    if (a.getId() == attachmentId) {
                        attachment = a;
                        break;
                    }
                }

                if (attachment == null) {
                    logger.warn("\"Received Attachment\" " + attachmentId + " for mailId " + emailId + " not found in S3. It may be pending processing by the Attachment Service.");
                }
            } catch (S3ServiceException e) {
                logger.error("Error retrieving attachments for email ID " + emailId + ": " + e.getMessage());
            }
        }

        return attachment;
    }

    @SuppressWarnings("unchecked")
    public List<Attachment> getReceivedAttachmentsNoData(int emailId) throws SQLException {
        HashMap<String, Object> param = new HashMap<String, Object>();
        param.put("emailId", emailId);
        logger.debug("Checking DB for attachments for mailId: " + emailId);
        return sqlMapClient.queryForList("ReceivedAttachment.getAttachments", param);
    }


    @SuppressWarnings("unchecked")
    public List<Attachment> getOriginalAttachmentsNoData(int emailId) throws SQLException {
        HashMap<String, Object> param = new HashMap<String, Object>();
        param.put("emailId", emailId);
        logger.debug("Checking DB for original attachments for mailId: " + emailId);
        return sqlMapClient.queryForList("ReceivedAttachment.getOriginalAttachments", param);
    }

    /**
     * get the list of the attachments ,not including the data of each attachment.
     * <p/>
     * WARNING: "ReceivedAttachment.getAttachmentData" loads data from txtbl_original_attachment,
     * NOT txtbl_received_attachment. Note that however that the object being returned by this
     * method is of type Attachment and not OriginalReceivedAttachment. This needs to be cleaned
     * up by getting rid of the useless OriginalReceivedAttachment class.
     *
     * @param param
     * @return Attachment
     * @throws SQLException
     * @param: emailid
     */
    @SuppressWarnings("unchecked")
    public List<Attachment> getReceivedAttachmentData(HashMap param) throws SQLException {
        boolean dataFoundInDB = true;
        int emailId = Integer.valueOf(param.get("emailId").toString());
        java.util.Date mailDate = (java.util.Date) param.get("mailDateAsDate");

        // first check DB
        List<Attachment> attachments = sqlMapClient.queryForList("ReceivedAttachment.getAttachmentData", param);
        for (Attachment attachment : attachments) {
            if (attachment == null || isNullOrEmpty(attachment.getData())) {
                logger.info("Found no attachment data in DB for mailId: " + emailId);
                dataFoundInDB = false;
                break;
            }
        }

        // now check S3
        if (!dataFoundInDB) {
            try {
                List<OriginalReceivedAttachment> originalAttachments = AmazonS3.getInstance().retrieveOriginalAttachments(mailDate, emailId);
                attachments = toAttachments(originalAttachments);
                logger.info(attachments.size() + " attachments in S3 for mailId: " + emailId + ", using mailDate=" + mailDate);
            } catch (S3ServiceException e) {
                logger.error("Error retrieving attachments for email ID " + emailId + ": " + e.getMessage());
            }
        }

        return attachments;
    }

    private boolean isNullOrEmpty(byte[] data) {
        return data == null || data.length == 0;
    }

    private List<Attachment> toAttachments(List<OriginalReceivedAttachment> originalAttachments) {
        List<Attachment> attachments = new ArrayList<Attachment>();

        for (OriginalReceivedAttachment originalAttachment : originalAttachments) {
            attachments.add(toAttachment(originalAttachment));
        }

        return attachments;
    }

    private Attachment toAttachment(OriginalReceivedAttachment originalAttachment) {
        Attachment attachment = new Attachment();

        attachment.setEmailId(originalAttachment.getEmailId());
        attachment.setId(originalAttachment.getId());
        attachment.setName(originalAttachment.getName());
        attachment.setData(originalAttachment.getData());

        return attachment;
    }


    private static String renameToJpg(String attachmentFileName) {

        String file = attachmentFileName.toUpperCase();

        StringBuilder sb = new StringBuilder(attachmentFileName);

        if (file.endsWith(".BMP") || file.endsWith(".PNG") || file.endsWith(".GIF")) {
            // handle: file.bmp, file.png, etc.
            int index = file.lastIndexOf(".");
            if (index > 0) {
                sb.replace(index, index + 4, ".jpg");
            }
        } else if (file.endsWith(".JPG") && file.indexOf(".BMP.") > 0 || file.indexOf(".PNG.") > 0 || file.indexOf(".GIF.") > 0) {
            // handle: file.bmp.jpg, file.png.jpg, etc.
            int start = file.length() - (".JPG".length() + ".XXX".length());
            int end = start + ".JPG".length() + ".XXX".length();
            sb.replace(start, end, ".jpg");
        }

        return sb.toString();
    }

    public String getAttachmentName(int attachmentid) throws DALException {
        Map<String, Integer> params = new HashMap<String, Integer>();

        params.put("id", attachmentid);

        try {
            return (String) sqlMapClient.queryForObject("ReceivedAttachment.getAttachmentName", params);
        } catch (SQLException e) {
            throw new DALException("Unable to get attachment name for " + attachmentid, e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Attachment> getAttachmentHeaders(String userId, String emailStatus, int startEmailId, int endEmailId) throws DALException {
        HashMap<String, Object> param = new HashMap<String, Object>();
        param.put("userId", userId);
        param.put("startEmailId", startEmailId);
        param.put("endEmailId", endEmailId);
        param.put("emailStatus", emailStatus);

        try {
            return sqlMapClient.queryForList("ReceivedAttachment.getAttachmentHeaders", param);
        } catch (SQLException e) {
            throw new DALException("Unable to get attachment Headers for userId " + userId, e);
        }
    }


    @SuppressWarnings("unchecked")
    public OriginalReceivedAttachment getOriginalAttachmentFromReceivedAttachment(int emailId, String name) throws DALException {
        HashMap<String, Object> param = new HashMap<String, Object>();
        param.put("email_id", emailId);
        param.put("name", name);
        try {
            return (OriginalReceivedAttachment) sqlMapClient.queryForObject("ReceivedAttachment.getOriginalAttachmentFromReceivedAttachment", param);
        } catch (SQLException e) {
            throw new DALException(String.format("Unable to get original attachment Headers for emailId=%d, name=%s ",

                    emailId, name), e);

        }
    }
}