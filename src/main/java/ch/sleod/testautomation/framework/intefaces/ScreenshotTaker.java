package ch.sleod.testautomation.framework.intefaces;

import ch.sleod.testautomation.framework.common.logging.Screenshot;
import ch.sleod.testautomation.framework.core.component.TestStepMonitor;

public interface ScreenshotTaker {
    Screenshot takeScreenShot(TestStepMonitor testStepMonitor);
}
