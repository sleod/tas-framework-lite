package ch.qa.testautomation.tas.core.service;

import ch.qa.testautomation.tas.common.enumerations.TestType;
import ch.qa.testautomation.tas.configuration.PropertyResolver;
import ch.qa.testautomation.tas.core.component.DriverManager;
import ch.qa.testautomation.tas.core.component.TestCaseObject;
import ch.qa.testautomation.tas.core.component.TestRunResult;
import ch.qa.testautomation.tas.core.json.container.JSONRunnerConfig;
import ch.qa.testautomation.tas.core.json.deserialization.JSONContainerFactory;
import ch.qa.testautomation.tas.exception.ExceptionBase;
import ch.qa.testautomation.tas.exception.ExceptionErrorKeys;
import ch.qa.testautomation.tas.rest.connection.hpqc.QCRestClient;
import ch.qa.testautomation.tas.rest.connection.jira.JIRARestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ch.qa.testautomation.tas.common.logging.SystemLogger.info;
import static ch.qa.testautomation.tas.common.logging.SystemLogger.trace;

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

    public void qcFeedback(List<TestCaseObject> testCaseObjects) {
        trace("Feedback Test Result back to QC...");
        new QCRestClient(PropertyResolver.getQCConfigFile()).syncTestCasesAndRunResults(testCaseObjects);
    }


}
