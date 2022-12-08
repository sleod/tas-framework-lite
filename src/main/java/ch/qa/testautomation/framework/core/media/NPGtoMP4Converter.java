package ch.qa.testautomation.framework.core.media;

import org.jcodec.api.awt.AWTSequenceEncoder;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.Rational;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

public class NPGtoMP4Converter {

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
                encoder.encodeImage(getScaledImage(image));
            }
            encoder.finish();
        } finally {
            NIOUtils.closeQuietly(out);
        }
    }

    private static BufferedImage getScaledImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        if (width % 2 != 0 || height % 2 != 0) {
            width += width % 2;
            height += height % 2;
            BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            target.getGraphics().drawImage(image, 0, 0, width, height, null);
            return target;
        } else {
            return image;
        }
    }
}