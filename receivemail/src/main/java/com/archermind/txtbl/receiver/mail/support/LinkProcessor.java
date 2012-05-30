package com.archermind.txtbl.receiver.mail.support;

import com.archermind.txtbl.domain.OriginalReceivedAttachment;
import com.archermind.txtbl.utils.SysConfigManager;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkProcessor {


    private static final String IMG_ATTACHMENT_EXTENSION = ".jpg";

    private static final String TXT_ATTACHMENT_EXTENSION = ".txt";

    private static final int MAX_ATTACHMENTS_PER_EMAIL = Integer.valueOf(SysConfigManager.instance().getValue("maxAttachmentsPerEmail", "5"));

    private static final boolean REDIRECTING_LINK_CHECK = Boolean.valueOf(SysConfigManager.instance().getValue("redirectingLinkCheck", "false"));

    private static final int MAX_ATTACHMENT_NAME_LENGTH = Integer.valueOf(SysConfigManager.instance().getValue("maxAttachmentNameLength", "1000"));

    private static final Pattern LINK_PATTERN = Pattern.compile("(http[s]*://[a-zA-Z_0-9$_.+!*'()\\-/\\?:@=&]+)", Pattern.CASE_INSENSITIVE);

    private static final int MAX_RETRIES = Integer.valueOf(SysConfigManager.instance().getValue("providerSupportRetryCount", "1"));

    private static final int CONNECTION_TIMEOUT = Integer.valueOf(SysConfigManager.instance().getValue("providerSupportConnectionTimeout", "45000"));

    private static final int MAX_CONNECTIONS_PER_CLIENT = Integer.valueOf(SysConfigManager.instance().getValue("providerSupportMaxConnectionsPerClient", "10000"));

    private HttpClient httpClient = null;

    private static LinkProcessor linkProcessor = null;

    private static final Logger log = Logger.getLogger(LinkProcessor.class);

    public LinkProcessor(){

        this.httpClient = new HttpClient(getConnectionManager());
        this.httpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(MAX_RETRIES, true));
        this.httpClient.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, CONNECTION_TIMEOUT);

    }

    public static LinkProcessor getInstance(){
        if (linkProcessor==null){
            linkProcessor = new LinkProcessor();
        }

        return linkProcessor;
    }

    

     public List<OriginalReceivedAttachment> createLinkAttachments(String message) throws Exception
     {
        List<OriginalReceivedAttachment> attachments = new ArrayList<OriginalReceivedAttachment>();

        Matcher matcher = LINK_PATTERN.matcher(message);

        int index = 0;

        while (matcher.find(index) && attachments.size() < MAX_ATTACHMENTS_PER_EMAIL)
        {
            String url = matcher.group(1);

            OriginalReceivedAttachment attachment = new OriginalReceivedAttachment();

            String extension = isImageResource(url) ? IMG_ATTACHMENT_EXTENSION : TXT_ATTACHMENT_EXTENSION;

            if (url.length() <= MAX_ATTACHMENT_NAME_LENGTH)
            {
                String attachName = url.replace("http://", "http#").replaceAll("/", "#");

                attachment.setName(attachName + "_" + index + extension);

                //attachment.setData(("LINK:" + url).getBytes(UTF_8_ENCODING));

                attachments.add(attachment);
            }

            index = matcher.end();
        }

        return attachments;
    }

    private boolean isImageResource(String url)
    {

        boolean isImage = false;

        GetMethod get = null;

        try
        {

            if (url.contains("twitpic.com"))
            {
                isImage = true;
            }
            else if (hasImageExtension(url))
            {
                isImage = true;
            }
            else if (REDIRECTING_LINK_CHECK)
            {
                HttpMethodParams params = new HttpMethodParams();
                params.setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);

                get = new GetMethod(url);
                get.setParams(params);
                get.setFollowRedirects(true);

                int returnCode = httpClient.executeMethod(get);

                if (returnCode == 301)
                {
                    log.debug(String.format( "url %s is a redirect- attempting to retrieve \"location\" from response header", url));

                    String redirect = get.getResponseHeader("location").getValue();

                    isImage = hasImageExtension(redirect);
                }
            }
        }
        catch (Throwable t)
        {
            log.warn(String.format("Unable to determine if url %s points to an image resource", url), t);
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
        }

        return isImage;
    }

    private boolean hasImageExtension(String rsc)
    {
        rsc = rsc.toLowerCase();
        return rsc != null && (rsc.endsWith(".jpg") || rsc.endsWith(".jpeg") || rsc.endsWith(".gif") || rsc.endsWith(".png"));
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

}
