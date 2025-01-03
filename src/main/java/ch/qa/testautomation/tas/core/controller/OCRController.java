package ch.qa.testautomation.tas.core.controller;

import ch.qa.testautomation.tas.common.IOUtils.FileLocator;
import ch.qa.testautomation.tas.common.logging.SystemLogger;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Word;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertNotNull;


public class OCRController {

    private static String LANGUAGE = "deu";
    private static int ITERATOR_LEVEL = ITessAPI.TessPageIteratorLevel.RIL_WORD;

    private static final ITesseract tesseract = new Tesseract();

    public static void setLanguage(String language) {
        OCRController.LANGUAGE = language;
    }

    public static void setIteratorLevel(int level) {
        OCRController.ITERATOR_LEVEL = level;
    }

    public static Rectangle findTextPosition(String text, BufferedImage image) {
        return findTextLinePosition(text, image);
    }

    public static void setPageSegMode(int mode) {
        tesseract.setPageSegMode(mode);
    }

    public static void setOcrEngineMode(int mode) {
        tesseract.setOcrEngineMode(mode);
    }

    private static Rectangle findTextLinePosition(String text, BufferedImage image) {
        tesseract.setDatapath(FileLocator.findResource("tessdata").toString());
        tesseract.setLanguage(LANGUAGE);
        Rectangle boundingBox = null;
        for (Word word : tesseract.getWords(image, ITERATOR_LEVEL)) {
            if (word.getText().contains(text)) {
                boundingBox = word.getBoundingBox();
                SystemLogger.info("Find Element with Text: " + text + " on " + boundingBox);
                break;
            }
        }
        assertNotNull(boundingBox, "Element with Text: " + text + " was not found on Image!");
        return boundingBox;
    }

    public static boolean checkTextLineWithText(String text, BufferedImage image) {
        tesseract.setDatapath(FileLocator.findResource("tessdata").toString());
        tesseract.setLanguage(LANGUAGE);
        for (Word word : tesseract.getWords(image, ITessAPI.TessPageIteratorLevel.RIL_TEXTLINE)) {
            if (word.getText().contains(text)) {
                return true;
            }
        }
        return false;
    }
}
