package com.archermind.txtbl.utils;

import org.jboss.logging.Logger;
import org.owasp.esapi.codecs.Codec;
import org.owasp.esapi.codecs.MySQLCodec;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.errors.EncodingException;
import org.owasp.esapi.errors.IntrusionException;
import org.apache.commons.lang.CharUtils;
import com.archermind.txtbl.domain.Email;

import java.util.List;

/**
 * Used to escape user input to make it safe to use in a SQL query (prevent SQL-Injection)
 */
public class MySQLEncoder {
    private static final Codec MYSQL_CODEC = new MySQLCodec(MySQLCodec.MYSQL_MODE);

    private static final Logger logger = Logger.getLogger(MySQLEncoder.class);

    private static boolean sqlEncodingEnabled = Boolean.parseBoolean(SysConfigManager.instance().getValue("sqlEncodingEnabled", "true"));

    /**
     * Escapes the given string to make it safe to use in a SQL query.
     *
     * @param str The string to encode.
     * @return The encoded string.
     */
    public static String encode(String str) {
        return ESAPI.encoder().encodeForSQL(MYSQL_CODEC, canonicalize(str));
    }

    /**
     * Removes escape characters from the String.
     *
     * @param str The MySQL escaped string.
     *
     * @return The String, free of the MySQL escape character.
     */
    public static String decode(String str) {
         return removeControlChars(canonicalize(str));
    }

    /**
     * For SQL-injection prevention, escape vulnerable fields prior to using them in SQL queries.
     *
     * @param emailList  The list of email to process.
     *
     * @return Passthrough of the argument emailList - for convenience.
     */
    public static List<Email> encode(List<Email> emailList) {
        for (Email email : emailList) {
            encode(email);
        }
        return emailList;
    }

    /**
     * For SQL-injection prevention, escape vulnerable fields prior to using them in SQL queries.
     *
     * @param email  The email to process.
     *
     * @return Passthrough of the argument email - for convenience.
     */
    public static Email encode(Email email) {
        if (supports(email)) {
            // subject field
            //email.setSubject(MySQLEncoder.encode(email.getSubject()));

            // other fields ...
        }
        return email;
    }

    /**
     * Remove escape characters from the escaped fields.
     * @param emailList list of Email to process.
     *
     * @return Passthrough of the argument emailList - for convenience.
     */
    public static List<Email> decode(List<Email> emailList) {
        for (Email email : emailList) {
            decode(email);
        }
        return emailList;
    }

    /**
     * Remove escape characters from the escaped fields.
     *
     * @param email The email to process
     *
     * @return Passthrough of the argument email - for convenience.
     */
    public static Email decode(Email email) {
        if (supports(email)) {
            // subject field
            long start = System.nanoTime();

            //email.setSubject(MySQLEncoder.decode(email.getSubject()));

            if (logger.isDebugEnabled())
            {
                long time = (System.nanoTime() - start) / 1000000;

                if (time > 10)
                {
                    logger.debug("decode completed in " + (System.nanoTime() - start) / 1000000 + " ms for message " + email.getId());
                }
            }
            // other fields ...
        }
        return email;
    }

    /**
     * We only want to have to support for TWITTER for now - when that changes in the future, we will simply return True.
     *
     * @param email The email to check
     *
     * @return True if the email is supported, false otherwise
     */
    private static boolean supports(Email email) {
        return sqlEncodingEnabled && (email != null && email.getMessage_type() != null && email.getMessage_type().toUpperCase().contains("TWITTER"));
    }

    /**
     * Canonicalization is simply the operation of reducing a possibly encoded string down to its simplest form.
     * This is important, because attackers frequently use encoding to change their input in a way that will bypass
     * validation filters, but still be interpreted properly by the target of the attack. Note that data encoded more
     * than once is not something that a normal user would generate and should be regarded as an attack.
     * - esapi javadoc
     *
     * @param str
     * @return
     */
    private static String canonicalize(String str) {

        try {

            str = ESAPI.encoder().canonicalize(str);

        } catch (EncodingException ee) {
            // TODO - how to deal with this? for now, simply return the str.
            logger.warn(String.format("Invalid encoding detected while processing %s", str), ee);

        } catch (IntrusionException ie) {
            // TODO - how to deal with this potential attack? for now, propagate the exception.
            logger.warn(String.format("Potential encoding attack detected while processing %s", str), ie);

            throw ie;
        }

        return str;
    }

    private static String removeControlChars(String str) {
        StringBuilder sb = new StringBuilder();

        for (char c : str.toCharArray()) {
            if (!CharUtils.isAsciiControl(c)) {
                sb.append(c);
            } else {
                sb.append("?");
            }
        }

        return sb.toString();
    }

}
