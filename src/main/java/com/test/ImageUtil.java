package com.test;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

public class ImageUtil {
	
	public static final int RATIO = 0;
	public static final int SAME = -1;
	
	
	public static final int WIDTH_800 = 800;
	public static final int WIDTH_400 = 400;
	public static final int WIDTH_160 = 160;

	public static final String WIDTH_800_NAME = "large";
	public static final String WIDTH_400_NAME = "medium";
	public static final String WIDTH_160_NAME = "small";

	
	public static InputStream resize(String fileName, InputStream is, int maxWidth, int maxHeight) throws IOException {

		Image srcImg = ImageIO.read(is);
		
		String suffix = fileName.substring(fileName.lastIndexOf('.')+1).toLowerCase();

		int srcWidth = srcImg.getWidth(null);
		int srcHeight = srcImg.getHeight(null);

		int destWidth = -1, destHeight = -1;

		if (maxWidth == SAME) {
			destWidth = srcWidth;
		} else if (maxWidth > 0) {
			destWidth = maxWidth;
		}

		if (maxHeight == SAME) {
			destHeight = srcHeight;
		} else if (maxHeight > 0) {
			destHeight = maxHeight;			
		}
		
		if (!(maxWidth == RATIO && maxHeight == RATIO)) {
			
			if(srcWidth < destWidth && maxHeight == RATIO) {
				destWidth = srcWidth;
			}
			
			if(srcHeight < destHeight && maxWidth == RATIO) {
				destWidth = srcHeight;
			}
		}

		if (maxWidth == RATIO && maxHeight == RATIO) {
			destWidth = srcWidth;
			destHeight = srcHeight;
		} else if (maxWidth == RATIO) {
			double ratio = ((double)destHeight) / ((double)srcHeight);
			destWidth = (int)((double)srcWidth * ratio);
		} else if (maxHeight == RATIO) {
			double ratio = ((double)destWidth) / ((double)srcWidth);
			destHeight = (int)((double)srcHeight * ratio);
		}

		Image imgTarget = srcImg.getScaledInstance(destWidth, destHeight, Image.SCALE_SMOOTH); 
		int pixels[] = new int[destWidth * destHeight]; 
		PixelGrabber pg = new PixelGrabber(imgTarget, 0, 0, destWidth, destHeight, pixels, 0, destWidth); 
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
			throw new IOException(e.getMessage());
		} 
		BufferedImage destImg = new BufferedImage(destWidth, destHeight, BufferedImage.TYPE_INT_RGB); 
		destImg.setRGB(0, 0, destWidth, destHeight, pixels, 0, destWidth); 

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ImageIO.write(destImg, suffix, os);
		InputStream destIs = new ByteArrayInputStream(os.toByteArray());

		return destIs;
	}
}
