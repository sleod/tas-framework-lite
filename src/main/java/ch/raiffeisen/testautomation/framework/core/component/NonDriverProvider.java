package ch.raiffeisen.testautomation.framework.core.component;

import ch.raiffeisen.testautomation.framework.common.logging.SystemLogger;
import ch.raiffeisen.testautomation.framework.intefaces.DriverProvider;

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
