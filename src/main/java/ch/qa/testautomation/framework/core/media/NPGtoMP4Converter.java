package ch.qa.testautomation.framework.core.media;

import ch.qa.testautomation.framework.common.logging.SystemLogger;
import org.jcodec.api.awt.AWTSequenceEncoder;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.Rational;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.util.Arrays.asList;

public class NPGtoMP4Converter {

    /**
     * generate video using sequence images
     *
     * @param videoFilePath file name of video
     * @param pathImages    path of image store
     * @param imageExt      image format suffix
     */
    public static void generateVideoBySequenceImages(String videoFilePath, String pathImages, String imageExt, int fps) {
        try {
            Path directoryPath = Paths.get(new File(pathImages).toURI());
            if (Files.isDirectory(directoryPath)) {
                DirectoryStream<Path> stream = Files.newDirectoryStream(directoryPath, "*." + imageExt);
                List<File> filesList = new ArrayList<>();
                for (Path path : stream) {
                    filesList.add(path.toFile());
                }
                File[] files = new File[filesList.size()];
                filesList.toArray(files);
                sortByNumber(files);
                convertImageFiles(asList(files), videoFilePath, fps);
            }
        } catch (IOException ex) {
            SystemLogger.warn("Generate video failed!");
            SystemLogger.error(ex);
        }
    }

    /**
     * Convert list of images to mp4 video
     *
     * @param images        list of images
     * @param videoFilePath video file path
     * @param fps           fps of video
     * @throws IOException io exception
     */
    public static void convertImages(Collection<BufferedImage> images, String videoFilePath, int fps) throws IOException {
        SeekableByteChannel out = null;
        try {
            out = NIOUtils.writableFileChannel(videoFilePath);
            // for Android use: AndroidSequenceEncoder
            AWTSequenceEncoder encoder = new AWTSequenceEncoder(out, Rational.R(fps, 1));
            for (BufferedImage image : images) {
                encoder.encodeImage(image);
            }
            encoder.finish();
        } finally {
            NIOUtils.closeQuietly(out);
        }
    }

    public static void convertImageFiles(Collection<File> files, String videoFilePath, int fps) throws IOException {
        SeekableByteChannel out = null;
        try {
            out = NIOUtils.writableFileChannel(videoFilePath);
            // for Android use: AndroidSequenceEncoder
            AWTSequenceEncoder encoder = new AWTSequenceEncoder(out, Rational.R(fps, 1));
            for (File img : files) {
                // Generate the image, for Android use Bitmap
                BufferedImage image = ImageIO.read(img);
                // Encode the image
                encoder.encodeImage(image);
            }
            encoder.finish();
        } finally {
            NIOUtils.closeQuietly(out);
        }
    }

    private static void sortByNumber(File[] files) {
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                int n1 = extractNumber(o1.getName());
                int n2 = extractNumber(o2.getName());
                return n1 - n2;
            }

            private int extractNumber(String name) {
                int i = 0;
                try {
                    int s = name.lastIndexOf('_') + 1;
                    int e = name.lastIndexOf('.');
                    String number = name.substring(s, e);
                    i = Integer.parseInt(number);
                } catch (Exception e) {
                    // if filename does not match the format then default to 0
                }
                return i;
            }
        });
    }
}