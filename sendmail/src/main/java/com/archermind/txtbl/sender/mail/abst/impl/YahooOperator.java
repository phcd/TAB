package com.archermind.txtbl.sender.mail.abst.impl;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.dal.business.impl.UserService;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.Attachment;
import com.archermind.txtbl.domain.Email;
import com.archermind.txtbl.domain.EmailPojo;
import com.archermind.txtbl.sender.mail.abst.Operator;
import com.archermind.txtbl.sender.mail.html.parse.YahooMailAJAX;
import com.archermind.txtbl.sender.mail.html.parse.YahooMailHtmlParse;
import com.archermind.txtbl.utils.MailTools;
import org.jboss.logging.Logger;

public class YahooOperator extends Operator {

	YahooMailHtmlParse yahooMailHtmlParse = null;

    private static final Logger log = Logger.getLogger(YahooOperator.class);


	@Override
	public String sendMail(List<EmailPojo> list) {
		Account account = null;
		DefaultHttpClient httpClient = null;
		String yahooServerName = null;
        Email email = null;
		String faultMid = "";
		ArrayList<String> attachementId = new ArrayList<String>();
		UserService error = new UserService();

		// String htmlbody = null;
		try {
			for (int i = 0; i < 3; i++) {

				yahooMailHtmlParse = new YahooMailHtmlParse();
				httpClient = new DefaultHttpClient();
				account = list.get(0).getAccount();
				String html = getShowFolder(httpClient, account.getName(), true);
				if (isNeedLogin(html)) {
					html = login(account.getName(), account.getPassword(),
							httpClient);
					// getShowFolder(httpClient, account.getName(), false);

				}
				yahooServerName = getServerName(html);
				if (!"".equals(yahooServerName) && yahooServerName != null) {
					break;
				}
				if (i == 2) {
					String verify = yahooMailHtmlParse.getFilter(html,
							"Please verify your password");
					if (!"".equals(verify) && verify != null) {
						for (EmailPojo emailPojo : list) {
							faultMid = faultMid
									+ emailPojo.getEmail().getMailid() + ",";
							log.error("need verify !: "
									+ emailPojo.getAccount().getName());
							error.updateAccountMessages("need verify !:",
									account.getName(), "1");
							error.modifyChangeFlag(account.getUser_id(), "1");
						}
					} else {
						for (EmailPojo emailPojo : list) {
							faultMid = faultMid
									+ emailPojo.getEmail().getMailid() + ",";
						}
						log.error("yahoo advertisement: " + account.getName());

					}
				}
			}

			if (yahooServerName != null && !"".equals(yahooServerName.trim())) {
				YahooMailAJAX yahooMailAJAX = new YahooMailAJAX(httpClient,
						yahooServerName);
				for (EmailPojo emailPojo : list) {
					email = emailPojo.getEmail();

					if (emailPojo.getAttachement().size() > 0) {
						attachementId = this.getAttachementlist(emailPojo
								.getAttachement(), yahooMailAJAX);
					}
					String resPonse = yahooMailAJAX.sendMail(email.getTo(), account
							.getName(), email.getCc(), email.getBcc(), email
							.getFrom_alias(), new String(emailPojo.getBody()
							.getData()), "", email.getSubject(), attachementId);

					attachementId.clear();
					faultMid = getFaultMid(resPonse, account, email);
				}
			}

		} catch (Exception e) {
			log.fatal("sendMail/YahooOperator/Exception:" + "[" + account.getName() + "]" + e, e);
		}

		if ("".equals(faultMid)) {
			faultMid = null;
			log.info("send Mail seccuss: " + account.getName() + " mailId: "
					+ email.getMailid());
		}
		return faultMid;
	}

	public String getFaultMid(String resPonse, Account account, Email email) {
		String faultMid = "";
		String resPonseString = yahooMailHtmlParse.getFilter(resPonse, "HumanVerificationRequired");
		if ("manyToOrCc".equals(resPonse)) {
			faultMid = faultMid + email.getMailid() + ",";
		}
		if ("".equals(resPonseString) || resPonseString != null) {
			faultMid = faultMid + email.getMailid() + ",";
			log
					.error("sendMail/YahooOperator/Exception: Human Verification required"
							+ "[" + account.getName());
			return faultMid;
		}
		resPonseString = yahooMailHtmlParse.getFilter(resPonse, "Reissuing session ID");
		if (!"".equals(resPonseString) && resPonseString != null) {
			faultMid += email.getMailid() + ",";
			log.error("trying smtp " + "[" + account.getName() + "]");
			return faultMid;
		}
		if(resPonse.indexOf("<SOAP-ENV:Body><SendMessageResponse/></SOAP-ENV:Body>")>0) {
			updateMailFlag(email.getMailid());
			return "";
		}
		resPonseString = yahooMailHtmlParse.getFilter(resPonse, "<SendMessageResponse><mid>");
		
		if ("".equals(resPonseString) || resPonseString == null) {
			faultMid += email.getMailid() + ",";
			return faultMid;
		} else {
			updateMailFlag(email.getMailid());
		}
		return faultMid;
	}

	

	public ArrayList<String> getAttachementlist(List<Attachment> attchmentList,
			YahooMailAJAX yahooMailAJAX) throws Exception {
		ArrayList<String> attachementId = new ArrayList<String>();
		for (Attachment attachment : attchmentList) {
			byte[] body = yahooMailAJAX.getBody(attachment.getData(), attachment.getName());
			attachementId.add(getAttachementId(body, yahooMailAJAX));
		}

		return attachementId;
	}

	public String getAttachementId(byte body[], YahooMailAJAX yahooMailAJAX)
			throws Exception {
		return yahooMailHtmlParse.getFilter(new String(yahooMailAJAX
				.binaryPost(body)), "(?<=<id>)[\\w*\\d*]+");
	}

	public String getShowFolder(DefaultHttpClient httpClient, String name,
			boolean isNeedSetCookies) throws DALException {

		String html = null;
		String url = "http://mail.yahoo.com/";
		if (isNeedSetCookies) {
			if (MailTools.setCookies(httpClient, name)) {
				try {
					html = new String(get(url, httpClient));
				} catch (Exception e) {
					log.fatal("sendMail/YahooOperator/Exception: " + "["
							+ name + "] [connect failure]", e);
					log.error("sendMail/YahooOperator/Exception: " + "["
							+ name + "] [connect failure]", e);
				}
				return html;
			} else {
				return "";
			}

		} else
			try {
				html = new String(get(url, httpClient));
			} catch (Exception e) {
				log.error("sendMail/YahooOperator/Exception: " + "[" + name
						+ "] [connect failure]", e);
			}
		return html;
	}

	// public boolean login(String mailboxName, String password,
	// DefaultHttpClient httpClient) throws Exception {
	//
	// boolean succFlag = false;
	// httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY,
	// CookiePolicy.BROWSER_COMPATIBILITY);
	// HttpPost httpPost = new
	// HttpPost("https://login.yahoo.com/config/login?");
	// List<NameValuePair> nvps = new ArrayList<NameValuePair>();
	// nvps.add(new BasicNameValuePair("login", mailboxName));
	// nvps.add(new BasicNameValuePair("passwd", password));
	// nvps.add(new BasicNameValuePair(".src", "ym"));
	// httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
	// HttpResponse response = httpClient.execute(httpPost);
	// HttpEntity httpEntity = response.getEntity();
	// List<Cookie> cookie = httpClient.getCookieStore().getCookies();
	//
	// if (cookie != null && cookie.size() > 0) {
	// succFlag = true;
	//
	// }
	// if (httpEntity != null) {
	// httpEntity.consumeContent();
	// }
	//
	// return succFlag;
	// }
	public String login(String mailboxName, String password,
			DefaultHttpClient httpClient) throws Exception {
		httpClient.getCookieStore().clear();
		httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY,
				CookiePolicy.BROWSER_COMPATIBILITY);
		HttpPost httpPost = new HttpPost(
				"https://login.yahoo.com/config/login?");
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("login", mailboxName));
		nvps.add(new BasicNameValuePair("passwd", password));
		nvps.add(new BasicNameValuePair(".src", "ym"));
		httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
		HttpResponse response = httpClient.execute(httpPost);
		HttpEntity httpEntity = response.getEntity();
		List<Cookie> cookie = httpClient.getCookieStore().getCookies();
		if (cookie.size() > 0)
			MailTools.saveCookies(mailboxName, cookie);
		return EntityUtils.toString(httpEntity);

	}

	public byte[] get(String url, DefaultHttpClient httpClient) throws Exception {
		HttpGet httpGet = new HttpGet(url);
		httpGet.setHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
		httpGet.setHeader("Accept", "*/*");
		httpGet.setHeader("Content-Type", "text/xml");
		HttpResponse response = httpClient.execute(httpGet);
		HttpEntity httpEntity = response.getEntity();
		byte[] data = EntityUtils.toByteArray(httpEntity);
		if (httpEntity != null) {
			httpEntity.consumeContent();
		}
		return data;
	}

	public boolean isNeedLogin(String html) {
		YahooMailHtmlParse mailParse = new YahooMailHtmlParse();
		return !(mailParse.getFilter(html, "name=\"login_form\"") == null && !"".equals(html) && mailParse.getFilter(html, "The error, LaunchCurlError-28, occurred when trying to connect to Yahoo! Mail") == null);

	}

	public String getServerName(String html) {
		if (html != null && !"".equals(html))
			return yahooMailHtmlParse.getServerName(html);
		else
			return "";

	}
}
