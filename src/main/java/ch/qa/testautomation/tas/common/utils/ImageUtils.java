package ch.qa.testautomation.tas.common.utils;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import ch.qa.testautomation.tas.core.controller.FindImageInImage;
import ch.qa.testautomation.tas.core.controller.UserRobot;

public class ImageUtils {

    /**
     * find sub image in a big image with both specific size
     *
     * @param subImage                    sub image
     * @param allowedPixelFailsPercent    percentage of allowed pixel fails
     * @param allowedPixelColorDifference allowed pixel color diff
     * @return Rectangle of sub image, null if not found
     */
    public static Rectangle findSubImageOnScreen(BufferedImage subImage, double allowedPixelFailsPercent,
                                                 int allowedPixelColorDifference) throws AWTException {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return FindImageInImage.
                findSubImage(subImage, subImage.getWidth(), subImage.getHeight(), UserRobot.captureMainFullScreen(),
                        screenSize.width, screenSize.height, allowedPixelFailsPercent, allowedPixelColorDifference);
    }

    /**
     * Finds a sub-image within a larger image.
     *
     * @param subImage                    the sub-image to find
     * @param bigImage                    the larger image to search within
     * @param allowedPixelFailsPercent    percentage of allowed pixel fails
     * @param allowedPixelColorDifference allowed pixel color diff
     * @return Rectangle of sub image, null if not found
     */
    public static Rectangle findSubImageIn(BufferedImage subImage, BufferedImage bigImage,
                                           double allowedPixelFailsPercent, int allowedPixelColorDifference) {
        return FindImageInImage.findSubImage(subImage, subImage.getWidth(), subImage.getHeight(), bigImage,
                bigImage.getWidth(), bigImage.getHeight(), allowedPixelFailsPercent, allowedPixelColorDifference);
    }

}
