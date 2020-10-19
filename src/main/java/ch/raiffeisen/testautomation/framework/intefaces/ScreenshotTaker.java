package ch.raiffeisen.testautomation.framework.intefaces;

import ch.raiffeisen.testautomation.framework.common.logging.Screenshot;
import ch.raiffeisen.testautomation.framework.core.component.TestStepMonitor;

public interface ScreenshotTaker {
    Screenshot takeScreenShot(TestStepMonitor testStepMonitor);
}
