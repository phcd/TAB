package com.archermind.txtbl.domain;

import com.archermind.txtbl.utils.CipherTools;
import com.archermind.txtbl.utils.UtilsTools;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import com.archermind.txtbl.utils.StringUtils;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class Account implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String DELETE_ACCOUNT_STATUS = "F";
    private static final String NEW_ACCOUNT_STATUS = "T";
    private static final String FIRST_TIME_FETCH_STATUS = "FT";

    public static final String ACTIVE = "1";
    public static final String INACTIVE = "0";
    public static final String DEFAULT_FOLDER = "INBOX";
    public static final String GMAIL_ALL_MAILS_FOLDER = "[Gmail]/All Mail";
    public static final String GMAIL_DRAFTS_FOLDER = "[Gmail]/Drafts";
    public static final String GOOGLEMAIL_ALL_MAILS_FOLDER = "[Google Mail]/All Mail";
    public static final String GOOGLEMAIL_DRAFTS_FOLDER = "[Google Mail]/Drafts";

    // Fields                      

    private int id = 0;

    private String user_id = "";

    private String name = "";

    private String loginName = "";

    private String alias_name = null;

    private String password = "";

    private String securityPassword = "";

    private String sendHost = "";

    private String sendPort = "";

    private String sendProtocolType = "";

    private String receiveHost = "";

    private String receivePort = "";

    private String receiveProtocolType = "";

    private String receiveHostFbaPath = "";

    private String receiveHostPrefix = "";

    private String sendHostFbaPath = "";

    private String sendHostPrefix = "";

    private String sendTs = "";

    private String receiveTs = "";

    private String needAuth = "";

    private String comment = "";

    private String status = "";

    private int server_id = 0; // receive protocol

    private String folder_id = "";

    private String messaging_id = "";

    private String domain;

    private transient HashMap<String, String> meta;

    /* add fields start */

    private String command;

    /* add fields end */
    private String message = "";

    private String save_type = "";

    private String orderFlag = "";

    private String register_status = "0";

    private int key_id = 0;

    private int sent_id = 0; // sent protocol

    MspToken mspToken = new MspToken();

    private String login_status = "0";

    private String features = "";

    private Date register_time;

    private String folder_hash;

    private Integer folder_depth;

    private int message_count;

    private Date last_mailcheck;

    private Date last_reconciliation;

    private Date last_received_date;

    private String imei;

    private Long last_received_tweet_id;

    private Long last_received_dm_id;

    private Long last_sent_dm_id;

    private Long last_received_mention_id;

    private Integer login_failures;

    private Date last_login_failure;

    private String contactEmail;

    private String exchangeConnMode;
    private Country country;

    private Date last_subscribeAlert;
    private Date last_calendar;

    private boolean keyIdSet = false;

    private boolean passwordPopulatedWithEncryptedPassword = false;

    private String oauthToken;
    private String oauthTokenSecret;
    private String consumerKey;
    private String consumerSecret;

    private int totalReceived;

    private int totalSent;

    private String active_sync_key;

    private String active_sync_device_id;

    private String active_sync_device_type;
    private String contacts_sync_key;
    private String protocol;
    private String OWAUrl;
    private String folderName;
    private String excludeFolder;
    private PartnerCode partnerCode;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * default constructor
     */

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        if (name != null) {
            this.name = name.toLowerCase().trim();
        } else
            this.name = name;

    }

    public String getLoginName() {
        return this.loginName;
    }

    public void setLoginName(String loginName) {
        if (loginName != null) {
            this.loginName = loginName.toLowerCase().trim();
            if (loginName.endsWith("@txtblmail.cn")) {
                loginName = loginName.substring(0, loginName
                        .indexOf("@txtblmail.cn"));

                this.loginName = loginName;
            }
        } else
            this.loginName = loginName;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Encrypt password

    public String getSecurityPassword() {

        try {

            String sRet[] = UtilsTools.getKeyString(key_id, name)
                    .split(";,;,;");
            String sKey = sRet[0];

            // System.out.println("Encrypt key = " + sKey + " iKeyId = " +
            // iKeyId);

            this.key_id = Integer.parseInt(sRet[1]);

            securityPassword = new String(Base64.encodeBase64(CipherTools
                    .RC4Encrypt(password, sKey)), "utf-8");

        } catch (UnsupportedEncodingException e) {

            securityPassword = "";
            e.printStackTrace();

        }

        // this.securityPassword = password;

        return securityPassword;
    }

    // Decrypt password
    public void setSecurityPassword(String securityPassword) {
        this.password = securityPassword;
        if (keyIdSet) {
            decryptPassword();
        } else {
            passwordPopulatedWithEncryptedPassword = true;
        }
    }

    private void decryptPassword() {
        try {

            String sRet[] = UtilsTools.getKeyString(key_id, name).split(";,;,;");
            String sKey = sRet[0];

            this.password = CipherTools.RC4Decrype(Base64.decodeBase64(this.password.getBytes("utf-8")), sKey);

        } catch (UnsupportedEncodingException e) {
            securityPassword = "";
            e.printStackTrace();
        } finally {
            passwordPopulatedWithEncryptedPassword = false;
            keyIdSet = false;
        }
    }

    public String getSendHost() {
        return this.sendHost;
    }

    public void setSendHost(String sendHost) {
        this.sendHost = sendHost;
    }

    public String getSendPort() {
        return this.sendPort;
    }

    public void setSendPort(String sendPort) {
        this.sendPort = sendPort;
    }

    public String getSendProtocolType() {
        return this.sendProtocolType;
    }

    public void setSendProtocolType(String sendProtocolType) {
        this.sendProtocolType = sendProtocolType;
    }

    public String getReceiveHost() {
        return this.receiveHost;
    }

    public void setReceiveHost(String receiveHost) {
        this.receiveHost = receiveHost;
    }

    public String getReceivePort() {
        return this.receivePort;
    }

    public void setReceivePort(String receivePort) {
        this.receivePort = receivePort;
    }

    public String getReceiveProtocolType() {
        return this.receiveProtocolType;
    }

    public void setReceiveProtocolType(String receiveProtocolType) {
        this.receiveProtocolType = receiveProtocolType;
    }

    public String getSendTs() {
        return this.sendTs;
    }

    public void setSendTs(String sendTs) {
        this.sendTs = sendTs;
    }

    public String getReceiveTs() {
        return this.receiveTs;
    }

    public void setReceiveTs(String receiveTs) {
        this.receiveTs = receiveTs;
    }

    public String getNeedAuth() {
        return this.needAuth;
    }

    public void setNeedAuth(String needAuth) {
        this.needAuth = needAuth;
    }

    public String getComment() {
        return this.comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public HashMap<String, String> getMeta() {
        return meta;
    }

    public void setMeta(HashMap<String, String> meta) {
        this.meta = meta;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAlias_name() {
        return alias_name;
    }

    public void setAlias_name(String alias_name) {
        this.alias_name = alias_name;
    }

    public int getServer_id() {
        return server_id;
    }

    public void setServer_id(int server_id) {
        this.server_id = server_id;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getSave_type() {
        return save_type;
    }

    public void setSave_type(String save_type) {
        this.save_type = save_type;
    }

    public String getOrderFlag() {
        return orderFlag;
    }

    public void setOrderFlag(String orderFlag) {
        this.orderFlag = orderFlag;
    }

    public static void main(String[] args) {
        Account account = new Account();
        account.setUser_id("88903");
        account.setName("sjbriggs@live.com");
        account.setSecurityPassword("cxNb5w5r3U9g7l1n");
        account.setKey_id(1);

        System.out.println(account.getSecurityPassword());
    }

    public String getRegister_status() {
        return register_status;
    }

    public void setRegister_status(String register_status) {
        this.register_status = register_status;
    }

    public int getKey_id() {
        return key_id;
    }

    public void setJustKey_id(int key_id) {
        this.key_id = key_id;
    }

    public void setKey_id(int key_id) {
        this.key_id = key_id;
        this.keyIdSet = true;
        if (this.passwordPopulatedWithEncryptedPassword) {
            decryptPassword();
        }
    }

    public int getSent_id() {
        return sent_id;
    }

    public void setSent_id(int sent_id) {
        this.sent_id = sent_id;
    }

    public MspToken getMspToken() {
        return mspToken;
    }

    public void setMspToken(MspToken mspToken) {
        this.mspToken = mspToken;
    }

    public String getLogin_status() {
        return login_status;
    }

    public void setLogin_status(String login_status) {
        this.login_status = login_status;
    }

    public String getFeatures() {
        return features;
    }

    public void setFeatures(String features) {
        this.features = features;
    }

    public Date getRegister_time() {
        return register_time;
    }

    public void setRegister_time(Date register_time) {
        this.register_time = register_time;
    }

    public String getReceiveHostFbaPath() {
        return receiveHostFbaPath;
    }

    public void setReceiveHostFbaPath(String receiveHostFbaPath) {
        this.receiveHostFbaPath = receiveHostFbaPath;
    }

    public String getReceiveHostPrefix() {
        return receiveHostPrefix;
    }

    public void setReceiveHostPrefix(String receiveHostPrefix) {
        this.receiveHostPrefix = receiveHostPrefix;
    }

    public String getSendHostFbaPath() {
        return sendHostFbaPath;
    }

    public void setSendHostFbaPath(String sendHostFbaPath) {
        this.sendHostFbaPath = sendHostFbaPath;
    }

    public String getSendHostPrefix() {
        return sendHostPrefix;
    }

    public void setSendHostPrefix(String sendHostPrefix) {
        this.sendHostPrefix = sendHostPrefix;
    }

    public String getFolder_hash() {
        return folder_hash;
    }

    public void setFolder_hash(String folder_hash) {
        this.folder_hash = folder_hash;
    }

    public Integer getMessage_count() {
        return message_count;
    }

    public void setMessage_count(Integer message_count) {
        this.message_count = message_count;
    }

    public Date getLast_mailcheck() {
        return last_mailcheck;
    }

    public void setLast_mailcheck(Date last_mailcheck) {
        this.last_mailcheck = last_mailcheck;
    }

    public Integer getFolder_depth() {
        return folder_depth;
    }

    public void setFolder_depth(Integer folder_depth) {
        this.folder_depth = folder_depth;
    }

    public Date getLast_reconciliation() {
        return last_reconciliation;
    }

    public void setLast_reconciliation(Date last_reconciliation) {
        this.last_reconciliation = last_reconciliation;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public boolean isGmailHosted() {
        return Protocol.isGmailHosted(this.receiveProtocolType);
    }

    public boolean isYahooHosted() {
        return Protocol.isYahooHosted(this.receiveProtocolType, name);
    }

    public boolean isExchange() {
        return Protocol.isExchange(this.receiveProtocolType);
    }

    public boolean isMSP() {
        return Protocol.isMSP(this.receiveProtocolType);
    }

    public boolean isXobniImapIdle() {
        return Protocol.isXobniImapIdle(this.receiveProtocolType);
    }

    public boolean isXobniImap() {
        return Protocol.isXobniIMap(this.receiveProtocolType);
    }

    public boolean isXobniOauthIdle() {
        return Protocol.isXobniOauthIdle(this.receiveProtocolType);
    }

    public boolean isXobniOauth() {
        return Protocol.isXobniOauth(this.receiveProtocolType);
    }

    public boolean isXobniYahooImap() {
        return Protocol.isXobniYahooImap(this.receiveProtocolType);
    }

    public boolean isPop3() {
        return Protocol.isPop3(this.receiveProtocolType);
    }

    public boolean isGmailPop3() {
        return Protocol.isGmailPop3(this.receiveProtocolType);
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE);
        builder.append("userId:").append(user_id).append(" name:").append(name).append(" command:").append(command).append(" receiveProtocolType:").append(receiveProtocolType).
                append(" server_id:").append(server_id);
        return builder.toString();
    }

    public Date getLast_received_date() {
        return last_received_date;
    }

    public void setLast_received_date(Date last_received_date) {
        this.last_received_date = last_received_date;
    }

    public Long getLast_received_tweet_id() {
        return last_received_tweet_id;
    }

    public void setLast_received_tweet_id(Long last_received_tweet_id) {
        this.last_received_tweet_id = last_received_tweet_id;
    }

    public Long getLast_received_dm_id() {
        return last_received_dm_id;
    }

    public Long getLast_sent_dm_id() {
        return last_sent_dm_id;
    }

    public void setLast_received_mention_id(Long last_received_mention_id) {
        this.last_received_mention_id = last_received_mention_id;
    }

    public Long getLast_received_mention_id() {
        return last_received_mention_id;
    }

    public void setLast_received_dm_id(Long last_received_dm_id) {
        this.last_received_dm_id = last_received_dm_id;
    }

    public void setLast_sent_dm_id(Long last_sent_dm_id) {
        this.last_sent_dm_id = last_sent_dm_id;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public Integer getLogin_failures() {
        return login_failures;
    }

    public void setLogin_failures(Integer login_failures) {
        this.login_failures = login_failures;
    }

    public Date getLast_login_failure() {
        return last_login_failure;
    }

    public void setLast_login_failure(Date last_login_failure) {
        this.last_login_failure = last_login_failure;
    }

    public String getExchangeConnMode() {
        return exchangeConnMode;
    }

    public void setExchangeConnMode(String exchangeConnMode) {
        this.exchangeConnMode = exchangeConnMode;
    }

    public boolean isIMapIdle() {
        return Protocol.isIMapIdle(this.receiveProtocolType);
    }

    public String getFolder_id() {
        return folder_id;
    }

    public void setFolder_id(String folder_id) {
        this.folder_id = folder_id;
    }

    public String getMessaging_id() {
        return messaging_id;
    }

    public void setMessaging_id(String messaging_id) {
        this.messaging_id = messaging_id;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public Date getLast_subscribeAlert() {
        return last_subscribeAlert;
    }

    public void setLast_subscribeAlert(Date last_subscribeAlert) {
        this.last_subscribeAlert = last_subscribeAlert;
    }

    public String getOauthToken() {
        return oauthToken;
    }

    public void setOauthToken(String oauthToken) {
        this.oauthToken = oauthToken;
    }

    public String getOauthTokenSecret() {
        return oauthTokenSecret;
    }

    public void setOauthTokenSecret(String oauthTokenSecret) {
        this.oauthTokenSecret = oauthTokenSecret;
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }


    public String getConsumerSecret() {
        return consumerSecret;
    }

    public void setConsumerSecret(String consumerSecret) {
        this.consumerSecret = consumerSecret;
    }

    public int getTotalReceived() {
        return totalReceived;
    }

    public void setTotalReceived(int totalReceived) {
        this.totalReceived = totalReceived;
    }

    public int getTotalSent() {
        return totalSent;
    }

    public void setTotalSent(int totalSent) {
        this.totalSent = totalSent;
    }

    public String getActive_sync_key() {
        return active_sync_key;
    }

    public void setActive_sync_key(String active_sync_key) {
        this.active_sync_key = active_sync_key;
    }

    public Date getLast_calendar() {
        return last_calendar;
    }

    public void setLast_calendar(Date last_calendar) {
        this.last_calendar = last_calendar;
    }

    public void setNewAccountStatus() {
        this.status = NEW_ACCOUNT_STATUS;
    }

    public void setDeletedAccountStatus() {
        this.status = DELETE_ACCOUNT_STATUS;
    }

    public boolean isDeleted() {
        return DELETE_ACCOUNT_STATUS.equals(status);
    }

    public boolean isNewAccount() {
        return NEW_ACCOUNT_STATUS.equals(status);
    }


    public String getActive_sync_device_id() {
        return active_sync_device_id;
    }

    public void setActive_sync_device_id(String active_sync_device_id) {
        this.active_sync_device_id = active_sync_device_id;
    }

    public String getActive_sync_device_type() {
        return active_sync_device_type;
    }

    public void setActive_sync_device_type(String active_sync_device_type) {
        this.active_sync_device_type = active_sync_device_type;
    }

    public String getContacts_sync_key() {
        return contacts_sync_key;
    }

    public void setContacts_sync_key(String contacts_sync_key) {
        this.contacts_sync_key = contacts_sync_key;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getOWAUrl() {
        return OWAUrl;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setOWAUrl(String OWAUrl) {
        this.OWAUrl = OWAUrl;
    }

    public boolean hasAdvancedConfigDetails() {
        return !StringUtils.isEmpty(this.protocol);
    }

    public boolean advanceConfigSameAsRecvServerConfig(Server server) {
        return StringUtils.equals(this.receiveHost, server.getReceiveHost()) && StringUtils.equals(this.receivePort, server.getReceivePort())
                && StringUtils.equals(this.receiveProtocolType, server.getReceiveProtocolType()) && StringUtils.equals(this.receiveTs, server.getReceiveTs())
                && StringUtils.equals(this.receiveHostFbaPath, server.getReceiveHostFbaPath()) && StringUtils.equals(this.receiveHostPrefix, server.getReceiveHostPrefix());
    }

    public boolean isXobniAccount() {
        return Country.Xobni == country;
    }


    public String getFolderNameToConnect() {
        if (!StringUtils.isEmpty(folderName)) {
            return folderName;
        }

        if (isXobniImapIdle() || isXobniOauthIdle()) {
            return GMAIL_ALL_MAILS_FOLDER;
        }
        return DEFAULT_FOLDER;
    }

    public boolean isTwitterAccount() {

        return name.contains("twitterpeek");
    }

    public List<String> validate() {
        List<String> errors = new ArrayList<String>();
        if (StringUtils.isEmpty(name)) {
            errors.add("name empty");
        }
        boolean credentialIncomplete;
        boolean passwordEmpty = StringUtils.isEmpty(password);
        if (isXobniAccount()) {
            credentialIncomplete = passwordEmpty && (StringUtils.isEmpty(oauthToken) || StringUtils.isEmpty(oauthTokenSecret));
            if (passwordEmpty) {
                if ((StringUtils.isEmpty(consumerKey) != StringUtils.isEmpty(consumerSecret))) {
                    errors.add("consumerKey or consumerSecret is empty. If Oauth account both need need to be empty or both populated.");
                }
            }
        } else {
            credentialIncomplete = StringUtils.isEmpty(password);
        }
        if (credentialIncomplete) {
            errors.add("credentials incomplete");
        }
        return errors;
    }

    public boolean shouldValidateUsingOauth() {
        return isXobniAccount() && StringUtils.isEmpty(password);
    }

    public boolean isActive() {
        return ACTIVE.equals(this.status);
    }

    public boolean isInActive() {
        return INACTIVE.equals(this.status);
    }

    public boolean isIdleAccount() {
        return Protocol.isIdle(receiveProtocolType);
    }

    public String getLoginFailureNotificationMessage() {
        String message = "Credentials invalid for " + name + "\n";
        if (isXobniOauthIdle()) {
            message += "Oauth token : " + StringUtils.trimToEmpty(oauthToken) + "\n";
            message += "Oauth secret token: " + StringUtils.trimToEmpty(oauthTokenSecret) + "\n";
            message += "consumer key: " + StringUtils.trimToEmpty(consumerKey) + "\n";
            message += "consumer secret: " + StringUtils.trimToEmpty(consumerSecret) + "\n";
        }
        return message;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public void setExcludeFolder(String excludeFolder) {
        this.excludeFolder = excludeFolder;
    }

    public void setPartnerCode(PartnerCode partnerCode) {
        this.partnerCode = partnerCode;
    }

    public PartnerCode getPartnerCode() {
        return partnerCode;
    }

    public String getDraftsFolder() {
        if (!StringUtils.isEmpty(excludeFolder)) {
            return excludeFolder;
        }

        String folderNameToConnect = getFolderNameToConnect();
        if (folderNameToConnect.equals(GMAIL_ALL_MAILS_FOLDER)) {
            return GMAIL_DRAFTS_FOLDER;
        }

        if (folderNameToConnect.equals(GOOGLEMAIL_ALL_MAILS_FOLDER)) {
            return GOOGLEMAIL_DRAFTS_FOLDER;
        }

        return null;
    }

    public String[] getFoldersToValidateAgainst() {
        if (isXobniImapIdle() || isXobniOauthIdle()) {
            return new String[]{GMAIL_ALL_MAILS_FOLDER, GOOGLEMAIL_ALL_MAILS_FOLDER, DEFAULT_FOLDER};
        }
        return new String[]{DEFAULT_FOLDER};
    }

    public boolean canHandleHtml() {
        return (partnerCode != null && (PartnerCode.peekint.equals(partnerCode) || PartnerCode.qcom.equals(partnerCode))) || isXobniAccount();
    }

    public boolean hasBeenDispatchedForFirstTimeFetch() {
        return FIRST_TIME_FETCH_STATUS.equals(status);
    }

    public void setDispatchForFirstTimeFetch() {
        status = FIRST_TIME_FETCH_STATUS;
    }
}


