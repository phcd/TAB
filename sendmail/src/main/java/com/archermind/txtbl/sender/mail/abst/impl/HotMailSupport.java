package com.archermind.txtbl.sender.mail.abst.impl;




import com.archermind.txtbl.domain.Attachment;
import com.archermind.txtbl.domain.OriginalReceivedAttachment;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.util.List;
import java.util.Random;

//TODO: Paul - pull out into somehting common - lot of similarities between this and the receiver hotmailsupport
public class HotMailSupport {

	public String getTransactionID(String soap) {
		String[] temp = soap.split("<TransactionID>");
		if (temp.length > 1) {
			return temp[1].split("</TransactionID>")[0];
		} else
			return "";
	}

	public String login(DefaultHttpClient httpClient, String name, String password) throws Exception {
		StringBuffer sb = new StringBuffer();

		sb.append("<Envelope xmlns=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:wsse=\"http://schemas.xmlsoap.org/ws/2003/06/secext\" xmlns:saml=\"urn:oasis:names:tc:SAML:1.0:assertion\" xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2002/12/policy\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" xmlns:wst=\"http://schemas.xmlsoap.org/ws/2004/04/trust\">");
		sb.append("<Header>");
		sb.append("<ps:AuthInfo xmlns:ps=\"http://schemas.microsoft.com/Passport/SoapServices/PPCRL\" Id=\"PPAuthInfo\">");
		sb.append("<ps:HostingApp>");
		sb.append("</ps:HostingApp>");
		sb.append("<ps:BinaryVersion>2</ps:BinaryVersion>");
		sb.append("<ps:UIVersion>");
		sb.append("</ps:UIVersion>");
		sb.append("<ps:Cookies />");
		sb.append("<ps:RequestParams>");
		sb.append("</ps:RequestParams>");
		sb.append("</ps:AuthInfo>");
		sb.append("<wsse:Security xmlns=\"http://schemas.xmlsoap.org/ws/2004/04/trust\">");
		sb.append("<wsse:UsernameToken Id=\"user\">");
		sb.append("<wsse:Username>");
		sb.append(name);
		sb.append("</wsse:Username>");
		sb.append("<wsse:Password>");
		sb.append(password);
		sb.append("</wsse:Password>");
		sb.append("</wsse:UsernameToken>");
		sb.append("</wsse:Security>");
		sb.append("</Header>");
		sb.append("<Body>");
		sb.append(" <ps:RequestMultipleSecurityTokens xmlns:ps=\"http://schemas.microsoft.com/Passport/SoapServices/PPCRL\" Id=\"RSTS\">");
		sb.append("<wst:RequestSecurityToken xmlns=\"http://schemas.xmlsoap.org/ws/2002/04/secext\">");
		sb.append("<wst:RequestType>http://schemas.xmlsoap.org/ws/2004/04/security/trust/Issue</wst:RequestType>");
		sb.append("<wsp:AppliesTo xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/03/addressing\">");
		sb.append("<wsa:EndpointReference>");
		sb.append("<wsa:Address>http://Passport.NET/tb</wsa:Address>");
		sb.append("</wsa:EndpointReference>");
		sb.append("</wsp:AppliesTo>");
		sb.append("</wst:RequestSecurityToken>");
		sb.append("<wst:RequestSecurityToken xmlns=\"http://schemas.xmlsoap.org/ws/2002/04/secext\">");
		sb.append("<wst:RequestType>http://schemas.xmlsoap.org/ws/2004/04/security/trust/Issue</wst:RequestType>");
		sb.append("<wsp:AppliesTo xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/03/addressing\">");
		sb.append("<wsa:EndpointReference>");
		sb.append("<wsa:Address>mws.beta.mobile.live.com</wsa:Address>");
		sb.append("</wsa:EndpointReference>");
		sb.append("</wsp:AppliesTo>");
		sb.append("<wsse:PolicyReference URI=\"NFS_24HR_10_SAML\" />");
		sb.append("</wst:RequestSecurityToken>");
		sb.append("</ps:RequestMultipleSecurityTokens>");
		sb.append("</Body>");
		sb.append("</Envelope>");
		if(name.endsWith("msn.com"))
			return new String(post(httpClient,"https://msnia.login.live.com/pp600/RST.srf", sb.toString()));
		else
			return new String(post(httpClient,"https://login.passport.com/RST.srf", sb.toString()));

	}

	public String sendMail(DefaultHttpClient httpClient, String messageId, String to, String cc, String bcc, String subject, String body, List<Attachment> attachementList, List<OriginalReceivedAttachment> originalReceivedAttachment, String createdTime, String securityid) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append("<env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\">");
		sb.append("<env:Header>");
		sb
				.append("<wsa:MessageID wsu:Id=\"MessageID\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">id:");
		String temp[];
		if (!"".equals(messageId) && messageId != null)
			sb.append(messageId);
		else
			sb.append(createMessageId());
		sb.append("</wsa:MessageID>");
		sb
				.append("<wsa:ReplyTo wsu:Id=\"ReplyTo\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\"	xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">");
		sb.append("<wsa:Address>http://207.68.152.91/MspWebSink</wsa:Address>");
		sb.append("<wsa:ReferenceProperties>");
		sb.append("<mws:DeviceAddress	xmlns:mws=\"http://schemas.live.com/mws/2006/10/core\">uuid:771f3ef0-b120-45f7-8127-a221cee9217c/Mail</mws:DeviceAddress>");
		sb.append("</wsa:ReferenceProperties>");
		sb.append("</wsa:ReplyTo>");
		sb.append("<wsa:To wsu:Id=\"To\"");
		sb.append("	xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\"");
		sb.append("	xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">http://schemas.live.com/mws/2006/10/mail</wsa:To>");
		sb.append("<wsa:Action wsu:Id=\"Action\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\"");
		sb.append("	xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">http://schemas.live.com/mws/2006/10/mail/SendMessage</wsa:Action>");
		sb.append("<DeviceOptions	xmlns=\"http://schemas.live.com/mws/2006/10/core\">");
		sb.append("<Locale xmlns=\"http://schemas.live.com/mws/2006/10/core\">1033</Locale>");
		sb.append("<UserAgent xmlns=\"http://schemas.live.com/mws/2006/10/core\">");
		sb.append("<Manufacturer>NO_Mfgr</Manufacturer>");
		sb.append("<Model>NO_Model</Model>");
		sb.append("<OS>");
		sb.append("<Name>NO_OS</Name>");
		sb.append("<Version>NO_Version</Version>");
		sb.append("</OS>");
		sb.append("<IMSI>NO_IMSI</IMSI>");
		sb.append("<MSISDN>NO_MSISDN</MSISDN>");
		sb.append("</UserAgent>");
		sb.append("</DeviceOptions>");
		sb.append("<Application xmlns=\"http://schemas.live.com/mws/2006/10/core\">");
		sb.append("<Name>MSP Tool</Name>");
		sb.append("<Vendor>Microsoft</Vendor>");
		sb.append("<Version>MSP Tool v3.6.14</Version>");
		sb.append("</Application>");
        sb.append(this.addSecurity(createdTime, securityid).split("<env:Body />")[0]).append("<env:Body>");
		sb.append("<Message xmlns=\"http://schemas.live.com/mws/2006/10/mail\">");
		if (!"".equals(to)&& to !=null) {
			temp = to.trim().replaceAll("\\s+", ";").replaceAll(",+", ";").split(";+");
            for (String aTemp : temp) {
                sb.append("<Recipient d4p1:Type=\"To\" xmlns:d4p1=\"http://schemas.live.com/mws/2006/10/mail\">");
                sb.append("<d4p1:Email>");
                sb.append(aTemp);
                sb.append("</d4p1:Email>");
                sb.append("</Recipient>");

            }
		}
		

		if (!"".equals(cc) && cc != null) {
			temp = cc.trim().replaceAll("\\s+", ";").replaceAll(",+", ";").split(";+");
            for (String aTemp : temp) {
                sb.append("<Recipient d4p1:Type=\"Cc\" xmlns:d4p1=\"http://schemas.live.com/mws/2006/10/mail\">");
                sb.append("<d4p1:Email>");
                sb.append(aTemp);
                sb.append("</d4p1:Email>");
                sb.append("</Recipient>");
            }
		}
		if (!"".equals(bcc) && bcc != null) {
			temp = bcc.trim().replaceAll("\\s+", ";").replaceAll(",+", ";").split(";+");
            for (String aTemp : temp) {
                sb.append("<Recipient d4p1:Type=\"Bcc\" xmlns:d4p1=\"http://schemas.live.com/mws/2006/10/mail\">");
                sb.append("<d4p1:Email>");
                sb.append(aTemp);
                sb.append("</d4p1:Email>");
                sb.append("</Recipient>");
            }
		}
		sb.append("<Subject>");
		sb.append(subject);
		sb.append("</Subject>");
		sb.append("<PlainBody>");
		if (!"".equals(body) && body != null)
			body = body.trim();
		sb.append(body);
		sb.append("</PlainBody>");

		if (originalReceivedAttachment != null && originalReceivedAttachment.size() > 0 && attachementList == null) {
			for (OriginalReceivedAttachment attachment : originalReceivedAttachment) {
				sb.append("  <Attachment>");
				sb.append("   <FileName>");
				sb.append(attachment.getName());
				sb.append("</FileName>");
				sb.append("  <Blob d5p1:TotalBytes=\"");
				sb.append(attachment.getData().length);
				sb.append("\" d5p1:StartByte=\"0\" d5p1:EndByte=\"952\" xmlns:d5p1=\"http://schemas.live.com/mws/2006/10/mail\">");
				sb.append("<Data>");
				sb.append(Base64.encode(attachment.getData()));
				sb.append("</Data>");
				sb.append("</Blob>");
				sb.append("</Attachment>");
			}
		}
		if (attachementList != null && attachementList.size() > 0 && originalReceivedAttachment == null) {
			for (Attachment attachment : attachementList) {
				sb.append("  <Attachment>");
				sb.append("   <FileName>");
				sb.append(attachment.getName());
				sb.append("</FileName>");
				sb.append("  <Blob d5p1:TotalBytes=\"");
				sb.append(attachment.getData().length);
				sb.append("\" d5p1:StartByte=\"0\" d5p1:EndByte=\"952\" xmlns:d5p1=\"http://schemas.live.com/mws/2006/10/mail\">");
				sb.append("<Data>");
				sb.append(Base64.encode(attachment.getData()));
				sb.append("</Data>");
				sb.append("</Blob>");
				sb.append("</Attachment>");
			}
		}
		sb.append("</Message>");
		sb.append("</env:Body>");
		sb.append("</env:Envelope>");
		System.out.println(sb.toString());
		return new String(basicAuthPost(httpClient, "https://http.mws.mobile.live.com/2006/10/MWP2007_02/MailService.asmx", sb.toString(),
				"application/soap+xml; charset=utf-8; action=\"http://schemas.live.com/mws/2006/10/mail/SendMessage\""));

	}

	public String addSecurity(String createdTime, String securityid) {
		StringBuffer sb = new StringBuffer();
		sb
				.append("<Security xmlns=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">\t\t<wsu:Timestamp wsu:Id=\"Timestamp\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">  <wsu:Created>");
		sb.append(createdTime);
		sb
				.append("</wsu:Created>   </wsu:Timestamp>  <wsse:BinarySecurityToken wsu:Id=\"token\" wsse:ValueType=\"http://docs.oasis-open.org/wss/2004/XX/oasis-2004XX-wss-saml-token-profile-1.0#SAMLAssertionID\" wsse:EncodingType=\"#Base64Binary\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">");
		sb.append(securityid);
		sb.append("</wsse:BinarySecurityToken>  </Security> <Billing\txmlns=\"http://schemas.xmlsoap.org/ws/2005/08/billing\">");
		sb.append("<OperatorID ");
		sb.append("xmlns=\"http://schemas.xmlsoap.org/ws/2005/08/billing\">435</OperatorID><GatewayID xmlns=\"http://schemas.xmlsoap.org/ws/2005/08/billing\">177</GatewayID> ");
		sb.append(" <OperatorCustomerRef  xmlns=\"http://schemas.xmlsoap.org/ws/2005/08/billing\"> OperatorCustomerRef </OperatorCustomerRef> ");
		sb.append("<TransactionID  xmlns=\"http://schemas.xmlsoap.org/ws/2005/08/billing\">2bab9bc5-30ac-4103-8523-80f445024089</TransactionID> <TransactionStart ");
		sb.append(" xmlns=\"http://schemas.xmlsoap.org/ws/2005/08/billing\">");
		sb.append(createdTime);
		sb.append("</TransactionStart>  <TransactionFinish ");
		sb.append(" xmlns=\"http://schemas.xmlsoap.org/ws/2005/08/billing\">0001-01-01T00:00:00.0000000Z</TransactionFinish> <ServiceActions ");
		sb.append(" xmlns=\"http://schemas.xmlsoap.org/ws/2005/08/billing\" /></Billing> </env:Header><env:Body /></env:Envelope>");
		return sb.toString();
	}

	public String subcribeAlert(DefaultHttpClient httpClient, String messageId, String createdTime, String securityid, String url) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb
				.append("<env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\">");
		sb.append("<env:Header>");
		sb
				.append("<wsa:MessageID wsu:Id=\"MessageID\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">id:");
		sb.append(messageId);
		sb.append("</wsa:MessageID>");
		sb
				.append("<wsa:ReplyTo wsu:Id=\"ReplyTo\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">");
		sb.append("<wsa:Address>http://207.68.152.91/MspWebSink</wsa:Address>");
		sb.append("<wsa:ReferenceProperties>");
		sb
				.append("<mws:DeviceAddress xmlns:mws=\"http://schemas.live.com/mws/2006/10/core\">uuid:771f3ef0-b120-45f7-8127-a221cee9217c/Mail</mws:DeviceAddress>");
		sb.append("</wsa:ReferenceProperties>");
		sb.append("</wsa:ReplyTo>");
		sb
				.append("<wsa:To wsu:Id=\"To\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">http://schemas.live.com/mws/2006/10/mail</wsa:To>");
		sb
				.append("<wsa:Action wsu:Id=\"Action\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">http://schemas.live.com/mws/2006/10/mail/Subscribe</wsa:Action>");
		sb
				.append("<DeviceOptions xmlns=\"http://schemas.live.com/mws/2006/10/core\">");
		sb.append("<Locale>1033</Locale>");
		sb.append("<UserAgent>");
		sb.append("<Manufacturer>NO_Mfgr</Manufacturer>");
		sb.append("<Model>NO_Model</Model>");
		sb.append("<OS>");
		sb.append("<Name>NO_OS</Name>");
		sb.append("<Version>NO_Version</Version>");
		sb.append("</OS>");
		sb.append("<IMSI>NO_IMSI</IMSI>");
		sb.append("<MSISDN>NO_MSISDN</MSISDN>");
		sb.append("</UserAgent>");
		sb.append("</DeviceOptions>");
		sb
				.append("<Application xmlns=\"http://schemas.live.com/mws/2006/10/core\">");
		sb.append("<Name>MSP Tool</Name>");
		sb.append("<Vendor>Microsoft</Vendor>");
		sb.append("<Version>MSP Tool v3.6.14</Version>");
		sb.append("</Application>");
		sb
				.append(addSecurity(createdTime, securityid).split(
						"</env:Header>")[0]);
		sb.append("</env:Header>");
		sb.append("<env:Body>");
		sb
				.append("<Subscribe xmlns=\"http://schemas.xmlsoap.org/ws/2004/08/eventing\">");
		sb.append("<Delivery>");
		sb.append("<NotifyTo>");
		sb
				.append("<Address xmlns=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">");
		sb.append(url);
		sb.append("</Address>");
		sb.append("</NotifyTo>");
		sb.append("</Delivery>");
		sb.append("</Subscribe>");
		sb.append("</env:Body>");
		sb.append("</env:Envelope>");
		
		return new String(
				basicAuthPost(
						httpClient,
						"https://http.mws.mobile.live.com/2006/10/MWP2007_02/MailService.asmx",
						sb.toString(),
						"application/soap+xml; charset=utf-8; action=\"http://schemas.live.com/mws/2006/10/mail/Subscribe\""));
	}

	public String getMessageId(String soap) {
		return soap.split("<wsa:MessageID>")[1].split("</wsa:MessageID>")[0].split("urn:uuid:")[1];
	}

	public String getFullFolderList(DefaultHttpClient httpClient, String createdTime, String securityid, String MessageId) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append("<env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\">");
		sb.append("<env:Header>");
		sb.append("<wsa:MessageID wsu:Id=\"MessageID\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">id:");
		if (!"".equals(MessageId) && MessageId != null)
			sb.append(MessageId);
		else
			sb.append(createMessageId());

		sb.append("</wsa:MessageID>");
		sb.append("<wsa:ReplyTo wsu:Id=\"ReplyTo\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">");
		sb.append("<wsa:Address>http://207.68.152.91/MspWebSink</wsa:Address>");
		sb.append("<wsa:ReferenceProperties>");
		sb.append("<mws:DeviceAddress xmlns:mws=\"http://schemas.live.com/mws/2006/10/core\">uuid:771f3ef0-b120-45f7-8127-a221cee9217c/Mail</mws:DeviceAddress>");
		sb.append("</wsa:ReferenceProperties>");
		sb.append("</wsa:ReplyTo>");
		sb.append("<wsa:To wsu:Id=\"To\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">http://schemas.live.com/mws/2006/10/mail</wsa:To>");
		sb.append("<wsa:Action wsu:Id=\"Action\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">http://schemas.live.com/mws/2006/10/mail/Sync</wsa:Action>");
		sb.append("<DeviceOptions xmlns=\"http://schemas.live.com/mws/2006/10/core\">");
		sb.append("<Locale>1033</Locale>");
		sb.append("<UserAgent>");
		sb.append("<Manufacturer>NO_Mfgr</Manufacturer>");
		sb.append("<Model>NO_Model</Model> ");
		sb.append("<OS>");
		sb.append("<Name>NO_OS</Name>");
		sb.append("<Version>NO_Version</Version>");
		sb.append("</OS>");
		sb.append("<IMSI>NO_IMSI</IMSI>");
		sb.append("<MSISDN>NO_MSISDN</MSISDN>");
		sb.append("</UserAgent>");
		sb.append("</DeviceOptions>");
		sb.append("<Application xmlns=\"http://schemas.live.com/mws/2006/10/core\">");
		sb.append("<Name>MSP Tool</Name>");
		sb.append(" <Vendor>Microsoft</Vendor>");
		sb.append("<Version>MSP Tool v3.6.14</Version>");
		sb.append("</Application>");
		sb.append("<BaseKnowledge xmlns=\"http://schemas.xmlsoap.org/ws/2005/08/ssync\">");
		sb.append("<ChangeNumber xmlns=\"http://schemas.live.com/mws/2006/10/mail\">0</ChangeNumber>");
		sb.append("</BaseKnowledge>");
		sb.append("<Filter xmlns=\"http://schemas.xmlsoap.org/ws/2005/08/ssync\">Mail/Folder[*]</Filter>");
		sb.append(addSecurity(createdTime, securityid));
		return new String(
				basicAuthPost(
						httpClient,					     
						"https://http.mws.mobile.live.com/2006/10/MWP2007_02/MailService.asmx",
						sb.toString().replaceAll("\\n", ""),
						"application/soap+xml; charset=utf-8; action=\"http://schemas.live.com/mws/2006/10/mail/Sync\""));
		
	}

	public String getSecurityid(String soap) throws Exception {		
		return Base64.encode(("<EncryptedData xmlns=\"http://www.w3.org/2001/04/xmlenc#\" Id=\"Assertion1\" "+soap.split("<EncryptedData xmlns=\"http://www.w3.org/2001/04/xmlenc#\" Id=\"Assertion1\"")[1].split("</EncryptedData>")[0]+"</EncryptedData>").getBytes());
		}

	public String getCreateTime(String soap) {
		return soap.split("<wsu:Created>")[1].split("</wsu:Created>")[0];
	}

	public byte[] post(DefaultHttpClient httpClient, String url, String body) throws Exception {
		HttpPost post = new HttpPost(url);
		org.apache.http.HttpEntity entity = new StringEntity(body);
		post.setEntity(entity);
		HttpResponse respones = httpClient.execute(post);
		return EntityUtils.toByteArray(respones.getEntity());
	}

	public static byte[] basicAuthPost(DefaultHttpClient httpClient, String url, String body, String ContentType) throws Exception {
		HttpPost post = new HttpPost(url);

		post.setHeader("Content-Type", ContentType);
		post.setHeader("Authorization", "Basic bGR+X1QjeWZTNiMhOl84X1F+YyE3VyNTRg==");
		org.apache.http.HttpEntity entity = new StringEntity(body);
		post.setEntity(entity);
		HttpResponse respones = httpClient.execute(post);
		return EntityUtils.toByteArray(respones.getEntity());
	}



	public static String createMessageId() {
		Random rand = new Random();
		return Integer.toString(rand.nextInt(9999999)) + "-" + Integer.toString(rand.nextInt(9999)) + "-" + Integer.toString(rand.nextInt(9999)) + "-" + Integer.toString(rand.nextInt(9999)) + "-"
				+ Integer.toString(rand.nextInt(999999999));
	}
}
