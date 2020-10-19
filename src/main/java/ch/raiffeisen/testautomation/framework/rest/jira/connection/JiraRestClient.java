package ch.raiffeisen.testautomation.framework.rest.jira.connection;


import ch.raiffeisen.testautomation.framework.common.utils.TimeUtils;
import ch.raiffeisen.testautomation.framework.configuration.PropertyResolver;
import ch.raiffeisen.testautomation.framework.core.json.deserialization.JSONContainerFactory;
import net.rcarz.jiraclient.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;

public class JiraRestClient {

    private static final int MAX = 500;
    private final HashMap<String, Issue> jiraIssueNameIndex = new HashMap<>(MAX);
    private final HashMap<String, Issue> jiraIssueKeyIndex = new HashMap<>(MAX);
    private final HashMap<String, Issue> jiraIssueOfSprint = new HashMap<>(MAX);
    private final JiraClient jira;
    private JSONObject customFieldConfig;
    private JSONObject jiraConfig;

    /**
     * standard construct that use the basic authentication with defined user in properties
     */
    public JiraRestClient(){
        jira = new JiraClient(jiraConfig.getString("host"), buildCreds());
        customFieldConfig = JSONContainerFactory.getConfig(PropertyResolver.getJiraCustomFieldConfig());
        jiraConfig = JSONContainerFactory.getConfig(PropertyResolver.getJIRAConfigFile());
    }

    /**
     * build jira client with given url, user and password
     *
     * @param url      jira host
     * @param user     user name
     * @param password password
     */
    public JiraRestClient(String url, String user, String password) {
        jira = new JiraClient(url, new BasicCredentials(user, password));
        customFieldConfig = JSONContainerFactory.getConfig(PropertyResolver.getJiraCustomFieldConfig());
        jiraConfig = JSONContainerFactory.getConfig(PropertyResolver.getJIRAConfigFile());
    }

    /**
     * build jira client with given host, user, pass and pre-load stories with given projects name and sprint
     *
     * @param url      jira host
     * @param user     user name
     * @param pass     password
     * @param projects project name
     * @param sprint   sprint name
     */
    public JiraRestClient(String url, String user, String pass, String projects, String sprint) throws JiraException {
        jira = new JiraClient(url, new BasicCredentials(user, pass));
        loadStoriesOfSprint(projects, sprint);
        customFieldConfig = JSONContainerFactory.getConfig(PropertyResolver.getJiraCustomFieldConfig());
        jiraConfig = JSONContainerFactory.getConfig(PropertyResolver.getJIRAConfigFile());
    }


    public HashMap<String, List<Issue>> getClosedIssuesInSprint(String project, String sprint) throws JiraException {
        HashMap<String, List<Issue>> result = new HashMap<>();
        List<Issue> ce = jira.searchIssues("project=" + project + " AND type=Epic AND status=Closed AND sprint=\"" + sprint + "\"", MAX).issues;
        result.put("closedEpics", ce);
        List<Issue> cs = jira.searchIssues("project=" + project + " AND type=Story AND status=Closed AND sprint=\"" + sprint + "\"", MAX).issues;
        result.put("closedStories", cs);
        List<Issue> cb = jira.searchIssues("project=" + project + " AND type=Bug AND status=Closed AND sprint=\"" + sprint + "\"", MAX).issues;
        result.put("closedBugs", cb);
        return result;
    }

    /**
     * get issue from pre-loaded issues
     *
     * @param name name of issue
     * @return issue
     */
    public Issue getIssueFromNameIndex(String name) {
        return jiraIssueNameIndex.get(name);
    }

    /**
     * get issue from pre-loaded issues
     *
     * @param key issue key
     * @return issue
     */
    public Issue getIssueFromKeyIndex(String key) {
        return jiraIssueKeyIndex.get(key);
    }

    /**
     * get issue description via issue key. In case the issue does not pre loaded,
     * Jira client will search the issue with given key.
     *
     * @param issueKey key of issue
     * @return description
     * @throws JiraException jira exception
     */
    public String getIssueDescription(String issueKey) throws JiraException {
        if (issueKey != null && !issueKey.equalsIgnoreCase("null")) {
            if (jiraIssueKeyIndex.containsKey(issueKey)) {
                return jiraIssueKeyIndex.get(issueKey).getDescription();
            } else {
                return jira.getIssue(issueKey).getDescription();
            }
        } else {
            throw new RuntimeException("Issue key can not be null for search!");
        }
    }

    /**
     * get issue Summary via issue key. In case the issue does not pre loaded,
     * Jira client will search the issue with given key.
     *
     * @param issueKey key of issue
     * @return Summary of issue
     * @throws JiraException jira exception
     */
    public String getIssueSummary(String issueKey) throws JiraException {
        if (issueKey != null && !issueKey.equalsIgnoreCase("null")) {
            if (jiraIssueKeyIndex.containsKey(issueKey)) {
                return jiraIssueKeyIndex.get(issueKey).getSummary();
            } else {
                return jira.getIssue(issueKey).getSummary();
            }
        } else {
            throw new RuntimeException("Issue key can not be null for search!");
        }

    }

    /**
     * get field of given issue
     *
     * @param iss   issue
     * @param fname field name
     * @return name
     */
    public String getField(Issue iss, String fname) {
        if (iss == null) {
            return null;
        } else {
            return iss.getField(fname).toString();
        }
    }

    /**
     * find issue with key
     *
     * @param key issue key
     * @return issue
     * @throws JiraException jira exception
     */
    public Issue getIssue(String key) throws JiraException {
        if (key != null && !key.equalsIgnoreCase("null")) {
            if (jiraIssueKeyIndex.containsKey(key)) {
                return jiraIssueKeyIndex.get(key);
            } else {
                return jira.getIssue(key);
            }
        } else {
            throw new RuntimeException("Issue key can not be null for search!");
        }
    }

    /**
     * fetch stories of epic
     *
     * @param epicKey epic key
     * @param project project name
     * @return list of story names
     */
    public LinkedList<String> searchStoriesOfEpic(String epicKey, String project) throws JiraException {
        LinkedList<String> stories = new LinkedList<>();
        List<Issue> ce = jira.searchIssues("project=" + project + " AND type=story AND 'Epic Link'=" + epicKey, MAX).issues;
        ce.forEach(iss -> stories.add(iss.getKey()));

        return stories;
    }

    /**
     * search issue with key
     *
     * @param ikey issue key
     * @return issue
     * @throws JiraException jira exception
     */
    public Issue searchIssueWithKey(String ikey) throws JiraException {
        return jira.getIssue(ikey);
    }

    /**
     * search project relevant issue with give summary
     *
     * @param project   relevant project name
     * @param summary   summary content
     * @param issueType issue type like: bug, test...
     * @return issue
     */
    public Issue searchIssueWithSummary(String project, String summary, String issueType) throws JiraException {
        String[] token = summary.split(" ");
        Issue issue = null;
        String jql = "project=" + project + " AND type='" + issueType + "' AND summary ~ '" + token[0] + "'";
        Issue.SearchResult searchResult = jira.searchIssues(jql);
        if (searchResult != null) {
            List<Issue> results = searchResult.issues;
            for (Issue iss : results) {
                if (iss.getSummary().equalsIgnoreCase(summary)) {
                    issue = iss;
                    break;
                }
            }
        }
        return issue;
    }

    /**
     * create issue with given properties
     *
     * @param summary     summary of issue
     * @param description description of issue
     * @param issueType   issue type
     * @return created issue key
     */
    public String createIssue(String project, String summary, String description, String issueType) throws RestException, IOException, URISyntaxException {
        JSONObject payload = buildIssue(project, summary, description, issueType);
        String resp = executePost(null, payload);
        return JSONObject.fromObject(resp).get("key").toString();
    }

    /**
     * create Xray test case with given properties
     *
     * @param project     project
     * @param summary     summary of test case
     * @param description description of test case
     * @param definition  definition of test case
     * @param planKeys    xray test play key
     * @return created issue key
     */
    public String createXrayTest(String project, String summary, String description, String definition, List<String> planKeys) throws RestException, IOException, URISyntaxException {
        JSONObject payload = buildXrayTest(project, summary, description, definition, planKeys);
        String resp = executePost(null, payload);
        return JSONObject.fromObject(resp).get("key").toString();
    }

    /**
     * execute post request with payload
     *
     * @param path    path of post
     * @param payload payload in json format
     * @return response of post request
     */
    public String executePost(String path, JSONObject payload) throws URISyntaxException, IOException, RestException {
        return jira.getRestClient().post(path, payload).toString();
    }

    /**
     * create new issue
     *
     * @param project     project name
     * @param summary     summary of issue
     * @param description description of issue
     * @param issueType   type of issue
     * @return created issue
     */
    public Issue createNewIssue(String project, String summary, String description, String issueType) {
        Issue newIssue = null;
        try {
            newIssue = jira.createIssue(project, issueType).field(Field.SUMMARY, summary).field(Field.DESCRIPTION, description).execute();
        } catch (JiraException e) {
            e.printStackTrace();
        }
        return newIssue;
    }

    /**
     * add xray test case to test plan
     *
     * @param testKeys test case key
     * @param planKey  test play key
     */
    public void addTestsToPlan(List<String> testKeys, String planKey) throws RestException, IOException, URISyntaxException {
        String path = "rest/raven/1.0/api/testplan/" + planKey + "/test";
        JSONObject payload = new JSONObject();
        JSONArray keyList = new JSONArray();
        keyList.addAll(testKeys);
        payload.element("add", keyList);
        executePost(path, payload);
        executePost(path, payload);
    }

    /**
     * remove xray test case from test plan
     *
     * @param testKeys test case key
     * @param planKey  test plan key
     */
    public void removeTestsFromPlan(List<String> testKeys, String planKey) throws RestException, IOException, URISyntaxException {
        String path = "rest/raven/1.0/api/testplan/" + planKey + "/test";
        JSONObject payload = new JSONObject();
        JSONArray keyList = new JSONArray();
        keyList.addAll(testKeys);
        payload.element("remove", keyList);
        executePost(path, payload);
    }

    /**
     * add issue as link to another issue
     *
     * @param source  source issue
     * @param targets target issue
     */
    public void addIssueLink(String source, List<String> targets) throws RestException, IOException, URISyntaxException {
        String path = "rest/api/2/issueLink";
        for (String target : targets) {
            JSONObject type = new JSONObject().element("name", "Tests");
            JSONObject outwardIssue = new JSONObject().element("key", target);
            JSONObject inwardIssue = new JSONObject().element("key", source);
            JSONObject comment = new JSONObject().element("body", "Link xray test to story");
            JSONObject payload = new JSONObject().element("type", type)
                    .element("inwardIssue", inwardIssue)
                    .element("outwardIssue", outwardIssue)
                    .element("comment", comment);
            executePost(path, payload);
        }
    }

    /**
     * add xray test exection to test plan
     *
     * @param exeKey   execution key
     * @param planKeys test plan key
     * @throws RestException      rest exception
     * @throws IOException        io exception
     * @throws URISyntaxException uri exception
     */
    public void addTestExecutionToPlan(String exeKey, List<String> planKeys) throws RestException, IOException, URISyntaxException {
        for (String planKey : planKeys) {
            String path = "rest/raven/1.0/api/testplan/" + planKey + "/testexecution";
            JSONArray keyList = new JSONArray();
            JSONObject payload = new JSONObject();
            keyList.add(exeKey);
            payload.element("add", keyList);
            executePost(path, payload);
        }
    }

    /**
     * add xray test cases to execution
     *
     * @param testKeys key of test cases
     * @param key      test execution key
     * @throws RestException      rest exception
     * @throws IOException        io exception
     * @throws URISyntaxException uri exception
     */
    public void addTestsToExecution(List<String> testKeys, String key) throws RestException, IOException, URISyntaxException {
        String path = "rest/raven/1.0/api/testexec/" + key + "/test";
        JSONObject payload = new JSONObject();
        JSONArray keyList = new JSONArray();
        keyList.addAll(testKeys);
        payload.element("add", keyList);
        executePost(path, payload);
    }

    public void removeTestsFromExecution(List<String> testKeys, String key) throws RestException, IOException, URISyntaxException {
        String path = "rest/raven/1.0/api/testexec/" + key + "/test";
        JSONObject payload = new JSONObject();
        JSONArray keyList = new JSONArray();
        keyList.addAll(testKeys);
        payload.element("remove", keyList);
        executePost(path, payload);
    }

    /**
     * find xray test coverages
     *
     * @param projectKey project key
     * @param name       issue name
     * @return list of issue keys
     * @throws JiraException jira exception
     */
    public List<String> findTestCoverages(String projectKey, String name) throws JiraException {
        List<String> coverages = new LinkedList<>();
        String defaultPattern = "^(Test )(.*)$";
        String multiCoverage = "\\[((" + projectKey + "-\\d+)|,)+\\]";
        Pattern pattern = Pattern.compile(multiCoverage);
        Matcher ma = pattern.matcher(name);
        if (ma.find()) {
            String multiKeys = ma.group();
            Pattern keyPattern = Pattern.compile("(" + projectKey + "-\\d+)");
            Matcher keys = keyPattern.matcher(multiKeys);
            while (keys.find()) {
                String key = keys.group();
                coverages.add(key);
            }
        } else {
            pattern = Pattern.compile(defaultPattern);
            ma = pattern.matcher(name);
            if (ma.find()) {
                String storyName = ma.group(1);
                Issue.SearchResult sr = jira.searchIssues("project=" + projectKey + "AND summary~'" + storyName + "'");
                for (Issue iss : sr.issues) {
                    if (iss.getSummary().equalsIgnoreCase(storyName)) {
                        coverages.add(iss.getKey());
                    }
                }
            }
        }
        return coverages;
    }

    /**
     * set status of run result to run
     *
     * @param exeKey test execution key
     * @param status results map: (test case key : console output)
     */
    public void setStatusToRun(String exeKey, Map<String, String> status) throws JiraException, IOException, RestException {
        Issue execution = jira.getIssue(exeKey);
        //get run list of test execution that contains the instances of test cases
        JSONArray runList = (JSONArray) execution.getField(customFieldConfig.getJSONObject("xray").getJSONObject("execution").getString("runList"));
        for (Object obj : runList) {
            JSONObject instance = (JSONObject) obj;
            //b:test case key
            if (status.containsKey(instance.get("b"))) {
                //get result of the instance via tcKey
                String report = status.get(instance.get("b"));
                //get runId of the instance of test case
                String runId = instance.get("c").toString();
                //fetch result of run instance to: PASS, TO DO, FAIL, ABORTED
                String runStatus = fetchRunStatus(report);
                //build uri to get and update status
                String path = "rest/raven/1.0/api/testrun/" + runId + "/status?status=" + runStatus;
                URI uri = URI.create(jiraConfig.getString("host") + path);
                jira.getRestClient().put(uri, null);
                //upload console output as run evidence
                addRunEvidence(runId, report);
            }
        }

    }

    /**
     * upload evidence to run in test execution
     *
     * @param runId         run id of the evidence
     * @param consoleOutput messages
     * @throws IOException   io exception
     * @throws RestException rest exception
     */
    public void addRunEvidence(String runId, String consoleOutput) throws IOException, RestException {
        String path = "rest/raven/1.0/api/testrun/" + runId + "/attachment";
        JSONObject payload = new JSONObject();
        payload.element("data", Base64.getEncoder().encodeToString(consoleOutput.getBytes()))
                .element("filename", TimeUtils.getFormattedLocalTimestamp().concat(".testRunReport.txt"))
                .element("contentType", "text/plain");
        URI uri = URI.create(jiraConfig.get("host") + path);
        jira.getRestClient().post(uri, payload);
    }

    /**
     * attach object to issue natively
     *
     * @param key  issue key
     * @param file attachment file
     */
    public void addAttachment(String key, File file) throws JiraException {
        Issue issue = jira.getIssue(key);
        issue.addAttachment(file);
    }

    public String get(String uri) throws URISyntaxException, IOException, RestException {
        return jira.getRestClient().get(uri).toString();
    }


    /**
     * build BasicCredentials for jira client
     *
     * @return BasicCredentials
     */
    private BasicCredentials buildCreds() {
        String user = jiraConfig.getString("user");
        if (user == null) {
            user = PropertyResolver.getSystemUser();
        }
        String pass = jiraConfig.getString("password");
        if (pass == null) {
            pass = PropertyResolver.getPasswordFromProperty();
        } else {
            pass = new String(Base64.getDecoder().decode(pass));
        }
        return new BasicCredentials(user, pass);
    }

    /**
     * pre-load stories into map with given project name and sprint
     *
     * @param projects project name
     * @param sprint   sprint name
     */
    private void loadStoriesOfSprint(String projects, String sprint) throws JiraException {
        String[] pkey = projects.split(":");
        for (String key : pkey) {
            String jql = "project='" + key + "' AND type=Story AND sprint='" + sprint + "'";
            List<Issue> sr = jira.searchIssues(jql, MAX).issues;
            sr.forEach(issue -> jiraIssueOfSprint.put(issue.getKey(), issue));
        }

    }

    /**
     * pre-load stories into map with given project name and sprint
     *
     * @param projects project name
     */
    private void loadStoriesOfProjects(String projects) throws JiraException {
        String[] pkey = projects.split(":");
        for (String key : pkey) {
            List<Issue> sr = jira.searchIssues("project=" + key + " AND type in (Story, Epic)", MAX).issues;
            sr.forEach(issue -> {
                jiraIssueNameIndex.put(issue.getSummary(), issue);
                jiraIssueKeyIndex.put(issue.getKey(), issue);
            });
        }
    }


    /**
     * fetch status of run result in json
     *
     * @param consoleOutput message
     * @return status
     */
    private String fetchRunStatus(String consoleOutput) {
        String status = "PASS";
        List<String> fails = asList("AssertionError", "(FAILED)", "(NOT PERFORMED)", "(PENDING)");
        for (String statement : fails) {
            if (consoleOutput.contains(statement)) {
                status = "FAIL";
                break;
            }
        }
        return status;
    }

    /**
     * get field "aggregatedTimeSpent" from issue
     *
     * @param issue issue
     * @return time spent
     */
    private long getAggregatedTimeSpent(Issue issue) {
        String value = issue.getField("aggregatetimespent").toString();
        if (value == null || value.equalsIgnoreCase("null")) {
            return 0L;
        } else {
            return Long.parseLong(value);
        }
    }

    /**
     * build Xray Test Object. Note that, the customerfield can be changed
     *
     * @param projectKey  project key
     * @param summary     summary
     * @param description description of test
     * @param definition  definition of test
     * @param plans       plan key
     * @return JSONObject as payload
     */
    private JSONObject buildXrayTest(String projectKey, String summary, String description, String definition, List<String> plans) {
        JSONObject project = new JSONObject().element("key", projectKey);
        JSONObject issuetype = new JSONObject().element("name", "Xray Test");
        JSONObject testType = new JSONObject().element("name", "Cucumber");
        JSONObject contentType = new JSONObject().element("name", "Scenario");
        JSONObject assignee = new JSONObject().element("name", jiraConfig.getString("user"));
        JSONArray planList = new JSONArray();
        planList.addAll(plans);
        JSONObject fieldConfig = customFieldConfig.getJSONObject("xray").getJSONObject("test");
        JSONObject fields = new JSONObject().element("project", project)
                .element("summary", summary)
                .element("assignee", assignee)
                .element("description", description)
                .element("issuetype", issuetype)
                .element(fieldConfig.getString("testType"), testType)
                .element(fieldConfig.getString("contentType"), contentType)
                .element(fieldConfig.getString("definition"), definition)
                .element(fieldConfig.getString("planList"), planList);
        JSONObject self = new JSONObject().element("fields", fields);
        return self;
    }

    /**
     * build Issue with given properties
     *
     * @param projectKey  project key
     * @param summary     summary
     * @param description description of issue
     * @param issieType   issue type
     * @return JSONObject as payload
     */
    private JSONObject buildIssue(String projectKey, String summary, String description, String issieType) {
        JSONObject project = new JSONObject().element("key", projectKey);
        JSONObject issuetype = new JSONObject().element("name", issieType);
        JSONObject assignee = new JSONObject().element("name", jiraConfig.getString("user"));
        JSONObject fields = new JSONObject().element("project", project)
                .element("summary", summary)
                .element("assignee", assignee)
                .element("description", description)
                .element("issuetype", issuetype);
        JSONObject self = new JSONObject().element("fields", fields);
        return self;
    }

}
