package com.archermind.txtbl.domain;


import org.jboss.logging.Logger;

import java.io.Serializable;
import java.util.StringTokenizer;

public class TwitterMeta implements Serializable {

    private static final Logger log = Logger.getLogger(TwitterMeta.class);

    private static final String DEFAULT_SOURCE = "PEEKINC";

    private long messageId = -1L;
    private String source = DEFAULT_SOURCE;

    public TwitterMeta(long messageId, String source) {
        this.messageId = messageId;
        this.source = source;
    }

    public TwitterMeta(long messageId) {
        this(messageId, DEFAULT_SOURCE);
    }

    /**
     * Default constructor- required by JAXB. Not very useful since TwitterMeta is immutable
     */
    public TwitterMeta() {
    }

    public String getSource() {
        return source;
    }

    public long getMessageId() {
        return messageId;
    }


    // package scope
    /**
     * Converts the twitter meta into the form stored in the comment field of txtbl_email_received
     *
     * @return
     */
    String toComment() {
        return messageId + "|" + source;
    }

    /**
     * Parses the txtbl_email_received comment field to create the twitter meta data. This
     * is called from Email.java
     *
     * @param comment Comment formatted by this c
     * @return  A TwitterMeta parsed from comment. If comment is not of the expected format, the
     * message Id of the meta will be -1, and the source will be set to the comment string.
     */
    static TwitterMeta createFromString(String comment) {

        long messageId = -1;
        String source = comment;
        try {
            StringTokenizer st = new StringTokenizer(comment, "|");
            messageId = Long.parseLong(st.nextToken());
            source = st.nextToken();
        } catch (Throwable t) { // number format, index out of bounds

        }

        return new TwitterMeta(messageId, source == null ? "web" : source);
    }

}
