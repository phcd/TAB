package com.archermind.txtbl.taskfactory.test;

import junit.framework.TestCase;

public class Assert2Array {
	public static void assertEquals(String[][] arrayException,
			String[][] arrayResult) {
		TestCase.assertEquals(arrayException.length, arrayResult.length);
		for (int i = 0; i < arrayException.length; i++) {
			TestCase.assertEquals(arrayException[i].length,
					arrayResult[i].length);
			for (int j = 0; j < arrayException[i].length; j++) {
				TestCase.assertEquals(arrayException[i][j], arrayResult[i][j]);
			}
		}
	}
}
