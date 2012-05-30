package com.txtbl.links.urlhandler;

import com.archermind.txtbl.attachment.format.inte.impl.JPGFormatImpl;
import com.archermind.txtbl.domain.Attachment;
import org.jboss.logging.Logger;


import java.util.List;

/**
 * A UrlHandler which consumes a PNG image specified as a URL
 */
public class PNGUrlHandler extends BinaryDataUrlHandler
{
    private static final Logger log = Logger.getLogger(PNGUrlHandler.class);
    
    public byte[] createFromURL(String url, String context)
    {

        log.debug(String.format("processing url %s for %s", url, context));

        try
        {
            if (url.toLowerCase().endsWith(".png"))
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

    private byte[] createAttachmentFromURL(String url, String context)
    {

        try
        {
            byte[] data = getAttachment(url, context);

            List<Attachment> a = new JPGFormatImpl().format("", "", "png", data, getTargetHeight(), getTargetWidth());

            return a.get(0).getData();


        }
        catch (Throwable t)
        {
            log.fatal(String.format("unable to load image from %s for %s", url, context), t);
        }

        return null;
    }

}
