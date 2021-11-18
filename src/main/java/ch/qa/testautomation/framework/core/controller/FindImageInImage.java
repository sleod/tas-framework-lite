package ch.qa.testautomation.framework.core.controller;

import ch.qa.testautomation.framework.common.logging.SystemLogger;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.Assert.assertNotNull;


public class FindImageInImage {

    public static Rectangle findSubImage(BufferedImage subImage, int subWidth, int subHeight, BufferedImage img, int width, int height,
                                         double allowedPixelFailsPercent, int allowedPixelColorDifference) {
        SystemLogger.trace("SUB_IMAGE_TOTAL_PIXELS=" + subWidth * subHeight);
        SystemLogger.trace("IMAGE_TOTAL_PIXELS=" + width * height);
        int allowedPixelFailsCount = (int) (((double) (subWidth * subHeight) / 100.0) * allowedPixelFailsPercent);
        SystemLogger.trace("ALLOWED_PIXEL_FAILS_COUNT=" + allowedPixelFailsCount);
        long startMillis = System.currentTimeMillis();
        int xOffsetMax = width - subWidth;
        Rectangle rectangle = null;
        for (int xOffset = 0; xOffset <= xOffsetMax; xOffset++) {
            int yOffsetMax = height - subHeight;
            for (int yOffset = 0; yOffset <= yOffsetMax; yOffset++) {
                if (subImageIsAtOffset(subImage, img, xOffset, yOffset, allowedPixelFailsCount, allowedPixelColorDifference)) {
                    SystemLogger.trace("FOUND=YES");
                    SystemLogger.trace("FOUND_AT_X=" + xOffset);
                    SystemLogger.trace("FOUND_AT_Y=" + yOffset);
                    SystemLogger.trace("RUN_TIME_MILLIS=" + (System.currentTimeMillis() - startMillis));
                    rectangle = new Rectangle(xOffset, yOffset, subWidth, subHeight);
                }
            }
        }
        if (rectangle == null) {
            SystemLogger.trace("FOUND=NO");
        }
        SystemLogger.trace("RUN_TIME_MILLIS=" + (System.currentTimeMillis() - startMillis));
        assertNotNull("The given image was not found!", rectangle);
        return rectangle;
    }

    private static boolean subImageIsAtOffset(BufferedImage subImage, BufferedImage img, int xOffset, int yOffset,
                                              int allowedPixelFailsCount, int allowedPixelColorDifference) {
        int width = img.getWidth();
        int height = img.getHeight();
        int subWidth = subImage.getWidth();
        int subHeight = subImage.getHeight();
        int actualPixelFailsCount = 0;

        for (int subImageX = 0; subImageX < subWidth; subImageX++) {
            if (xOffset + subImageX >= width) {
                SystemLogger.trace("Should not occur-1");
                return false;
            }
            for (int subImageY = 0; subImageY < subHeight; subImageY++) {
                if (yOffset + subImageY >= height) {
                    SystemLogger.trace("Should not occur-2");
                    return false;
                }
                int subImagePixel = subImage.getRGB(subImageX, subImageY);
                int r = (subImagePixel >> 0) & 0x000000ff;
                int g = (subImagePixel >> 1) & 0x000000ff;
                int b = (subImagePixel >> 2) & 0x000000ff;
                int v1 = r + g + b;

                int x = xOffset + subImageX;
                int y = yOffset + subImageY;
                int imgPixel = img.getRGB(x, y);
                r = (imgPixel >> 0) & 0x000000ff;
                g = (imgPixel >> 1) & 0x000000ff;
                b = (imgPixel >> 2) & 0x000000ff;
                int v2 = r + g + b;
                int colorDifference = Math.abs(v1 - v2);
                if (colorDifference > allowedPixelColorDifference) {
                    actualPixelFailsCount++;
                    if (actualPixelFailsCount > allowedPixelFailsCount) {
                        return false;
                    }
                }
            }
        }
        SystemLogger.trace("FAILED_PIXEL_COUNT=" + actualPixelFailsCount);
        double p = actualPixelFailsCount / ((double) subWidth * (double) subHeight / 100.0);
        SystemLogger.trace("FAILED_PIXEL_PERCENT=" + p);
        return true;
    }
}
