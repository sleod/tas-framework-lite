package ch.qa.testautomation.framework.rest.jira.connection;

import ch.qa.testautomation.framework.common.IOUtils.FileOperation;
import ch.qa.testautomation.framework.configuration.PropertyResolver;
import ch.qa.testautomation.framework.core.component.TestRunResult;
import ch.qa.testautomation.framework.core.json.deserialization.JSONContainerFactory;
import ch.qa.testautomation.framework.rest.base.RestClientBase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.ws.rs.core.Response;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class JIRARestClient extends RestClientBase {
    private final JIRAConnector jiraConnector;
    private String user = "";

    /**
     * Constructor with basic authentication
     *
     * @param username username
     * @param password password
     * @param host     jiraUrl
     */
    public JIRARestClient(String host, String username, String password) {
        super(new JIRAConnector(host, username, password));
        this.jiraConnector = (JIRAConnector) getRestDriver();
        this.user = username;
    }

    /**
     * Create Jira Rest Client with PAT Token
     *
     * @param patToken PAT Token
     */
    public JIRARestClient(String host, String patToken) {
        super(new JIRAConnector(host, patToken));
        this.jiraConnector = (JIRAConnector) getRestDriver();
    }

    public JsonNode getIssue(String issueKey) {
        Response response = jiraConnector.get("/rest/api/latest/issue/" + issueKey);
        return getResponseNode(response, "Fail on get Issue with key: " + issueKey);
    }

    public JsonNode searchIssue(String jql) {
        Response response = jiraConnector.get("/rest/api/latest/search", "jql", encodeUrlPath(jql));
        return getResponseNode(response, "Fail on search Issue with JQL: " + jql);
    }

    public JsonNode createTestExecution(String projectKey, String summary, String description) {
        String path = "/rest/api/latest/issue/";
        Response response = jiraConnector.post(path, buildIssue(projectKey, summary, description, "Test Execution").asText());
        return getResponseNode(response, "Fail on create Test Execution: " + path);
    }

    /**
     * add xray test exection to test plan
     *
     * @param exeKey   execution key
     * @param planKeys test plan key
     */
    public void addTestExecutionToPlan(String exeKey, List<String> planKeys) {
        for (String planKey : planKeys) {
            String path = "rest/raven/2.0/api/testplan/" + planKey + "/testexecution";
            ObjectMapper objectMapper = new ObjectMapper();
            ArrayNode keyList = objectMapper.createArrayNode();
            ObjectNode payload = objectMapper.createObjectNode();
            keyList.add(exeKey);
            payload.set("add", keyList);
            jiraConnector.post(path, payload.asText());
        }
    }

    /**
     * remove xray test case from test plan
     *
     * @param testKeys test case key
     * @param planKey  test plan key
     */
    public void removeTestsFromPlan(List<String> testKeys, String planKey) {
        String path = "rest/raven/1.0/api/testplan/" + planKey + "/test";
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode keyList = objectMapper.createArrayNode();
        ObjectNode payload = objectMapper.createObjectNode();
        testKeys.forEach(keyList::add);
        payload.set("remove", keyList);
        jiraConnector.post(path, payload.asText());
    }

    /**
     * add issue as link to another issue
     *
     * @param source  source issue key
     * @param targets target issue keys
     */
    public void addIssueLink(String source, List<String> targets) {
        String path = "rest/api/latest/issueLink";
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
            jiraConnector.post(path, payload.asText());
        }
    }

    /**
     * add xray test cases to execution
     *
     * @param testKeys key of test cases
     * @param key      test execution key
     */
    public void addTestsToExecution(List<String> testKeys, String key) {
        String path = "rest/raven/latest/api/testexec/" + key + "/test";
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode keyList = objectMapper.createArrayNode();
        ObjectNode payload = objectMapper.createObjectNode();
        testKeys.forEach(keyList::add);
        payload.set("add", keyList);
        jiraConnector.post(path, payload.asText());
    }

    /**
     * remove xray test cases from execution
     *
     * @param testKeys key of test cases
     * @param key      test execution key
     */
    public void removeTestsFromExecution(List<String> testKeys, String key) {
        String path = "rest/raven/latest/api/testexec/" + key + "/test";
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode keyList = objectMapper.createArrayNode();
        ObjectNode payload = objectMapper.createObjectNode();
        testKeys.forEach(keyList::add);
        payload.set("remove", keyList);
        jiraConnector.post(path, payload.asText());
    }


    /**
     * set status of run result to run
     *
     * @param exeKey               test execution key
     * @param testCaseIdAndResults results map: (test case key : result)
     */
    public void updateStatusToExecution(String exeKey, Map<String, TestRunResult> testCaseIdAndResults) throws IOException {
        JsonNode jiraConfig = JSONContainerFactory.getConfig(PropertyResolver.getJiraConfigFile());
        //get execution
        JsonNode execution = getIssue(exeKey);
        //get run list of test execution that contains the instances of test cases
        JsonNode runListNode = execution.get(jiraConfig.get("customFields").get("execution").get("tests").asText());
        if (runListNode.isArray()) {
            ArrayNode runList = (ArrayNode) runListNode;
            for (JsonNode instance : runList) {
                //b:test case key
                String testId = instance.get("b").asText();
                if (testCaseIdAndResults.containsKey(testId)) {
                    //get result of the instance via tcKey
                    TestRunResult testRunResult = testCaseIdAndResults.get(testId);
                    //get runId of the instance of test case
                    String runId = instance.get("c").toString();
                    //fetch result of run instance to: PASS, TO DO, FAIL, ABORTED
                    String runStatus = testRunResult.getStatus().text();
                    //build uri to get and update status
                    String path = "rest/raven/latest/api/testrun/" + runId + "/status?status=" + runStatus;
                    jiraConnector.put(path, "");
                    //upload console output as run evidence
                    addRunEvidence(runId, new File(testRunResult.getLogFilePath()));
                }
            }
        }
    }

    /**
     * upload evidence to run in test execution
     *
     * @param runId   run id of the evidence
     * @param logFile log file
     * @throws IOException IOeException while reading file
     */
    public void addRunEvidence(String runId, File logFile) throws IOException {
        String path = "rest/raven/latest/api/testrun/" + runId + "/attachment";
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("data", Base64.getEncoder().encodeToString(FileOperation.readFileToByteArray(logFile)))
                .put("filename", logFile.getName())
                .put("contentType", "text/plain");
        jiraConnector.post(path, payload.asText());
    }

    /**
     * build Xray Test Object. Note that, the customerfield can be changed
     *
     * @param projectKey  project key
     * @param summary     summary
     * @param description description of test
     * @param plans       plan key
     * @return JSONObject as payload
     */
    private JsonNode buildXrayTest(String projectKey, String summary, String description, List<String> plans, List<String> testStepNames) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode project = objectMapper.createObjectNode().put("id", projectKey);
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

    //todo: check with jira rest api
    /**
     * build Issue with given properties
     *
     * @param projectKey  project key
     * @param summary     summary
     * @param description description of issue
     * @param issieType   issue type
     * @return JSONObject as payload
     */
    private JsonNode buildIssue(String projectKey, String summary, String description, String issieType) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode project = objectMapper.createObjectNode().put("id", projectKey);
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
