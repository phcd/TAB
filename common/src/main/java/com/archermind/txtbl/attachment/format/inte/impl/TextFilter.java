package com.archermind.txtbl.attachment.format.inte.impl;

import java.util.HashMap;
import java.util.Map;

import com.archermind.txtbl.attachment.format.inte.ITextFilter;
import com.archermind.txtbl.utils.MailUtils;

public class TextFilter implements ITextFilter {

	static Map<Character, Character> toASCII = new HashMap<Character, Character>();
	static {
		toASCII.put((char) 8216, (char) 39);
		toASCII.put((char) 8217, (char) 39);
		toASCII.put((char) 8221, (char) 34);
		toASCII.put((char) 8220, (char) 34);
		toASCII.put((char) 8226, (char) 183);
		toASCII.put((char) 183, (char) 183);
	}

	public String filter(String text) {
        return  MailUtils.clean(text, true, true);
	}

	public static void main(String[] args) {
		char c = 0xC2;
		char d = 0xB7;
		System.out.println(c);
		System.out.println(d);
		System.out.println(c >> 8);
		System.out.println(c & 0xFF);
		String s = "•";
		System.out.println((int) s.charAt(0));
	}
}
