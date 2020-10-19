package ch.raiffeisen.testautomation.framework.common.abstraction;

import ch.raiffeisen.testautomation.framework.common.logging.SystemLogger;

public abstract class SingleTestObject {
    public void logStepInfo(String text) {
        SystemLogger.logStepInfo(Thread.currentThread().getName(), text);
    }

}
