package com.archermind.txtbl.attachment.format.inte.impl;

import com.archermind.txtbl.attachment.format.support.DefaultJPG;
import com.archermind.txtbl.attachment.format.support.GifDecoder;
import com.archermind.txtbl.domain.Attachment;

import org.jboss.logging.Logger;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

public class GIFFormatImpl extends PNGFormatImpl {

    private static final Logger log = Logger.getLogger(GIFFormatImpl.class);

    @Override
	public List<Attachment> format(String mailbox, String msgID, String filename, byte[] dataByte, int targetHeight, int targetWidth) {
        String message = String.format("resizing gif attachment mailbox=%s, msgID=%s, filename=%s, size=%s bytes", mailbox, msgID, filename, dataByte==null ? 0 : dataByte.length);

		log.info("[start] " + message);

		byte[] fileData;
		Attachment attachment = new Attachment();
		List<Attachment> attachList = new ArrayList<Attachment>();
		attachment.setName(changeFilename(filename));
		try {
			GifDecoder gifDecoder = new GifDecoder();
			gifDecoder.read(new ByteArrayInputStream(dataByte));
			int frameCount = gifDecoder.getFrameCount();
			if (frameCount > 0) {
				BufferedImage frame = gifDecoder.getFrame(0);
				fileData = resize(frame, "JPG", targetHeight, targetWidth);
			} else {
				fileData = DefaultJPG.getJPG();
			}
		} catch (Exception e) {
			fileData = DefaultJPG.getJPG();
			log.error("format/GIFFormatImpl/Exception: [" + mailbox + "] [" + msgID + "] [" + filename + "]", e);
		}

		fileData = fileData != null ? fileData : new byte[0];

		attachment.setData(fileData);
		attachment.setSize(fileData.length);
		attachList.add(attachment);

        log.info("[end] " + message);

		return attachList;
	}
}
