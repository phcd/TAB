package com.txtbl.links.urlhandler;

import com.archermind.txtbl.utils.SysConfigManager;
import com.archermind.txtbl.utils.MailUtils;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

public class DefaultUrlHandler implements UrlHandler
{
    private static final Logger log = Logger.getLogger(DefaultUrlHandler.class);

    private static final int MAX_RETRIES = Integer.valueOf(SysConfigManager.instance().getValue("tweetHandlerRetryCount", "1"));

    private static final int CONNECTION_TIMEOUT = Integer.valueOf(SysConfigManager.instance().getValue("tweetHandlerConnectionTimeout", "10000"));

    private static final int MAX_CONNECTIONS_PER_CLIENT = Integer.valueOf(SysConfigManager.instance().getValue("tweetHandlerMaxConnectionsPerClient", "10000"));

    // TODO - see getAuthString
    private static String nyTimesUsername = "peek";

    private static String nyTimesPassword = "lex10017";

    private HttpClient httpClient = null;

    /**
     * Initializes the TweetHandler by configuring the http client instance.
     */
    public DefaultUrlHandler()
    {
        this.httpClient = new HttpClient(getConnectionManager());
        this.httpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(MAX_RETRIES, false));
        this.httpClient.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, CONNECTION_TIMEOUT);
    }

    /**
     * Creates an attachment from the twitpic image at the given url.
     *
     * @param url The twitpic url
     * @return A collection of (1) attachment - the twitpic image, or an empty list if we fail to consume the image.
     */
    public byte[] createFromURL(String url, String context)
    {
        log.debug(String.format("processing url %s for %s", url, context));

        try
        {
            if (isSupportedMimeType(url, context))
            {
                return createAttachmentFromURL(url, context);
            }
            else
            {
                log.info(String.format("Unsupported Content-Type for url %s for %s", url, context));
            }
        }
        catch (Throwable t)
        {
            log.fatal(String.format("Unable to process attachments from url %s for %s", url, context), t);
        }

        return null;
    }

    /**
     *
     */
    private byte[] createAttachmentFromURL(String targetUrl, String context) throws MalformedURLException, UnsupportedEncodingException
    {
        File file = null;

        try
        {
            file = createTempFile(targetUrl, context);

            if (file != null)
            {
                String text = LynxHelper.dumpUrl(file.toURL().toExternalForm());

                if (text != null)
                {
                    if(log.isTraceEnabled())
                        log.trace(String.format("Processing lynx response:\n%s", text));

                    String[] lines = text.split("\n");

                    StringBuilder buffer = new StringBuilder();

                    for (String line : lines)
                    {
                        if (LynxHelper.isReadable(line))
                        {
                            buffer.append(MailUtils.clean(line.replaceAll(LynxHelper.LYNK, ""), false)).append("\n");
                        }
                    }

                    if(log.isTraceEnabled())
                        log.trace(String.format("text parsed from %s........................:\n%s", targetUrl, buffer));

                    return buffer.toString().getBytes("UTF-8");
                }
            }
        }

        finally
        {
            if (file != null)
            {
                try
                {
                    file.delete();
                }
                catch (Throwable t)
                {
                    log.warn(String.format("failed to delete temporary file %s", file.getName()), t);
                }
            }
        }

        return null;
    }

    private File createTempFile(String url, String context)
    {

        File file = null;

        HttpURLConnection conn = null;
        OutputStream os = null;

        GetMethod get = null;

        try
        {
            HttpMethodParams params = new HttpMethodParams();
            params.setCookiePolicy(CookiePolicy.DEFAULT);

            get = new GetMethod(url);
            get.setParams(params);

            int rc = httpClient.executeMethod(get);

            String data = get.getResponseBodyAsString();

            file = File.createTempFile("lynx", ".html");

            os = new FileOutputStream(file);

            IOUtils.write(data, os);

            os.flush();

        }
        catch (Throwable t)
        {

            log.warn(String.format("unable to create data file from %s for %s", url, context), t);

        }
        finally
        {
            try
            {
                if (get != null)
                {
                    get.releaseConnection();
                }
            }
            catch (Throwable t)
            {
                // nothing to do here
            }

            IOUtils.closeQuietly(os);

            if (conn != null)
            {
                conn.disconnect();
            }
        }

        return file;
    }


    private MultiThreadedHttpConnectionManager getConnectionManager()
    {
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();

        HttpConnectionManagerParams params = new HttpConnectionManagerParams();
        params.setMaxTotalConnections(MAX_CONNECTIONS_PER_CLIENT);
        params.setConnectionTimeout(CONNECTION_TIMEOUT);
        params.setSoTimeout(CONNECTION_TIMEOUT);

        connectionManager.setParams(params);

        return connectionManager;
    }

    private boolean isSupportedMimeType(String url, String context)
    {

        HeadMethod head = null;
        try
        {

            HttpMethodParams params = new HttpMethodParams();
            params.setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);

            head = new HeadMethod(url);
            head.setParams(params);
            head.setFollowRedirects(false);

            httpClient.executeMethod(head);

            String mime = null;
            Header header = head.getResponseHeader("Content-Type");
            if (header != null)
            {
                mime = header.getValue();
            }

            return mime != null && (
                    mime.contains("text/html") ||
                            mime.contains("text") ||
                            mime.contains("xml") ||
                            mime.contains("plain"));
        }
        catch (Throwable t)
        { // IOException, etc.
            log.error(String.format("Unable to determine Content-Type for url %s for %s", url, context), t);
        }
        finally
        {
            try
            {
                if (head != null)
                {
                    head.releaseConnection();
                }
            }
            catch (Throwable t)
            {
                // nothing to do here
            }
        }

        return true;
    }

}
