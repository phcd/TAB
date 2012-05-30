package com.archermind.txtbl.receiver.mail.support;

import com.archermind.txtbl.dal.business.impl.UserService;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.OriginalReceivedAttachment;
import com.archermind.txtbl.domain.OriginalReceivedEmail;
import com.archermind.txtbl.mail.store.MessageStoreException;
import com.archermind.txtbl.receiver.mail.bean.*;
import com.archermind.txtbl.utils.*;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.apache.commons.digester.Digester;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.jboss.logging.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.util.HtmlUtils;

import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class NewMSPSupport extends NewProviderSupport {
    private static final Logger log = Logger.getLogger(ActiveSyncSupport.class);

    private static final int CONNECTION_TIMEOUT = Integer.valueOf(SysConfigManager.instance().getValue("messageStoreConnectionTimeout", "30000"));
    private static final int MAX_CONNECTIONS_PER_CLIENT = Integer.valueOf(SysConfigManager.instance().getValue("messageStoreMaxConnectionsPerClient", "10000"));
    private static Collection<String> mspRecoverableErrorMessages = new HashSet<String>();
    private static Collection<String> mspFatalErrorMessages = new HashSet<String>();
    private HttpClient httpClient = new HttpClient(getConnectionManager());

    private static final String STANDARD_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static String loginRequest;
    private static String fullFolderListRequest;
    private static String fullMessageListRequest;
    private static String headerAndAttachmentListRequest;
    private static String getAttachmentRequest;
    private static String security;
    private static String subscribeRequest;


    static {
        loginRequest = loadResource("com/archermind/txtbl/receiver/mail/prop/loginRequest.xml");

        loginRequest = loadResource("com/archermind/txtbl/receiver/mail/prop/loginRequest.xml");
        fullFolderListRequest = loadResource("com/archermind/txtbl/receiver/mail/prop/fullFolderListRequest.xml");
        fullMessageListRequest = loadResource("com/archermind/txtbl/receiver/mail/prop/fullMessageListRequest.xml");
        security = loadResource("com/archermind/txtbl/receiver/mail/prop/security.xml");
        headerAndAttachmentListRequest = loadResource("com/archermind/txtbl/receiver/mail/prop/headerAndAttachmentListRequest.xml");
        getAttachmentRequest = loadResource("com/archermind/txtbl/receiver/mail/prop/getAttachmentRequest.xml");
        subscribeRequest = loadResource("com/archermind/txtbl/receiver/mail/prop/subscribe.xml");

        mspFatalErrorMessages.add("Windows Live Hotmail is temporarily unavailable");
        mspFatalErrorMessages.add("Service/Auth Check not available due to System failure");
        mspFatalErrorMessages.add("SystemFailure");

        // allowing some extra ability to add to this list in case we see new error messages
        String additionalMessages = SysConfigManager.instance().getValue("msp.fatal.error.messages");

        if (StringUtils.isNotEmpty(additionalMessages)) {
            String[] messages = additionalMessages.split(",");

            if (messages.length > 0) {
                mspFatalErrorMessages.addAll(Arrays.asList(messages));
            }
        }

        mspRecoverableErrorMessages.add("Sign in failed");
        mspRecoverableErrorMessages.add("The request failed. Please try again.");
        mspRecoverableErrorMessages.add("InvalidSecurity");
        mspRecoverableErrorMessages.add("Mobile Token Was Invalid");
        mspRecoverableErrorMessages.add("Message Was Received Outside Of Accepted");

        // allowing some extra ability to add to this list in case we see new error messages
        additionalMessages = SysConfigManager.instance().getValue("msp.recoverable.error.messages");

        if (StringUtils.isNotEmpty(additionalMessages)) {
            String[] messages = additionalMessages.split(",");

            if (messages.length > 0) {
                mspRecoverableErrorMessages.addAll(Arrays.asList(messages));
            }
        }
    }

    public MSPLoginBean login(String name, String password) throws Exception {
        String request = setParameterValues(loginRequest, new String[]{"username", "password"}, new String[]{name, password});

        String response;
        if (name.endsWith("msn.com")) {
            response = post(httpClient, "https://msnia.login.live.com/pp600/RST.srf", request);
        } else
            response = post(httpClient, "https://login.passport.com/RST.srf", request);

        Digester digester = new Digester();
        digester.addObjectCreate("S:Envelope", MSPLoginBean.class);
        digester.addCallMethod("S:Envelope/S:Body/wst:RequestSecurityTokenResponseCollection/wst:RequestSecurityTokenResponse/wst:LifeTime/wsu:Created", "setCreatedTime", 0);
        digester.addCallMethod("S:Envelope/S:Fault/faultstring", "setError", 0);
        InputStream responseStream = new ByteArrayInputStream(response.getBytes("UTF-8"));
        MSPLoginBean result = (MSPLoginBean) digester.parse(responseStream);
        responseStream.close();
        if (StringUtils.isEmpty(result.getError())) {
            result.setToken(getSecurityid(response));
        }
        return result;
    }

    /**
     * This method makes subscribe requesto to server
     *
     * @param messageId id of message
     * @param loginBean bean with credentials
     * @param url       connection url
     * @return result bean with response identifirs
     * @throws Exception raised exception
     */
    public MSPSubscribeBean subcribeAlert(String messageId, MSPLoginBean loginBean, String url, StopWatch watch, String context, String expireDate) throws Exception {
        String request = setParameterValues(subscribeRequest, new String[]{"MessageId", "Securityid", "Url", "ExpireDate"}, new String[]{StringUtils.emptyStringIfNull(messageId), addSecurity(loginBean.getCreatedTime(), loginBean.getToken()), url, expireDate});

        StopWatchUtils.newTask(watch, "Sending subscribe alert request", context, log);
        log.info(String.format("subscribe request = %s", request));
        String response = basicAuthPost(httpClient, "https://http.mws.mobile.live.com/2006/10/MWP2007_02/MailService.asmx", request, "application/soap+xml; charset=utf-8; action=\"http://schemas.live.com/mws/2006/10/mail/Subscribe\"");
        StopWatchUtils.newTask(watch, "Parsing of subscribe alert response", context, log);
        Digester digester = new Digester();
        digester.addObjectCreate("soap:Envelope", MSPSubscribeBean.class);
        digester.addCallMethod("soap:Envelope/soap:Header/Billing/TransactionID", "setTransactionID", 0);
        digester.addCallMethod("soap:Envelope/soap:Header/Billing/WebServiceCustomerID", "setWebServiceCustomerID", 0);
        InputStream responseStream = new ByteArrayInputStream(response.getBytes("UTF-8"));
        MSPSubscribeBean result = (MSPSubscribeBean) digester.parse(responseStream);
        responseStream.close();
        result.setResponse(response);
        return result;
    }

    /**
     * Method replaces parameters by given values. Parameter should be marked in text by braces.
     * For example {paramName}. Parameter names are matched to values by index.
     *
     * @param text        xml content to be processed
     * @param paramNames  parameter names
     * @param paramValues parameter values
     * @return result xml text
     */
    private String setParameterValues(String text, String[] paramNames, String[] paramValues) {
        String result = text;
        for (int i = 0; i < paramNames.length; i++) {
            result = result.replace("{" + paramNames[i] + "}", paramValues[i]);
        }
        return result;
    }

    /**
     * Method returns secufity token for signing mail requests
     *
     * @param createdTime create time
     * @param securityid  security token value
     * @return security xml
     */
    public String addSecurity(String createdTime, String securityid) {
        String request = security;
        return setParameterValues(request, new String[]{"createdTime", "securityid"},
                new String[]{createdTime, securityid});
    }


    /**
     * Method for getting full message headers list from given folder
     *
     * @param messageId message id
     * @param folderId  required folder id
     * @param loginBean credentials bean
     * @return result xml response
     * @throws Exception raised exception
     */
    public String getFullMessageList(String messageId, String folderId, MSPLoginBean loginBean, StopWatch watch, String context) throws Exception {
        StopWatchUtils.newTask(watch, "Get messages from inbox", context, log);
        String request = setParameterValues(fullMessageListRequest, new String[]{"MessageId", "FolderId", "Security"}, new String[]{StringUtils.emptyStringIfNull(messageId), StringUtils.emptyStringIfNull(folderId), addSecurity(loginBean.getCreatedTime(), loginBean.getToken())});
        return basicAuthPost(httpClient, "https://http.mws.mobile.live.com/2006/10/MWP2007_02/MailService.asmx", request, "application/soap+xml; charset=utf-8; action=\"http://schemas.live.com/mws/2006/10/mail/Sync\"");
    }

    /**
     * Method returns populated mail bean for given messageId and folderId. Result bean then processed by getMail method.
     *
     * @param mailMessageId  mail message id
     * @param folderId       folder id
     * @param messageId      message id
     * @param createdTime    create time
     * @param securityid     security token
     * @param hasAttachments attachment indicator. if it's false then method will
     *                       not request server for attachment bodies
     * @return populated bean
     * @throws Exception raised exception
     */
    public MSPMailBean getHeaderAndAttachementList(String mailMessageId, String folderId, String messageId,
                                                   String createdTime, String securityid, boolean hasAttachments) throws Exception {
        String request = setParameterValues(headerAndAttachmentListRequest,
                new String[]{"MessageId", "FolderId", "MailMessageId", "Security"},
                new String[]{messageId, folderId, mailMessageId, addSecurity(createdTime, securityid)});
        String response = basicAuthPost(httpClient,
                "https://http.mws.mobile.live.com/2006/10/MWP2007_02/MailService.asmx",
                request,
                "application/soap+xml; charset=utf-8; action=\"http://schemas.live.com/mws/2006/10/mail/Sync\"");
        Digester digester = new Digester();
        digester.addObjectCreate("soap:Envelope", MSPMailBean.class);
        digester.addCallMethod("soap:Envelope/soap:Body/DataSetChanges/p5:UpdatedObject/Message/From/Name", "setBcc", 0);
        digester.addCallMethod("soap:Envelope/soap:Body/DataSetChanges/p5:UpdatedObject/Message/From/Name", "setEmailFromAlias", 0);
        digester.addCallMethod("soap:Envelope/soap:Body/DataSetChanges/p5:UpdatedObject/Message/From/Email", "setEmailFrom", 0);
        digester.addCallMethod("soap:Envelope/soap:Body/DataSetChanges/p5:UpdatedObject/Message/Subject", "setSubject", 0);
        digester.addCallMethod("soap:Envelope/soap:Body/DataSetChanges/p5:UpdatedObject/Message/DateSent", "setMailTime", 0);
        digester.addCallMethod("soap:Envelope/soap:Body/DataSetChanges/p5:UpdatedObject/Message/PlainBody", "setPlainBody", 0);
        digester.addCallMethod("soap:Envelope/soap:Body/DataSetChanges/p5:UpdatedObject/Message/Attachment/p9:FileName", "addArrachment", 0);
        digester.addCallMethod("soap:Envelope/soap:Body/DataSetChanges/p5:UpdatedObject/Message/RecipientList", "setRecipientValue", 0);
        digester.addSetProperties("soap:Envelope/soap:Body/DataSetChanges/p5:UpdatedObject/Message/RecipientList",
                "p9:Type", "recipientType");
        digester.addCallMethod("soap:Envelope/soap:Body/soap:Fault/soap:Reason/soap:Text", "setErrorText", 0);
        InputStream responseStream = new ByteArrayInputStream(response.getBytes("UTF-8"));
        MSPMailBean result = (MSPMailBean) digester.parse(responseStream);
        responseStream.close();
        result.setResponse(response);

        if (hasAttachments) {
            request = setParameterValues(getAttachmentRequest, new String[]{"MessageId", "FolderId", "MailMessageId", "Security"},
                    new String[]{messageId, folderId, mailMessageId, addSecurity(createdTime, securityid)});
            response = basicAuthPost(httpClient, "https://http.mws.mobile.live.com/2006/10/MWP2007_02/MailService.asmx",
                    request,
                    "application/soap+xml; charset=utf-8; action=\"http://schemas.live.com/mws/2006/10/mail/Sync\"");
            digester = new Digester();
            digester.push(result);
            digester.addCallMethod("soap:Envelope/soap:Body/DataSetChanges/p5:UpdatedObject/Message/Attachment/p9:Blob/p9:Data",
                    "setAttachData", 0);
            digester.addSetProperties("soap:Envelope/soap:Body/DataSetChanges/p5:UpdatedObject/Message/Attachment",
                    "p9:AttachmentID", "attachNumber");
            responseStream = new ByteArrayInputStream(response.getBytes("UTF-8"));
            digester.parse(responseStream);
            responseStream.close();
        }
        return result;

    }

    /**
     * Method makes mail bean by given folderId and messageId for processing in provider class
     *
     * @param account
     * @param folderId       folder id
     * @param messageId      message id
     * @param mailMessageId  mail message id
     * @param loginBean      credentials bean
     * @param userId         user id
     * @param accountName    account name
     * @param mailSize       mail size. If it's greater than defined in "maxMessageSize" value then returned mail
     *                       content will be replaced with predefined value.
     * @param hasAttachments attachemnts indicator         @return populated mail bean
     * @throws Exception raised exception
     */
    public OriginalReceivedEmail getMail(Account account, String messageId, String mailMessageId, MSPLoginBean loginBean, String userId, String accountName, int mailSize, boolean hasAttachments) throws Exception {

        MSPMailBean headerAndAttachementList = null;

        try {

            List<OriginalReceivedAttachment> attachList = new ArrayList<OriginalReceivedAttachment>();
            OriginalReceivedEmail mail = new OriginalReceivedEmail();

            headerAndAttachementList = this.getHeaderAndAttachementList(mailMessageId, account.getFolder_id(), messageId, loginBean.getCreatedTime(), loginBean.getToken(), hasAttachments);
            //when 'to' is null in response
            if (StringUtils.isEmpty(headerAndAttachementList.getEmailTo())) {
                headerAndAttachementList.setEmailFrom(accountName);
            }
            if (StringUtils.isNotEmpty(headerAndAttachementList.getErrorText())) {
                log.info("HotMailSupport: " + headerAndAttachementList.getErrorText());
                return null;
            }
            mail.setUid(mailMessageId);
            mail.setUserId(userId);
            mail.setEmailFrom(headerAndAttachementList.getEmailFrom());
            mail.setCc(headerAndAttachementList.getCc());
            mail.setBcc(headerAndAttachementList.getBcc());
            Date date = getMailDate(headerAndAttachementList);
            mail.setMailTime(dateToStr(date));
            mail.setEmailTo(headerAndAttachementList.getEmailTo());
            mail.setEmailFromAlias(headerAndAttachementList.getEmailFromAlias());

            String subject = headerAndAttachementList.getSubject();

            if (subject != null) {
                subject = HtmlUtils.htmlUnescape(subject);
            }

            mail.setSubject(subject);

            if (mailSize > maximumMessageSize) {
                String emailDroppedMessage = getEmailDroppedMessage(account, date, mailSize, mail.getEmailFrom());
                mail.setBody(HtmlUtils.htmlUnescape(emailDroppedMessage).getBytes());
                return mail;
            } else if (!StringUtils.isEmpty(headerAndAttachementList.getPlainBody())) {
                mail.setBody(HtmlUtils.htmlUnescape(headerAndAttachementList.getPlainBody()).getBytes());
            } else {
                mail.setBody("".getBytes());
            }

            if (hasAttachments) {
                for (int i = 0; i < headerAndAttachementList.getAttachments().size(); i++) {
                    OriginalReceivedAttachment attachment = new OriginalReceivedAttachment();
                    attachment.setData(Base64.decode(headerAndAttachementList.getAttachments().get(i).getData()));
                    attachment.setName(headerAndAttachementList.getAttachments().get(i).getFileName());
                    attachment.setSize(attachment.getData() == null ? 0 : attachment.getData().length);
                    attachList.add(attachment);
                }
            }
            mail.setAttachList(attachList);


            mail.getAttachList().addAll(LinkProcessor.getInstance().createLinkAttachments(new String(mail.getBody())));

            return mail;
        } catch (Throwable t) {
            try {
                throw new RuntimeException(String.format("SOAP fault encountered while processing msp message for account=%s, uid=%s, fault: %s",
                        accountName, userId, headerAndAttachementList.getErrorText()));
            } catch (Throwable unused) {
                // we throw the original exception
                throw new RuntimeException(String.format("Unable to parse msp message for account=%s, uid=%s, response was %s",
                        accountName, userId, headerAndAttachementList.getResponse()), t);
            }
        }
    }

    private Date getMailDate(MSPMailBean headerAndAttachementList) throws ParseException {
        String dateString = headerAndAttachementList.getMailTime();
        if (dateString.toUpperCase().endsWith("Z")) {
            String temp[] = dateString.split("T");
            if (temp.length > 1) {
                temp[1] = temp[1].substring(0, 8);
                SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                TimeZone timeZone = TimeZone.getTimeZone("UTC");
                f.setTimeZone(timeZone);
                return f.parse(temp[0] + "T" + temp[1] + "Z");
            }
        }
        String tempStr = dateString.substring(dateString.length() - 9, dateString.length());
        if (tempStr.startsWith(":") && "-".equals(tempStr.substring(3, 4)) && ":".equals(tempStr.substring(6, 7))) {
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            return f.parse(dateString.substring(0, 22) + dateString.substring(23, dateString.length()));
        }
        return new Date();
    }

    /**
     * Util method for converting java.util.Date to MSP mail date format
     *
     * @param date value to be converted
     * @return formatted date value
     */
    public static String dateToStr(Date date) {
        String dateTime = "";
        DateFormat dateFormat = new SimpleDateFormat(STANDARD_DATE_FORMAT);
        TimeZone timeZone = TimeZone.getDefault();
        dateFormat.setTimeZone(timeZone);
        if (date != null) {
            dateTime = dateFormat.format(date);
        }
        return dateTime;
    }

    /**
     * Util method for converting MSP mail date format into java.util.Date object
     *
     * @param text value to be converted
     * @return formatted date value
     */
    public Date strToDate(String text) {
        if (StringUtils.isEmpty(text)) {
            return null;
        } else {
            DateFormat dateFormat = new SimpleDateFormat(STANDARD_DATE_FORMAT);

            try {
                return dateFormat.parse(text);
            } catch (Throwable t) {
                log.warn(String.format("Unable to parse %s, error %s", text, t.toString()));
                return null;
            }
        }
    }

    /**
     * Method makes corresponding request to server and returms bean with folder list result
     *
     * @param loginBean credentials bean
     * @param MessageId mail message id
     * @return result bean
     * @throws Exception raised exception
     */
    public MSPFullFolderListResponse getFullFolderList(MSPLoginBean loginBean, String MessageId, StopWatch watch, String context)
            throws Exception {
        String messId = StringUtils.isEmpty(MessageId) ? createMessageId() : MessageId;
        String securityId = "";
        String createdTime;
        if (loginBean != null) {
            securityId = loginBean.getToken();
            createdTime = loginBean.getCreatedTime();
        } else {
            createdTime = getCreatedTime();
        }
        StopWatchUtils.newTask(watch, "Requesting Full FolderList", context, log);
        String request = setParameterValues(fullFolderListRequest, new String[]{"MessageId", "Security"},
                new String[]{messId, addSecurity(createdTime, securityId)});
        String response = basicAuthPost(httpClient, "https://http.mws.mobile.live.com/2006/10/MWP2007_02/MailService.asmx", request, "application/soap+xml; charset=utf-8; action=\"http://schemas.live.com/mws/2006/10/mail/Sync\"");

        Digester digester = new Digester();

        StopWatchUtils.newTask(watch, "Parsing  List of Folders", context, log);
        digester.addObjectCreate("soap:Envelope", MSPFullFolderListResponse.class);
        digester.addCallMethod("soap:Envelope/soap:Header/wsa:MessageID", "setMessagingId", 0);
        digester.addCallMethod("soap:Envelope/soap:Body/DataSetChanges/p5:NewObject/p5:ObjectReference/ReferenceProperties/MailFolderID"
                , "addFolderId", 0);
        digester.addCallMethod("soap:Envelope/soap:Body/soap:Fault/soap:Reason/soap:Text", "setError", 0);
        ByteArrayInputStream responseStream = new ByteArrayInputStream(response.getBytes("UTF-8"));
        MSPFullFolderListResponse result = (MSPFullFolderListResponse) digester.parse(responseStream);
        responseStream.close();
        result.setResponse(response);
        return result;

    }

    /**
     * Util method that returns security token value
     *
     * @param soap server response
     * @return token value
     * @throws Exception raised exception
     */
    public String getSecurityid(String soap) throws Exception {
        return Base64
                .encode(("<EncryptedData xmlns=\"http://www.w3.org/2001/04/xmlenc#\" Id=\"Assertion1\" "
                        + soap
                        .split("<EncryptedData xmlns=\"http://www.w3.org/2001/04/xmlenc#\" Id=\"Assertion1\"")[1]
                        .split("</EncryptedData>")[0] + "</EncryptedData>")
                        .getBytes());
    }

    public String post(HttpClient httpClient, String url, String body)
            throws Exception {
        PostMethod post = new PostMethod(url);
        RequestEntity entity = new StringRequestEntity(body, null, null);
        post.setRequestEntity(entity);
        httpClient.executeMethod(post);
        return post.getResponseBodyAsString();
    }

    public static String basicAuthPost(HttpClient httpClient,
                                       String url, String body, String ContentType) throws Exception {
        PostMethod post = new PostMethod(url);
        post.setRequestHeader("Content-Type", ContentType);
        post.setRequestHeader("Authorization",
                "Basic bGR+X1QjeWZTNiMhOl84X1F+YyE3VyNTRg==");

        RequestEntity entity = new StringRequestEntity(body, null, null);
        post.setRequestEntity(entity);
        httpClient.executeMethod(post);
        return post.getResponseBodyAsString();
    }

    /**
     * Method for parsing given server response and getting mail headers array of MSPMessageHeader instances.
     *
     * @param s server response to parse
     * @return result array
     * @throws Exception raised exception
     */
    public MSPEmailHeaders getMailHeaders(String s, StopWatch watch, String context)
            throws Exception {

//		log.info("---------------------getMailHeaders-------------------------------");
//		log.info(s);
//		log.info("------------------------------------------------------------------");
        StopWatchUtils.newTask(watch, "Parsing list of messages from inbox", context, log);
        Digester digester = new Digester();
        MSPEmailHeaders headers = new MSPEmailHeaders();
        digester.push(headers);
        digester.addCallMethod("soap:Envelope/soap:Body/DataSetChanges/p5:NewObject", "addHeader", 3,
                new Class[]{String.class, Integer.class, Boolean.class});
        digester.addCallParam("soap:Envelope/soap:Body/DataSetChanges/p5:NewObject/p5:ObjectReference/ReferenceProperties/MailMessageID", 0);
        digester.addCallParam("soap:Envelope/soap:Body/DataSetChanges/p5:NewObject/Message/Size", 1);
        digester.addCallParam("soap:Envelope/soap:Body/DataSetChanges/p5:NewObject/Message/HasAttachments", 2);
        digester.addCallParam("soap:Envelope/soap:Body/DataSetChanges/p5:NewObject/Message/HasAttachments", 2);
        digester.addCallMethod("soap:Envelope/soap:Body/soap:Fault/soap:Reason/soap:Text", "setError", 0);
        InputStream responseStream = new ByteArrayInputStream(s.getBytes("UTF-8"));
        digester.parse(responseStream);
        responseStream.close();
        return headers;
    }

    private static String createMessageId() {
        Random rand = new Random();
        return Integer.toString(rand.nextInt(9999999)) + "-"
                + Integer.toString(rand.nextInt(9999)) + "-"
                + Integer.toString(rand.nextInt(9999)) + "-"
                + Integer.toString(rand.nextInt(9999)) + "-"
                + Integer.toString(rand.nextInt(999999999));
    }

    public boolean hasRecoverableFailure(String errorMessage) {
        boolean result = false;
        if (!StringUtils.isEmpty(errorMessage)) {
            for (String message : mspRecoverableErrorMessages) {
                if (errorMessage.contains(message)) {
                    result = true;
                }
            }
        }
        return result;
    }

    public boolean hasFatalFailure(String errorMessage) {
        boolean result = false;
        if (!StringUtils.isEmpty(errorMessage)) {
            for (String message : mspFatalErrorMessages) {
                if (errorMessage.contains(message)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    public static String getCreatedTime() {
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        TimeZone timeZone = TimeZone.getTimeZone("GMT");
        dateFormat.setTimeZone(timeZone);
        return dateFormat.format(date);
    }

    /**
     * Util method for mail processing in provider class.
     *
     * @param to
     * @param from
     * @param subject
     * @param body
     * @param sentDate
     * @param contType
     * @param cc
     * @param bcc
     * @param attachmentsList
     * @return
     */
    public Message getMessage(String to, String from, String subject, String body, Date sentDate, String contType,
                              String cc, String bcc, List<OriginalReceivedAttachment> attachmentsList) {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage msg = null;
        try {
            msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from));
            msg.addRecipients(Message.RecipientType.TO, UtilsTools.tranAddr(to));
            msg.setSubject(subject);
            msg.setContent(body, contType);
            msg.setSentDate(sentDate);
            if ((attachmentsList != null) && (attachmentsList.size() > 0)) {
                MimeMultipart mp = new MimeMultipart("multipart");
                for (OriginalReceivedAttachment attach : attachmentsList) {
                    log.info("Processing attachment " + attach);
                    BodyPart tmpAttachBody = new MimeBodyPart();
                    tmpAttachBody.setFileName(attach.getName());
                    tmpAttachBody.setDataHandler(new DataHandler(new ByteArrayDataSource(attach.getData(), "application/octet-stream")));
                    mp.addBodyPart(tmpAttachBody);
                }
                BodyPart bodyPart = new MimeBodyPart();
                if (body != null) {
                    bodyPart.setText(body);
                } else {
                    bodyPart.setText("");
                }
                mp.addBodyPart(bodyPart);
                msg.setContent(mp);
                msg.setHeader("Content-Type", "multipart/*");
            }
            msg.addRecipients(Message.RecipientType.CC, UtilsTools.tranAddr(cc));
            msg.addRecipients(Message.RecipientType.BCC, UtilsTools.tranAddr(bcc));
        } catch (Exception e) {
            log.warn(String.format("unable to get message from %s to %s", from, to));
        }
        return msg;
    }

    public Message processMessage(Account account, int messageNumber, Set<String> storeMessageIds, String storeBucket, String messageId, boolean hasAttachments, MSPLoginBean credentials, int size, EmailIdStoreProcess emailProcess, String context, StopWatch watch) throws Exception {
        return processMessage(account, storeBucket, new EmptyMessage(), messageNumber, messageId, context, watch, getMessageValidators(account, storeMessageIds, storeBucket, context, watch, credentials, size, hasAttachments), emailProcess);
    }

    private List<MessageValidator> getMessageValidators(final Account account, final Set<String> storeMessageIds, final String storeBucket, final String context, final StopWatch watch, final MSPLoginBean credentials, final int size, final boolean hasAttachments) {
        ArrayList<MessageValidator> messageValidators = new ArrayList<MessageValidator>();
        messageValidators.add(new DoNothingOnFailureMessageValidator() {
            public Message validate(Message message, int messageNumber, String messageId) throws Exception {
                if (isMessageAlreadyProcessed(messageId, account, storeBucket, storeMessageIds)) {
                    return null;
                }
                return message;
            }
        });
        messageValidators.add(new MessageValidator() {
            public Message validate(Message message, int messageNumber, String messageId) throws Exception {
                StopWatchUtils.newTask(watch, "Downloading email", context, log);
                OriginalReceivedEmail originalEmail = getMail(account, messageId, messageId, credentials, account.getUser_id(), account.getName(), size, hasAttachments);
                message = getMessage(originalEmail.getEmailTo(), originalEmail.getEmailFrom(), originalEmail.getSubject(), new String(originalEmail.getBody()), strToDate(originalEmail.getMailTime()), originalEmail.getMail_type(), originalEmail.getCc(), originalEmail.getBcc(), originalEmail.getAttachList());
                if (isSentMessage(account, message)) {
                    return null;
                }
                return message;
            }
        });
        messageValidators.add(new MessageValidator() {
            public Message validate(Message message, int messageNumber, String messageId) throws Exception {
                if (isMessageTooOld(account, message, context)) {
                    return null;
                }
                return message;
            }
        });
        return messageValidators;
    }


    public int handleFirstTime(Account account, String bucket, StopWatch watch, String context, int folderDepth, int newMessages, MSPLoginBean credentials, List<MSPMessageHeader> messageHeaders, Set<String> storedMessageIds) throws Exception {
        Date lastMessageReceivedDate = null;
        EmailIdStoreProcess emailProcess = getEmailProcess(account, bucket, watch, context, storedMessageIds);
        StopWatchUtils.newTask(watch, "Start first time receive", context, log);

        //Saving last 5 messages
        for (int messageNumber = 0; (messageNumber < 5) && (messageNumber < messageHeaders.size()); messageNumber++) {
            MSPMessageHeader currentMessageHeader = messageHeaders.get(messageNumber);
            String messageId = currentMessageHeader.getMessageId();

            StopWatchUtils.newTask(watch, "Processing email", context, log);
            Message savedMessage = processMessage(account, messageNumber + 1, storedMessageIds, bucket, messageId, currentMessageHeader.getHasAttachments(), credentials, currentMessageHeader.getSize(), emailProcess, context, watch);
            if (savedMessage != null) {
                if (lastMessageReceivedDate == null || lastMessageReceivedDate.before(savedMessage.getSentDate())) {
                    lastMessageReceivedDate = savedMessage.getSentDate();
                }

                newMessages++;
            }
        }
        // Mail was received successfully. Now update last_received_date in email account
        StopWatchUtils.newTask(watch, "Storing ids", context, log);
        handleMigratedAccount(messageHeaders, account, bucket, folderDepth, lastMessageReceivedDate);
        return newMessages;
    }

    public void handleMigratedAccount(List<MSPMessageHeader> messages, Account account, String bucket, int folderDepth, Date lastMessageReceivedDate) throws Exception{
        storeIds(messages, account, bucket);
        updateAccount(account, null, 0, folderDepth, lastMessageReceivedDate);
    }



    private void storeIds(List<MSPMessageHeader> messages, Account account, String bucket) throws MessageStoreException {
        List<String> ids = new ArrayList<String>();

        for (int i = 0; (i < messages.size()) && (messages.get(i) != null); i++) {
            // the formatting below is necessary to stay consistent with POP bulk imports
            String messageId = messages.get(i).getMessageId();
            ids.add(messageId + " " + messageId);
        }

        getMessageIdStore().addMessageInBulk(account.getId(), bucket, IdUtil.encodeMessageIds(ids), account.getCountry());
    }

    public EmailIdStoreProcess getEmailProcess(Account account, String bucket, StopWatch watch, String context, Set<String> storedMessageIds) {
        EmailSaveProcess saveProcess = new EmailSaveProcess(account, watch, context);
        return new EmailIdStoreProcess(account, storedMessageIds, bucket, context, watch, saveProcess);
    }

    /**
     * Util method for loading requests files
     *
     * @param filePath path to xml file
     * @return file content
     */
    static public String loadResource(String filePath) {

        StringBuilder sb = new StringBuilder();
        BufferedReader fr = null;

        // Return result xml content with truncated lead and trail spaces in element bodies
        try {
            InputStream in = new ClassPathResource(filePath).getInputStream();
            fr = new BufferedReader(new InputStreamReader(in));
            String currentLine = fr.readLine();
            while (currentLine != null) {
                // this statement reads the line from the file and print it to
                // the console.
                sb.append(currentLine.trim());
                sb.append(' ');
                currentLine = fr.readLine();
            }

        } catch (Exception e) {
            log.error(String.format("Error load %s file content: ", filePath), e);
        } finally {
            try {
                if (fr != null) {
                    fr.close();
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        return sb.toString().replace("> ", ">").replace(" <", "<");
    }

    private MultiThreadedHttpConnectionManager getConnectionManager() {
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();

        HttpConnectionManagerParams params = new HttpConnectionManagerParams();
        params.setMaxTotalConnections(MAX_CONNECTIONS_PER_CLIENT);
        params.setConnectionTimeout(CONNECTION_TIMEOUT);
        params.setSoTimeout(CONNECTION_TIMEOUT);

        connectionManager.setParams(params);

        return connectionManager;
    }

    public boolean isTimeToReconcile(Account account) {
        UserService userService = new UserService();
        Date accountResult = userService.getAccountSubscribeAlertDate(account.getName());
        return accountResult == null || (System.currentTimeMillis() - accountResult.getTime()) > 1000l * 60 * 60 * 24 * Long.valueOf(SysConfigManager.instance().getValue("subscribeAlertIntervalInDays", "15"));
    }

}
