package ch.raiffeisen.testautomation.google;

import ch.raiffeisen.testautomation.framework.common.IOUtils.FileLocator;
import ch.raiffeisen.testautomation.framework.common.logging.SystemLogger;
import ch.raiffeisen.testautomation.framework.common.utils.ZipUtils;
import ch.raiffeisen.testautomation.framework.configuration.PropertyResolver;
import ch.raiffeisen.testautomation.framework.core.json.container.JSONRunnerConfig;
import ch.raiffeisen.testautomation.framework.core.json.deserialization.JSONContainerFactory;
import ch.raiffeisen.testautomation.framework.intefaces.DBDataCollector;
import ch.raiffeisen.testautomation.framework.rest.TFS.connection.TFSConnector;
import ch.raiffeisen.testautomation.framework.rest.TFS.connection.TFSRestClient;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class MyDBDataCollector implements DBDataCollector {

    @Override
    public List<Map<String, Object>> getData() {
        return null;
    }

}
