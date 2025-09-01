package ch.qa.testautomation.tas.common.logging;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.imageio.ImageIO;

import ch.qa.testautomation.tas.common.IOUtils.FileOperation;
import ch.qa.testautomation.tas.common.enumerations.ImageFormat;
import ch.qa.testautomation.tas.common.utils.DateTimeUtils;
import ch.qa.testautomation.tas.configuration.PropertyResolver;
import ch.qa.testautomation.tas.exception.ExceptionBase;
import ch.qa.testautomation.tas.exception.ExceptionErrorKeys;
import lombok.Getter;


/**
 * Object of Screenshot contains image and time properties
 */
public class Screenshot {

    @Getter
    private final String name;
    @Getter
    private final LocalDateTime timeStamp = DateTimeUtils.getLocalDateTimeNow();
    private final LocalDate today = DateTimeUtils.getLocalDateToday();
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
     * Creates a Screenshot object from a file.
     * @param srcFile the source file
     * @param testCaseName the test case name
     * @param stepName the step name
     */
    public Screenshot(File srcFile, String testCaseName, String stepName) {
        this.testCaseName = testCaseName;
        this.stepName = stepName;
        this.name = testCaseName + "_" + stepName;
        this.screenshotFile = writeImageToLocalFile(FileOperation.readImageFile(srcFile), PropertyResolver.getTestCaseReportLocation());
        FileOperation.deleteFile(srcFile);
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
        String location = folderPath + DateTimeUtils.getFormattedDate(today, "yyyy-MM-dd") + "/" + testCaseName + "/";
        String fileName = stepName + "_" + DateTimeUtils.formatLocalDateTime(timeStamp, "yyyy-MM-dd_HH-mm-ss");
        return writeImageToLocalFile(image, location, fileName);
    }

    /**
     * Writes the image file to the local file system.
     *
     * @param image the image data
     * @param location the location to save the image
     * @param fileName the name of the image file
     * @return the created image file
     */
    private File writeImageToLocalFile(BufferedImage image, String location, String fileName) {
        if (!location.endsWith("/") && !location.startsWith("//")) {
            location += "/";
        }
        location += "visualRegressionFiles/" + testCaseName + "/";
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
