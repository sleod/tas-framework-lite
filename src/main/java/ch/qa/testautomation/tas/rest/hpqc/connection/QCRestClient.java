package ch.qa.testautomation.tas.rest.hpqc.connection;

import ch.qa.testautomation.tas.common.IOUtils.FileOperation;
import ch.qa.testautomation.tas.common.logging.Screenshot;
import ch.qa.testautomation.tas.configuration.PropertyResolver;
import ch.qa.testautomation.tas.core.component.TestCaseObject;
import ch.qa.testautomation.tas.core.component.TestCaseStep;
import ch.qa.testautomation.tas.core.component.TestRunResult;
import ch.qa.testautomation.tas.core.component.TestStepResult;
import ch.qa.testautomation.tas.core.json.container.JSONTestCase;
import ch.qa.testautomation.tas.core.json.deserialization.JSONContainerFactory;
import ch.qa.testautomation.tas.exception.ApollonBaseException;
import ch.qa.testautomation.tas.exception.ApollonErrorKeys;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static ch.qa.testautomation.tas.common.logging.SystemLogger.debug;
import static ch.qa.testautomation.tas.common.logging.SystemLogger.info;
import static ch.qa.testautomation.tas.common.utils.StringTextUtils.isValid;

public class QCRestClient {

    private final QCConnector qcConnector;
    private final String domain;
    private final String project;
    private final String testPlanRootFolder;
    private final String testLabRootFolder;
    private final String testPlanFolderPath;
    private final String testLabFolderPath;
    private final String testSetName;
    private final boolean vcEnabled;
    private final String user;
    private String testPlanFolderID = "";
    private String testLabFolderID = "";
    private String testPlanRootFolderID = "";
    private String testLabRootFolderID = "";
    private final Map<String, String> testCaseRequiredFields;

    private String currentTestCasePackage = "";

    /**
     * QC Rest Client with qcConfig.json
     *
     * @param configFilePath file path of qcConfig.json
     */
    public QCRestClient(String configFilePath) {
        JsonNode config = JSONContainerFactory.getConfig(configFilePath);
        this.user = PropertyResolver.getQCUser();
        this.qcConnector = new QCConnector(config.get("host").asText(), user, PropertyResolver.getQCPassword());
        this.project = config.get("project").asText();
        this.domain = config.get("domain").asText();
        this.testPlanRootFolder = config.get("testPlanRootFolder").asText();
        this.testLabRootFolder = config.get("testLabRootFolder").asText();
        this.testPlanFolderPath = config.get("testPlanFolderPath").asText();
        this.testLabFolderPath = config.get("testLabFolderPath").asText();
        this.vcEnabled = config.get("versionControlEnabled").asBoolean();
        this.testSetName = config.get("testSetName").asText();
        testCaseRequiredFields = new ObjectMapper().convertValue(config.get("testCaseRequiredFields"), new TypeReference<>() {
        });
    }

    /**
     * get entity id by its name
     *
     * @param name       entity name like "project 234", should be global unique
     * @param entityType entity type
     * @return folder id
     */
    public String getEntityIDByName(int entityType, String name) {
        String query = buildQuery(QCConstants.PARAM_NAME, name);
        String searchResult = findEntity(entityType, query);
        return getIdInResponse(searchResult);
    }

    /**
     * get entity id in response xml
     *
     * @param entry response xml
     * @return id in string
     */
    public String getIdInResponse(String entry) {
        return getFirstEntityInResponse(entry).getEntityID();
    }

    /**
     * get first entity in response xml
     *
     * @param entry response xml
     * @return qc entity
     */
    public QCEntity getFirstEntityInResponse(String entry) {
        List<QCEntity> entities = QCEntities.getQCEntitiesFromXMLString(entry);
        if (!entities.isEmpty()) {
            return entities.get(0);
        } else {
            throw new ApollonBaseException(ApollonErrorKeys.CUSTOM_MESSAGE, "Entity in Response can not be found! ->" + entry);
        }
    }

    /**
     * Create QC Test Case into Test Plan
     *
     * @param name        test case name
     * @param parentId    parent folder id
     * @param description description of test case
     * @return QC Rest Response with comment
     */
    public QCResponseMessage createTestCase(String name, String description, String parentId) {
        QCEntity qce = QCEntityBuilder.buildNewTestCaseEntity(user, name, description, parentId, getRequiredFields(QCConstants.ENTITY_TYPE_TEST_CASE), testCaseRequiredFields);
        return new QCResponseMessage(createEntityIntoQC(qce), "Create Test Case: " + name);
    }

    /**
     * Update QC Test Case Content
     *
     * @param testCaseId  test-id
     * @param description description of test case
     * @return QC Rest Response with comment
     */
    public QCResponseMessage updateTestCase(String testCaseId, String description) {
        //get existing entity of test case by id
        QCEntity qce = getEntityByID(QCConstants.ENTITY_TYPE_TEST_CASE, testCaseId);
        qce.setAttribute(QCConstants.PARAM_DESCRIPTION, description);
        return new QCResponseMessage(updateEntityInQC(qce), "Update Test Case: " + qce.getEntityName());
    }

    /**
     * update design step to test case
     *
     * @param steps      list of all steps
     * @param testcaseId test case id
     * @return list of result of sync each step
     */
    public List<QCResponseMessage> updateDesignStepsToTestCase(List<String[]> steps, String testcaseId) {
        //get all existing steps and update
        int entityType = QCConstants.ENTITY_TYPE_DESIGN_STEP;
        LinkedList<String[]> newSteps = new LinkedList<>(steps);
        List<QCEntity> designSteps = searchQCEntity(entityType, buildQuery(QCConstants.PARAM_PARENT_ID, testcaseId));
        LinkedList<QCResponseMessage> results = new LinkedList<>();
        designSteps.stream().peek(designStep -> {
            if (designStep.getFieldValue(QCConstants.PARAM_PARENT_ID).equals(testcaseId)) {
                String[] step = new String[]{"", ""};//action, excepted
                if (!newSteps.isEmpty()) {
                    step = newSteps.removeFirst();
                }
                designStep.setAttribute(QCConstants.PARAM_DESCRIPTION, convertTextToHTML(step[0]));
                designStep.setAttribute(QCConstants.PARAM_EXPECTED, convertTextToHTML(step[1]));
            } else {
                throw new ApollonBaseException(ApollonErrorKeys.CUSTOM_MESSAGE, "Unexpected foreign Design Step to update!");
            }
        }).forEachOrdered((designStep) -> {
            Response response = updateEntityInQC(designStep);
            String pattern = "Update Step: " + designStep.getFieldValue(QCConstants.PARAM_NAME);
            results.add(new QCResponseMessage(response, pattern));
        });
        //create new steps remained in step list
        results.addAll(createNewDesignStepsToTC(newSteps, testcaseId, designSteps.size() + 1));
        return results;
    }

    /**
     * create new steps into test case
     *
     * @param steps      steps
     * @param testcaseId test-id
     * @return responses
     */
    public LinkedList<QCResponseMessage> createNewDesignStepsToTC(List<String[]> steps, String testcaseId) {
        return createNewDesignStepsToTC(steps, testcaseId, 1);
    }

    /**
     * create new steps into test case with begin order
     *
     * @param steps      steps
     * @param testcaseId test-id
     * @param orderBegin begin order, default 1
     * @return responses
     */
    public LinkedList<QCResponseMessage> createNewDesignStepsToTC(List<String[]> steps, String testcaseId, int orderBegin) {
        AtomicInteger stepOrder = new AtomicInteger(orderBegin);
        LinkedList<QCResponseMessage> results = new LinkedList<>();
        steps.stream().map(entry -> QCEntityBuilder.buildDesignStep(convertTextToHTML(entry[0]), convertTextToHTML(entry[1]), testcaseId, stepOrder.get()))
                .map(this::createEntityIntoQC).forEachOrdered((resp) -> results.add(new QCResponseMessage(resp, "Create Step: " + stepOrder.getAndIncrement())));
        return results;
    }

    /**
     * check if entity exists
     *
     * @param entityType type
     * @param name       name
     * @param parentId   pid
     * @return true if found
     */
    public boolean existsEntity(int entityType, String name, String parentId) {
        String id = getEntityIDByName(QCConstants.getContainerName(entityType), name, parentId);
        return !id.isEmpty();
    }

    /**
     * get entity with name and parent id
     *
     * @param name name of entity
     * @return id if only one found else not found with "" and more found with "0"
     */
    public String getEntityIDByName(String container, String name, String parentId) {
        List<QCEntity> results;
        if (isValid(parentId)) {
            results = searchQCEntity(container, buildQuery(ImmutableMap.of(QCConstants.PARAM_NAME, name, QCConstants.PARAM_PARENT_ID, parentId)));
        } else {
            results = searchQCEntity(container, buildQuery(QCConstants.PARAM_NAME, name));
        }
        if (results.isEmpty()) {
            return "";
        } else if (results.size() > 1) {
            return "0";
        } else {
            return results.get(0).getEntityID();
        }
    }

    /**
     * create test plan folder
     *
     * @param name     name
     * @param parentId pdi
     * @return response
     */
    public QCResponseMessage createTestPlanFolder(String name, String parentId) {
        String requiredFields = getRequiredFields(QCConstants.ENTITY_TYPE_TESTPLAN_FOLDER);
        QCEntity qcTCFolder = QCEntityBuilder.buildNewTestPlanFolderEntity(name, parentId, requiredFields);
        Response resp = createEntityIntoQC(qcTCFolder);
        String pattern = "Create new Test Plan Folder: " + name;
        return new QCResponseMessage(resp, pattern);
    }

    /**
     * create test lab folder
     *
     * @param name     name
     * @param parentId pdi
     * @return response
     */
    public QCResponseMessage createTestLabFolder(String name, String parentId) {
        String requiredFields = getRequiredFields(QCConstants.ENTITY_TYPE_TESTLAB_FOLDER);
        QCEntity qcTCFolder = QCEntityBuilder.buildNewTestLabFolderEntity(name, parentId, requiredFields);
        Response resp = createEntityIntoQC(qcTCFolder);
        String pattern = "Create new Test Lab Folder: " + name;
        return new QCResponseMessage(resp, pattern);
    }

    /**
     * create test set
     *
     * @param name     name
     * @param parentId pdi
     * @return response
     */
    public QCResponseMessage createTestSet(String name, String parentId) {
        String requiredFields = getRequiredFields(QCConstants.ENTITY_TYPE_TESTSET);
        QCEntity qce = QCEntityBuilder.buildNewTestSetEntity(name, parentId, requiredFields);
        Response resp = createEntityIntoQC(qce);
        String pattern = "Create new TestSet: " + name;
        return new QCResponseMessage(resp, pattern);
    }

    /**
     * add new Instance to test set in lab
     *
     * @param testCaseId test case entity id in test plan
     * @param testSetId  test set id in lab
     * @return response message
     */
    public QCResponseMessage addNewInstanceToSet(String testCaseId, String testSetId) {
        int entityType = QCConstants.ENTITY_TYPE_INSTANCE;
        QCEntity qce = QCEntityBuilder.buildNewInstanceEntity(testSetId, testCaseId, user, getRequiredFields(entityType));
        Response resp = createEntityIntoQC(qce);
        String pattern = "Create new Instance id: " + testCaseId + " into Test Set id: " + testSetId + "Status:";
        return new QCResponseMessage(resp, pattern);
    }

    /**
     * cover test case with requirement
     *
     * @param testId test case id
     * @param reqId  requirement id
     * @return QCResponseMessage
     */
    public QCResponseMessage coverTestCaseWithRequirement(String testId, String reqId) {
        int entityType = QCConstants.ENTITY_TYPE_COVERAGE;
        QCEntity qce = QCEntityBuilder.buildNewRequirementEntity(reqId, testId, getRequiredFields(entityType));
        Response resp = createEntityIntoQC(qce);
        String pattern = "Create REQ Coverage to Test Case";
        return new QCResponseMessage(resp, pattern);
    }

    /**
     * @param qce QCEntity
     * @return null if entity is not folder entity, empty list if no child.
     */
    public List<QCEntity> getFolderEntityChildern(QCEntity qce) {
        List<QCEntity> childern = new ArrayList<>();
        int tcFolder = QCConstants.ENTITY_TYPE_TESTPLAN_FOLDER;
        int tsFolder = QCConstants.ENTITY_TYPE_TESTLAB_FOLDER;
        int entityType = qce.getEntityType();
        if (entityType == tcFolder || entityType == tsFolder) {
            String container = QCConstants.getContainerName(entityType);
            String pid = qce.getEntityID();
            String query = buildQuery(QCConstants.PARAM_PARENT_ID, pid);
            String searchResults = findEntity(container, query);
            if (QCEntities.getTotalResults(searchResults) > 0) {
                childern = getEntitiesFromSearchResults(searchResults);
            }
            if (entityType == tsFolder) {
                searchResults = findEntity(QCConstants.TESTLAB_TESTSET_CONTAINER, query);
                childern.addAll(getEntitiesFromSearchResults(searchResults));
            }
        }
        return childern;
    }

    /**
     * get all test cases from folder per container type
     *
     * @param parentFolder can be tested case folder or test set
     * @return list of test cases
     */
    public List<QCEntity> getTestCases(QCEntity parentFolder) {
        List<QCEntity> childern = new ArrayList<>();
        int tcFolder = QCConstants.ENTITY_TYPE_TESTPLAN_FOLDER;
        int tsFolder = QCConstants.ENTITY_TYPE_TESTLAB_FOLDER;
        int tsSets = QCConstants.ENTITY_TYPE_TESTSET;
        int entityType = parentFolder.getEntityType();
        String pid = parentFolder.getEntityID();
        if (entityType != tsFolder) {
            if (entityType == tcFolder) {
                String container = QCConstants.TEST_CASE_CONTAINER;
                String query = buildQuery(QCConstants.PARAM_PARENT_ID, pid);
                childern.addAll(searchQCEntity(container, query));
            } else if (entityType == tsSets) {
                String container = QCConstants.TESTLAB_INSTANCE_CONTAINER;
                String query = buildQuery("cycle-id", pid);
                childern.addAll(searchQCEntity(container, query));
                getTCNameOfInstance(childern);
            }
        }
        return childern;
    }

    /**
     * get all related test case name of instances correspondingly
     *
     * @param instances instances in test set
     */
    public void getTCNameOfInstance(List<QCEntity> instances) {
        instances.forEach(instance -> {
            String testid = instance.getFieldValue("test-id");
            String container = QCConstants.TEST_CASE_CONTAINER;
            String query = buildQuery("id", testid);
            String searchResults = findEntity(container, query);
            if (QCEntities.getTotalResults(searchResults) > 0) {
                String name = getEntitiesFromSearchResults(searchResults).get(0).getEntityName();
                instance.setAttribute("name", name);
            }
        });
    }

    /**
     * get test case root folder entity
     *
     * @return qc entity
     */
    public QCEntity getTCFolderRootEntity() {
        String[] folders = testLabRootFolder.split("/");
        QCEntity folder = null;
        String parentid = "";
        int entityType = QCConstants.ENTITY_TYPE_TESTPLAN_FOLDER;
        //loop on path
        for (String folderName : folders) {
            if (folderName.equals("Subject")) {
                parentid = getEntityIDByName(entityType, folderName);
            } else {
                String entry = findEntity(entityType, buildQuery(ImmutableMap.of(QCConstants.PARAM_NAME, folderName, QCConstants.PARAM_PARENT_ID, parentid)));
                folder = getFirstEntityInResponse(entry);
                parentid = folder.getEntityID();
            }
        }
        return folder;
    }

    /**
     * get test lab root folder entity
     *
     * @return qc entity
     */
    public QCEntity getTLFolderRootEntity() {
        String[] folders = testLabRootFolder.split("/");
        QCEntity folder = null;
        String parentid = "";
        int entityType = QCConstants.ENTITY_TYPE_TESTLAB_FOLDER;
        for (String foldername : folders) {
            if (foldername.equals("Root")) {
                parentid = getEntityIDByName(entityType, foldername);
            } else {
                String entry = findEntity(entityType, buildQuery(ImmutableMap.of(QCConstants.PARAM_NAME, foldername, QCConstants.PARAM_PARENT_ID, parentid)));
                folder = getFirstEntityInResponse(entry);
                parentid = folder.getEntityID();
            }
        }
        return folder;
    }

    /**
     * used ony for update test plan tree on GUI
     *
     * @param root root of tree
     * @param node tree node
     */
    public void createChild(QCEntityNode root, DefaultMutableTreeNode node) {
        List<QCEntity> leafChildern = root.getLeafChildern();
        List<QCEntity> nodeChildern = root.getNodeChildern();
        leafChildern.forEach(qce -> node.add(new DefaultMutableTreeNode(new QCEntityNode(qce, null, null))));
        if (leafChildern.isEmpty() && nodeChildern.isEmpty()) {
            node.add(new DefaultMutableTreeNode("empty"));
        } else {
            nodeChildern.forEach(qce -> {
                QCEntityNode qn = new QCEntityNode(qce, getFolderEntityChildern(qce), getTestCases(qce));
                DefaultMutableTreeNode child = new DefaultMutableTreeNode(qn);
                node.add(child);
                createChild(qn, child);
            });
        }
    }

    /**
     * Search QC Entity per type via query
     *
     * @param entityType type of entity
     * @param query      QC Query
     * @return list of all found entities
     */
    public List<QCEntity> searchQCEntity(int entityType, String query) {
        String searchResults = findEntity(entityType, query);
        return getEntitiesFromSearchResults(searchResults);
    }

    /**
     * Search QC Entity in the container via query
     *
     * @param container container of entity
     * @param query     QC Query
     * @return list of all found entities
     */
    public List<QCEntity> searchQCEntity(String container, String query) {
        String searchResults = findEntity(container, query);
        return getEntitiesFromSearchResults(searchResults);
    }

    /**
     * used for GUI only
     * build a tree structure of QC Test Plan
     *
     * @param folderRootEntity QC Entity of Test Plan Root Folder
     * @param qcRootTreeNode   Node Container
     */
    public void buildQCTree(QCEntity folderRootEntity, DefaultMutableTreeNode qcRootTreeNode) {
        QCEntityNode treeRoot = new QCEntityNode(folderRootEntity, getFolderEntityChildern(folderRootEntity), getTestCases(folderRootEntity));
        qcRootTreeNode.setUserObject(treeRoot);
        createChild(treeRoot, qcRootTreeNode);
    }

    /**
     * get entity full schema via entity type
     *
     * @param entityType type of qc entity {@link QCConstants}
     * @return schema
     */
    public String getEntitySchema(int entityType) {
        return qcConnector.getFieldsOfQCEntity(domain, project, entityType, false).readEntity(String.class);
    }

    public void close() {
        qcConnector.close();
    }

    /**
     * add new Run to Test Instance in Test Set of Test Lab
     *
     * @param testInstanceID testInstanceID
     * @param tcResult       test case run result
     * @return response of action
     */
    public List<QCResponseMessage> addNewRunToInstance(String testInstanceID, TestRunResult tcResult) {
        LinkedList<QCResponseMessage> results = new LinkedList<>();
        String required = getRequiredFields(QCConstants.ENTITY_TYPE_RUN);
        QCEntity qcInsEntity = getEntityByID(QCConstants.ENTITY_TYPE_INSTANCE, testInstanceID);
        //createEntityIntoQC Run
        QCEntity qcRun = QCEntityBuilder.buildNewRunEntity(qcInsEntity, required, user, tcResult.getName());
        Response resp = createEntityIntoQC(qcRun);
        QCResponseMessage qcrm = new QCResponseMessage(resp, "Add new Run to Instance: " + qcInsEntity.getEntityName());
        results.add(qcrm);
        //hold runId
        String runId = qcrm.getEntityId();
        //update run content, status and attachment
        results.addAll(updateRunContent(runId, tcResult));
        return results;
    }

    /**
     * update run result back to qc
     *
     * @param runId     run id
     * @param tcrResult list of step result
     * @return list of update message
     */
    public List<QCResponseMessage> updateRunContent(String runId, TestRunResult tcrResult) {
        LinkedList<QCResponseMessage> results = new LinkedList<>();
        String runStaus = tcrResult.getStatus().text();
        List<QCEntity> runSteps = getRunSteps(runId);
        //prepare and update Run-steps
        for (QCEntity runStep : runSteps) {
            List<TestStepResult> stepResults = tcrResult.getStepResults();
            if (stepResults.size() != runSteps.size()) {
                throw new ApollonBaseException(ApollonErrorKeys.CUSTOM_MESSAGE, "Run Steps count are not equals step results count!");
            }
            int stepOrder = Integer.parseInt(runStep.getFieldValue(QCConstants.PARAM_STEP_ORDER));
            TestStepResult stepResult = stepResults.get(stepOrder - 1);
            String stepId = runStep.getEntityID();
            String stepStatus = stepResult.getStatus().text();
            String actual = stepResult.getInfo();
            List<Screenshot> evidence = stepResult.getScreenshots();
            runStep.setAttribute(QCConstants.PARAM_STATUS, stepStatus);
            runStep.setAttribute(QCConstants.PARAM_ACTUAL, convertTextToHTML(actual));
            Response resp = updateEntityInQC(runStep);
            String pattern = "Update Run-Step: " + runStep.getEntityName() + " with status: " + stepStatus;
            results.add(new QCResponseMessage(resp, pattern));
            for (Screenshot screenshot : evidence) {
                resp = appendAttachment(QCConstants.ENTITY_TYPE_RUN_STEP, stepId, screenshot.getScreenshotFile());
                results.add(new QCResponseMessage(resp, "Upload screenshot"));
                if (screenshot.hasPageFile()) {
                    resp = appendAttachment(QCConstants.ENTITY_TYPE_RUN_STEP, stepId, screenshot.getPageFile());
                    results.add(new QCResponseMessage(resp, "Upload page source"));
                }
            }
        }
        //update Test Instance
        results.add(updateRunStatus(runId, runStaus));
        //upload log file
        Response resp = appendAttachment(QCConstants.ENTITY_TYPE_RUN, runId, new File(tcrResult.getLogFilePath()));
        results.add(new QCResponseMessage(resp, "Upload log file"));
        return results;
    }

    /**
     * update test instance status
     *
     * @param runId  run id
     * @param status status of test
     * @return response of rest client
     */
    public QCResponseMessage updateRunStatus(String runId, String status) {
        Response resp = updateEntityInQC(QCConstants.ENTITY_TYPE_RUN, runId, QCEntityBuilder.buildSingleFieldPayload(QCConstants.PARAM_STATUS, status));
        String pattern = "Update Run of Instance with status: " + status;
        return new QCResponseMessage(resp, pattern);
    }

    /**
     * get list of Design Steps of a test case
     *
     * @param testId test case id
     * @return list of qc entities
     */
    public List<QCEntity> getDesignSteps(String testId) {
        int entityType = QCConstants.ENTITY_TYPE_DESIGN_STEP;
        String query = buildQuery(QCConstants.PARAM_PARENT_ID, testId);
        String searchResults = findEntity(entityType, query);
        return getEntitiesFromSearchResults(searchResults);
    }

    /**
     * get list of run steps of a test case instance
     *
     * @param runId test run instance id
     * @return list of qc entities
     */
    public List<QCEntity> getRunSteps(String runId) {
        int entityType = QCConstants.ENTITY_TYPE_RUN_STEP;
        String query = buildQuery(QCConstants.PARAM_PARENT_ID, runId);
        String searchResults = findEntity(entityType, query);
        return getEntitiesFromSearchResults(searchResults);
    }

    /**
     * add run steps to instance in lab
     *
     * @param runId        instance id
     * @param testId       test case id
     * @param designStepId design step id
     * @param status       run status
     * @return request response
     */
    public Response addRunStepsToInstance(String runId, String testId, String designStepId, String status) {
        int entityType = QCConstants.ENTITY_TYPE_RUN_STEP;
        String required = getRequiredFields(entityType);
        QCEntity qce = QCEntityBuilder.buildNewInstanceStep(designStepId, testId, runId, status, required);
        //createEntityIntoQC
        return createEntityIntoQC(qce);
    }

    /**
     * get required fields of qc entity
     *
     * @param entityType entity type
     * @return xml content of fields definition
     */
    public String getRequiredFields(int entityType) {
        return qcConnector.getFieldsOfQCEntity(domain, project, entityType, true).readEntity(String.class);
    }

    /**
     * get qc entity by id
     *
     * @param entityType entity type
     * @param id         entity id in qc
     * @return qc entity
     */
    public QCEntity getEntityByID(int entityType, String id) {
        String container = QCConstants.getContainerName(entityType);
        String result = qcConnector.getEntityFromQCContainerByID(domain, project, container, id).readEntity(String.class);
        return getFirstEntityInResponse(result);
    }

    /**
     * get qc entity by id
     *
     * @param entityType entity type
     * @param id         entity id in qc
     * @return qc entity
     */
    public QCEntity getEntityByID(int entityType, int id) {
        return getEntityByID(entityType, String.valueOf(id));
    }

    /**
     * delete entity in qc
     *
     * @param entity qc entity to be deleted
     * @return response
     */
    public Response deleteQCEntityInQC(QCEntity entity) {
        return qcConnector.delete(domain, project, entity.getEntityType(), entity.getEntityID());
    }

    /**
     * attach file to test case in qc
     *
     * @param storyFile file to attach
     * @param tcid      test case id
     * @return response message
     */
    public QCResponseMessage attachFileToTestCase(String storyFile, String tcid) {
        Response resp = appendAttachment(QCConstants.ENTITY_TYPE_TEST_CASE, tcid, FileOperation.getFile(storyFile).toFile());
        return new QCResponseMessage(resp, "Attach Story File: " + storyFile);
    }

    /**
     * get attachment of entity in qc
     *
     * @param entityType entity type
     * @param parentId   entity id
     * @param fileName   attach file name in qc
     * @return steam of attachment
     */
    public InputStream getAttachmentContent(int entityType, String parentId, String fileName) {
        return findAttachment(entityType, parentId, fileName).readEntity(InputStream.class);
    }

    public Response findAttachment(int entityType, String parentId, String fileName) {
        return qcConnector.getAttachmentsFromQCEntity(domain, project, parentId, entityType, fileName);
    }

    public List<QCEntity> getInstancesIdByTestCaseId(String testCaseId, String testSetId) {
        return searchQCEntity(QCConstants.ENTITY_TYPE_INSTANCE, buildQuery(ImmutableMap.of(QCConstants.PARAM_TEST_CASE_ID, testCaseId, QCConstants.PARAM_INSTANCE_ID_IN_TEST_CASE, testSetId)));
    }

    /**
     * Fetch all QC Entities into a List
     *
     * @param results Response Entry of search result
     * @return List of Entites
     */
    public List<QCEntity> getEntitiesFromSearchResults(String results) {
        return QCEntities.getQCEntitiesFromXMLString(results);
    }

    public List<QCEntity> getTestRunSteps(String runId) {
        return searchQCEntity(QCConstants.TESTLAB_RUN_STEPS_CONTAINER, buildQuery(QCConstants.PARAM_PARENT_ID, runId));
    }

    public QCEntity getLastRunsOfTestCase(String testCaseId) {
        List<QCEntity> runs = searchQCEntity(QCConstants.TESTLAB_RUN_CONTAINER, buildQuery(QCConstants.PARAM_TEST_CASE_ID, testCaseId)).stream().sorted().toList();
        if (!runs.isEmpty()) {
            return runs.get(runs.size() - 1);
        } else {
            throw new ApollonBaseException(ApollonErrorKeys.CUSTOM_MESSAGE, "No Runs found with Test Case ID:" + testCaseId);
        }
    }

    public QCEntity getLastRunsOfInstance(String instanceId) {
        List<QCEntity> runs = searchQCEntity(QCConstants.TESTLAB_RUN_CONTAINER, buildQuery(QCConstants.PARAM_INSTANCE_ID_IN_RUN, instanceId)).stream().sorted().toList();
        if (!runs.isEmpty()) {
            return runs.get(runs.size() - 1);
        } else {
            throw new ApollonBaseException(ApollonErrorKeys.CUSTOM_MESSAGE, "No Runs found with Test Case ID:" + instanceId);
        }
    }

    public QCEntity getLastTestRunStepOfDesignStep(String designStepId) {
        List<QCEntity> runs = searchQCEntity(QCConstants.TESTLAB_RUN_STEPS_CONTAINER, buildQuery(QCConstants.PARAM_DESIGNSTEP_ID_IN_RUNSTEP, designStepId)).stream().sorted().toList();
        if (!runs.isEmpty()) {
            return runs.get(runs.size() - 1);
        } else {
            throw new ApollonBaseException(ApollonErrorKeys.CUSTOM_MESSAGE, "No Run Step found with Design Step ID:" + designStepId);
        }
    }

    /**
     * psot entity to qc
     *
     * @param qcItem qc entity
     * @return response
     */
    public Response createEntityIntoQC(QCEntity qcItem) {
        return qcConnector.createQCEntity(domain, project, qcItem.getEntityType(), qcItem.getXMLContent());
    }

    /**
     * get QC Entity with query, function like search
     *
     * @param entityType entity type like test case, test case folder...
     * @param query      qc query token
     * @return Response Entry in String
     */
    public String findEntity(int entityType, String query) {
        String container = QCConstants.getContainerName(entityType);
        return findEntity(container, query);
    }

    /**
     * get QC Entity with query, function like search
     *
     * @param container entity container, like test case, test case folder...
     * @param query     qc query token
     * @return Response Entry in String
     */
    public String findEntity(String container, String query) {
        return qcConnector.getEntityFromQCContainer(domain, project, container, query).readEntity(String.class);
    }

    /**
     * updateQCEntity entity to qc
     *
     * @param entity qc entity
     * @return response
     */
    public Response updateEntityInQC(QCEntity entity) {
        if (vcEnabled) {
            return qcConnector.updateQCEntityWithVersion(domain, project, entity);
        } else {
            return qcConnector.updateQCEntity(domain, project, entity);
        }
    }

    public Response updateEntityInQC(int entityTyp, String entityId, String payload) {
        if (vcEnabled) {
            return qcConnector.updateQCEntityWithVersion(domain, project, entityTyp, entityId, payload);
        } else {
            return qcConnector.updateQCEntity(domain, project, QCConstants.getContainerName(entityTyp), entityId, payload);
        }
    }

    /**
     * post attachment to qc entity
     *
     * @param entityType entity type
     * @param entityId   entity id
     * @param file       file
     * @return response
     */
    public Response appendAttachment(int entityType, String entityId, File file) {
        byte[] content = FileOperation.readFileToByteArray(file);
        info("Try to Upload Attachment: " + file.getName());
        return qcConnector.appendAttachmentsToQCEntity(domain, project, entityId, entityType, file.getName(), content);
    }

    public String buildQuery(String attribute, String value) {
        return "{" + buildQueryPart(attribute, value) + "}";
    }

    public String buildQuery(Map<String, String> parameters) {
        if (parameters.isEmpty()) return "";
        StringBuilder query = new StringBuilder();
        parameters.forEach((key, value) -> query.append(buildQueryPart(key, value)).append(";"));
        String content = query.toString();
        return "{" + StringUtils.chop(content) + "}";
    }

    private String buildQueryPart(String key, String content) {
        if (isValid(content)) {
            if (key.contains(QCConstants.PARAM_NAME)) {
                return key + "[" + content.replaceAll("[^a-zA-Z0-9=_-]", "*") + "]";
            } else {
                return key + "[" + content + "]";
            }
        } else {
            throw new ApollonBaseException(ApollonErrorKeys.CUSTOM_MESSAGE, "Query Parameter can not be empty!");
        }
    }

    /**
     * Feedback Test Results to test case
     *
     * @param testCaseObjects test case object
     */
    public void syncTestCasesAndRunResults(List<TestCaseObject> testCaseObjects) {
        testCaseObjects.forEach(testCaseObject -> {
            String tcName = testCaseObject.getOriginalName();
            String description = testCaseObject.getDescription();
            String testCasePackage = testCaseObject.getPackageName().replace(PropertyResolver.getTestCaseLocation(), "");
            String folderID = getTestPlanFolderID();
            if (isValid(testCasePackage) && !currentTestCasePackage.equals(testCasePackage)) {
                currentTestCasePackage = testCasePackage;
                debug("Check testCasePackage: " + currentTestCasePackage);
                folderID = makeTestPlanDirs(currentTestCasePackage, getTestPlanFolderID());
            }
            String tcID;
            List<QCResponseMessage> messages = new LinkedList<>();
            if (!existsEntity(QCConstants.ENTITY_TYPE_TEST_CASE, tcName, folderID)) {//create test case
                tcID = createTestCase(tcName, description, folderID).getEntityId();
                messages.addAll(createNewDesignStepsToTC(testCaseObject.getSteps().stream()
                        .map(TestCaseStep::toDesignStep).collect(Collectors.toList()), tcID));
                messages.add(attachFileToTestCase(testCaseObject.getFilePath(), tcID));
            } else {//update test case
                tcID = getEntityIDByName(QCConstants.TEST_CASE_CONTAINER, tcName, folderID);
                String fileName = FileOperation.getFileName(testCaseObject.getFilePath());
                String jsonString = JSONContainerFactory.getJSONFileContent(getAttachmentContent(QCConstants.ENTITY_TYPE_TEST_CASE, tcID, fileName));
                JSONTestCase jsonTestCase = JSONContainerFactory.buildJSONObject(jsonString, JSONTestCase.class);
                if (!testCaseObject.getTestCase().equals(jsonTestCase)) {
                    messages.add(updateTestCase(tcID, description));
                    messages.addAll(updateDesignStepsToTestCase(testCaseObject.getSteps().stream().map(TestCaseStep::toDesignStep).collect(Collectors.toList()), tcID));
                    messages.add(attachFileToTestCase(testCaseObject.getFilePath(), tcID));
                }
            }
            //build testset name
            String currentTestSetName = testSetName;
            if (isValid(currentTestCasePackage)) {
                currentTestSetName = testSetName + "_" + currentTestCasePackage.replace("/", "_");
            }
            debug("Check testSetName: " + currentTestSetName);
            //create test set
            String testSetID;
            if (!existsEntity(QCConstants.ENTITY_TYPE_TESTSET, currentTestSetName, getTestLabFolderID())) {
                testSetID = createTestSet(currentTestSetName, getTestLabFolderID()).getEntityId();
            } else {
                testSetID = getEntityIDByName(QCConstants.TESTLAB_TESTSET_CONTAINER, currentTestSetName, getTestLabFolderID());
            }
            String testInstanceID;
            //add instance to test set
            if (getInstancesIdByTestCaseId(tcID, testSetID).isEmpty()) {
                testInstanceID = addNewInstanceToSet(tcID, testSetID).getEntityId();
            } else {
                testInstanceID = getInstancesIdByTestCaseId(tcID, testSetID).get(0).getEntityID();
            }
            //output messages
            messages.forEach(message -> info(message.getMessage()));
            //add new run: test variant will be added as a run to original test instance
            addNewRunToInstance(testInstanceID, testCaseObject.getTestRunResult()).forEach(message -> info(message.getMessage()));
        });
    }

    public String makeTestPlanDirs(String folderPath, String parentId) {
        String finalFolderId;
        String[] pathToken = folderPath.replace("\\", "/").split("/");
        if (pathToken.length == 0) {
            finalFolderId = makeDir(QCConstants.ENTITY_TYPE_TESTPLAN_FOLDER, folderPath, parentId);
        } else {
            int index = 0;
            if (pathToken[0].equals(testPlanRootFolder)) {
                index = 1;
            }
            String lastFolderId = parentId;
            for (int i = index; i < pathToken.length; i++) {
                lastFolderId = makeDir(QCConstants.ENTITY_TYPE_TESTPLAN_FOLDER, pathToken[i], lastFolderId);
            }
            finalFolderId = lastFolderId;
        }
        return finalFolderId;
    }

    public String makeTestLabDirs(String folderPath, String parentId) {
        String finalFolderId;
        String[] pathToken = folderPath.replace("\\", "/").split("/");
        if (pathToken.length == 0) {
            finalFolderId = makeDir(QCConstants.ENTITY_TYPE_TESTLAB_FOLDER, folderPath, parentId);
        } else {
            int index = 0;
            if (pathToken[0].equals(testLabRootFolder)) {
                index = 1;
            }
            String lastFolderId = parentId;
            for (int i = index; i < pathToken.length; i++) {
                lastFolderId = makeDir(QCConstants.ENTITY_TYPE_TESTLAB_FOLDER, pathToken[i], lastFolderId);
            }
            finalFolderId = lastFolderId;
        }
        return finalFolderId;
    }

    public String getTestPlanRootFolderID() {
        if (testPlanRootFolderID.isEmpty()) {
            testPlanRootFolderID = getEntityIDByName(QCConstants.ENTITY_TYPE_TESTPLAN_FOLDER, testPlanRootFolder);
        }
        info("testPlanRootFolderID: " + testPlanRootFolderID);
        return testPlanRootFolderID;
    }

    public String getTestLabRootFolderID() {
        if (testLabRootFolder.equals("Root")) {
            testLabRootFolderID = "0";
        }
        if (testLabRootFolderID.isEmpty()) {
            testLabRootFolderID = getEntityIDByName(QCConstants.ENTITY_TYPE_TESTLAB_FOLDER, testLabRootFolder);
        }
        info("testLabRootFolderID: " + testLabRootFolderID);
        return testLabRootFolderID;
    }

    public String getTestPlanFolderID() {
        if (testPlanFolderID.isEmpty() && isValidFolderPath(testPlanFolderPath, "Subject")) {
            testPlanFolderID = makeTestPlanDirs(testPlanFolderPath, getTestPlanRootFolderID());
        }
        return testPlanFolderID;
    }

    public String getTestLabFolderID() {
        if (testLabFolderID.isEmpty() && isValidFolderPath(testLabFolderPath, "Root")) {
            testLabFolderID = makeTestLabDirs(testLabFolderPath, getTestLabRootFolderID());
        }
        return testLabFolderID;
    }

    private boolean isValidFolderPath(String path, String keyword) {
        if (path.startsWith(keyword)) {
            path = path.substring(keyword.length());
        }
        if (path.contains(keyword)) {
            throw new ApollonBaseException(ApollonErrorKeys.CUSTOM_MESSAGE, "Test Plan Folder Path should not contain 'Subject' as sub folder!");
        } else {
            return true;
        }
    }

    private String makeDir(int entityTyp, String folderName, String parentId) {
        String folderId;
        if (folderName.equals(testPlanRootFolder)) {
            folderId = getTestPlanRootFolderID();
        } else if (folderName.equals(testLabRootFolder)) {
            folderId = getTestLabRootFolderID();
        } else {
            if (existsEntity(entityTyp, folderName, parentId)) {
                folderId = getEntityIDByName(QCConstants.getContainerName(entityTyp), folderName, parentId);
            } else {
                if (entityTyp == QCConstants.ENTITY_TYPE_TESTPLAN_FOLDER) {
                    folderId = createTestPlanFolder(folderName, parentId).getEntityId();
                } else {
                    folderId = createTestLabFolder(folderName, parentId).getEntityId();
                }
            }
        }
        return folderId;
    }

    public String convertTextToHTML(String text) {
        String html = "<html><body>{0}</body></html>";
        String paragraph = "<p>{0}</p>";
        if (!text.contains(System.lineSeparator())) {
            return text;
        } else {
            String lines = Arrays.stream(text.split(System.lineSeparator()))
                    .map(line -> MessageFormat.format(paragraph, line))
                    .collect(Collectors.joining());
            return MessageFormat.format(html, lines);
        }
    }
}