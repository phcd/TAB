package com.archermind.txtbl.sender.mail.abst.impl;

import java.text.DateFormat;

import org.jboss.logging.Logger;
import org.springframework.web.util.HtmlUtils;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

import org.apache.http.impl.client.DefaultHttpClient;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.dal.business.IMspTokenService;

import com.archermind.txtbl.dal.business.impl.MspTokenService;
import com.archermind.txtbl.dal.business.impl.UserService;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.Email;
import com.archermind.txtbl.domain.EmailPojo;
import com.archermind.txtbl.domain.MspToken;
import com.archermind.txtbl.sender.mail.abst.Operator;
//import com.archermind.txtbl.utils.SysConfigManager;
import com.archermind.txtbl.utils.SysConfigManager;


public class HotmailOperatorMsp extends Operator {

    private static final Logger log = Logger.getLogger(HotmailOperatorMsp.class);

    HotMailSupport hotMailSupport = new HotMailSupport();

	@Override
	public String sendMail(List<EmailPojo> list) {
		Account account = list.get(0).getAccount();
		DefaultHttpClient httpClient = new DefaultHttpClient();
		String token = null;
		long days;
		String createdTime = null;
		String messageId = null;
		String faultMid = null;
		try {
		
            days = getSecurityIdDays(account);// days of
			
			
			if (days / 1000 / 61 / 61 / 24< 90 && days > -1) {
				token = getToken(account);
				createdTime = getCreatedTime();				
				String showFolder = hotMailSupport.getFullFolderList(httpClient, createdTime, token, "");
				if(showFolder.indexOf("Sign in failed")>0) {
					if (!"".equals(account.getPassword()) && account.getPassword() != null) {
						 String loginStr = hotMailSupport.login(httpClient, account.getName(), account.getPassword());
						 token = hotMailSupport.getSecurityid(loginStr);
						 createdTime = hotMailSupport.getCreateTime(loginStr);
						 showFolder = hotMailSupport.getFullFolderList(httpClient, createdTime, token, "");
						 
						 messageId = hotMailSupport.getMessageId(showFolder);
						 String transactionID =
						 hotMailSupport.getTransactionID(hotMailSupport.subcribeAlert(httpClient, messageId, createdTime, token, SysConfigManager.instance().getValue("subscribe.url")));
						 if (!"".equals(transactionID) && transactionID != null) {
						 log.info("HotmailProviderMsp subcribe success transactionID " + transactionID);
						 }
						 saveToken(account, createdTime, token, transactionID);
						 log.info("hotmail scriping to msp!!!! [" + account.getName()
						 + "]");
						 }
				}
				messageId = hotMailSupport.getMessageId(showFolder);
				log.info("HotmailOperatorMsp token days is "+days+" less than 90 days");
			
				
						

			} else {
				log.info("HotmailOperatorMsp: token is over 90 days " + account.getName());
				UserService error = new UserService();
				error.updateAccountMessages("HotmailOperatorMsp username or password not right", account.getName(), "2");
				error.modifyChangeFlag(account.getUser_id(), "1");
			}


            for (EmailPojo emailPojo : list) {

                Email email = emailPojo.getEmail();
                String response = hotMailSupport.sendMail(httpClient, messageId, email.getTo(), email.getCc(), email.getBcc(), HtmlUtils.htmlEscape(email.getSubject()), HtmlUtils.htmlEscape(new String(emailPojo.getBody().getData())), emailPojo.getAttachement(), null, createdTime, token);
                faultMid = getFaultMid(response, account, email);
            }
        } catch (Exception e) {
			log.error("HotmailOperatorMsp ["+account.getName()+"] fault",e);
		}
		return faultMid;
	}
	
	private static String getCreatedTime()
	{
		Date date = new Date();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		TimeZone timeZone = TimeZone.getTimeZone("GMT");
		dateFormat.setTimeZone(timeZone);
        return dateFormat.format(date);
	}

	private static String getToken(Account account) throws Exception {
		 IMspTokenService service = new MspTokenService();
		 return new String (service.getMspToken(account.getUser_id(), account.getName()).getToken_id(),"utf-8");
	}

	

	private static int saveToken(Account account,String createdTime,String token,String transactionID) throws DALException {
		IMspTokenService service = new MspTokenService();
		MspToken mspToken= new MspToken();
		mspToken.setName(account.getName());
		mspToken.setUser_id(account.getUser_id());	
		mspToken.setCreate_number(new Date().getTime());
		mspToken.setToken_id(token.getBytes());
		mspToken.setTransaction_id(transactionID);
		mspToken.setComment(createdTime);
		return	service.setMspToken(mspToken);
	}
	private static long getSecurityIdDays(Account account) throws DALException {
		// CookieSpec cookiespec = CookiePolicy.getDefaultSpec();
		
		 long days = -1;
		 long createTime =0;
		 IMspTokenService service = new MspTokenService();	
		 MspToken mspToken =service.getMspToken(account.getUser_id(), account.getName());
		 if(mspToken!=null)
		 {
			 createTime =mspToken.getCreate_number() ;
		 }
		
		  if(createTime>0)
		 {
			 days=  (new Date().getTime()- createTime)/ 1000 / 60 / 60 / 24;
		 }	
		 
		  
		
		return days;

	}
	
	public String getFaultMid(String resPonse, Account account, Email email) {
	
		String faultMid = "";
		if(!"".equals(resPonse)&& resPonse!=null)
		{
			if(resPonse.split("<Status>").length>1)
			{
				if("SUCCESS".equals(resPonse.split("<Status>")[1].split("</Status>")[0]))
				{
					log.info("HotmailOperatorMsp ["+account.getName()+"]"+" email Id: ["+email.getMailid()+"] send success" );
					faultMid = null;
				}
				else
				{

					faultMid = faultMid+email.getMailid()+",";
					log.info("HotmailOperatorMsp ["+account.getName()+"] send from local, faultMid: " +faultMid);
				
				}
			}
			else 
				{
					faultMid = faultMid+email.getMailid()+",";
					log.info("HotmailOperatorMsp ["+account.getName()+"] send from local, faultMid: " +faultMid);
				}
		}
		if("".equals(faultMid))
			faultMid=null;
		if(faultMid==null)
			updateMailFlag(email.getMailid());
		return faultMid;
		
	}


}
