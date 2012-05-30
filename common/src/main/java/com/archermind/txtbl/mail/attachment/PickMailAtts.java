package com.archermind.txtbl.mail.attachment;

public class PickMailAtts {
	public static boolean isPicture(String filename) {
		int p=filename.lastIndexOf(".");
		String filenameSuffix=filename.substring(p+1);
        return "jpg".equals(filenameSuffix) || "jpeg".equals(filenameSuffix) || "bmp".equals(filenameSuffix) || "png".equals(filenameSuffix) || "gif".equals(filenameSuffix);
    }
}
 

