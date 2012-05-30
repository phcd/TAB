package com.archermind.txtbl.attachment.format.inte.impl;

import com.archermind.txtbl.attachment.format.inte.AttachmentFormat;
import com.archermind.txtbl.attachment.format.inte.ICharSetDetector;
import com.archermind.txtbl.attachment.format.inte.ITextFilter;
import com.archermind.txtbl.domain.Attachment;

import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;

public class TXTFormatImpl implements AttachmentFormat {

    private static final Logger log = Logger.getLogger(TXTFormatImpl.class);

	private ITextFilter textFilter = null;
	private ICharSetDetector charSetDetector = null;
	private final static String CHARSET = "ISO-8859-1";

    public TXTFormatImpl(TextFilter textFilter, CharSetDetector charSetDetector) {
        this.textFilter = textFilter;
        this.charSetDetector = charSetDetector;
    }

    public List<Attachment> format(String mailbox, String msgID, String filename, byte[] dataByte, int targetHeight, int targetWidth) {
		List<Attachment> attachList = null;
		try {
			String charSet = charSetDetector.detect(dataByte);
			String text = new String(dataByte, charSet);
			text = textFilter.filter(text);
			attachList = new ArrayList<Attachment>();
			Attachment attachment = new Attachment();
			attachment.setName(filename);
			attachment.setData(text.getBytes(CHARSET));
			if (attachment.getData() != null) {
				attachment.setSize(attachment.getData().length);
			}
			attachList.add(attachment);
		} catch (Exception e) {
			log.error("format/TXTFormatImpl/Exception: [" + mailbox + "] [" + msgID + "] [" + filename + "]", e);
		}
		return attachList;
	}
}
