package ch.qa.testautomation.framework.common.abstraction;

import ch.qa.testautomation.framework.common.logging.SystemLogger;

public abstract class SingleTestObject {
    public void logStepInfo(String text) {
        SystemLogger.logStepInfo(Thread.currentThread().getName(), text);
    }

}
