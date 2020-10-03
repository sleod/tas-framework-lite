package ch.sleod.testautomation.framework.common.logging;

import ch.sleod.testautomation.framework.common.IOUtils.FileOperation;
import ch.sleod.testautomation.framework.common.enumerations.ImageFormat;
import ch.sleod.testautomation.framework.common.utils.TimeUtils;
import ch.sleod.testautomation.framework.configuration.PropertyResolver;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static ch.sleod.testautomation.framework.common.logging.SystemLogger.error;


/**
 * Object of Screenshot contains image and time properties
 */
public class Screenshot {

    private final BufferedImage image;
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
                pageFile = writePageFile(pageSource, PropertyResolver.getDefaultTestCaseReportLocation());
            } catch (IOException ex) {
                error(ex);
            }
        }
        return pageFile;
    }

    public Screenshot(byte[] imageData, String testCaseName, String stepName, String pageSource) throws IOException {
        this.image = createImageFromBytes(imageData);
        this.testCaseName = testCaseName;
        this.stepName = stepName;
        this.pageSource = pageSource;
        this.screenshotFile = writeImageToLocalFile(PropertyResolver.getDefaultTestCaseReportLocation());
    }

    public Screenshot(BufferedImage imageData, String testCaseName, String stepName) throws IOException {
        this.image = imageData;
        this.testCaseName = testCaseName;
        this.stepName = stepName;
        this.pageSource = "";
        this.screenshotFile = writeImageToLocalFile(PropertyResolver.getDefaultTestCaseReportLocation());
    }

//    public Screenshot(BufferedImage imageData, String folderPath, String fileName, String format, String pageSource) {
//        this.image = imageData;
//        this.pageSource = pageSource;
//        this.testCaseName = "";
//        this.stepName = fileName;
//        this.screenshotFile = writeImageToLocalFile(folderPath);
//        this.format = ImageFormat.getFormat(format);
//    }

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
     * convert buffered image to bytes
     *
     * @param imageData buffered image
     * @return bytes
     */
    private byte[] convertToBytes(BufferedImage imageData) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, format.value(), out);
        return out.toByteArray();
    }


    /**
     * write image file to local
     *
     * @return image file
     */
    private File writeImageToLocalFile(String folderPath) throws IOException {
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
