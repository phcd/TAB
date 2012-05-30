package com.archermind.txtbl.utils;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class StringUtils {
    public static String scrapePasswordFromQML(String qml)
    {
        if (qml != null)
        {
            return qml.replaceAll("<Password><!\\[CDATA\\[.*\\]\\]></Password>", "<Password><![CDATA[*********]]></Password>");
        }
        else
        {
            return qml;
        }
    }
    public static boolean isNumeric(String target)
    {
        return org.apache.commons.lang.StringUtils.isNumeric(target);
    }

    public static boolean isNotEmpty(String target) {
        return !isEmpty(target);
    }

    public static boolean isEmpty(String string) {
        return string ==  null || string.trim().equals("");
    }
    
	public static String[] parseString(String oldString, String delimiters) {

		StringTokenizer ddd = new StringTokenizer(oldString, delimiters);
		int counts = ddd.countTokens();

		String[] _biiAccounts = new String[counts];
        for (int i = 0; i < _biiAccounts.length; i++) {
            _biiAccounts[i] = (String) ddd.nextElement();
        }
		return _biiAccounts;
	}

	public static String formatPaymentCreateTime(Date date, String parttern) {

		SimpleDateFormat sdf = new SimpleDateFormat(parttern);
		return sdf.format(date);
	}

	public static Date checkExpiryDate(String expiryDate, String parttern) {
		SimpleDateFormat sdf = new SimpleDateFormat(parttern);
		try {
			Date date = sdf.parse(expiryDate);
            return new Date(date.getTime());
		} catch (ParseException e) {
			e.printStackTrace();
		}
        return null;
	}

	/***************************************************************************
	 * 
	 * @param data
	 * @return
	 */
	public static String dumpBytes(byte[] data) {
		StringBuffer strings = new StringBuffer();
		strings.append("\n");
		for (int i = 0; i < data.length; i++) {

			strings.append(String.format("0x%-2X,", data[i]));

			if ((i + 1) % 30 == 0)
				strings.append("\n");

		}

		return strings.toString();
	}

	/**
	 * 
	 * @param encrypt
	 * @param keys
	 * @return
	 */
	public static String decryption(String encrypt) {
		byte[] passbyte = Base64.decode(encrypt);
		if (passbyte == null || passbyte.length == 0)
			try {
				passbyte = encrypt.getBytes("utf-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return new String(passbyte);

	}

	/**
	 * A6----7C (124) 0x95---2E(46)
	 * 
	 * @param originalxml
	 * @return
	 */
	public static String replaceSpecialChars(String originalxml) {

		try {
			originalxml = originalxml.replaceAll(new String(
					new byte[] { (byte) 0xA6 }, "UTF-8"), new String(
					new byte[] { 0x7C }, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			originalxml = originalxml.replaceAll(new String(
					new byte[] { (byte) 0x95 }, "UTF-8"), new String(
					new byte[] { 0x2E }, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return originalxml;
	}

	public static String replaceCommaAndSemicolon(String originalxml) {
		if (originalxml == null)
			return originalxml;
		try {
			originalxml = originalxml.replaceAll(new String(new byte[] { (byte) 0x2C }, "UTF-8"), "");   //comma
			originalxml = originalxml.replaceAll(",", "");

			originalxml = originalxml.replaceAll(new String(new byte[] { (byte) 0x3B }, "UTF-8"), "");   //semi colon
			originalxml = originalxml.replaceAll(";", "");
            
			originalxml = originalxml.replaceAll(new String(new byte[] { (byte) 0x25 }, "UTF-8"), "");   //percent
			originalxml = originalxml.replaceAll("%", "");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return originalxml;
	}

	public static String unvisblepassword(String originalxml) {
		if (originalxml == null)
			return originalxml;
		String PASSWORD_BEGINTAG = "<Password>";
		String PASSWORD_ENDTAG = "</Password>";
		StringBuffer strings = new StringBuffer();

		int ibeginpos = originalxml.indexOf(PASSWORD_BEGINTAG);

		if (ibeginpos < 0)
			return originalxml;

		ibeginpos += PASSWORD_BEGINTAG.length();

		int iendpos = originalxml.indexOf(PASSWORD_ENDTAG);

		if (iendpos < 0 || iendpos <= ibeginpos)
			return originalxml;

		strings.append(originalxml.subSequence(0, ibeginpos));

		strings.append("********");
		strings.append(originalxml.substring(iendpos));
		return strings.toString();

	}

	// <Data><!
	// </Data>

	public static String unvisbleEmailBody(String originalxml) {
		if (originalxml == null)
			return originalxml;
		String PASSWORD_BEGINTAG = "<Data>";
		String PASSWORD_ENDTAG = "</Data>";
		StringBuffer strings = new StringBuffer();

		int ibeginpos = originalxml.indexOf(PASSWORD_BEGINTAG);

		if (ibeginpos < 0)
			return originalxml;

		ibeginpos += PASSWORD_BEGINTAG.length();

		int iendpos = originalxml.indexOf(PASSWORD_ENDTAG);

		if (iendpos < 0 || iendpos <= ibeginpos)
			return originalxml;

		strings.append(originalxml.subSequence(0, ibeginpos));

		strings.append("Hidden body");
		strings.append(originalxml.substring(iendpos));
		return strings.toString();

	}

	// <Bill><Account>4828512951754023$044$lastname$firstname$27513$amount$102011</Account><StartDate>
	public static String unvisblePayAccount(String originalxml) {
		if (originalxml == null)
			return originalxml;
		String PASSWORD_BEGINTAG = "<Bill><Account>";
		String PASSWORD_ENDTAG = "</Account><StartDate>";
		StringBuffer strings = new StringBuffer();

		int ibeginpos = originalxml.indexOf(PASSWORD_BEGINTAG);

		if (ibeginpos < 0)
			return originalxml;

		ibeginpos += PASSWORD_BEGINTAG.length();

		int iendpos = originalxml.indexOf(PASSWORD_ENDTAG);

		if (iendpos < 0 || iendpos <= ibeginpos)
			return originalxml;

		strings.append(originalxml.subSequence(0, ibeginpos));
		// strings.append("you cannot email body ^-^ on the track log!");
		strings.append(originalxml.substring(iendpos));
		return strings.toString();

	}

	public static String unvisbleMeta(String originalxml) {
		if (originalxml == null)
			return originalxml;
		String PASSWORD_BEGINTAG = "<Meta>";
		String PASSWORD_ENDTAG = "</Meta>";
		StringBuffer strings = new StringBuffer();

		int ibeginpos = originalxml.indexOf(PASSWORD_BEGINTAG);

		if (ibeginpos < 0)
			return originalxml;

		ibeginpos += PASSWORD_BEGINTAG.length();

		int iendpos = originalxml.indexOf(PASSWORD_ENDTAG);

		if (iendpos < 0 || iendpos <= ibeginpos)
			return originalxml;

		strings.append(originalxml.subSequence(0, ibeginpos));
		// strings.append("you cannot email body ^-^ on the track log!");
		strings.append(originalxml.substring(iendpos));
		return strings.toString();

	}



	public static String unvisbleContent(String originalxml) {
		originalxml = unvisbleMeta(originalxml);
		originalxml = unvisblePayAccount(originalxml);
		return unvisbleEmailBody(originalxml);
	}

    /**
     * Returns file extension ,if file doesn't have extension returns empty string.
     *
     * @param fileName name of file
     * @return extension
     */
    public static String getFileExtension(String fileName) {
        //... Find the position of the last dot.  Get extension.
        int dotPos = fileName.lastIndexOf(".");
        return dotPos != -1 ? fileName.substring(dotPos + 1) : "";
    }

    /**
     *  Returns shortened file name, if file name length greater than sizeLimit else original file name .
     *
     * @param fileName name of file
     * @param sizeLimit max length of fileName.
     * @return processed file name
     */
    public static String getShortFileName(String fileName, int sizeLimit) {
        String result = fileName;
        if (fileName.length() > sizeLimit) {
            String extension = getFileExtension(fileName);
            int index = sizeLimit - extension.length() - 3; // 3 == "...".length()
            result = fileName.substring(0, index) + "..." + extension;
        }
        return result;
    }

    /**
     * Returns true if the specified version is "among" the supported versions. If for example the specified version is
     * "Ex:01.09.15", and "Ex:01.09" is a supported version, we return true.
     *
     * @param version The version to check
     * @param supportedVersionString The supported versions string, for example: "Ex:01.09, Ex:01.10, Alpine"
     * @return
     */
    public static boolean isVersionSupported(String version, String supportedVersionString) {
        if(StringUtils.isEmpty(version)) {
            return false;
        }
        
        String[] supportedVersions = supportedVersionString.split(",");

        for (String supportedVersion : supportedVersions) {
            if (version.contains(supportedVersion.trim())) {
                return true;
            }
        }
        return false;
    }

    public static boolean equals(String firstString, String secondString) {
        return isEmpty(firstString) && isEmpty(secondString) || firstString.equals(secondString);
    }

    /**
     * JunitTest
     *
     * @param args
     */
    public static void main(String[] args) {

        String testStr = "womensweardaily WWD ?????? http://cli.gs/z90phs";

        for (int i = 0; i < testStr.length(); i++) {
            int c = (int) testStr.charAt(i);
            System.out.println(testStr.charAt(i) + "(" + c + ")");
        }

        System.out.println("Before: " + testStr);
        System.out.println(" After: " + StringUtils.replaceSpecialChars(testStr));

        Date ddd = StringUtils.checkExpiryDate("062008", "MMyyyy");
        System.out.println(ddd);
        System.out.println(" validateEmail");

        System.out
                .println(StringUtils.formatPaymentCreateTime(new Date(), "MMDDyyySSssss"));

        String[] cards = StringUtils
                .parseString(
                        "daniel@getpeek.com;;,daniel@getpeek.com;;,daniel@getpeek.com;;",
                        ",;");
        String xxxxCredCard = "";

        for (int i = 0; i < cards.length - 1; i++) {
            cards[i] = "xxxxx";
            xxxxCredCard += "xxxxx-";
        }

        System.out.println(xxxxCredCard + cards[cards.length - 1]);

        System.out
                .println(StringUtils.unvisblepassword("asdasdfpassword>12344666</password>asdfasdfasdf"));

        System.out
                .println(StringUtils.unvisblepassword("asdasdf<Password>12344666</Password>asdfasdfasdf"));

        // alias
        System.out.println(StringUtils.replaceCommaAndSemicolon("archermind df.fgh;fgh "));

        //
    }

    public static String getNameFromLink(String url, int maxLength)
    {
        if (url != null)
        {
            url = url.replaceAll("http://www.", "");
            url = url.replaceAll("https://www.", "");
            url = url.replaceAll("[^a-zA-Z0-9]", "");

            if (url.length() > maxLength)
            {
                return url.substring(0, maxLength);
            }
            else
            {
                return url;
            }
        }
        else
        {
            return url;
        }

    }

    public static boolean allNotEmpty(String... params) {
        boolean valid = true;
        for (String param : params) {
            valid = valid && !StringUtils.isEmpty(param);
        }
        return valid;
    }


    public static Map<String, String> splitAliasAndAddress(String addressString) {
        Map<String, String> aliasAddressMap = new HashMap<String, String>();
        try {
            InternetAddress internetAddress = new InternetAddress(addressString);
            String alias = internetAddress.getPersonal();
            String address = internetAddress.getAddress();
            aliasAddressMap.put("alias", alias);
            aliasAddressMap.put("address", address);
        } catch (AddressException ignored) {}
        return aliasAddressMap;
    }

    public static String obfuscateCredentialsInJsonString(String jsonString) {
        if(StringUtils.isEmpty(jsonString)) {
            return "";
        }

        String[] jsonParts = jsonString.split(",");
        for (int i = 0; i < jsonParts.length; i++) {
            String jsonPart = jsonParts[i];
            String[] params = jsonPart.split(":");
            if(params != null && params.length == 2) {
                if(params[0].contains("password") || params[0].contains("oauth")) {
                    params[1] = "'*******'";
                    jsonParts[i] = org.apache.commons.lang.StringUtils.join(params, ":");
                }

            }
        }
        return org.apache.commons.lang.StringUtils.join(jsonParts, ",");        
    }

    public static String trimToEmpty(String string) {
        return org.apache.commons.lang.StringUtils.trimToEmpty(string);
    }

    public static String getEncodedString(String string) {
        try {
            return new String(string.getBytes("UTF-8"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "";
        } catch (Exception e) {
            return "";
        }
    }

    public static String emptyStringIfNull(String string) {
        return string != null ? string : "";
    }
}
