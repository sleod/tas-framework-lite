package ch.sleod.testautomation.framework.common.logging;

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

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public String getTestCaseName() {
        return testCaseName;
    }

    public File getScreenshotFile() {
        return screenshotFile;
    }

    public Screenshot(byte[] imageData, String testCaseName, String stepName) {
        this.image = createImageFromBytes(imageData);
        this.testCaseName = testCaseName;
        this.stepName = stepName;
        this.screenshotFile = writeImageToLocalFile(PropertyResolver.getDefaultTestCaseReportLocation());
    }

    public Screenshot(BufferedImage imageData, String testCaseName, String stepName) {
        this.image = imageData;
        this.testCaseName = testCaseName;
        this.stepName = stepName;
        this.screenshotFile = writeImageToLocalFile(PropertyResolver.getDefaultTestCaseReportLocation());
    }

    public Screenshot(BufferedImage imageData, String folderPath, String fileName, String format) {
        this.image = imageData;
        this.testCaseName = "";
        this.stepName = fileName;
        this.screenshotFile = writeImageToLocalFile(folderPath);
        this.format = ImageFormat.getFormat(format);
    }


    /**
     * write image file to local
     *
     * @return image file
     */
    private File writeImageToLocalFile(String folderPath) {
        String location = folderPath + TimeUtils.getFormattedDate(today, "yyyy-MM-dd") + "/" + testCaseName + "/";
        File folder = new File(location);
        String filePath = location + stepName + "_" + TimeUtils.formatLocalDateTime(timeStamp, "yyyy-MM-dd_HH-mm-ss") + "." + format.value();
        File target = new File(filePath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        try {
            ImageIO.write(image, format.value(), target);
        } catch (IOException ex) {
            SystemLogger.error(ex);
        }
        return target;
    }

    /**
     * create Image with byte array
     *
     * @param imageData byte array
     * @return BufferedImage
     */
    public static BufferedImage createImageFromBytes(byte[] imageData) {
        ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
        try {
            return ImageIO.read(bais);
        } catch (IOException e) {
            SystemLogger.error(e);
        }
        return null;
    }

    /**
     * convert buffered image to bytes
     *
     * @param imageData buffered image
     * @return bytes
     */
    private byte[] convertToBytes(BufferedImage imageData) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(image, format.value(), out);
            return out.toByteArray();
        } catch (IOException e) {
            SystemLogger.error(e);
        }
        return null;
    }

}
