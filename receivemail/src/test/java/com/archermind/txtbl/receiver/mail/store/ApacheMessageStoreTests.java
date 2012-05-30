package com.archermind.txtbl.receiver.mail.store;

import com.archermind.txtbl.mail.store.ApacheMessageStore;
import com.archermind.txtbl.mail.store.MessageStoreException;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class ApacheMessageStoreTests extends TestCase
{

    public void testGetMessages() throws MessageStoreException
    {
        ApacheMessageStore store = new ApacheMessageStore();

        
    }

    public void testManyUsers() throws MessageStoreException
    {
        ApacheMessageStore store = new ApacheMessageStore();

        /*


        long testStart = System.currentTimeMillis();

        for (int i = 3456789; i < 3466789; i++)
        {
            String bucket = store.getBucket(i);
            
            for (int m = 987654321; m < 987654321 + 10000; m++)
            {
                store.addMessage(i, bucket, "gmail" + m + "-" + "uc" + m);
            }
        }

        System.out.printf("added 10,000 users with 10,000 messages in %d millis\n", (System.nanoTime()-testStart) / 1000000);


*/
        long testStart = System.currentTimeMillis();

        for (int i = 3456789; i < 3456789 + 100; i++)
        {
            String bucket = store.getBucket(i);

            for (int m = 987654321; m < 987654321 + 10000; m++)
            {
                assertTrue(store.hasMessage(i, bucket, "gmail" + m + "-" + "uc" + m, null));
            }
        }

        System.out.printf("checked 10,000 users with 10,000 messages in %d millis\n", (System.nanoTime() - testStart) / 1000000);

    }

    public void testAccountDelete() throws MessageStoreException, IOException
    {
        int accountId = 23456;

        ApacheMessageStore store = new ApacheMessageStore();

        store.deleteAllMessages(accountId, null);
    }

    public void testAdd() throws MessageStoreException
    {
        int accountId = 23456;

        ApacheMessageStore store = new ApacheMessageStore();

        store.addMessage(accountId, store.getBucket(accountId), "gnmail123123213", null);
    }

    public void testSimple() throws MessageStoreException
    {

        for (int i = 0; i < 10000; i++)
        {
            File file = new File(new File("C:\\Apache\\Apache2.2\\htdocs\\messageids\\6\\65\\654\\6543\\65432"), "gmail" + i + "-ul" + i);

            if (!file.exists())
            {
                try
                {
                    file.createNewFile();
                }
                catch (IOException e)
                {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
            else
            {
                file.delete();
            }

        }
        int accountId = 23456;

        ApacheMessageStore store = new ApacheMessageStore();

        String bucket = store.getBucket(accountId);

        for (int i = 0; i < 1000; i++)
        {
            store.hasMessage(accountId, bucket, "gmail" + i, null);
        }

    }
}
