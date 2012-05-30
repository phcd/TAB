package com.archermind.txtbl.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.MimeUtility;


import com.beetstra.jutf7.CharsetProvider;

public class EncodeHandler {

	public String parseMsgSubject(Message msg) throws MessagingException, UnsupportedEncodingException {
		String subject = msg.getSubject();
		if (msg.getSubject().toLowerCase().indexOf("utf-7") != -1) {
			subject = UTF7T0Str(subject.toLowerCase().substring(subject.indexOf("?Q?") + 3, subject.length() - 2));
		} else {
			subject = MimeUtility.decodeText(subject);
		}
		return subject;
	}

	public String parseMsgContent(Part bodyPart) throws MessagingException, IOException {
		String content = "";
		byte[] dataByte = new byte[bodyPart.getInputStream().available()];
		bodyPart.getInputStream().read(dataByte);
		String mimeType = bodyPart.getContentType();
		if (mimeType != null) {
			if (mimeType.toLowerCase().indexOf("utf-7") != -1) {
				content += UTF7T0Str(new String(dataByte));
			} else {
				content += new String(dataByte, "utf-8");
			}
		}
		return content;
	}

	private String UTF7T0Str(String utf7Str) {
		CharsetProvider charsetProvider = new CharsetProvider();
		Charset charset = charsetProvider.charsetForName("utf-7");
		ByteBuffer byteBuffer = ByteBuffer.wrap(utf7Str.getBytes());
        return charset.decode(byteBuffer).toString();
	}
}
