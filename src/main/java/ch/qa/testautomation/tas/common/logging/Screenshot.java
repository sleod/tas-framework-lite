package ch.qa.testautomation.tas.common.logging;

import ch.qa.testautomation.tas.common.IOUtils.FileOperation;
import ch.qa.testautomation.tas.common.enumerations.ImageFormat;
import ch.qa.testautomation.tas.common.utils.DateTimeUtils;
import ch.qa.testautomation.tas.configuration.PropertyResolver;
import ch.qa.testautomation.tas.exception.ApollonBaseException;
import ch.qa.testautomation.tas.exception.ApollonErrorKeys;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;


/**
 * Object of Screenshot contains image and time properties
 */
public class Screenshot {

    private final LocalDateTime timeStamp = DateTimeUtils.getLocalDateTimeNow();
    private final LocalDate today = DateTimeUtils.getLocalDateToday();
    private final String testCaseName;
    private final String stepName;
    private final File screenshotFile;
    private final ImageFormat format = ImageFormat.getFormat(PropertyResolver.getScreenshotFormat());
    private final String pageSource;
    private File pageFile = null;

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public String getTestCaseName() {
        return testCaseName;
    }

    public File getScreenshotFile() {
        return screenshotFile;
    }

    public File getPageFile() {
        if (pageFile == null) {
            if (hasPageFile()) {
                pageFile = writePageFile(pageSource, PropertyResolver.getTestCaseReportLocation());
            }
        }
        return pageFile;
    }

    public Screenshot(BufferedImage imageData, String testCaseName, String stepName, String pageSource) {
        this.testCaseName = testCaseName;
        this.stepName = stepName;
        this.pageSource = pageSource;
        this.screenshotFile = writeImageToLocalFile(imageData, PropertyResolver.getTestCaseReportLocation());
    }

    public boolean hasPageFile() {
        return !pageSource.isEmpty();
    }

    public File writePageFile(String pageSource, String folderPath) {
        if (pageSource.isEmpty()) {
            return null;
        }
        String location = folderPath + DateTimeUtils.getFormattedDate(today, "yyyy-MM-dd") + "/" + testCaseName + "/";
        File folder = new File(location);
        String filePath = location + stepName + "_" + DateTimeUtils.formatLocalDateTime(timeStamp, "yyyy-MM-dd_HH-mm-ss") + ".html";
        File target = new File(filePath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        FileOperation.writeStringToFile(pageSource, target);
        return target;
    }

    /**
     * write image file to local
     *
     * @return image file
     */
    private File writeImageToLocalFile(BufferedImage image, String folderPath) {
        String location = folderPath + DateTimeUtils.getFormattedDate(today, "yyyy-MM-dd") + "/" + testCaseName + "/";
        File folder = new File(location);
        String filePath = location + stepName + "_" + DateTimeUtils.formatLocalDateTime(timeStamp, "yyyy-MM-dd_HH-mm-ss") + "." + format.value();
        File target = new File(filePath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        try {
            ImageIO.write(image, format.value(), target);
        } catch (IOException ex) {
            throw new ApollonBaseException(ApollonErrorKeys.CUSTOM_MESSAGE, ex, "Exception while taking screenshot with driver!");
        }
        return target;
    }
}
