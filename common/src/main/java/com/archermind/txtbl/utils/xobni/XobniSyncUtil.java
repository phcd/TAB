package com.archermind.txtbl.utils.xobni;

import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.XobniAccount;
import com.archermind.txtbl.utils.SysConfigManager;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.jboss.logging.Logger;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class XobniSyncUtil {
    public static final String xobniHeaderAPIVersion ="X-Xobni-Cloud-API";
    public static final String xobniHeaderDBVersion = "X-Db-Version";
    public static final String xobniHeaderClientName = "X-Xobni-Client";
    public static final String xobniHeaderSessionToken = "X-Session-Token";

    public static final int XOBNI_200_OK= 200;
    public static final int XOBNI_DB_VERSION_ERROR = 412;
    public static final int XOBNI_AUTH_SESSION_ID_ERROR_401 = 401;
    public static final int XOBNI_AUTH_SESSION_ID_ERROR_403 = 403;
    public static final int XOBNI_ERROR = -1;
    public static final int XOBNI_UNKNOWHOST_ERROR = 0;

    private static final String SYNC_URL_SUFFIX = "/email";
    private static final String INVALID_PEEK_TOKEN_SUFFIX = "/peek_token_invalid";
    private static final String DB_VER_URL_SUFFIX = "/dbVer";

    private static HashMap<String, String> xobniHeaders;

    private static Logger log = Logger.getLogger(XobniSyncUtil.class);

    static {
        xobniHeaders = new HashMap<String, String>();
        xobniHeaders.put(XobniSyncUtil.xobniHeaderAPIVersion, SysConfigManager.instance().getValue("xobniAPIVersion", "1.0.3"));
        xobniHeaders.put(XobniSyncUtil.xobniHeaderClientName, SysConfigManager.instance().getValue("xobniClientName", "Peek Receivers"));
    }

    private XobniSyncUtil() {}

    public static XobniSyncUtil INSTANCE = new XobniSyncUtil();

    public XobniResponse getExpectedDBVersion(XobniAccount xobniAccount) {
        XobniResponse xobniResponse;
        String context = getContext(xobniAccount);
        String url = xobniAccount.getSyncUrl() + DB_VER_URL_SUFFIX;
        try {
            log.debug(context + "  Sending JSON message: " + url);

            PostMethod post = getXobniPostMethod(url, xobniAccount);
            post.setRequestEntity(new StringRequestEntity("DB Version request", "text/plain", "UTF-8"));

            int response = executeMethod(xobniAccount, post);

            if ((response == XOBNI_DB_VERSION_ERROR) || (response == XOBNI_200_OK)){
                //return expected header
                Header headerDBVer = post.getResponseHeader(xobniHeaderDBVersion);
                if (headerDBVer == null) {
                      log.error(context + "  DB Version request - Expected Header " + xobniHeaderDBVersion + " not found!");
                    xobniResponse = new XobniResponse(XOBNI_200_OK);
                } else {
                    xobniResponse = new XobniResponse(XOBNI_200_OK, headerDBVer.getValue());
                }
            } else {
                log.error(context + "  DB Synch Response (" + response +") from xobni was not expected: "+ XOBNI_DB_VERSION_ERROR);
                xobniResponse = new XobniResponse(response);
            }
            post.releaseConnection();
        } catch (Throwable e) {
            log.fatal(context + "  Error fetching DB Version to Xobni using URL " + url, e);
            xobniResponse = new XobniResponse(XOBNI_ERROR);
        }
        return xobniResponse;
    }

    /* sends JSON message to Xobni */
    public int sync(JSONObject jsonMsg, XobniAccount xobniAccount) {
        String context = getContext(xobniAccount);
        String xobniSyncURL = xobniAccount.getSyncUrl() + SYNC_URL_SUFFIX;
        try {
            log.debug(context + "  Sending JSON message: " + xobniSyncURL);

            PostMethod post = getXobniPostMethod(xobniSyncURL, xobniAccount);
            post.setRequestEntity(new StringRequestEntity(jsonMsg.toString(), "text/plain", "UTF-8"));

            try {
                int response = executeMethod(xobniAccount, post);

                if (response != XOBNI_200_OK) {
                    logErrors(context, post, response);
                }
                return response;
            } finally {
                post.releaseConnection();
            }
        } catch (UnknownHostException e) {
            log.error(context + "  Error posting JSON Sync to Xobni using URL " + xobniSyncURL, e);
            return XOBNI_UNKNOWHOST_ERROR;
        } catch (Throwable e) {
            log.error(context + "  Error posting JSON Sync to Xobni using URL " + xobniSyncURL, e);
            return XOBNI_ERROR;
        }
    }

    public boolean notifyAccountLock(Account account, XobniAccount xobniAccount) {
        String context = getContext(xobniAccount);
        String url = xobniAccount.getSyncUrl() + INVALID_PEEK_TOKEN_SUFFIX;
        try {
            log.debug(context + "  Sending JSON message: " + url);

            PostMethod post = getXobniPostMethod(url, xobniAccount);
            post.setQueryString(new NameValuePair[] {
                new NameValuePair("receiver", XobniUtil.getReceiver(account)),
                new NameValuePair("email", account.getName())
            });

            try {
                int response = executeMethod(xobniAccount, post);

                if(response == XOBNI_200_OK) {
                    return true;
                }

                logErrors(context, post, response);
            } finally {
                post.releaseConnection();
            }
        } catch (Throwable e) {
            log.error(context + "  Error notify account lock Xobni using URL " + url, e);
        }
        return false;
    }

    private void logErrors(String context, PostMethod post, int response) {
        String errorString = context + "  Response from xobni was an ERROR! - response: " + response + " status: " + post.getStatusText();
        if(response != XOBNI_DB_VERSION_ERROR) {
            log.error(errorString);
        } else {
            log.info(errorString);
        }
        Header[] headers = post.getResponseHeaders();
        for (Header header : headers) {
            log.debug(context + " : " + header.toString());
        }
    }

    private PostMethod getXobniPostMethod(String xobniSyncURL, XobniAccount xAccount) {
        String sessionId = xAccount.getSessionId();
        PostMethod post = new PostMethod(xobniSyncURL);

        Set headerSet = xobniHeaders.entrySet();
        Iterator iter = headerSet.iterator();

        if( !xobniHeaders.containsKey(xobniHeaderAPIVersion) ||
                !xobniHeaders.containsKey(xobniHeaderClientName)) {
            log.warn("Xobni Dispatch request for SessionId: " + sessionId + " may not contain all required headers");
        }

        while(iter.hasNext()) {
            Map.Entry header = (Map.Entry)iter.next();
            post.addRequestHeader((String)header.getKey(), (String)header.getValue());
        }

        post.addRequestHeader(xobniHeaderSessionToken, sessionId);
        post.addRequestHeader(xobniHeaderDBVersion, xAccount.getDbVersion());
        return post;
    }

    private String getContext(XobniAccount xobniAccount) {
        return "Account: " + xobniAccount.getName() +  " SessionID: " + xobniAccount.getSessionId();
    }

    private HttpClient getHttpClient() {
        HttpClient httpClient = new HttpClient();
        httpClient.getParams().setParameter("http.socket.timeout", new Integer(60000));
        httpClient.getParams().setParameter("http.protocol.content-charset", "UTF-8");
        return httpClient;
    }

    private int executeMethod(XobniAccount xobniAccount, PostMethod post) throws IOException {
        String context = getContext(xobniAccount);
        log.debug(context + " dispatch post: " + post);
        int response = getHttpClient().executeMethod(post);
        log.debug(context + "  Response from xobni (" + String.valueOf(response) +") : " + post.getStatusText());
        return response;
    }
}


