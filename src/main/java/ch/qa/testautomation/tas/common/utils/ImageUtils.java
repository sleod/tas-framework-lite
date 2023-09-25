package ch.qa.testautomation.tas.common.utils;

import ch.qa.testautomation.tas.core.controller.FindImageInImage;
import ch.qa.testautomation.tas.core.controller.UserRobot;

import java.awt.*;
import java.awt.image.BufferedImage;

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

    public static Rectangle findSubImageIn(BufferedImage subImage, BufferedImage bigImage,
                                           double allowedPixelFailsPercent, int allowedPixelColorDifference) {
        return FindImageInImage.findSubImage(subImage, subImage.getWidth(), subImage.getHeight(), bigImage,
                bigImage.getWidth(), bigImage.getHeight(), allowedPixelFailsPercent, allowedPixelColorDifference);
    }

}
