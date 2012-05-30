package com.archermind.txtbl.attachment.format.inte.impl;

import com.archermind.txtbl.attachment.format.inte.AttachmentFormat;
import com.archermind.txtbl.attachment.format.inte.ITextFilter;
import com.archermind.txtbl.attachment.format.support.PDFHelper;
import com.archermind.txtbl.attachment.format.support.DefaultJPG;
import com.archermind.txtbl.domain.Attachment;

import com.archermind.txtbl.utils.FinalizationUtils;
import com.archermind.txtbl.utils.StringUtils;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.PDFTextStripper;
import org.jboss.logging.Logger;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PDFFormatImpl implements AttachmentFormat {

    private static final Logger log = Logger.getLogger(PDFFormatImpl.class);

	private ITextFilter textFilter = null;
	private final static String CHARSET = "ascii";

    private static String ERROR = "format/PDFFormatImpl/Exception: [%s] [%s] [%s]" ;

    public PDFFormatImpl(TextFilter textFilter)
    {
        this.textFilter = textFilter;
    }

	public List<Attachment> format(String mailbox, String msgID, String filename, byte[] dataByte, int targetHeight, int targetWidth)
    {
        log.info(String.format("[start] extracting pdf contents mailbox=%s, msgID=%s, fileName=%s", mailbox, msgID, filename));

		byte[] fileData = null;
		PDDocument document = null;
		List<Attachment> attachList = new ArrayList<Attachment>();
		Attachment attachment = new Attachment();
		attachment.setName(filename);
		String error = "";
        InputStream inputStream = null;
		try
        {
			inputStream = new ByteArrayInputStream(dataByte);

			PDFParser parser = new PDFParser(inputStream);

            if (System.getProperty("os.name").indexOf("Windows") != -1) 
            {
				parser.setTempDirectory(new File("c:/"));
			}
            else
            {
				parser.setTempDirectory(new File("/tmp"));
			}
			
			parser.parse();

			document = parser.getPDDocument();

            attachList.addAll(getImages(mailbox, msgID, filename, targetHeight, targetWidth, document.getDocumentCatalog().getAllPages()));

			PDFTextStripper stripper = new PDFTextStripper();

			String text = stripper.getText(document);

			text = textFilter.filter(text);

			fileData = text.getBytes(CHARSET);
		}
        catch (Throwable t)
        {
			if (StringUtils.isNotEmpty(t.getMessage()) && t.getMessage().equals("Error decrypting document, details: Error: The supplied password does not match either the owner or user password in the document."))
            {
				error = " The attached PDF is password protected and cannot be viewed on your Peek";
                log.error(error);
			}
            else
            {
                error = "There was a problem processing the attachement so that it could be viewed on your Peek. A few files cannot be processed properly due to various reasons.";
                log.error(String.format(ERROR, mailbox, msgID, filename), t);
            }
		}
        finally
        {
            FinalizationUtils.close(document);
            FinalizationUtils.close(inputStream);
        }
        
        if (fileData == null)
        {
            StringBuffer sb = new StringBuffer();
            sb.append("File name is: ").append(filename).append("\n");
            sb.append("File size is: ").append(dataByte.length).append("\n").append(error);
            attachment.setData(sb.toString().getBytes());
            attachment.setSize(dataByte.length);
        }
        else
        {
            attachment.setData(fileData);
            attachment.setSize(attachment.getData().length);
        }
        
        attachList.add(0, attachment);

        log.info(String.format("[end] extracting pdf contents mailbox=%s, msgID=%s, fileName=%s", mailbox, msgID, filename));

		return attachList;
	}


    private List<Attachment> getImages(String mailbox, String msgID, String filename,  int targetHeight, int targetWidth, List pages) {
        log.info(String.format("[start] the convertion of pdf into images contais with parameters: mailbox=%s, msgID=%s, fileName=%s", mailbox, msgID, filename));
        ArrayList<Attachment> attachments = new ArrayList<Attachment>();
        Iterator iter = pages.iterator();
        JPGResizer imageResizer = new JPGResizer();
        int i = 1;
        int maxPdfPagesCount = PDFHelper.getMaxPdfPagesCount();
        while( iter.hasNext() &&  i <= maxPdfPagesCount )
        {
            Attachment attachment = new Attachment();
            attachments.add(attachment);
            try {
                PDPage page = (PDPage) iter.next();
                BufferedImage image = page.convertToImage();
                BufferedImage rotateImage = imageResizer.rotate90(image);
                byte[] data = imageResizer.resize(rotateImage, "JPG", targetHeight, targetWidth);
                attachment.setData(data);
                attachment.setSize(attachment.getData().length);
            } catch (Exception e) {
                attachment.setData(DefaultJPG.getJPG());        //TODO reveise should be error image
                attachment.setSize(attachment.getData().length);
                log.error(String.format(ERROR, mailbox, msgID, filename), e);
            }
            i++;

        }
        log.info(String.format("[end] the convertion of pdf into images contais with parameters: mailbox=%s, msgID=%s, fileName=%s", mailbox, msgID, filename));
        return attachments;
    }
}
