package com.txtbl.links.urlhandler;


import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.jboss.logging.Logger;


/**
 * Base UrlHandler for binary resources specified as a URL.
 */
public abstract class BinaryDataUrlHandler implements UrlHandler {
    private static final Logger log = Logger.getLogger(JPGUrlHandler.class);

    // mail.image.height (195)
    private static final int targetHeight = 195;

    // mail.image.width (320)
    private static final int targetWidth = 320;

    protected byte[] getAttachment(String url, String context) {

        HttpURLConnection conn = null;
        InputStream stream = null;

        try {
            URL targetUrl = new URL(url);

            conn = (HttpURLConnection)targetUrl.openConnection();

            stream = conn.getInputStream();

            return IOUtils.toByteArray(stream);

        } catch (Throwable t) {

            log.fatal(String.format("unable to load data from %s for %s", url, context), t);

        } finally {

            IOUtils.closeQuietly(stream);
            if (conn != null) {
                conn.disconnect();
            }
        }

        return null;
    }

    protected int getTargetHeight() {
        return targetHeight;
    }

    protected int getTargetWidth() {
        return targetWidth;
    }

    protected boolean isJpgOrPng(String resourceName) {
        return resourceName.toLowerCase().endsWith(".jpg") || resourceName.toLowerCase().endsWith(".png");
    }

    protected boolean isGif(String resourceName) {
        return resourceName.toLowerCase().endsWith(".gif");
    }

}
