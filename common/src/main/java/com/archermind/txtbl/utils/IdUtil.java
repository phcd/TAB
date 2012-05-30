package com.archermind.txtbl.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IdUtil {

    private static final String UTF_8_ENCODING = "UTF-8";
    
    public static String encodeMessageIds(List ids) {
        List<String> encodedIds  = new ArrayList<String>();
        for (Object id : ids) {
            String idString = id.toString();
            String[] idParts = idString.split(" ");
            idString = (idParts.length == 2) ? idParts[1] : idParts[0];
            encodedIds.add(encode(idString));
        }
        return " " + org.apache.commons.lang.StringUtils.join(encodedIds, "\r\n ");
    }

    public static String enodePop3MessageIdString(String idString) {
        return encodeMessageIds(Arrays.asList(idString.split("\r\n")));
    }

    public static String encode(String s) {
        try {
            return URLEncoder.encode(s, UTF_8_ENCODING);
        } catch (UnsupportedEncodingException e) {
            //
        }
        return s;
    }

    public static String decode(String s) {
       try {
            return URLDecoder.decode(s, UTF_8_ENCODING);
        } catch (UnsupportedEncodingException e) {
            //
        }
        return s;
    }

}
