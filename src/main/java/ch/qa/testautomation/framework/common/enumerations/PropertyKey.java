package ch.qa.testautomation.framework.common.enumerations;

/**
 * enum of property keys which contain in DefaultTestRunProperties.properties.
 * they will be used in {@link ch.qa.testautomation.framework.configuration.PropertyResolver}
 */
public enum PropertyKey {
    DEFAULT_TESTCASE_REPORT_DIR("default.testcase.report.location"),
    DEFAULT_TESTCASE_LOCATION("default.testcase.location"),
    DEFAULT_TESTDATA_LOCATION("default.testdata.location"),
    DEFAULT_TEST_AUTOMATION_PACKAGE("default.package.test.automation"),
    DEFAULT_SCREENSHOT_FORMAT("default.screenshot.format"),
    DEFAULT_DATE_FORMAT("default.data.format"),
    DEFAULT_LOGGER_NAME("default.logger.name"),
    WEBDRIVER_NAME("webdriver.name"),
    DEFAULT_WEBDRIVER_BIN_LOCATION("default.webdriver.bin.location"),
    DEFAULT_DRIVER_CONFIG_LOCATION("default.driver.config.location"),
    DEFAULT_PAGE_CONFIG_LOCATION("default.page.config.location"),
    DEFAULT_OCR_TESSDATA_LOCATION("default.ocr.tessdata.location"),
    DEFAULT_RUN_MODE_DEBUG("default.run.mode.debug"),
    DEFAULT_RUN_TFS_CONNECT("default.run.tfs.connect"),
    DEFAULT_RUN_TFS_SYNC("default.run.tfs.sync"),
    DEFAULT_RUN_JIRA_CONNECT("default.run.jira.connect"),
    DEFAULT_RUN_JIRA_SYNC("default.run.jira.sync"),
    DEFAULT_RESULTS_DB_STORE("default.results.db.store"),
    DEFAULT_GENERATE_VIDEO("default.generate.video"),
    DEFAULT_VIDEO_FPS("default.video.fps"),
    REMOTE_WEB_DRIVER_FOLDER("remote.web.driver.folder"),
    STOP_RUN_ON_ERROR("stop.run.on.error"),
    WEBDRIVER_CHROME_DRIVER("webdriver.chrome.driver"),
    WEBDRIVER_EDGE_DRIVER("webdriver.edge.driver"),
    WEBDRIVER_EDGE_FILENAME("webdriver.edge.filename"),
    WEBDRIVER_CHROME_FILENAME("webdriver.chrome.filename"),
    ALLURE_REPORT_SERVICE("allure.report.service"),
    ALLURE_RESULTS_DIRECTORY("allure.results.directory"),
    ALLURE_REPORT_DIRECTORY("allure.report.directory"),
    ALLURE_REPORT_KEEP_LATEST("allure.report.keep.latest"),
    ALLURE_REPORT_TYPE("allure.report.type"),
    ALLURE_REPORT_REBASE("allure.report.rebase"),
    ALLURE_REPORT_ALL_ENVIRONMENT_VARIABLES("allure.report.AllEnvironmentVariables"),
    CURRENT_TESTCASE_NAME("current.testcase.name"),
    ENCODED_PASSWORD("encoded.password"),
    TFS_RUNNER_CONFIG("tfs.runner.config"),
    REPORT_SERVICE_RUNNER_CONFIG("reportService.runner.config"),
    TFS_CONFIGURATION_ID("tfs.configuration.id"),
    TEXT_EDITOR("text.editor"),
    TEST_DRIVER_REMOTE_CONFIG("test.driver.remote.config"),
    USE_HEADLESS_CHROME("driver.browser.headless"),
    USE_FULLSCREEN("driver.fullscreen"), SCREEN_SIZE("driver.screen.size"),
    TEST_DRIVER_MOBILE_CONFIG("test.driver.mobile.config"),
    DEMO_MODE_ENABLED("test.mode.demo"), DEMO_MODE_SLEEP("test.mode.demo.sleep"), DEMO_MODE_COLOR("test.mode.demo.color"),
    RETRY_MODE_ENABLED("test.mode.retry"), RETRY_OVER_STEPS("retry.over.steps"), SELENIDE_CONFIGURATION_TIMEOUT("selenide.configuration.timeout"),
    DB_CONFIG("db.config"), QC_CONFIG("qc.config"), REST_CONFIG("rest.config"), JIRA_CONFIG("jira.config"),
    JIRA_EXEC_CONFIG("jira.execution.config"), DEFAULT_VIDEO_FORMAT("default.video.format"),
    DEFAULT_EXECUTION_MULTI_THREADING("default.execution.multi.threading"), DEFAULT_EXECUTION_THREADS("default.execution.threads"),
    EXECUTION_REMOTE_SELENIUM_HUB("execution.remote.selenium.hub"), TEST_ENVIRONMENT("test.environment"),
    RUNNER_MARKUP_OBJECTID_ENABLED("runner.markup.objectid.enabled"), DEFAULT_DOWNLOAD_LOCATION("default.download.directory"),
    RUNTIME_DB_USER("runtime.db.user"),
    RUNTIME_DB_HOST("runtime.db.host"),
    RUNTIME_DB_TYPE("runtime.db.type"),
    RUNTIME_DB_PORT("runtime.db.port"),
    RUNTIME_DB_PASSWORD("runtime.db.password"),
    RUNTIME_DB_SNAME("runtime.db.sname"),
    RUNTIME_REST_HOST("runtime.rest.host"),
    RUNTIME_REST_USER("runtime.rest.user"),
    RUNTIME_REST_PASSWORD("runtime.rest.password"),
    RUNTIME_REST_PAT("runtime.rest.pat");

    private final String key;

    PropertyKey(String value) {
        key = value;
    }

    public String key() {
        return key.trim();
    }

}
