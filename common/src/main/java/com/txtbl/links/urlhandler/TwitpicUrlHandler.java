package com.txtbl.links.urlhandler;

import com.archermind.txtbl.attachment.format.inte.impl.GIFFormatImpl;
import com.archermind.txtbl.attachment.format.inte.impl.JPGFormatImpl;
import com.archermind.txtbl.utils.StringUtils;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.commons.io.IOUtils;
import org.jboss.logging.Logger;

import java.io.InputStream;
import java.net.HttpURLConnection;


/**
 * Twitpic URL Handler implementation
 */
public class TwitpicUrlHandler extends BinaryDataUrlHandler
{
    private static final Logger log = Logger.getLogger(PDFUrlHandler.class);
   

    private static final int maxAttachmentNameLength = 10;

    /**
     * Creates an attachment from the twitpic image at the given url.
     *
     * @param url The twitpic url
     * @return A collection of (1) attachment - the twitpic image, or an empty list if we fail to consume the image.
     */
    public byte[] createFromURL(String url, String context)
    {

        log.debug(String.format("processing url %s for %s", url, context));

        WebClient webClient = null;

        try
        {
            if (url.toLowerCase().contains(TWITPIC_URL_PATTERN))
            {
                webClient = new WebClient();

                return createAttachmentFromURL(webClient, url, context);
            }
            else
            {
                log.debug(String.format("This url handler is not suitable for %s, %s", url, context));
            }
        }
        catch (Throwable t)
        { 
            log.fatal(String.format("Unable to process attachments from url %s for %s", url, context), t);
        }
        finally
        {
            try
            {
                if (webClient != null)
                {
                    webClient.closeAllWindows();
                }
            }
            catch (Throwable t)
            {
                // nothing to do here
            }
        }

        return null;
    }

    private byte[] createAttachmentFromURL(WebClient webClient, String twitPicUrl, String context)
    {

        InputStream stream = null;

        try
        {
            webClient.setJavaScriptEnabled(false);

            HtmlPage page = webClient.getPage(twitPicUrl);

            HtmlElement element = page.getElementById("photo-display");

            if (element != null)
            {
                String src = element.getAttribute("src");

                if (StringUtils.isNotEmpty(src))
                {
                    String name = getTwitPicImageName(src);

                    log.debug(String.format("we have identifed a twitpic image %s, %s", name, context));

                    byte[] data = getAttachment(src, context);

                    if (isJpgOrPng(name))
                    {
                        return new JPGFormatImpl().resize(data, "JPG", getTargetHeight(), getTargetWidth());
                    }
                    else if (isGif(name))
                    {
                        return new GIFFormatImpl().resize(data, "JPG", getTargetHeight(), getTargetWidth());
                    }
                }
                else
                {
                    log.warn(String.format("we have a twitpic, we have photo-display item, but no src attribute at %s for %s", twitPicUrl, context));
                }
            }
            else
            {
                log.warn(String.format("we have a twitpic link but can't find photo-display element at %s for %s", twitPicUrl, context));
            }

        }
        catch (Throwable t)
        {
            log.warn(String.format("we have a twitpic link but can't load image from %s for %s", twitPicUrl, context), t);
        }
        finally
        {

            IOUtils.closeQuietly(stream);

        }

        return null;
    }

    /**
     * Parses and returns the image name from the twitpic image source string.
     *
     * @param twitPicImageSource Image source string, for example:
     *                           http://s3.amazonaws.com/twitpic/photos/large/34982736.jpg?AWSAccessKeyId=0ZRYP5X5F6FSMBCCSE82&Expires=1255105908&Signature=GQ3do73Hnr0rBRiuXF1%2FYl3KGUM%3D
     * @return The image name
     */
    private String getTwitPicImageName(String twitPicImageSource)
    {

        try
        {
            String name = twitPicImageSource.substring(twitPicImageSource.lastIndexOf("/") + 1);

            String[] tokens = name.split("\\?");

            String fileName = tokens[0];

            if (fileName.length() > maxAttachmentNameLength)
            {
                log.warn(String.format("attachment file %s name length exceeds maximum allowed (%d), name will be truncated", fileName, maxAttachmentNameLength));

                int index = fileName.length() - maxAttachmentNameLength;
                fileName = fileName.substring(index);
            }

            return fileName;
        }
        catch (Throwable t)
        {
            // We'll get here if our assumptions about the twitpic src url is incorrect

            log.warn(String.format("unable to determine twitpic image name from source %s", twitPicImageSource), t);

            throw new RuntimeException(t);
        }

    }

}
