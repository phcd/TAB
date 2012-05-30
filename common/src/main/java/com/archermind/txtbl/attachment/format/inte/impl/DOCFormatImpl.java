package com.archermind.txtbl.attachment.format.inte.impl;

import com.archermind.txtbl.attachment.format.inte.AttachmentFormat;
import com.archermind.txtbl.attachment.format.inte.ITextFilter;
import com.archermind.txtbl.attachment.format.support.SupportAllImage;
import com.archermind.txtbl.domain.Attachment;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Picture;
import org.apache.poi.hwpf.usermodel.Range;
import org.jboss.logging.Logger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class DOCFormatImpl implements AttachmentFormat {

    private static final Logger log = Logger.getLogger(DOCFormatImpl.class);
	private ITextFilter textFilter = null;
	private final static String CHARSET = "ISO-8859-1";

    public DOCFormatImpl(TextFilter textFilter) {
        this.textFilter = textFilter;
    }

    /**
     * It seems that this method returns list of attachment where content is attachment #1 and any images embedded in the document
     * make up attachments 2 through N
     *
     * @param mailbox
     * @param msgID
     * @param filename
     * @param dataByte
     * @param targetHeight
     * @param targetWidth
     * @return
     */
    public List<Attachment> format(String mailbox, String msgID, String filename, byte[] dataByte, int targetHeight, int targetWidth) {
        log.debug(String.format("[start] extracting ms word contents mailbox=%s, msgID=%s, fileName=%s", mailbox, msgID, filename));

        byte[] fileData = null;

        HWPFDocument doc = null;

        List<Attachment> pictures = new ArrayList<Attachment>();
        String error = "";
        try {
            InputStream bis = new ByteArrayInputStream(dataByte);

            doc = new HWPFDocument(bis);

            Range range = doc.getRange();

            String originalText= range.text();

            String documentText = originalText.replaceAll("", "").replaceAll("", "|").replaceAll("", "--##--").replaceAll("\n", " \r\n").replaceAll("\r", " \r\n");

            StringBuilder buffer = new StringBuilder();

            buffer.append(documentText.split("HYPERLINK")[0]).append("\n");

            for (String link : documentText.split("HYPERLINK")) {
                if (link.indexOf("\"http:") != -1) {
                    if (link.indexOf("--##--") != -1) {
                        buffer.append(link.substring(link.indexOf("--##--")).replaceAll("--##--", " "));
                    }
                }
            }

            documentText = buffer.toString();
			documentText = textFilter.filter(documentText);
            fileData = documentText.getBytes(CHARSET);

		} catch (EncryptedDocumentException ede) {
			error = "The attached document is password protected and cannot be viewed on your Peek";
			log.error("format/DOCFormatImpl/Exception: [" + mailbox + "] ["
					+ msgID + "] [" + filename + "]", ede);
		} catch (Exception e) {
			error = "There was a problem processing the attachement so that it could be viewed on your Peek. A few files cannot be processed properly due to various reasons.  ";
			log.error("format/DOCFormatImpl/Exception: [" + mailbox + "] ["
					+ msgID + "] [" + filename + "]", e);
		}
        Attachment documentContentsAttachment = new Attachment();

        documentContentsAttachment.setName(filename);

        if (fileData == null) {
			StringBuffer sb = new StringBuffer();
			sb.append("File name is: ").append(filename).append("\n");
			sb.append("File size is: ").append(dataByte.length).append("\n").append(error);
            documentContentsAttachment.setData(sb.toString().getBytes());
            documentContentsAttachment.setSize(dataByte.length);
        } else {
            documentContentsAttachment.setData(fileData);
            documentContentsAttachment.setSize(documentContentsAttachment.getData().length);
        }

        pictures.add(documentContentsAttachment);

        try {
            @SuppressWarnings("unchecked")
            List<Picture> documentPictures = doc.getPicturesTable().getAllPictures();

            for (Picture picture : documentPictures) {
                List<Attachment> formattedPictures = SupportAllImage.format(picture.suggestFullFileName(), picture.getContent(), targetHeight, targetWidth);

                if (formattedPictures != null && formattedPictures.size() > 0) {
                    Attachment embeddedImageAttachment = formattedPictures.get(0);
                    embeddedImageAttachment.setName("." + changeFilename(picture.suggestFullFileName()));
                    pictures.add(embeddedImageAttachment);
                }
            }
        } catch (Exception e) {
            log.error("format/DOCFormatImpl/Exception: [" + mailbox + "] [" + msgID + "] [" + filename + "] [pic]", e);
        }

        log.debug(String.format("[end] extracting ms word contents mailbox=%s, msgID=%s, fileName=%s", mailbox, msgID, filename));

        return pictures;
    }

    private String changeFilename(String filename) {
        if (filename != null) {
            filename = filename.toLowerCase();
            if (!filename.endsWith(".jpg") && !filename.endsWith(".jpeg")) {
                filename = filename + ".jpg";
            }
		}
		return filename;
	}
}
