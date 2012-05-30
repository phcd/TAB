package com.archermind.txtbl.receiver.mail.support;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.dal.business.impl.EmailRecievedService;
import com.archermind.txtbl.domain.*;
import com.archermind.txtbl.mail.store.MessageStore;
import com.archermind.txtbl.mail.store.MessageStoreFactory;
import com.archermind.txtbl.receiver.mail.dal.DALDominator;
import com.archermind.txtbl.utils.ReceiverUtilsTools;
import com.archermind.txtbl.utils.*;
import com.ibm.icu.text.Normalizer;
import com.ibm.icu.text.Transliterator;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.CharUtils;
import twitter4j.*;
import twitter4j.User;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TwitterSupport {
    private static final org.jboss.logging.Logger logger = org.jboss.logging.Logger.getLogger(TwitterSupport.class);

    private static final int MAX_RETRIES = Integer.valueOf(SysConfigManager.instance().getValue("tweeterSupportRetryCount", "1"));

    private static final int CONNECTION_TIMEOUT = Integer.valueOf(SysConfigManager.instance().getValue("tweeterSupportConnectionTimeout", "30000"));

    private static final int MAX_CONNECTIONS_PER_CLIENT = Integer.valueOf(SysConfigManager.instance().getValue("tweeterSupportMaxConnectionsPerClient", "10000"));

    private static final String UTF_8_ENCODING = "UTF-8";

    private static final String LOCKED_OUT_MESSAGE = "Sorry, it looks like your Twitter account password has changed. Please go to Settings->Account Info and update your password to resume TwitterPeek service.";

    private static final String TWEET_IMG_ATTACHMENT_EXTENSION = ".jpg.tweet";

    private static final String TWEET_TXT_ATTACHMENT_EXTENSION = ".txt.tweet";

    private static final int MAX_ATTACHMENT_NAME_LENGTH = Integer.valueOf(SysConfigManager.instance().getValue("maxAttachmentNameLength", "10"));

    private static final Pattern HREF_PATTERN = Pattern.compile("<[Aa] .*>(.*)<.*", Pattern.CASE_INSENSITIVE);

    private static final Pattern LINK_PATTERN = Pattern.compile("(http[s]*://[a-zA-Z_0-9$_.+!*'()\\-/\\?:@=&]+)", Pattern.CASE_INSENSITIVE);

    private final MessageStore messageIdStore = MessageStoreFactory.getStore();

    private int maximumMessagesToProcessFirstTime = 10;

    private int maximumTweetsToProcess = 50;
    private int maximumMentionsToProcess = 50;
    private int maximumDirectMessagesToProcess = 50;
    private int maximumLoginFailures = 3;

    private String lockedOutMessage = null;

    private String twitterPeekDomain = "twitterpeek";

    private final Transliterator transliterator = Transliterator.getInstance("Latin");

    private final boolean isTransliterate;

    private final char[] doubleQuotes;
    private final char[] apostrophes;
    private final char[] dashes;

    private HttpClient httpClient = null;



    public TwitterSupport() {
        this.lockedOutMessage =  SysConfigManager.instance().getValue("twitterLockedOutMessage", LOCKED_OUT_MESSAGE);
        this.maximumLoginFailures = Integer.valueOf(SysConfigManager.instance().getValue("maximumTwitterLoginFailures", "3"));
        this.maximumMessagesToProcessFirstTime = Integer.valueOf(SysConfigManager.instance().getValue("maximumTwitterMessagesFirstTime", "10"));
        this.maximumTweetsToProcess = Integer.valueOf(SysConfigManager.instance().getValue("maximumTweetsToProcess", "50"));
        this.maximumMentionsToProcess = Integer.valueOf(SysConfigManager.instance().getValue("maximumMentionsToProcess", "50"));
        this.maximumDirectMessagesToProcess = Integer.valueOf(SysConfigManager.instance().getValue("maximumDirectMessagesToProcess", "50"));
        this.twitterPeekDomain = SysConfigManager.instance().getValue("twitterPeekDomain", "twitterpeek");
        this.doubleQuotes = SysConfigManager.instance().getValue("doubleQuotes", "\u201C\u201D\u201F\u2033\u2036").toCharArray();
        this.apostrophes = SysConfigManager.instance().getValue("apostrophes", "\u2035\u2032\u2018\u2019\u201B").toCharArray();
        this.dashes = SysConfigManager.instance().getValue("dashes", "\u001E\u2013\u2014\u2015").toCharArray();
        this.isTransliterate = Boolean.parseBoolean(SysConfigManager.instance().getValue("transliterate", "true"));

        /**
         * Initializes the TweetHandler by configuring the http client instance.
         */
        this.httpClient = new HttpClient(getConnectionManager());
        this.httpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(MAX_RETRIES, true));
        this.httpClient.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, CONNECTION_TIMEOUT);
    }

    public List<Status> getFriendsTimeline(Twitter twitter, Account account, StopWatch watch, String context) throws TwitterException {
        StopWatchUtils.newTask(watch, "getFriendsTimeline", context, logger);

        int page = 1;
        long maxId = Long.MAX_VALUE;
        Long sinceId = account.getLast_received_tweet_id() == null ? 1L : account.getLast_received_tweet_id();
        Paging paging = new Paging(page, getMaximumTweetsToProcess(account), sinceId, maxId);

        return twitter.getFriendsTimeline(paging);
    }

    public List<Status> getMentions(Twitter twitter, Account account, StopWatch watch, String context) throws TwitterException {
        StopWatchUtils.newTask(watch, "getMentions", context, logger);

        int page = 1;
        long maxId = Long.MAX_VALUE;
        Long sinceId = account.getLast_received_mention_id() == null ? 1L : account.getLast_received_mention_id();
        Paging paging = new Paging(page, getMaximumMentionsToProcess(), sinceId, maxId);

        return twitter.getMentions(paging);
    }

    public List<DirectMessage> getReceivedDirectMessages(Twitter twitter, Account account, StopWatch watch, String context) throws TwitterException {
        StopWatchUtils.newTask(watch, "getReceivedDirectMessages", context, logger);

        int page = 1;
        long maxId = Long.MAX_VALUE;
        Long sinceId = account.getLast_received_dm_id() == null ? 1L : account.getLast_received_dm_id();
        Paging paging = new Paging(page, getMaximumDirectMessagesToProcess(), sinceId, maxId);

        return twitter.getDirectMessages(paging);
    }

    public List<DirectMessage> getSentDirectMessages(Twitter twitter, Account account, StopWatch watch, String context) throws TwitterException {
        StopWatchUtils.newTask(watch, "getSentDirectMessages", context, logger);

        int page = 1;
        long maxId = Long.MAX_VALUE;
        Long sinceId = account.getLast_sent_dm_id() == null ? 1L : account.getLast_sent_dm_id();
        Paging paging = new Paging(page, getMaximumDirectMessagesToProcess(), sinceId, maxId);

        return twitter.getSentDirectMessages(paging);
    }

    public void cleanup() {
        try {
            httpClient.getHttpConnectionManager().closeIdleConnections(0);
        } catch (Throwable t) {
            // nothing to do here
        }
    }

    public boolean processMessage(Account account, Status message, int messageNumber, String messageId, String twitterId, String storeBucket, String context, StopWatch watch) throws Exception {
        boolean result = false;

        long start = System.nanoTime();

        try {
            String messageName = "TWITTER_STATUS_" + messageId;

            if(logger.isDebugEnabled())
                logger.debug(String.format("msgNum=%d, messageId=%s, message is new for %s", messageNumber, messageId, context));

            try {
                StopWatchUtils.newTask(watch, String.format("msgNum=%s, storeCheck (%s)", messageNumber, messageName), context, logger);

                if (!messageIdStore.hasMessage(account.getId(), storeBucket, messageName, account.getCountry())) {
                    StopWatchUtils.newTask(watch, String.format("msgNum=%s, addToStore (%s)", messageNumber, messageName), context, logger);

                    if (messageIdStore.addMessage(account.getId(), storeBucket, messageName, account.getCountry())) {
                        StopWatchUtils.newTask(watch, String.format("msgNum=%s, saving", messageNumber), context, logger);

                        saveMessage(message, account, messageId, messageNumber, isMention(message, twitterId));
                    } else {
                        if(logger.isDebugEnabled())
                         logger.warn(String.format("dup has been avoided msgNum=%s (%s) for %s", messageNumber, messageName, context));
                    }
                } else {
                    if(logger.isDebugEnabled())
                        logger.debug(String.format("Skipping message since it was found in the Message ID Store. msgNum=%d, messageId=%s, messageName=%s, account %s", messageNumber, messageId, messageName, context));
                }
            } catch (Throwable e) {
                StopWatchUtils.newTask(watch, String.format("msgNum=%s, removeFromStore", messageNumber), context, logger);

                messageIdStore.deleteMessage(account.getId(), storeBucket, messageName, account.getCountry());

                if(logger.isDebugEnabled())
                    logger.debug(String.format("Error saving message. msgNum=%d, messageId=%s, account %s", messageNumber, messageId, context), e);
                logger.warn(String.format("Error saving message. msgNum=%d, messageId=%s, account %s. reason: %s", messageNumber, messageId, context, e.getMessage()));
            }

            result = true;
        } finally {
            if(logger.isDebugEnabled())
                logger.debug(String.format("msgNum=%d, checked if messageId=%s is known in %dms for %s", messageNumber, messageId, (System.nanoTime() - start) / 1000000, context));
        }

        return result;
    }

    public boolean processMessage(Account account, DirectMessage message, int messageNumber, String messageId, String storeBucket, String context, StopWatch watch) throws Exception {
        boolean result = false;

        long start = System.nanoTime();

        try {
            String messageName = "TWITTER_DM_" + messageId;

            if(logger.isDebugEnabled())
                logger.debug(String.format("msgNum=%d, messageId=%s, message is new for %s", messageNumber, messageId, context));

            try {
                StopWatchUtils.newTask(watch, String.format("msgNum=%s, storeCheck (%s)", messageNumber, messageName), context, logger);

                if (!messageIdStore.hasMessage(account.getId(), storeBucket, messageName, account.getCountry())) {
                    StopWatchUtils.newTask(watch, String.format("msgNum=%s, addToStore (%s)", messageNumber, messageName), context, logger);

                    if (messageIdStore.addMessage(account.getId(), storeBucket, messageName, account.getCountry())) {
                        StopWatchUtils.newTask(watch, String.format("msgNum=%s, saving", messageNumber), context, logger);
                        saveMessage(message, account, messageId, messageNumber);
                    } else {
                        logger.warn(String.format("dup has been avoided msgNum=%s (%s) for %s", messageNumber, messageName, context));
                    }
                } else {
                    logger.debug(String.format("Skipping message since it was found in the Message ID Store. msgNum=%d, messageId=%s, messageName=%s, account %s", messageNumber, messageId, messageName, context));
                }
            } catch (Throwable e) {
                StopWatchUtils.newTask(watch, String.format("msgNum=%s, removeFromStore", messageNumber), context, logger);

                messageIdStore.deleteMessage(account.getId(), storeBucket, messageName, account.getCountry());

                logger.warn(String.format("Error saving message. msgNum=%d, messageId=%s, account %s. reason: %s", messageNumber, messageId, context, e.getMessage()));
            }

            result = true;
        } finally {
            if(logger.isDebugEnabled())
                logger.debug(String.format("msgNum=%d, checked if messageId=%s is known in %dms for %s", messageNumber, messageId, (System.nanoTime() - start) / 1000000, context));
        }

        return result;
    }

    private int getMaximumTweetsToProcess(Account account) {
        return isFirstTime(account) ? maximumMessagesToProcessFirstTime : maximumTweetsToProcess;
    }

    private int getMaximumMentionsToProcess() {
        return maximumMentionsToProcess;
    }

    private int getMaximumDirectMessagesToProcess() {
        return maximumDirectMessagesToProcess;
    }

    public boolean isFirstTime(Account account) {
        return account.getLast_mailcheck() == null && account.getMessage_count() == 0;
    }

    public void updateAccount(Account account, int newMessages, int folderDepth, Date lastMessageReceivedDate, long lastReceivedTweetId, long lastReceivedMentionId, long lastReceivedDmId, long lastSentDmId) throws DALException {
        account.setMessage_count(account.getMessage_count() + newMessages);

        account.setLast_mailcheck(new Date(System.currentTimeMillis()));

        account.setFolder_depth(folderDepth);

        if (lastReceivedTweetId > 0) {
            account.setLast_received_tweet_id(lastReceivedTweetId);
        }

        if (lastReceivedMentionId > 0) {
            account.setLast_received_mention_id(lastReceivedMentionId);
        }

        if (lastReceivedDmId > 0) {
            account.setLast_received_dm_id(lastReceivedDmId);
        }

        if (lastSentDmId > 0) {
            account.setLast_sent_dm_id(lastSentDmId);
        }

        if (lastMessageReceivedDate == null) {
            DALDominator.updateAccountReceiveInfo(account);
        } else {
            account.setLast_received_date(lastMessageReceivedDate);

            DALDominator.updateAccountReceiveInfoAndReceivedDate(account);
        }
    }

    private boolean saveMessage(Status message, Account account, String messageId, int messageNumber, boolean isMention) throws Exception {
        if(logger.isDebugEnabled())
            logger.debug(String.format("[%s] msgNum=%d, saving, uid=%s, messageId=%s", account.getName(), messageNumber, account.getUser_id(), messageId));

        OriginalReceivedEmail original = createOriginalReceivedEmail(message, account, messageId);

        if(logger.isDebugEnabled())
            logger.debug(String.format("[%s] msgNum=%d, saving %s", account.getName(), messageNumber, original));

        String messageType = isMention ? "TWITTER_MENTION" : "TWITTER_STATUS";
        String source = message.getSource();

        EmailPojo pojo = getEmailPojo(original, account.getName(), message.getUser().getName(), messageType, source);

        int saveStatus = DALDominator.newSaveMailTwitter(pojo, original);

        if(logger.isDebugEnabled())
            logger.debug(String.format("[%s] msgNum=%d, saving completed with status %s", account.getName(), messageNumber, saveStatus));

        return true;
    }

    /**
     * Saves the message, mostly via old framework code, hence few anti-patterns are still in place
     *
     * @return
     */
    private boolean saveMessage(DirectMessage message, Account account, String messageId, int messageNumber) throws Exception {
        if(logger.isDebugEnabled())
            logger.debug(String.format("[%s] msgNum=%d, saving, uid=%s, messageId=%s", account.getName(), messageNumber, account.getUser_id(), messageId));

        OriginalReceivedEmail original = createOriginalReceivedEmail(message, account, messageId);
        if(logger.isDebugEnabled())
            logger.debug(String.format("[%s] msgNum=%d, saving %s", account.getName(), messageNumber, original));

        String messageType = "TWITTER_DM";
        String source = null;  // not available for DMs

        EmailPojo pojo = getEmailPojo(original, account.getName(), message.getSender().getName(), messageType, source);

        int saveStatus = DALDominator.newSaveMailTwitter(pojo, original);
        if(logger.isDebugEnabled())
            logger.debug(String.format("[%s] msgNum=%d, saving completed with status %s", account.getName(), messageNumber, saveStatus));

        return true;
    }

    private OriginalReceivedEmail createOriginalReceivedEmail(Status message, Account account, String messageId) throws Exception {
        return createOriginalReceivedEmail(account, messageId, message.getUser(), message.getCreatedAt(), message.getText());
    }

    private OriginalReceivedEmail createOriginalReceivedEmail(DirectMessage message, Account account, String messageId) throws Exception {
        return createOriginalReceivedEmail(account, messageId, message.getSender(), message.getCreatedAt(), message.getText());
    }

    private OriginalReceivedEmail createOriginalReceivedEmail(Account account, String messageId, User user, Date createDate, String message) throws Exception {
        OriginalReceivedEmail original = new OriginalReceivedEmail();

        original.setMail_type(messageId);
        original.setCc("");
        original.setBcc("");
        original.setEmailTo(account.getLoginName());
        original.setEmailFrom(user.getScreenName() + "@" + twitterPeekDomain);
        original.setReply(user.getScreenName() + "@" + twitterPeekDomain);
        original.setUid(messageId);
        original.setUserId(account.getUser_id());
        original.setMailTime(ReceiverUtilsTools.dateToStr(createDate));

        String twitterMessage = clean(message);

        original.setSubject(twitterMessage);
        original.setBody(twitterMessage.getBytes(UTF_8_ENCODING));

        if (containsUrl(message)) {
            original.setAttachList(createTweetAttachments(twitterMessage));
        }

        return original;
    }

    /**
     * Returns true if the Status message is a twitter "mention" - mentions start with "@twitter_id"
     *
     * @param message   The Status message
     * @param twitterId The authenticated user's twitter id
     * @return true if the message is a mention, false otherwise
     */
    private boolean isMention(Status message, String twitterId) {
        return twitterId != null && message.getText().toUpperCase().startsWith("@" + twitterId.toUpperCase());
    }

    private EmailPojo getEmailPojo(OriginalReceivedEmail original, String mailbox, String fromAlias, String messageType, String source) {
        Email email = new Email();

        email.setMessage_type(messageType);

        email.setTwitterMeta(new TwitterMeta(Long.valueOf(original.getUid()), parseSource(source)));

        email.setSubject(original.getSubject());
        email.setFrom(original.getEmailFrom());
        email.setTo(ReceiverUtilsTools.subAddress(original.getEmailTo()));
        email.setCc(ReceiverUtilsTools.subAddress(original.getCc()));
        email.setBcc(ReceiverUtilsTools.subAddress(original.getBcc()));
        email.setMaildate(original.getMailTime());
        email.setStatus("0");
        email.setUserId(original.getUserId());
        email.setReply(ReceiverUtilsTools.subAddress(original.getReply()));

        String fromAddr = original.getEmailFrom();

        if (StringUtils.isNotEmpty(fromAlias)) {
            email.setFrom_alias(fromAlias);
        } else if (fromAddr.contains("@")) {
            email.setFrom_alias(fromAddr.split("@")[0]);
        } else {
            email.setFrom_alias(fromAddr);
        }

        email.setOriginal_account(mailbox);

        List<Attachment> list = new ArrayList<Attachment>();

        List<OriginalReceivedAttachment> allAttachList = original.getAttachList();

        for (OriginalReceivedAttachment attach : allAttachList) {
            if(logger.isDebugEnabled())
                logger.info(String.format("processing attachment %s", attach));

            Attachment attachment = new Attachment();
            attachment.setName(attach.getName());
            attachment.setSize(attach.getId());
            attachment.setData(attach.getData());

            list.add(attachment);
        }

        Body body = new Body();

        body.setData(original.getBody());

        email.setBodySize(body.getData().length);

        EmailPojo emailPojo = new EmailPojo();
        emailPojo.setEmail(email);
        emailPojo.setBody(body);
        emailPojo.setAttachement(list);


        return emailPojo;
    }

    /**
     * If the source is an html anchor, return the anchor name,  otherwise, return the source string.
     * <p/>
     * For example, given:
     * <p/>
     * "<a href=\"http://www.ping.fm/\" rel=\"nofollow\">Ping.fm</a>"
     * <p/>
     * We return "Ping.fm"
     *
     * @param source
     * @return
     */
    private String parseSource(String source) {
        String src = source;

        try {
            Matcher matcher = HREF_PATTERN.matcher(source);
            if (matcher.matches()) {
                src = matcher.group(1);
            }
        } catch (Throwable t) {
            if(logger.isDebugEnabled())
                logger.debug("Error parsing source  " + source, t);
        }

        // "source" will be Null for DMs - default to "web"
        if (src == null) {
            src = "web";
        }

        return src;
    }

    /**
     * Removes special characters from the twitter message
     *
     * @param message
     * @return
     */
    private String clean(String message) {
        if (message == null) {
            return null;
        }
        String newMessage = isTransliterate ? transliterator.transform(message) : message;
        StringBuilder builder = new StringBuilder();
        for (char _char : newMessage.toCharArray()) {
            //replaces NON ANSII special symbols
            if (ArrayUtils.contains(doubleQuotes, _char)) {
                builder.append("\"");
                continue;
            }
            if (ArrayUtils.contains(apostrophes, _char)) {
                builder.append("\'");
                continue;
            }
            if (ArrayUtils.contains(dashes, _char)) {
                builder.append("-");
                continue;
            }
            //decomposes and ANSII symbols are added into the buffer .
            String normalize = Normalizer.normalize(_char, Normalizer.NFKD);
            if (normalize.length() > 1) {
                for (char _normalizeChar : normalize.toCharArray()) {
                    if (CharUtils.isAscii(_normalizeChar)) {
                        builder.append(_normalizeChar);
                    }
                }
            } else if (normalize.length() == 1) {
                char c = normalize.charAt(0);
                if (c < 32 || c > 255) {
                    c = 63; // '?'
                }
                builder.append(c);
            }
        }
        return builder.toString();
    }

    /**
     * Returns true if the twitter message contains a url
     *
     * @param twitterMessage The twitter message.
     */
    private boolean containsUrl(String twitterMessage) {
        return LINK_PATTERN.matcher(twitterMessage).find();
    }

    private List<OriginalReceivedAttachment> createTweetAttachments(String twitterMessage) throws Exception {
        List<OriginalReceivedAttachment> attachments = new ArrayList<OriginalReceivedAttachment>();

        Matcher matcher = LINK_PATTERN.matcher(twitterMessage);

        int index = 0;

        while (matcher.find(index)) {
            String url = matcher.group(1);

            OriginalReceivedAttachment attachment = new OriginalReceivedAttachment();

            String extension = TWEET_TXT_ATTACHMENT_EXTENSION;
            if (isImageResource(url)) {
                extension = TWEET_IMG_ATTACHMENT_EXTENSION;
            }

            String attachName = StringUtils.getNameFromLink(url, MAX_ATTACHMENT_NAME_LENGTH);

            attachment.setName(attachName + "_" + index + extension);

            attachment.setData(("LINK:" + url).getBytes(UTF_8_ENCODING));

            attachments.add(attachment);

            attachment.setSize(attachment.getData() == null ? 0 : attachment.getData().length);


            index = matcher.end();
        }

        return attachments;
    }

    private boolean isImageResource(String url) {

        boolean isImage = false;

        GetMethod get = null;

        try {

            if (url.contains("twitpic.com")) {
                isImage = true;
            } else if (hasImageExtension(url)) {
                isImage = true;
            } else {
                HttpMethodParams params = new HttpMethodParams();
                params.setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);

                get = new GetMethod(url);
                get.setParams(params);
                get.setFollowRedirects(true);

                int returnCode = httpClient.executeMethod(get);

                if (returnCode == 301) {
                    if(logger.isDebugEnabled())
                        logger.debug(String.format("url %s is a redirect- attempting to retrieve \"location\" from response header", url));

                    String redirect = get.getResponseHeader("location").getValue();

                    isImage = hasImageExtension(redirect);
                }
            }
        } catch (Throwable t) { // IOException, etc.
            logger.warn(String.format("Unable to determine if url %s points to an image resource", url), t);
        } finally {
            try {
                if (get != null) {
                    get.releaseConnection();
                }
            } catch (Throwable t) {
                // nothing to do here
            }
        }

        return isImage;
    }

    private boolean hasImageExtension(String rsc) {
        rsc = rsc.toLowerCase();
        return rsc != null && (rsc.endsWith(".jpg") || rsc.endsWith(".jpeg") || rsc.endsWith(".gif") || rsc.endsWith(".png"));
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


    //TODO - Paul - remove duplication
    public void updateWithLoginFailure(String context, Account account) throws DALException {
        account.setLast_mailcheck(new Date(System.currentTimeMillis()));

        if (account.getLogin_failures() == null) {
            account.setLogin_failures(1);
        } else {
            account.setLogin_failures(account.getLogin_failures() + 1);
        }

        account.setLast_login_failure(new Date(System.currentTimeMillis()));

        DALDominator.updateAccountReceiveInfo(account);

        if (exceededMaximumLoginFailures(account)) {
            saveAccountLockedNotificationEm(context, account);
        }
    }

    public boolean exceededMaximumLoginFailures(Account account) {
        return account.getLogin_failures() != null && account.getLogin_failures() > maximumLoginFailures;
    }


    void saveAccountLockedNotificationEm(String context, Account account) {
        try {
            Email email = new Email();
            email.setSubject(lockedOutMessage);
            email.setFrom("peek_inc@twitterpeek");
            email.setFrom_alias("Peek");
            email.setTo(account.getLoginName());
            email.setUserId(account.getUser_id());
            email.setEmail_type("TWITTER");
            email.setMessage_type("TWITTER_ALERT");
            email.setBodySize(email.getSubject().length());
            email.setStatus("0");

            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            email.setMaildate(dateFormat.format(new Date(System.currentTimeMillis()))); // ugh!

            // TwitterMeta info (tweeet-id and source)
            email.setTwitterMeta(new TwitterMeta(System.currentTimeMillis()));

            email.setOriginal_account(account.getLoginName() + "@twitterpeek");

            // create the pojgo
            EmailPojo emailPojo = new EmailPojo();
            emailPojo.setEmail(email);

            Body body = new Body();
            body.setData(email.getSubject().getBytes());

            // note, the email will be encoded to prevent sql-injection. since this email is destined directly for the
            // device, we need to reverse the encoding after the call to newSaveEmail
            new EmailRecievedService().newSaveEmail(account, email, body);

        } catch (Throwable t) {
            logger.warn(String.format("failed to save locked out message for %s", context), t);
        }
    }
}
