package ch.sleod.testautomation.framework.common.abstraction;

import ch.sleod.testautomation.framework.common.logging.SystemLogger;

public abstract class SingleTestObject {
    public void logStepInfo(String text) {
        SystemLogger.logStepInfo(Thread.currentThread().getName(), text);
    }

}
