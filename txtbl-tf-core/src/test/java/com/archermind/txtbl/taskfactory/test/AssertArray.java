package com.archermind.txtbl.taskfactory.test;

import junit.framework.TestCase;

public class AssertArray {
	public static void assertEquals(String[] arrayException,
			String[] arrayResult) {
		TestCase.assertEquals(arrayException.length, arrayResult.length);
		for (int i = 0; i < arrayException.length; i++) {
			TestCase.assertEquals(arrayException[i], arrayResult[i]);
		}
	}
}
