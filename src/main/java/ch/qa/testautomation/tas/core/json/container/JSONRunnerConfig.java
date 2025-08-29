package ch.qa.testautomation.tas.core.json.container;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class JSONRunnerConfig {
    @Setter
    private String runName;
    @Setter
    private String planId;
    @Setter
    private String suiteId;
    @Setter
    @Getter
    private String testPlanId;
    private Map<String, String> testExecutionIdMap;
    @Setter
    @Getter
    private String projectKey;
    @Setter
    private boolean fullRun;
    @Setter
    private boolean failureRetest;
    @Setter
    @Getter
    private String configurationId;
    @Getter
    private Map<String, String> testCaseFiles;
    @Setter
    @Getter
    private String[] selectedTestCaseIds;
    @Getter
    private Map<String, String> tfsConfig;

    @Getter
    @Setter
    private Map<String, String> testPlanConfig;

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

    public void setTestCaseFiles(String key, String value) {
        testCaseFiles = new LinkedHashMap<>();
        this.testCaseFiles.put(key, value);
    }

    public void setTfsConfig(String key, String value) {
        tfsConfig = new LinkedHashMap<>();
        this.tfsConfig.put(key, value);
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

    public String getRunName() {
        if (System.getProperty("tfs.runName") == null) {
            return runName;
        } else {
            return System.getProperty("tfs.runName");
        }
    }

    public Multimap<String, String> getCoverageMap() {
        return Multimaps.invertFrom(Multimaps.forMap(testCaseFiles), HashMultimap.create());
    }
}
