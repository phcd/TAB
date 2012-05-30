package com.archermind.txtbl.attachment.format.inte.impl;

import com.archermind.txtbl.attachment.format.inte.AttachmentFormat;
import com.archermind.txtbl.domain.Attachment;
import org.apache.poi.hslf.exceptions.EncryptedPowerPointFileException;
import org.apache.poi.hslf.model.Slide;
import org.apache.poi.hslf.usermodel.SlideShow;
import org.jboss.logging.Logger;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PPTFormatImpl implements AttachmentFormat {

	private static final Logger log = Logger.getLogger(PPTFormatImpl.class);

	private ImageResizer imageResizer;

	public void setImageResizer(ImageResizer imageResizer) {
		this.imageResizer = imageResizer;
	}

	public List<Attachment> format(String mailbox, String msgID,
			String filename, byte[] dataByte, int targetHeight, int targetWidth) {
		log.debug("Start converting PPT to image .......");
        log.info("PPTFormatImpl  format ................" + "  filename: " + filename + "  mailbox: " + mailbox + "  msgID: " + msgID);

		List<Attachment> tempList = new ArrayList<Attachment>();

		try {
			InputStream bis = new ByteArrayInputStream(dataByte);
			SlideShow ppt = new SlideShow(bis);

			/* this needs to be changed to passed height and width and image scaled down */
			Dimension pgsize = ppt.getPageSize();

			Slide[] slide = ppt.getSlides();
			for (int i = 0; i < slide.length; i++) {
				Attachment attachment = new Attachment();
				BufferedImage img = new BufferedImage(pgsize.width, pgsize.height, BufferedImage.TYPE_INT_RGB);
				Graphics2D graphics = img.createGraphics();
	            //clear the drawing area
	            graphics.setPaint(Color.white);
	            graphics.fill(new Rectangle2D.Float(0, 0, pgsize.width, pgsize.height));

	            //render
	            slide[i].draw(graphics);
	            //resize
	            byte[] fileData = resize(img, targetHeight, targetWidth);
	            // save the output
				String name = String.format("slide-%s.jpeg", (i+1));
				log.debug(name);
				attachment.setName(name);
				attachment.setData(fileData);
				attachment.setSize(attachment.getData().length);
				tempList.add(attachment);
			}
		} catch (EncryptedPowerPointFileException ede) {
			log.error("format/PPTFormatImpl/Exception: [" + mailbox + "] [" + msgID + "] [" + filename + "]", ede);
		} catch (Exception e) {
			log.error("format/PPTFormatImpl/Exception: [" + mailbox + "] [" + msgID + "] [" + filename + "]", e);
		}
		return tempList;
	}

	public byte[] resize(BufferedImage imgSrc, int targetHeight, int targetWidth) throws Exception {
		return imageResizer.resize(imgSrc, "jpeg", targetHeight, targetWidth);
	}
}
