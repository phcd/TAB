package com.txtbl.links.urlhandler;

import com.archermind.txtbl.utils.MailUtils;
import org.jboss.logging.Logger;


import java.util.regex.Pattern;


/**
 * New York Times URL Handler implementation
 */
public class NewYorkTimesUrlHandler implements UrlHandler
{

    private static final Logger log = Logger.getLogger(DefaultUrlHandler.class);

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
            if (url.toLowerCase().contains(UrlHandler.NYTIMES_URL_PATTERN))
            {
                return createAttachmentFromURL(url, context);
            }
            else
            {
                log.debug(String.format("This url handler is not suitable for %s, %s", url, context));
            }
        }
        catch (Throwable t)
        { // IOException, etc.
            log.warn(String.format("Unable to process attachments from url %s for %s", url, context), t);
        }

        return null;
    }

    /**
     *
     */
    private byte[] createAttachmentFromURL(String targetUrl, String context)
    {
        try
        {
            String text = LynxHelper.dumpUrl(targetUrl);

            if (text != null)
            {
//                text = text.replaceAll("\\[\\d+](.+\n){0,20}", "");
//                text = text.replaceAll("\\d+.*", "");
//                text = text.replaceAll("([#].*)", "");

                // this gets us 90% there, now let's cover the remaining 10% manually
                //            text = text.substring(0, text.indexOf("Related Searches"));
                String[] lines = text.split("\n");
                StringBuilder buffer = new StringBuilder();

                for (String line : lines)
                {
//                    if (line.contains("lynx") || line.toLowerCase().contains("jboss"))
//                    {
//                        continue;
//                    }
//                    line = line.trim();
                    if (LynxHelper.isReadable(line) && !"Financial Tools [Select a Financial Tool]".equals(line.trim()))
                    {
                        buffer.append(MailUtils.clean(line.replaceAll(LynxHelper.LYNK, ""), false)).append("\n");
                    }
                }

                if(log.isTraceEnabled())
                    log.trace(buffer);

                return buffer.toString().getBytes("UTF-8");
            }

        }
        catch (Throwable e)
        {
            log.fatal(String.format("error processing attachments from url %s for %s", targetUrl, context), e);
        }

        return null;
    }

}
