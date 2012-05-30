package com.archermind.txtbl.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class IdUtilTest {
    @Test
    public void getMessageIdString() {
        Assert.assertEquals(" 1\r\n 2\r\n 3", IdUtil.encodeMessageIds(Arrays.asList(1,2,3)));
        Assert.assertEquals(" 1\r\n 2\r\n 3", IdUtil.encodeMessageIds(Arrays.asList("1 1","2 2","2 3")));
        Assert.assertEquals(" 1289068730.6662.mailjk1.mailnet.ptd.net%2CS%3D2758\r\n %3Csecond%3E\r\n third%2C%23", IdUtil.enodePop3MessageIdString("1 1289068730.6662.mailjk1.mailnet.ptd.net,S=2758\r\n2 <second>\r\n3 third,#"));
    }
}
