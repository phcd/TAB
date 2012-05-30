package com.archermind.txtbl.attachment.format.support;

public class ExcelUtil {
	public static int MAX_NO_OF_CHARACTERS_PER_COLUMN = 100;
	public static int MAX_NO_OF_CHARACTERS_PER_COLUMN_FOR_OLD_EXCEL = 30;

	public String subString(String para, int noOfCharacters) {
		if (para.length() > noOfCharacters) {
			para = para.substring(0, noOfCharacters - 3);
			return para + "...";
		} else {
			return para + fillblank(noOfCharacters - para.length());
		}
	}

	private static String fillblank(int blank) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < blank; i++) {
			sb.append(" ");
		}
		return sb.toString();
	}

	public String subString(String cell) {
		return subString(cell, MAX_NO_OF_CHARACTERS_PER_COLUMN);
	}

}
