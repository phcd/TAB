/**
 *
 */
package com.archermind.txtbl.dal.mode;


import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;
import org.jboss.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.util.Properties;


public class SqlMapFactory
{
    private static final Logger log = Logger.getLogger(SqlMapFactory.class);
    public static final String CONFIG_FILE = "com/archermind/txtbl/dal/sqlmap/SqlMapConfig.xml";

    private static SqlMapClient masterSqlMapClient;
    private static SqlMapClient slaveSqlMapClient;

    static
    {
        try
        {
            log.info("SqlMapFactory initializing master sql map client");

            masterSqlMapClient = buildSqlMapClient("java:txtbl-master");

            log.info("SqlMapFactory determining if slave config is permitted");

            Boolean slaveDeployed = checkIfSlaveDeployed();

            if (slaveDeployed)
            {
                log.info("SqlMapFactory is starting with slave usage enabled.");
                slaveSqlMapClient = buildSqlMapClient("java:txtbl-slave");
            }
            else
            {
                log.info("SqlMapFactory is starting with slave usage disabled.");
                slaveSqlMapClient = masterSqlMapClient;
            }

        }
        catch (Throwable e)
        {
            log.fatal("Unexpected error during initialization of sql map client", e);
        }
    }

    private static Boolean checkIfSlaveDeployed() {
        try {
            return new InitialContext().lookup("java:txtbl-slave") != null;
        } catch (NamingException ex) {
            log.info("Slave sql client is not deployed. Master only configuration will be utilized.");
            return false;
        }

    }

    private static SqlMapClient buildSqlMapClient(String jndi) throws IOException
    {
        Properties jndiProperties = new Properties();
        jndiProperties.setProperty("DataSourceJNDI", jndi);
        return SqlMapClientBuilder.buildSqlMapClient(Resources.getResourceAsReader(CONFIG_FILE), jndiProperties);
    }

    public static SqlMapClient getMasterSqlMapClient()
    {
        return masterSqlMapClient;
    }

    public static SqlMapClient getSlaveSqlMapClient()
    {
        if (slaveSqlMapClient == null)
        {
            log.warn("Slave sql client is beeing requested while it has not been initialized, master client will be returned instead.");
            return masterSqlMapClient;
        }
        else
        {
            return slaveSqlMapClient;
        }
    }


}
