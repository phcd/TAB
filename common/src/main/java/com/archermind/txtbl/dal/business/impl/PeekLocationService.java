package com.archermind.txtbl.dal.business.impl;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.dal.business.IPeekLocationService;
import com.archermind.txtbl.dal.orm.*;
import com.archermind.txtbl.domain.*;

import com.archermind.txtbl.pushmail.utility.Common;
import com.archermind.txtbl.pushmail.utility.TopicInfo;
import com.archermind.txtbl.utils.*;
import com.sun.mail.smtp.SMTPMessage;
import org.jboss.logging.Logger;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class PeekLocationService implements IPeekLocationService {
    private static final Logger logger = Logger.getLogger(PeekLocationService.class);

    private static final boolean userMasterForPeekLocationReads;
    private static final boolean isTransliteratePoke;
    private static final boolean isTransliteratePokePeekvetica;

    static {
        userMasterForPeekLocationReads = Boolean.parseBoolean(SysConfigManager.instance().getValue("userMasterForPeekLocationReads", "false"));
        isTransliteratePoke = Boolean.parseBoolean(SysConfigManager.instance().getValue("transliteratePoke", "true"));
        isTransliteratePokePeekvetica = Boolean.parseBoolean(SysConfigManager.instance().getValue("transliteratePokePeekvetica", "true"));
    }

    /**
     * Updates the location of the device.
     *
     * @param device The device
     * @throws DALException if there's a DB failure
     */
    public void updateLocation(Device device) throws DALException {
        PeekLocationORMap peekLocationORMap = new PeekLocationORMap();
        try {
            /*
                * Note - we might want to check if the user has an active content service subscription
                * before creating/updating the location record.
                */
            peekLocationORMap.updateLocation(device);

        } catch (SQLException e) {
            DALException dalException = new DALException(ErrorCode.CODE_DAL_, e);
            logger.error("Peek location update failed for device: " + device.getDeviceCode(), dalException);
            throw dalException;
        }
    }

    /**
     * Returns the location information for the device using a registered email address.
     *
     * @param key   Partner API key.
     * @param email The email address
     * @return The device location
     */
    public LocationServiceResponse locate(String key, String email) {

        LocationServiceResponse response = new LocationServiceResponse();

        if (isValidAPIKey(key)) {
            PeekLocation peekLocation = locate(email);
            if (peekLocation != null) {
                response.setPeekLocation(peekLocation);
            } else {
                response.setCode(LocationServiceResponse.NOT_A_PEEKSTER_ERROR);
            }
        } else {
            response.setCode(LocationServiceResponse.INVALID_KEY);
        }

        return response;
    }

    /**
     * Returns the location information for the device using a registered email address.
     *
     * @param email The email address
     * @return The device location
     */
    public PeekLocation locate(String email) {
        if (logger.isTraceEnabled())
            logger.trace(String.format("locate(email=%s)", String.valueOf(email)));
        PeekLocation peekLocation = null;

        try {
            PeekLocationORMap peekLocationORMap = new PeekLocationORMap(userMasterForPeekLocationReads);
            peekLocation = peekLocationORMap.getPeekLocationByEmail(email);
        } catch (SQLException e) {
            logger.error("Unable to retrieve Peek Location for device using email " + email + ": " + e.getMessage());
        }

        return peekLocation;
    }

    /**
     * Processes an opt-in request.
     */

    // TODO - verify that peekEmail is a valid peekster; also verify if we have access to all of the user's accounts
    public LocationServiceResponse optIn(String key, String contentProviderId, String contentProviderName, String optInUrl, String optOutUrl, String peekEmail) {
        // first check to see if we're dealing with a valid geo service provider
        LocationServiceResponse response = new LocationServiceResponse();
        try {
            GeoLocationServiceProviderORMap geoLocationServiceProviderORMap = new GeoLocationServiceProviderORMap(userMasterForPeekLocationReads);
            GeoLocationServiceProvider geoLocationServiceProvider = geoLocationServiceProviderORMap.getGeoLocationServiceProviderByKey(key);
            if (geoLocationServiceProvider == null) {
                logger.error("Unable to retrieve Geo Location Service Provider using key: " + key);
                response.setCode(LocationServiceResponse.VALIDATION_ERROR);
                return response;
            }

            // now we create the content service provider if it doesn't already exist
            int geoLocationServiceProviderId = geoLocationServiceProvider.getId();
            ContentServiceProviderORMap contentServiceProviderORMap = new ContentServiceProviderORMap();
            ContentServiceProvider contentServiceProvider = contentServiceProviderORMap.getContentServiceProviderByCpid(geoLocationServiceProviderId, contentProviderId);
            if (contentServiceProvider == null) {
                contentServiceProvider = new ContentServiceProvider();
                contentServiceProvider.setCpid(contentProviderId);
                contentServiceProvider.setGeoLocationServiceProviderId(geoLocationServiceProviderId);
                contentServiceProvider.setName(contentProviderName);
                contentServiceProvider.setOptInUrl(optInUrl);
                contentServiceProvider.setOptOutUrl(optOutUrl);
                contentServiceProviderORMap.createContentServiceProvider(contentServiceProvider);

                logger.info("Created Content Service Provider " + contentServiceProvider + " for Geo Location Service Provider " + geoLocationServiceProvider);
            }

            // finally, we create the subscriber if there's not already an existing entry
            ContentServiceSubscriberORMap contentServiceSubscriberORMap = new ContentServiceSubscriberORMap();

            List<ContentServiceSubscriber> subscriptions = contentServiceSubscriberORMap.getContentServiceSubscriberByEmail(peekEmail);

            String uuid = this.getExistingUuid(contentServiceProvider.getId(), subscriptions);
            if (uuid == null) {
                uuid = UUID.randomUUID().toString();
                ContentServiceSubscriber contentServiceSubscriber = new ContentServiceSubscriber();
                contentServiceSubscriber.setContentServiceProviderId(contentServiceProvider.getId());
                contentServiceSubscriber.setCreateDate(new Date());
                contentServiceSubscriber.setEmail(peekEmail);
                contentServiceSubscriber.setUuid(uuid);

                contentServiceSubscriberORMap.createContentServiceSubscriber(contentServiceSubscriber);
            }

            response.setUuid(uuid);
        } catch (SQLException e) {
            logger.error("Database error encountered " + peekEmail + " for Content Service Provider " + contentProviderName + ", as requested by Geo Location Service Provider having key: " + key + ". " + e.getMessage());
            response.setCode(LocationServiceResponse.INTERNAL_ERROR);
            return response;
        }
        return response;
    }

    /**
     * Returns the UUID if there exists a subscription for the given content service provider.
     *
     * @param contentServiceProviderId The content service provider
     * @param subscriptions            The existing subscriptions for a known peek email address
     * @return
     */
    private String getExistingUuid(int contentServiceProviderId, List<ContentServiceSubscriber> subscriptions) {

        String uuid = null;

        if (subscriptions != null) {
            for (ContentServiceSubscriber subscriber : subscriptions) {
                if (subscriber.getContentServiceProviderId() == contentServiceProviderId) {
                    uuid = subscriber.getUuid();
                    break;
                }
            }
        }

        return uuid;
    }

    /**
     * Processes an Opt-Out request
     */
    public LocationServiceResponse optOut(String key, String uuid, String peekEmail) {
        LocationServiceResponse response = new LocationServiceResponse();

        // first check to see if we're dealing with a valid geo service provider
        try {
            GeoLocationServiceProviderORMap geoLocationServiceProviderORMap = new GeoLocationServiceProviderORMap();
            GeoLocationServiceProvider geoLocationServiceProvider = geoLocationServiceProviderORMap.getGeoLocationServiceProviderByKey(key);
            if (geoLocationServiceProvider == null) {
                logger.error("Unable to retrieve Geo Location Service Provider using key: " + key);
                response.setCode(LocationServiceResponse.VALIDATION_ERROR);
            }

            ContentServiceSubscriberORMap contentServiceSubscriberORMap = new ContentServiceSubscriberORMap();
            ContentServiceSubscriber contentServiceSubscriber = contentServiceSubscriberORMap.getContentServiceSubscriberByUuid(uuid);
            if (contentServiceSubscriber != null) {
                if (contentServiceSubscriber.getOptInDate() == null) {
                    // for completeness
                    contentServiceSubscriber.setOptInDate(new Date());
                }
                contentServiceSubscriber.setOptOutDate(new Date());

                contentServiceSubscriberORMap.updateContentServiceSubscriber(contentServiceSubscriber);
            }
        } catch (SQLException e) {
            logger.error("Database error encountered while Opting-Out " + uuid + " as requested by Geo Location Service Provider having key: " + key + ". " + e.getMessage());
            response.setCode(LocationServiceResponse.INTERNAL_ERROR);
        }
        return response;
    }

    /**
     * Returns a listing of all subscribers where the optIn date is not null and the optOut date is not null.
     *
     * @return A listing of users having active subscriptions to a content provider service.
     */
    public List<ContentServiceSubscriber> getAllActiveSubscribers() {
        try {
            ContentServiceSubscriberORMap contentServiceSubscriberORMap = new ContentServiceSubscriberORMap();
            return contentServiceSubscriberORMap.getAllActiveSubscribers();
        } catch (SQLException e) {
            logger.error("Database error encountered while retrieving the list of active content service subscribers: " + e.getMessage());
        }
        return new ArrayList<ContentServiceSubscriber>();
    }

    /**
     * Sets the opt-in date to indicate the peekster's confirmation of subscription to
     * the content provider's service.
     *
     * @param contentProviderId The UUID of the content provider.
     * @param peekEmail         The peek email address used to subscribe to the service.
     */
    public void confirmSubscription(String contentProviderId, String peekEmail) {

        try {
            List<ContentServiceSubscriber> subscribers = new ContentServiceSubscriberORMap(userMasterForPeekLocationReads).getContentServiceSubscriberByEmail(peekEmail);

            if (subscribers.size() == 0) {
                logger.error("Error encountered while processing Opt-In confirmation for user " + peekEmail + ", Content Service Provider " + contentProviderId + ": No Content Service subscriptions found for user.");
            } else {
                boolean found = false;
                for (ContentServiceSubscriber contentServiceSubscriber : subscribers) {
                    ContentServiceProvider contentServiceProvider = contentServiceSubscriber.getContentServiceProvider();
                    ContentServiceSubscriberORMap contentServiceSubscriberORMap = new ContentServiceSubscriberORMap();
                    if (contentServiceProvider != null && contentProviderId.equals(contentServiceProvider.getCpid())) {
                        contentServiceSubscriber.setOptInDate(new Date(System.currentTimeMillis()));
                        contentServiceSubscriberORMap.updateContentServiceSubscriber(contentServiceSubscriber);
                        found = true;
                    }
                }

                if (!found) {
                    logger.error("Error encountered while processing Opt-In confirmation for user " + peekEmail + ", Content Service Provider " + contentProviderId + ": A subscription was not found for this Content Service Provider.");
                }
            }
        } catch (SQLException e) {
            logger.error("Database error encountered while processing Opt-In confirmation for user " + peekEmail + ", Content Service Provider " + contentProviderId + ": " + e.getMessage());
        }
    }

    /**
     * Sends an email to a Peekster.
     *
     * @param key              The caller's PeekAPI key.
     * @param type             The message type (SMS || EMAIL | TWITTER_DM | TWITTER_ALERT)
     * @param mailDate         The mail sent date.
     * @param fromEmailAddress The "from" email address.
     * @param fromAlias        The from alias name.
     * @param peekEmailAddress The Peekster's email address.
     * @param subject          The message subject line.
     * @param body             The message body.
     * @param push             True if the Peekster should be notified immediately.
     */
    public LocationServiceResponse poke(String key, String type, Date mailDate, String fromEmailAddress, String fromAlias, String peekEmailAddress, String subject, String body, boolean push) {

        LocationServiceResponse response = new LocationServiceResponse();

        if (isValidAPIKey(key)) {
            try {
                Integer userId = new AccountORMap(userMasterForPeekLocationReads).getUserId(peekEmailAddress);
                if (userId != null) {


                    String messageBody = containsHeaders(body) ? getBody(body) : body;
                    String s_subject = subject;
                    String s_fromAlias = fromAlias;
                    if (isTransliteratePoke) {

                        messageBody = MailUtils.clean(messageBody,
                                isTransliteratePoke, isTransliteratePokePeekvetica);
                        s_subject = MailUtils.clean(subject,
                                isTransliteratePoke, isTransliteratePokePeekvetica);
                        s_fromAlias = MailUtils.clean(fromAlias,
                                isTransliteratePoke, isTransliteratePokePeekvetica);

                        logger.debug("--Input Message Body:" + body + "    --PeekFriendly message body:" + messageBody);
                        logger.debug("--MIME body in ISO-8859-1 Hex: " + StringUtils.dumpBytes(body.getBytes("ISO-8859-1")));
                        logger.debug("--MIME body in UTF-8 Hex: " + StringUtils.dumpBytes(body.getBytes("UTF-8")));
                        logger.debug("--MIME body in ISO-8859-1 Hex after extraction: " + StringUtils.dumpBytes(body.getBytes("ISO-8859-1")));
                        logger.debug("--MIME body in UTF-8 Hex after extraction: " + StringUtils.dumpBytes(body.getBytes("UTF-8")));

                    }

                    logger.info("Inbound Poke Message " + peekEmailAddress + " --Input Message Body:" + body + "    --PeekFriendly message body:" + messageBody);

                    OriginalReceivedEmail originalEmail = new OriginalReceivedEmail();
                    originalEmail.setSubject(s_subject);
                    originalEmail.setEmailFrom(fromEmailAddress);

                    if (peekEmailAddress.endsWith("@twitterpeek")) {
                        originalEmail.setEmailTo(peekEmailAddress.split("@")[0]);
                    } else {
                        originalEmail.setEmailTo(peekEmailAddress);
                    }
                    originalEmail.setBody(messageBody.getBytes("ISO-8859-1"));
                    originalEmail.setUserId(String.valueOf(userId.intValue()));
                    originalEmail.setMail_type(type);

                    originalEmail.setMailTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(mailDate));

                    int succFlag = new EmailRecievedService().saveEmail(
                            getEmailPojo(originalEmail, s_fromAlias), originalEmail);
                    if (succFlag > 0) {
                        logger.info("Successfully poked " + peekEmailAddress);
                        if (push) {
                            sendObjectToPush(getTopicInfo(String.valueOf(userId.intValue())));
                        }
                    }
                } else {
                    response.setCode(LocationServiceResponse.NOT_A_PEEKSTER_ERROR);
                    logger.error("Unable to retrieve user ID for account " + peekEmailAddress);
                }
            } catch (Exception e) {
                response.setCode(LocationServiceResponse.INTERNAL_ERROR);
                logger.info(String.format("Error poking Peekster %s. Key=%s, from=%s: %s ", peekEmailAddress, key, fromEmailAddress, e.getMessage()));
            }
        } else {
            // we may choose to fail silently here rather than advertise the fact that the key is invalid
            response.setCode(LocationServiceResponse.INVALID_KEY);
            logger.error("Invalid Partner Key specified " + key);
        }

        return response;
    }

    public int createExchangeLogin(String accountName, String loginName) {

        ExchangeLogin exchangeLogin = new ExchangeLogin();
        exchangeLogin.setAccountName(accountName);
        exchangeLogin.setLoginName(loginName);

        ExchangeLoginORMap exchangeLoginORMap = new ExchangeLoginORMap();
        try {
            return exchangeLoginORMap.addOrUpdateExchangeLogin(exchangeLogin);
        } catch (Exception e) {
            logger.error(e);
            return 0;
        }
    }

    private EmailPojo getEmailPojo(OriginalReceivedEmail original, String fromAlias) {
        Email email = new Email();

        if (original.getMail_type().startsWith("TWITTER_")) {
            original.setMail_type("TWITTER");

            email.setTwitterMeta(new TwitterMeta(0, "poke"));
            email.setMailid((int) System.currentTimeMillis()); // TODO - this sux!! the id must be unique for twitter peek
            email.setEmail_type("TWITTER");
            email.setMessage_type("TWITTER_ALERT");
            email.setOriginal_account(original.getEmailTo() + "@twitterpeek");
        } else {
            email.setOriginal_account(original.getEmailTo());
            email.setMessage_type(original.getMail_type());
        }

        email.setMaildate(original.getMailTime());
        email.setSubject(original.getSubject());
        email.setFrom(original.getEmailFrom());
        email.setTo(original.getEmailTo());
        email.setFrom_alias(fromAlias);
        email.setStatus("0");
        email.setUserId(original.getUserId());

        EmailPojo emailPojo = new EmailPojo();
        emailPojo.setEmail(email);
        Body body = new Body();
        body.setData(original.getBody());
        email.setBodySize(body.getData().length);
        emailPojo.setBody(body);

        return emailPojo;
    }

    /**
     * Validates the partner key. Returns False if the key is invalid or the partner status is inactive, True otherwise.
     *
     * @param key The partner key
     * @return True if the key is valid, false otherwise.
     */
    private boolean isValidAPIKey(String key) {
        if (key != null) {
            // TODO Implement me!! Introduce "PARTNER" table, keeping in mind GeoLoc Service Provider table already exists.
        }
        return true;
    }


    private void sendObjectToPush(Serializable object) {
        logger.info(String.format("Sending object to push, object=%s", object));

        String isPushMailOn = SysConfigManager.instance().getValue("isPushMailOn");
        String pushMailTopicJNDI = SysConfigManager.instance().getValue("pushmail.topic.jndi");
        String pushMailTopicURL = SysConfigManager.instance().getValue("pushmail.topic.url");

        if (StringUtils.isNotEmpty(isPushMailOn) && "true".equals(isPushMailOn)) {
            if (StringUtils.isNotEmpty(pushMailTopicJNDI) && StringUtils.isNotEmpty(pushMailTopicURL)) {
                try {
                    logger.info(String.format("Sending topic message %s to %s on %s", object, pushMailTopicJNDI, pushMailTopicURL));

                    SendTopicMessageClient.getInstance(pushMailTopicJNDI, pushMailTopicURL, pushMailTopicJNDI).send(object);
                } catch (Exception e) {
                    logger.info(String.format("Error sending topic message %s to %s on %s: %s", object, pushMailTopicJNDI, pushMailTopicURL, e.getMessage()));
                }
            } else {
                logger.warn(String.format("pushmail is missing connection parameters. pushmail.topic.jndi=%s, pushmail.topic.url=%s", pushMailTopicJNDI, pushMailTopicURL));
            }
        } else {
            logger.warn("pushmail is not enabled. please check the isPushMailOn configuration property. it is currently set to " + isPushMailOn);
        }
    }

    private TopicInfo getTopicInfo(String userId) {
        TopicInfo tif = new TopicInfo();
        tif.setUuid(userId);
        tif.setFlag(Common.NOTIFY);
        tif.setDate(new Date());
        byte[] udpPacket = new byte[2];
        udpPacket[0] = 'Y';
        udpPacket[1] = 0x00;
        tif.setUdpPacket(udpPacket);
        return tif;
    }

    private boolean containsHeaders(String message) {
        return message.contains("ContentType");
    }

    private String getBody(String message) {
        String body = null;

        try {
            logger.info("Retrieving body from message: [" + message + "]");

            Properties props = new Properties();

            SMTPMessage smtpMessage = new SMTPMessage(Session.getDefaultInstance(props), new ByteArrayInputStream(message.getBytes()));

            Object o = smtpMessage.getContent();

            if (o instanceof MimeMultipart) {
                logger.info("The message is of type MimeMultipart...");

                MimeMultipart content = (MimeMultipart) smtpMessage.getContent();

                int count = content.getCount();
                if (count > 0) {
                    BodyPart bodyPart = content.getBodyPart(0);
                    if (bodyPart != null) {
                        body = bodyPart.getContent().toString();
                    }
                }
            } else {
                if (o != null && o.toString().length() > 0) {
                    // content type text/plain
                    body = o.toString();
                    logger.info("new converted body is: [" + body + "]");
                } else {
                    // content type unknown
                    logger.warn("The message is not of type MimeMultipart and the content type is not known, the body could not be processed.");
                    body = message;
                }
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return body;
    }

    /* can be used to check the last time the device connected (if location is enabled) */
    public Date getLastUpdatedTimestamp(String user_id) throws DALException {
        PeekLocationORMap peekLocationORMap = new PeekLocationORMap();
        try {
            return peekLocationORMap.getLastUpdatedTimestamp(user_id);
        } catch (SQLException e) {
            DALException dalException = new DALException(ErrorCode.CODE_DAL_, e);
            logger.error("Last updated timestamp fetch failed for user " + user_id);
            throw dalException;
        }
    }

    public static void main(String[] args) {
        String msg1 = "Received: from mf1095e42.tmodns.net ([66.94.9.241] helo=mailx09.tmomail.net)\n" +
                "        by host.getpeek.net with esmtp (Exim 4.69)\n" +
                "        (envelope-from <16462034262@tmomail.net>)\n" +
                "        id 1Lw2CT-0006mj-Gp\n" +
                "        for dan.morel#gmail.com@user.getpeek.net; Mon, 20 Apr 2009 18:42:57 -0400\n" +
                "Received: from mgwsnq06.tmomail.net (162.74.176.10.in-addr.arpa [10.176.74.162])\n" +
                "        by mailx09.tmomail.net (8.14.1/8.12.11) with ESMTP id n3KJgkLI032081\n" +
                "        for <dan.morel#gmail.com@user.getpeek.net>; Mon, 20 Apr 2009 15:42:50 -0700\n" +
                "Received: by mgwsnq06.tmomail.net (Multimedia IP message store 6.1.999.11) id 49DE6D8200227CAA for dan.morel#gmail.com@user.getpeek.net; Mon, 20 Apr 2009 15:42:50 -0700\n" +
                "Date: Mon, 20 Apr 2009 15:42:50 -0700\n" +
                "From: 16462034262@tmomail.net\n" +
                "To: dan.morel#gmail.com@user.getpeek.net\n" +
                "Message-ID: <28067110.71708271240267370300.JavaMail.imb@mgwsnq06.tmomail.net>\n" +
                "Subject: RE: Tes\n" +
                "MIME-Version: 1.0\n" +
                "Content-Type: multipart/mixed;   boundary=\"----=_Part_1283478_33414784.1240267370291\"\n" +
                "\n" +
                "------=_Part_1283478_33414784.1240267370291\n" +
                "Content-Type: text/plain;charset=utf-8\n" +
                "Content-Transfer-Encoding: BASE64\n" +
                "\n" +
                "SGVsCi0tLS0tLS0tLS0tLS0tLS0tLQpkYW4ubW9yZWxAZ21haWwuY29tIC8gVGVzIC8gVGVzdGluZwoK\n" +
                "------=_Part_1283478_33414784.1240267370291--";

        String msg2 = "Received: from mf1095e42.tmodns.net ([66.94.9.241] helo=mailx09.tmomail.net)\n" +
                "        by host.getpeek.net with esmtp (Exim 4.69)\n" +
                "        (envelope-from <16462034262@tmomail.net>)\n" +
                "        id 1Lw2CT-0006mj-Gp\n" +
                "        for dan.morel#gmail.com@user.getpeek.net; Mon, 20 Apr 2009 18:42:57 -0400\n" +
                "Received: from mgwsnq06.tmomail.net (162.74.176.10.in-addr.arpa [10.176.74.162])\n" +
                "        by mailx09.tmomail.net (8.14.1/8.12.11) with ESMTP id n3KJgkLI032081\n" +
                "        for <dan.morel#gmail.com@user.getpeek.net>; Mon, 20 Apr 2009 15:42:50 -0700\n" +
                "Received: by mgwsnq06.tmomail.net (Multimedia IP message store 6.1.999.11) id 49DE6D8200227CAA for dan.morel#gmail.com@user.getpeek.net; Mon, 20 Apr 2009 15:42:50 -0700\n" +
                "Date: Mon, 20 Apr 2009 15:42:50 -0700\n" +
                "From: 16462034262@tmomail.net\n" +
                "To: dan.morel#gmail.com@user.getpeek.net\n" +
                "Message-ID: <28067110.71708271240267370300.JavaMail.imb@mgwsnq06.tmomail.net>\n" +
                "Subject: RE: Tes\n" +
                "MIME-Version: 1.0\n" +
                "Content-Type: multipart/mixed;   boundary=\"----=_Part_1283478_33414784.1240267370291\"\n" +
                "\n" +
                "------=_Part_1283478_33414784.1240267370291\n" +
                "Content-Type: text/plain;charset=utf-8\n" +
                "\n" +
                "Hello World!\n" +
                "------=_Part_1283478_33414784.1240267370291--";

        // Peek Social Message
        String msg3 = "From: from@getpeek.com\n" +
                "Reply-To: admin@getpeek.com\n" +
                "To: dan.morel@gmail.com\n" +
                "Message-ID: <3560063.0.1253112195089.JavaMail.marc@sirius>\n" +
                "Subject: subject\n" +
                "MIME-Version: 1.0\n" +
                "Content-Type: text/plain; charset=us-ascii\n" +
                "Content-Transfer-Encoding: 7bit\n" +
                "\n" +
                "The time has come for all good men to come to the aid of their country. " +
                "http%3A%2F%2Fwww.nytimes.com%2F2010%2F08%2F31%2Fbusiness%2Fglobal%2F31yen.html%3Fref%3Dglobal-home" +
                "  http://www.nytimes.com/2010/08/31/business/global/31yen.html?ref=global-home";

        String msg4 = "The quick brown fox jumped over the lazy dog. http://";

        PeekLocationService s = new PeekLocationService();

        System.out.println("msg1: " + s.getBody(msg1));
        System.out.println("msg2: " + s.getBody(msg2));
        System.out.println("msg3: " + s.getBody(msg3));
        System.out.println("msg4: " + s.getBody(msg4));
    }

}