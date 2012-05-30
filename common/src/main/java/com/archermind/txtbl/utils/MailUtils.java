package com.archermind.txtbl.utils;

import com.archermind.txtbl.dal.business.impl.UserService;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.Device;
import com.archermind.txtbl.parser.MessageParser;
import com.ibm.icu.text.Transliterator;
import org.apache.commons.lang.ArrayUtils;
import org.jboss.logging.Logger;

import javax.mail.Message;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MailUtils {

    private static final Logger log = Logger.getLogger(MailUtils.class);

    private static Transliterator transliterator = Transliterator.getInstance("Latin");

    private static char[] doubleQuotes = SysConfigManager.instance().getValue("doubleQuotes", "\u201C\u201D\u201F\u2033\u2036").toCharArray();
    private static char[] apostrophes = SysConfigManager.instance().getValue("apostrophes", "\u2035\u2032\u2018\u2019\u201B").toCharArray();
    private static char[] dashes = SysConfigManager.instance().getValue("dashes", "\u001E\u2013\u2014\u2015").toCharArray();
    private static char[] newline = SysConfigManager.instance().getValue("newline", "\u0085\u2028\u2029\r\n\f\u000B").toCharArray();        // newline definitions
    private static char[] tabs = SysConfigManager.instance().getValue("tabs", "\t").toCharArray();
    private static final boolean isTransliterateEmail = Boolean.parseBoolean(SysConfigManager.instance().getValue("transliterateEmail", "true"));
    private static final boolean isTransliterateEmailPeekvetica = Boolean.parseBoolean(SysConfigManager.instance().getValue("transliterateEmailPeekvetica", "true"));


    public static String[] validateAndRemoveEmailEnhance(String email) {
        StringBuffer strings1 = new StringBuffer();
        StringBuffer strings2 = new StringBuffer();
        String[] s = new String[2];
        SysConfigManager scfgManager = SysConfigManager.instance();
        String EMAIL_SUFFIX = "10digit.convert.email";

        if (email == null)
            return null;

        Pattern p = Pattern.compile("(\\w|\\.|%|/|-|\\+|=|\\?)+@([A-Za-z0-9\\-]+\\.)+[A-Za-z]+");
        String[] emails = StringUtils.parseString(email, ";,");

        for (int ii = 0; ii < emails.length; ii++) {
            if (!"".equals(emails[ii].trim())) {
                emails[ii] = emails[ii].trim();
                Matcher m = p.matcher(emails[ii]);

                boolean validation = m.find();

                if (checkDigits(emails[ii])) {
                    log.debug(String.format("adding a text %s", emails[ii]));
                    strings2.append(emails[ii]).append(scfgManager.getValue(EMAIL_SUFFIX)).append(";");
                } else {
                    log.debug(String.format("address %s not a text", emails[ii]));
                }

                if (validation) {
                    if (isSmsEmail(emails[ii], scfgManager.getValue(EMAIL_SUFFIX))) {
                        strings2.append(emails[ii]).append(";");
                    } else {
                        strings1.append(emails[ii].trim()).append(";");
                    }
                }
            }
        }

        s[0] = new String(strings1);
        s[1] = new String(strings2);

        return s;
    }


    private static boolean isSmsEmail(String email, String emailSuffix) {
        boolean isSms = false;

        int flagPos = email.indexOf("@");

        if (flagPos < 0) return isSms;

        String curEmail = email.substring(flagPos);

        if (curEmail.equalsIgnoreCase(emailSuffix))
            isSms = true;

        return isSms;
    }

    private static boolean checkDigits(String to) {
        char[] tmp = to.toCharArray();

        for (int i = 0; i < tmp.length; i++) {
            if (tmp[i] == '+') { // plus sign
                log.debug(String.format("Plus sign in text address"));
                if (i != 0) {
                    return false;
                }
            } else if ((int) tmp[i] < 48 || (int) tmp[i] > 57) { // 0 and 9
                return false;
            }
        }
        return true;
    }

    public static boolean validateMailID(String mailid) {
        if (log.isTraceEnabled())
            log.trace(String.format("validateMailID(mailid=%s)", (null != mailid ? mailid : "null")));
        boolean validation = false;
        if (mailid == null)
            return validation;
        Pattern p = Pattern.compile("^\\d+$");
        Matcher m = p.matcher(mailid);

        validation = m.find();
        return validation;
    }

    public static boolean validateAlias(String crmAlias, String account_alias_division) {
        if (log.isTraceEnabled())
            log.trace(String.format("validateAlias(crmAlias=%s, account_alias_division=%s)", String.valueOf(crmAlias), String.valueOf(account_alias_division)));
        return crmAlias != null && StringUtils.parseString(crmAlias, account_alias_division).length > 1;
    }

    /**
     * Removes special characters from the message  // Not used for Twitter.
     * For Twitter see TwitterSupport.java.clean
     *
     * @param message                 string to clean
     * @param isTransliterate         Do basic cleaning for isolatin-1
     * @param transliteratePeekvetica Do extended transformation for peek character set
     * @return cleansed string
     */
    public static String clean(String message, boolean isTransliterate, boolean transliteratePeekvetica) {

        if (message == null) {
            log.debug("message is null, returning early");
            return null;
        }
        if (!isTransliterate) {
            log.debug("transliterateEmail is false, skipping all Email transliteration");
            return message;
        }

        String newMessage = transliterator.transform(message);


        StringBuilder builder = new StringBuilder();
        for (char _char : newMessage.toCharArray()) {
            //replaces NON ANSII special symbols
            if (ArrayUtils.contains(doubleQuotes, _char)) {
                builder.append("\"");
                continue;
            }
            if (ArrayUtils.contains(apostrophes, _char)) {
                builder.append("\'");
                continue;
            }
            if (ArrayUtils.contains(dashes, _char)) {
                builder.append("-");
                continue;
            }
            if (ArrayUtils.contains(tabs, _char)) {
                builder.append(" ");
                continue;
            }


            if (ArrayUtils.contains(newline, _char)) {
                builder.append(Character.toString(_char));
            } else //check for low/high ascii
            {
                char c = _char;
                if (!ArrayUtils.contains(newline, c)) {
                    if (c < 32 || c > 255) {
                        c = 63; // '?'
                    }
                }
                builder.append(transformPeekvetica(c, transliteratePeekvetica));
            }
        }
        return builder.toString();
    }

    public static boolean validateEmailAddress(String email) {
        boolean validation = false;
        if (email == null)
            return validation;

        if (email.contains(new String(new byte[]{(byte) 0x2C}))
                || email.contains(","))
            return validation;
        if (email.contains(new String(new byte[]{(byte) 0x3B}))
                || email.contains(";"))
            return validation;
        if (email.contains(new String(new byte[]{(byte) 0x25}))
                || email.contains("%"))
            return validation;

        validation = true;
        return validation;
    }

    public static String clean(String message, boolean isTransliterateEmail) {
        return clean(message, isTransliterateEmail, false); //assume peekvetica transform is false
    }

    private static char transformPeekvetica(char oldchar, boolean doTransform) {
        if (!doTransform) {
            //log.debug("transliterateEmailPeekvetica is false, skipping extended transliteration");
            return oldchar;
        }
        char c = oldchar;

        switch (c) {
            case 222:          //thorn (as in thor) character (looks like a P)
            case 254:
                c = 80;        // capital P
                return c;
            case 216:          //zeroslash
                c = 48;        // capital P
                return c;
            case 248:          // oslash
                c = 111;       // lowercase o
                return c;
            case 208:          // D with stroke
                c = 68;        // Capital D
                return c;
            case 164:          // &curren;
                c = 42;        // asterisk?
                return c;
            case 160:          //&nbsp;
                c = 32;        // space
                return c;
            case 210:           //Cap O with accent grave
                c = 79;        //Cap O with no accent
                return c;
            case 226:           //Lower a with circumflex
            case 227:           //Lower a with tilde
            case 228:           //Lower a with umlat
            case 229:           //Lower a with "halo" or "ring"
                c = 97;         //Lower a with no accent
                return c;
            case 230:           //combined ae
                c = 97;         //letter a TODO: how to return the e?
                return c;
            case 242:           //Lower o with accent grave
            case 244:           //Lower o with circumflex
            case 245:           //Lower o with tilde
                c = 111;        //lower o, no accent
                return c;
        }

        //else no transformation
        return oldchar;
    }

    /**
     * (\w|\.|%|/|\-|\+|\=|\?)+@([A-Za-z0-9\-]+\.)+[A-Za-z]+ check email account
     * if validat
     */
    public static boolean validateEmail(String email) {
        boolean validation = false;

        if (email == null)
            return validation;

        Pattern p = Pattern.compile("(\\w|\\.|%|/|\\-|\\+|\\=|\\?)+@([A-Za-z0-9\\-]+\\.)+[A-Za-z]+");
        String[] emails = StringUtils.parseString(email, "");

        for (String email1 : emails) {
            if (!"".equals(email1.trim())) {

                Matcher m = p.matcher(email1.trim());

                validation = m.find();
                if (!validation) break;

            }

        }
        return validation;
    }

    /**
     * @param email
     * @return
     */
    public static String validateAndRemoveEmail(String email) {
        StringBuffer strings = new StringBuffer();

        if (email == null)
            return email;

        Pattern p = Pattern.compile("(\\w|\\.|%|/|\\-|\\+|\\=|\\?)+@([A-Za-z0-9\\-]+\\.)+[A-Za-z]+");
        String[] emails = StringUtils.parseString(email, ";,");

        for (String email1 : emails) {
            if (!"".equals(email1.trim())) {

                Matcher m = p.matcher(email1.trim());

                boolean validation = m.find();
                if (validation) strings.append(email1.trim()).append(",");
            }
        }
        return strings.toString();
    }

    /**
     * ******************************
     * <p/>
     * Creates a cleaned body in the correct encoding
     * if exception occurs
     */
    public static String getBody(Message message, Account account, Device device) throws Exception {
        boolean convertHtml = !account.canHandleHtml();
        boolean isTransliterateEmail = !account.isXobniAccount() && ClientUtils.isDeviceAPeek(device) && MailUtils.isTransliterateEmail;
        boolean isTransliterateEmailPeekvetica = !account.isXobniAccount() && ClientUtils.isDeviceAPeek(device) && MailUtils.isTransliterateEmailPeekvetica;
        String cleanMessage = clean(new MessageParser().parseMsgContent(message, convertHtml), isTransliterateEmail, isTransliterateEmailPeekvetica);
        if (cleanMessage != null) {
            return cleanMessage;
        }
        return "";
    }

    public static String getBody(Message message, Account account) throws Exception {
        Device device = new UserService().getDeviceByUserId(account.getUser_id());
        return getBody(message, account, device);
    }

}
