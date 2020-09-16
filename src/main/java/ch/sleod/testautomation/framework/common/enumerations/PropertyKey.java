package ch.sleod.testautomation.framework.common.enumerations;

import ch.sleod.testautomation.framework.configuration.PropertyResolver;

/**
 * enum of property keys which contain in DefaultTestRunProperties.properties.
 * they will be used in {@link PropertyResolver}
 */
public enum PropertyKey {
    DEFAULT_TESTCASE_REPORT_DIR("default.testcase.report.location"),
    DEFAULT_TESTCASE_LOCATION("default.testcase.location"),
    DEFAULT_TESTDATA_LOCATION("default.testdata.location"),
    DEFAULT_TEST_AUTOMATION_PACKAGE("default.package.test.automation"),
    DEFAULT_SCREENSHOT_FORMAT("default.screenshot.format"),
    DEFAULT_DATE_FORMAT("default.data.format"),
    DEFAULT_LOGGER_NAME("default.logger.name"),
    DEFAULT_WEBDRIVER_NAME("default.webdriver.name"),
    DEFAULT_WEBDRIVER_BIN_LOCATION("default.webdriver.bin.location"),
    DEFAULT_DRIVER_CONFIG_LOCATION("default.driver.config.location"),
    DEFAULT_PAGE_CONFIG_LOCATION("default.page.config.location"),
    DEFAULT_OCR_TESSDATA_LOCATION("default.ocr.tessdata.location"),
    DEFAULT_RUN_MODE_DEBUG("default.run.mode.debug"),
    DEFAULT_RUN_TFS_FEEDBACK("default.run.tfs.feedback"),
    DEFAULT_RESULTS_DB_STORE("default.results.db.store"),
    DEFAULT_GENERATE_VIDEO("default.generate.video"),
    DEFAULT_VIDEO_FPS("default.video.fps"),
    REMOTE_WEB_DRIVER_FOLDER("remote.web.driver.folder"),
    STOP_RUN_ON_ERROR("stop.run.on.error"),
    WEBDRIVER_CHROME_DRIVER("webdriver.chrome.driver"),
    WEBDRIVER_IE_DRIVER("webdriver.ie.driver"),
    WEBDRIVER_IE_FILENAME("webdriver.ie.filename"),
    WEBDRIVER_CHROME_FILENAME("webdriver.chrome.filename"),
    ALLURE_RESULTS_DIRECTORY("allure.results.directory"),
    ALLURE_REPORT_DIRECTORY("allure.report.directory"),
    ALLURE_REPORT_KEEP_LATEST("allure.report.keep.latest"),
    ALLURE_REPORT_TYPE("allure.report.type"),
    ALLURE_REPORT_REBASE("allure.report.rebase"),
    CURRENT_TESTCASE_NAME("current.testcase.name"),
    ENCODED_PASSWORD("encoded.password"),
    TFS_RUNNER_CONFIG("tfs.runner.config"),
    TEXT_EDITOR("text.editor"),
    //    TEST_DRIVER_WEB_CONFIG("test.driver.web.config"),
    DRIVER_NAME("driver.name"),
    TEST_DRIVER_REMOTE_CONFIG("test.driver.remote.config"),
    USE_HEADLESS_CHROME("driver.chrome.headless"),
    USE_FULLSCREEN("driver.fullscreen"),
    DRIVER_IE_KEEP_CACHE("driver.ie.keep.cache"),
    TEST_DRIVER_MOBILE_CONFIG("test.driver.mobile.config"),
    DEMO_MODE_ENABLED("test.mode.demo"), DEMO_MODE_SLEEP("test.mode.demo.sleep"), DEMO_MODE_COLOR("test.mode.demo.color"),
    RETRY_MODE_ENABLED("test.mode.retry"), RETRY_OVER_STEPS("retry.over.steps"),
    TEST_DB_CONFIG("test.db.config"), TEST_QC_CONFIG("test.qc.config"), TEST_REST_CONFIG("test.rest.config"), TEST_JIRA_CONFIG("test.jira.config"),
    JIRA_CUSTOMFIELD_CONFIG("jira.customfield.config"), DEFAULT_VIDEO_FORMAT("default.video.format"),
    DEFAULT_EXECUTION_MULTI_THREADING("default.execution.multi.threading"), DEFAULT_EXECUTION_THREADS("default.execution.threads"),
    EXECUTION_REMOTE_SELENIUM_HUB("execution.remote.selenium.hub"),
    RUNNER_MARKUP_OBJECTID_ENABLED("runner.markup.objectid.enabled");

    private String key;

    PropertyKey(String value) {
        key = value;
    }

    public String key() {
        return key.trim();
    }

}
