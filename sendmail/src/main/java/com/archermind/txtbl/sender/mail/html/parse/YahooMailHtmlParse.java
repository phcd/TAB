package com.archermind.txtbl.sender.mail.html.parse;
import java.io.BufferedReader;
import java.io.CharArrayReader;

import java.util.ArrayList;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class YahooMailHtmlParse {
	private Pattern p = null;

    public String getSlogin(String html)
	{
		return	html.split("name=\".slogin\" value=\"")[1].split("\">")[0];
	
	}
	public String getChallenge(String html )
	{
		return html.split("name=\".challenge\" value=\"")[1].split("\">")[0];
	}
	
	public String getU(String html)
	{
		return html.split("name=\".u\" value=\"")[1].split("\">")[0];
	}
	public String getPd(String html)
	{
		return html.split("name=\".pd\" value=\"")[1].split("\">")[0];
	}
	public String getFilter(String html, String regex) {
		Matcher matcher = null;
		String filter = null;
		if (regex != null) {
			p = Pattern.compile(regex);
		}
		if (html != null) {
			matcher = p.matcher(html);
		} else {
			return null;
		}
		while (matcher.find()) {
			filter = matcher.group();
		}

		return filter;
	}

	
	public String getServerName(String html) {
		String serverName = null;
        String serverName_regex = "http://us\\.\\w+.mail.yahoo.com";
		serverName =this.getFilter(html, serverName_regex);
		if("".equals(serverName)||serverName == null)		
			if(html.split("'serverName': '").length>1)
		serverName="http://"+ html.split("'serverName': '")[1].split("',")[0];
			else 
				return null;
		return serverName;
	}

	
	
	
	public String mailToCcFilter(String html)
	{
		
		Matcher matcher = null;
		String filter = "";
		p = Pattern.compile("\\w+@\\w+.\\w+");
		
		if (html != null) {
			matcher = p.matcher(html);
		} else {
			return null;
		}
		while (matcher.find()) {
			filter = filter +matcher.group()+";";
		}
		return filter;
		
	}
	
	
	public String getMailBody(String html) {
		return html;
	}


	
	
	public String bodyFilter(String html ,String regex) throws Exception
	{
		boolean flag = false;
		boolean blankFlag = false;
		StringBuffer body = new StringBuffer();
		String s = null;
		CharArrayReader reader = new CharArrayReader(html.toCharArray());
		BufferedReader buf = new BufferedReader(reader);
		
			while ((s = buf.readLine()) != null) {
				if (this.getFilter(s.toUpperCase(), regex.replaceAll("\\?", "\\\\?")) != null
						&& !"".equals(this.getFilter(s, regex))) {
					flag = true;
					continue;
				}
				if (flag == true)
					if (s == null || "".equals(s))
						blankFlag = true;
				if (flag && blankFlag) {
					if (s != null && !"".equals(s)) {	 
						if(getFilter(s, "^[-+_*]+=*_*\\w*[Part]?\\w+")!=null)
							{
								break;
							}
						
					}
					body.append(s);
				}
				
			}
		
		
				buf.close();
			
		

		return body.toString();
		
	}
	public String getHtmlBody(String html) throws Exception {
        String getHtmlBody_regex = "CONTENT-TYPE: TEXT/HTML;";
		return this.bodyFilter(html, getHtmlBody_regex);
	}
	public String getTextBody(String html) throws Exception {
        String getTextBody_regex = "CONTENT-TYPE: TEXT/PLAIN;";
		return this.bodyFilter(html, getTextBody_regex);
	}
	public ArrayList<String> getAttachmentName(String html) throws Exception
	{
		String s = null;
		ArrayList<String> list = new ArrayList<String>();
		CharArrayReader reader = new CharArrayReader(html.toCharArray());
		BufferedReader buf = new BufferedReader(reader);
		
		boolean flag = false;
		boolean charFlag = false;
			while((s = buf.readLine()) != null)
			{
				if(this.getFilter(s, "Content-Disposition: attachment; filename")!=null)
				{
					flag = true;
				}
				if(this.getFilter(s, "Content-Disposition: attachment;$")!=null)
				{
					flag = true;
					charFlag = true;
				}
				if(flag)
				{
					if(charFlag )
					{
						charFlag = false;
						continue;
					}
					String[] result = s.split("filename=");					
					list.add(result[1].substring(1,result[1].length()-1));					
						flag= false;
					
				}
			}
	
		return list;
	}
	
	public String getAttachment(String html,String attachmentName) throws Exception
	{		
		return this.bodyFilter(html, attachmentName.toUpperCase());
	}
	public String getWssid(String html)
	{
		return this.getFilter(this.getFilter(html, "wssid=[\\w+\\W*]*&amp"),"(?<=[=])[\\w+\\W*]*(?=&)");
	}
	

	
}
