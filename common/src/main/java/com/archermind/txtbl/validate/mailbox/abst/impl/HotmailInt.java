package com.archermind.txtbl.validate.mailbox.abst.impl;


import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.MspToken;
import com.archermind.txtbl.validate.mailbox.abst.Validate;
import org.apache.http.impl.client.DefaultHttpClient;
import com.archermind.txtbl.utils.StringUtils;
import org.jboss.logging.Logger;

import java.util.Date;


public class HotmailInt extends Validate
{
    private static final Logger log = Logger.getLogger(HotmailInt.class);

    public void validate(Account account) throws Exception
    {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        String loginStr = null;

        HotMailSupport hotMailSupport = new HotMailSupport();

        log.info(String.format("starting hotmail msp validation account=%s, uid=%s", account.getName(), account.getUser_id()));

        /**
         * VV: fairly weird, some strange retry logic in case we get that particular error
         */
        for (int i = 0; i < 3; i++)
        {
            loginStr = hotMailSupport.login(httpClient, account.getName(), account.getPassword());

            
            if (!(loginStr.indexOf("The specified property name/id does not exist") > -1))
            {
                i = 3;
            }
        }

        if (loginStr != null && loginStr.contains("Authentication Failure"))
        {
            throw new Exception("username or password not right");
        }

        String token = hotMailSupport.getSecurityid(loginStr);
        String createdTime = hotMailSupport.getCreateTime(loginStr);
        String showFolder = hotMailSupport.getFullFolderList(httpClient, createdTime, token, "");

        if (showFolder.indexOf("(PNG)There was a problem checking the security header") > -1)
        {
            account.setMspToken(getToken(account, createdTime, "token not right", "transactionID not right"));
            log.info("There was a proble with msp server create waring mail successfull");
            return;
        }

        account.setMspToken(getToken(account, createdTime, token, "no transactionID"));

        /**
         * VV: very quesitonable logic below - probably is no longer necessary considering auth failure check added above
         */
    
        if (loginStr != null && loginStr.split("Can not find the record in Database").length > 1)
        {
            log.error("valiMailboxConfigHotmail/HotmailOperatorMsp/Exception: [cookie is null] [" + account.getName() + "]");
            throw new Exception("username or password not right");
        }

        if (StringUtils.isEmpty(showFolder))
        {
            log.error("valiMailboxConfigHotmail/HotmailOperatorMsp/Exception: [cookie is null] [" + account.getName() + "]");
            throw new Exception("username or password not right");
        }


    }

    private MspToken getToken(Account account, String createdTime, String token, String transactionID) throws DALException
    {

        MspToken mspToken = new MspToken();
        mspToken.setName(account.getName());
        mspToken.setUser_id(account.getUser_id());
        mspToken.setCreate_number(new Date().getTime());
        mspToken.setToken_id(token.getBytes());
        mspToken.setTransaction_id(transactionID);
        mspToken.setComment(createdTime);
        return mspToken;
    }


}
