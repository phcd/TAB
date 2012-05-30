package com.archermind.txtbl.attachment.format.inte.impl;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class JPGResizer extends AbstractResizer {

    protected byte[] resize(String imgFormat, int targetHeight, int targetWidth, Image image, int imageType) throws IOException {
        BufferedImage target = new BufferedImage(targetWidth, targetHeight, imageType);
        Graphics graphics = target.getGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();
        return toByteArray(target, imgFormat);
    }


}
