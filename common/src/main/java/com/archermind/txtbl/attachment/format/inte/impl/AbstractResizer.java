package com.archermind.txtbl.attachment.format.inte.impl;

import com.archermind.txtbl.utils.SysConfigManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class AbstractResizer implements ImageResizer {
	private Integer filterWidth;
	private Integer filterHeight;

	public void setFilterWidth(Integer filterWidth) {
		this.filterWidth = filterWidth;
	}

	public void setFilterHeight(Integer filterHeight) {
		this.filterHeight = filterHeight;
	}

	public int getFilterWidth() {
		if (filterWidth != null) {
			return filterWidth;
		} else {
			return Integer.parseInt(SysConfigManager.instance().getValue("receiver.mail.image.filter.width"));
		}
	}

	public int getFilterHeight() {
		if (filterHeight != null) {
			return filterHeight;
		} else {
			return Integer.parseInt(SysConfigManager.instance().getValue("receiver.mail.image.filter.height"));
		}
	}

	protected byte[] toByteArray(BufferedImage image, String imgFormat) throws IOException {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        ImageIO.write(image, imgFormat, bs);
		return bs.toByteArray();
	}

    public BufferedImage rotate90(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage dimg = new BufferedImage(h, w, img.getType());
        Graphics2D g = dimg.createGraphics();
        g.translate((h-w)/2, (w-h)/2);
        g.rotate(Math.toRadians(90), w / 2, h / 2);
        g.drawImage(img, null, 0, 0);
        g.dispose();
        return dimg;
    }

    public byte[] resize(BufferedImage imgSrc, String imgFormat, int targetHeight, int targetWidth) throws Exception {
        int height = imgSrc.getHeight();
        int width = imgSrc.getWidth();
        int filterWidth = getFilterWidth();
        int filterHeight = getFilterHeight();
        if (width <= filterWidth || height <= filterHeight) {
            return new byte[4];
        }
        if (height > targetHeight || width > targetWidth) {
            float resizeMultiple;
            if ((float) height / targetHeight > (float) width / targetWidth) {
                resizeMultiple = (float) height / targetHeight;
            } else {
                resizeMultiple = (float) width / targetWidth;
            }
            targetHeight = (int) (height / resizeMultiple);
            targetWidth = (int) (width / resizeMultiple);
        } else {
            targetHeight = height;
            targetWidth = width;
        }
        Image image = imgSrc.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        int imageType = (imgSrc.getType() == BufferedImage.TYPE_CUSTOM ? BufferedImage.TYPE_INT_RGB : imgSrc.getType());
        return resize(imgFormat, targetHeight, targetWidth, image, imageType);
    }

    protected abstract byte[] resize(String imgFormat, int targetHeight, int targetWidth, Image image, int imageType) throws IOException;
}