package com.archermind.txtbl;

import org.junit.BeforeClass;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class AbstractCommonTests {


    private static final String MASTER = "java:txtbl-master";
    private static final String SLAVE = "java:txtbl-slave";

    private static final String DATA_SOURCE = "mysql-ds";

    private static boolean isFirst = true;

    @BeforeClass
    public static void setup() throws NamingException, IOException, SQLException {


        if (isFirst) {
            ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("test-providers.xml");
            SimpleNamingContextBuilder builder = new SimpleNamingContextBuilder();

            DataSource dataSource = (DataSource) applicationContext.getBean(DATA_SOURCE);
            builder.bind(MASTER, dataSource);
            builder.bind(SLAVE, dataSource);
            builder.activate();
            //executeSQL(dataSource, "/sql/create_tables.sql");
            isFirst = false;
        }

    }

    private static void executeSQL(DataSource dataSource, String sqlFilename) throws SQLException, IOException {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            /*ScriptRunner runner = new ScriptRunner(connection, false, true);
            FileReader reader = new FileReader(new ClassPathResource(sqlFilename).getFile());

            runner.runScript(reader);*/
        }
        finally {
            if (connection != null) {
                connection.close();
            }
        }
    }


}
