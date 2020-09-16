package ch.sleod.testautomation.google;


import ch.sleod.testautomation.framework.common.abstraction.SingleTestObject;
import ch.sleod.testautomation.framework.core.annotations.TestObject;
import ch.sleod.testautomation.framework.core.annotations.TestStep;
import ch.sleod.testautomation.framework.core.controller.ExternAppController;
import net.sourceforge.tess4j.ITessAPI;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@TestObject(name = "Test OCR")
public class TestOCR extends SingleTestObject {

    @TestStep(name = "Do something", takeScreenshot = true)
    public void testExtAppController() throws AWTException, IOException {
        //start notepad
        ExternAppController.runCommandAndWait("cmd /c start /max notepad", 1);
        //settings
        ExternAppController.setLanguage("deu");
        ExternAppController.setIteratorLevel(ITessAPI.TessPageIteratorLevel.RIL_WORD);
        ExternAppController.setPageSegMode(1);
        ExternAppController.setOcrEngineMode(1);
        //start action
        Rectangle editElement = ExternAppController.findElementAndLeftClick("Edit", false, 1);
        ExternAppController.findElementAndLeftClick("Paste", false, 1);
        ExternAppController.leftClickOnElement(editElement, false, 1);
        ExternAppController.findElementAndLeftClick("Find", false, 1);
        ExternAppController.sendText("Try something");
        ExternAppController.pressKeys(KeyEvent.VK_ESCAPE);
        ExternAppController.pressKeys(KeyEvent.VK_ALT, KeyEvent.VK_E, KeyEvent.VK_R);
        ExternAppController.pressKeys(KeyEvent.VK_ESCAPE);
        ExternAppController.findElementAndLeftClick("File", false, 1);
        ExternAppController.findElementAndLeftClick("Print", false, 3);
        ExternAppController.pressKeys(KeyEvent.VK_ESCAPE);
        ExternAppController.findElementAndLeftClick("File", false, 2);
        //exit
        Rectangle exit = ExternAppController.findSubImageOnScreen(ImageIO.read(new File("D:\\exit.png")), 36, 24,
                5.0, 5);
        ExternAppController.leftClickOnElement(exit, false, 1);
        //dont save
        Rectangle dontSave = ExternAppController.findSubImageOnScreen(ImageIO.read(new File("D:\\dontSave.png")), 57, 11,
                5.0, 5);
        ExternAppController.leftClickOnElement(dontSave, false, 1);
    }

    @Test
    public void testPS() throws IOException, AWTException {
//        Runtime.getRuntime().exec("cmd /c start powershell.exe \"C:\\Users\\uex15227\\Downloads\\DEV\\appActive.ps1\" ");
//        System.out.println("CHF 125'125'111.86".replaceAll("[^0-9.]", ""));
    }

}
