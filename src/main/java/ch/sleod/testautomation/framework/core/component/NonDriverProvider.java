package ch.sleod.testautomation.framework.core.component;

import ch.sleod.testautomation.framework.common.logging.SystemLogger;
import ch.sleod.testautomation.framework.intefaces.DriverProvider;

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
