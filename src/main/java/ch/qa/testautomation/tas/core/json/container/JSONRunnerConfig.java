package ch.qa.testautomation.tas.core.json.container;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import java.util.*;

public class JSONRunnerConfig {
    private String runName;
    private String planId;
    private String suiteId;
    private String testPlanId;
    private Map<String, String> testExecutionIdMap;
    private String projectKey;
    private boolean fullRun;
    private boolean failureRetest;
    private String configurationId;
    private Map<String, String> testCaseFiles;
    private String[] selectedTestCaseIds;
    private Map<String, String> tfsConfig;

    private Map<String, String> testPlanConfig;

    public String getTestPlanId() {
        return testPlanId;
    }

    public void setTestPlanId(String testPlanId) {
        this.testPlanId = testPlanId;
    }

    public Map<String, String> getTestExecutionIdMap() {
        return Objects.isNull(testExecutionIdMap) ? Collections.emptyMap() : testExecutionIdMap;
    }

    @JsonAnySetter
    public void setTestCaseIdMap(String key, String value) {
        if (Objects.isNull(testExecutionIdMap)) {
            testExecutionIdMap = new HashMap<>(2);
        }
        testExecutionIdMap.put(key, value);
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public void setTestCaseFiles(String key, String value) {
        testCaseFiles = new LinkedHashMap<>();
        this.testCaseFiles.put(key, value);
    }

    public void setTfsConfig(String key, String value) {
        tfsConfig = new LinkedHashMap<>();
        this.tfsConfig.put(key, value);
    }

    public void setTestPlanConfig(Map<String, String> config) {
        this.testPlanConfig = config;
    }

    public String getPlanId() {
        if (System.getProperty("tfs.planId") == null) {
            return planId;
        } else {
            return System.getProperty("tfs.planId");
        }
    }

    public String getSuiteId() {
        if (System.getProperty("tfs.suiteId") == null) {
            return suiteId;
        } else {
            return System.getProperty("tfs.suiteId");
        }
    }

    public boolean isFullRun() {
        if (System.getProperty("tfs.fullRun") == null) {
            return fullRun;
        } else {
            return System.getProperty("tfs.fullRun").equalsIgnoreCase("true");
        }
    }

    public boolean isFailureRetest() {
        if (System.getProperty("tfs.failureRetest") == null) {
            return failureRetest;
        } else {
            return System.getProperty("tfs.failureRetest").equalsIgnoreCase("true");
        }
    }

    public Map<String, String> getTestCaseFiles() {
        return testCaseFiles;
    }

    public String[] getSelectedTestCaseIds() {
        return selectedTestCaseIds;
    }

    public Map<String, String> getTfsConfig() {
        return tfsConfig;
    }

    public Map<String, String> getTestPlanConfig() {
        return testPlanConfig;
    }

    public String getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(String configurationId) {
        this.configurationId = configurationId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public void setSuiteId(String suiteId) {
        this.suiteId = suiteId;
    }

    public void setFullRun(boolean fullRun) {
        this.fullRun = fullRun;
    }

    public void setFailureRetest(boolean failureRetest) {
        this.failureRetest = failureRetest;
    }

    public void setSelectedTestCaseIds(String[] selectedTestCaseIds) {
        this.selectedTestCaseIds = selectedTestCaseIds;
    }

    public String getRunName() {
        if (System.getProperty("tfs.runName") == null) {
            return runName;
        } else {
            return System.getProperty("tfs.runName");
        }
    }

    public void setRunName(String runName) {
        this.runName = runName;
    }

    public Multimap<String, String> getCoverageMap() {
        return Multimaps.invertFrom(Multimaps.forMap(testCaseFiles), HashMultimap.create());
    }
}
