package ch.qa.testautomation.tas.core.component;

import ch.qa.testautomation.tas.common.logging.SystemLogger;
import ch.qa.testautomation.tas.intefaces.DriverProvider;

/**
 * Driver provider for non-webdriver scenarios.
 */
public class NonDriverProvider implements DriverProvider {
    @Override
    public <T> T getDriver() {
        return null;
    }

    @Override
    public void close() {
        SystemLogger.info("NonDriver is closed");
    }

    @Override
    public void initialize() {
        SystemLogger.info("NonDriver init.");
    }
}
