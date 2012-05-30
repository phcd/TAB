package com.archermind.txtbl.receiver.mail.abst.impl;

import com.archermind.txtbl.validate.mailbox.abst.impl.HotMailSupport;
import junit.framework.TestCase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.commons.httpclient.methods.PostMethod;

public class MspTests extends TestCase
{

    public void testMSN() throws Exception
    {
        DefaultHttpClient httpClient = new DefaultHttpClient();

        HotMailSupport hotMailSupport = new HotMailSupport();

        String loginStr = hotMailSupport.login(httpClient, "kevinmsnpeek2@msn.com", "txtbl123");

        System.out.println(loginStr);

    }
}
