package ch.qa.testautomation.framework.rest.jira.connection;

import ch.qa.testautomation.framework.common.IOUtils.FileOperation;
import ch.qa.testautomation.framework.common.utils.DateTimeUtils;
import ch.qa.testautomation.framework.configuration.PropertyResolver;
import ch.qa.testautomation.framework.core.component.TestRunResult;
import ch.qa.testautomation.framework.core.json.container.JSONRunnerConfig;
import ch.qa.testautomation.framework.core.json.deserialization.JSONContainerFactory;
import ch.qa.testautomation.framework.exception.ApollonBaseException;
import ch.qa.testautomation.framework.exception.ApollonErrorKeys;
import ch.qa.testautomation.framework.rest.base.QUERY_OPTION;
import ch.qa.testautomation.framework.rest.base.RestClientBase;
import ch.qa.testautomation.framework.rest.base.RestDriverBase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.ws.rs.core.Response;

import java.io.File;
import java.util.*;

public class JIRARestClient extends RestClientBase {
    private String user = "";
    private static final String GENERAL_PATH = "rest/api/latest/";
    private static final String XRAY_PATH = "rest/raven/latest/api/";

    /**
     * Constructor with basic authentication
     *
     * @param username username
     * @param password password
     * @param host     jiraUrl
     */
    public JIRARestClient(String host, String username, String password) {
        super(new RestDriverBase(host, username, password));
        this.user = username;
    }

    /**
     * Create Jira Rest Client with PAT Token
     *
     * @param patToken PAT Token
     */
    public JIRARestClient(String host, String patToken) {
        super(new RestDriverBase(host, patToken));
    }

    /**
     * get issue with key
     *
     * @param issueKey issue key
     * @return issue in JsonNode
     */
    public JsonNode getIssue(String issueKey) {
        Response response = getRestDriver().get(GENERAL_PATH + "issue/" + issueKey);
        return getResponseNode(response, "Fail on get Issue with key: " + issueKey);
    }

    /**
     * search issues with JQL
     *
     * @param jql jql
     * @return search result
     */
    public JsonNode searchIssue(String jql) {
        Response response = getRestDriver().get(GENERAL_PATH + "search", "jql", jql);
        return getResponseNode(response, "Fail on search Issue with JQL!");
    }

    /**
     * create test execution
     *
     * @param projectKey  project key
     * @param summary     summary
     * @param description description
     * @return created issue
     */
    public JsonNode createTestExecution(String projectKey, String summary, String description) {
        return createIssue(buildIssue(projectKey, summary, description, "Test Execution"));
    }

    /**
     * generally create issue in jira with payload in case issue with same summery not exists
     *
     * @param payload body to post
     * @return response in JsonNode
     */
    public JsonNode createIssue(JsonNode payload) {
        return createIssue(payload.get("fields").get("summary").asText(), payload.toString());
    }

    /**
     * generally create issue in jira with payload in case issue with same summery not exists
     *
     * @param payload body to post
     * @param summary summary of issue
     * @return response in JsonNode
     */
    public JsonNode createIssue(String summary, String payload) {
        String path = GENERAL_PATH + "issue";
        JsonNode result = searchIssue("summary~'" + encodeUrlPath(summary) + "'");
        if (result.get("total").asInt() == 0) {
            return getResponseNode(getRestDriver().post(path, payload),
                    "Fail on create issue: " + path + "\nwith payload: " + payload);
        } else {
            throw new ApollonBaseException(ApollonErrorKeys.JIRA_ISSUE_SUMMARY_EXISTS, summary);
        }
    }


    /**
     * generally edit issue in jira with payload {"fields":{...}}
     *
     * @param payload  body to post
     * @param issueKey issueKey
     * @return response in JsonNode
     */
    public JsonNode updateIssue(String issueKey, JsonNode payload) {
        String path = GENERAL_PATH + "issue/" + issueKey;
        return getResponseNode(getRestDriver().put(path, payload.toString()),
                "Fail on update issue: " + path + "\nwith payload: " + payload);
    }

    /**
     * get tests in execution
     *
     * @param key      test execution key
     * @param detailed boolean if detailed data retrieved
     * @return tests information
     */
    public JsonNode getTestsInExecution(String key, boolean detailed) {
        String path = XRAY_PATH + "testexec/" + key + "/test";
        return getResponseNode(getRestDriver().get(path, "detailed=" + detailed),
                "Fail on get tests in execution with path: " + path);
    }

    /**
     * get test ids in Execution with query option according run status
     *
     * @param key          execution key
     * @param query_option {@link QUERY_OPTION}
     * @return list of test keys
     */
    public List<String> getTestsInExecution(String key, QUERY_OPTION query_option) {
        JsonNode jiraTests = getTestsInExecution(key, false);
        List<String> testIds = new LinkedList<>();
        if (query_option == null || query_option.equals(QUERY_OPTION.ALL)) {
            jiraTests.forEach(test -> testIds.add(test.get("key").asText()));
        } else if (query_option.equals(QUERY_OPTION.EXCEPT_SUCCESS)) {
            jiraTests.forEach(test -> {
                if (!test.get("status").asText().equalsIgnoreCase(JiraRunStatus.PASS.text())) {
                    testIds.add(test.get("key").asText());
                }
            });
        } else if (query_option.equals(QUERY_OPTION.FAILED_ONLY)) {
            jiraTests.forEach(test -> {
                if (!test.get("status").asText().equalsIgnoreCase(JiraRunStatus.FAIL.text())) {
                    testIds.add(test.get("key").asText());
                }
            });
        }
        return testIds;
    }

    /**
     * get test runs for every test in test execution
     *
     * @param key test execution key
     * @return test runs
     */
    public JsonNode getTestRunsInExecution(String key) {
        String path = XRAY_PATH + "testruns";
        return getResponseNode(getRestDriver().get(path, "testExecKey", key),
                "Fail on get test runs in execution with path: " + path);
    }

    /**
     * get test run key:id map of tests in execution
     *
     * @param key test execution key
     * @return test run key:id map
     */
    public Map<String, Integer> getTestRunKeyIdMapInExecution(String key) {
        JsonNode runs = getTestRunsInExecution(key);
        Map<String, Integer> idKeyMap = new HashMap<>(runs.size());
        runs.forEach(run -> idKeyMap.put(run.get("testKey").asText(), run.get("id").asInt()));
        return idKeyMap;
    }

    /**
     * add xray test execution to test plan
     *
     * @param exeKey  execution key
     * @param planKey test plan key
     * @return result
     */
    public JsonNode addTestExecutionToPlan(String exeKey, String planKey) {
        String path = XRAY_PATH + "testplan/" + planKey + "/testexecution";
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode keyList = objectMapper.createArrayNode();
        ObjectNode payload = objectMapper.createObjectNode();
        keyList.add(exeKey);
        payload.set("add", keyList);
        return getResponseNode(getRestDriver().post(path, payload.toString()),
                "Fail on add execution to Plans with path: " + path);
    }

    /**
     * remove xray test case from test plan
     *
     * @param testKeys test case key
     * @param planKey  test plan key
     * @return result
     */
    public JsonNode removeTestsFromPlan(List<String> testKeys, String planKey) {
        String path = XRAY_PATH + "testplan/" + planKey + "/test";
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode keyList = objectMapper.createArrayNode();
        ObjectNode payload = objectMapper.createObjectNode();
        testKeys.forEach(keyList::add);
        payload.set("remove", keyList);
        return getResponseNode(getRestDriver().post(path, payload.toString()),
                "Fail on delete tests from Plans with path: " + path);
    }

    /**
     * add issue as link to another issue
     *
     * @param source  source issue key
     * @param targets target issue keys
     */
    public void addIssueLink(String source, List<String> targets) {
        String path = GENERAL_PATH + "issueLink";
        ObjectMapper objectMapper = new ObjectMapper();
        for (String target : targets) {
            ObjectNode type = objectMapper.createObjectNode().put("name", "Tests");
            ObjectNode outwardIssue = objectMapper.createObjectNode().put("key", target);
            ObjectNode inwardIssue = objectMapper.createObjectNode().put("key", source);
            ObjectNode comment = objectMapper.createObjectNode().put("body", "Link xray test to story");
            ObjectNode payload = objectMapper.createObjectNode()
                    .<ObjectNode>set("type", type)
                    .<ObjectNode>set("inwardIssue", inwardIssue)
                    .<ObjectNode>set("outwardIssue", outwardIssue)
                    .set("comment", comment);
            getRestDriver().post(path, payload.toString());
        }
    }

    /**
     * add xray test cases to execution
     *
     * @param testKeys key of test cases
     * @param execKey  test execution key
     * @return result
     */
    public JsonNode addTestsToExecution(List<String> testKeys, String execKey) {
        String path = XRAY_PATH + "testexec/" + execKey + "/test";
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode keyList = objectMapper.createArrayNode();
        ObjectNode payload = objectMapper.createObjectNode();
        testKeys.forEach(keyList::add);
        payload.set("add", keyList);
        return getResponseNode(getRestDriver().post(path, payload.toString()),
                "Fail on add tests to test execution with path: " + path);
    }

    /**
     * remove xray test cases from execution
     *
     * @param testKeys key of test cases
     * @param key      test execution key
     * @return result
     */
    public JsonNode removeTestsFromExecution(List<String> testKeys, String key) {
        String path = XRAY_PATH + "testexec/" + key + "/test";
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode keyList = objectMapper.createArrayNode();
        ObjectNode payload = objectMapper.createObjectNode();
        testKeys.forEach(keyList::add);
        payload.set("remove", keyList);
        return getResponseNode(getRestDriver().post(path, payload.toString()),
                "Fail on add tests to test execution with path: " + path);
    }

    /**
     * set status of run result to run
     *
     * @param jiraExecConfig       test run config
     * @param testCaseIdAndResults results map: (test case key : result)
     */
    public void updateRunStatusInExecution(JSONRunnerConfig jiraExecConfig, Map<String, TestRunResult> testCaseIdAndResults) {
        String exeKey = jiraExecConfig.getTestExecutionId();
        if (exeKey.isEmpty()) {//create one execution for test runs
            JsonNode issue = createTestExecution(jiraExecConfig.getProjectKey(),
                    jiraExecConfig.getRunName() + " - " + DateTimeUtils.getFormattedLocalTimestamp(),
                    "Automated Test Execution with Apollon Framework " + DateTimeUtils.getFormattedLocalTimestamp());
            exeKey = issue.get("key").asText();
            addTestsToExecution(new ArrayList<>(testCaseIdAndResults.keySet()), exeKey);
        }
        //get runs in execution
        JsonNode runs = getTestRunsInExecution(exeKey);
        if (runs.isArray()) {
            runs.forEach(run -> {
                String testKey = run.get("testKey").asText();
                if (testCaseIdAndResults.containsKey(testKey)) {
                    updateRunStatus(run, testCaseIdAndResults.get(testKey));
                }
            });
        }
    }

    public void updateRunStatus(JsonNode run, TestRunResult testRunResult) {
        List<File> dataFiles = testRunResult.getAttachments();
        dataFiles.add(new File(testRunResult.getLogFilePath()));
        if (run != null) {
            updateRunStatus(run.get("id").asText(), testRunResult.getDescription(),
                    dataFiles,
                    testRunResult.getStatus().intValue());
        } else {
            throw new ApollonBaseException(ApollonErrorKeys.TEST_RUN_OR_TEST_RUN_RESULT_IS_NULL);
        }
    }

    public JsonNode updateRunStatus(String runId, String description, List<File> dataFiles, int statusIndex) {
        String path = XRAY_PATH + "testrun/" + runId;
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode evidence = objectMapper.createArrayNode();
        dataFiles.forEach(file ->
                evidence.add(objectMapper.createObjectNode()
                        .put("filename", file.getName())
                        .put("contentType", "plain/text")
                        .put("data", FileOperation.encodeFileToBase64(file))));
        JsonNode evidences = objectMapper.createObjectNode().set("add", evidence);
        ObjectNode payload = objectMapper.createObjectNode()
                .put("status", JiraRunStatus.getText(statusIndex))
                .put("comment", description)
                .set("evidences", evidences);
        return getResponseNode(getRestDriver().put(path, payload.toString()), "Fail with update status to run: " + runId);
    }

    /**
     * build Xray Test Object. Note that, the customer fields can be changed
     *
     * @param projectKey  project key
     * @param summary     summary
     * @param description description of test
     * @param plans       plan key
     * @return JSONObject as payload
     */
    private JsonNode buildXrayTest(String projectKey, String summary, String description, List<String> plans, List<String> testStepNames) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode project = objectMapper.createObjectNode().put("key", projectKey);
        ObjectNode issueType = objectMapper.createObjectNode().put("name", "Test").put("id", "10400");
        ObjectNode testType = objectMapper.createObjectNode().put("value", "Manual").put("id", "10200");
        ArrayNode planList = objectMapper.createArrayNode();
        plans.forEach(planList::add);
        ArrayNode steps = objectMapper.createArrayNode();
        for (int i = 0; i < testStepNames.size(); i++) {
            ObjectNode step = objectMapper.createObjectNode().put("index", i + 1)
                    .set("fields", objectMapper.createObjectNode().put("Action", testStepNames.get(i)));
            steps.add(step);
        }
        JsonNode jiraConfig = JSONContainerFactory.getConfig(PropertyResolver.getJiraConfigFile());
        JsonNode customFields = jiraConfig.get("customFields").get("test");
        ObjectNode fields = objectMapper.createObjectNode()
                .put("summary", summary)
                .put("description", description)
                .<ObjectNode>set("project", project)
                .<ObjectNode>set("issuetype", issueType)
                .<ObjectNode>set(customFields.get("testType").asText(), testType)
                .<ObjectNode>set(customFields.get("testSteps").asText(), steps)
                .set(customFields.get("testPlans").asText(), planList);
        if (!user.isEmpty()) {
            ObjectNode assignee = objectMapper.createObjectNode().put("name", user);
            fields.set("assignee", assignee);
        }
        return objectMapper.createObjectNode().<ObjectNode>set("fields", fields);
    }


    /**
     * build Issue with given properties. like:
     * <a href="https://developer.atlassian.com/server/jira/platform/jira-rest-api-examples/#creating-an-issue-using-a-project-key-and-field-names">Jira REST API examples</a>
     *
     * @param projectKey  project key
     * @param summary     summary
     * @param description description of issue
     * @param issieType   issue type
     * @return JsonNode as payload
     */
    private JsonNode buildIssue(String projectKey, String summary, String description, String issieType) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode project = objectMapper.createObjectNode().put("key", projectKey);
        ObjectNode issueType = objectMapper.createObjectNode().put("name", issieType);
        ObjectNode fields = objectMapper.createObjectNode()
                .put("summary", summary)
                .put("description", description)
                .<ObjectNode>set("project", project)
                .set("issuetype", issueType);
        if (!user.isEmpty()) {
            ObjectNode assignee = objectMapper.createObjectNode().put("name", user);
            fields.set("assignee", assignee);
        }
        return objectMapper.createObjectNode().<ObjectNode>set("fields", fields);
    }

}