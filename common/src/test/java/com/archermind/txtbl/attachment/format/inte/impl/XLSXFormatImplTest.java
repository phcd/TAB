package com.archermind.txtbl.attachment.format.inte.impl;

import com.archermind.txtbl.attachment.format.support.ExcelResponsesToWeb;
import com.archermind.txtbl.domain.Attachment;
import com.archermind.txtbl.utils.FileReadUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.util.List;

public class XLSXFormatImplTest {
	/**
	 * A test case actually for debugging to locate bugs.
	 *
	 * @throws Exception
	 */
	public void attachmentTest() throws Exception {
		XmlBeanFactory formatFactory = new XmlBeanFactory(new ClassPathResource(
				"com/archermind/txtbl/attachment/format/attachmentFactory.xml"));
		File attachment = new File("E:/peekTest/xlsxTest.xlsx");
		long attachmentSize = attachment.length();
		byte[] attachmentBytes = FileReadUtils.getBytesFromFile(attachment);

		Assert.assertEquals(attachmentSize, attachmentBytes.length);

		List<Attachment> list = ((XLSXFormatImpl) (formatFactory.getBean("xlsx"))).format(null, null, "filename",
				attachmentBytes, 0, 0);
		System.out.println("How many sheets in this xlsx file: " + list.size());
		System.out.println(list.get(0).getName());

		String str = new String(list.get(0).getData());
		System.out.println(str);
		String txt = new ExcelResponsesToWeb().ResponsesToWeb(1, 1, str.getBytes());
		System.out.println(txt);
		txt = new ExcelResponsesToWeb().ResponsesToWeb(1, 2, str.getBytes());
		System.out.println(txt);
		txt = new ExcelResponsesToWeb().ResponsesToWeb(1, 3, str.getBytes());
		System.out.println(txt);
		// txt = new ExcelResponsesToWeb().ResponsesToWeb(2, 1, str
		// .getBytes());
		// System.out.println(txt);
		// txt = new ExcelResponsesToWeb().ResponsesToWeb(3, 1, str
		// .getBytes());
		// System.out.println(txt);
		// txt = new ExcelResponsesToWeb().ResponsesToWeb(3, 2, str
		// .getBytes());
		// System.out.println(txt);
	}

	@Test
    @Ignore
	public void formatErrorTest() throws Exception {
		XLSXFormatImpl format = new XLSXFormatImpl(new TextFilter());

		String name = "xlsx_formatting_error.xlsx";
		//		long attachmentSize = attachment.length();
		byte[] attachmentBytes = FileReadUtils.getBytesFromFile(new File(ClassLoader.getSystemResource(name).getFile()));

		List<Attachment> sheets = format.format(null, null, name, attachmentBytes, 0, 0);

		System.out.println(sheets.size());
		for (Attachment attachment : sheets) {
			System.out.println(attachment.getComment());
			String str = new String(attachment.getData());
			System.out.println(str);
			String txt = new ExcelResponsesToWeb().ResponsesToWeb(1, 1, str.getBytes());
			System.out.println(txt);
			txt = new ExcelResponsesToWeb().ResponsesToWeb(1, 2, str.getBytes());
			System.out.println(txt);
			txt = new ExcelResponsesToWeb().ResponsesToWeb(1, 3, str.getBytes());
			System.out.println(txt);
		}
	}
}
