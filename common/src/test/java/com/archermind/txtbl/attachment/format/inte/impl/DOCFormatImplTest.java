package com.archermind.txtbl.attachment.format.inte.impl;

import com.archermind.txtbl.domain.Attachment;
import com.archermind.txtbl.utils.FileReadUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.List;

public class DOCFormatImplTest {
	/**
	 * A test case actually for debugging to locate bugs.
	 *
	 * @throws Exception
	 */
	@Test
    @Ignore
	public void attachmentTest() throws Exception {
		DOCFormatImpl docFormat = new DOCFormatImpl(new TextFilter());

		String name = "doc_number_formatting_error.doc";
		File attachment = new File(ClassLoader.getSystemResource(name).getFile());
		long attachmentSize = attachment.length();
		byte[] attachmentBytes = FileReadUtils.getBytesFromFile(attachment);

		Assert.assertEquals(attachmentSize, attachmentBytes.length);
		List<Attachment> list = docFormat.format(null, null, name, attachmentBytes, 0, 0);
		for (Attachment att : list) {
			System.out.println(new String(att.getData(), "ASCII"));
		}

	}
}
