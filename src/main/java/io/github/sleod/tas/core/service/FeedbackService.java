package io.github.sleod.tas.core.service;

import io.github.sleod.tas.common.enumerations.TestType;
import io.github.sleod.tas.configuration.PropertyResolver;
import io.github.sleod.tas.core.component.DriverManager;
import io.github.sleod.tas.core.component.TestCaseObject;
import io.github.sleod.tas.core.component.TestRunResult;
import io.github.sleod.tas.core.json.container.JSONRunnerConfig;
import io.github.sleod.tas.core.json.deserialization.JSONContainerFactory;
import io.github.sleod.tas.exception.ExceptionBase;
import io.github.sleod.tas.exception.ExceptionErrorKeys;
import io.github.sleod.tas.rest.connection.hpqc.QCRestClient;
import io.github.sleod.tas.rest.connection.jira.JIRARestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.sleod.tas.common.logging.SystemLogger.info;
import static io.github.sleod.tas.common.logging.SystemLogger.trace;

/**
 * Service to feedback test results to external systems like JIRA or HP QC/ALM.
 *
 * @author Patrick Schmid
 */
public class FeedbackService {

    public void jiraFeedback(List<TestCaseObject> testCaseObjects) {
        info("Feedback Test Result back to JIRA...");
        //get data from test case object
        Map<String, TestRunResult> testRunResultMap = new HashMap<>(testCaseObjects.size());
        for (TestCaseObject testCaseObject : testCaseObjects) {
            String testCaseId = testCaseObject.getTestCaseId();
            Map<String, String> testCaseIdMap = testCaseObject.getTestCase().getTestCaseIdMap();
            if (testCaseId.isEmpty() && !testCaseObject.getTestType().equals(TestType.MOBILE_APP)
                || testCaseObject.getTestType().equals(TestType.MOBILE_APP) && testCaseIdMap.isEmpty()) {
                throw new ExceptionBase(ExceptionErrorKeys.TEST_CASE_ID_IS_REQUIRED, testCaseObject.getName());
            }
            if (!testCaseObject.getTestType().equals(TestType.MOBILE_APP)) {
                testRunResultMap.put(testCaseId, testCaseObject.getTestRunResult());
            } else {
                testRunResultMap.put(testCaseIdMap.get(DriverManager.getCurrentPlatform()), testCaseObject.getTestRunResult());
            }
        }
        JSONRunnerConfig jiraExecutionConfig = JSONContainerFactory.getRunnerConfig(PropertyResolver.getJiraExecutionConfigFile());
        JIRARestClient restClient = new JIRARestClient(PropertyResolver.getJiraHost(), PropertyResolver.getJiraPAT());
        restClient.updateRunStatusInExecution(jiraExecutionConfig, testRunResultMap);
        restClient.close();
    }

    /**
     * Feedback test results to HP QC/ALM.
     *
     * @param testCaseObjects List of TestCaseObject containing test results to be fed back.
     */
    public void qcFeedback(List<TestCaseObject> testCaseObjects) {
        trace("Feedback Test Result back to QC...");
        new QCRestClient(PropertyResolver.getQCConfigFile()).syncTestCasesAndRunResults(testCaseObjects);
    }


}
