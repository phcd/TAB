package com.txtbl.links.urlhandler;

import com.archermind.txtbl.attachment.format.inte.impl.PDFFormatImpl;
import com.archermind.txtbl.attachment.format.inte.impl.TextFilter;
import com.archermind.txtbl.domain.Attachment;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * A UrlHandler which consumes a PDF document specified as a URL
 */
public class PDFUrlHandler extends BinaryDataUrlHandler
{
    private static final Logger log = Logger.getLogger(PDFUrlHandler.class);

    public byte[] createFromURL(String url, String context)
    {
        log.debug(String.format("processing url %s for %s", url, context));

        try
        {
            if (url.toLowerCase().endsWith(".pdf"))
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
            byte[] data = getAttachment(url, context);

            List<Attachment> attachments = new PDFFormatImpl(new TextFilter()).format("", "", "pdf", data, 0, 0);
            if (attachments.size() > 0)
            {
                return attachments.get(0).getData();
            }
        }
        catch (Throwable t)
        {
            log.fatal(String.format("unable to load image from %s for %s", url, context), t);
        }

        return null;
    }

}
