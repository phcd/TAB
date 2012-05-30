package com.txtbl.links.urlhandler;

import com.archermind.txtbl.attachment.format.inte.impl.JPGFormatImpl;
import org.jboss.logging.Logger;


/**
 * A UrlHandler which consumes a JPG image specified as a URL
 */
public class JPGUrlHandler extends BinaryDataUrlHandler
{
    private static final Logger log = Logger.getLogger(JPGUrlHandler.class);

    public byte[] createFromURL(String url, String context)
    {

        log.debug(String.format("processing url %s for %s", url, context));

        try
        {
            if (url.toLowerCase().endsWith(".jpg") || url.toLowerCase().endsWith(".jpeg"))
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
            log.fatal(String.format("Unable to process attachments from url %s for %s", url, context), t);
        }

        return null;
    }

    private byte[] createAttachmentFromURL(String url, String context)
    {

        try
        {
            byte[] image = getAttachment(url, context);

            return new JPGFormatImpl().resize(image, "JPG", getTargetWidth(), getTargetHeight());
        }
        catch (Throwable t)
        {
            log.fatal(String.format("unable to load image from %s for %s", url, context), t);
        }

        return null;
    }

}
