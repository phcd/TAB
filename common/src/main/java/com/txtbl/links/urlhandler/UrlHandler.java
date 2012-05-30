package com.txtbl.links.urlhandler;

/**
 * Interface to be implemented by URL handler implementations.
 *
 * WARNING: UrlHandler implementions must be thread-safe.
 */
public interface UrlHandler {
    final String TWITPIC_URL_PATTERN = "twitpic.com";
    final String NYTIMES_URL_PATTERN = "www.nytimes.com";
    final String PDF_URL_PATTERN = ".pdf";
    final String JPG_URL_PATTERN = ".jpg";
    final String GIF_URL_PATTERN = ".gif";
    final String PNG_URL_PATTERN = ".png";

    /**
     * Visits the specified URL and returns a list of attachments.
     *
     * @param url The url to visit.
     * @param context The context information - useful for debugging.
     *
     * @return A collection of attachments or an empty list if the url cannot be processed.
     */
    byte[] createFromURL(String url, String context);
}
