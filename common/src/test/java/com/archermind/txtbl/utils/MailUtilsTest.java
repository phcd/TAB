package com.archermind.txtbl.utils;

import org.junit.Test;
import static org.junit.Assert.*;
import junit.framework.*;
import com.archermind.txtbl.domain.Email;


/**
 * Created by IntelliJ IDEA.
 * User: kevin.wanminkee
 * Date: Jun 16, 2010
 * Time: 7:20:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class MailUtilsTest
{

    @Test
    public void testCleanNullString()
    {
        String msg = null;
        String output = MailUtils.clean(msg,true, true);

        assertNull(msg, output);
    }

    @Test
    public void testCleanEmptyString()
    {
        String msg = "";
        String output = MailUtils.clean(msg,true, true);

        assertEquals("Empty string not handled correctly", msg, output);
    }
}
