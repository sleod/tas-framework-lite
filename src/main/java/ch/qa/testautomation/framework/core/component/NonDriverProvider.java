package ch.qa.testautomation.framework.core.component;

import ch.qa.testautomation.framework.common.logging.SystemLogger;
import ch.qa.testautomation.framework.intefaces.DriverProvider;

public class NonDriverProvider implements DriverProvider {
    @Override
    public <T> T getDriver() {
        return null;
    }

    @Override
    public void close() {
        SystemLogger.trace("NonDriver is closed");
    }

    @Override
    public void initialize() {
        SystemLogger.trace("NonDriver init.");
    }
}
