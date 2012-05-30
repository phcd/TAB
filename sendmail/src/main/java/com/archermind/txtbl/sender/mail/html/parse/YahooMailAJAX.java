package com.archermind.txtbl.sender.mail.html.parse;

import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class YahooMailAJAX {

	private DefaultHttpClient httpClient = null;

	private String yahooServerName;

	YahooMailHtmlParse mailParse = new YahooMailHtmlParse();

	String url = null;

	byte[] data = null;

	String body = null;

	public YahooMailAJAX(DefaultHttpClient httpClient, String yahooServerName) throws Exception {
		this.yahooServerName = yahooServerName;
		this.httpClient = httpClient;

	}

	public String getWssId() throws Exception {

		url = yahooServerName + "/ws/mail/v1/soap?m=GetMetaData&wssid=&appid=YahooMailRC";
		body = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/1999/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/1999/XMLSchema\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\"> "
				+ "<SOAP-ENV:Body>" + "<m:GetMetaData xmlns:m=\"urn:yahoo:ymws\">" + "</m:GetMetaData>" + "</SOAP-ENV:Body></SOAP-ENV:Envelope>";
		data = post(url, body);
		return mailParse.getWssid(new String(data));
	}

	public String sendMail(String to, String from, String cc, String bcc, String name, String text, String html, String subject, ArrayList<String> list) throws Exception {
		if(subject.length()>=250)
			subject = subject.substring(0,245)+"...";
		if(!"".equals(text)&&text!=null)
			text = text.trim();
		StringBuffer sb = new StringBuffer();
		if (list.size() > 0) {
			for (String id : list) {
				sb.append("<attachment ");
				sb.append("attachment=\"upload://");
				sb.append(id);
				sb.append("\"  ");
				sb.append("disposition=\"attachment\" />   ");
			}
		}

		url = yahooServerName + "/ws/mail/v1/soap?m=SendMessage&appid=YahooMailRC&wssid=" + getWssId();
		body = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/1999/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/1999/XMLSchema\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\"><SOAP-ENV:Body><m:SendMessage xmlns:m=\"urn:yahoo:ymws\"><message>";
		if (!"".equals(to) && to != null)
			body = body + targetAddress(to,"to"); //body = body + "<to><email>" + to + "</email></to>";
		if (!"".equals(cc) && cc != null) {
			body = body + targetAddress(cc,"cc"); //body = body + "<cc><email>" + cc + "</email></cc>";
		}
		if (!"".equals(bcc) && bcc != null) {
			body = body + targetAddress(bcc,"bcc"); //body = body + "<bcc><email>" + bcc + "</email></bcc>";
		}
		body = body + "<from>";
		if (!"".equals(name) && name != null) {
			body = body + "<name>" + name + "</name>";
		}
		body = body + "<email>" + from + "</email></from><mailer>YahooMailRC/975.41</mailer><simplebody><text>" + text + "</text> ";
		if (!"".equals(html) && html != null) {
			body = body + "<html> " + html + " </html>";
		}
		body = body + sb.toString() + " </simplebody><subject>" + subject + "</" + "subject></message></m:SendMessage></SOAP-ENV:Body></SOAP-ENV:Envelope>";
		data = this.post(url, body);
		return new String(data);
	}

	public byte[] getBody(byte[] data, String attachmentName) {
		StringBuffer sb = new StringBuffer();
		sb.append("-----------------------------111\r\n");
		sb.append("Content-Disposition: form-data; name=\"_charset_\"\r\n");
		sb.append("\r\n");
		sb.append("utf-8\r\n");
		sb.append("-----------------------------111\r\n");
		sb.append("Content-Disposition: form-data; name=\"uploadfile\"; filename=\"");
		sb.append(attachmentName);
		sb.append("\" \r\n");
		sb.append("Content-Type: application/octet-stream\r\n");
		sb.append("\r\n");
		byte[] head = sb.toString().getBytes();
		byte[] end = "\r\n-----------------------------111--\r\n".getBytes();
		byte[] body = new byte[head.length + data.length + end.length];
		System.arraycopy(head, 0, body, 0, head.length);
		System.arraycopy(data, 0, body, head.length, data.length);
		System.arraycopy(end, 0, body, head.length + data.length, end.length);
		return body;
	}

	public byte[] post(String url, String body) throws Exception {
		byte[] data = null;

		HttpPost httpPost = new HttpPost(url);
		if (!"".equals(body) && body != null) {
			StringEntity stringEntity = new StringEntity(body);
			httpPost.setEntity(stringEntity);
			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity httpEntity = response.getEntity();
			data = EntityUtils.toByteArray(httpEntity);
			if (httpEntity != null) {
				httpEntity.consumeContent();
			}
		}

		return data;
	}

	public byte[] binaryPost(byte[] body) throws Exception {
		byte[] data = null;

		HttpPost httpPost = new HttpPost(yahooServerName + "/ya/upload");
		httpPost.setHeader("Content-Type", "multipart/form-data; boundary=---------------------------111");
		if (body != null && body.length == 0) {
			ByteArrayEntity byteArrayEntity = new ByteArrayEntity(body);
			httpPost.setEntity(byteArrayEntity);
			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity httpEntity = response.getEntity();
			data = EntityUtils.toByteArray(httpEntity);
			if (httpEntity != null) {
				httpEntity.consumeContent();
			}
		}

		return data;
	}

	public byte[] get(String url) throws Exception {
		HttpGet httpGet = new HttpGet(url);
		httpGet.addHeader("User-Agent", "Mozilla/4.0");
		HttpResponse response = httpClient.execute(httpGet);
		HttpEntity httpEntity = response.getEntity();
		byte[] data = EntityUtils.toByteArray(httpEntity);
		if (httpEntity != null) {
			httpEntity.consumeContent();
		}
		return data;
	}
	
	public String targetAddress(String addr,String type) throws Exception {
		String target = ""; 
		if (!"".equals(addr) && addr != null) {
			String[] address = addr.trim().replaceAll("\\s+", ";").replaceAll(",+", ";").split(";+");
			for (String temp : address) {
				target += "<" + type + "><email>" + temp + "</email></" + type + ">";
			}
		}
		return target;
	}
	

}
