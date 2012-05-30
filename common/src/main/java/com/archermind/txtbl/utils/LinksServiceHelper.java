package com.archermind.txtbl.utils;

import com.archermind.txtbl.exception.SystemException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.jboss.logging.Logger;


import java.io.IOException;

public class LinksServiceHelper {
    private static final Logger logger = Logger.getLogger(LinksServiceHelper.class);

    private static final int CONNECTION_TIMEOUT = Integer.valueOf(SysConfigManager.instance().getValue("linksServiceConnectionTimeout", "30000"));
    private static final int MAX_CONNECTIONS_PER_CLIENT = Integer.valueOf(SysConfigManager.instance().getValue("linksServiceMaxConnectionsPerClient", "10000"));


    private static final String LINKS_SERVICE_URL = SysConfigManager.instance().getValue("linksServiceUrl");


    private static HttpClient httpClient;

    static
    {
        try
        {
            httpClient = new HttpClient(getConnectionManager());
        }
        catch (Throwable t)
        {
            logger.fatal("unable to initialize http client", t);
        }
    }

    public static byte[] process(String url, String context) throws IOException
    {
        if (StringUtils.isEmpty(LINKS_SERVICE_URL))
        {
            logger.warn("links service url is undefined, no links service....");

            return null;
        }
        else
        {
            PostMethod method = new PostMethod(LINKS_SERVICE_URL);

            method.setParameter("url", url);
            method.setParameter("context", context);

            long nanos = System.nanoTime();

            int returnCode = -1;

            try
            {
                returnCode = httpClient.executeMethod(method);

                if (returnCode == 200)
                {
                    logger.debug(String.format("return code is %s for url %s", returnCode, url));

                    return method.getResponseBody();
                }
                else
                {
                    throw new SystemException(String.format("Unable to communicate with links service: %s. Return code %d.", LINKS_SERVICE_URL, returnCode));
                }
            }
            finally
            {
                method.releaseConnection();

                logger.debug(String.format("%s for %s returned %s completed in %d", method.getClass().getName(), method.getURI(), returnCode, (System.nanoTime() - nanos) / 1000000l));
            }
        }

    }

    private static MultiThreadedHttpConnectionManager getConnectionManager()
    {
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();

        HttpConnectionManagerParams params = new HttpConnectionManagerParams();
        params.setMaxTotalConnections(MAX_CONNECTIONS_PER_CLIENT);
        params.setConnectionTimeout(CONNECTION_TIMEOUT);
        params.setSoTimeout(CONNECTION_TIMEOUT);

        connectionManager.setParams(params);

        return connectionManager;
    }
}
