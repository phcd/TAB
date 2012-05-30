package com.archermind.txtbl.attachment.format.inte.impl;

import com.archermind.txtbl.attachment.format.inte.AttachmentFormat;
import com.archermind.txtbl.domain.Attachment;
import org.jboss.logging.Logger;

import java.util.LinkedList;
import java.util.List;

public class RAWXLSFormatImpl implements AttachmentFormat {

    private static final Logger log = Logger.getLogger(TXTFormatImpl.class);

    public List<Attachment> format(String mailbox, String msgID, String filename, byte[] dataByte, int targetHeight, int targetWidth) {
		try {
            log.info("formatting excel for mmx format/RAWXLSFormatImpl/: [" + mailbox + "] [" + msgID + "] [" + filename + "]");

			List<Attachment> attachList = new LinkedList<Attachment>();
			Attachment attachment = new Attachment();
			attachment.setName(filename);
			attachment.setData(dataByte);
			if (attachment.getData() != null) {
				attachment.setSize(attachment.getData().length);
			}
			attachList.add(attachment);
            return attachList;
		} catch (Exception e) {
			log.error("format/RAWXLSFormatImpl/Exception: [" + mailbox + "] [" + msgID + "] [" + filename + "]", e);
		}
		return null;
    }
}