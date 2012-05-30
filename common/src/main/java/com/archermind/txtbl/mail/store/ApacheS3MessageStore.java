package com.archermind.txtbl.mail.store;

import com.archermind.txtbl.domain.Country;
import com.archermind.txtbl.utils.AmazonS3;
import com.archermind.txtbl.utils.StopWatch;
import org.jboss.logging.Logger;
import org.jets3t.service.S3ServiceException;

import java.util.HashSet;
import java.util.Set;

public class ApacheS3MessageStore extends ApacheMessageStore
{
    private static final Logger logger = Logger.getLogger(ApacheS3MessageStore.class);

    private long lastApacheFailure = 0;

    @Override
    public boolean hasMessage(Integer accountId, String bucket, String messageId, Country country) throws MessageStoreException
    {
        long start = System.nanoTime();

        boolean exists = false;

        try
        {
            logger.debug(String.format("checking if message %s exists in apache for account %s in %s", messageId, accountId, bucket));

            boolean isFailover = isFailover();

            if (!isFailover)
            {
                try
                {
                    exists = super.hasMessage(accountId, bucket, messageId, country);
                }
                catch (MessageStoreException ex)
                {
                    logger.fatal(String.format("apache store is for account=%s failing, will fallback on s3", accountId), ex);

                    lastApacheFailure = System.currentTimeMillis();
                }
            }

            isFailover = isFailover();

            if (!exists || isFailover)
            {
                try
                {
                    logger.debug(String.format("checking if message %s exists in s3 for account %s in %s", messageId, accountId, bucket));

                    if (AmazonS3.getInstance().hasMessage(accountId, messageId))
                    {
                        if (!isFailover)
                        {
                            super.addMessage(accountId, bucket, messageId, country);
                        }

                        return true;
                    }
                    else
                    {
                        return false;
                    }
                }
                catch (S3ServiceException e)
                {
                    throw new MessageStoreException(String.format("Unable to check if message id %s exists in s3 for account %s", messageId, accountId));
                }
            }
        }
        finally
        {
            logger.debug(String.format("checking for existance for messsage %s for account %s in %s took %dms", messageId, accountId, bucket, (System.nanoTime() - start) / 1000000));
        }

        return exists;
    }

    @Override
    public boolean addMessage(Integer accountId, String bucket, String messageId, Country country) throws MessageStoreException
    {
        if (! isFailover())
        {
            try
            {
                if (! super.addMessage(accountId, bucket, messageId, country))
                {
                    // looks like add was not succesful as resource already exists
                    return false;
                }

            }
            catch (MessageStoreException ex)
            {
                logger.fatal(String.format("Unable to add message to apache for %s in %s", accountId, bucket), ex);

                lastApacheFailure = System.currentTimeMillis();
            }
        }

        try
        {
            if (! AmazonS3.getInstance().addMessage(accountId, messageId))
            {
                // looks like add was not succesful as resource already exists
                return false;
            }
        }
        catch (S3ServiceException e)
        {
            throw new MessageStoreException(String.format("Unable to add message %s to s3 for account %s", messageId, accountId));
        }

        return true;
    }

    @Override
    public void deleteMessage(Integer accountId, String bucket, String messageId, Country country) throws MessageStoreException
    {
        super.deleteMessage(accountId, bucket, messageId, country);

        try
        {
            AmazonS3.getInstance().deleteMessage(accountId, messageId);
        }
        catch (S3ServiceException e)
        {
            throw new MessageStoreException(String.format("Unable to delete message %s to s3 for account %s", messageId, accountId));
        }
    }

    @Override
    public void deleteAllMessages(Integer accountId, Country country) throws MessageStoreException
    {
        try
        {
            super.deleteAllMessages(accountId, country);

            AmazonS3.getInstance().deleteAllMessages(accountId);
        }
        catch (S3ServiceException e)
        {
            throw new MessageStoreException(String.format("Unable to  delete all message for account %s",  accountId));
        }
    }

    @Override
    public boolean reconcileIds(Integer accountId, String bucket, String messageIds, Country country) {
        return true;
    }

    @Override
    public void addMessageInBulk(Integer accountId, String bucket, String messageIds, Country country) throws MessageStoreException
    {
        if (! isFailover())
        {
            try
            {
                super.addMessageInBulk(accountId, bucket, messageIds, country);
            }
            catch (MessageStoreException ex)
            {
                logger.fatal(String.format("unable to add messages in bulk is for account=%s, will fallback on s3", accountId), ex);
            }
        }

        try
        {
            AmazonS3.getInstance().addMessagesInBulk(accountId, messageIds);
        }
        catch (S3ServiceException e)
        {
            throw new MessageStoreException(String.format("Unable to add messages in bulk to s3 %s for account %s", bucket, accountId));
        }
    }

    public Set<String> getMessages(Integer accountId, Country country) throws MessageStoreException
    {
        if (! isFailover())
        {
            try
            {
                return super.getMessages(accountId, country, "", new StopWatch());
            }
            catch (MessageStoreException ex)
            {
                logger.fatal(String.format("apache store is for account=%s failing, will fallback on s3", accountId), ex);

                lastApacheFailure = System.currentTimeMillis();
            }
        }

        try
        {
            return AmazonS3.getInstance().retrieveMessageIds(accountId);
        }
        catch (S3ServiceException e)
        {
            throw new MessageStoreException(String.format("Unable to get messages from s3 for account %s", accountId));
        }
    }

    private boolean isFailover()
    {
        if (lastApacheFailure == 0 || System.currentTimeMillis() - lastApacheFailure > 30000l)
        {
            return false;
        }
        else
        {
            logger.warn(String.format("message store is in failover mode, will retry connecting in %d ms", System.currentTimeMillis() - lastApacheFailure));
            return true;
        }
    }

    @Override
    public void reconcile(Integer accountId)
    {
        long start = System.nanoTime();

        logger.info(String.format("attempting to reconcile message store for account=%s", accountId));

        int missingInS3 = 0;
        int missingInApache = 0;

        try
        {
            Set<String> s3Ids = AmazonS3.getInstance().retrieveMessageIds(accountId);
            Set<String> apacheIds = super.getMessages(accountId, null, "", new StopWatch());

            Set<String> gap = new HashSet<String>(apacheIds);
            // let's find the gap, whatever is left in the set is what needs to be added to s3
            gap.removeAll(s3Ids);

            if (gap.size() > 0)
            {
                missingInS3 = gap.size();

                logger.info(String.format("Identified gap between apache and s3. %d ids are missing in s3 for account=%d", gap.size(), accountId));

                int count = 0;

                for (String id : gap)
                {
                    if (count++ > 100) break;

                    try {
                        AmazonS3.getInstance().addMessage(accountId, id);
                    } catch (S3ServiceException e) {
                        logger.fatal(String.format("error adding message=%s for %s", id, accountId));
                        throw e;
                    }
                }
            }

            // now let's do the reverse
            gap = new HashSet<String>(s3Ids);

            gap.removeAll(apacheIds);

            if (gap.size() > 0)
            {
                int count = 0;

                missingInApache = gap.size();

                String bucket = super.getBucket(accountId);

                logger.info(String.format("Identified gap between s3 and apache. %d ids are missing in apache for account=%d", gap.size(), accountId));


                for (String id : gap)
                {
                    if (count++ > 100) break;

                    super.addMessage(accountId, bucket, id, null);
                }
            }
        }
        catch (S3ServiceException e)
        {
            logger.fatal(String.format("unable to reconcile for %d, s3 error", accountId), e);

        }
        catch (MessageStoreException e)
        {
            logger.fatal(String.format("unable to reconcile for %d, s3 error", accountId), e);
        }
        finally
        {
            logger.info(String.format("Completed reconciliation for account=%d in %s millis. missingInS3=%d, missingInApache=%d", accountId, (System.nanoTime() - start) / 1000000, missingInS3, missingInApache));
        }


    }
}
