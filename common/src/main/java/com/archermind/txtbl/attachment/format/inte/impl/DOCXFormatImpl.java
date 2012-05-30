package com.archermind.txtbl.attachment.format.inte.impl;

import com.archermind.txtbl.attachment.format.inte.AttachmentFormat;
import com.archermind.txtbl.attachment.format.inte.ITextFilter;
import com.archermind.txtbl.domain.Attachment;

import org.apache.poi.POIXMLDocument;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.jboss.logging.Logger;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class DOCXFormatImpl implements AttachmentFormat {

    private static final Logger log = Logger.getLogger(DOCXFormatImpl.class);

	private ITextFilter textFilter = null;
	private final static String CHARSET = "ISO-8859-1";

    public DOCXFormatImpl(TextFilter textFilter) {
        this.textFilter = textFilter;
    }

    public List<Attachment> format(String mailbox, String msgID, String filename, byte[] dataByte, int targetHeight, int targetWidth) {
		log.info("DOCXFormatImpl ....................");

		List<Attachment> tempList = new ArrayList<Attachment>();
		Attachment attachment = new Attachment();
		attachment.setSize(dataByte.length);
		attachment.setName(filename.replaceFirst("\\.docx$", ".doc")); //TODO should be removed when device can proccess .docx
		try {
			String OS = System.getProperty("os.name");
			String filename1 = (OS.indexOf("Windows") != -1) ? "c:/" + filename : "/tmp/" + filename;

			FileOutputStream fos = new FileOutputStream(filename1, true);

			fos.write(dataByte);
			fos.flush();
			fos.close();

			OPCPackage pack = POIXMLDocument.openPackage(filename1);

            XWPFWordExtractor extractor = new XWPFWordExtractor(pack);

			String textTmp = extractor.getText();
			textTmp = textFilter.filter(textTmp);

			attachment.setData(textTmp.getBytes(CHARSET));

		} catch (InvalidOperationException ioe) {
			String error = " The attached document is password protected and cannot be viewed on your Peek";
			log.error("format/DOCXFormatImpl/Exception: [" + mailbox + "] [" + msgID + "] [" + filename + "] [pic]", ioe);
			StringBuffer sb = new StringBuffer();
			sb.append("File name is: ").append(filename).append("\n");
			sb.append("File size is: ").append(dataByte.length).append("\n").append(error);
			attachment.setData(sb.toString().getBytes());
			attachment.setSize(dataByte.length);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("format/DOCXFormatImpl/Exception: [" + mailbox + "] [" + msgID + "] [" + filename + "] [pic]", e);
			StringBuffer sb = new StringBuffer();
			sb.append("File name is: ").append(filename).append("\n");
			sb.append("File size is: ").append(dataByte.length).append("\n");
			sb.append("There was a problem processing the attachement so that it could be viewed on your Peek. A few files cannot be processed properly due to various reasons. ");
			attachment.setData(sb.toString().getBytes());
			attachment.setSize(dataByte.length);
		}
		tempList.add(attachment);
		return tempList;
	}
}
