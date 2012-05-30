package com.archermind.txtbl.utils;

import com.sun.mail.imap.IMAPSSLStore;
import com.sun.mail.imap.IMAPFolder;
import net.oauth.OAuthConsumer;

import javax.mail.Session;
import javax.mail.URLName;
import javax.mail.Folder;
import java.security.Security;
import java.util.Properties;
import java.util.Date;


/**
 * Performs XOAUTH authentication.
 * <p/>
 * <p>Before using this class, you must call {@code initialize} to install the
 * XOAUTH SASL provider.
 */
public class XoauthAuthenticator
{

    /**
     * Generates a new OAuthConsumer with token and secret of
     * "anonymous"/"anonymous". This can be used for testing.
     */
    public static OAuthConsumer getAnonymousConsumer()
    {
        return new OAuthConsumer(null, "anonymous", "anonymous", null);
    }

    /**
     * Installs the XOAUTH SASL provider. This must be called exactly once before
     * calling other methods on this class.
     */
    public static void initialize()
    {
        Security.addProvider(new com.archermind.txtbl.utils.XoauthProvider());
    }

    /**
     * Connects and authenticates to an IMAP server with XOAUTH. You must have
     * called {@code initialize}.
     *
     * @param host             Hostname of the imap server, for example {@code
     *                         imap.googlemail.com}.
     * @param port             Port of the imap server, for example 993.
     * @param userEmail        Email address of the user to authenticate, for example
     *                         {@code xoauth@gmail.com}.
     * @param oauthToken       The user's OAuth token.
     * @param oauthTokenSecret The user's OAuth token secret.
     * @param consumer         The application's OAuthConsumer. For testing, use
     *                         {@code getAnonymousConsumer()}.
     * @param debug            Whether to enable debug logging on the IMAP connection.
     * @return An authenticated IMAPSSLStore that can be used for IMAP operations.
     */
    public static IMAPSSLStore connectToImap(String host, int port, String userEmail, String oauthToken, String oauthTokenSecret, OAuthConsumer consumer, boolean debug) throws Exception
    {
        Properties props = new Properties();
        props.setProperty("mail.imaps.sasl.enable", "true");
        props.setProperty("mail.imaps.sasl.mechanisms", "XOAUTH");

        props.setProperty(XoauthSaslClientFactory.OAUTH_TOKEN_PROP, oauthToken);
        props.setProperty(XoauthSaslClientFactory.OAUTH_TOKEN_SECRET_PROP, oauthTokenSecret);
        props.setProperty(XoauthSaslClientFactory.CONSUMER_KEY_PROP, consumer.consumerKey);
        props.setProperty(XoauthSaslClientFactory.CONSUMER_SECRET_PROP, consumer.consumerSecret);
        Session session = Session.getInstance(props);
        session.setDebug(debug);

        final URLName unusedUrlName = null;
        IMAPSSLStore store = new IMAPSSLStore(session, unusedUrlName);
        final String emptyPassword = "";
        store.connect(host, port, userEmail, emptyPassword);
        return store;
    }

    /**
     * Authenticates to IMAP with parameters passed in on the commandline.
     */
    public static void main(String args[]) throws Exception
    {
        String email = "paul@getpeek.in";
        String oauthToken = "1/hGadsF8G06ULmZWP6OFdaE4uvbHTywz15pa8cPO7Psc";
        String oauthTokenSecret = "0Bs5+KM211srfBju9Lot08OO";

        initialize();
        Date date = new Date();
        IMAPSSLStore imapSslStore = connectToImap("imap.gmail.com", 993, email, oauthToken, oauthTokenSecret, getAnonymousConsumer(), false);
        System.out.println("Successfully authenticated to IMAP.");
        System.out.println(String.format("Date Diff:%d", new Date().getTime() - date.getTime()));
        IMAPFolder inbox = (IMAPFolder) imapSslStore.getFolder("INBOX");
        System.out.println("got imap folder");
        inbox.open(Folder.READ_ONLY);
        System.out.println("opened inbox");
    }
}
