package ch.qa.testautomation.framework.rest.TFS.connection;

import ch.qa.testautomation.framework.common.IOUtils.FileOperation;
import ch.qa.testautomation.framework.common.enumerations.TestStatus;
import ch.qa.testautomation.framework.common.utils.TimeUtils;
import ch.qa.testautomation.framework.configuration.PropertyResolver;
import ch.qa.testautomation.framework.core.component.TestRunResult;
import ch.qa.testautomation.framework.exception.JsonProcessException;
import ch.qa.testautomation.framework.rest.base.RestClientBase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.ws.rs.core.Response;
import org.junit.Assert;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static ch.qa.testautomation.framework.common.utils.SafeCase.notNull;
import static ch.qa.testautomation.framework.core.json.ObjectMapperSingleton.getObjectMapper;
import static java.util.Arrays.asList;

public class TFSRestClient extends RestClientBase {

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
        super(new TFSConnector(host, pat, apiVersion));
        this.tfsConnector = (TFSConnector) getRestDriver();
        this.apiVersion = apiVersion;
        this.organization = organization;
        this.collection = collection;
        this.project = project;
        safeCase(asList(this.tfsConnector, this.apiVersion, this.collection, this.project, this.organization));
    }

    /**
     * Constructor with map of tfs config
     *
     * @param tfsConfig map of config
     */
    public TFSRestClient(Map<String, String> tfsConfig) {
        super(new TFSConnector(tfsConfig.get("host"), tfsConfig.get("pat"), tfsConfig.get("apiVersion")));
        this.tfsConnector = (TFSConnector) getRestDriver();
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
    public JsonNode createTestRun(String runName, String testPlanId, String testSuiteId, List<String> testCaseIds) {

        String path = organization + "/" + collection + "/" + project + "/_apis/test/runs";
        Response response = tfsConnector.post(path, buildTestRunPayLoad(runName, testPlanId, testSuiteId, testCaseIds));

        return getResponseNode(response, "Failed on Create Test Run! Response Code: " + response.getStatus());
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
    public JsonNode attachmentToTestResult(String runId, String resultsId, String fileName, String comment, String stream) {

        ObjectMapper objectMapper = getObjectMapper();

        String api_Version = apiVersion;
        String path = organization + "/" + collection + "/" + project + "/_apis/test/Runs/" + runId + "/Results/" + resultsId + "/attachments";
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("stream", stream)
                .put("fileName", fileName)
                .put("attachmentType", "GeneralAttachment")
                .put("comment", comment);
        if (!api_Version.contains("preview")) {
            api_Version += "-preview.1";
        }
        Response response = tfsConnector.post(path, payload.toString(), api_Version);

        return getResponseNode(response, "Failed on attach file to result! Response Code: " + response.getStatus());

    }


    /**
     * update test run result
     *
     * @param runId                run id
     * @param testCaseIdAndResults map of test case tfs id and junit test run result
     * @return response of update
     */
    public JsonNode updateTestRunResults(String runId, Map<String, TestRunResult> testCaseIdAndResults) {

        String path = organization + "/" + collection + "/" + project + "/_apis/test/runs/" + runId + "/Results";
        Response response = tfsConnector.patch(path, buildTestRunResultPayload(testCaseIdAndResults, getTestRunResults(runId)));

        return getResponseNode(response, "Failed on Update Test Run Results! Response Code: " + response.getStatus());
    }

    /**
     * update run to complete state with date
     *
     * @param runId         run id
     * @param state         The state of the test run Below are the valid values - NotStarted, InProgress, Completed, Aborted, Waiting
     * @param completedDate date of completion
     * @return object after update in json
     */
    public JsonNode setTestRunState(String runId, String state, String completedDate) {

        ObjectNode payload = getObjectMapper().createObjectNode();
        payload.put("state", state)
                .put("completedDate", completedDate);

        return updateTestRun(runId, payload.toString());
    }

    /**
     * update test run with payload
     *
     * @param runId   run id
     * @param payload payload in json
     * @return object after update in json
     */
    public JsonNode updateTestRun(String runId, String payload) {

        String path = organization + "/" + collection + "/" + project + "/_apis/test/runs/" + runId;
        Response response = tfsConnector.patch(path, payload);

        return getResponseNode(response, "Failed on Update Test Run! Response Code: " + response.getStatus());
    }

    /**
     * get json object of test run
     *
     * @param runId test run id
     * @return object in json
     */
    public JsonNode getTestRun(String runId) {

        String path = organization + "/" + collection + "/" + project + "/_apis/test/runs/" + runId;
        Response response = tfsConnector.get(path);

        return getResponseNode(response, "Failed to get Test Run! Response Code: " + response.getStatus());
    }

    /**
     * get json object of test run results
     *
     * @param runId test run id
     * @return object of results in json
     */
    public JsonNode getTestRunResults(String runId) {

        String path = organization + "/" + collection + "/" + project + "/_apis/test/runs/" + runId + "/results";
        Response response = tfsConnector.get(path);

        return getResponseNode(response, "Failed to get Test Run Results! Response Code: " + response.getStatus());
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
            JsonNode jsonObject = getTestPoint(testPlanId, testSuiteId, testCaseId);
            //get value array of test points and get the first object of test point in array and get the id of test point
            testPoints[i] = jsonObject.get("id").asInt();
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
    public JsonNode getTestPoint(String testPlanId, String testSuiteId, String testCaseId) {

        ObjectMapper objectMapper = getObjectMapper();
        String path = organization + "/" + collection + "/" + project + "/_apis/testplan/Plans/" +
                testPlanId + "/Suites/" + testSuiteId + "/TestPoint";
        Response response = tfsConnector.get(path, "testCaseId", testCaseId);

        if (response.getStatus() == 200) {

            ArrayNode values;
            try {
                values = (ArrayNode) objectMapper.readTree(response.readEntity(String.class)).get("value");
            } catch (JsonProcessingException e) {
                throw new JsonProcessException(e);
            }

            if (values.size() > 0) {
                return values.get(0);
            } else {
                throw new RuntimeException("Given test case was not found in given test plan / suite! " + path);
            }

        } else throw new RuntimeException("Can not find test point with path: " + path);
    }

    /**
     * @param suiteId suite id
     * @param planId  plan id
     * @param option  can be null as default to get all, or {@link QUERY_OPTION}
     * @return list of ids which be found with option
     */
    public List<String> getTestCaseIdsInPlan(String planId, String suiteId, QUERY_OPTION option) {
        ObjectMapper objectMapper = getObjectMapper();
        String path = organization + "/" + collection + "/" + project + "/_apis/test/Plans/" +
                planId + "/Suites/" + suiteId + "/testcases";
        Response response = tfsConnector.get(path);
        if (response.getStatus() == 200) {
            ArrayNode testCaseArray;
            try {
                testCaseArray = (ArrayNode) objectMapper.readTree(response.readEntity(String.class)).get("value");
            } catch (JsonProcessingException e) {
                throw new JsonProcessException(e);
            }
            List<String> testCaseIds = new LinkedList<>();
            for (int i = 0; i < testCaseArray.size(); i++) {
                JsonNode entry = testCaseArray.get(i);
                String tcid = entry.get("testCase").get("id").textValue();
                handlingTestCaseIdWithOption(testCaseIds, planId, suiteId, tcid, option);
            }
            return testCaseIds;
        } else {
            throw new RuntimeException("Error while getting test case id from TFS! \n" + response.readEntity(String.class));
        }
    }

    /**
     * get test plan configuration
     *
     * @param configurationId configuration id
     * @return config map
     */
    public Map<String, String> getTestPlanConfiguration(String configurationId) {
        ObjectMapper objectMapper = getObjectMapper();
        String path = organization + "/" + collection + "/" + project + "/_apis/testplan/configurations/" + configurationId;
        Response response = tfsConnector.get(path);
        if (response.getStatus() == 200) {
            JsonNode result;
            try {
                result = objectMapper.readTree(response.readEntity(String.class));
            } catch (JsonProcessingException e) {
                throw new JsonProcessException(e);
            }
            Map<String, String> config = new LinkedHashMap<>();
            config.put("name", result.get("name").textValue());
            config.put("description", result.get("description").textValue());
            ArrayNode values = (ArrayNode) result.get("values");
            for (int i = 0; i < values.size(); i++) {
                config.put(values.get(i).get("name").textValue(), values.get(i).get("value").textValue());
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
    public String getLastRunStatus(String planId, String suiteId, String tcid) { //prüfen
        return getTestPoint(planId, suiteId, tcid).get("results").get("outcome").textValue();
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

    /**
     * download driver file
     *
     * @param filePath   driver file location path
     * @param targetFile target file to storage
     */
    public void downloadFile(String filePath, File targetFile) {
        String resourceProject = PropertyResolver.getResourceTFSProject();
        String path = organization + "/" + collection + "/" + resourceProject + "/_apis/tfvc/items";
        Response response = tfsConnector.downloadItem(path, filePath);
        if (!isSuccessful(response)) {//in case failed, try with current tfs project
            path = organization + "/" + collection + "/" + project + "/_apis/tfvc/items";
            response = tfsConnector.downloadItem(path, filePath);
        }
        storeStreamIntoFile(response, targetFile);
    }

    /**
     * get items in map with sorted key of its version
     *
     * @param scopePath     look up folder
     * @param fileName      filename
     * @param isFirstLookUp if it is the first look up
     * @return map
     */
    public Map<Integer, String> getItemsMap(String scopePath, String fileName, Boolean isFirstLookUp) {
        String resourceProject = PropertyResolver.getResourceTFSProject();
        String path = organization + "/" + collection + "/" + resourceProject + "/_apis/tfvc/items";
        Map<String, String> params = new HashMap<>(3);
        //decode url path back to plain text with white space
        if (scopePath.contains("%")) {
            scopePath = URLDecoder.decode(scopePath, StandardCharsets.UTF_8);
        }
        params.put("scopePath", scopePath);
        params.put("download", "false");
        params.put("recursionLevel", "full");
        Response response = tfsConnector.get(path, params);
        if (!isSuccessful(response)) {//in case failed, try with current tfs project
            path = organization + "/" + collection + "/" + project + "/_apis/tfvc/items";
            response = tfsConnector.get(path, params);
        }
        JsonNode result = getResponseNode(response, "Fail to get item list!");
        int size = result.get("count").asInt();
        if (size > 0) {
            Map<Integer, String> items = new HashMap<>(size);
            ArrayNode values = (ArrayNode) result.get("value");
            values.forEach(item -> {
                String itemPath = item.get("path").asText();
                if (itemPath.endsWith(".exe") && FileOperation.getFileName(itemPath).contains(fileName)) {
                    items.put(item.get("version").asInt(), itemPath);
                }//todo: other os
            });
            if (items.size() > 0) {
                LinkedHashMap<Integer, String> reverseSortedMap = new LinkedHashMap<>();
                items.entrySet()
                        .stream()
                        .sorted(Map.Entry.comparingByKey(Comparator.reverseOrder()))
                        .forEachOrdered(entry -> reverseSortedMap.put(entry.getKey(), entry.getValue()));
                return reverseSortedMap;
            } else if (isFirstLookUp) {//try to look up in parent folder
                return getItemsMap(scopePath.substring(0, scopePath.lastIndexOf("/")), fileName, false);
            }
        }
        return Collections.emptyMap();
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
        ObjectMapper objectMapper = getObjectMapper();
        ObjectNode plan = objectMapper.createObjectNode();
        plan.put("id", planId);
        int[] testPoints = getTestPoints(planId, testSuiteId, testCaseIds);
        //Array in ein Node umwandeln
        ArrayNode testPointsNode = objectMapper.createArrayNode();
        for (int item : testPoints) {
            testPointsNode.add(item);
        }
        ObjectNode payload = objectMapper.createObjectNode()
                .put("name", runName)
                .put("automated", true)
                .set("plan", plan);
        payload.put("startDate", TimeUtils.getISOTimestamp());
        payload.set("pointIds", testPointsNode);
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
            safe = notNull(parameter);
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
    private String buildTestRunResultPayload(Map<String, TestRunResult> testCaseIdAndResults, JsonNode tfsRunResults) {
        ObjectMapper objectMapper = getObjectMapper();
        ArrayNode payload = objectMapper.createArrayNode();
        ArrayNode resultsValues = (ArrayNode) tfsRunResults.get("value");
        for (int i = 0; i < resultsValues.size(); i++) {
            //get result from result value array
            JsonNode result = resultsValues.get(i);
            //get test run result of framework via tfs test case id
            String id = result.get("testCase").get("id").asText();
            //get test run result from junit test
            TestRunResult testCaseRunResult = testCaseIdAndResults.get(id);
            //build request body
            ObjectNode requestBody = objectMapper.createObjectNode()
                    .put("id", result.get("id").asInt())
                    .put("startedDate", TimeUtils.getInstantFromMilli(testCaseRunResult.getStart()))
                    .put("completedDate", TimeUtils.getInstantFromMilli(testCaseRunResult.getStop()))
                    .put("outcome", testCaseRunResult.getStatus().text())
                    .put("state", "Completed")
                    .put("comment", testCaseRunResult.getName());
            //errorMessage, comment, stackTrace missing;
            if (testCaseRunResult.getStatus().equals(TestStatus.FAIL)) {
                requestBody.put("errorMessage", testCaseRunResult.getTestFailure().getMessage())
                        .put("stackTrace", testCaseRunResult.getTestFailure().getTrace());
            }
            //insert test case result into array
            payload.insert(i, requestBody);
        }
        return payload.toString();
    }

    public void close() {
        tfsConnector.close();
    }

}
