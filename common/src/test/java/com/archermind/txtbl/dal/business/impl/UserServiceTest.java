package com.archermind.txtbl.dal.business.impl;

import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class UserServiceTest {
    @Test
    public void isTimeStampValid() {
        Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"));

        UserService service = new UserService();

        calendar.set(1900, 10, 10);
        Assert.assertFalse(service.isTimeStampValid(calendar.getTime()));

        calendar.set(2038, 10, 10);
        Assert.assertFalse(service.isTimeStampValid(calendar.getTime()));

        calendar.set(2010, 10, 10);
        Assert.assertTrue(service.isTimeStampValid(calendar.getTime()));

        calendar.set(1970, 0, 02, 00, 00, 01);
        Assert.assertTrue(service.isTimeStampValid(calendar.getTime()));

        calendar.set(2038, 0, 17, 03, 14, 07);
        Assert.assertTrue(service.isTimeStampValid(calendar.getTime()));
    }


}
