package com.archermind.txtbl.validate.mailbox.inte.impl;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.dal.business.IEmailServerService;
import com.archermind.txtbl.dal.business.IUserService;
import com.archermind.txtbl.dal.business.impl.TxtblWebService;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.Country;
import com.archermind.txtbl.domain.Protocol;
import com.archermind.txtbl.domain.Server;
import com.archermind.txtbl.utils.*;
import com.archermind.txtbl.validate.mailbox.abst.Validate;
import com.archermind.txtbl.validate.mailbox.inte.MailValidateInte;
import com.webalgorithm.exchange.utility.DomainUtility;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

import javax.mail.AuthenticationFailedException;
import java.util.*;


//TODO: include activesync into guessing

public class MailValidateImpl implements MailValidateInte {
    //TODO: India Specific and US specific addresses
    private static final String UNEXPECTED_ERROR = "Whoops! We have encountered an unexpected error. Please email feedback@getpeek.com for support.";
    private static final String CONFIG_SAVE_ERROR = "Whoops! We have encountered an error saving your account config. Please email feedback@getpeek.com for support.";
    private static final String UNABLE_TO_CONFIGURE_MESSAGE = "Whoops! We could not figure out the email settings for %s.  Please email feedback@getpeek.com for support.";

    static final String ACTIVE_SYNC_PREFIX = "Microsoft-Server-ActiveSync";

    private static BeanFactory beanFactory = null;

    private static final Logger log = Logger.getLogger(MailValidateImpl.class.getName());

    private ISysConfigManager sysConfigManager;
    private IEmailServerService emailServerService;
    private IMailer mailer;
    private IUserService userService;

    public MailValidateImpl(ISysConfigManager sysConfigManager, IEmailServerService emailServerService, IMailer mailer, IUserService userService) {

        this.sysConfigManager = sysConfigManager;
        this.emailServerService = emailServerService;
        this.mailer = mailer;
        this.userService = userService;
    }

    static {
        try {
            beanFactory = new XmlBeanFactory(new ClassPathResource("com/archermind/txtbl/validate/mailbox/xml/validatorsBeanFactory.xml"));
            log.info("[initialization validator beanFactory success]");
        } catch (Exception e) {
            log.error("static/MailValidateImpl/Exception: ", e);
        }
    }

    public String validateSendConfig(Account account) {
        setupLoginName(account);
        String errorMsg = null;
        try {
            if (beanFactory.containsBean(account.getSendProtocolType())) {
                Validate validate = (Validate) beanFactory.getBean(account.getSendProtocolType());
                validate.validate(account);
            } else {
                if (!"localsmtp".equals(account.getSendProtocolType())) {
                    errorMsg = UNEXPECTED_ERROR;
                }
            }
        } catch (Exception e) {
            errorMsg = e.getMessage();
            if (errorMsg == null) {
                errorMsg = UNEXPECTED_ERROR;
            }
            log.error("validateSendConfig/MailValidateImpl/Exception: [" + account.getName() + "]", e);
        }
        if (errorMsg != null && !account.isGmailHosted()) {
            if (account.getLoginName().equals(account.getName())) {
                String nameWithDomainNameStripped = UtilsTools.stripDomainName(account.getName());
                if (!account.getName().equals(nameWithDomainNameStripped)) {
                    account.setLoginName(nameWithDomainNameStripped);
                    errorMsg = validateSendConfig(account);
                    if (!StringUtils.isEmpty(errorMsg)) {
                        account.setLoginName(account.getName());
                    }
                }
            }
        }
        return errorMsg;
    }

    public String validateReceiveConfig(Account account) {
        if (log.isTraceEnabled())
            log.trace(String.format("validateReceiveConfig(account=%s)", String.valueOf(account)));

        setupLoginName(account);
        String errorMsg = null;
        try {
            log.info("validating receive configuration for " + account + " protocolType=" + account.getReceiveProtocolType());
            if (beanFactory.containsBean(account.getReceiveProtocolType())) {
                log.trace("there is a bean defined for " + account.getReceiveProtocolType());
                Validate validate = (Validate) beanFactory.getBean(account.getReceiveProtocolType());
                validate.validate(account);
            } else {
                log.trace("there is no bean defined for " + account.getReceiveProtocolType());
                errorMsg = UNEXPECTED_ERROR;
            }
        }
        catch (Throwable e) {
            log.error(String.format("Unexpected error while validating account: name=%s, login=%s, receiveHost=%s, receivePort=%s, receiveProtocolType=%s, receiveTS=%s", account.getName(), account.getLoginName(), account.getReceiveHost(), account.getReceivePort(), account.getReceiveProtocolType(), account.getReceiveTs()), e);

            errorMsg = e.getMessage();
            if (errorMsg == null) {
                errorMsg = UNEXPECTED_ERROR;
            }


        }
        if (errorMsg != null && !account.isGmailHosted()) {
            if (account.getLoginName().equals(account.getName())) {
                String nameWithDomainNameStripped = UtilsTools.stripDomainName(account.getName());
                if (!account.getName().equals(nameWithDomainNameStripped)) {
                    account.setLoginName(nameWithDomainNameStripped);
                    errorMsg = validateReceiveConfig(account);
                    if (!StringUtils.isEmpty(errorMsg)) {
                        account.setLoginName(account.getName());
                    }
                }
            }
        }
        log.info("[validate receiving end] [" + errorMsg + "]");
        return errorMsg;
    }

    /**
     * @param account
     * @return String
     * @see Validate Mailbox Config
     */
    public String validateMailboxConfig(Server server, Account account) {
        String errorMsg;

        if (!StringUtils.isEmpty(server.getReceiveProtocolType())) {
            UtilsTools.mapReceiveServerDetails(account, server);
            errorMsg = validateReceiveConfig(account);
        } else if (!StringUtils.isEmpty(server.getSendProtocolType())) {
            UtilsTools.mapSendServerDetails(account, server);
            errorMsg = validateSendConfig(account);
        } else {
            errorMsg = UNEXPECTED_ERROR;
        }

        return errorMsg;
    }

    //First we try guessing

    public String surmiseMailboxConfig(Account account, Country country) {
        if (log.isTraceEnabled())
            log.trace(String.format("surmiseMailboxConfig(account=%s, country=%s)", String.valueOf(account), String.valueOf(country)));

        log.info("[Begin guessing domain settings] [" + account.getName() + "] [" + account.getServer_id() + "] ");
        String guessTrack = "";
        String advancedAccountDetailsEnteredByUser = "";
        List<String> protocols = null;
        String errorMsg = null;
        //TODO - Paul - checks happens once before this ... eliminate need to check twice

        boolean bHasAdvancedConfigDetails = account.hasAdvancedConfigDetails();
        if (log.isTraceEnabled())
            log.trace("bHasAdvancedConfigDetails=" + (bHasAdvancedConfigDetails ? "true" : "false"));

        if (!bHasAdvancedConfigDetails) {
            String suffixServerName = UtilsTools.parseSuffixServerName(account.getName());
            if (log.isTraceEnabled())
                log.trace("suffixServerName=" + suffixServerName);

            String[] mxRecords = MXLookup.lookupMailHosts(suffixServerName);

            if (log.isTraceEnabled())
                log.trace("mxRecords=" + org.apache.commons.lang.StringUtils.join(mxRecords, ","));

            boolean isMxRecordsEmpty = UtilsTools.isEmpty(mxRecords);
            if (log.isTraceEnabled())
                log.trace("isMxRecordsEmpty=" + (isMxRecordsEmpty ? "true" : "false"));

            if (isMxRecordsEmpty) {
                guessTrack = "\n\n- MxLookup failed, now checking to see if server is an exchange server\n";
                protocols = Arrays.asList(Protocol.EXCHANGE);
            } else {
                log.info("[" + account.getName() + " ] [" + org.apache.commons.lang.StringUtils.join(mxRecords, ";") + "]");

                String domainMatchedFromMxLookup = getDomainMatches(mxRecords);
                if (log.isTraceEnabled())
                    log.trace("domainMatchedFromMxLookup=" + domainMatchedFromMxLookup);

                boolean isDomainMatchedFromMxLookupEmpty = StringUtils.isEmpty(domainMatchedFromMxLookup);
                if (log.isTraceEnabled())
                    log.trace("isDomainMatchedFromMxLookupEmpty=" + (isDomainMatchedFromMxLookupEmpty ? "true" : "false"));

                if (!isDomainMatchedFromMxLookupEmpty) {
                    errorMsg = validateAndSaveMailServer(account, domainMatchedFromMxLookup, country);
                    if (errorMsg == null) {
                        log.info("[domain guessing end] [" + account.getName() + "] [" + account.getServer_id() + "] [" + account.getSent_id() + "] [" + errorMsg + "]");
                        return errorMsg;
                    }
                } else {
                    protocols = getAllProtocolsToSurmise(account.getReceiveProtocolType());
                }
            }
        } else {
            advancedAccountDetailsEnteredByUser += guessTrack(account, null, true);
            if (log.isTraceEnabled())
                log.trace("advancedAccountDetailsEnteredByUser=" + advancedAccountDetailsEnteredByUser);
            advancedAccountDetailsEnteredByUser += guessTrack(account, null, false);
            if (log.isTraceEnabled())
                log.trace("advancedAccountDetailsEnteredByUser=" + advancedAccountDetailsEnteredByUser);
            protocols = Arrays.asList(account.getReceiveProtocolType());
            if (log.isTraceEnabled())
                log.trace("protocols=" + org.apache.commons.lang.StringUtils.join(protocols, ";"));

        }

        boolean isProtocolsEmpty = UtilsTools.isEmpty(protocols);
        if (log.isTraceEnabled())
            log.trace("isProtocolsEmpty=" + (isProtocolsEmpty ? "true" : "false"));

        if (!isProtocolsEmpty) {
            errorMsg = surmise(account, protocols, false, country);
        }

        boolean isErrorMsgEmpty = StringUtils.isEmpty(errorMsg);
        if (log.isTraceEnabled())
            log.trace("isErrorMsgEmpty=" + (isErrorMsgEmpty ? "true" : "false"));

        if (!isErrorMsgEmpty) {
            guessTrack += errorMsg;
            return emailErrorMesg(account, country, guessTrack, advancedAccountDetailsEnteredByUser);
        }
        return null;
    }

    public Server getLocalSendServer() {
        try {
            return emailServerService.getSentServerConfig("localsmtp.com");
        } catch (DALException e) {
            log.error(e);
            return null;
        }

    }

    //private methods

    /**
     * @param host
     * @return String
     */
    private String hostConfigFormat(String host) {
        host = host.replaceAll("\\.+", ".");
        if (host.startsWith(".")) {
            host = host.substring(1, host.length());
        }
        if (host.endsWith(".")) {
            host = host.substring(0, host.length() - 1);
        }
        return host;
    }

    /**
     * @param inStr
     * @return String[]
     */
    private String[] configFormat(String inStr) {
        String[] tempStr = null;
        if (inStr != null && !"".equals(inStr.trim())) {
            inStr = inStr.replaceAll("\\s+", ",");
            inStr = inStr.replaceAll(";+", ",");
            inStr = inStr.replaceAll(",+", ",");
            if (inStr.startsWith(",")) {
                inStr = inStr.substring(1, inStr.length());
            }
            if (inStr.endsWith(",")) {
                inStr = inStr.substring(0, inStr.length() - 1);
            }
            tempStr = inStr.split(",+");
        }
        return tempStr;
    }

    private String emailErrorMesg(Account account, Country country, String errorMsg, String advancedAccountDetailsEnteredByUser) {
        if (log.isTraceEnabled())
            log.trace(String.format("emailErrorMesg(account=%s, country=%s, errorMsg=%s, advancedAccountDetailsEnteredByUser=%s)", String.valueOf(account),
                    String.valueOf(country), String.valueOf(errorMsg), String.valueOf(advancedAccountDetailsEnteredByUser)));

        String from = sysConfigManager.getValue("validator.guess.failure.delivery.from", "care@getpeek.com", country);
        String to = account.getName();
        if (country == Country.Xobni) {
            to = SysConfigManager.instance().getValue("xobni.auth.fail.to", "webdev-alerts@xobni.com");
        }
        String cc = sysConfigManager.getValue("validator.guess.failure.delivery.cc", "care@getpeek.com", country);
        String bcc = sysConfigManager.getValue("validator.guess.failure.delivery.bcc", country);
        String message = getEmailMessage(account, errorMsg, advancedAccountDetailsEnteredByUser);
        String subject = "Adding an account to your Peek";
        mailer.sendMail(from, to, cc, bcc, message, subject);
        userService.saveEmailGuess(account.getName());
        errorMsg = String.format(UNABLE_TO_CONFIGURE_MESSAGE, account.getName());
        log.info("[end] [" + account.getName() + "] [" + account.getServer_id() + "] [" + account.getSent_id() + "] [" + errorMsg + "]");
        return errorMsg;
    }

    private List<String> getAllProtocolsToSurmise(String accountProtocolType) {
        if (log.isTraceEnabled())
            log.trace(String.format("getAllProtocolsToSurmise(accountProtocolType=%s)", accountProtocolType));

        if (!StringUtils.isEmpty(accountProtocolType)) {
            return Arrays.asList(accountProtocolType);
        }
        return Arrays.asList(Protocol.POP3, Protocol.IMAP, Protocol.EXCHANGE);
    }

    String getEmailMessage(Account account, String errorMsg, String advancedAccountDetailsEnteredByUser) {
        return "Hi there, \n\n" + String.format("We see you have been trying to add %s to your Peek, but not having success. This message gives you some information that could help you get it going.\n\n", account.getName())
                + String.format("When you try adding an email account, the Peek servers use many of the most common settings for server names to get it working. In this attempt at %s, our servers were not successful. You can register your Peek in Advanced mode by clicking in the wheel on the Add Account screen, or you can just send us the settings if your Peek does not support this feature. The settings we need are sending and receiving server names and ports.\n\n", new Date())
                + "For your reference,\n\n" + "Here is what you entered:" + advancedAccountDetailsEnteredByUser + "\n\n\nand here is what our system tried:" + errorMsg + "\n\nHappy Peeking!\nThe team at Peek";
    }

    private String validateReceiveAndSendConfigs(Account account, List<String> protocols, boolean validatingDomain) {
        if (log.isTraceEnabled())
            log.trace(String.format("validateReceiveAndSendConfigs(account=%s, protocols=%s, validatingDomain=%s)", String.valueOf(account),
                    org.apache.commons.lang.StringUtils.join(protocols, ","), validatingDomain));

        String guessTrack = "";
        if (null != protocols) {
            for (String protocol : protocols) {
                String protocolGuessFailure = validateReceiveConfigs(account, protocol, validatingDomain);
                if (StringUtils.isEmpty(protocolGuessFailure)) {
                    guessTrack = "";
                    break;
                }
                guessTrack += protocolGuessFailure;
            }
        }
        if (StringUtils.isEmpty(guessTrack)) {
            if (account.isExchange()) {
                UtilsTools.mapReceieveDtailsToSend(account);
            } else {
                return validateSendConfigs(account, validatingDomain);
            }
        }
        return guessTrack;
    }

    private String validateReceiveConfigs(Account account, String protocol, boolean validatingDomain) {
        if (log.isTraceEnabled())
            log.trace(String.format("validateReceiveConfigs(account=%s, protocols=%s, validatingDomain=%s)", String.valueOf(account),
                    protocol, validatingDomain));

        Validate validate = (Validate) beanFactory.getBean(protocol);
        List<Account> list = getMailboxConfig(account, protocol);
        if (UtilsTools.isEmpty(list)) {
            log.info("Unable to find any servers to validate against " + account.getName());
            return "Unable to find any servers to validate against";
        }
        String guessTrack = "";
        boolean succ = false;
        String loginName = account.getLoginName();
        List<Boolean> loginCombinations = Arrays.asList(false, true);
        if (!StringUtils.isEmpty(loginName) && !loginName.equals(account.getName())) {
            loginCombinations = Arrays.asList(false);
        }

        for (boolean stripLoginName : loginCombinations) {
            for (Account acco : list) {
                try {
                    if (validatingDomain) {
                        acco.setLoginName("txtbl");
                        acco.setPassword("txtbl");
                    }
                    if (stripLoginName) {
                        acco.setLoginName(UtilsTools.stripDomainName(acco.getName()));
                    }
                    validate.validate(acco);
                    UtilsTools.mapReceiveDetails(account, acco);
                    guessTrack = null;
                    succ = true;
                    break;
                } catch (AuthenticationFailedException e) {
                    if (validatingDomain) {
                        UtilsTools.mapReceiveDetails(account, acco);
                        guessTrack = null;
                        break;
                    }
                    guessTrack += guessTrack(acco, e.getMessage(), true);
                    log.warn("surmiseMailboxConfig/MailValidateImpl/Exception: [" + account.getName() + "]", e);
                } catch (Exception e) {
                    guessTrack += guessTrack(acco, e.getMessage(), true);
                    log.warn("surmiseMailboxConfig/MailValidateImpl/Exception: [" + account.getName() + "]", e);
                }
            }
            if (validatingDomain) {
                //no need to run iteration to strip login name
                break;
            }
            if (succ) {
                break;
            }
        }
        return guessTrack;
    }

    private String validateSendConfigs(Account account, boolean validatingDomain) {
        String protocol = Protocol.SMTP;
        Validate validate = (Validate) beanFactory.getBean(protocol);
        List<Account> list = sendConfigs(account);
        String guessTrack = "";
        if (UtilsTools.isEmpty(list)) {
            log.info("Unable to find any servers to validate against " + account.getName());
            guessTrack = "Unable to find any servers to validate against";
        } else {
            for (Account acco : list) {
                try {
                    if (validatingDomain) {
                        acco.setLoginName("txtbl");
                        acco.setPassword("txtbl");
                    }
                    validate.validate(acco);
                    UtilsTools.mapSendDetails(account, acco);
                    guessTrack = null;
                    break;
                } catch (AuthenticationFailedException e) {
                    if (validatingDomain) {
                        UtilsTools.mapSendDetails(account, acco);
                        break;
                    }
                    guessTrack = guessTrack(acco, e.getMessage(), false);
                    log.warn("surmiseMailboxConfig/MailValidateImpl/Exception: [" + account.getName() + "]", e);
                } catch (Exception e) {
                    guessTrack = guessTrack(acco, e.getMessage(), false);
                    log.warn("surmiseMailboxConfig/MailValidateImpl/Exception: [" + account.getName() + "]", e);
                }
            }
        }
        if (!StringUtils.isEmpty(guessTrack) && !validatingDomain) {
            try {
                Server localSendServer = getLocalSendServer();
                UtilsTools.mapSendServerDetails(account, localSendServer);
                validate = (Validate) beanFactory.getBean(protocol);
                validate.validate(account);
                guessTrack = null;
            } catch (Exception e) {
                guessTrack = guessTrack(account, e.getMessage(), false);
            }
        }
        return guessTrack;
    }

    private String validateAndSaveMailServer(Account account, String domain, Country country) {
        if (log.isTraceEnabled())
            log.trace(String.format("validateAndSaveMailServer(account=%s, domain=%s, country=%s)",
                    String.valueOf(account), domain, String.valueOf(country)));

        String error;
        try {
            List<Server> serverList = MailServersPool.getInstance().getReceiveServerList(domain, country);
            if (log.isTraceEnabled())
                log.trace("serverList=" + String.valueOf(serverList));

            Server serverR = null;
            if (!UtilsTools.isEmpty(serverList)) {
                serverR = serverList.get(0);
            }
            Server serverS = null;
            boolean isXobni = (country == Country.Xobni);
            if (!isXobni) {
                serverS = emailServerService.getSentServerConfig(domain);
            }

            if (serverR == null || (!isXobni && (serverS == null))) {
                error = UNEXPECTED_ERROR;

            } else {
                String accountDomain = UtilsTools.parseSuffixServerName(account.getName());
                Server server = createServer(serverR, serverS, accountDomain);

                UtilsTools.mapServerDetails(account, server);
                error = validateReceiveConfig(account);
                if (error == null) {
                    if (!isXobni) {
                        error = validateSendConfig(account);
                    }
                } else {
                    error += guessTrack(account, error, true);
                }
                if (error == null) {
                    error = createServersIfNecessary(account, country);
                } else {
                    error += guessTrack(account, error, false);
                }

            }
        } catch (Exception e) {
            error = e.getMessage();
            if (error == null) {
                error = UNEXPECTED_ERROR;
            }
            log.error("validateAndSaveMailServer/MailValidateImpl/Exception: " + "[" + account.getName() + "]", e);
        }
        return error;
    }

    private void setupLoginName(Account account) {
        if (log.isDebugEnabled())
            log.debug("setupLoginName account=" + account);

        if (StringUtils.isEmpty(account.getLoginName())) {
            account.setLoginName(account.getName());
        }
    }

    private void setServerDefaults(Server server) {
        server.setOrderFlag("0");
        server.setStatus("1");
        server.setLevel("6");
        server.setSentLevel("6");
    }

    private String getReceiveServerErrorMsg(Server server) {
        String errorMsg = String.format("receive host: %s receive port: %s receive protocol type: %s receive security: %s", server.getReceiveHost(), server.getReceivePort(), server.getReceiveProtocolType(), getSecurityDisplayString(server.getReceiveTs()));
        if (Protocol.isExchange(server.getReceiveProtocolType())) {
            errorMsg += getExchangeSpecificErrorMsg(server.getReceiveHostFbaPath(), server.getReceiveHostPrefix());
        }
        return errorMsg;
    }

    private String getExchangeSpecificErrorMsg(String fbaPath, String prefix) {
        return String.format(" fba path: %s exchange prefix: %s", fbaPath, prefix);
    }

    private List<String> getArray(String string) {
        ArrayList<String> strings = new ArrayList<String>();
        strings.add(string);
        return strings;
    }

    private List<Account> receiveConfigs(Account account, String protocol, String prefixKey, Map<String, List<String>> defaultPorts) {
        List<Account> list = new ArrayList<Account>();
        String mailboxSuffix = UtilsTools.parseSuffixServerName(account.getName());
        List<String> receiveHosts = getHosts(account.getReceiveHost(), mailboxSuffix, prefixKey);
        setPorts(account.getReceivePort(), account.getReceiveTs(), defaultPorts);
        for (String receiveHost : receiveHosts) {
            for (String security : defaultPorts.keySet()) {
                for (String receivePort : defaultPorts.get(security)) {
                    list.add(createAccountWithReceiveConfig(account, "1", receivePort, protocol, security, receiveHost));
                }
            }
        }

        return list;
    }

    private void setPorts(String port, String security, Map<String, List<String>> defaultPorts) {
        boolean standardPort = false;
        String securityOptionForStandardPort = null;
        if (!StringUtils.isEmpty(port)) {
            for (String key : defaultPorts.keySet()) {
                List<String> ports = defaultPorts.get(key);
                if (ports.contains(port)) {
                    standardPort = true;
                    securityOptionForStandardPort = key;
                    if (key.equals(security)) {
                        break;
                    }
                }
            }
            if (!standardPort) {
                for (String key : defaultPorts.keySet()) {
                    List<String> ports = defaultPorts.get(key);
                    if (!ports.contains(port)) {
                        ports.add(0, port);
                    }
                }
            }
        }
        List<String> securityOptions = new ArrayList<String>();
        if (standardPort) {
            securityOptions.add(securityOptionForStandardPort);
            if (!(securityOptionForStandardPort.equals("") && StringUtils.isEmpty(security)) && !securityOptionForStandardPort.equals(security)) {
                securityOptions.add(security);
            }
        } else {
            if (!StringUtils.isEmpty(security)) {
                securityOptions.add(security);
            } else {
                securityOptions.addAll(defaultPorts.keySet());
                securityOptions.remove("tls");
            }
        }
        for (String key : new ArrayList<String>(defaultPorts.keySet())) {
            if (!securityOptions.contains(key)) {
                defaultPorts.remove(key);
            }
        }
    }

    private List<String> getHosts(String host, String mailboxSuffix, String prefixKey) {
        List<String> hosts = new ArrayList<String>();
        String[] prefixes = configFormat(sysConfigManager.getValue(prefixKey));
        for (String prefix : prefixes) {
            hosts.add(hostConfigFormat(prefix + "." + mailboxSuffix.trim()));
        }
        if (!StringUtils.isEmpty(host)) {
            if (!hosts.contains(host)) {
                hosts.add(0, host);
            } else {
                return Arrays.asList(host);
            }
        }
        return hosts;
    }

    private void addExchangeMailBoxConfig(Account account, String receiveHost) {
        buildExchangeServerList(account, receiveHost);
    }

    private Account createAccountWithReceiveConfig(Account account, String status, String port, String protocol, String receiveTs,
                                                   String receiveHost) {
        String name = account.getName();
        String loginName = account.getLoginName();
        String password = account.getPassword();

        Account acco = new Account();
        acco.setName(name);
        acco.setPassword(password);
        setLoginName(name, loginName, acco);

        acco.setStatus(status);
        acco.setReceiveTs(receiveTs);
        acco.setReceivePort(port);
        acco.setReceiveProtocolType(protocol);
        acco.setReceiveHost(receiveHost);
        return acco;
    }

    private Account createAccountWithSendConfig(Account account, String status, String needAuth, String port, String protocol,
                                                String sendTs, String sendHost) {
        String name = account.getName();
        String loginName = account.getLoginName();
        String password = account.getPassword();

        Account acco = new Account();
        acco.setName(name);
        acco.setPassword(password);
        setLoginName(name, loginName, acco);

        acco.setStatus(status);
        acco.setNeedAuth(needAuth);
        acco.setSendPort(port);
        acco.setSendProtocolType(protocol);
        acco.setSendTs(sendTs);
        acco.setSendHost(sendHost);
        return acco;
    }

    private void setLoginName(String name, String loginName, Account acco) {
        acco.setLoginName(com.archermind.txtbl.utils.StringUtils.isEmpty(loginName) ? name : loginName);
    }

    private String getSendServerErrorMsg(Server server) {
        String errorMsg = String.format("send host: %s send port: %s send protocol type: %s send ts: %s need auth: %s", server.getSendHost(), server.getSendPort(), server.getSendProtocolType(), getSecurityDisplayString(server.getSendTs()), getNeedAuthDisplyString(server.getNeedAuth()));
        if (Protocol.isExchange(server.getSendProtocolType())) {
            errorMsg += getExchangeSpecificErrorMsg(server.getSendHostFbaPath(), server.getSendHostPrefix());
        }
        return errorMsg;

    }

    private List<Server> buildExchangeServerList(Account account, String receiveHost) {
        String name = account.getName();
        String domain = name.substring(name.indexOf("@") + 1);
        List<Server> servers = new ArrayList<Server>();
        servers.add(buildActiveSyncDomain(domain, receiveHost));
        servers.add(buildWebDavDomain(domain, receiveHost));
        return servers;
    }

    private Server buildWebDavDomain(String domainName, String webmailURL) {
        com.webalgorithm.exchange.utility.dto.Domain domain = new DomainUtility().completeDetails(getWebMailURL(webmailURL));
        if (domain == null) {
            return null;
        }
        //TODO: remove hard coding
        String port = domain.isSsl() ? "443" : "80";
        String security = domain.isSsl() ? "ssl" : "";
        String host = domain.getHost();

        //double check to make sure the prefix isn't in the host
        if (domain.getHost().contains("/" + domain.getPrefix())) {
            host = domain.getHost().substring(0, host.indexOf("/" + domain.getPrefix()));
        }

        return UtilsTools.createServer(domainName, host, port, security, Protocol.EXCHANGE, domain.getFbaPath(), domain.getPrefix());
    }

    private Server buildActiveSyncDomain(String domainName, String webmailURL) {

        String port = isSsl(webmailURL) ? "443" : "80";
        String security = isSsl(webmailURL) ? "ssl" : "";
        String host = getWebMailURL(webmailURL);

        //Add the ActiveSync prefix if it wasn't included in the URL
        if (!(host.contains("/" + ACTIVE_SYNC_PREFIX))) {
            host += "/" + ACTIVE_SYNC_PREFIX;
        }

        return UtilsTools.createServer(domainName, host, port, security, Protocol.ACTIVE_SYNC, null, null);
    }

    private boolean isSsl(String webmailURL) {
        return webmailURL.startsWith("https");
    }

    private Server createServer(Server receiveServer, Server sendServer, String domain) {
        if (log.isTraceEnabled())
            log.trace(String.format("createServer(receiveServer=%s, sendServer=%s, domain=%s)", String.valueOf(receiveServer)
                    , String.valueOf(sendServer), domain));

        Server server = new Server();

        //fill in receive properites
        if (receiveServer != null) {
            UtilsTools.mapReceiveServerDetails(server, receiveServer);
        }
        //fill in send properites
        if (sendServer != null) {
            UtilsTools.mapSendServerDetails(server, sendServer);
        }
        server.setName(domain);
        setServerDefaults(server);
        return server;
    }

    public String createServersIfNecessary(Account account, Country country) {
        if (log.isTraceEnabled())
            log.trace(String.format("createServersIfNecessary(account=%s, country=%s)", String.valueOf(account)
                    , String.valueOf(country)));

        String receiveProtocolType = account.getReceiveProtocolType();
        if (log.isTraceEnabled())
            log.trace("receiveProtocolType:" + receiveProtocolType);

        if ((country == Country.Xobni) && !Protocol.isXobni(receiveProtocolType)) {
            String xobniReceiveProtocolType = Protocol.getXobniProtocol(account.getReceiveProtocolType());
            if (StringUtils.isEmpty(xobniReceiveProtocolType)) {
                return "Invalid Xobni type";
            }
            account.setReceiveProtocolType(xobniReceiveProtocolType);
            account.setServer_id(0);
        }

        boolean validReceiveServerUsed = account.getServer_id() > 0;
        if (log.isTraceEnabled())
            log.trace("validReceiveServerUsed:" + (validReceiveServerUsed ? "true" : "false"));

        boolean validSendServerUsed = account.getSent_id() > 0;
        if (log.isTraceEnabled())
            log.trace("validSendServerUsed:" + (validSendServerUsed ? "true" : "false"));

        if (validReceiveServerUsed && validReceiveServerUsed) {
            return null;
        }

        String errorMsg = null;

        Server server = new Server();

        if (!validReceiveServerUsed) {
            UtilsTools.mapReceiveServerDetails(server, account);
        }
        //If localsmtp server is used not to valdiate
        if (!validSendServerUsed) {
            UtilsTools.mapSendServerDetails(server, account);
        }

        setServerDefaults(server);

        try {
            if (new TxtblWebService().addPop3Server(server) == 0) {
                errorMsg = constructConfigSaveErrorMessage(server);
            } else {
                if (!validReceiveServerUsed) {
                    account.setServer_id(server.getId());
                }
                if (!validSendServerUsed) {
                    account.setSent_id(server.getSent_id());
                }
            }
        } catch (Exception e) {
            errorMsg = e.getMessage();
            if (errorMsg == null) {
                errorMsg = UNEXPECTED_ERROR;
            }
            log.warn("surmiseDomainConfig/MailValidateImpl/Exception: [" + account.getName() + "]", e);
        }
        return errorMsg;
    }

    //visible due to tests

    String getWebMailURL(String webmailURL) {

        String tempURL;
        if (webmailURL.startsWith("http://")) {
            tempURL = webmailURL.substring("http://".length());
        } else if (webmailURL.startsWith("https://")) {
            tempURL = webmailURL.substring("https://".length());
        } else {
            tempURL = webmailURL;
        }

        //Add the ActiveSync prefix if it wasn't included in the URL
        if ((tempURL.contains("/" + ACTIVE_SYNC_PREFIX))) {
            tempURL = tempURL.substring(0, tempURL.indexOf("/" + ACTIVE_SYNC_PREFIX));
        } else if (tempURL.contains("/owa")) {
            tempURL = tempURL.substring(0, tempURL.indexOf("/owa"));
        } else if (tempURL.contains("/exchange")) {
            tempURL = tempURL.substring(0, tempURL.indexOf("/exchange"));
        }


        return tempURL;

    }

    String constructConfigSaveErrorMessage(Server server) {
        return CONFIG_SAVE_ERROR + "\n"
                + getReceiveServerErrorMsg(server) + "\n"
                + getSendServerErrorMsg(server);
    }

    String getNeedAuthDisplyString(String needAuth) {
        return "1".equals(needAuth) ? "yes" : "no";
    }

    String getSecurityDisplayString(String security) {
        if (StringUtils.isEmpty(security)) {
            return "none";
        }
        return security;
    }

    String guessTrack(Account account, String errorMsg, boolean receiveServer) {
        if (log.isTraceEnabled())
            log.trace(String.format("gueesTrack(account=%s, errorMsg=%s, receiveServer=%s)",
                    String.valueOf(account), errorMsg, (receiveServer ? "true" : "false")));
        String guessTrack = String.format("email: %s login name: %s ", account.getName(), account.getLoginName());
        Server server = new Server();
        UtilsTools.mapServerDetails(server, account);
        if (receiveServer) {
            guessTrack += getReceiveServerErrorMsg(server);
        } else {
            guessTrack += getSendServerErrorMsg(server);
        }
        if (!StringUtils.isEmpty(errorMsg)) {
            guessTrack += String.format(" failed due to: %s", errorMsg);
        }
        log.debug(String.format("%s, password: %s", guessTrack, account.getPassword()));
        guessTrack = "\n\n" + guessTrack;
        return guessTrack;
    }

    List<Account> sendConfigs(Account account) {
        List<Account> list = new ArrayList<Account>();
        //TODO: Paul - temporary fix - to reduce timeouts on client side while guessing - for now if host is provided then it will use it or will fall back on the local
        if (!StringUtils.isEmpty(account.getSendHost())) {
//            String mailboxSuffix = UtilsTools.parseSuffixServerName(account.getName());
//            String prefixKey = "validator.guess.smtp.prefix.match";
            HashMap<String, List<String>> defaultPorts = new HashMap<String, List<String>>();
            defaultPorts.put("", getArray("25"));
            defaultPorts.put("ssl", getArray("465"));
            defaultPorts.put("tls", getArray("465"));

//            List<String> sendHosts = getHosts(account.getSendHost(), mailboxSuffix, prefixKey);
            List<String> sendHosts = Arrays.asList(account.getSendHost().trim());
            setPorts(account.getSendPort(), account.getSendTs(), defaultPorts);
            for (String sendHost : sendHosts) {
                for (String security : defaultPorts.keySet()) {
                    for (String sendPort : defaultPorts.get(security)) {
                        list.add(createAccountWithSendConfig(account, "1", "1", sendPort, Protocol.SMTP, security, sendHost));
                    }
                }
            }
        }

        return list;
    }

    /**
     * @return List<Account>
     */
    List<Account> getMailboxConfig(Account account, String protocol) {
        if (log.isTraceEnabled())
            log.trace(String.format("getMailboxConfig(account=%s, protocols=%s)", String.valueOf(account),
                    protocol));

        List<Account> list = new ArrayList<Account>();
        String mailboxSuffix = UtilsTools.parseSuffixServerName(account.getName());
        if (Protocol.POP3.equals(protocol)) {
            HashMap<String, List<String>> defaultPorts = new HashMap<String, List<String>>();
            defaultPorts.put("", getArray("110"));
            defaultPorts.put("ssl", getArray("995"));
            defaultPorts.put("tls", getArray("995"));
            return receiveConfigs(account, protocol, "validator.guess.pop3.prefix.match", defaultPorts);
        } else if (Protocol.isExchange(protocol)) {
            List<String> receiveHosts = getHosts(account.getReceiveHost(), mailboxSuffix, "validator.guess.exchange.prefix.match");
            for (String receiveHost : receiveHosts) {
                addExchangeMailBoxConfig(account, receiveHost);
            }
        } else {
            HashMap<String, List<String>> defaultPorts = new HashMap<String, List<String>>();
            defaultPorts.put("", getArray("143"));
            defaultPorts.put("ssl", getArray("993"));
            defaultPorts.put("tls", getArray("993"));
            return receiveConfigs(account, protocol, "validator.guess.imap.prefix.match", defaultPorts);
        }
        return list;
    }


    String getDomainMatches(String[] mxRecords) {
        if (log.isTraceEnabled())
            log.trace(String.format("getDomainMatches(mxRecords=%s)", org.apache.commons.lang.StringUtils.join(mxRecords, ";")));
        //yahoo.com=yahoo,rockmail,ymail;gmail.com=google,gmail,getpeek;hotmail.com=hotmail
        //googlemailextc
        String mxMatchString = sysConfigManager.getValue("validator.guess.mx.match");
        if (log.isTraceEnabled())
            log.trace("mxMatchString=" + mxMatchString);

        HashMap<String, String> mxMatches = new HashMap<String, String>();
        for (String mxMatchGroup : mxMatchString.split(";+")) {
            String[] groupParts = mxMatchGroup.split("=");
            if (groupParts.length == 2) {
                String domain = groupParts[0];
                for (String mxMatach : groupParts[1].split(",")) {
                    mxMatches.put(mxMatach.toLowerCase(), domain);
                }
            }
        }
        for (String mxRecord : mxRecords) {
            for (String mxMatch : mxMatches.keySet()) {
                if (mxRecord.toLowerCase().contains(mxMatch)) {
                    return mxMatches.get(mxMatch);
                }
            }
        }
        return null;
    }

    String surmise(Account account, List<String> protocols, boolean validatingDomain, Country country) {
        if (log.isTraceEnabled())
            log.trace(String.format("surmise(account=%s, protocols=%s, validatingDomain=%s, country=%s)",
                    String.valueOf(account), org.apache.commons.lang.StringUtils.join(protocols, ","),
                    (validatingDomain ? "true" : "false"), String.valueOf(country)));

        String errorMsg = validateReceiveAndSendConfigs(account, protocols, validatingDomain);
        if (StringUtils.isEmpty(errorMsg)) {
            errorMsg = createServersIfNecessary(account, country);
        }
        return errorMsg;
    }

}
