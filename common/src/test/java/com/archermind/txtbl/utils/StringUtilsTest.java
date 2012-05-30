package com.archermind.txtbl.utils;

import junit.framework.Assert;
import org.junit.Test;

import java.nio.charset.Charset;
import java.util.Map;

public class StringUtilsTest {
    @Test
    public void isEmpty() {
        Assert.assertTrue(StringUtils.isEmpty(" "));
        Assert.assertTrue(StringUtils.isEmpty(null));
        Assert.assertTrue(StringUtils.isEmpty(" \t"));
        Assert.assertFalse(StringUtils.isEmpty("cxadsf adsf"));
        Assert.assertFalse(StringUtils.isEmpty("cxadsf "));

        Assert.assertFalse(org.apache.commons.lang.StringUtils.isEmpty(" "));
        Assert.assertTrue(org.apache.commons.lang.StringUtils.isEmpty(null));
        Assert.assertFalse(org.apache.commons.lang.StringUtils.isEmpty(" \t"));
        Assert.assertFalse(org.apache.commons.lang.StringUtils.isEmpty("cxadsf adsf"));
        Assert.assertFalse(org.apache.commons.lang.StringUtils.isEmpty("cxadsf "));
    }

    @Test
    public void allNotEmpty() {
        Assert.assertTrue(StringUtils.allNotEmpty("asdf", "asdfd"));
        Assert.assertFalse(StringUtils.allNotEmpty("", "asdfd"));
        Assert.assertFalse(StringUtils.allNotEmpty(null, "asdfd"));
        Assert.assertFalse(StringUtils.allNotEmpty("    ", "asdfd"));
    }

    @Test
    public void splitAliasAndAddress() {
        Map<String,String> map = StringUtils.splitAliasAndAddress("Daniel Bryg <dbryg@yahoo.com>");
        Assert.assertEquals("Daniel Bryg", map.get("alias"));
        Assert.assertEquals("dbryg@yahoo.com", map.get("address"));

        map = StringUtils.splitAliasAndAddress("Daniel Bryg");
        Assert.assertNull(map.get("alias"));
        Assert.assertNull(map.get("address"));

        map = StringUtils.splitAliasAndAddress("dbryg@yahoo.com");
        Assert.assertNull(map.get("alias"));
        Assert.assertEquals("dbryg@yahoo.com", map.get("address"));

        map = StringUtils.splitAliasAndAddress("<dbryg@yahoo.com>");
        Assert.assertNull(map.get("alias"));
        Assert.assertEquals("dbryg@yahoo.com", map.get("address"));

        map = StringUtils.splitAliasAndAddress("<dbryg@yahoo.com>  ");
        Assert.assertNull(map.get("alias"));
        Assert.assertEquals("dbryg@yahoo.com", map.get("address"));

        map = StringUtils.splitAliasAndAddress("<dbryg@yahoo.com");
        Assert.assertNull(map.get("alias"));
        Assert.assertNull(map.get("address"));

        map = StringUtils.splitAliasAndAddress("<>");
        Assert.assertNull(map.get("alias"));
        Assert.assertNull(map.get("address"));

        map = StringUtils.splitAliasAndAddress("<dasdf>");
        Assert.assertNull(map.get("alias"));
        Assert.assertEquals("dasdf", map.get("address"));

    }

    @Test
    public void obfuscateCredentialsInJsonString() {
        Assert.assertEquals("", StringUtils.obfuscateCredentialsInJsonString(""));
        Assert.assertEquals("", StringUtils.obfuscateCredentialsInJsonString(null));
        Assert.assertEquals("{'emailId':'paul@getpeek.com', 'password':'*******', 'sessionId': '1qm78litxgCWFuzgQbrhcJ3r2i59RDdbSplYowBh', 'accountType':'xobni'}", StringUtils.obfuscateCredentialsInJsonString("{'emailId':'paul@getpeek.com', 'password':'password', 'sessionId': '1qm78litxgCWFuzgQbrhcJ3r2i59RDdbSplYowBh', 'accountType':'xobni'}"));
        Assert.assertEquals("{'emailId':'paul@getpeek.com', 'password':'*******', 'sessionId' '1qm78litxgCWFuzgQbrhcJ3r2i59RDdbSplYowBh', 'accountType':'xobni'}", StringUtils.obfuscateCredentialsInJsonString("{'emailId':'paul@getpeek.com', 'password':'password', 'sessionId' '1qm78litxgCWFuzgQbrhcJ3r2i59RDdbSplYowBh', 'accountType':'xobni'}"));
        Assert.assertEquals("asdfdsaf", StringUtils.obfuscateCredentialsInJsonString("asdfdsaf"));
        Assert.assertEquals("password", StringUtils.obfuscateCredentialsInJsonString("password"));
        Assert.assertEquals("password:", StringUtils.obfuscateCredentialsInJsonString("password:"));
        Assert.assertEquals("password:'*******'", StringUtils.obfuscateCredentialsInJsonString("password:sadfds"));
    }

    @Test
    public void getEncodedString() {
        Assert.assertEquals("Aasdfds  ?", StringUtils.getEncodedString("Aasdfds  ?"));
        StringUtils.getEncodedString("?!<­!? ©??­^");
    }
}
