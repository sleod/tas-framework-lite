package ch.qa.testautomation.framework.core.media;

import ch.qa.testautomation.framework.common.IOUtils.FileOperation;
import ch.qa.testautomation.framework.common.logging.ScreenCapture;
import ch.qa.testautomation.framework.common.utils.DateTimeUtils;
import ch.qa.testautomation.framework.configuration.PropertyResolver;
import ch.qa.testautomation.framework.core.component.TestRunResult;
import ch.qa.testautomation.framework.exception.ApollonBaseException;
import ch.qa.testautomation.framework.exception.ApollonErrorKeys;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;


public class ImageHandler {

    private static final LinkedHashMap<String, List<File>> imageStore = new LinkedHashMap<>();

    /**
     * Handle image to take or not
     *
     * @param driver web driver for screen taken
     */
    public static synchronized void saveScreenshot(WebDriver driver) {
        if (PropertyResolver.isGenerateVideoEnabled()) {
            saveImagesForVideo((TakesScreenshot) driver);
        }
    }

    /**
     * finalize video with saved images
     *
     * @param testRunResult test result
     */
    public static synchronized void finishVideoRecording(TestRunResult testRunResult) {
        String tid = Thread.currentThread().getName();
        if (imageStore.get(tid) != null && !imageStore.get(tid).isEmpty()) {
            generateVideo(testRunResult);
        }
    }

    /**
     * save screenshot for making video to current list
     *
     * @param taker ScreenshotTaker
     */
    private static synchronized void saveImagesForVideo(TakesScreenshot taker) {
        String tid = Thread.currentThread().getName();
        imageStore.putIfAbsent(tid, new LinkedList<>());
        String location = PropertyResolver.getTestCaseReportLocation() +
                DateTimeUtils.getFormattedDate(DateTimeUtils.getLocalDateToday(), "yyyy-MM-dd") + "/";
        String filePath = location + UUID.randomUUID() + ".png";
        File imageFile = new File(filePath);
        try {
            ImageIO.write(ScreenCapture.takeScreenShot(taker), "png", imageFile);
        } catch (IOException ex) {
            throw new ApollonBaseException(ApollonErrorKeys.IOEXCEPTION_BY_WRITING, ex, filePath);
        }
        imageStore.get(tid).add(imageFile);
    }

    public synchronized static void convertVideoBase64(String base64String, TestRunResult testRunResult) {
        String filePath = PropertyResolver.getTestCaseReportLocation() +
                DateTimeUtils.getFormattedDate(DateTimeUtils.getLocalDateToday(), "yyyy-MM-dd") + "/" + testRunResult.getName() + "/"
                + "MobileAppTest_" + DateTimeUtils.formatLocalDateTime(DateTimeUtils.getLocalDateTimeNow(), "yyyy-MM-dd_HH-mm-ss") + "."
                + PropertyResolver.getVideoFormat();
        File video = new File(filePath);
        testRunResult.setVideoFilePath(video.getAbsolutePath());
        FileOperation.writeBytesToFile(Base64.getMimeDecoder().decode(base64String), video);
    }

    /**
     * Compress image with given quality in float (0.75, 0.50...)
     *
     * @param bufferedImage buffered image
     * @param quality       quality in float
     * @return compressed image
     * @throws IOException io exception
     */
    private BufferedImage compress(BufferedImage bufferedImage, float quality) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(bufferedImage, quality, out);
        return ImageIO.read(new ByteArrayInputStream(out.toByteArray()));
    }

    private static synchronized void write(BufferedImage image, float quality, ByteArrayOutputStream out) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersBySuffix("JPG");
        if (!writers.hasNext()) {
            throw new IllegalStateException("No writers found");
        }
        ImageWriter writer = writers.next();
        ImageOutputStream ios = ImageIO.createImageOutputStream(out);
        writer.setOutput(ios);
        ImageWriteParam param = writer.getDefaultWriteParam();
        if (quality >= 0) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
        }
        writer.write(null, new IIOImage(image, null, null), param);
    }

    /**
     * generate video and save file path to result
     *
     * @param testRunResult test run result
     */
    private static synchronized void generateVideo(TestRunResult testRunResult) {
        String tid = Thread.currentThread().getName();
        String filePath = PropertyResolver.getTestCaseReportLocation() +
                DateTimeUtils.getFormattedDate(DateTimeUtils.getLocalDateToday(), "yyyy-MM-dd") + "/" + testRunResult.getName() + "/"
                + "TestRunVideo_" + DateTimeUtils.formatLocalDateTime(DateTimeUtils.getLocalDateTimeNow(), "yyyy-MM-dd_HH-mm-ss") + "."
                + PropertyResolver.getVideoFormat();
        try {
            NPGtoMP4Converter.convertImageFiles(imageStore.get(tid), filePath, 1);
            testRunResult.setVideoFilePath(new File(filePath).getAbsolutePath());
            for (File file : imageStore.get(tid)) {
                assert file.delete();
            }
        } catch (IOException ex) {
            throw new ApollonBaseException(ApollonErrorKeys.CUSTOM_MESSAGE, ex, "Video Generation Failed!");
        }
        imageStore.get(tid).clear();
    }
}
