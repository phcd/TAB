package com.archermind.txtbl.attachment.format.inte.impl;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class PNGResizer extends AbstractResizer {

    protected byte[] resize(String imgFormat, int targetHeight, int targetWidth, Image image, int imageType) throws IOException {
        BufferedImage target = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = target.createGraphics();
        graphics2D.setBackground(Color.WHITE);
        graphics2D.fillRect(0, 0, targetWidth, targetHeight);
        graphics2D.drawImage(image, 0, 0, null);
        graphics2D.dispose();
        return toByteArray(target, imgFormat);
    }
}
