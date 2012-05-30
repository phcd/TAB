package com.archermind.txtbl.utils;

import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.dal.business.ITxtblWebService;
import com.archermind.txtbl.dal.business.impl.TxtblWebService;
import com.archermind.txtbl.domain.TxtblCookie;

import javax.mail.internet.InternetAddress;

public class MailTools {
	
	public static  boolean setCookies(DefaultHttpClient httpClient, String name) throws DALException {
		ITxtblWebService service = new TxtblWebService();
		List<TxtblCookie> list = service.queryTxtblCookieByAccountName(name);
		if (list != null && list.size() > 0) {
			for (TxtblCookie c : list) {
				BasicClientCookie cookie = new BasicClientCookie(c.getCookiesName(), c.getCookiesValue());
				cookie.setDomain(c.getDomain());
				cookie.setDomain(c.getDomain());
				cookie.setPath(c.getPath());
				httpClient.getCookieStore().addCookie(cookie);
			}
			return true;
		} else
			return false;

	}

	public static int saveCookies(String name, List<Cookie> cookieList) throws DALException {
		ITxtblWebService service = new TxtblWebService();
		List<TxtblCookie> list = new ArrayList<TxtblCookie>();
		for (Cookie cookie : cookieList) {
			TxtblCookie txtblCookie = new TxtblCookie();
			txtblCookie.setEmailAccount(name);
			txtblCookie.setDomain(cookie.getDomain());
			txtblCookie.setPath(cookie.getPath());
			txtblCookie.setCookiesName(cookie.getName());
			txtblCookie.setCookiesValue(cookie.getValue());

            Date expiryDate = cookie.getExpiryDate();
            if (expiryDate == null) {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, 2999);
                expiryDate = cal.getTime();
            }

            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            
			txtblCookie.setExpiryDate(dateFormat.format(expiryDate));

			list.add(txtblCookie);
		}

		return service.saveOrUpdateTxtblCookies(list);
	}

    public static String getAddress(InternetAddress[] internetAddress)
    {
        String address = "";
        if (internetAddress != null && internetAddress.length > 0) {
            for (InternetAddress internetAddres : internetAddress) {
                if ("".equals(address)) {
                    address = internetAddres.getAddress();
                } else {
                    address += ";" + internetAddres.getAddress();
                }
            }
        }
        return address;
    }

}
