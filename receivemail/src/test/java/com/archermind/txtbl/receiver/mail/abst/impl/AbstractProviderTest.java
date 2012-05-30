package com.archermind.txtbl.receiver.mail.abst.impl;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.dal.business.impl.UserService;
import com.archermind.txtbl.domain.Account;
import com.ibatis.common.jdbc.ScriptRunner;
import org.junit.BeforeClass;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;


public abstract class AbstractProviderTest 
{

    private static final String JNDI = "java:txtbl-master";

    private static final String DATA_SOURCE = "mysql-ds";

    private static final String PATH = "/com/archermind/txtbl/receiver/mail/xml/test-providers.xml";

    private static final String TXTBL_SYS_CONFIG = "/sql/txtbl_sys_config.sql";
    private static final String TXTBL_SYS_RECEIVED = "/sql/txtbl_sys_received.sql";
    private static final String TXTBL_USER = "/sql/txtbl_user.sql";
    private static final String TXTBL_EMAIL_RECEIVED = "/sql/txtbl_email_received.sql";
    private static final String TXTBL_EMAIL_ACCOUTN = "/sql/txtbl_email_account.sql";


    private static DataSource dataSource;

    @BeforeClass
    public static void setup() throws NamingException, IOException, SQLException
    {

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

    private static void executeSQL(DataSource dataSource, String sqlFilename) throws SQLException, IOException
    {
        Connection connection = null;
        try
        {
            connection = dataSource.getConnection();
            ScriptRunner runner = new ScriptRunner(connection, false, true);
            FileReader reader = new FileReader(new ClassPathResource(sqlFilename).getFile());

            runner.runScript(reader);
        }
        finally
        {
            if (connection != null)
            {
                connection.close();
            }
        }
    }



    protected Account getAccount(String protocol, String domain) throws DALException
    {
        List<Account> accounts = new UserService().getAccountToTaskfactory(protocol);

        Account account = null;
        for (Account _account : accounts)
        {
            System.out.println("loading accounts: " + _account.toString());
            if (_account.getName().contains(domain))
            {
                account = _account;
                break;
            }
        }
        return account;
    }


    protected boolean containsDroppedEmail(Account account)
    {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String sql = "select count(*) from txtbl_email_received where subject = 'Your email' and user_id = ? ";
        return jdbcTemplate.queryForInt(sql, new Object[]{account.getUser_id()}) > 0;
    }

    protected void updateHash(Account account)  {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update("update txtbl_email_account set folder_hash = '' where id = ?", new Object[] {account.getId()});
    }

}
