package com.archermind.txtbl.attachment.format.inte.impl;

import com.archermind.txtbl.domain.Attachment;
import com.archermind.txtbl.utils.FileReadUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Ignore;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.util.List;

public class DOCXFormatImplTest {
	/**
	 * A test case actually for debugging to locate bugs.
	 *
	 * @throws Exception
	 */
	@Test
    @Ignore
	public void attachmentTest() throws Exception {
		XmlBeanFactory formatFactory = new XmlBeanFactory(
				new ClassPathResource(
						"com/archermind/txtbl/attachment/format/attachmentFactory.xml"));
		File attachment = new File("E:/peekTest/docxExample.docx");
		long attachmentSize = attachment.length();
		byte[] attachmentBytes = FileReadUtils.getBytesFromFile(attachment);

		Assert.assertEquals(attachmentSize, attachmentBytes.length);
		DOCXFormatImpl df = (DOCXFormatImpl) (formatFactory.getBean("docx"));
		List<Attachment> list = df.format(null, null, "doctest",
				attachmentBytes, 0, 0);
		for (Attachment att : list) {
			System.out.println(new String(att.getData(), "ISO-8859-1"));
		}

	}
}
