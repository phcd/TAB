package com.archermind.txtbl.attachment.format.support;

import java.util.List;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

import com.archermind.txtbl.attachment.format.inte.AttachmentFormat;
import com.archermind.txtbl.domain.Attachment;

public class SupportAllImage {

	private static BeanFactory imageFactory = null;

    private static final Logger log = Logger.getLogger(SupportAllImage.class);

	static {
		try {
			imageFactory = new XmlBeanFactory(new ClassPathResource("com/archermind/txtbl/attachment/format/imageFactory.xml"));
			log.info("[initialization Image Factory success]");
		} catch (Exception e) {
			log.error("static/SupportAllImage/Exception: ", e);
		}
	}

	public static List<Attachment> format(String filename, byte[] dataByte, int targetHeight, int targetWidth) {
		String filenameSuffix = getFilenameSuffix(filename);
		if (imageFactory.containsBean(filenameSuffix)) {
			AttachmentFormat format = (AttachmentFormat) imageFactory.getBean(filenameSuffix);
			return format.format("", "", filename, dataByte, targetHeight, targetWidth);
		}
		return null;
	}

	private static String getFilenameSuffix(String filename) {
		String filenameSuffix = "";
		if (filename != null) {
			filename = filename.toLowerCase();
			filenameSuffix = filename.substring(filename.lastIndexOf(".") + 1, filename.length()).trim();
		}
		return filenameSuffix;
	}
}
