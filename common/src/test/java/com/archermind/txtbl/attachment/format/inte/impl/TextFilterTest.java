package com.archermind.txtbl.attachment.format.inte.impl;

import org.junit.Assert;
import org.junit.Test;

public class TextFilterTest {
	/**
	 * A test case actually for debugging to locate bugs.
	 *
	 * @throws Exception
	 */
	@Test
	public void filter() throws Exception {
		TextFilter tf = new TextFilter();
		String s = "t";
		//System.out.println((int) s.charAt(0));
		String out = tf.filter(s);
		//byte[] attachmentBytes = FileReadUtils.getBytesFromFile(attachment);
		System.out.println(String.format("%s %s", out, Integer.toHexString((int) out.charAt(0))));
		Assert.assertTrue(out.length() > 0);
		//System.out.println((int) out.charAt(0));
	}
}
