package ch.qa.testautomation.framework.intefaces;

import ch.qa.testautomation.framework.common.logging.Screenshot;
import ch.qa.testautomation.framework.core.component.TestStepMonitor;

public interface ScreenshotTaker {
    Screenshot takeScreenShot(TestStepMonitor testStepMonitor);
}
