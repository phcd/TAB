package com.archermind.txtbl.receiver.mail.abst.impl;

import com.archermind.txtbl.authenticator.Authenticator;
import com.archermind.txtbl.authenticator.ImapAuthenticator;
import com.archermind.txtbl.dal.business.impl.UserService;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.receiver.mail.support.NewProviderSupport;
import com.archermind.txtbl.sync.IMAPSyncUtil;
import com.archermind.txtbl.utils.StopWatch;
import com.ibatis.common.jdbc.ScriptRunner;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ImapSyncProviderTests extends AbstractProviderTest {
    private static final String JNDI = "java:txtbl-master";

    private static final String DATA_SOURCE = "mysql-ds";

    private static final String PATH = "/com/archermind/txtbl/receiver/mail/xml/test-providers.xml";

    private static final String TXTBL_SYS_CONFIG = "/sql/txtbl_sys_config.sql";
    private static final String TXTBL_SYS_RECEIVED = "/sql/txtbl_sys_received.sql";
    private static final String TXTBL_USER = "/sql/txtbl_user.sql";
    private static final String TXTBL_EMAIL_RECEIVED = "/sql/txtbl_email_received.sql";
    private static final String TXTBL_EMAIL_ACCOUTN = "/sql/txtbl_email_account.sql";

    private NewProviderSupport newProviderSupport = new NewProviderSupport();
    private Authenticator authenticator = new ImapAuthenticator();
    private IMAPSyncUtil syncUtil = new IMAPSyncUtil();

    private static DataSource dataSource;

    @BeforeClass
    public static void setup() throws NamingException, IOException, SQLException {

        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(PATH);
        SimpleNamingContextBuilder builder = new SimpleNamingContextBuilder();

        dataSource = (DataSource) applicationContext.getBean(DATA_SOURCE);
        builder.bind(JNDI, dataSource);
        builder.activate();

        executeSQL(dataSource, TXTBL_USER);
        executeSQL(dataSource, TXTBL_SYS_CONFIG);
        executeSQL(dataSource, TXTBL_SYS_RECEIVED);
        executeSQL(dataSource, TXTBL_EMAIL_ACCOUTN);
        executeSQL(dataSource, TXTBL_EMAIL_RECEIVED);

    }

    private static void executeSQL(DataSource dataSource, String sqlFilename) throws SQLException, IOException {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            ScriptRunner runner = new ScriptRunner(connection, false, true);
            FileReader reader = new FileReader(new ClassPathResource(sqlFilename).getFile());

            runner.runScript(reader);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    public int buildImapStatusInt(int dirtyFlags, int flags) {
        int out = dirtyFlags << (32 - 5);
        out = out | flags;

        return out;
    }

    //This test only functions if pro.getMessages is made public

    //@Test
    public void testGetAccount() {
        Account account;// = new UserService().getAccount("txtblmatt@gmail.com");

        int newMessages;
        String context = "";
        String messageStoreBucket = "";

        StopWatch watch = new StopWatch();
        List<String> draftMessageIds = new ArrayList<String>();

        UserService us = new UserService();


        account = us.getAccount("txtblmatt@gmail.com");
        //account = us.getAccount(2000000011);

        ImapSyncProvider pro = new ImapSyncProvider(newProviderSupport, authenticator);

        pro.receiveMail(account);

        //int i = pro.receiveMail(account);

        try {

            //Figure out why you are getting back null from the authenticator  newProviderSupport
            Folder folder = authenticator.connect(account, context, watch, newProviderSupport, account.getFolderNameToConnect());
            //The following line
            //newMessages = pro.getMessages(account, folder, messageStoreBucket, folder.getMessageCount(), draftMessageIds, context, watch);
            newProviderSupport.syncEmails(account, folder, context);

        } catch (Throwable t) {
            Assert.fail();
        }

        int k = 5;

    }

    @Test
    public void testGetImapFlagsFromStatus() {
        Flags flags;

        flags = IMAPSyncUtil.getImapFlagsFromStatus(0x01);
        Assert.assertEquals(flags, new Flags(Flags.Flag.SEEN));

        flags = IMAPSyncUtil.getImapFlagsFromStatus(0x02);
        Assert.assertEquals(flags, new Flags(Flags.Flag.ANSWERED));

        flags = IMAPSyncUtil.getImapFlagsFromStatus(0x04);
        Assert.assertEquals(flags, new Flags(Flags.Flag.FLAGGED));

        flags = IMAPSyncUtil.getImapFlagsFromStatus(0x08);
        Assert.assertEquals(flags, new Flags(Flags.Flag.DELETED));

        flags = IMAPSyncUtil.getImapFlagsFromStatus(0x10);
        Assert.assertEquals(flags, new Flags(Flags.Flag.DRAFT));

        flags = IMAPSyncUtil.getImapFlagsFromStatus(0x1f);
        Flags testFlags = new Flags(Flags.Flag.SEEN);
        testFlags.add(Flags.Flag.ANSWERED);
        testFlags.add(Flags.Flag.FLAGGED);
        testFlags.add(Flags.Flag.DELETED);
        testFlags.add(Flags.Flag.DRAFT);

        Assert.assertEquals(testFlags, flags);
    }

    @Test
    public void testGetFlippedTrueBitSet() {
        int test;
        //   df     f      a      df - dirty flag bits, f - flag bits, a - correct solution bits
        int[][] testArray = {
                {0x00, 0x00, 0x00},
                {0x00, 0x01, 0x00},
                {0x01, 0x00, 0x00},
                {0x01, 0x01, 0x01},
                {0x1f, 0x1f, 0x1f},
                {0x1f, 0x01, 0x01},
                {0x15, 0x12, 0x10},
        };

        for (int i = 0; i < testArray.length; i++) {
            test = IMAPSyncUtil.getFlippedTrueBitSet(buildImapStatusInt(testArray[i][0], testArray[i][1]));
            Assert.assertEquals(test, testArray[i][2]);
        }

    }

    @Test
    public void testGetFlippedFalseBitSet() {
        int test;
        //   df     f      a      df - dirty flag bits, f - flag bits, a - correct solution bits
        int[][] testArray = {
                {0x00, 0x00, 0x00},
                {0x00, 0x01, 0x00},
                {0x01, 0x00, 0x01},
                {0x01, 0x01, 0x00},
                {0x1f, 0x1f, 0x00},
                {0x1f, 0x01, 0x1e},
                {0x15, 0x12, 0x05},
                {0x1f, 0x00, 0x1f},
        };

        for (int i = 0; i < testArray.length; i++) {
            test = IMAPSyncUtil.getFlippedFalseBitSet(buildImapStatusInt(testArray[i][0], testArray[i][1]));
            Assert.assertEquals(test, testArray[i][2]);
        }

    }

    @Test
    public void testGetFlippedToTrueFlags() {
        Flags testFlags;
        //   df     f      a      df - dirty flag bits, f - flag bits, a - correct solution bits
        int[][] testArray = {
                {0x00, 0x00, 0x00},
                {0x00, 0x01, 0x00},
                {0x01, 0x00, 0x00},
                {0x01, 0x01, 0x01},
                {0x1f, 0x1f, 0x1f},
                {0x1f, 0x01, 0x01},
                {0x15, 0x12, 0x10},
        };

        for (int i = 0; i < testArray.length; i++) {
            testFlags = IMAPSyncUtil.getFlippedToTrueFlags(buildImapStatusInt(testArray[i][0], testArray[i][1]));
            Assert.assertEquals(testFlags, IMAPSyncUtil.getImapFlagsFromStatus(testArray[i][2]));
        }

    }

    @Test
    public void testGetFlippedToFalseFlags() {
        Flags testFlags;
        //   df     f      a      df - dirty flag bits, f - flag bits, a - correct solution bits
        int[][] testArray = {
                {0x00, 0x00, 0x00},
                {0x00, 0x01, 0x00},
                {0x01, 0x00, 0x01},
                {0x01, 0x01, 0x00},
                {0x1f, 0x1f, 0x00},
                {0x1f, 0x01, 0x1e},
                {0x15, 0x12, 0x05},
                {0x1f, 0x00, 0x1f},
        };

        for (int i = 0; i < testArray.length; i++) {
            testFlags = IMAPSyncUtil.getFlippedToFalseFlags(buildImapStatusInt(testArray[i][0], testArray[i][1]));
            Assert.assertEquals(testFlags, IMAPSyncUtil.getImapFlagsFromStatus(testArray[i][2]));
        }

    }

    @Test
    public void testGetImapStatusFromFlags() {
        Flags testFlags;

        //Seen
        testFlags = new Flags(Flags.Flag.SEEN);
        Assert.assertEquals(0x01, IMAPSyncUtil.getStatusFromImapFlags(testFlags));

        //Answered
        testFlags = new Flags(Flags.Flag.ANSWERED);
        Assert.assertEquals(0x02, IMAPSyncUtil.getStatusFromImapFlags(testFlags));

        //Flagged
        testFlags = new Flags(Flags.Flag.FLAGGED);
        Assert.assertEquals(0x04, IMAPSyncUtil.getStatusFromImapFlags(testFlags));

        //Deleted
        testFlags = new Flags(Flags.Flag.DELETED);
        Assert.assertEquals(0x08, IMAPSyncUtil.getStatusFromImapFlags(testFlags));

        //Draft
        testFlags = new Flags(Flags.Flag.DRAFT);
        Assert.assertEquals(0x10, IMAPSyncUtil.getStatusFromImapFlags(testFlags));

        //Seen + Answered
        testFlags = new Flags(Flags.Flag.ANSWERED);
        testFlags.add(Flags.Flag.SEEN);
        Assert.assertEquals(0x03, IMAPSyncUtil.getStatusFromImapFlags(testFlags));

        //Seen + Deleted
        testFlags = new Flags(Flags.Flag.SEEN);
        testFlags.add(Flags.Flag.DELETED);
        Assert.assertEquals(0x09, IMAPSyncUtil.getStatusFromImapFlags(testFlags));

        //Nothing
        testFlags = new Flags();
        Assert.assertEquals(0x00, IMAPSyncUtil.getStatusFromImapFlags(testFlags));

        //Null
        testFlags = null;
        Assert.assertEquals(0x00, IMAPSyncUtil.getStatusFromImapFlags(testFlags));

        //Seen, Answered, Flagged, Deleted and Draft
        testFlags = new Flags(Flags.Flag.SEEN);
        testFlags.add(Flags.Flag.ANSWERED);
        testFlags.add(Flags.Flag.FLAGGED);
        testFlags.add(Flags.Flag.DELETED);
        testFlags.add(Flags.Flag.DRAFT);
        Assert.assertEquals(0x1f, IMAPSyncUtil.getStatusFromImapFlags(testFlags));
    }
}
