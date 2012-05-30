package com.archermind.txtbl.utils;

import java.util.Random;

public class CreateRandomTools {

	private static final int codeCount = 20;

	private static final char[] codeSequence = { 'A', 'B', 'C', 'D', 'E', 'F',
			'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S',
			'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
			'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
			't', 'u', 'v', 'w', 'x', 'y', 'z' };

	public static String RandomKey() {
		Random random = new Random();
		String strRand = "";
		for (int i = 0; i < codeCount; i++) {
			strRand = strRand + codeSequence[random.nextInt(52)];
		}
		return strRand;
	}

	public static void main(String[] args) {
		for (int i = 0; i < 3000; i++)
			System.out.println(RandomKey() + "      " + RandomKey().length() + "     " + i);
	}
}
