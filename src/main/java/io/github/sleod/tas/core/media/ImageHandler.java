package io.github.sleod.tas.core.media;

import io.github.sleod.tas.common.IOUtils.FileOperation;
import io.github.sleod.tas.common.utils.DateTimeUtils;
import io.github.sleod.tas.configuration.PropertyResolver;
import io.github.sleod.tas.exception.ExceptionBase;
import io.github.sleod.tas.exception.ExceptionErrorKeys;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static io.github.sleod.tas.common.logging.SystemLogger.debug;
import static io.github.sleod.tas.common.logging.SystemLogger.info;


public class ImageHandler {

    private static final ThreadLocal<List<File>> imageStore = new ThreadLocal<>();

    public synchronized static String convertVideoBase64(String base64String, String name) {
        String filePath = PropertyResolver.getTestCaseReportLocation() +
                DateTimeUtils.getFormattedDate(DateTimeUtils.getLocalDateToday(), "yyyy-MM-dd") + "/" + name + "/"
                + "MobileAppTest_" + DateTimeUtils.formatLocalDateTime(DateTimeUtils.getLocalDateTimeNow(), "yyyy-MM-dd_HH-mm-ss") + "."
                + PropertyResolver.getVideoFormat();
        File video = new File(filePath);
        FileOperation.streamMediaStringToFile(base64String, video);
        return video.getAbsolutePath();
    }

    public static File comparePixel(File expected, File actual, IgnoredScreen ignoredScreen) {
        try {
            BufferedImage image1 = ImageIO.read(expected);
            BufferedImage image2 = ImageIO.read(actual);
            // Check if the images have the same dimensions
            if (image1.getWidth() != image2.getWidth() || image1.getHeight() != image2.getHeight()) {
                throw new ExceptionBase(ExceptionErrorKeys.CUSTOM_MESSAGE, "Virtual Reg. Images must have the same dimensions!");
            }
            // Create an image to store the diff result
            BufferedImage diffImage = new BufferedImage(image1.getWidth(), image1.getHeight(), BufferedImage.TYPE_INT_ARGB);
            // Get Graphics2D object to draw on the diff image
            Graphics2D g2d = diffImage.createGraphics();
            g2d.drawImage(image1, 0, 0, null); // Initially, draw the first image on the diff image
            // Compare pixel by pixel and highlight differences
            if (Objects.nonNull(ignoredScreen)) {
                info("Ignored screen: " + ignoredScreen.getLocation() + " - " + ignoredScreen.getSize());
            }
            for (int y = 0; y < image1.getHeight(); y++) {
                for (int x = 0; x < image1.getWidth(); x++) {
                    if (ignoreArea(ignoredScreen, x, y)) {
                        // Optionally, mark the ignored area in a specific color (e.g., transparent or gray)
                        diffImage.setRGB(x, y, Color.GRAY.getRGB());
                    } else {
                        // If the pixels are different, highlight the difference in red
                        if (image1.getRGB(x, y) != image2.getRGB(x, y)) {
                            diffImage.setRGB(x, y, Color.RED.getRGB());
                        }
                    }
                }
            }
            g2d.dispose(); // Release resources
            String filePath = actual.getParentFile().getAbsolutePath() + "/" + actual.getName().replace("_actual", "_diff");
            File imageFile = new File(filePath);
            boolean isOK = ImageIO.write(diffImage, "png", imageFile);
            if (!isOK) {
                debug("Write diff Image failed!");
            }
            return imageFile;
        } catch (Throwable ex) {
            throw new ExceptionBase(ExceptionErrorKeys.CUSTOM_MESSAGE, ex, "Compare Pixel went wrong!");
        }
    }

    private static boolean ignoreArea(IgnoredScreen ignoredScreen, int x, int y) {
        if (Objects.isNull(ignoredScreen)) return false;
        // Skip comparison for pixels inside the ignored area
        int ignoreX = ignoredScreen.getLocation().x;
        int ignoreY = ignoredScreen.getLocation().y;
        int ignoreWidth = ignoredScreen.getSize().width;
        int ignoreHeight = ignoredScreen.getSize().height;
        return x >= ignoreX && x < ignoreX + ignoreWidth && y >= ignoreY && y < ignoreY + ignoreHeight;
    }

    private static byte[] convertBufferedImage(BufferedImage bufferedImage, String format) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, format, outputStream);
        } catch (IOException ex) {
            throw new ExceptionBase(ExceptionErrorKeys.IOEXCEPTION_BY_WRITING, ex, "Error while converting BufferedImage to Byte Array!");
        }
        return outputStream.toByteArray();
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

}
