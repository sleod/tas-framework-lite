package io.github.sleod.tas.common.utils;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import io.github.sleod.tas.exception.ExceptionBase;
import io.github.sleod.tas.exception.ExceptionErrorKeys;

import javax.imageio.ImageIO;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * Utility class for QR code operations such as reading from images and creating images from base64 strings.
 */
public class QrCodeUtils {

    /**
     * @param filePath filepath
     * @return String QR code
     * Read QR Code from image like .jpg, png... and return it as string
     */
    public static String readQrCodeFromImage(String filePath) {
        try {
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource
                    (ImageIO.read(new FileInputStream(filePath)))));
            return new MultiFormatReader().decode(binaryBitmap).getText();
        } catch (NotFoundException | IOException ex) {
            throw new ExceptionBase(ExceptionErrorKeys.IOEXCEPTION_GENERAL, ex, "reading QR Code with image " + filePath);
        }
    }

    /**
     * S
     *
     * @param fileOutputPath       output path
     * @param base64EncodedPicture Create a qr code image from base64 encoded picture String. Example: MQ MEssages that contains the picture in jpg format
     */
    public static void createQrCodeImageFromEncodedString(String fileOutputPath, String base64EncodedPicture) {
        byte[] imageByteArray = Base64.getDecoder().decode(base64EncodedPicture);
        FileOutputStream imageOutFile = null;
        try {
            imageOutFile = new FileOutputStream(fileOutputPath);
            imageOutFile.write(imageByteArray);
            imageOutFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (imageOutFile != null) {
            try {
                imageOutFile.close();
            } catch (IOException ex) {
                throw new ExceptionBase(ExceptionErrorKeys.IOEXCEPTION_GENERAL, ex, "writing file: " + fileOutputPath);
            }
        }
    }

}


