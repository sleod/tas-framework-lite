package ch.raiffeisen.testautomation.framework.common.utils;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.imageio.ImageIO;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

import static ch.raiffeisen.testautomation.framework.common.logging.SystemLogger.error;

/**
 * @author UEX13996
 *
 * Utils for manipulating and reading of generated QR codes.
 *
 */

public class QrCodeUtils {

    /**
     * @param filePath
     * @return String QR code
     * Read QR Code from image like .jpg, png... and return it as string
     */
    public static String readQrCodeFromImage(String filePath){
        Result qrCodeResult = null;
        try {
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource
                    (ImageIO.read(new FileInputStream(filePath)))));
             qrCodeResult = new MultiFormatReader().decode(binaryBitmap);

        }catch (Exception e){
           error(e);
        }
        return qrCodeResult.getText();    }

    /**S
     * @param fileOutputPath
     * @param base64EncodedPicture
     *
     * Create an qr code image from base64 encoded picture String. Example: MQ MEssages that contains the picture in jpg format
     *
     */
    public static void createQrCodeImageFromEncodedString(String fileOutputPath , String base64EncodedPicture ){
        byte[] imageByteArray = Base64.getDecoder().decode(base64EncodedPicture);
        FileOutputStream imageOutFile = null;

        try {
            imageOutFile = new FileOutputStream(fileOutputPath);
            imageOutFile.write(imageByteArray);
            imageOutFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (imageOutFile != null){
            try {
                imageOutFile.close();
            } catch (IOException e) {
                error(e);
            }
        }
    }

}


