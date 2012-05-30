package com.archermind.txtbl.attachment.format.inte.impl;

import com.archermind.txtbl.attachment.format.support.ExcelResponsesToWeb;
import com.archermind.txtbl.domain.Attachment;
import com.archermind.txtbl.utils.FileReadUtils;
import com.archermind.txtbl.utils.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class XLSFormatImplTest {
	/**
	 * A test case actually for debugging to locate bugs.
	 *
	 * @throws Exception
	 */
	@Test
	public void attachmentTest() throws Exception {

/* This test crashes due to the hardcoded path (DB)
		XmlBeanFactory formatFactory = new XmlBeanFactory(
				new ClassPathResource(
						"com/archermind/txtbl/attachment/format/attachmentFactory.xml"));
		File attachment = new File("/Users/jmpak/Desktop/test_xls.xls");
		long attachmentSize = attachment.length();
		byte[] attachmentBytes = FileReadUtils.getBytesFromFile(attachment);

		Assert.assertEquals(attachmentSize, attachmentBytes.length);

		List<Attachment> list = ((XLSFormatImpl) (formatFactory.getBean("xls")))
				.format(null, null, "filename", attachmentBytes, 0, 0);
//		System.out.println("How many sheets in this xls file: " + list.size());
		String comment = list.get(0).getComment();
		System.out.println(StringUtils.parseString(comment, ";")[0]);
		System.out.println(StringUtils.parseString(comment, ";")[1]);
		System.out.println(comment);
		String str = new String(list.get(0).getData());
//		System.out.println(str);
		ExcelResponsesToWeb excelResponsesToWeb = new ExcelResponsesToWeb();
		String txt = excelResponsesToWeb.ResponsesToWeb(1, 1, str
				.getBytes());
		System.out.println(txt);
*/
	}

    public static void main(String[] args) throws IOException
    {
        File attachment = new File("/Users/jmpak/Desktop/attachment-logs/attachments/Activation MTD.xlsx");
        byte[] attachmentBytes = FileReadUtils.getBytesFromFile(attachment);
        new XLSXFormatImpl(new TextFilter()).format(null, null, "filename", attachmentBytes, 0, 0);
    }
}
