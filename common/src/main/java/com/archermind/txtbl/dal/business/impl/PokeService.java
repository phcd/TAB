package com.archermind.txtbl.dal.business.impl;

import com.archermind.txtbl.dal.business.IPokeService;
import com.archermind.txtbl.dal.orm.AccountORMap;
import com.archermind.txtbl.domain.*;

import com.archermind.txtbl.parser.MessageParser;
import com.archermind.txtbl.pushmail.utility.Common;
import com.archermind.txtbl.pushmail.utility.TopicInfo;
import com.archermind.txtbl.utils.MailUtils;
import com.archermind.txtbl.utils.SendTopicMessageClient;
import com.archermind.txtbl.utils.StringUtils;
import com.archermind.txtbl.utils.SysConfigManager;
import com.sun.mail.smtp.SMTPMessage;
import org.jboss.logging.Logger;

import javax.mail.MessagingException;
import javax.mail.Session;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.*;

public class PokeService implements IPokeService {
    private static final Logger log = Logger.getLogger(PokeService.class);

    private static Set<String> keys = new HashSet<String>();
    private static boolean userMasterForPeekLocationReads;
    private static boolean isTransliteratePoke;
    private static boolean isTransliteratePokePeekvetica;

    static {
        userMasterForPeekLocationReads =  Boolean.parseBoolean(SysConfigManager.instance().getValue("userMasterForPeekLocationReads",  "false"));
        isTransliteratePoke = Boolean.parseBoolean(SysConfigManager.instance().getValue("transliteratePoke", "true"));
        isTransliteratePokePeekvetica = Boolean.parseBoolean(SysConfigManager.instance().getValue("transliteratePokePeekvetica", "true"));
    }

    public PokeService(){
        keys.add("211");  //3Jam
        keys.add("04191956"); //PeekSocial

        /* Ones I have handed out via email*/
        keys.add("800");
        keys.add("801");
        keys.add("802"); //gomobo/olo
        keys.add("100");
        keys.add("807");  //seamlessweb
        keys.add("808");          //grubhub
        keys.add("806");
        keys.add("124");
        keys.add("84");
        keys.add("380");
    }

    /**
     * Sends an email to a Peekster.
     *
     * @param poke - data object with poke details
     * @param push True if the Peekster should be notified immediately.
     */
    public LocationServiceResponse poke(Poke poke, boolean push) {
        LocationServiceResponse response = new LocationServiceResponse();
        MessageParser parser = new MessageParser();
        SMTPMessage message = getMessage(poke.getBody());

        if (isValidAPIKey(poke.getKey())) {
            try {
                Integer userId = new AccountORMap(userMasterForPeekLocationReads).getUserId(poke.getTo());
                if (userId != null) {
                    String messageBody = parser.parseMsgContent(message, true);
                    messageBody = MailUtils.clean(messageBody, isTransliteratePoke, isTransliteratePokePeekvetica);

                    log.info("Inbound Poke Message " + poke.getTo() + " --Input Message Body:" + poke.getBody() + "    --PeekFriendly message body:" + messageBody);

                    OriginalReceivedEmail originalEmail = poke.toOriginalReceivedEmail(userId);
                    originalEmail.setBody(messageBody.getBytes("ISO-8859-1"));
                    List<OriginalReceivedAttachment> originalAttachments = new ArrayList<OriginalReceivedAttachment>();
                    originalAttachments.addAll(parser.parseMsgAttach(getMessage(messageBody),poke.getTo(),Long.toString(System.currentTimeMillis())));
                    try {
                        originalAttachments.addAll(parser.createLinkAttachments(messageBody));
                    } catch (Throwable t) {
                        log.warn("unable to process link attachments due to " + t.toString(), t);
                    }

                    originalEmail.setAttachList(originalAttachments);

                    int succFlag = new EmailRecievedService().saveEmail(getEmailPojo(originalEmail, poke.getAlias()), originalEmail);
                    if (succFlag > 0) {
                        log.info("Successfully poked " + poke.getTo());
                        if (push) {
                            sendObjectToPush(getTopicInfo(String.valueOf(userId.intValue())));
                        }
                    }
                } else {
                    response.setCode(LocationServiceResponse.NOT_A_PEEKSTER_ERROR);
                    log.error("Unable to retrieve user ID for account " + poke.getTo());
                }
            } catch (Exception e) {
                response.setCode(LocationServiceResponse.INTERNAL_ERROR);
                log.info(String.format("Error poking Peekster %s. Key=%s, from=%s: %s ", poke.getTo(), poke.getKey(), poke.getFrom(), e.getMessage()));
            }
        } else {
            // we may choose to fail silently here rather than advertise the fact that the key is invalid
            response.setCode(LocationServiceResponse.INVALID_KEY);
            log.error("Invalid Partner Key specified " + poke.getKey());
        }

        return response;
    }


    /**
     * Validates the partner key. Returns False if the key is invalid or the partner status is inactive, True otherwise.
     *
     * @param key The partner key
     * @return  True if the key is valid, false otherwise.
     */
    private boolean isValidAPIKey(String key) {
        return key != null && keys.contains(key);
    }


    private EmailPojo getEmailPojo(OriginalReceivedEmail original, String fromAlias) throws Exception {
        Email email = new Email();
        if (original.getMail_type().startsWith("TWITTER_")) {
            original.setMail_type("TWITTER");
            email.setTwitterMeta(new TwitterMeta(0, "poke"));
            email.setMailid((int)System.currentTimeMillis()); // TODO - this sux!! the id must be unique for twitter peek
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


    private void sendObjectToPush(Serializable object) {
        log.info(String.format("Sending object to push, object=%s", object));

        String isPushMailOn = SysConfigManager.instance().getValue("isPushMailOn");
        String pushMailTopicJNDI = SysConfigManager.instance().getValue("pushmail.topic.jndi");
        String pushMailTopicURL = SysConfigManager.instance().getValue("pushmail.topic.url");

        if (StringUtils.isNotEmpty(isPushMailOn) && "true".equals(isPushMailOn)) {
            if (StringUtils.isNotEmpty(pushMailTopicJNDI) && StringUtils.isNotEmpty(pushMailTopicURL)) {
                try {
                    log.info(String.format("Sending topic message %s to %s on %s", object, pushMailTopicJNDI, pushMailTopicURL));
                    SendTopicMessageClient.getInstance(pushMailTopicJNDI, pushMailTopicURL, pushMailTopicJNDI).send(object);
                } catch (Exception e) {
                    log.info(String.format("Error sending topic message %s to %s on %s: %s", object, pushMailTopicJNDI, pushMailTopicURL, e.getMessage()));
                }
            } else {
                log.warn(String.format("pushmail is missing connection parameters. pushmail.topic.jndi=%s, pushmail.topic.url=%s", pushMailTopicJNDI, pushMailTopicURL));
            }
        } else {
            log.warn("pushmail is not enabled. please check the isPushMailOn configuration property. it is currently set to " + isPushMailOn);
        }
    }


    private SMTPMessage getMessage(String message){
        SMTPMessage smtpMessage = null;
        try{
            Properties props = new Properties();
            smtpMessage = new SMTPMessage(Session.getDefaultInstance(props), new ByteArrayInputStream(message.getBytes()));
        } catch (MessagingException e) {
            log.error(e);
        }
        return smtpMessage;
    }


public static void main(String [] args) {
//       String msg1 =  "Received: from mf1095e42.tmodns.net ([66.94.9.241] helo=mailx09.tmomail.net)\n" +
//                "        by host.getpeek.net with esmtp (Exim 4.69)\n" +
//                "        (envelope-from <16462034262@tmomail.net>)\n" +
//                "        id 1Lw2CT-0006mj-Gp\n" +
//                "        for dan.morel#gmail.com@user.getpeek.net; Mon, 20 Apr 2009 18:42:57 -0400\n" +
//                "Received: from mgwsnq06.tmomail.net (162.74.176.10.in-addr.arpa [10.176.74.162])\n" +
//                "        by mailx09.tmomail.net (8.14.1/8.12.11) with ESMTP id n3KJgkLI032081\n" +
//                "        for <dan.morel#gmail.com@user.getpeek.net>; Mon, 20 Apr 2009 15:42:50 -0700\n" +
//                "Received: by mgwsnq06.tmomail.net (Multimedia IP message store 6.1.999.11) id 49DE6D8200227CAA for dan.morel#gmail.com@user.getpeek.net; Mon, 20 Apr 2009 15:42:50 -0700\n" +
//                "Date: Mon, 20 Apr 2009 15:42:50 -0700\n" +
//                "From: 16462034262@tmomail.net\n" +
//                "To: dan.morel#gmail.com@user.getpeek.net\n" +
//                "Message-ID: <28067110.71708271240267370300.JavaMail.imb@mgwsnq06.tmomail.net>\n" +
//                "Subject: RE: Tes\n" +
//                "MIME-Version: 1.0\n" +
//                "Content-Type: multipart/mixed;   boundary=\"----=_Part_1283478_33414784.1240267370291\"\n" +
//                "\n" +
//                "------=_Part_1283478_33414784.1240267370291\n" +
//                "Content-Type: text/plain;charset=utf-8\n" +
//                "Content-Transfer-Encoding: BASE64\n" +
//                "\n" +
//                "SGVsCi0tLS0tLS0tLS0tLS0tLS0tLQpkYW4ubW9yZWxAZ21haWwuY29tIC8gVGVzIC8gVGVzdGluZwoK\n" +
//                "------=_Part_1283478_33414784.1240267370291--";

//        String msg2 =  "Received: from mf1095e42.tmodns.net ([66.94.9.241] helo=mailx09.tmomail.net)\n" +
//                 "        by host.getpeek.net with esmtp (Exim 4.69)\n" +
//                 "        (envelope-from <16462034262@tmomail.net>)\n" +
//                 "        id 1Lw2CT-0006mj-Gp\n" +
//                 "        for dan.morel#gmail.com@user.getpeek.net; Mon, 20 Apr 2009 18:42:57 -0400\n" +
//                 "Received: from mgwsnq06.tmomail.net (162.74.176.10.in-addr.arpa [10.176.74.162])\n" +
//                 "        by mailx09.tmomail.net (8.14.1/8.12.11) with ESMTP id n3KJgkLI032081\n" +
//                 "        for <dan.morel#gmail.com@user.getpeek.net>; Mon, 20 Apr 2009 15:42:50 -0700\n" +
//                 "Received: by mgwsnq06.tmomail.net (Multimedia IP message store 6.1.999.11) id 49DE6D8200227CAA for dan.morel#gmail.com@user.getpeek.net; Mon, 20 Apr 2009 15:42:50 -0700\n" +
//                 "Date: Mon, 20 Apr 2009 15:42:50 -0700\n" +
//                 "From: 16462034262@tmomail.net\n" +
//                 "To: dan.morel#gmail.com@user.getpeek.net\n" +
//                 "Message-ID: <28067110.71708271240267370300.JavaMail.imb@mgwsnq06.tmomail.net>\n" +
//                 "Subject: RE: Tes\n" +
//                 "MIME-Version: 1.0\n" +
//                 "Content-Type: multipart/mixed;   boundary=\"----=_Part_1283478_33414784.1240267370291\"\n" +
//                 "\n" +
//                 "------=_Part_1283478_33414784.1240267370291\n" +
//                 "Content-Type: text/plain;charset=utf-8\n" +
//                 "\n" +
//                 "Hello World!\n" +
//                 "------=_Part_1283478_33414784.1240267370291--";

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

//        String msg4 = "The quick brown fox jumped over the lazy dog.";


    /*String key, String sentDateUTC, String to, String from, String alias, String subject, String body, String type*/
        Poke testPoke = new Poke("100","2010-08-31T17:14:11.000Z","dan.morel@gmail.com","dan@getpeek.com","Dan Morel","Testing HTTP",msg3,"APP");

        PokeService poke = new PokeService();
        LocationServiceResponse response = poke.poke(testPoke,true);
        System.out.println(response.getMessage());
    }
}
