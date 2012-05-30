package com.archermind.txtbl.attachment.format.inte.impl;


import com.archermind.txtbl.attachment.format.inte.AttachmentFormat;
import com.archermind.txtbl.attachment.format.support.DefaultJPG;
import com.archermind.txtbl.domain.Attachment;
import org.jboss.logging.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class JPGFormatImpl implements AttachmentFormat {
    private static final Logger log = Logger.getLogger(JPGFormatImpl.class);

	public List<Attachment> format(String mailbox, String msgID, String filename, byte[] dataByte, int targetHeight, int targetWidth) {
        String message = String.format("resizing jpg attachment mailbox=%s, msgID=%s, filename=%s, size=%s bytes", mailbox, msgID, filename, dataByte==null ? 0 : dataByte.length);

		log.info("[start] " + message);

		Attachment attachment = new Attachment();
		List<Attachment> attachList = new ArrayList<Attachment>();
		attachment.setName(changeFilename(filename));
		try {
			attachment.setData(resize(dataByte, "JPG", targetHeight, targetWidth));
		} catch (Exception e) {
			attachment.setData(DefaultJPG.getJPG());
			log.error("format/JPGFormatImpl/Exception: [" + mailbox + "] [" + msgID + "] [" + filename + "]", e);
		}
		attachment.setSize(attachment.getData().length);
		if (attachment.getSize() != 4) {
			attachList.add(attachment);
		}

        log.info("[end] " + message);

		return attachList;
	}

	public byte[] resize(byte[] dataByte, String imgFormat, int targetHeight, int targetWidth) throws Exception {
		BufferedImage imgSrc = ImageIO.read(new ByteArrayInputStream(dataByte));

		byte[] fileData;
		if (imgSrc != null) {
			fileData = resize(imgSrc, imgFormat, targetHeight, targetWidth);
		} else {
			log.error("Unable to resize image!");
			fileData = dataByte;
		}
		return fileData;
	}

	public byte[] resize(BufferedImage imgSrc, String imgFormat, int targetHeight, int targetWidth) throws Exception {
		ImageResizer imageResizer = new JPGResizer();
		return imageResizer.resize(imgSrc, imgFormat, targetHeight, targetWidth);
	}

	public String changeFilename(String filename) {
		if (filename != null) {
			filename = filename.toLowerCase();
			if (!filename.endsWith(".jpg") && !filename.endsWith(".jpeg")) {
				filename = filename + ".jpg";
			}
		}
		return filename;
	}
}
