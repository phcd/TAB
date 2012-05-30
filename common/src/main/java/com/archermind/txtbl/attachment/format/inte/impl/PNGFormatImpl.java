package com.archermind.txtbl.attachment.format.inte.impl;

import java.awt.image.BufferedImage;

public class PNGFormatImpl extends JPGFormatImpl {

	public byte[] resize(BufferedImage imgSrc, String imgFormat, int targetHeight, int targetWidth) throws Exception {
		ImageResizer imageResizer = new PNGResizer();
		return imageResizer.resize(imgSrc, imgFormat, targetHeight, targetWidth);
	}

}
