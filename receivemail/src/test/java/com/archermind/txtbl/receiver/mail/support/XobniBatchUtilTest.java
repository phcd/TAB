package com.archermind.txtbl.receiver.mail.support;

import junit.framework.Assert;
import org.junit.Test;

public class XobniBatchUtilTest {
    @Test
    public void testGetBatchSize() throws Exception {
        Assert.assertEquals(1000, XobniBatchUtil.INSTANCE.getBatchSize(1000, 0, 2000));
        Assert.assertEquals(1000, XobniBatchUtil.INSTANCE.getBatchSize(1000, 1000, 2000));
        Assert.assertEquals(200, XobniBatchUtil.INSTANCE.getBatchSize(1000, 2000, 2200));
        Assert.assertEquals(200, XobniBatchUtil.INSTANCE.getBatchSize(1000, 0, 200));
        Assert.assertEquals(100, XobniBatchUtil.INSTANCE.getBatchSize(1000, 100, 200));
    }

    @Test
    public void testGetStartIndex() throws Exception {
        Assert.assertEquals(1000, XobniBatchUtil.INSTANCE.getStartIndex(0, 1000, false));
        Assert.assertEquals(900, XobniBatchUtil.INSTANCE.getStartIndex(100, 1000, false));
        Assert.assertEquals(1, XobniBatchUtil.INSTANCE.getStartIndex(0, 1000, true));
        Assert.assertEquals(101, XobniBatchUtil.INSTANCE.getStartIndex(100, 1000, true));
        Assert.assertEquals(1001, XobniBatchUtil.INSTANCE.getStartIndex(1000, 1500, true));
        Assert.assertEquals(1, XobniBatchUtil.INSTANCE.getStartIndex(0, 493, true));
    }

    @Test
    public void testGetEndIndex() throws Exception {
        Assert.assertEquals(1001, XobniBatchUtil.INSTANCE.getEndIndex(0, 1000, 2000, false));
        Assert.assertEquals(1, XobniBatchUtil.INSTANCE.getEndIndex(1000, 1000, 2000, false));
        Assert.assertEquals(1000, XobniBatchUtil.INSTANCE.getEndIndex(0, 1000, 2000, true));
        Assert.assertEquals(2000, XobniBatchUtil.INSTANCE.getEndIndex(1000, 1000, 2000, true));
        Assert.assertEquals(1500, XobniBatchUtil.INSTANCE.getEndIndex(1000, 1000, 1500, true));
        Assert.assertEquals(400, XobniBatchUtil.INSTANCE.getEndIndex(0, 400, 493, true));
    }
}
