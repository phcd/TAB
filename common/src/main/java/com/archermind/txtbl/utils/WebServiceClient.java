package com.archermind.txtbl.utils;

import com.archermind.txtbl.domain.Account;
import org.apache.commons.httpclient2.HttpClient;
import org.apache.commons.httpclient2.methods.PostMethod;
import org.apache.commons.httpclient2.HttpStatus;
import org.apache.commons.httpclient2.UsernamePasswordCredentials;
import org.jboss.logging.Logger;

import java.io.IOException;

public class WebServiceClient {
    private static Logger log = Logger.getLogger(WebServiceClient.class);

    private static String emailServerWSUrl;
    private static String adminUser;
    private static String adminPassword;
    private static int emailServerWSTimeout;

    static {
        emailServerWSUrl = SysConfigManager.instance().getValue("emailServerWSUrl");
        emailServerWSTimeout = Integer.valueOf(SysConfigManager.instance().getValue("emailServerWSTimeut", "60000"));
        adminUser = SysConfigManager.instance().getValue("emailServerUser", "admin");
        adminPassword = SysConfigManager.instance().getValue("emailServerPassword", "txtbl123");
    }
    
    public static boolean resetAccount(Account account) {
        if(StringUtils.isEmpty(emailServerWSUrl)) {
            log.error("emailServerWSUrl empty");
            return false;
        }

        String context = "reset account : " + account.getName() + " uuid : " + account.getUser_id();
        try {
            String accountResetUrl = emailServerWSUrl + "/accounts/" + account.getId() + "/accountreset";
            //TODO - Paul - remove duplication
            HttpClient httpClient = new HttpClient();
            httpClient.setConnectionTimeout(emailServerWSTimeout);
            PostMethod post = new PostMethod(accountResetUrl);
            post.setRequestHeader("Content-type", "application/json");
            httpClient.getState().setCredentials("Peek realm", post.getHostConfiguration().getHost(), new UsernamePasswordCredentials(adminUser, adminPassword));
            int response = httpClient.executeMethod(post);
            log.info(context + " response code : " + response);
            return response == HttpStatus.SC_OK;
        } catch (IOException e) {
            log.error(context, e);
            return false;
        }
    }
}
