package com.archermind.txtbl.mail.store;

import com.archermind.txtbl.domain.Country;
import com.archermind.txtbl.utils.IdUtil;
import com.archermind.txtbl.utils.StopWatch;
import com.archermind.txtbl.utils.StopWatchUtils;
import com.archermind.txtbl.utils.SysConfigManager;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ApacheMessageStore implements MessageStore
{
    private static final int MAX_RETRIES = Integer.valueOf(SysConfigManager.instance().getValue("messageStoreRetryCount", "1"));
    private static final int CONNECTION_TIMEOUT = Integer.valueOf(SysConfigManager.instance().getValue("messageStoreConnectionTimeout", "5000"));
    private static final int BULK_CONNECTION_TIMEOUT = Integer.valueOf(SysConfigManager.instance().getValue("bulkMessageStoreConnectionTimeout", "10000"));
    private static final int MAX_CONNECTIONS_PER_CLIENT = Integer.valueOf(SysConfigManager.instance().getValue("messageStoreMaxConnectionsPerClient", "15000"));

    private static final String SLASH = "/";
    private static final Logger log = Logger.getLogger(ApacheMessageStore.class);

    private HttpClient putHttpClient;
    private HttpClient getHttpClient;
    private HttpClient deleteHttpClient;
    private HttpClient peekHttpClient;

    private static final String MESSAGEID_STORE_URL_KEY = "messageid.store.url";

    public ApacheMessageStore()
    {

        peekHttpClient = new HttpClient(getConnectionManager());
        putHttpClient = new HttpClient(getConnectionManager());
        getHttpClient = new HttpClient(getConnectionManager());
        deleteHttpClient = new HttpClient(getConnectionManager());


        peekHttpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(MAX_RETRIES, true));
        putHttpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(MAX_RETRIES, true));
        getHttpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(MAX_RETRIES, true));
        deleteHttpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(MAX_RETRIES, true));

        peekHttpClient.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, CONNECTION_TIMEOUT);
        putHttpClient.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, CONNECTION_TIMEOUT);
        getHttpClient.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, CONNECTION_TIMEOUT);
        deleteHttpClient.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, CONNECTION_TIMEOUT);
    }

    public boolean hasMessage(Integer accountId, String bucket, String messageId, Country country) throws MessageStoreException
    {
        String storeUrl = getStoreUrl(country);
        String url = storeUrl + SLASH + bucket + SLASH+ IdUtil.encode(messageId);
        log.info(String.format("fetching message for %d with url %s and message-id %s",accountId,url,IdUtil.encode(messageId)));

        GetMethod method = new GetMethod(url);

        try
        {
            int returnCode = execute(method, getHttpClient);

            return returnCode == HttpStatus.SC_OK;
        }
        catch (IOException e)
        {
            throw new MessageStoreException(String.format("Unable to check existence of message %s in apache at %s for account=%s", messageId, url, accountId), e);
        }
    }

    public void addMessageInBulk(Integer accountId, String bucket, String messageIds, Country country) throws MessageStoreException {
        String storeUrl = getStoreUrl(country);
        String url = storeUrl + SLASH + bucket + SLASH + "bulk";
        putMessages(accountId, messageIds, url);
    }

    private boolean putMessages(Integer accountId, String messageIds, String url) throws MessageStoreException {
        long nanos = System.nanoTime();

        int returnCode = 0;

        try
        {
            PutMethod method = new PutMethod(url);
            method.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, BULK_CONNECTION_TIMEOUT);

            method.setRequestEntity(new StringRequestEntity(messageIds,null,null));

            try
            {
                returnCode = putHttpClient.executeMethod(method);
                boolean success = returnCode == HttpStatus.SC_CREATED;
                if(!success) {
                   log.error(String.format("%s for accountId %s - failed with return code %s", method.getURI(), accountId, returnCode));
                }
                return success;
            }
            finally
            {
                method.releaseConnection();

                log.debug(String.format("%s for %s returned %s completed in %d", method.getClass().getName(), method.getURI(), returnCode, (System.nanoTime() - nanos) / 1000000l));
            }
        }
        catch (IOException e)
        {
            throw new MessageStoreException(String.format("Unable to add new messages to %s for account=%s", url, accountId), e);
        }
    }

    public boolean reconcileIds(Integer accountId, String bucket, String messageIds, Country country) throws MessageStoreException {
        String storeUrl = getStoreUrl(country);
        String url = storeUrl + SLASH + bucket + SLASH + "reconcile";

        return putMessages(accountId, messageIds, url);
    }

    public boolean addMessage(Integer accountId, String bucket, String messageId, Country country) throws MessageStoreException
    {
        String storeUrl = getStoreUrl(country);
        String url = storeUrl + SLASH + bucket + SLASH;
        log.info(String.format("adding message for %d with url %s",accountId,url));

        try
        {
            return put(url, IdUtil.encode(messageId));
        }
        catch (IOException e)
        {
            throw new MessageStoreException(String.format("Unable to add new message %s to %s for account=%s", messageId, url, accountId), e);
        }
    }

    public void deleteMessage(Integer accountId, String bucket, String messageId, Country country) throws MessageStoreException
    {
        String storeUrl = getStoreUrl(country);
        String url = storeUrl + SLASH + bucket + SLASH + IdUtil.encode(messageId);

        try
        {
            delete(url, IdUtil.encode(messageId));
        }
        catch (IOException e)
        {
            throw new MessageStoreException(String.format("Unable to delete message %s from url %s account=%s", messageId, url, accountId), e);
        }
    }

    public void deleteAllMessages(Integer accountId, Country country) throws MessageStoreException
    {
        String storeUrl = getStoreUrl(country);
        String url = storeUrl + SLASH + getBucket(accountId);

        try
        {
            delete(url, null);
        }
        catch (IOException e)
        {
            throw new MessageStoreException(String.format("Unable to delete all messages for account=%s", accountId), e);
        }
    }

    public Set<String> getMessages(Integer accountId, Country country,String context, StopWatch watch) throws MessageStoreException
    {

        if(log.isTraceEnabled())
            log.trace(String.format("getMessages(accountId=%s", accountId));

        String storeUrl = getStoreUrl(country);
        String url = storeUrl + SLASH + getBucket(accountId) + SLASH +"ids";

        if(log.isTraceEnabled())
            log.trace("url="+url);

        GetMethod method = new GetMethod(url);

        Set<String> ids = new HashSet<String>();

        long start = System.nanoTime();

        try
        {
            StopWatchUtils.newTask(watch, "getHttpClient.executeMethod", context, log);
            int returnCode = getHttpClient.executeMethod(method);

            if (returnCode == HttpStatus.SC_OK)
            {
                StopWatchUtils.newTask(watch,"getResponseAsString", context, log);
                String stream = method.getResponseBodyAsString();

                if (StringUtils.isNotEmpty(stream))
                {
                    String decodeId = null;
                    for (String id : stream.split(",") ) {
			            log.info(String.format("Decoding ID %s for account %s",id,accountId));
                        decodeId = IdUtil.decode(id);
                        if (!decodeId.equals(id)) {
                            ids.add(decodeId);
                        }
                        ids.add(id);
                    }
                }
            }
        }
        catch (IOException e)
        {
            throw new MessageStoreException("Unable to retrieve messages for " + accountId, e);
        }
        finally
        {
            method.releaseConnection();

            log.debug(String.format("retrieve messages for account %s in %dms", accountId, (System.nanoTime() - start) / 1000000));
        }

        return ids;
    }

    private String getStoreUrl(Country country) {
        return SysConfigManager.instance().getValue(MESSAGEID_STORE_URL_KEY, country);
    }

    public String getBucket(Integer accountId)
    {
        if(log.isTraceEnabled())
            log.trace(String.format("getBucket(accountId=%s", String.valueOf(accountId)));

        StringBuilder account = new StringBuilder();

        account.append(accountId);

        while (account.length() < 6)
        {
            account.insert(0, "0");
        }

        char[] numerics = account.toString().toCharArray();

        StringBuilder bucket = new StringBuilder();

        String path = "";

        for (int i = numerics.length - 1; i >= 0; i--)
        {
            path += numerics[i];

            bucket.append(SLASH);
            bucket.append(path);
        }

        //return bucket.toString();
        return account.toString();
    }

    public void reconcile(Integer accountId)
    {
        // nothing to do
    }

    private boolean put(String resource, String encodedMessageId) throws IOException
    {
        PutMethod putMethod = new PutMethod(resource);
        putMethod.setRequestEntity(new StringRequestEntity(encodedMessageId, null, null));
        return execute(putMethod, putHttpClient) == HttpStatus.SC_CREATED;
    }

    private boolean delete(String resource, String encodedMessageId) throws IOException
    {
        DeleteMethod deleteMethod = new DeleteMethod(resource);
        if (encodedMessageId != null) {
            deleteMethod.setRequestEntity(new StringRequestEntity(encodedMessageId, null, null));
        }
        return execute(deleteMethod, deleteHttpClient) == HttpStatus.SC_CREATED;
    }

    private int execute(HttpMethod method, HttpClient httpClient) throws IOException
    {
        long nanos = System.nanoTime();

        int returnCode = -1;

        try
        {
            returnCode = httpClient.executeMethod(method);
        }
        finally
        {
            method.releaseConnection();

            log.debug(String.format("%s for %s returned %s completed in %d", method.getClass().getName(), method.getURI(), returnCode, (System.nanoTime() - nanos) / 1000000l));
        }


        return returnCode;
    }


    private MultiThreadedHttpConnectionManager getConnectionManager()
    {
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();

        HttpConnectionManagerParams params = new HttpConnectionManagerParams();
        params.setMaxTotalConnections(MAX_CONNECTIONS_PER_CLIENT);
        params.setConnectionTimeout(CONNECTION_TIMEOUT);
        params.setSoTimeout(CONNECTION_TIMEOUT);

        connectionManager.setParams(params);

        return connectionManager;
    }

}
