package ch.sleod.testautomation.framework.core.media;

import ch.sleod.testautomation.framework.common.logging.ScreenCapture;
import ch.sleod.testautomation.framework.common.logging.SystemLogger;
import ch.sleod.testautomation.framework.common.utils.TimeUtils;
import ch.sleod.testautomation.framework.configuration.PropertyResolver;
import ch.sleod.testautomation.framework.core.component.TestRunResult;
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

import static ch.sleod.testautomation.framework.common.logging.SystemLogger.error;
import static ch.sleod.testautomation.framework.common.logging.SystemLogger.warn;


public class ImageHandler {

    private static final LinkedHashMap<String, LinkedList<File>> imageStore = new LinkedHashMap<>();

    /**
     * Handle image to take or not
     *
     * @param driver web driver for screen taken
     */
    public static synchronized void handleImage(WebDriver driver) {
        if (PropertyResolver.isGenerateVideoEnabled()) {
            saveImagesForVideo((TakesScreenshot) driver);
        }
    }

    /**
     * before test case, prepare recording with test case name
     *
     * @param testRunResult e.g. parentFolderName.testCaseName to make unique id
     */
    public static synchronized void prepareVideoRecording(TestRunResult testRunResult) {
        finishVideoRecording(testRunResult);
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
        imageStore.computeIfAbsent(tid, k -> new LinkedList<>());
        BufferedImage screenshot = ScreenCapture.takeScreenShot(taker);
        String location = PropertyResolver.getDefaultTestCaseReportLocation() + TimeUtils.getFormattedDate(TimeUtils.getLocalDateToday(), "yyyy-MM-dd") + "/";
        String filePath = location + UUID.randomUUID() + ".png";
        File imageFile = new File(filePath);
        try {
            ImageIO.write(screenshot, "png", imageFile);
        } catch (IOException ex) {
            SystemLogger.error(ex);
        }
        imageStore.get(tid).add(imageFile);
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
        Iterator writers = ImageIO.getImageWritersBySuffix("JPG");
        if (!writers.hasNext()) {
            throw new IllegalStateException("No writers found");
        }
        ImageWriter writer = (ImageWriter) writers.next();
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
        String filePath = PropertyResolver.getDefaultTestCaseReportLocation() +
                TimeUtils.getFormattedDate(TimeUtils.getLocalDateToday(), "yyyy-MM-dd") + "/" + testRunResult.getName() + "/"
                + "TestRunVideo_" + TimeUtils.formatLocalDateTime(TimeUtils.getLocalDateTimeNow(), "yyyy-MM-dd_HH-mm-ss") + "."
                + PropertyResolver.getDefaultVideoFormat();
        try {
            NPGtoMP4Converter.convertImageFiles(imageStore.get(tid), filePath, PropertyResolver.getDefaultVideoFPS());
            testRunResult.setVideoFilePath(new File(filePath).getAbsolutePath());
            for (File file : imageStore.get(tid)) {
                file.delete();
            }
        } catch (IOException ex) {
            warn("Video Generation Failed!");
            error(ex);
        }
        imageStore.get(tid).clear();
    }
}
