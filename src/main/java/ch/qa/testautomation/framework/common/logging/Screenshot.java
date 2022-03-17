package ch.qa.testautomation.framework.common.logging;

import ch.qa.testautomation.framework.common.IOUtils.FileOperation;
import ch.qa.testautomation.framework.common.enumerations.ImageFormat;
import ch.qa.testautomation.framework.common.utils.TimeUtils;
import ch.qa.testautomation.framework.configuration.PropertyResolver;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static ch.qa.testautomation.framework.common.logging.SystemLogger.error;


/**
 * Object of Screenshot contains image and time properties
 */
public class Screenshot {

    private final LocalDateTime timeStamp = TimeUtils.getLocalDateTimeNow();
    private final LocalDate today = TimeUtils.getLocalDateToday();
    private final String testCaseName;
    private final String stepName;
    private final File screenshotFile;
    private ImageFormat format = ImageFormat.getFormat(PropertyResolver.getDefaultScreenshotFormat());
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
            try {
                if (hasPageFile()) {
                    pageFile = writePageFile(pageSource, PropertyResolver.getDefaultTestCaseReportLocation());
                }
            } catch (IOException ex) {
                error(ex);
            }
        }
        return pageFile;
    }

    public Screenshot(byte[] imageData, String testCaseName, String stepName, String pageSource) throws IOException {
        this.testCaseName = testCaseName;
        this.stepName = stepName;
        this.pageSource = pageSource;
        this.screenshotFile = writeImageToLocalFile(createImageFromBytes(imageData), PropertyResolver.getDefaultTestCaseReportLocation());
    }

    public Screenshot(BufferedImage imageData, String testCaseName, String stepName) throws IOException {
        this.testCaseName = testCaseName;
        this.stepName = stepName;
        this.pageSource = "";
        this.screenshotFile = writeImageToLocalFile(imageData, PropertyResolver.getDefaultTestCaseReportLocation());
    }


    /**
     * create Image with byte array
     *
     * @param imageData byte array
     * @return BufferedImage
     */
    public static BufferedImage createImageFromBytes(byte[] imageData) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
        return ImageIO.read(bais);
    }

    public boolean hasPageFile() {
        return !pageSource.isEmpty();
    }

    public File writePageFile(String pageSource, String folderPath) throws IOException {
        if (pageSource.isEmpty()) {
            return null;
        }
        String location = folderPath + TimeUtils.getFormattedDate(today, "yyyy-MM-dd") + "/" + testCaseName + "/";
        File folder = new File(location);
        String filePath = location + stepName + "_" + TimeUtils.formatLocalDateTime(timeStamp, "yyyy-MM-dd_HH-mm-ss") + ".html";
        File target = new File(filePath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        FileOperation.writeBytesToFile(pageSource.getBytes(), target);
        return target;
    }

    /**
     * write image file to local
     *
     * @return image file
     */
    private File writeImageToLocalFile(BufferedImage image, String folderPath) throws IOException {
        String location = folderPath + TimeUtils.getFormattedDate(today, "yyyy-MM-dd") + "/" + testCaseName + "/";
        File folder = new File(location);
        String filePath = location + stepName + "_" + TimeUtils.formatLocalDateTime(timeStamp, "yyyy-MM-dd_HH-mm-ss") + "." + format.value();
        File target = new File(filePath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        ImageIO.write(image, format.value(), target);
        return target;
    }
}