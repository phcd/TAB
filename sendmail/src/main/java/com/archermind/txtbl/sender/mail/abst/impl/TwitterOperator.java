package com.archermind.txtbl.sender.mail.abst.impl;

import com.archermind.txtbl.dal.business.impl.PeekLocationService;
import com.archermind.txtbl.dal.orm.ReceivedORMap;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.Email;
import com.archermind.txtbl.domain.EmailPojo;
import com.archermind.txtbl.sender.mail.abst.Operator;
import com.archermind.txtbl.sender.mail.dal.DALDominator;
import com.archermind.txtbl.utils.SysConfigManager;
import com.archermind.txtbl.validate.mailbox.abst.impl.TwitterValidate;
import org.jboss.logging.Logger;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.http.AccessToken;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class TwitterOperator extends Operator {

    private static final Logger log = Logger.getLogger(TwitterOperator.class);

    private static final String STATUS = "TWITTER_STATUS";
    private static final String DM = "TWITTER_DM";
    private static final String REPLY = "TWITTER_REPLY";

    private static final String ALERT = "TWITTER_ALERT";

    private static String pokeApiKey;
    private static String twitterFailedDMFrom;
    private static String twitterFailedDMFromAlias;
    private static String twitterFailedDMText;
    private final static String twitterApplicationToken = SysConfigManager.instance().getValue("twitterAppToken", "zDSKZM7neuTokwTNwXFE6g");
    private final static String twitterApplicationTokenSecret = SysConfigManager.instance().getValue("twitterAppTokenSecret", "uMY5omezylXni395Qa2IGTHKkje72QDjBmYGOb48");


    static {
        pokeApiKey = SysConfigManager.instance().getValue("pokeApiKey", "");
        twitterFailedDMFrom = SysConfigManager.instance().getValue("twitterFailedDMFrom", "peek_inc@twitterpeek");
        twitterFailedDMFromAlias = SysConfigManager.instance().getValue("twitterFailedDMFromAlias", "Peek");
        twitterFailedDMText = SysConfigManager.instance().getValue("twitterFailedDMText", "We were unable to deliver your Direct Message. Please ensure that the intended recipient is in your followers list.");
    }

    /**
     * @param list
     */
    public String sendMail(List<EmailPojo> list) {

        Account account = null;
        String failureId = null;

        try {
            account = list.get(0).getAccount();

            log.info("[attempting to send tweet] " + account.getName());            
            AccessToken token = DALDominator.fetchTwitterToken(account);
            Twitter twitter = new TwitterFactory().getOAuthAuthorizedInstance(twitterApplicationToken, twitterApplicationTokenSecret, token);


            for (EmailPojo emailPojo : list) {
                try {

                    doSend(twitter, emailPojo);

                    log.info("[" + account.getName() + "] [sending success] [" + emailPojo.getEmail().getMailid() + "]");
                    updateMailFlag(emailPojo.getEmail().getMailid());
                } catch (Exception e) {
                    Email email = emailPojo.getEmail();
                    failureId = failureId == null ? String.valueOf(email.getMailid()) : failureId + "," + email.getMailid();
                    log.error("[" + account.getName() + "] [sending failed] [" + failureId + "] " + e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("[" + account.getName() + "] [sending failed - Twitter authentication error] " + e.getMessage());
            for (EmailPojo emailPojo : list) {
                Email email = emailPojo.getEmail();
                failureId = failureId == null ? String.valueOf(email.getMailid()) : failureId + "," + email.getMailid();
            }
        }

        return failureId;
    }

    private void doSend(Twitter twitter, EmailPojo emailPojo) throws TwitterException {
        String mailType = emailPojo.getEmail().getEmail_type().toUpperCase();

        if (STATUS.equals(mailType)) {
            updateStatus(twitter, emailPojo);
        } else if (DM.equals(mailType)) {
            sendDirectMessage(twitter, emailPojo);
        } else if (REPLY.equals(mailType)) {
            replyToMessage(twitter, emailPojo);
        } else {
            updateStatus(twitter, emailPojo);
            // throw new RuntimeException("Invalid Twitter mail type specified: \"" + mailType + "\"");
        }
    }

    private void updateStatus(Twitter twitter, EmailPojo emailPojo) throws TwitterException {
        twitter.updateStatus(emailPojo.getEmail().getSubject());
    }

    private void sendDirectMessage(Twitter twitter, EmailPojo emailPojo) throws TwitterException {
        try {
            Email email = emailPojo.getEmail();
            twitter.sendDirectMessage(TwitterValidate.getTwitterId(email.getTo()), email.getSubject());
        } catch (Throwable t) {
            // Alert the user that we could not send the DM
            Email email = emailPojo.getEmail();

            String message = twitterFailedDMText + " (" + email.getTo() + ")";
            new PeekLocationService().poke(pokeApiKey, ALERT, new Date(), twitterFailedDMFrom, twitterFailedDMFromAlias, email.getFrom(), message, message, true /*push*/);

            // propagate the original exception
            throw (new TwitterException(t.getMessage()));
        }
    }

    private void replyToMessage(Twitter twitter, EmailPojo emailPojo) throws TwitterException {
        Email email = emailPojo.getEmail();
        String subject = email.getSubject();
        String username = TwitterValidate.getTwitterId(email.getTo());

        if (!subject.startsWith("@" + username)) {
            subject = "@" + username + " " + subject;
        }

        twitter.updateStatus(subject, getReplyToStatusId(email));
    }

    private long getReplyToStatusId(Email email) {
        long replyToStatusId = -1;

        try {
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("uuid", email.getUserId());
            params.put("mailid", email.getOrigin_id());

            ReceivedORMap receivedORMap = new ReceivedORMap();
            Email originalEmail = receivedORMap.getReceivedEmail(params);

            if (originalEmail != null) {
                replyToStatusId = originalEmail.getTwitterMeta().getMessageId();
            }
        } catch (Exception e) {
            log.info("Error retrieving reply-to status id for original message ID " + email.getOrigin_id());
        }

        return replyToStatusId;
    }
}
