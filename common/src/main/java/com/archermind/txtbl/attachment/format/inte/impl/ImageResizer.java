package com.archermind.txtbl.attachment.format.inte.impl;

import java.awt.image.BufferedImage;

public interface ImageResizer {
	public byte[] resize(BufferedImage imgSrc, String imgFormat, int targetHeight, int targetWidth) throws Exception;
}
