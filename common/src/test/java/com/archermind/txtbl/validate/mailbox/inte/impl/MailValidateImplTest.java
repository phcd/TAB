package com.archermind.txtbl.validate.mailbox.inte.impl;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.dal.business.IEmailServerService;
import com.archermind.txtbl.dal.business.IUserService;
import com.archermind.txtbl.domain.*;
import com.archermind.txtbl.send.mail.bean.AuthUser;
import com.archermind.txtbl.utils.IMailer;
import com.archermind.txtbl.utils.ISysConfigManager;
import com.archermind.txtbl.utils.UtilsTools;
import org.junit.Assert;
import org.junit.Test;
import twitter4j.http.AccessToken;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.security.Security;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;


public class MailValidateImplTest
{
    @Test
    public void getDomainMatches(){
        MailValidateImpl mailValidate = new MailValidateImpl(new SysConfigManager(), null, null, null);

        Assert.assertEquals("gmail.com", mailValidate.getDomainMatches(new String[]{"googleaxmx"}));
        Assert.assertEquals("gmail.com", mailValidate.getDomainMatches(new String[]{"GOOGLEAXMX"}));
        Assert.assertEquals("yahoo.com", mailValidate.getDomainMatches(new String[]{"rockmail"}));
        Assert.assertEquals("yahoo.com", mailValidate.getDomainMatches(new String[]{"yahoox"}));
        Assert.assertEquals("bizmail.yahoo.com", mailValidate.getDomainMatches(new String[]{"biz.mail.yahoo.com"}));
        Assert.assertEquals("hotmail.com", mailValidate.getDomainMatches(new String[]{"hotmail"}));
        Assert.assertNull(mailValidate.getDomainMatches(new String[]{"rocketmail"}));
        Assert.assertNull(mailValidate.getDomainMatches(new String[]{"hot"}));
    }

    @Test
    public void constructConfigSaveErrorMessage(){
        MailValidateImpl mailValidate = new MailValidateImpl(new SysConfigManager(), null, null, null);
        Assert.assertEquals("Whoops! We have encountered an error saving your account config. Please email feedback@getpeek.com for support.\nreceive host: receive host receive port: 123 receive protocol type: pop3 receive security: ssl\nsend host: send host send port: 111 send protocol type: smtp send ts: none need auth: yes", mailValidate.constructConfigSaveErrorMessage(getPopAndSMTPServer()));
        Assert.assertEquals("Whoops! We have encountered an error saving your account config. Please email feedback@getpeek.com for support.\nreceive host: receive host receive port: 123 receive protocol type: newexchange receive security: ssl fba path: fba_path exchange prefix: exchange\nsend host: send host send port: 111 send protocol type: smtp send ts: none need auth: yes", mailValidate.constructConfigSaveErrorMessage(getExchangeAndSMTPServer()));
    }

    @Test
    public void guessTrack() {
        MailValidateImpl mailValidate = new MailValidateImpl(null, null, null, null);
        Server server = getPopAndSMTPServer();
        Account account = new Account();
        account.setName("email");
        account.setLoginName("login_name");
        UtilsTools.mapServerDetails(account, server);
        
        Assert.assertEquals("\n\nemail: email login name: login_name receive host: receive host receive port: 123 receive protocol type: pop3 receive security: ssl failed due to: error message", mailValidate.guessTrack(account, "error message", true));
        Assert.assertEquals("\n\nemail: email login name: login_name send host: send host send port: 111 send protocol type: smtp send ts: none need auth: yes failed due to: error message", mailValidate.guessTrack(account, "error message", false));
    }

    @Test
    public void getSecurityDisplayString() {
        MailValidateImpl mailValidate = new MailValidateImpl(null, null, null, null);
        Assert.assertEquals("ssl", mailValidate.getSecurityDisplayString("ssl"));
        Assert.assertEquals("none", mailValidate.getSecurityDisplayString(null));
        Assert.assertEquals("none", mailValidate.getSecurityDisplayString(""));        
    }

    @Test
    public void getNeedAuthDisplyString() {
        MailValidateImpl mailValidate = new MailValidateImpl(null, null, null,null);
        Assert.assertEquals("yes", mailValidate.getNeedAuthDisplyString("1"));
        Assert.assertEquals("no", mailValidate.getNeedAuthDisplyString("0"));
        Assert.assertEquals("no", mailValidate.getNeedAuthDisplyString(""));
        Assert.assertEquals("no", mailValidate.getNeedAuthDisplyString(null));
    }

    @Test
    public void getEmailMessage() {
        MailValidateImpl mailValidate = new MailValidateImpl(null, null, null,null);
        Account account = new Account();
        account.setName("paul@getpeek.in");
        System.out.println(mailValidate.getEmailMessage(account, "\n\nemail: amol@drwn.com login name: amol@drwn.com receive host: imap.gmail.com receive port: 0 receive protocol type: newimap receive security: ssl failed due to: Connection timed out", "\n\nemail: amol@drwn.com login name: amol@drwn.com receive host: imap.drwn.com receive port: 10 receive protocol type: newimap receive security: ssl"));
    }

//    @Test
    public void surmiseMailboxConfig() {
        MailValidateImpl mailValidate = new MailValidateImpl(new SysConfigManager(), new EmailServerService(), new Mailer(), new UserService());
        Account account = new Account();
        account.setName("amol@12daysofpeekmas.com");
        account.setPassword("mailster");
        account.setProtocol("POP");
        account.setReceiveProtocolType(Protocol.POP3);
        account.setSendProtocolType(Protocol.SMTP);
        account.setReceiveHost("mail.12daysofpeekmas.com");
        account.setSendHost("mail.12daysofpeekmas.com");
        Assert.assertNotNull(mailValidate.surmiseMailboxConfig(account, Country.INDIA));
    }

//    @Test
    public void surmiseMailboxConfigWhenPartialInfoProvided() {
        MailValidateImpl mailValidate = new MailValidateImpl(new SysConfigManager(), new EmailServerService(), new Mailer(), new UserService()){
            String surmise(Account account, List<String> protocols, boolean validatingDomain, boolean createServers) {
                Assert.assertEquals(1, protocols.size());
                Assert.assertEquals(Protocol.POP3, protocols.get(0));
                return null;
            }
        };
        Account account = new Account();
        account.setName("amol@12daysofpeekmas.com");
        account.setReceiveHost("mail.12daysofpeekmas.com");
        account.setPassword("somepassword");
        account.setProtocol("POP");
        account.setReceiveProtocolType(Protocol.POP3);
        Assert.assertNull(mailValidate.surmiseMailboxConfig(account, Country.INDIA));
    }

    @Test
    public void getMailboxConfigIncludesStandardConfigsWhenNonStandardConfigDetailsProvided(){
        MailValidateImpl mailValidate = new MailValidateImpl(new SysConfigManager(), new EmailServerService(), new Mailer(), new UserService());
        Account account = new Account();
        account.setName("jmpak80@yahoo.co.in");
        account.setLoginName("jmpak80@yahoo.co.in");
        account.setPassword("somepassword");
        account.setProtocol("POP");

        List<Account> recevieveConfigs = mailValidate.getMailboxConfig(account, Protocol.POP3);
        Assert.assertEquals(32, recevieveConfigs.size());

        account.setReceiveHost("receiveHost");
        recevieveConfigs = mailValidate.getMailboxConfig(account, Protocol.POP3);
        Assert.assertEquals(34, recevieveConfigs.size());
        Assert.assertEquals("receiveHost", recevieveConfigs.get(0).getReceiveHost());

        account.setReceiveHost("mail.yahoo.co.in");
        recevieveConfigs = mailValidate.getMailboxConfig(account, Protocol.POP3);
        Assert.assertEquals(2, recevieveConfigs.size());

        account.setReceivePort("123");
        recevieveConfigs = mailValidate.getMailboxConfig(account, Protocol.POP3);
        Assert.assertEquals(4, recevieveConfigs.size());

        account.setReceivePort("995");
        recevieveConfigs = mailValidate.getMailboxConfig(account, Protocol.POP3);
        Assert.assertEquals(2, recevieveConfigs.size());

        account.setReceivePort("995");
        account.setReceiveTs("ssl");
        recevieveConfigs = mailValidate.getMailboxConfig(account, Protocol.POP3);
        Assert.assertEquals(1, recevieveConfigs.size());

//TODO: Paul - temporary fix - to reduce timeouts on client side while guessing - for now if host is provided then it will use it or will fall back on the local
//        List<Account> sendConfigs = mailValidate.getMailboxConfig(account, Protocol.SMTP);
//        Assert.assertEquals(26, sendConfigs.size());
//
//        account.setSendHost("sendHost");
//        sendConfigs = mailValidate.getMailboxConfig(account, Protocol.SMTP);
//        Assert.assertEquals(28, sendConfigs.size());
//
//        account.setSendHost("smtp.yahoo.co.in");
//        sendConfigs = mailValidate.getMailboxConfig(account, Protocol.SMTP);
//        Assert.assertEquals(2, sendConfigs.size());
//
//        account.setSendPort("123");
//        sendConfigs = mailValidate.getMailboxConfig(account, Protocol.SMTP);
//        Assert.assertEquals(4, sendConfigs.size());
//
//        account.setSendPort("465");
//        sendConfigs = mailValidate.getMailboxConfig(account, Protocol.SMTP);
//        Assert.assertEquals(2, sendConfigs.size());
//
//        account.setSendPort("465");
//        account.setSendTs("ssl");
//        sendConfigs = mailValidate.getMailboxConfig(account, Protocol.SMTP);
//        Assert.assertEquals(1, sendConfigs.size());
//
//        account.setSendPort("0");
//        account.setSendTs("");
//        sendConfigs = mailValidate.getMailboxConfig(account, Protocol.SMTP);
//        Assert.assertEquals(4, sendConfigs.size());

    }

    @Test
    public void getWebMailURL() {
        MailValidateImpl mailValidate = new MailValidateImpl(new SysConfigManager(), new EmailServerService(), new Mailer(), new UserService());
        Assert.assertEquals("test", mailValidate.getWebMailURL("http://test/" + MailValidateImpl.ACTIVE_SYNC_PREFIX));
        Assert.assertEquals("test", mailValidate.getWebMailURL("http://test/owa"));
        Assert.assertEquals("test", mailValidate.getWebMailURL("http://test/exchange"));
        Assert.assertEquals("test", mailValidate.getWebMailURL("test"));
        Assert.assertEquals("test", mailValidate.getWebMailURL("http://test"));
    }
    
    @Test
    public void getMailboxConfigWhenPartialInfoProvided(){
        MailValidateImpl mailValidate = new MailValidateImpl(new SysConfigManager(), new EmailServerService(), new Mailer(), new UserService());
        Account account = new Account();
        account.setName("jmpak80@yahoo.co.in");
        account.setLoginName("jmpak80@yahoo.co.in");
        account.setPassword("somepassword");
        account.setProtocol("POP");
        account.setReceiveHost("mail.yahoo.co.in");

        List<Account> recevieveConfigs = mailValidate.getMailboxConfig(account, Protocol.POP3);
        Assert.assertEquals(2, recevieveConfigs.size());

        account.setReceiveTs("ssl");
        recevieveConfigs = mailValidate.getMailboxConfig(account, Protocol.POP3);
        Assert.assertEquals(1, recevieveConfigs.size());

        account.setReceiveTs("");
        account.setReceivePort("995");
        recevieveConfigs = mailValidate.getMailboxConfig(account, Protocol.POP3);
        Assert.assertEquals(2, recevieveConfigs.size());

        account.setReceiveTs("ssl");
        account.setReceivePort("995");
        recevieveConfigs = mailValidate.getMailboxConfig(account, Protocol.POP3);
        Assert.assertEquals(1, recevieveConfigs.size());
        Assert.assertEquals("995", recevieveConfigs.get(0).getReceivePort());

        account.setReceiveHost(null);
        recevieveConfigs = mailValidate.getMailboxConfig(account, Protocol.POP3);
        Assert.assertEquals(16, recevieveConfigs.size());

//TODO: Paul - temporary fix - to reduce timeouts on client side while guessing - for now if host is provided then it will use it or will fall back on the local
//        String sendProtocol = Protocol.SMTP;
//        account.setSendHost("smtp.yahoo.co.in");
//        List<Account> sendConfigs = mailValidate.getMailboxConfig(account, sendProtocol);
//        Assert.assertEquals(2, sendConfigs.size());
//
//        account.setSendTs("ssl");
//        sendConfigs = mailValidate.getMailboxConfig(account, Protocol.SMTP);
//        Assert.assertEquals(1, sendConfigs.size());
//
//        account.setSendTs("");
//        account.setSendPort("25");
//        sendConfigs = mailValidate.getMailboxConfig(account, Protocol.SMTP);
//        Assert.assertEquals(1, sendConfigs.size());
//
//        account.setSendTs("ssl");
//        account.setSendPort("465");
//        sendConfigs = mailValidate.getMailboxConfig(account, Protocol.SMTP);
//        Assert.assertEquals(1, sendConfigs.size());
//        Assert.assertEquals("465", sendConfigs.get(0).getSendPort());
//
//        account.setSendHost(null);
//        sendConfigs = mailValidate.getMailboxConfig(account, Protocol.SMTP);
//        Assert.assertEquals(13, sendConfigs.size());

        account.setSendHost(null);
        recevieveConfigs = mailValidate.sendConfigs(account);
        Assert.assertEquals(0, recevieveConfigs.size());

        account.setSendHost("sendHost");
        recevieveConfigs = mailValidate.sendConfigs(account);
        Assert.assertEquals(2, recevieveConfigs.size());

        account.setSendHost("sendHost");
        account.setSendPort("110");
        recevieveConfigs = mailValidate.sendConfigs(account);
        Assert.assertEquals(4, recevieveConfigs.size());

        account.setSendHost("sendHost");
        account.setSendPort("25");
        recevieveConfigs = mailValidate.sendConfigs(account);
        Assert.assertEquals(1, recevieveConfigs.size());

        account.setSendHost("sendHost");
        account.setSendPort("465");
        recevieveConfigs = mailValidate.sendConfigs(account);
        Assert.assertEquals(2, recevieveConfigs.size());

        account.setSendHost("sendHost");
        account.setSendPort("465");
        account.setSendTs("ssl");
        recevieveConfigs = mailValidate.sendConfigs(account);
        Assert.assertEquals(1, recevieveConfigs.size());
    }

    private Server getExchangeAndSMTPServer() {
        Server server = new Server();
        populateExchangeReceiveDetails(server);
        populateSendDetails(server);
        return server;
    }

    private void populateExchangeReceiveDetails(Server server)
    {
        populateBasicReceiveDetails(server);
        server.setReceiveProtocolType(Protocol.EXCHANGE);
        server.setReceiveHostFbaPath("fba_path");
        server.setReceiveHostPrefix("exchange");
    }

    public Server getPopAndSMTPServer() {
        Server server = new Server();
        populateRecevieDetails(server);
        populateSendDetails(server);
        return server;
    }

    private void populateSendDetails(Server server)
    {
        server.setSendHost("send host");
        server.setSendPort("111");
        server.setSendProtocolType("smtp");
        server.setNeedAuth("1");
    }

    private void populateRecevieDetails(Server server)
    {
        populateBasicReceiveDetails(server);
        server.setReceiveProtocolType("pop3");
    }

    private void populateBasicReceiveDetails(Server server)
    {
        server.setReceiveHost("receive host");
        server.setReceivePort("123");
        server.setReceiveTs("ssl");
    }
}

class EmailServerService implements IEmailServerService {
    public List<Server> getServers(String status) throws DALException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<Server> getSentServers(String status) throws DALException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<Server> getServersbyName(String name) throws DALException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<Server> getSentServersbyName(String name) throws DALException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Server getServersbyId(String id) throws DALException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int JobDeleteAllEmail(String currentDay) throws DALException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String[] getAllReceiveProtocolType() throws DALException {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Server getSentServerConfig(String sName) throws DALException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<Server> getMailServerConfig(String name) throws DALException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<String> getDomainNames(String nameCriteria, int limit) throws DALException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}

class SysConfigManager implements ISysConfigManager {
    private HashMap<String, String> configValues;

    SysConfigManager() {
        configValues = new HashMap<String, String>();
        configValues.put("validator.guess.mx.match", "bizmail.yahoo.com=biz.mail.yahoo.com;yahoo.com=yahoox,rockmail,ymail;gmail.com=google,gmail,getpeek;hotmail.com=hotmail;hh=;=;HH==");
        configValues.put("validator.guess.smtp.prefix.match", "smtp;pop;mail;mailhost;outgoing;smtp-server;smtpauth;authsmtp;smtp.mail;smtp.email;smtp.isp;plus.smtp.mail;mx");
        configValues.put("validator.guess.pop3.prefix.match", "pop;pop3;pop.3;mail;pop.mail;pop.email;pop3.mail;pop3.email;incoming;pop-server;mail-server;pop.3.isp;plus.pop.mail;postoffice;postoffice.isp;pop.business");
        configValues.put("validator.guess.exchange.prefix.match", "mex07a");
        configValues.put("validator.guess.imap.prefix.match", "imap;mail;imap.mail;imap.email;incoming;imap-server;mail-server;imap.isp;plus.imap.mail;postoffice;postoffice.isp");

        configValues.put("validator.guess.failure.delivery.from", "paul@getpeek.in");
        configValues.put("validator.guess.failure.delivery.cc", "paul@getpeek.in");
    }

    public String getValue(String key)
    {
        return configValues.get(key);
    }

    @Override
    public String getValue(String key, String defaultValue)
    {
        return getValue(key);
    }

    @Override
    public String getValue(String key, String defaultValue, Country country)
    {
        return getValue(key);
    }

    @Override
    public String getValue(String key, Country country)
    {
        return getValue(key);
    }
    
}

class Mailer implements IMailer {
    public void sendMail(String from, String to, String cc, String bcc, String message, String subject) {
        Message msg = null;
        Session session = null;
        Properties props = null;
        Transport transport = null;
        try {
            props = new Properties();

            props.setProperty("mail.transport.protocol", "smtp");
            props.setProperty("mail.smtp.host", "smtp.gmail.com");
            props.setProperty("mail.smtp.port", "465");
            if ("ssl".equals("ssl")) {
                Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
                props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.setProperty("mail.smtp.socketFactory.fallback", "false");
                props.setProperty("mail.smtp.socketFactory.port", "465");
            }
            props.put("mail.smtp.auth", "true");
            session = Session.getInstance(props, new AuthUser("paul@getpeek.in", "xxxx"));
            transport = session.getTransport();
            transport.connect();
            try {
                msg = createMsg(to, from, cc, bcc, message, subject, session);
                transport.sendMessage(msg, msg.getAllRecipients());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (transport != null && transport.isConnected()) {
                    transport.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static MimeMessage createMsg(String to, String from, String cc, String bcc, String message, String subject, Session session) throws Exception {

        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(tranFromAddr(from));
        if (to != null && !"".equals(to.trim())) {
            msg.setRecipients(Message.RecipientType.TO, UtilsTools.tranAddr(to.trim()));
        }
        if (cc != null && !"".equals(cc.trim())) {
            msg.setRecipients(Message.RecipientType.CC, UtilsTools.tranAddr(cc.trim()));
        }
        if (bcc != null && !"".equals(bcc.trim())) {
            msg.setRecipients(Message.RecipientType.BCC, UtilsTools.tranAddr(bcc.trim()));
        }
        msg.setSubject(subject);
        msg.setText(message);

        msg.saveChanges();
        return msg;
    }

    private static InternetAddress tranFromAddr(String addr) throws Exception {
        InternetAddress address = new InternetAddress();
        address.setAddress(addr.trim());
        return address;
    }

}

class UserService implements IUserService {

    @Override
    public int modifyStatus(String user_id, String status) throws DALException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int addAccount(Account account) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Account getAccount(String user_id, String name) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Account getAccount(long account_id) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Account> getAccounts(String user_id) throws DALException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int modifyAccount(Account account) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int addContacts(List<Contact> list) throws DALException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Contact> getPagedContacts(String user_id, int page_no, int page_size, String type) throws DALException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int modifyContacts(List<Contact> list) throws DALException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int removeContact(String user_id, String email, String type) throws DALException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int removeContacts(List<Contact> list) throws DALException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int addDevices(Device device) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getDeviceCode(String user_id) throws DALException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List getDeviceByIDvcIDSim(String devicecode, String simcode) throws DALException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int clearAccountExceptionMessages(String user_id) throws DALException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int updateAccountMessages(String message, String name, String sLoginStatus) throws DALException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int saveEmailGuess(String name) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int modifyChangeFlag(String id, String change_flag) throws DALException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Account> getAccountToTaskfactory(String sRvcPtlType) throws DALException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int addContactTrack(ContactTrack contactTrack) throws DALException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ContactTrack getContactTrack(String userId, String name) throws DALException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int modifyContactTrack(ContactTrack contactTrack) throws DALException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int removeContactTrack(String userId, String name) throws DALException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Account> getReceiveAccount(String sTransctionId) throws DALException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int modifyDevicePin(String userId, String sPin) throws DALException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int modifyUserFeatures(String userId, String sFeaturs) throws DALException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Account> getAllAccounts(String userId) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean resetPeekAccount(String id, List<Account> accounts) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Country getCountry(String userId) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public AccessToken fetchTwitterToken(String email) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean deleteXobniAccount(String email) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean removeUser(String userid) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean removeEmailAccount(Account account) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int registerUser_new(UserPojo pojo, XobniAccount xobniAccount) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean resetEmailAccount(Account account) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean deactivateEmailAccount(Account account) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean activateEmailAccount(Account account) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean deleteTwitterAccount(String email){
        return false;
    }

    @Override
    public XobniAccount getXobniAccountByUserID(String userid) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean updateXobniDBVersion(String accountName, String dbVersion) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getAccountsCountByProvider(String provider) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getStalledXobniAccountsCount() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Integer> getSyncDisabledXobniAccounts(Date date) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}