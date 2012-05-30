package com.archermind.txtbl.receiver.mail.abst.impl;

import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.exception.SystemException;
import com.archermind.txtbl.mail.store.MessageStore;
import com.archermind.txtbl.mail.store.MessageStoreFactory;
import com.archermind.txtbl.receiver.mail.abst.Provider;
import com.archermind.txtbl.receiver.mail.dal.DALDominator;
import com.archermind.txtbl.receiver.mail.support.TwitterSupport;
import com.archermind.txtbl.receiver.mail.utils.ProviderStatistics;
import com.archermind.txtbl.utils.ReceiverUtilsTools;
import com.archermind.txtbl.utils.StopWatch;
import com.archermind.txtbl.utils.StopWatchUtils;
import com.archermind.txtbl.utils.SysConfigManager;
import org.jboss.logging.Logger;
import twitter4j.*;
import twitter4j.http.AccessToken;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// TODO: need refactoring badly

public class TwitterProvider implements Provider
{

    private static final Logger log = Logger.getLogger(TwitterProvider.class);

    private static ProviderStatistics statistics = new ProviderStatistics();

    private final static String twitterApplicationToken= SysConfigManager.instance().getValue("twitterAppToken", "zDSKZM7neuTokwTNwXFE6g");
    private final static String twitterApplicationTokenSecret= SysConfigManager.instance().getValue("twitterAppTokenSecret", "uMY5omezylXni395Qa2IGTHKkje72QDjBmYGOb48");


    private MessageStore messageStore = MessageStoreFactory.getStore();

    protected TwitterSupport support;

    public TwitterProvider(TwitterSupport support)
    {
        this.support = support;
    }

    /**
     * Processes a mailcheck for a given account
     *
     * @param account
     * @return Number of new messages
     */
    public int receiveMail(final Account account)
    {
        String context = String.format("account=%s, uid=%s, email=%s", account.getId(), account.getUser_id(), account.getName());

        StopWatch watch = new StopWatch("twitter status check " + context);

        int newMessages = 0;
        int folderDepth = 0;

        try
        {
            StopWatchUtils.newTask(watch, "loadAccountLatestReceiveInfo", context, log);

            if (!DALDominator.loadAccountLatestReceiveInfo(account))
            {
                log.warn(String.format("account has been removed but twitter status checks continue for %s", context));
                // this means that we can't find account anymore - probably to do with deletion
                return 0;
            }

            if (support.exceededMaximumLoginFailures(account))
            {
                log.warn(String.format("exceeded maximum login failures with %d attempts for %s", account.getLogin_failures(), context));
            }
            else
            {
                // process status and DM messages
                newMessages = processMessages(account, watch, context);
                folderDepth = newMessages;
            }
        }
        catch (Throwable e)
        {
            throw new SystemException(String.format("unexpected failure during receive mail for %s", context), e);
        }
        finally
        {
            watch.stop();

            log.info(ReceiverUtilsTools.printWatch(watch, folderDepth, newMessages, true/*folder hash match*/));

            String summary = statistics.enterStats(TwitterProvider.class.getName(), folderDepth, newMessages, watch.getTotalTimeMillis());

            log.info(String.format("completed twitter check for %s, depth=%d, newMessages=%s, time=%sms [%s]", context, folderDepth, newMessages, watch.getTotalTimeMillis(), summary));

            support.cleanup();
        }

        return newMessages;
    }

    private int processMessages(Account account, StopWatch watch, String context) throws Exception
    {
        Date lastMessageReceivedDate = null;

        int totalMessages = 0;
        long lastReceivedMentionId = 0;
        long lastReceivedTweetId = 0;
        long lastReceivedDmId = 0;
        long lastSentDmId = 0;

        // initialize twitter
        AccessToken token = DALDominator.fetchTwitterToken(account.getName());
        Twitter twitter = new TwitterFactory().getOAuthAuthorizedInstance(twitterApplicationToken,twitterApplicationTokenSecret,token);
        

        // get message id store bucket
        String messageStoreBucket = messageStore.getBucket(account.getId()); // calculate once

        log.debug(String.format("store bucket is %s for %s", messageStoreBucket, context));

        // apply special rules if this is the first time we're processing this account
        if (support.isFirstTime(account))
        {
            return handleFirstTime(twitter, account, messageStoreBucket, watch, context);
        }

        // process tweets and metions
        StopWatchUtils.newTask(watch, "processTweetsAndMentions", context, log);

        try
        {
            TwitterResult twitterResult = processTweetsAndMentions(twitter, account, messageStoreBucket, watch, context);

            totalMessages += twitterResult.numMessages;
            lastReceivedTweetId = twitterResult.lastReceivedTweetId;
            lastReceivedMentionId = twitterResult.lastReceivedMentionId;
            lastMessageReceivedDate = twitterResult.lastMessageReceivedDate;

            String summary = statistics.enterStats(TwitterProvider.class.getName(), totalMessages, totalMessages, watch.getTotalTimeMillis());

            log.info(String.format("completed twitter status/mention check for %s, newMessages=%s, time=%sms [%s]", context, twitterResult.numMessages, watch.getTotalTimeMillis(), summary));
        }
        catch (TwitterException ex)
        {
            log.warn(String.format("processing of twitter status messages for %s failed due to twitter reported failure: %s", context, ex.getMessage()));

            if (ex.getStatusCode() == 401)
            {
                support.updateWithLoginFailure(context, account);

                log.warn(String.format("aborting receiving at processing of status messages for %s due to authentication problem", context));

                throw ex;
            }
        }
        catch (Throwable ex)
        {
            // trap exception so we can attempt to process Direct Messages
            log.warn(String.format("processing of twitter status messages for %s failed due to a general failure: %s", context, ex.getMessage()));
        }

        // process received direct messages
        StopWatchUtils.newTask(watch, "processReceivedDirectMessages", context, log);

        try
        {
            TwitterResult twitterResult = processReceivedDirectMessages(twitter, account, messageStoreBucket, watch, context);

            totalMessages += twitterResult.numMessages;
            lastReceivedDmId = twitterResult.lastDmId;
            if (twitterResult.lastMessageReceivedDate != null)
            {
                lastMessageReceivedDate = twitterResult.lastMessageReceivedDate;
            }

            String summary = statistics.enterStats(TwitterProvider.class.getName(), totalMessages, totalMessages, watch.getTotalTimeMillis());

            log.info(String.format( "completed twitter received DM check for %s, newMessages=%s, time=%sms [%s]", context, twitterResult.numMessages, watch.getTotalTimeMillis(), summary));
        }
        catch (TwitterException ex)
        {
            log.warn(String.format("processing of DM messages for %s failed due to twitter reported failure: %s", context, ex.getMessage()));

            if (ex.getStatusCode() == 401)
            {
                support.updateWithLoginFailure(context, account);

                log.warn(String.format("aborting receiving at processing of DMs for %s due to authentication problem", context));

                throw ex;
            }
        }
        catch (Exception ex)
        {
            log.warn(String.format("processing of twitter DM messages for %s failed: %s", context, ex.getMessage()));
            throw ex;
        }

        // process sent direct messages
        StopWatchUtils.newTask(watch, "processSentDirectMessages", context, log);

        try
        {
            TwitterResult twitterResult = processSentDirectMessages(twitter, account, messageStoreBucket, watch, context);

            totalMessages += twitterResult.numMessages;
            lastSentDmId = twitterResult.lastDmId;
            if (twitterResult.lastMessageReceivedDate != null)
            {
                lastMessageReceivedDate = twitterResult.lastMessageReceivedDate;
            }

            String summary = statistics.enterStats(TwitterProvider.class.getName(), totalMessages, totalMessages, watch.getTotalTimeMillis());

            log.info(String.format( "completed twitter sent DM check for %s, newMessages=%s, time=%sms [%s]", context, twitterResult.numMessages, watch.getTotalTimeMillis(), summary));
        }
        catch (TwitterException ex)
        {
            log.warn(String.format("processing of sent DMs for %s failed due to twitter reported failure: %s", context, ex.getMessage()));

            if (ex.getStatusCode() == 401)
            {
                support.updateWithLoginFailure(context, account);

                log.warn(String.format("aborting receiving at processing of sent DMs for %s due to authentication problem", context));

                throw ex;
            }
        }
        catch (Exception ex)
        {
            log.warn(String.format("processing of twitter sent DM messages for %s failed: %s", context, ex.getMessage()));
            throw ex;
        }

        // update account
        StopWatchUtils.newTask(watch, "updateAccount", context, log);

        support.updateAccount(account, totalMessages, totalMessages, lastMessageReceivedDate, lastReceivedTweetId, lastReceivedMentionId, lastReceivedDmId, lastSentDmId);

        return totalMessages;
    }

    private int handleFirstTime(Twitter twitter, Account account, String messageStoreBucket, StopWatch watch, String context) throws Exception
    {
        log.info(String.format( "performing first-time mail check for %s", context));

        int numMessages = 0;

        // retrieve status updates from twitter
        List<Status> statuses = support.getFriendsTimeline(twitter, account, watch, context);

        Date lastMessageReceivedDate = null;
        long lastReceivedTweetId = 0;
        if (statuses.size() > 0) {
            lastMessageReceivedDate = statuses.get(0).getCreatedAt();
            lastReceivedTweetId = statuses.get(0).getId();
        }

        // process the status messages
        int count = 1;
        for (Status status : statuses)
        {
            if (status != null)
            {
                String messageId = String.valueOf(status.getId());

                if (support.processMessage(account, status, count++, messageId, twitter.getScreenName(), messageStoreBucket, context, watch))
                {
                    numMessages++;
                }
            }
        }

        // make sure we update the lastReceivedMentionId so that we don't pick up stale mentions on subsequent checks
        long lastReceivedMentionId = 0;
        long lastReceivedDmId = 0;
        long lastSentDmId = 0;

        try
        {

            List<Status> mentions = support.getMentions(twitter, account, watch, context);
            if (mentions.size() > 0) {
                lastReceivedMentionId = mentions.get(0).getId();
                log.info(String.format( "first-time mail check for %s, initialized lastReceivedMentionId to %s", context, lastReceivedMentionId));
            }

            // make sure we update the lastReceivedDmId so that we don't pick up stale dms on subsequent checks
            List<DirectMessage> receivedDms = support.getReceivedDirectMessages(twitter, account, watch, context);
            if (receivedDms.size() > 0) {
                lastReceivedDmId = receivedDms.get(0).getId();
                log.info(String.format( "first-time mail check for %s, initialized lastReceivedDmId to %s", context, lastReceivedDmId));
            }

            // make sure we update the lastSentDmId so that we don't pick up stale dms on subsequent checks
            List<DirectMessage> sentDms = support.getSentDirectMessages(twitter, account, watch, context);
            if (sentDms.size() > 0) {
                lastSentDmId = sentDms.get(0).getId();
                log.info(String.format( "first-time mail check for %s, initilized lastSentDmId to %s", context, lastSentDmId));
            }

            support.updateAccount(account, numMessages, numMessages, lastMessageReceivedDate, lastReceivedTweetId, lastReceivedMentionId, lastReceivedDmId, lastSentDmId);

        }
        catch (TwitterException ex)
        {
            if (ex.getStatusCode() == 401)
            {
                support.updateWithLoginFailure(context, account);

                log.warn(String.format("aborting first time receiving for %s due to authentication problem", context));

                support.updateAccount(account, numMessages, numMessages, lastMessageReceivedDate, lastReceivedTweetId, lastReceivedMentionId, lastReceivedDmId, lastSentDmId);
            }

            throw ex;
        }

        return numMessages;
    }

    private TwitterResult processTweetsAndMentions(Twitter twitter, Account account, String messageStoreBucket, StopWatch watch, String context) throws Exception
    {
        int numMessages = 0;
        Date lastMessageReceivedDate = null;

        // retrieve status updates from twitter
        List<Status> statuses = support.getFriendsTimeline(twitter, account, watch, context);

        // retrieve mentions from twitter
        List<Status> mentions = support.getMentions(twitter, account, watch, context);

        // create the exclusion list- the list which will contain the status messages which have a corresponding mention.
        // Those status messages will be excluded since we prefer mentions
        List<Status> exclusionList = new ArrayList<Status>();

        for (Status mention : mentions) {
            for (Status status : statuses) {
                long mentionId = mention.getId();
                long statusId = status.getId();
                if (mentionId == statusId) { // not using mention.getId() == status.getId() to avoid a debugger scope issue
                    exclusionList.add(status);
                    break;
                }
            }
        }

        // save the last received status and mention IDs
        long lastReceivedMentionId = 0;
        if (mentions.size() > 0) {
            lastReceivedMentionId = mentions.get(0).getId();
        }

        long lastReceivedTweetId = 0;
        if (statuses.size() > 0) {
            lastMessageReceivedDate = statuses.get(0).getCreatedAt();
            lastReceivedTweetId = statuses.get(0).getId();
        }

        // process the status messages, excluding the ones in the exclusion list
        int count = 1;
        for (Status status : statuses)
        {
            if (status != null && !exclusionList.contains(status))
            {
                String messageId = String.valueOf(status.getId());

                if (support.processMessage(account, status, count++, messageId, twitter.getScreenName(), messageStoreBucket, context, watch))
                {
                    numMessages++;
                }
            }
        }

        // process the mentions
        count = 1;
        for (Status mention : mentions)
        {
            if (mention != null)
            {
                String messageId = String.valueOf(mention.getId());

                if (support.processMessage(account, mention, count++, messageId, twitter.getScreenName(), messageStoreBucket, context, watch))
                {
                    numMessages++;
                }
            }
        }

        return new TwitterResult(numMessages, lastReceivedTweetId, lastReceivedMentionId, lastMessageReceivedDate);
    }

    private TwitterResult processReceivedDirectMessages(Twitter twitter, Account account, String messageStoreBucket, StopWatch watch, String context) throws Exception
    {
        int numMessages = 0;
        Date lastMessageReceivedDate = null;

        // retrieve received DMs
        List<DirectMessage> messages = support.getReceivedDirectMessages(twitter, account, watch, context);

        int lastReceivedDmId = 0;

        if (messages.size() > 0) {
            lastMessageReceivedDate = messages.get(0).getCreatedAt();
            lastReceivedDmId = messages.get(0).getId();
        }

        int count = 1;
        for (DirectMessage message : messages)
        {
            if (message != null)
            {
                String messageId = String.valueOf(message.getId());

                if (support.processMessage(account, message, count++, messageId, messageStoreBucket, context, watch))
                {
                    numMessages++;
                }
            }
        }

        return new TwitterResult(numMessages, lastReceivedDmId, lastMessageReceivedDate);
    }

    private TwitterResult processSentDirectMessages(Twitter twitter, Account account, String messageStoreBucket, StopWatch watch, String context) throws Exception
    {
        int numMessages = 0;
        Date lastMessageReceivedDate = null;

        // retrieve sent DMs
        List<DirectMessage> messages = support.getSentDirectMessages(twitter, account, watch, context);

        int lastSentDmId = 0;

        if (messages.size() > 0) {
            lastMessageReceivedDate = messages.get(0).getCreatedAt();
            lastSentDmId = messages.get(0).getId();
        }

        int count = 1;
        for (DirectMessage message : messages)
        {
            if (message != null)
            {
                String messageId = String.valueOf(message.getId());

                if (support.processMessage(account, message, count++, messageId, messageStoreBucket, context, watch))
                {
                    numMessages++;
                }
            }
        }

        return new TwitterResult(numMessages, lastSentDmId, lastMessageReceivedDate);
    }

    private class TwitterResult {
        public int numMessages;
        public long lastReceivedTweetId;
        public long lastReceivedMentionId;
        public long lastDmId;
        public Date lastMessageReceivedDate;

        public TwitterResult(int numMessages, long lastReceivedTweetId, long lastReceivedMentionId, Date lastMessageReceivedDate) {
            this.numMessages = numMessages;
            this.lastReceivedTweetId = lastReceivedTweetId;
            this.lastReceivedMentionId = lastReceivedMentionId;
            this.lastMessageReceivedDate = lastMessageReceivedDate;
        }

        public TwitterResult(int numMessages, long lastDmId, Date lastMessageReceivedDate) {
            this.numMessages = numMessages;
            this.lastDmId = lastDmId;
            this.lastMessageReceivedDate = lastMessageReceivedDate;
        }
    }

}
