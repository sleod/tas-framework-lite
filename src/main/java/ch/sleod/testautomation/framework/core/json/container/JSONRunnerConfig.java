package ch.sleod.testautomation.framework.core.json.container;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import java.util.LinkedHashMap;
import java.util.Map;

public class JSONRunnerConfig {
    @JsonProperty
    private String runName;
    @JsonProperty
    private String planId;
    @JsonProperty
    private String suiteId;
    @JsonProperty
    private boolean fullRun;
    @JsonProperty
    private boolean failureRetest;
    @JsonProperty
    private boolean feedbackAfterSingleTest;
    @JsonProperty
    private String configurationId;
    @JsonProperty
    private Map<String, String> testCaseFiles = new LinkedHashMap<>();
    @JsonProperty
    private String[] selectedTestCaseIds;
    @JsonProperty
    private Map<String, String> tfsConfig = new LinkedHashMap<>();

    private Map<String, String> testPlanConfig = new LinkedHashMap<>();

    @JsonAnySetter
    public void setTestCaseFiles(String key, String value) {
        this.testCaseFiles.put(key, value);
    }

    public void setTfsConfig(String key, String value) {
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

    @JsonAnyGetter
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

    public void setFeedbackAfterSingleTest(boolean feedbackAfterSingleTest) {
        this.feedbackAfterSingleTest = feedbackAfterSingleTest;
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

    public boolean feedbackAfterSingleTest() {
        if (System.getProperty("tfs.feedbackAfterSingleTest") == null) {
            return feedbackAfterSingleTest;
        } else {
            return System.getProperty("tfs.feedbackAfterSingleTest").equalsIgnoreCase("true");
        }
    }
}
