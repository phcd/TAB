package com.archermind.txtbl.attachment.format.inte.impl;

import com.archermind.txtbl.attachment.format.inte.AttachmentFormat;
import com.archermind.txtbl.attachment.format.inte.ITextFilter;
import com.archermind.txtbl.domain.Attachment;

import org.jboss.logging.Logger;

import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.rtf.RTFEditorKit;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class RTFFormatImpl implements AttachmentFormat {

	private static final Logger log = Logger.getLogger(RTFFormatImpl.class);

	private ITextFilter textFilter = null;
	private final static String CHARSET = "ISO-8859-1";

    public RTFFormatImpl(TextFilter textFilter) {
        this.textFilter = textFilter;
    }

    public List<Attachment> format(String mailbox, String msgID, String filename, byte[] dataByte, int targetHeight, int targetWidth) {
		log.debug("Start extractive RTF   file .......");

		List<Attachment> atts = new ArrayList<Attachment>();

        DefaultStyledDocument styledDoc = new DefaultStyledDocument();
        try {
            InputStream is = new ByteArrayInputStream (dataByte );
            new RTFEditorKit().read(is, styledDoc, 0);
            String bodyText = new String(styledDoc.getText(0, styledDoc.getLength()).getBytes("ISO8859_1"));
			bodyText = textFilter.filter(bodyText);
            Attachment attachment = new Attachment();
			attachment.setName(filename);
			attachment.setData(bodyText.getBytes(CHARSET));
			attachment.setSize(attachment.getData().length);
			atts.add(attachment);
        } catch ( Exception e) {
        	log.error("format/DOCFormatImpl/Exception: [" + mailbox + "] [" + msgID + "] [" + filename + "]", e);
        }

		return atts;
	}
}
