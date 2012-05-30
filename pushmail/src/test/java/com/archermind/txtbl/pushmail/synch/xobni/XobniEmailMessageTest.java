package com.archermind.txtbl.pushmail.synch.xobni;

import com.archermind.txtbl.dal.DALException;
import org.junit.Test;

public class XobniEmailMessageTest {
    @Test
    public void getXobniMessageString() throws DALException {
       /* Email email = new Email();
        String to = "jmpak80@gmail.com";
        email.setTo(to);
        email.setFrom("paul@getpeek.in");
        email.setFrom_alias("Manuel Paul");
        email.setSubject("Test message");
        email.setBodySize(100);
        email.setMaildate("2010-01-01 10:20:01");
        email.setOriginal_account("jmpak80@gmail.com");
        email.setMailid(123);
        email.setMessageId("MessageId");

        XobniEmailMessage message = new XobniEmailMessage("1111",email);
        JSONObject messageObject = message.getXobniMessageObject();
        Assert.assertEquals(to, messageObject.get("account"));
        Assert.assertEquals("MessageId", messageObject.get("messageId"));
        Assert.assertNotNull(messageObject.get("delivery-time"));
        Assert.assertEquals("Manuel Paul", ((LinkedHashMap)messageObject.get("from")).get("name"));
        Assert.assertNull(messageObject.get("cc"));

        email.setCc("kevin@txtbl.com;Daniel Bryg <dbryg@yahoo.com>;<paul@getpeek.in");
        message = new XobniEmailMessage("100",email);
        messageObject = message.getXobniMessageObject();
        List ccList = (List) messageObject.get("cc");
        JSONObject cc = (JSONObject) ccList.get(0);
        Assert.assertNull(cc.get("name"));
        Assert.assertEquals("kevin@txtbl.com", cc.get("smtp"));

        cc = (JSONObject) ccList.get(1);
        Assert.assertEquals("Daniel Bryg", cc.get("name"));
        Assert.assertEquals("dbryg@yahoo.com", cc.get("smtp"));

        cc = (JSONObject) ccList.get(2);
        Assert.assertNull(cc.get("name"));
        Assert.assertEquals("<paul@getpeek.in", cc.get("smtp"));

        System.out.println(message.getXobniMessageString());  */
    }
}
