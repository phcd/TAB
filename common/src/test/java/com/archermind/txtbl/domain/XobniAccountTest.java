package com.archermind.txtbl.domain;

import org.junit.Assert;
import org.junit.Test;

public class XobniAccountTest {
    @Test
    public void setSyncError() {
        XobniAccount xobniAccount = new XobniAccount();
        Assert.assertEquals(0, xobniAccount.syncFailureCount);
        Assert.assertNull(xobniAccount.lastSyncFailure);
        Assert.assertNull(xobniAccount.lastSyncFailureDate);
        Assert.assertTrue(xobniAccount.isActive());

        xobniAccount.setSyncError("403", 2);
        Assert.assertEquals(1, xobniAccount.syncFailureCount);
        Assert.assertEquals("403", xobniAccount.lastSyncFailure);
        Assert.assertNotNull(xobniAccount.lastSyncFailureDate);
        Assert.assertTrue(xobniAccount.isActive());

        xobniAccount.setSyncError("403", 2);
        Assert.assertEquals(2, xobniAccount.syncFailureCount);
        Assert.assertEquals("403", xobniAccount.lastSyncFailure);
        Assert.assertNotNull(xobniAccount.lastSyncFailureDate);
        Assert.assertTrue(xobniAccount.isActive());

        xobniAccount.setSyncError("403", 2);
        Assert.assertEquals(3, xobniAccount.syncFailureCount);
        Assert.assertEquals("403", xobniAccount.lastSyncFailure);
        Assert.assertNotNull(xobniAccount.lastSyncFailureDate);
        Assert.assertFalse(xobniAccount.isActive());
    }
}
