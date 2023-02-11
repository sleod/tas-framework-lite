package ch.qa.testautomation.framework.common.enumerations;

import ch.qa.testautomation.framework.configuration.PropertyResolver;

/**
 * enum of property keys which contain in DefaultTestRunProfil.properties.
 * they will be used in {@link PropertyResolver}
 */
public enum PropertyKey {
    RUN_META_FILTER("run.meta.filter"),
    PROFIL_NAME("profil.name"),
    TESTCASE_REPORT_DIR("testcase.report.location"),
    TESTCASE_LOCATION("testcase.location"),
    TESTCASE_FILE_EXTENSION("testcase.file.extension"),
    TESTDATA_LOCATION("testdata.location"),
    TEST_AUTOMATION_PACKAGE("package.test.automation"),
    SCREENSHOT_FORMAT("screenshot.format"),
    DATE_FORMAT("data.format"),
    LOGGER_NAME("logger.name"),
    DRIVER_BROWSER_NAME("driver.browser.name"),
    WEBDRIVER_BIN_LOCATION("webdriver.bin.location"),
    DRIVER_CONFIG_LOCATION("driver.config.location"),
    PAGE_CONFIG_LOCATION("page.config.location"),
    OCR_TESSDATA_LOCATION("ocr.tessdata.location"),
    DEBUG_TRACE_OUTPUT("debug.trace.output"),
    RUN_TFS_CONNECT("run.tfs.connect"),
    RUN_TFS_SYNC("run.tfs.sync"),
    RUN_JIRA_CONNECT("run.jira.connect"),
    RUN_JIRA_SYNC("run.jira.sync"),
    RUN_QC_SYNC("run.hpqc.sync"),
    GENERATE_VIDEO("generate.video"),
    RESOURCE_PROJECT("resource.project"),
    RESOURCE_DRIVER_LOCATION("resource.driver.location"),
    RUN_STOP_ON_ERROR("run.stop.on.error"),
    WEBDRIVER_CHROME_DRIVER("webdriver.chrome.driver"),
    WEBDRIVER_EDGE_DRIVER("webdriver.edge.driver"),
    WEBDRIVER_EDGE_FILENAME("webdriver.edge.filename"),
    WEBDRIVER_CHROME_FILENAME("webdriver.chrome.filename"),
    ALLURE_REPORT_SERVICE("allure.report.service"), ALLURE_REPORT_CLEANUP("allure.report.cleanup"),
    ALLURE_RESULTS_LOCATION("allure.results.location"),
    ALLURE_REPORT_LOCATION("allure.report.location"),
    ALLURE_REPORT_REBASE("allure.report.rebase"),
    ALLURE_REPORT_ALL_ENVIRONMENT_VARIABLES("allure.report.AllEnvironmentVariables"),
    CURRENT_TESTCASE_NAME("current.testcase.name"),
    TFS_RUNNER_CONFIG("tfs.runner.config"),
    REPORT_SERVICE_RUNNER_CONFIG("reportService.runner.config"),
    TFS_CONFIGURATION_ID("tfs.configuration.id"),
    TEXT_EDITOR("text.editor"),
    TEST_DRIVER_REMOTE_CONFIG("test.driver.remote.config"),
    DRIVER_BROWSER_HEADLESS("driver.browser.headless"),
    DRIVER_BROWSER_FULLSCREEN("driver.browser.fullscreen"), BROWSER_SCREEN_SIZE("driver.browser.screen.size"),
    TEST_DRIVER_MOBILE_CONFIG("test.driver.mobile.config"),
    DEMO_MODE_ENABLED("run.mode.demo"),
    RETRY_MODE_ENABLED("run.mode.retry"), RETRY_OVER_STEPS("run.retry.over.steps"), DRIVER_WAIT_TIMEOUT("driver.wait.timeout"),
    DB_CONFIG("db.config"), QC_CONFIG("qc.config"), REST_CONFIG("rest.config"), JIRA_CONFIG("jira.config"),
    JIRA_EXEC_CONFIG("jira.execution.config"), VIDEO_FORMAT("video.format"),
    RUN_DRIVER_RESTART("run.driver.restart"),
    TEST_ENVIRONMENT("test.environment"),
    DEBUG_KEEP_BROWSER("debug.keep.browser"),
    METHOD_NONHEADLESS_EXISTS("method.nonheadless.exists"),
    OPEN_PDF_IN_SYSTEM_READER("open.pdf.in.system.reader"),
    SIMPLE_STRING_PARAMETER_ALLOWED("simple.string.parameter.allowed"),
    DOWNLOAD_LOCATION("download.location"),
    EXECUTION_REMOOTE_PARALLEL("execution.remote.parallel"),
    EXECUTION_REMOTE_THREAD_MAX("execution.remote.thread.max"),
    DB_USER("db.user"), DB_HOST("db.host"), DB_TYPE("db.type"), DB_PORT("db.port"),
    DB_PASSWORD("db.password"), DB_NAME("db.name"),
    REST_HOST("rest.host"), REST_USER("rest.user"), REST_PASSWORD("rest.password"),
    REST_PAT("rest.pat"), JIRA_HOST("jira.host"),
    JIRA_USER("jira.user"), JIRA_PASSWORD("jira.password"), JIRA_PAT("jira.pat");

    private final String key;

    PropertyKey(String value) {
        key = value;
    }

    public String key() {
        return key.trim();
    }

}
