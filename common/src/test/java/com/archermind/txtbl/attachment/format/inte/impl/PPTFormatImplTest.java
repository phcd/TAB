package com.archermind.txtbl.attachment.format.inte.impl;

import com.archermind.txtbl.domain.Attachment;
import com.archermind.txtbl.utils.FileReadUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Ignore;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class PPTFormatImplTest {
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
		File attachment = new File("/Users/manoj/o.ppt");
		long attachmentSize = attachment.length();
		byte[] attachmentBytes = FileReadUtils.getBytesFromFile(attachment);

		Assert.assertEquals(attachmentSize, attachmentBytes.length);
		PPTFormatImpl df = (PPTFormatImpl) (formatFactory.getBean("ppt"));
		JPGResizer jpgResizer = (JPGResizer) (formatFactory.getBean("jpgResizer"));
		jpgResizer.setFilterHeight(195);
		jpgResizer.setFilterWidth(320);
		List<Attachment> list = df.format(null, null, "doctest",
				attachmentBytes, 195, 320);
		for (Attachment att : list) {
			System.out.println(att.getName());
			writeFile(att);
		}

	}

	private void writeFile(Attachment attachment) throws IOException {
		FileOutputStream out = new FileOutputStream(attachment.getName());
		out.write(attachment.getData());
		out.close();
	}
}
