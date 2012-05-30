package com.archermind.txtbl.taskfactory.loopnotice;

import com.archermind.txtbl.domain.Account;
import org.junit.Assert;
import org.junit.Test;

public class IdleDestinationInstanceTest {
    @Test
    public void getDestinationUrl() {
        IdleDestinationInstance destinationInstance = new IdleDestinationInstance("5;127.0.0.1:1099;8", null, null, 2);
        Assert.assertEquals("127.0.0.1:1099", destinationInstance.getDestinationUrl(new Account()));

        destinationInstance = new IdleDestinationInstance("5;127.0.0.1:1099,10.0.0.1:1100;8", null, null, 2);
        Account account = new Account();
        account.setId(12);
        Assert.assertEquals("127.0.0.1:1099", destinationInstance.getDestinationUrl(account));
        account.setId(10);
        Assert.assertEquals("10.0.0.1:1100", destinationInstance.getDestinationUrl(account));

        destinationInstance = new IdleDestinationInstance("5;127.0.0.1:1099,10.0.0.1:1100;8", null, null, 1);
        account.setId(10);
        Assert.assertEquals("127.0.0.1:1099", destinationInstance.getDestinationUrl(account));
        account.setId(11);
        Assert.assertEquals("10.0.0.1:1100", destinationInstance.getDestinationUrl(account));

        destinationInstance = new IdleDestinationInstance("5;127.0.0.1:1099,10.0.0.1:1100,10.0.0.2:1100;8", null, null, 2);
        account.setId(12);
        Assert.assertEquals("127.0.0.1:1099", destinationInstance.getDestinationUrl(account));
        account.setId(20);
        Assert.assertEquals("10.0.0.1:1100", destinationInstance.getDestinationUrl(account));
        account.setId(10);
        Assert.assertEquals("10.0.0.2:1100", destinationInstance.getDestinationUrl(account));


    }

    @Test
    public void getDestinationName() {
        String destinationName = "newimapidle";
        IdleDestinationInstance destinationInstance = new IdleDestinationInstance("5;127.0.0.1:1099;8", null, destinationName, 2);
        Assert.assertEquals(destinationName, destinationInstance.getDestinationName(new Account()));

        destinationInstance = new IdleDestinationInstance("5;127.0.0.1:1099,10.0.0.1:1100;8", null, destinationName, 2);
        Account account = new Account();
        account.setId(12);
        Assert.assertEquals(destinationName + "-0" , destinationInstance.getDestinationName(account));
        account.setId(10);
        Assert.assertEquals(destinationName + "-1", destinationInstance.getDestinationName(account));

        destinationInstance = new IdleDestinationInstance("5;127.0.0.1:1099,10.0.0.1:1100;8", null, destinationName, 1);
        account.setId(10);
        Assert.assertEquals(destinationName + "-0", destinationInstance.getDestinationName(account));
        account.setId(11);
        Assert.assertEquals(destinationName + "-1", destinationInstance.getDestinationName(account));

        destinationInstance = new IdleDestinationInstance("5;127.0.0.1:1099,10.0.0.1:1100,10.0.0.2:1100;8", null, destinationName, 2);
        account.setId(12);
        Assert.assertEquals(destinationName + "-0", destinationInstance.getDestinationName(account));
        account.setId(20);
        Assert.assertEquals(destinationName + "-1", destinationInstance.getDestinationName(account));
        account.setId(10);
        Assert.assertEquals(destinationName + "-2", destinationInstance.getDestinationName(account));


    }
}
