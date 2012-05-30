package com.archermind.txtbl.receiver.mail.support;

import com.archermind.txtbl.attachment.format.inte.impl.JPGResizer;
import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.dal.business.IMspTokenService;
import com.archermind.txtbl.dal.business.impl.MspTokenService;
import com.archermind.txtbl.domain.*;
import com.archermind.txtbl.receiver.mail.config.ReceiverConfig;
import com.archermind.txtbl.receiver.mail.dal.DALDominator;
import com.archermind.txtbl.utils.ReceiverUtilsTools;
import org.jboss.logging.Logger;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MspExpired {
	
	private static String largeAttachMsg = "[Sorry, this email contains attachments that exceed Peek's size limits. To access or forward all attachments, view this email on your computer.]\n";
	 

    private static final Logger log = Logger.getLogger(MspExpired.class);

	public static int notifytoClient() throws Exception
	{
		int flag =-1;
		List<MspToken> mspTokens = getTokenList();
		for(MspToken mspToken : mspTokens)
		{
			if(getSecurityIdDays(mspToken)>90)
			{
				
				flag=	notifyClient(mspToken.getName(),mspToken.getUser_id(),ReceiverConfig.getProp("after.invalid.msp.subject"),ReceiverConfig.getProp("after.invalid.msp.content"));
			}
		}
	return flag;
	}
	private static int notifyClient(String name,String userId ,String subject,String body) throws Exception {
		OriginalReceivedEmail originalEmail = new OriginalReceivedEmail();
		originalEmail.setSubject(subject);
		originalEmail.setEmailFrom("peek@peek.com");
		originalEmail.setEmailTo(name);
		originalEmail.setBody(body.getBytes());
		originalEmail.setUserId(userId);
		
        //TODO - Paul - not needed.
        return DALDominator.newSaveMail(getEmailPojo(originalEmail, name, originalEmail.getBcc()), originalEmail, new Account());
	}
	private static String changeFilename(String filename) {
		String filenameSuffix = getFilenameSuffix(filename).toLowerCase();
		if (!"jpg".equals(filenameSuffix) && !"jpeg".equals(filenameSuffix)) {
			filename = filename + ".jpg";
		}
		return filename;
	}
	private static boolean filenameFilter(String fileName) {
		String[] attachType = ReceiverConfig.getProp("attachType").split(",+");
		if (fileName.indexOf(".") != -1) {
			String filenameSuffix = getFilenameSuffix(fileName);
			for (String tempFilename : attachType) {
				if (filenameSuffix.toLowerCase().trim().equals(tempFilename.trim().toLowerCase())) {
                    return true;
                }
			}
		}
		return false;
	}
	
	private static EmailPojo getEmailPojo(OriginalReceivedEmail original, String mailbox, String fromAlias) throws UnsupportedEncodingException {
		if (mailbox.endsWith("hotmail.com") || mailbox.endsWith("msn.com") || mailbox.endsWith("live.com")) {
			byte[] bodyContent = original.getBody();
			original.setBody(ReceiverUtilsTools.bodyFilter(bodyContent).getBytes("UTF-8"));
		}
		Email email = new Email();
		email.setSubject(original.getSubject());
		email.setFrom(original.getEmailFrom());
		email.setTo(subAddress(original.getEmailTo()));
		email.setCc(subAddress(original.getCc()));
		email.setBcc(subAddress(original.getBcc()));
		email.setMaildate(original.getMailTime());
		email.setStatus("0");
		email.setUserId(original.getUserId());

		String fromAddr = original.getEmailFrom();
		if (fromAlias != null && !"".equals(fromAlias.trim())) {
			email.setFrom_alias(fromAlias);
		} else {
			if (fromAddr.indexOf("@") != -1) {
				email.setFrom_alias(com.archermind.txtbl.utils.UtilsTools.stripDomainName(fromAddr));
			} else {
				email.setFrom_alias(fromAddr);
			}
		}

		email.setOriginal_account(mailbox);

		List<Attachment> list = new ArrayList<Attachment>();

		boolean largeAttachmentFlag = false;
		int attachSize = Integer.parseInt(ReceiverConfig.getProp("attachSize"));
		for (OriginalReceivedAttachment attach : original.getAttachList()) {
			Attachment attachment = new Attachment();
			attachment.setName(attach.getName());
			attachment.setSize(attach.getId());
			attach.setId(null);
			if (attachment.getSize() > attachSize) {
				largeAttachmentFlag = true;
			}
			if (filenameFilter(attach.getName())) {
				String filenameSuffix = getFilenameSuffix(attach.getName().toLowerCase());
				if ("jpg".equals(filenameSuffix) || "jpeg".equals(filenameSuffix) || "bmp".equals(filenameSuffix) || "png".equals(filenameSuffix) || "gif".equals(filenameSuffix)) {
					try {
						attachment.setName(changeFilename(attach.getName()));
						byte[] dataByte = new JPGResizer().resize(ImageIO.read(new ByteArrayInputStream(attach.getData())), "JPG", Integer.parseInt(ReceiverConfig.getProp("imageHeight")), Integer.parseInt(ReceiverConfig.getProp("imageWidth")));
						attachment.setSize(dataByte.length);
						attachment.setData(dataByte);
					} catch (Exception e) {
						log.warn("getEmailPojo/Provider/Exception: [" + mailbox + "] [" + original.getUid() + "] [ " + attach.getName() + "] [filter attachment]", e);
					}
				} else {
					attachment.setData(attach.getData());
				}
			}
			list.add(attachment);
		}

		Body body = new Body();
		if (largeAttachmentFlag) {
			body.setData((largeAttachMsg + new String(original.getBody(), "UTF-8")).getBytes("UTF-8"));
		} else {
			body.setData(original.getBody());
		}
		email.setBodySize(body.getData().length);

		EmailPojo emailPojo = new EmailPojo();
		emailPojo.setEmail(email);
		emailPojo.setBody(body);
		emailPojo.setAttachement(list);
		return emailPojo;
	}
	
	private static String getFilenameSuffix(String filename) {
		String filenameSuffix = filename.substring(filename.lastIndexOf(".") + 1, filename.length());
		return filenameSuffix == null ? "" : filenameSuffix;
	}

	private static String subAddress(String address) {
		if (address != null) {
			if (address.length() > 3000) {
				address = address.substring(0, 3000);
				address = address.substring(0, address.lastIndexOf(";"));
			}
		}
		return address;
	}

	private static long getSecurityIdDays(MspToken mspToken) throws DALException {
		// CookieSpec cookiespec = CookiePolicy.getDefaultSpec();

		long days = -1;
		long createTime = 0;			
		if (mspToken != null) {
			createTime = mspToken.getCreate_number();
		}

		if (createTime > 0) {
			days = (new Date().getTime() - createTime) / 1000 / 60 / 60 / 24;
		}

		return days;

	}
	
	

	private static List<MspToken> getTokenList() throws Exception {
		IMspTokenService service = new MspTokenService();
        return service.getAllMspToken();
	}
}
