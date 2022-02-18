package ch.qa.testautomation.framework.rest.TFS.connection;

import ch.qa.testautomation.framework.common.enumerations.TestStatus;
import ch.qa.testautomation.framework.common.logging.SystemLogger;
import ch.qa.testautomation.framework.common.utils.SafeCase;
import ch.qa.testautomation.framework.common.utils.TimeUtils;
import ch.qa.testautomation.framework.core.component.TestRunResult;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Assert;

import jakarta.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static java.util.Arrays.asList;

public class TFSRestClient {

    //Rest Driver
    private final TFSConnector tfsConnector;
    //Attributes of TFS
    private final String apiVersion, organization, collection, project;

    /**
     * Constructor with full setting of TFS
     *
     * @param host         host
     * @param pat          personal access token
     * @param organization organization like tfs
     * @param collection   collection like RCH
     * @param project      project like ap.testtools
     * @param apiVersion   version like 5.0
     */
    public TFSRestClient(String host, String pat, String organization, String collection, String project, String apiVersion) {
        this.tfsConnector = new TFSConnector(host, pat, apiVersion);
        this.apiVersion = apiVersion;
        this.organization = organization;
        this.collection = collection;
        this.project = project;
        safeCase(asList(this.tfsConnector, this.apiVersion, this.collection, this.project, this.organization));
    }

    /**
     * Constructor with map of tfs config
     *
     * @param tfsConnector connector
     * @param tfsConfig    map of config
     */
    public TFSRestClient(TFSConnector tfsConnector, Map<String, String> tfsConfig) {
        this.tfsConnector = tfsConnector;
        this.apiVersion = tfsConfig.get("apiVersion");
        this.organization = tfsConfig.get("organization");
        this.collection = tfsConfig.get("collection");
        this.project = tfsConfig.get("project");
        safeCase(asList(this.tfsConnector, this.apiVersion, this.collection, this.project, this.organization));
    }

    /**
     * Constructor with map of tfs config
     *
     * @param tfsConfig map of config
     */
    public TFSRestClient(Map<String, String> tfsConfig) {
        this.tfsConnector = new TFSConnector(tfsConfig);
        this.apiVersion = tfsConfig.get("apiVersion");
        this.organization = tfsConfig.get("organization");
        this.collection = tfsConfig.get("collection");
        this.project = tfsConfig.get("project");
        safeCase(asList(this.tfsConnector, this.apiVersion, this.collection, this.project, this.organization));
    }

    /**
     * create test run with given test case id in test plan and test suite
     *
     * @param runName     test run name manuel
     * @param testPlanId  test plan id
     * @param testSuiteId test suite id
     * @param testCaseIds test case ids in list
     * @return response of creation
     */
    public JSONObject createTestRun(String runName, String testPlanId, String testSuiteId, List<String> testCaseIds) {
        String path = organization + "/" + collection + "/" + project + "/_apis/test/runs";
        Response response = tfsConnector.post(path, buildTestRunPayLoad(runName, testPlanId, testSuiteId, testCaseIds));
        if (response.getStatus() == 200) {
            return JSONObject.fromObject(response.readEntity(String.class));
        } else {
            throw new RuntimeException("Failed on Create Test Run! Response Code: " + response.getStatus());
        }
    }

    /**
     * upload attachment to the test result
     *
     * @param runId     test run id
     * @param resultsId test case result id
     * @param fileName  name of file be shown after upload
     * @param comment   comment of attachment
     * @param stream    base64 encoded string of file
     * @return response content json object
     */
    public JSONObject attachmentToTestResult(String runId, String resultsId, String fileName, String comment, String stream) {
        String api_Version = apiVersion;
        String path = organization + "/" + collection + "/" + project + "/_apis/test/Runs/" + runId + "/Results/" + resultsId + "/attachments";
        JSONObject payload = new JSONObject().element("stream", stream)
                .element("fileName", fileName)
                .element("attachmentType", "GeneralAttachment")
                .element("comment", comment);
        if (!api_Version.contains("preview")) {
            api_Version += "-preview.1";
        }
        Response response = tfsConnector.post(path, payload.toString(), api_Version);
        if (response.getStatus() == 200) {
            return JSONObject.fromObject(response.readEntity(String.class));
        } else {
            throw new RuntimeException("Failed on attach file to result! Response Code: " + response.getStatus());
        }
    }

    /**
     * update test run result
     *
     * @param runId                run id
     * @param testCaseIdAndResults map of test case tfs id and junit test run result
     * @return response of update
     */
    public JSONObject updateTestRunResults(String runId, Map<String, TestRunResult> testCaseIdAndResults) {
        String path = organization + "/" + collection + "/" + project + "/_apis/test/runs/" + runId + "/Results";
        Response response = tfsConnector.patch(path, buildTestRunResultPayload(testCaseIdAndResults, getTestRunResults(runId)));
        if (response.getStatus() == 200) {
            return JSONObject.fromObject(response.readEntity(String.class));
        } else {
            throw new RuntimeException("Failed on Update Test Run Results! Response Code: " + response.getStatus());
        }
    }

    /**
     * update run to complete state with date
     *
     * @param runId         run id
     * @param state         The state of the test run Below are the valid values - NotStarted, InProgress, Completed, Aborted, Waiting
     * @param completedDate date of completion
     * @return object after update in json
     */
    public JSONObject setTestRunState(String runId, String state, String completedDate) {
        JSONObject payload = new JSONObject()
                .element("state", state)
                .element("completedDate", completedDate);
        return updateTestRun(runId, payload.toString());
    }

    /**
     * update test run with payload
     *
     * @param runId   run id
     * @param payload payload in json
     * @return object after update in json
     */
    public JSONObject updateTestRun(String runId, String payload) {
        String path = organization + "/" + collection + "/" + project + "/_apis/test/runs/" + runId;
        Response response = tfsConnector.patch(path, payload);
        if (response.getStatus() == 200) {
            return JSONObject.fromObject(response.readEntity(String.class));
        } else {
            throw new RuntimeException("Failed on Update Test Run! Response Code: " + response.getStatus());
        }
    }

    /**
     * get json object of test run
     *
     * @param runId test run id
     * @return object in json
     */
    public JSONObject getTestRun(String runId) {
        String path = organization + "/" + collection + "/" + project + "/_apis/test/runs/" + runId;
        Response response = tfsConnector.get(path);
        if (response.getStatus() == 200) {
            return JSONObject.fromObject(response.readEntity(String.class));
        } else {
            return null;
        }
    }

    /**
     * get json object of test run results
     *
     * @param runId test run id
     * @return object of results in json
     */
    public JSONObject getTestRunResults(String runId) {
        String path = organization + "/" + collection + "/" + project + "/_apis/test/runs/" + runId + "/results";
        Response response = tfsConnector.get(path);
        if (response.getStatus() == 200) {
            return JSONObject.fromObject(response.readEntity(String.class));
        } else {
            return null;
        }
    }

    /**
     * Get test points of test case for test run creation
     *
     * @param testPlanId  test plan id
     * @param testSuiteId test suite id (test Sammlung)
     * @param testCaseIds list of test case id
     * @return int array of test points like "[1234, 3234]"
     */
    public int[] getTestPoints(String testPlanId, String testSuiteId, List<String> testCaseIds) {
        int[] testPoints = new int[testCaseIds.size()];
        for (int i = 0; i < testCaseIds.size(); i++) {
            String testCaseId = testCaseIds.get(i);
            //clean up duplicate ids with suffix "<num>"
            if (testCaseId.endsWith(">")) {
                testCaseId = testCaseId.substring(0, testCaseId.indexOf("<"));
            }
            JSONObject jsonObject = getTestPoint(testPlanId, testSuiteId, testCaseId);
            //get value array of test points and get the first object of test point in array and get the id of test point
            testPoints[i] = jsonObject.getInt("id");
        }
        return testPoints;
    }

    /**
     * Get test points of test case for test run creation
     *
     * @param testPlanId  test plan id
     * @param testSuiteId test suite id (test Sammlung)
     * @param testCaseId  test case id
     * @return JSON Object of Test Point
     */
    public JSONObject getTestPoint(String testPlanId, String testSuiteId, String testCaseId) {
        String path = organization + "/" + collection + "/" + project + "/_apis/testplan/Plans/" +
                testPlanId + "/Suites/" + testSuiteId + "/TestPoint";
        Response response = tfsConnector.get(path, "testCaseId", testCaseId);
        if (response.getStatus() == 200) {
            JSONArray values = JSONObject.fromObject(response.readEntity(String.class)).getJSONArray("value");
            if (values.size() > 0) {
                return values.getJSONObject(0);
            } else throw new RuntimeException("Given test case was not found in given test plan / suite! " + path);
        } else throw new RuntimeException("Can not find test point with path: " + path);
    }

    /**
     * @param suiteId suite id
     * @param planId  plan id
     * @param option  can be null as default to get all, or {@link QUERY_OPTION}
     * @return list of ids which be found with option
     */
    public List<String> getTestCaseIdsInPlan(String planId, String suiteId, QUERY_OPTION option) {
        String path = organization + "/" + collection + "/" + project + "/_apis/test/Plans/" +
                planId + "/Suites/" + suiteId + "/testcases";
        Response response = tfsConnector.get(path);
        if (response.getStatus() == 200) {
            JSONArray testCaseArray = JSONObject.fromObject(response.readEntity(String.class)).getJSONArray("value");
            List<String> testCaseIds = new LinkedList<>();
            for (int i = 0; i < testCaseArray.size(); i++) {
                JSONObject entry = testCaseArray.getJSONObject(i);
                String tcid = entry.getJSONObject("testCase").getString("id");
                handlingTestCaseIdWithOption(testCaseIds, planId, suiteId, tcid, option);
            }
            return testCaseIds;
        } else {
            throw new RuntimeException("Error while getting test case id from TFS! \n" + response.readEntity(String.class));
//            return Collections.emptyList();
        }
    }

    /**
     * get test plan configuration
     *
     * @param configurationId configuration id
     * @return config map
     */
    public Map<String, String> getTestPlanConfiguration(String configurationId) {
        String path = organization + "/" + collection + "/" + project + "/_apis/testplan/configurations/" + configurationId;
        Response response = tfsConnector.get(path);
        if (response.getStatus() == 200) {
            JSONObject result = JSONObject.fromObject(response.readEntity(String.class));
            Map<String, String> config = new LinkedHashMap<>();
            config.put("name", result.getString("name"));
            config.put("description", result.getString("description"));
            JSONArray values = result.getJSONArray("values");
            for (int i = 0; i < values.size(); i++) {
                config.put(values.getJSONObject(i).getString("name"), values.getJSONObject(i).getString("value"));
            }
            return config;
        } else {
            return Collections.emptyMap();
        }
    }

    /**
     * Get last run status of test case with given test plan, suite and test case id
     *
     * @param planId  test plan id
     * @param suiteId test suite id
     * @param tcid    test case id
     * @return test case id
     */
    public String getLastRunStatus(String planId, String suiteId, String tcid) {
        return getTestPoint(planId, suiteId, tcid).getJSONObject("results").getString("outcome");
    }

    /**
     * check if last run of test case is successful
     *
     * @param planId  test plan id
     * @param suiteId test suite id
     * @param tcid    test case id
     * @return true if successful
     */
    public boolean isSuccess(String planId, String suiteId, String tcid) {
        return "success".equalsIgnoreCase(getLastRunStatus(planId, suiteId, tcid));
    }

    /**
     * check if last run of test case is failed
     *
     * @param planId  test plan id
     * @param suiteId test suite id
     * @param tcid    test case id
     * @return true if failed
     */
    public boolean isFailed(String planId, String suiteId, String tcid) {
        return "failed".equalsIgnoreCase(getLastRunStatus(planId, suiteId, tcid));
    }

    public void downloadFilesAsZip(String folder, File targetFile) throws IOException {
        String path = organization + "/" + collection + "/" + project + "/_apis/tfvc/items";
        Response response = tfsConnector.downloadItemsInFolderAsZip(path, folder);
        if (response.getStatus() == 200) {
            targetFile.getParentFile().mkdirs();
            SystemLogger.trace("Write to target: " + targetFile.getAbsolutePath());
            Files.copy(response.readEntity(InputStream.class), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } else {
            throw new RuntimeException("Fail to download files from path! Response Code: " + response.getStatus());
        }
    }

    /**
     * in case all, failed only and success only the id will be added correspondingly to the list
     *
     * @param testCaseIds list of ids to add
     * @param planId      plan id
     * @param suiteId     suite id
     * @param tcid        test case id
     * @param option      option of add condition
     */
    private void handlingTestCaseIdWithOption(List<String> testCaseIds, String planId, String suiteId, String tcid, QUERY_OPTION option) {
        if (option == null) {
            option = QUERY_OPTION.ALL;
        }
        boolean toBeAdded = false;
        switch (option) {
            case ALL:
                toBeAdded = true;
                break;
            case FAILED_ONLY:
                if (isFailed(planId, suiteId, tcid)) {
                    toBeAdded = true;
                }
                break;
            case SUCCESS_ONLY:
                if (isSuccess(planId, suiteId, tcid)) {
                    toBeAdded = true;
                }
                break;
            case EXCEPT_SUCCESS:
                if (!isSuccess(planId, suiteId, tcid)) {
                    toBeAdded = true;
                }
                break;
        }
        if (toBeAdded) {
            testCaseIds.add(tcid);
        }
    }

    /**
     * Build Payload for create test run
     *
     * @param runName     test run name
     * @param planId      test plan id
     * @param testSuiteId test suite id (test Sammlung)
     * @param testCaseIds list of test case id
     * @return payload in json format
     */
    private String buildTestRunPayLoad(String runName, String planId, String testSuiteId, List<String> testCaseIds) {
        int[] testPoints = getTestPoints(planId, testSuiteId, testCaseIds);
        JSONObject plan = new JSONObject().element("id", planId);
        JSONObject payload = new JSONObject()
                .element("name", runName)
                .element("automated", true)
                .element("plan", plan)
                .element("startDate", TimeUtils.getISOTimestamp())
                .element("pointIds", testPoints);
        return payload.toString();
    }

    /**
     * to secure all attributes
     *
     * @param parameters params
     */
    private void safeCase(List<Object> parameters) {
        boolean safe = true;
        for (Object parameter : parameters) {
            safe = SafeCase.notNull(parameter);
            if (!safe) {
                break;
            }
        }
        Assert.assertTrue("Initialization of TFS Rest Client failed because of missing parameters", safe);
    }

    /**
     * build test run results to update test run in tfs
     *
     * @param testCaseIdAndResults map of test case id and test run result
     * @param tfsRunResults        to be updated
     * @return payload content in json format
     */
    private String buildTestRunResultPayload(Map<String, TestRunResult> testCaseIdAndResults, JSONObject tfsRunResults) {
        JSONArray payload = new JSONArray();
        JSONArray resultsValues = tfsRunResults.getJSONArray("value");
        for (int i = 0; i < resultsValues.size(); i++) {
            //create request body of payload
            JSONObject requestBody = new JSONObject();
            //get result from result value array
            JSONObject result = resultsValues.getJSONObject(i);
            //get test run result of framework via tfs test case id
            String id = result.getJSONObject("testCase").getString("id");
            //get test run result from junit test
            TestRunResult testCaseRunResult = testCaseIdAndResults.get(id);
            //build request body
            requestBody.element("id", result.getInt("id"))
                    .element("startedDate", TimeUtils.getInstantFromMilli(testCaseRunResult.getStart()))
                    .element("completedDate", TimeUtils.getInstantFromMilli(testCaseRunResult.getStop()))
                    .element("outcome", testCaseRunResult.getStatus().text())
                    .element("state", "Completed")
                    .element("comment", testCaseRunResult.getName());
            //errorMessage, comment, stackTrace missing;
            if (testCaseRunResult.getStatus().equals(TestStatus.FAIL)) {
                requestBody.element("errorMessage", testCaseRunResult.getTestFailure().getMessage());
                requestBody.element("stackTrace", testCaseRunResult.getTestFailure().getTrace());
            }
            //insert test case result into array
            payload.add(i, requestBody);
        }
        return payload.toString();
    }

    public void close() {
        tfsConnector.close();
    }

}
