package com.archermind.txtbl.utils;

import com.archermind.txtbl.domain.Attachment;
import com.archermind.txtbl.domain.OriginalReceivedAttachment;
import org.apache.poi.util.IOUtils;
import org.jboss.logging.Logger;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.AWSCredentials;
import org.jets3t.service.utils.Mimetypes;
import org.jets3t.service.utils.ServiceUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AmazonS3 {
    private static final String EMAIL_ID_KEY = "email-id";
    private static final String ATTACHMENT_ID_KEY = "attachment-id";
    private static final String ATTACHMENT_NAME_KEY = "attachment-name";
    private static final String ATTACHMENT_SIZE_KEY = "attachment-size";
    private static final String COMMENT_KEY = "comment";
    private static final String BROWSE_SEQ_KEY = "browse-att-seq-no";

    private static final String ACCOUNT_ID_KEY = "account-id";
    private static final String MESSAGE_ID_KEY = "message-id";

    private final Map<String, S3Bucket> bucketMap = new HashMap<String, S3Bucket>();


    // Attachment

    public enum AttachmentType {
        ORIGINAL,
        RECEIVED,
        SENT
    }

    private static final Logger logger = Logger.getLogger(AmazonS3.class);

    /**
     * The S3 buckets used - the SYS_CONFIG_AWS_BUCKET_SUFFIX is appended to the bucket name
     * to fully qualify the bucket name based on the environment.
     */
    private static final String ORIGINAL_ATTACHMENTS_BUCKET_NAME = "com.getpeek.attachments.original";
    private static final String RECEIVED_ATTACHMENTS_BUCKET_NAME = "com.getpeek.attachments.received";
    private static final String SENT_ATTACHMENTS_BUCKET_NAME = "com.getpeek.attachments.sent";
    private static final String MESSAGE_IDS_BUCKET_NAME = "com.getpeek.messageids";
    private static final String BULK_MESSAGE_IDS_BUCKET_NAME = "com.getpeek.bulkmessageids";

    private static final int S3_MAX_NAME_SIZE = 1024;

    private S3Service s3Service;

    // System Configuration Values
    private static final String SYS_CONFIG_AWS_ACCESS_KEY = "aws.s3.access.key";
    private static final String SYS_CONFIG_AWS_SECRET_KEY = "aws.s3.secret.key";
    private static final String SYS_CONFIG_AWS_BUCKET_SUFFIX = "aws.s3.bucket.suffix";

    private String awsBucketSuffix;

    private static AmazonS3 instance = null;

    /**
     * Returns the singleton instance, configured from SysConfig settings.
     *
     * @return The AmazonS3 singleton instance
     */
    public synchronized static AmazonS3 getInstance() {
        if (instance == null) {
            instance = new AmazonS3();
        }
        return instance;
    }

    /**
     * Returns the singleton instance, configured for the specified environment.
     *
     * @param env The environment (prod | staging | dev)
     * @return The AmazonS3 singleton instance
     */
    public synchronized static AmazonS3 getInstance(String env) {
        if (instance == null) {
            instance = new AmazonS3(env);
        }
        return instance;
    }

    /**
     * Private constructor, responsible for instantiating and initializing the instance.
     */
    private AmazonS3() {
        initialize();
    }

    /**
     * Private constructor, responsible for instantiating and initializing the instance.
     *
     * @param env
     */
    private AmazonS3(String env) {
        initialize(env);
    }

    public boolean hasMessage(Integer accountId, String messageId) throws S3ServiceException {
        if (logger.isTraceEnabled())
            logger.trace(String.format("hasMessage(accountId=%s, messageId=%s", String.valueOf(accountId), String.valueOf(messageId)));

        S3Bucket bucket = getBucket(createMessageIdsBucketName(MESSAGE_IDS_BUCKET_NAME, awsBucketSuffix));

        S3Object s3Object = null;

        try {
            // create the object so we can get to the key
            s3Object = createS3Object(accountId, messageId);

            s3Object = s3Service.getObject(bucket, s3Object.getKey());
        } catch (S3ServiceException e) {

            s3Object = null; // not found

        } finally {
            closeStream(s3Object);
        }

        return s3Object != null;
    }

    public void addMessagesInBulk(Integer accountId, String messageIds) throws S3ServiceException {
        S3Bucket bucket = getBucket(createMessageIdsBucketName(BULK_MESSAGE_IDS_BUCKET_NAME, awsBucketSuffix));

        S3Object s3Object = createBulkMessageIdsS3Object(accountId, messageIds);

        s3Service.putObject(bucket, s3Object);
    }

    String getBulkMessageIds(Integer accountId) throws S3ServiceException {
        return retrieveBulkMessageIds(accountId);
    }

    /**
     * Returns true if the message was added, false if the message already exists in the store.
     *
     * @param accountId
     * @param messageId
     * @return
     * @throws S3ServiceException
     */
    public boolean addMessage(Integer accountId, String messageId) throws S3ServiceException {
        S3Object s3Object = null;
        try {
            if (!hasMessage(accountId, messageId)) {
                // add the message if it doesn't already exist
                S3Bucket bucket = getBucket(createMessageIdsBucketName(MESSAGE_IDS_BUCKET_NAME, awsBucketSuffix));

                s3Object = createS3Object(accountId, messageId);

                s3Service.putObject(bucket, s3Object);

                return true;
            }
        } finally {
            closeStream(s3Object);
        }

        return false;
    }

    public void deleteMessage(Integer accountId, String messageId) throws S3ServiceException {

        S3Object s3Object = null;
        try {
            S3Bucket bucket = getBucket(createMessageIdsBucketName(MESSAGE_IDS_BUCKET_NAME, awsBucketSuffix));

            s3Object = createS3Object(accountId, messageId);

            s3Service.deleteObject(bucket, s3Object.getKey());
        } finally {
            closeStream(s3Object);
        }
    }

    void deleteBulkMessage(Integer accountId) throws S3ServiceException {
        logger.info("Deleting bulk ID message for " + accountId);

        S3Bucket bucket = getBucket(createBulkMessageIdsBucketName(BULK_MESSAGE_IDS_BUCKET_NAME, awsBucketSuffix));

        s3Service.deleteObject(bucket, String.valueOf(accountId));
    }

    public void deleteAllMessages(Integer accountId) throws S3ServiceException {
        S3Bucket bucket = getBucket(createMessageIdsBucketName(MESSAGE_IDS_BUCKET_NAME, awsBucketSuffix));

        S3Object[] s3Objects = retrieveMessageIdS3Objects(accountId);

        for (S3Object s3Object : s3Objects) {
            s3Service.deleteObject(bucket, s3Object.getKey());
        }
    }

    /**
     * Creates individual S3 message ID entries from the contents of the bulk message ID S3 object for the given account ID.
     *
     * @param accountId
     * @throws org.jets3t.service.S3ServiceException
     *
     * @throws org.jets3t.service.S3ServiceException
     *
     */
    void createMessageIdEntriesFromBulk(Integer accountId) throws S3ServiceException {
        String messageIds = getBulkMessageIds(accountId);

        if (messageIds != null) {
            // parse individual messageID entries from the message IDs string
            Map<Integer, String> messagesMap = getMessageIdsFromBulk(messageIds);

            //S3Bucket bucket = getBucket(createMessageIdsBucketName(MESSAGE_IDS_BUCKET_NAME, awsBucketSuffix));

            Collection<String> messageIdCollection = messagesMap.values();

//            S3Object[] objects = new S3Object[messagesMap.size()];
            for (String messageId : messageIdCollection) {
                logger.info("Adding message ID " + messageId + " for account " + accountId);

                this.addMessage(accountId, messageId);

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

//                objects[count++] = createS3Object(accountId, messageId);
            }

//            // Create a simple multi-threading service based on our existing S3Service
//            S3ServiceSimpleMulti simpleMulti = new S3ServiceSimpleMulti(s3Service);
//
//            // Upload multiple objects.
//            S3Object[] createdObjects = simpleMulti.putObjects(bucket, objects);

            // finally, get rid of the bulk id message
            deleteBulkMessage(accountId);
        }
    }

    public void processBulkMessageIdsBucket() throws S3ServiceException {
        logger.info("Processing bulk message ID entries ...");

        S3Bucket bucket = getBucket(createBulkMessageIdsBucketName(BULK_MESSAGE_IDS_BUCKET_NAME, awsBucketSuffix));

        // Note: We don't expect the bulk message Ids bucket to contain but a limited number of entries. Generally less
        // than 1K entries.
        S3Object[] s3Objects = s3Service.listObjects(bucket);

        logger.info("Bulk message bucket contains " + (s3Objects == null ? 0 : s3Objects.length) + " entries ...");

        if (s3Objects == null) {
            return;
        }

        for (S3Object s3Object : s3Objects) {

            s3Object = s3Service.getObjectDetails(bucket, s3Object.getKey());

            Object accountId = s3Object.getMetadata(ACCOUNT_ID_KEY);

            if (accountId != null) {
                logger.info("Processing bulk message ID entry for account " + accountId);

                createMessageIdEntriesFromBulk(Integer.valueOf(accountId.toString()));
            }

        }

    }

    public void saveAttachment(Date mailDate, OriginalReceivedAttachment originalAttachment) throws S3ServiceException {
        logger.info("Saving original attachment " + originalAttachment.getName() + " of type " + AttachmentType.ORIGINAL + " to S3");

        Attachment attachment = new Attachment();
        attachment.setEmailId(originalAttachment.getEmailId());
        attachment.setId(originalAttachment.getId());
        attachment.setName(originalAttachment.getName());
        attachment.setData(originalAttachment.getData());

        S3Object s3Object = doSaveAttachment(mailDate, attachment, AttachmentType.ORIGINAL);

        originalAttachment.setLocation(attachment.getLocation());
        originalAttachment.setSavedOnDate(s3Object.getLastModifiedDate());
    }

    /**
     * Saves the given attachment to S3. The attachmentType determines which in which bucket the
     * attachment is saved.
     *
     * @param attachment     The attachment to save.
     * @param attachmentType The attachment type (original, received, or sent)
     * @throws S3ServiceException If an S3 exception is raised.
     */
    public void saveAttachment(Attachment attachment, AttachmentType attachmentType) throws S3ServiceException {
        doSaveAttachment(null, attachment, attachmentType);
    }

    /**
     * Saves the given attachment to S3. The attachmentType determines which in which bucket the
     * attachment is saved.
     *
     * @param mailDate
     * @param attachment     The attachment to save.
     * @param attachmentType The attachment type (original, received, or sent)
     * @throws S3ServiceException If an S3 exception is raised.
     */
    public void saveAttachment(Date mailDate, Attachment attachment, AttachmentType attachmentType) throws S3ServiceException {
        doSaveAttachment(mailDate, attachment, attachmentType);
    }


    private S3Object doSaveAttachment(Date mailDate, Attachment attachment, AttachmentType attachmentType) throws S3ServiceException {
        logger.info("Saving attachment " + attachment.getName() + " of type " + attachmentType + " to S3");

        long start = System.currentTimeMillis();

        S3Object s3Object = null;

        try {
            s3Object = createS3Object(attachment);

            S3Bucket bucket = getBucket(mailDate, attachmentType);

            if (logger.isTraceEnabled())
                logger.trace("bucket name:" + bucket.getName() + " location:" + bucket.getLocation());

            String location = getLocation(bucket, s3Object);

            if (logger.isTraceEnabled())
                logger.trace("location=" + location);

            attachment.setLocation(location);

            s3Service.putObject(bucket, s3Object);

            logger.info("Attachment saved as " + s3Object.getKey() + " in bucket " + bucket.getName());
        } finally {
            closeStream(s3Object);
        }

        long end = System.currentTimeMillis();
        logger.info("Attachment " + attachment.getName() + " was saved in " + (end - start) + " ms.");

        return s3Object;
    }

    /**
     * Retrieves the original attachments associated with the given email ID.
     *
     * @param emailId The email ID.
     * @return The list of original attachments.
     * @throws S3ServiceException If an S3 exception is raised.
     */
    public List<OriginalReceivedAttachment> retrieveOriginalAttachments(int emailId) throws S3ServiceException {
        return retrieveOriginalAttachments(null, emailId);
    }

    /**
     * Retrieves the original attachments associated with the given email ID.
     *
     * @param mailDate
     * @param emailId  The email ID.
     * @return The list of original attachments.
     * @throws S3ServiceException If an S3 exception is raised.
     */
    public List<OriginalReceivedAttachment> retrieveOriginalAttachments(Date mailDate, int emailId) throws S3ServiceException {
        logger.info("Retrieving Original attachment(s) for email ID " + emailId);
        List<Attachment> attachments = retrieveAttachments(emailId, getBucket(mailDate, AttachmentType.ORIGINAL));

        List<OriginalReceivedAttachment> originalAttachments = new ArrayList<OriginalReceivedAttachment>();
        if (attachments != null) {
            for (Attachment attachment : attachments) {
                OriginalReceivedAttachment originalAttachment = new OriginalReceivedAttachment();
                originalAttachment.setEmailId(attachment.getEmailId());
                originalAttachment.setId(attachment.getId());
                originalAttachment.setName(attachment.getName());
                originalAttachment.setData(attachment.getData());
                originalAttachment.setContentType(attachment.getContentType());
                originalAttachments.add(originalAttachment);
                logger.debug("Built originalAttachment for " + attachment.getName());
            }
            return originalAttachments;
        } else {
            logger.warn("Attachment list returned is null, no attachments retrieved");
            return null;
        }
    }

    // TODO: this needs to be more efficient

    public OriginalReceivedAttachment retrieveOriginalAttachment(Date mailDate, Integer emailId, Integer originalAttachmentId) throws S3ServiceException {
        logger.info(String.format("Retrieving Original attachment %s for email ID %s", originalAttachmentId, emailId));

        List<Attachment> attachments = retrieveAttachments(emailId, getBucket(mailDate, AttachmentType.ORIGINAL));

        if (attachments != null) {
            for (Attachment attachment : attachments) {
                //logger.debug(UtilsTools.beanToString(attachment));
                if (originalAttachmentId.equals(attachment.getId())) {
                    OriginalReceivedAttachment originalAttachment = new OriginalReceivedAttachment();
                    originalAttachment.setEmailId(attachment.getEmailId());
                    originalAttachment.setId(attachment.getId());
                    originalAttachment.setName(attachment.getName());
                    originalAttachment.setData(attachment.getData());
                    originalAttachment.setSavedOnDate(attachment.getSavedOnDate());
                    originalAttachment.setContentType(attachment.getContentType());
                    originalAttachment.setSize(attachment.getSize());
                    logger.debug("Built originalAttachment for " + attachment.getName());
    
                    return originalAttachment;
                }
            }
        } else {
            logger.error("Attachment list retrieved was null!");
        }

        return null;
    }


    /**
     * Retrieves the received attachments associated with the given email ID.
     *
     * @param mailDate
     * @param emailId  The email ID.
     * @return The received attachments associated with the email ID.
     * @throws S3ServiceException If an S3 exception is raised.
     */
    public List<Attachment> retrieveReceivedAttachments(Date mailDate, int emailId) throws S3ServiceException {
        logger.info("Retrieving Received attachment(s) for email ID " + emailId);
        return retrieveAttachments(emailId, getBucket(mailDate, AttachmentType.RECEIVED));
    }

    /**
     * Retrieves the sent attachments associated with the given email ID.
     *
     * @param emailId The email ID.
     * @return The sent attachments associated with the email ID.
     * @throws S3ServiceException If an S3 exception is raised.
     */
    public List<Attachment> retrieveSentAttachments(int emailId) throws S3ServiceException {
        return retrieveSentAttachments(null, emailId);
    }

    /**
     * Retrieves the sent attachments associated with the given email ID.
     *
     * @param mailDate
     * @param emailId  The email ID.
     * @return The sent attachments associated with the email ID.
     * @throws S3ServiceException If an S3 exception is raised.
     */
    List<Attachment> retrieveSentAttachments(Date mailDate, int emailId) throws S3ServiceException {
        logger.info("Retrieving Sent attachment(s) for email ID " + emailId);
        return retrieveAttachments(emailId, getBucket(mailDate, AttachmentType.SENT));
    }

    /**
     * Returns the bucket name of the bucket used for the given attachment Type.
     *
     * @param attachmentType The attachment type.
     *
     * @return The bucket name.
     */
//	private String getBucketName(Date mailDate, AttachmentType attachmentType) throws S3ServiceException {
//		return getBucket(mailDate, attachmentType).getName();
//	}

    /**
     * Returns the bucket to use for the given attachment Type.
     *
     * @param mailDate
     * @param attachmentType The attachment type.
     * @return The bucket used for the given attachment type.
     * @throws org.jets3t.service.S3ServiceException
     *
     */
    private S3Bucket getBucket(Date mailDate, AttachmentType attachmentType) throws S3ServiceException {

        if (logger.isTraceEnabled())
            logger.trace(String.format("getBucket(mailData=%s attachmentType=%s)", String.valueOf(mailDate),
                    String.valueOf(attachmentType)));

        S3Bucket bucket;
        switch (attachmentType) {
            case ORIGINAL: {
                bucket = getBucket(createAttachmentBucketName(mailDate, ORIGINAL_ATTACHMENTS_BUCKET_NAME, awsBucketSuffix));
                break;
            }
            case RECEIVED: {
                bucket = getBucket(createAttachmentBucketName(mailDate, RECEIVED_ATTACHMENTS_BUCKET_NAME, awsBucketSuffix));
                break;
            }
            default: {
                bucket = getBucket(createAttachmentBucketName(mailDate, SENT_ATTACHMENTS_BUCKET_NAME, awsBucketSuffix));
                break;
            }
        }

        return bucket;
    }

    /**
     * Returns the named bucket.
     *
     * @param name The bucket name.
     * @return The S3 bucket if it exists, null otherwise.
     * @throws S3ServiceException If an S3 exception is raised.
     */
    private S3Bucket getBucket(String name) throws S3ServiceException {

        if (logger.isTraceEnabled())
            logger.trace(String.format("getBucket name=%s", name));

        S3Bucket bucket = bucketMap.get(name);

        if (bucket == null) {

            bucket = s3Service.getBucket(name);

            if (bucket == null) {
                bucket = s3Service.createBucket(name);
            }

            bucketMap.put(name, bucket);
        }

        return bucket;
    }

    /**
     * Retrieves the attachments associated with the given email ID from the specified bucket.
     *
     * @param emailId The email ID.
     * @param bucket  The bucket to check.
     * @return The attachments associated with the email ID.
     * @throws S3ServiceException If an S3 exception is raised.
     */
    private List<Attachment> retrieveAttachments(int emailId, S3Bucket bucket) throws S3ServiceException {
        logger.info("Retrieving attachment(s) for email ID " + emailId + " from bucket " + bucket.getName());

        long start = System.currentTimeMillis();

        List<Attachment> attachments = new ArrayList<Attachment>();

        // List only the attachments for the given emailId - partial s3object(s) will be returned
        String suffix = String.valueOf(emailId);
        String delimiter = null;
        S3Object[] s3Objects = s3Service.listObjects(bucket, suffix, delimiter);

        if (s3Objects != null) {
            for (S3Object s3Object : s3Objects) {
                // load the complete object
                s3Object = s3Service.getObject(bucket, s3Object.getKey());

                Attachment attachment = new Attachment();
                attachment.setLocation(getLocation(bucket, s3Object));
                attachment.setEmailId(emailId);
                Object attachmentId = s3Object.getMetadata(ATTACHMENT_ID_KEY);
                attachment.setId(attachmentId != null ? Integer.valueOf(attachmentId.toString()) : 0);
                attachment.setName((String) s3Object.getMetadata(ATTACHMENT_NAME_KEY));
                attachment.setComment((String) s3Object.getMetadata(COMMENT_KEY));
                attachment.setBrowseAttSeqNo((String) s3Object.getMetadata(BROWSE_SEQ_KEY));
                attachment.setData(getData(s3Object));
                attachment.setSize(attachment.getData() != null ? attachment.getData().length : 0);
                attachment.setSavedOnDate(s3Object.getLastModifiedDate());
                attachment.setContentType(s3Object.getContentType());
                attachments.add(attachment);

                logger.info("Created Attachment type from S3 object " + s3Object.getBucketName() + ":" + s3Object.getKey());
            }
        }

        long end = System.currentTimeMillis();
        int numAttachments = s3Objects != null ? s3Objects.length : 0;
        logger.info("Retrieved " + numAttachments + " attachments for email ID " + emailId + " in " + (end - start) + " ms.");

        return attachments;
    }

    /**
     * Retrieves the bulk message IDs for the given account.
     *
     * @param accountId The account ID.
     * @return The bulk message IDs associated with the given account.
     * @throws S3ServiceException If an S3 exception is raised.
     */
    private String retrieveBulkMessageIds(int accountId) throws S3ServiceException {
        logger.info("Retrieving bulk message IDs for account " + accountId);

        String messageIds = null;

        S3Bucket bucket = getBucket(createMessageIdsBucketName(BULK_MESSAGE_IDS_BUCKET_NAME, awsBucketSuffix));

        long start = System.currentTimeMillis();

        String suffix = String.valueOf(accountId);
        String delimiter = null;
        S3Object[] s3Objects = s3Service.listObjects(bucket, suffix, delimiter);

        // we only expect one object
        if (s3Objects != null) {
            for (S3Object s3Object : s3Objects) {
                // load the complete object
                s3Object = s3Service.getObject(bucket, s3Object.getKey());
                messageIds = new String(getData(s3Object));

                logger.info("Created bulk message ID string from S3 object " + s3Object.getBucketName() + ":" + s3Object.getKey());
            }
        }

        long end = System.currentTimeMillis();

        logger.info("Retrieved bulk message IDs for account " + accountId + " in " + (end - start) + " ms.");

        return messageIds;
    }

    /**
     * Creates a S3 object, representing the given attachment.
     *
     * @param attachment The attachment.
     * @return The S3Object representation of the Attachment.
     * @throws S3ServiceException If an S3 exception is raised.
     */
    private S3Object createS3Object(Attachment attachment) throws S3ServiceException {
        try {
            byte[] data = attachment.getData();
            if (data == null) {
                logger.warn("Attachment data is null for attachment: " + attachment);

                data = new byte[0];
            }

            // NOTE: the stream is closed by the calling program after the s3 "put" call
            ByteArrayInputStream is = new ByteArrayInputStream(data);
            byte[] md5Hash = ServiceUtils.computeMD5Hash(is);
            is.reset();

            S3Object s3Object = new S3Object(createAttachmentS3Name(attachment));
            s3Object.setDataInputStream(is);
            s3Object.setContentLength(is.available());
            s3Object.setMd5Hash(md5Hash);
            s3Object.setContentType(Mimetypes.getInstance().getMimetype(attachment.getName()));

            s3Object.addMetadata(ATTACHMENT_ID_KEY, String.valueOf(attachment.getId()));
            s3Object.addMetadata(EMAIL_ID_KEY, String.valueOf(attachment.getEmailId()));
            s3Object.addMetadata(ATTACHMENT_NAME_KEY, attachment.getName());
            s3Object.addMetadata(ATTACHMENT_SIZE_KEY, s3Object.getContentLength());
            s3Object.addMetadata(COMMENT_KEY, attachment.getComment());
            s3Object.addMetadata(BROWSE_SEQ_KEY, attachment.getBrowseAttSeqNo());
            return s3Object;

        } catch (NoSuchAlgorithmException e) {
            throw new S3ServiceException("Error computing MD5 Hash for attachment " + attachment.getName(), e);
        } catch (IOException e) {
            throw new S3ServiceException("IO Exception while computing MD5 Hash for attachment " + attachment.getName(), e);
        }
    }

    /**
     * Creates the S3 name for the attachment using the convention:
     * <emailID>.<attachmentID>.<attachmentName>
     *
     * @param attachment the attachment
     * @return The S3 name for the attachment
     */
    private String createAttachmentS3Name(Attachment attachment) {
        return createAttachmentS3Name(attachment.getEmailId(), attachment.getName());
    }

    /**
     * Creates the S3 name for the attachment using the convention:
     * <emailID>.<attachmentID>.<attachmentName>
     *
     * @param emailId        The email ID
     * @param attachmentName The attachment name
     * @return The S3 name for the attachment
     */
    private String createAttachmentS3Name(int emailId, String attachmentName) {
        StringBuilder name = new StringBuilder();

        try {
            name.append(emailId)
                    .append(".")
                    .append(System.currentTimeMillis())
                    .append(".")
                    .append(java.net.URLEncoder.encode(attachmentName, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // UTF-8 is a supported, so we won't get here, but we'll dump the stack for completeness
            e.printStackTrace();
        }

        return (name.length() > S3_MAX_NAME_SIZE) ? name.substring(0, S3_MAX_NAME_SIZE) : name.toString();
    }

    private byte[] getData(S3Object s3Object) throws S3ServiceException {
        byte[] data = null;

        InputStream stream = s3Object.getDataInputStream();

        try {
            data = IOUtils.toByteArray(stream);
        } catch (IOException e) {
            logger.error("Error reading data from S3 Object \"" + s3Object.getKey() + "\": " + e.getMessage());
        } finally {
            closeStream(s3Object);
        }

        return data == null ? new byte[0] : data;
    }

    /**
     * Initializes the S3 interface by loading system configuration values and creating the required buckets.
     *
     * @throws RuntimeException if the S3 interface cannot be initialized.
     */
    private void initialize() {
        logger.info("Initializing Amazon S3 Interface...");

        try {
            boolean ok = true;

            final String awsAccessKey = getSystemConfiguration(SYS_CONFIG_AWS_ACCESS_KEY);   // "0GJDTF53000365MMQKR2"
            if (awsAccessKey == null) {
                ok = false;
                logger.fatal("Fatal Error. System Configuration " + SYS_CONFIG_AWS_ACCESS_KEY + " is not defined. Please check the sys-config table");
            }

            final String awsSecretKey = getSystemConfiguration(SYS_CONFIG_AWS_SECRET_KEY);   // "hwy2k72KuyMxVuqH5AopYCm45Qe+hJSFU8OxEdSh"
            if (awsSecretKey == null) {
                ok = false;
                logger.fatal("Fatal Error. System Configuration " + SYS_CONFIG_AWS_SECRET_KEY + " is not defined. Please check the sys-config table");
            }

            final String awsBucketSuffix = getSystemConfiguration(SYS_CONFIG_AWS_BUCKET_SUFFIX);
            if (awsBucketSuffix == null) {
                ok = false;
                logger.fatal("Fatal Error. System Configuration " + SYS_CONFIG_AWS_BUCKET_SUFFIX + " is not defined. Please check the sys-config table");
            }

            if (ok) {
                this.awsBucketSuffix = awsBucketSuffix;
                this.s3Service = new RestS3Service(new AWSCredentials(awsAccessKey, awsSecretKey));

                logger.info("Amazon S3 Interface initialized successfully!");
            } else {
                throw new RuntimeException("Error initializing Amazon S3 Interface!");
            }
        } catch (S3ServiceException e) {
            logger.fatal("Error initializing Amazon S3 Interface! ", e);
            throw new RuntimeException("Error initializing Amazon S3 Interface");
        }
    }

    private void initialize(String env) {
        logger.info("Initializing Amazon S3 Interface...");

        try {
            final String awsAccessKey = "0GJDTF53000365MMQKR2";

            final String awsSecretKey = "hwy2k72KuyMxVuqH5AopYCm45Qe+hJSFU8OxEdSh";

            this.awsBucketSuffix = env;
            this.s3Service = new RestS3Service(new AWSCredentials(awsAccessKey, awsSecretKey));

            logger.info("Amazon S3 Interface initialized successfully!");
        } catch (S3ServiceException e) {
            logger.fatal("Error initializing Amazon S3 Interface! ", e);
            throw new RuntimeException("Error initializing Amazon S3 Interface");
        }

    }


    /**
     * Returns the system configuration value identified by the given key.
     *
     * @param key The system configuration key.
     * @return The system configuration value or null if no such key exists.
     */
    private String getSystemConfiguration(String key) {
        return SysConfigManager.instance().getValue(key);
    }

    /**
     * Creates the attachment bucket name using the mail date, name and suffix.
     * <p/>
     * Note: S3 is limited to 100 buckets, so any bucket naming stategy needs to be
     * implemented with that limitation in mind.
     *
     * @param mailDate
     * @param name     The bucket name.
     * @param suffix   The bucket suffix.
     * @return
     */
    private static String createAttachmentBucketName(Date mailDate, String name, String suffix) {
        /*
         * This is required to support legacy-buckets, which were created without the mail date component.
         */
        if (logger.isTraceEnabled())
            logger.trace(String.format("createAttachmentBucketName(mailDate=%s, name=%s, suffix=%s)", String.valueOf(mailDate), name, suffix));

        if (mailDate != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM");
            String dateString = formatter.format(mailDate);
            name = dateString + "." + name;
        }

        return name + "." + suffix.toLowerCase();
    }

    /**
     * Creates the message ids bucket name using the name and suffix.
     *
     * @param name   The bucket name.
     * @param suffix The bucket suffix.
     * @return
     */
    private static String createMessageIdsBucketName(String name, String suffix) {
        if (logger.isTraceEnabled())
            logger.trace(String.format("createMessageIdsBucketName(name=%s, suffix=%s)", name, suffix));
        return name + "." + suffix.toLowerCase();
    }

    /**
     * Creates the builk message ids bucket name using the name and suffix.
     *
     * @param name   The bucket name.
     * @param suffix The bucket suffix.
     * @return
     */
    private static String createBulkMessageIdsBucketName(String name, String suffix) {
        if (logger.isTraceEnabled())
            logger.trace(String.format("createBulkMessageIdsBucketName(name=%s, suffix=%s)", name, suffix));
        return createMessageIdsBucketName(name, suffix);
    }

    /**
     * Returns the location of the object in S3. The location is a string of the form:
     * S3:<bucket name>/<object key>
     *
     * @param bucket
     * @param s3Object
     * @return
     */
    private String getLocation(S3Bucket bucket, S3Object s3Object) {
        return "S3:" + bucket.getName() + "/" + s3Object.getKey();
    }

    /**
     * Creates an S3 object placeholder (1Byte) , representing the message id.
     *
     * @param accountId The user's account id.
     * @param messageId The message id.
     * @return The S3Object representation of the message id.
     * @throws S3ServiceException If an S3 exception is raised.
     */
    private S3Object createS3Object(int accountId, String messageId) {
        byte[] data = new byte[]{0};

        // NOTE: the stream is closed by the calling program after the s3 "put" call
        ByteArrayInputStream is = new ByteArrayInputStream(data);

        S3Object s3Object = new S3Object(createMessageIdS3Name(accountId, messageId));
        s3Object.setDataInputStream(is);
        s3Object.setContentLength(is.available());

        s3Object.addMetadata(ACCOUNT_ID_KEY, String.valueOf(accountId));
        s3Object.addMetadata(MESSAGE_ID_KEY, String.valueOf(messageId));

        return s3Object;
    }

    /**
     * Creates a S3 object, representing the bulk message id.
     *
     * @param accountId  The user's account id.
     * @param messageIds The message id.
     * @return The S3Object representation of the message id.
     * @throws S3ServiceException If an S3 exception is raised.
     */
    private S3Object createBulkMessageIdsS3Object(int accountId, String messageIds) throws S3ServiceException {
        try {
            byte[] data = messageIds.getBytes();

            // NOTE: the stream is closed by the calling program after the s3 "put" call
            ByteArrayInputStream is = new ByteArrayInputStream(data);
            byte[] md5Hash = ServiceUtils.computeMD5Hash(is);
            is.reset();

            S3Object s3Object = new S3Object(String.valueOf(accountId));
            s3Object.setDataInputStream(is);
            s3Object.setContentLength(is.available());
            s3Object.setMd5Hash(md5Hash);

            s3Object.addMetadata(ACCOUNT_ID_KEY, String.valueOf(accountId));
            return s3Object;

        } catch (NoSuchAlgorithmException e) {
            throw new S3ServiceException("Error computing MD5 Hash for bulk message IDs for account " + accountId, e);
        } catch (IOException e) {
            throw new S3ServiceException("IO Exception while computing MD5 Hash for bulk message IDs for account " + accountId, e);
        }
    }

    /**
     * Retrieves the given message Id S3 object if it exists;
     *
     * @param accountId The email ID.
     * @return The message Ids for the account
     * @throws S3ServiceException If an S3 exception is raised.
     */
    public Set<String> retrieveMessageIds(int accountId) throws S3ServiceException {

        Set<String> messageIds = new HashSet<String>();

        S3Object[] s3Objects = retrieveMessageIdS3Objects(accountId);

        for (S3Object s3Object : s3Objects) {

            //messageIds.add((String)s3Object.getMetadata(MESSAGE_ID_KEY));
            String key = s3Object.getKey();

            int index = key.indexOf(".");

            if (index > 0) {
                messageIds.add(key.substring(index + 1));
            }

        }

        return messageIds;
    }

    /**
     * Retrieves the given message Id S3 object if it exists;
     *
     * @param accountId The email ID.
     * @return The message Ids for the account
     * @throws S3ServiceException If an S3 exception is raised.
     */
    private S3Object[] retrieveMessageIdS3Objects(int accountId) throws S3ServiceException {

        long start = System.currentTimeMillis();

        //List<S3Object> messageIds = new ArrayList<S3Object>();

        S3Bucket bucket = getBucket(createMessageIdsBucketName(MESSAGE_IDS_BUCKET_NAME, awsBucketSuffix));

        String suffix = String.valueOf(accountId);
        String delimiter = null;
        S3Object[] s3Objects = s3Service.listObjects(bucket, suffix, delimiter);

        /*
        if (s3Objects != null)
        {
            System.out.println("    Bucket: " + bucket.getName());
            for (int i = 0; i < s3Objects.length; i++) {
                S3Object s3Object = s3Objects[i];

                System.out.println("Object Key: " + s3Object.getKey());

                // load the complete object- metadata won't be available otherwise
                //s3Object = s3Service.getObject(bucket, s3Object.getKey());

                messageIds.add(s3Object);
            }
        }

        */
        long end = System.currentTimeMillis();
        int numMessageIds = s3Objects.length;
        logger.info("Retrieved " + numMessageIds + " message IDs for account ID " + accountId + " in " + (end - start) + " ms.");

        return s3Objects;
    }

    /**
     * Creates a S3 name for the message ID.
     *
     * @param accountId
     * @param messageId
     * @return
     */
    private String createMessageIdS3Name(int accountId, String messageId) {
        StringBuilder name = new StringBuilder();

        try {
            name.append(accountId)
                    .append(".")
                    .append(java.net.URLEncoder.encode(messageId, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // UTF-8 is a supported, so we won't get here, but we'll dump the stack for completeness
            e.printStackTrace();
        }

        // NOTE: If the name length exceeds the S3 limit, this may be ok since we'll have a long
        // enough name to work with which should minimize clashes. We'll log it though.
        if (name.length() > S3_MAX_NAME_SIZE) {
            logger.warn("Message ID S3 name " + name + " exceeds the S3 max name legth - the name has been truncated to " + S3_MAX_NAME_SIZE + " characters.");
        }

        return (name.length() > S3_MAX_NAME_SIZE) ? name.substring(0, S3_MAX_NAME_SIZE) : name.toString();
    }

    /**
     * Parses the message IDs from the bulk message ID string.
     *
     * @param bulkMessageIds
     * @return A map of message number to message ID
     */
    private Map<Integer, String> getMessageIdsFromBulk(String bulkMessageIds) {
        String[] list = bulkMessageIds.split("\r\n");

        Map<Integer, String> messageIds = new HashMap<Integer, String>();

        // with gmail each id has this form: "3 GmailId122656c9cde5d09c"
        for (String idInfo : list) {
            try {
                String[] id = idInfo.split(" ");

                messageIds.put(Integer.valueOf(id[0]), id[1]);
            } catch (Throwable t) {
                logger.warn("Unable to determine the id from uidl stream %s for " + idInfo);
            }
        }

        return messageIds;
    }

    private void closeStream(S3Object s3Object) throws S3ServiceException {
        InputStream stream = s3Object != null ? s3Object.getDataInputStream() : null;
        if (stream != null) {
            try {
                stream.close();
            } catch (Exception e) {
                // nothing to do here
            }
        }
    }
}


