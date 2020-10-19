package ch.raiffeisen.testautomation.framework.core.controller;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class UserRobot {

    /**
     * take full main screen shot
     *
     * @return buffered image of screen shot
     * @throws AWTException awt exception
     */
    public static BufferedImage captureMainFullScreen() throws AWTException {
        Rectangle rectangle = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        Robot robot = new Robot();
        return robot.createScreenCapture(rectangle);
    }

    /**
     * move mouse to element position
     *
     * @param elementPosition position of element in type Rectangle
     * @throws AWTException awt exception
     */
    public static void moveMouseTo(Rectangle elementPosition) throws AWTException {
        Robot robot = new Robot();
        robot.mouseMove(elementPosition.x + 5, elementPosition.y + elementPosition.height / 3);
    }

    /**
     * move mouse to element position
     *
     * @param x x coords
     * @param y y coords
     * @throws AWTException awt exception
     */
    public static void moveMouseTo(int x, int y) throws AWTException {
        Robot robot = new Robot();
        robot.mouseMove(x, y);
    }

    /**
     * Perform left click on element position
     *
     * @param elementPosition position of element in type Rectangle
     * @param doubleClick     click twice if true
     * @throws AWTException awt exception
     */
    public static void leftClickOn(Rectangle elementPosition, boolean doubleClick) throws AWTException {
        Robot robot = new Robot();
        robot.mouseMove(elementPosition.x + 5, elementPosition.y + elementPosition.height / 3);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        if (doubleClick) {
            robot.mousePress(InputEvent.BUTTON1_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_MASK);
        }
    }

    /**
     * Send text to current element
     *
     * @param text text to send
     * @throws AWTException awt exception
     */
    public static void sendText(String text) throws AWTException {
        StringSelection stringSelection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, stringSelection);
        Robot robot = new Robot();
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);
    }

    /**
     * Perform right click on element position
     *
     * @param elementPosition position of element in type Rectangle
     * @param doubleClick     click twice if true
     * @throws AWTException awt exception
     */
    public static void rightClickOn(Rectangle elementPosition, boolean doubleClick) throws AWTException {
        Robot robot = new Robot();
        robot.mouseMove(elementPosition.x + 5, elementPosition.y + elementPosition.height / 3);
        robot.mousePress(InputEvent.BUTTON2_MASK);
        robot.mouseRelease(InputEvent.BUTTON2_MASK);
        if (doubleClick) {
            robot.mousePress(InputEvent.BUTTON2_MASK);
            robot.mouseRelease(InputEvent.BUTTON2_MASK);
        }
    }

    /**
     * Press single key
     *
     * @param keyEvent e.g. KeyEvent.VK_F1 {@link KeyEvent}
     * @throws AWTException awt exception
     */
    public static void pressKey(int keyEvent) throws AWTException {
        Robot robot = new Robot();
        robot.keyPress(keyEvent);
        robot.keyRelease(keyEvent);
    }

    /**
     * Press two keys as combination
     *
     * @param keyEvent1 key 1 {@link KeyEvent}
     * @param keyEvent2 key 2 {@link KeyEvent}
     * @throws AWTException awt exception
     */
    public static void pressKeys(int keyEvent1, int keyEvent2) throws AWTException {
        Robot robot = new Robot();
        robot.keyPress(keyEvent1);
        robot.keyPress(keyEvent2);
        robot.keyRelease(keyEvent1);
        robot.keyRelease(keyEvent2);
    }

    /**
     * Press three keys as combination
     *
     * @param keyEvent1 key1 {@link KeyEvent}
     * @param keyEvent2 key2 {@link KeyEvent}
     * @param keyEvent3 key3 {@link KeyEvent}
     * @throws AWTException awt exception
     */
    public static void pressKeys(int keyEvent1, int keyEvent2, int keyEvent3) throws AWTException {
        Robot robot = new Robot();
        robot.keyPress(keyEvent1);
        robot.keyPress(keyEvent2);
        robot.keyPress(keyEvent3);
        robot.keyRelease(keyEvent1);
        robot.keyRelease(keyEvent2);
        robot.keyRelease(keyEvent3);
    }


}
