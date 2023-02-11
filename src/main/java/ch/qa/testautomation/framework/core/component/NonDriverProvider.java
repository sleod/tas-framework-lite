package ch.qa.testautomation.framework.core.component;

import ch.qa.testautomation.framework.intefaces.DriverProvider;

import static ch.qa.testautomation.framework.common.logging.SystemLogger.info;

public class NonDriverProvider implements DriverProvider {
    @Override
    public <T> T getDriver() {
        return null;
    }

    @Override
    public void close() {
        info("NonDriver is closed");
    }

    @Override
    public void initialize() {
        info("NonDriver init.");
    }
}
