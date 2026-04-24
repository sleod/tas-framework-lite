package io.github.sleod.tas.common.logging;

import io.github.sleod.tas.common.IOUtils.FileOperation;
import io.github.sleod.tas.common.enumerations.ImageFormat;
import io.github.sleod.tas.configuration.PropertyResolver;
import io.github.sleod.tas.exception.ExceptionBase;
import io.github.sleod.tas.exception.ExceptionErrorKeys;
import lombok.Getter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.UUID;


/**
 * Object of Screenshot contains image and time properties
 */
public class Screenshot {

    @Getter
    private final String name;
    @Getter
    private final String testCaseName;
    @Getter
    private final String stepName;
    @Getter
    private final File screenshotFile;
    private final ImageFormat format = ImageFormat.getFormat(PropertyResolver.getScreenshotFormat());

    /**
     * Creates a Screenshot object from image data.
     * @param imageData the image data
     * @param testCaseName the test case name
     * @param stepName the step name
     */
    public Screenshot(BufferedImage imageData, String testCaseName, String stepName) {
        this.testCaseName = testCaseName;
        this.stepName = stepName;
        this.screenshotFile = writeImageToLocalFile(imageData, PropertyResolver.getTestCaseReportLocation());
        this.name = testCaseName + "_" + stepName;
    }

    /**
     * Creates a Screenshot object from image data.
     * @param imageData the image data
     * @param testCaseName the test case name
     * @param stepName the step name
     * @param name the screenshot name
     * @param location the screenshot location
     */
    public Screenshot(BufferedImage imageData, String testCaseName, String stepName, String name, String location) {
        this.testCaseName = testCaseName;
        this.stepName = stepName;
        this.screenshotFile = writeImageToLocalFile(imageData, location, stepName + "_" + name);
        this.name = testCaseName + "_" + stepName + "_" + name;
    }

    /**
     * write image file to local
     *
     * @return image file
     */
    private File writeImageToLocalFile(BufferedImage image, String folderPath) {
        if (!folderPath.endsWith("/") && !folderPath.startsWith("//")) {
            folderPath += "/";
        }
        String location = folderPath + "/" + testCaseName + "/";
        return writeImageToLocalFile(image, location, UUID.randomUUID().toString());
    }

    /**
     * write image file to local
     *
     * @return image file
     */
    private File writeImageToLocalFile(BufferedImage image, String location, String fileName) {
        if (!location.endsWith("/") && !location.startsWith("//")) {
            location += "/";
        }
        //folder/Step_x/
        location += "visualRegressionFiles/" + stepName.substring(0, stepName.indexOf(".")).replace(" ", "_") + "/";
        File target = new File(location + fileName + "." + format.value());
        FileOperation.makeDirs(new File(location));
        try {
            ImageIO.write(image, format.value(), target);
        } catch (IOException ex) {
            throw new ExceptionBase(ExceptionErrorKeys.CUSTOM_MESSAGE, ex, "Exception while taking screenshot with driver!");
        }
        return target;
    }
}
