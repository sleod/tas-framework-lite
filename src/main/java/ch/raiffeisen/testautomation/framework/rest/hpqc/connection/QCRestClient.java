package ch.raiffeisen.testautomation.framework.rest.hpqc.connection;

import ch.raiffeisen.testautomation.framework.common.IOUtils.FileOperation;
import ch.raiffeisen.testautomation.framework.common.logging.Screenshot;
import ch.raiffeisen.testautomation.framework.common.logging.SystemLogger;
import ch.raiffeisen.testautomation.framework.core.component.TestRunResult;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.*;

import static ch.raiffeisen.testautomation.framework.common.logging.SystemLogger.log;

public class QCRestClient {

    private final QCConnector qcConnector;
    private final String domain;
    private final String project;
    private final String tcRootFolder; // need full path, like root/project/test
    private final String tlRootFolder; // need full path, like root/project/test
    private final String vcEnabled;
    private final String user;
    private int stepOrder = 0;

    /**
     * init QC Rest Client with input of qc settings of project
     *
     * @param qcConnector  is QC Connector provider which generate Rest Driver
     * @param qcDomain     is QC domain
     * @param qcProject    is QC project name
     * @param tcRootFolder is Start Folder of the project in test plan
     * @param tlRootFolder is Start Folder of the project in lab
     * @param vcEnabled    version control flag
     */
    public QCRestClient(QCConnector qcConnector, String qcDomain, String qcProject, String tcRootFolder, String tlRootFolder, String vcEnabled) {
        this.qcConnector = qcConnector;
        user = qcConnector.getUser();
        domain = qcDomain;
        project = qcProject;
        this.tcRootFolder = tcRootFolder;
        this.tlRootFolder = tlRootFolder;
        this.vcEnabled = vcEnabled;
    }

    /**
     * get entity id by its name
     *
     * @param folderName like "project 234", should be global unique
     * @param entityType entity type
     * @return folder id
     */
    public String getEntityIDByName(int entityType, String folderName) {
        String query = buildQuery("name", folderName);
        String searchResult = getEntity(entityType, query);
        return getIdInResponse(searchResult);
    }

    /**
     * get current response entry
     *
     * @return string of response entry
     */
    public String getCurrntResponseEntry() {
        return qcConnector.getResponse().readEntity(String.class);
    }

    /**
     * get entity id in response xml
     *
     * @param entry response xml
     * @return id in string
     */
    public String getIdInResponse(String entry) {
        return getFirstEntityInResponse(entry).getQcEntityID();
    }

    /**
     * get first entity in response xml
     *
     * @param entry response xml
     * @return qc entity
     */
    public QCEntity getFirstEntityInResponse(String entry) {
        return QCEntities.getQCEntitiesFromXMLString(entry).get(0);
    }

    /**
     * get type of entity in xml content
     *
     * @param entry xml content
     * @return entity type in int
     */
    public int getTypeInResponse(String entry) {
        return getFirstEntityInResponse(entry).getEntityType();
    }


    /**
     * create new folder in test plan.
     *
     * @param dirName  "some folder"
     * @param parentId "12345"
     * @return response message
     */
    public QCResponseMessage addNewTestPlanFolder(String dirName, String parentId) {
        //ensure existence
        String query = "{" + buildQueryPart("name", dirName) + ";" + buildQueryPart("parent-id", parentId) + "}";
        int entityType = QCConstants.ENTITY_TYPE_TEST_FOLDER;
        QCEntityExistance entityExistence = getEntityExistence(entityType, query);
        if (!entityExistence.isExist()) {
            String required = getRequiredFields(entityType);
            QCEntity qcTCFolder = QCEntityBuilder.buildNewTestFolderEntity(dirName, parentId, required);
            //createEntityIntoQC
            Response resp = createEntityIntoQC(qcTCFolder);
            String pattern = "Create new Test Plan Folder: " + dirName + " -> {0}\n";
            return buildResultReport(resp, pattern);
        } else {
            String msg = "Test Plan Folder is already created, no Action!";
            return new QCResponseMessage(msg, entityExistence.getResponseEntry(), String.valueOf(entityExistence.getEntityId()));
        }
    }

    /**
     * Synchronize QC Test Case into Test Plan
     * <p>Case existed: update/override content into QC<p/>
     * <p>Case new: create new test case into plan folder</p>
     *
     * @param tcName      test case name
     * @param folderId    parent folder id
     * @param description description of test case
     * @return QC Rest Response with comment
     */
    public QCResponseMessage syncTestCase(String tcName, String description, String folderId, String application) {
        //gathering attributes
        int entityType = QCConstants.ENTITY_TYPE_TEST_CASE;
        //existence testcase
        String query = buildQuery("name", tcName);
        QCEntityExistance entityExistence = getEntityExistence(entityType, query);
        String requiredFields = getRequiredFields(entityType);
        Response response;
        if (!entityExistence.isExist()) {
            //new qc entity of test case
            QCEntity qce = QCEntityBuilder.buildNewTestCaseEntity(tcName, user, description, folderId, application, requiredFields);
            response = createEntityIntoQC(qce);
        } else {
            //get existing entity of test case by id
            QCEntity qce = getEntityByID(entityType, entityExistence.getEntityId());
            qce.setAttribute("description", description);
            qce.setAttribute("user-01", application);
            response = updateEntityInQC(qce);
        }
        return buildResultReport(response, "Create/Update Test Case: " + tcName + " -> {0}\n");
    }

    /**
     * synchronize design step to test case
     *
     * @param steps      list of steps
     * @param testcaseId test case id
     * @return list of result of sync each step
     */
    public List<String> syncDesignStepsToTestCase(LinkedList<String[]> steps, String testcaseId) {
        //get all existing steps
        int entityType = QCConstants.ENTITY_TYPE_DESIGN_STEP;
        List<QCEntity> desSteps = searchQCEntity(entityType, buildQuery("parent-id", testcaseId));
        //       if (deleteAllDesignSteps(desSteps)) {//create steps
        //           return createNewDesignStepsToTC(steps, testcaseId);
//        } else {//override Steps
        LinkedList<String> results = new LinkedList<>();
        desSteps.stream().peek((qce) -> {
            String[] step = new String[]{"", ""};//action, excepted
            if (!steps.isEmpty()) {
                step = steps.removeFirst();
            }
            qce.setAttribute("description", step[0]);
            qce.setAttribute("expected", step[1]);
        }).forEachOrdered((qce) -> {
            String pattern = "Update Step: " + qce.getFieldValue("name") + " -> {0}\n";
            String resText = buildResultReport(updateEntityInQC(qce), pattern).getMessage();
            results.add(resText);
        });
        stepOrder += desSteps.size();
        results.addAll(createNewDesignStepsToTC(steps, testcaseId));
        return results;
        //      }
    }

    /**
     * add new test set to lab
     *
     * @param tsName test set name
     * @param pid    parent id
     * @return response message
     */
    public QCResponseMessage addNewTestSet(String tsName, String pid) {
        int entityType = QCConstants.ENTITY_TYPE_TESTSET;
        String query = "{" + buildQueryPart("name", tsName) + ";" + buildQueryPart("parent-id", pid) + "}";
        QCEntityExistance entityExistence = getEntityExistence(entityType, query);
        if (!entityExistence.isExist()) {
            String requiredFields = getRequiredFields(entityType);
            QCEntity qce = QCEntityBuilder.buildNewTestSetEntity(tsName, pid, requiredFields);
            Response resp = createEntityIntoQC(qce);
            String pattern = "Create new TestSet: " + tsName + " -> {0}\n";
            return buildResultReport(resp, pattern);
        } else {
            String msg = "Test Set is alrealy created, no Action!";
            return new QCResponseMessage(msg, entityExistence.getResponseEntry(), String.valueOf(entityExistence.getEntityId()));
        }
    }

    /**
     * add new test set folder
     *
     * @param tsfName test set folder name
     * @param pid     parent id
     * @return response message
     */
    public QCResponseMessage addNewTestSetFolder(String tsfName, String pid) {
        int entityType = QCConstants.ENTITY_TYPE_TESTSETS_FOLDER;
        String query = "{" + buildQueryPart("name", tsfName) + ";" + buildQueryPart("parent-id", pid) + "}";
        QCEntityExistance entityExistence = getEntityExistence(entityType, query);
        if (!entityExistence.isExist()) {
            QCEntity qce = QCEntityBuilder.buildNewTestSetFolderEntity(tsfName, pid, getRequiredFields(entityType));
            Response resp = createEntityIntoQC(qce);
            String pattern = "Create new TestSet Folder: " + tsfName + " -> {0}\n";
            return buildResultReport(resp, pattern);
        } else {
            String msg = "Test Set Folder is alrealy created, no Action!";
            return new QCResponseMessage(msg, entityExistence.getResponseEntry(), String.valueOf(entityExistence.getEntityId()));
        }
    }

    /**
     * add new Instance to test set in lab
     *
     * @param testCaseId test case entity id in test plan
     * @param testSetId  test set id in lab
     * @return response message
     */
    public QCResponseMessage addNewInstanceToSet(String testCaseId, String testSetId) {
        String query = "{" + buildQueryPart("cycle-id", testCaseId) + ";" + buildQueryPart("test-id", testSetId) + "}";
        int entityType = QCConstants.ENTITY_TYPE_INSTANCE;
        QCEntityExistance entityExistence = getEntityExistence(entityType, query);
        if (!entityExistence.isExist()) {
            QCEntity qce = QCEntityBuilder.buildNewInstanceEntity(testSetId, testCaseId, user, getRequiredFields(entityType));
            Response resp = createEntityIntoQC(qce);
            String pattern = "Create new Instance id: " + testCaseId + " into Test Set id: "
                    + testSetId + "Status -> {0}\n";
            return buildResultReport(resp, pattern);
        } else {
            String msg = "Test Instance of this Test Case already in TestSet, no Action!";
            return new QCResponseMessage(msg, entityExistence.getResponseEntry(), String.valueOf(entityExistence.getEntityId()));
        }
    }

    /**
     * cover test case with requirement
     *
     * @param testId test case id
     * @param reqId  requirement id
     * @return response
     */
    public String coverTestCaseWithRequirement(String testId, String reqId) {
        int entityType = QCConstants.ENTITY_TYPE_COVERAGE;
        QCEntity qce = QCEntityBuilder.buildNewRequirementEntity(reqId, testId, getRequiredFields(entityType));
        Response resp = createEntityIntoQC(qce);
        String pattern = "Create REQ Coverage to Test Case " + " -> {0}\n";
        return buildResultReport(resp, pattern).getMessage();
    }

    /**
     * get test case id search by name
     *
     * @param tcName test case name
     * @return id in string
     */
    public String getTestCaseIdByName(String tcName) {
        int entityType = QCConstants.ENTITY_TYPE_TEST_CASE;
        String query = buildQuery("name", tcName);
        String xml = getEntity(entityType, query);
        return getIdInResponse(xml);
    }

    /**
     * @param qce QCEntity
     * @return null if entity is not folder entity, empty list if no child.
     */
    public List<QCEntity> getFolderEntityChildern(QCEntity qce) {
        List<QCEntity> childern = new ArrayList<>();
        int tcFolder = QCConstants.ENTITY_TYPE_TEST_FOLDER;
        int tsFolder = QCConstants.ENTITY_TYPE_TESTSETS_FOLDER;
        int entityType = qce.getEntityType();
        if (entityType == tcFolder || entityType == tsFolder) {
            String container = QCConstants.getContainerName(entityType);
            String pid = qce.getFieldValue("id");
            String query = buildQuery("parent-id", pid);
            String searchResults = getEntity(container, query);
            if (QCEntities.getTotalResults(searchResults) > 0) {
                childern = getEntitiesFromSearchResults(searchResults);
            }
            if (entityType == tsFolder) {
                searchResults = getEntity(QCConstants.TEST_LAB_TESTSET_CONTAINER, query);
                childern.addAll(getEntitiesFromSearchResults(searchResults));
            }
        }
        return childern;
    }

    /**
     * get all test cases from folder per container type
     *
     * @param parentFolder can be test case folder or test set
     * @return list of test cases
     */
    public List<QCEntity> getTeseCases(QCEntity parentFolder) {
        List<QCEntity> childern = new ArrayList<>();
        int tcFolder = QCConstants.ENTITY_TYPE_TEST_FOLDER;
        int tsFolder = QCConstants.ENTITY_TYPE_TESTSETS_FOLDER;
        int tsSets = QCConstants.ENTITY_TYPE_TESTSET;
        int entityType = parentFolder.getEntityType();
        String pid = parentFolder.getFieldValue("id");
        if (entityType != tsFolder) {
            if (entityType == tcFolder) {
                String container = QCConstants.TEST_CASE_CONTAINER;
                String query = buildQuery("parent-id", pid);
                childern.addAll(searchQCEntity(container, query));
            } else if (entityType == tsSets) {
                String container = QCConstants.TEST_LAB_INSTANCE_CONTAINER;
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
            String searchResults = getEntity(container, query);
            if (QCEntities.getTotalResults(searchResults) > 0) {
                String name = getEntitiesFromSearchResults(searchResults).get(0).getFieldValue("name");
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
        String[] folders = tcRootFolder.split("/");
        QCEntity folder = null;
        String parentid = null;
        int entityType = QCConstants.ENTITY_TYPE_TEST_FOLDER;
        //loop on path
        for (String foldername : folders) {
            if (foldername.equals("Subject")) {
                parentid = getEntityIDByName(entityType, foldername);
            } else {
                String query = "{" + buildQueryPart("name", foldername) + ";" + buildQueryPart("parent-id", parentid) + "}";
                String entry = getEntity(entityType, query);
                folder = getFirstEntityInResponse(entry);
                parentid = folder.getQcEntityID();
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
        String[] folders = tlRootFolder.split("/");
        QCEntity folder = null;
        String parentid = null;
        int entityType = QCConstants.ENTITY_TYPE_TESTSETS_FOLDER;
        for (String foldername : folders) {
            if (foldername.equals("Root")) {
                parentid = getEntityIDByName(entityType, foldername);
            } else {
                String query = "{" + buildQueryPart("name", foldername) + ";" + buildQueryPart("parent-id", parentid) + "}";
                String entry = getEntity(entityType, query);
                folder = getFirstEntityInResponse(entry);
                parentid = folder.getQcEntityID();
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
    public void createChildern(QCEntityNode root, DefaultMutableTreeNode node) {
        List<QCEntity> leafChildern = root.getLeafChildern();
        List<QCEntity> nodeChildern = root.getNodeChildern();
        leafChildern.forEach(qce -> node.add(new DefaultMutableTreeNode(new QCEntityNode(qce, null, null))));
        if (leafChildern.isEmpty() && nodeChildern.isEmpty()) {
            node.add(new DefaultMutableTreeNode("empty"));
        } else {
            nodeChildern.forEach(qce -> {
                QCEntityNode qn = new QCEntityNode(qce, getFolderEntityChildern(qce), getTeseCases(qce));
                DefaultMutableTreeNode child = new DefaultMutableTreeNode(qn);
                node.add(child);
                createChildern(qn, child);
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
        String searchResults = getEntity(entityType, query);
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
        String searchResults = getEntity(container, query);
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
        QCEntityNode treeRoot = new QCEntityNode(folderRootEntity, getFolderEntityChildern(folderRootEntity), getTeseCases(folderRootEntity));
        qcRootTreeNode.setUserObject(treeRoot);
        createChildern(treeRoot, qcRootTreeNode);
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
     * @param qcInsEntity qc Lab Test Instance entity
     * @param tcResult    test case run result
     * @return response of action
     */
    public List<String> addNewRunToInstance(QCEntity qcInsEntity, TestRunResult tcResult) {
        LinkedList<String> results = new LinkedList<>();
        int entityType = QCConstants.ENTITY_TYPE_RUN;
        String required = getRequiredFields(entityType);
        QCEntity qcRun = QCEntityBuilder.buildNewRunEntity(qcInsEntity, required, user);
        //createEntityIntoQC Run
        Response resp = createEntityIntoQC(qcRun);
        String testCaseName = qcInsEntity.getFieldValue("name");
        String pattern = "Add new Run to Instance: " + testCaseName + " -> {0}\n";
        QCResponseMessage qcrm = buildResultReport(resp, pattern);
        results.add(qcrm.getMessage());
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
    public List<String> updateRunContent(String runId, TestRunResult tcrResult) {
        LinkedList<String> results = new LinkedList<>();
        String runStaus = tcrResult.getStatus().text();
        List<QCEntity> runSteps = getRunSteps(runId);
        String testCaseName = tcrResult.getName();
        //prepair and update Runsteps
        for (QCEntity entity : runSteps) {
            String stepOrder = entity.getFieldValue("step-order");
            String stepId = entity.getQcEntityID();
            int index = Integer.parseInt(stepOrder) - 1;
            String stepStatus;
            String actual;
            List<Screenshot> avidence;
            if (index < tcrResult.getStepResults().size() && index >= 0) {
                stepStatus = tcrResult.getStepResults().get(index).getStatus().text();
                actual = tcrResult.getStepResults().get(index).getActual();
                avidence = tcrResult.getStepResults().get(index).getScreenshots();
            } else {
                stepStatus = "No Run";
                actual = "NOT PERFORMED";
                avidence = Collections.emptyList();
            }
            String name = entity.getFieldValue("name");
            String upToDate = QCEntityBuilder.buildUpdateContentOfRunStep(stepStatus, actual);
            Response resp = updateEntityInQC(entity, upToDate);
            String pattern = "Add new Run-Step: " + name + " with status: " + stepStatus + " -> {0}\n";
            for (Screenshot screenshot : avidence) {
                appendAttachment(QCConstants.ENTITY_TYPE_RUN_STEP, stepId, screenshot.getScreenshotFile());
                if (screenshot.hasPageFile()) {
                    appendAttachment(QCConstants.ENTITY_TYPE_RUN_STEP, stepId, screenshot.getPageFile());
                }
            }
            String resText = buildResultReport(resp, pattern).getMessage();
            results.add(resText);
        }
        //update Test Instance
        int entityType = QCConstants.ENTITY_TYPE_RUN;
        String updateMsg = updateTestInstanceStatus(entityType, runId, runStaus).getMessage();
        //upload report
        appendAttachment(entityType, runId, new File(tcrResult.getLogFilePath()));
        results.add(updateMsg);
        return results;
    }

    /**
     * update test instance status
     *
     * @param entityType entity type
     * @param id         parent id
     * @param status     status of test
     * @return response of rest client
     */
    public QCResponseMessage updateTestInstanceStatus(int entityType, String id, String status) {
        String upToDate = QCEntityBuilder.buildUpdateContentForTestInstanceStatus(status);
        Response resp = updateEntityInQC(getEntityByID(entityType, id), upToDate);
        String pattern = "Update Run of Instance with status: " + status + " ->  {0}\n";
        return buildResultReport(resp, pattern);
    }

    /**
     * get list of Design Steps of a test case
     *
     * @param testid test case id
     * @return list of qc entities
     */
    public List<QCEntity> getDesignSteps(String testid) {
        int entityType = QCConstants.ENTITY_TYPE_DESIGN_STEP;
        String query = buildQuery("parent-id", testid);
        String searchResults = getEntity(entityType, query);
        return getEntitiesFromSearchResults(searchResults);
    }

    /**
     * get list of run steps of a test case instance
     *
     * @param runid test run instance id
     * @return list of qc entities
     */
    public List<QCEntity> getRunSteps(String runid) {
        int entityType = QCConstants.ENTITY_TYPE_RUN_STEP;
        String query = buildQuery("parent-id", runid);
        String searchResults = getEntity(entityType, query);
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
     * get required fields of qc entity per framework.rest call
     *
     * @param entityType entity type
     * @return xml content of fields definition
     */
    public String getRequiredFields(int entityType) {
        return qcConnector.getFieldsOfQCEntity(domain, project, entityType, true).readEntity(String.class);
    }

    /**
     * get qc entity by id per framework.rest call
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
     * get qc entity by id per framework.rest call
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
     * @return response of framework.rest call
     */
    public Response deleteQCEntityInQC(QCEntity entity) {
        return qcConnector.delete(domain, project, entity.getEntityType(), entity.getFieldValue("id"));
    }

    /**
     * attach file to test case in qc
     *
     * @param storyFile file to attach
     * @param tcid      test case id
     * @return response message of framework.rest call
     */
    public QCResponseMessage attachFileToTestCase(File storyFile, String tcid) {
        Response resp = appendAttachment(QCConstants.ENTITY_TYPE_TEST_CASE, tcid, storyFile);
        String pattern = "Attach Story File:\n " + storyFile.getAbsolutePath() + "\n -> {0}\n";
        return buildResultReport(resp, pattern);
    }

    /**
     * get attachment of entity in qc
     *
     * @param entityType entity type
     * @param id         entity id
     * @param fileName   attach file name in qc
     * @return steam of attachment
     */
    public InputStream getAttachment(int entityType, String id, String fileName) {
        return qcConnector.getAttachmentsFromQCEntity(domain, project, id, entityType, fileName).readEntity(InputStream.class);
    }

    /**
     * Fetch all QC Entities into a List
     *
     * @param results Response Entry of search result
     * @return List of Entites
     */
    private List<QCEntity> getEntitiesFromSearchResults(String results) {
        return QCEntities.getQCEntitiesFromXMLString(results);
    }

    /**
     * psot entity to qc
     *
     * @param qcItem qc entity
     * @return response of framework.rest call
     */
    private Response createEntityIntoQC(QCEntity qcItem) {
        return qcConnector.createQCEntity(domain, project, qcItem.getEntityType(), qcItem.getXMLContent());
    }

    /**
     * get QC Entity with query, function like search
     *
     * @param entityType entity type like test case, test case folder...
     * @param query      qc query token
     * @return Response Entry in String
     */
    private String getEntity(int entityType, String query) {
        String container = QCConstants.getContainerName(entityType);
        return qcConnector.getEntityFromQCContainer(domain, project, container, query).readEntity(String.class);
    }

    /**
     * get QC Entity with query, function like search
     *
     * @param container entity container, like test case, test case folder...
     * @param query     qc query token
     * @return Response Entry in String
     */
    private String getEntity(String container, String query) {
        return qcConnector.getEntityFromQCContainer(domain, project, container, query).readEntity(String.class);
    }

    /**
     * updateQCEntity entity to qc
     *
     * @param entity qc entity
     * @return response of framework.rest call
     */
    private Response updateEntityInQC(QCEntity entity) {
        if (vcEnabled.equalsIgnoreCase("true")) {
            return qcConnector.updateQCEntityWithVersion(domain, project, entity);
        } else {
            return qcConnector.updateQCEntity(domain, project, entity);
        }
    }

    /**
     * updateQCEntity entity to qc
     *
     * @param entity qc entity
     * @return response of framework.rest call
     */
    private Response updateEntityInQC(QCEntity entity, String upToDate) {
        if (vcEnabled.equalsIgnoreCase("true")) {
            return qcConnector.updateQCEntityWithVersion(domain, project, entity.getEntityType(), entity.getQcEntityID(), upToDate);
        } else {
            return qcConnector.updateQCEntity(domain, project, entity, upToDate);
        }
    }

    /**
     * post attachment to qc entity
     *
     * @param entityType entity type
     * @param entityId   entity id
     * @param fileName   file name
     * @param content    content of file
     * @return response of framework.rest call
     */
    private Response appendAttachment(int entityType, String entityId, String fileName, byte[] content) {
        if (content != null) {
            Response resp = qcConnector.appendAttachmentsToQCEntity(domain, project, entityId, entityType, fileName, content);
            log("INFO", "Attachment appened: " + resp.getStatusInfo().getReasonPhrase());
            return resp;
        } else {
            log("INFO", "File Content is null, no Attachment appended.");
            return null;
        }
    }

    private Response appendAttachment(int entityType, String id, File file) {
        byte[] content = null;
        if (file.exists()) {
            try {
                log("INFO", "Try to read File for Attachment: " + file.getAbsolutePath());
                content = FileOperation.readFileToByteArray(file);
            } catch (IOException ex) {
                SystemLogger.error(ex);
            }
            log("INFO", "Try to Upload Attachment: " + file.getName());
            return appendAttachment(entityType, id, file.getName(), content);
        } else {
            return null;
        }
    }

    /**
     * build qc response message with returned entity after framework.rest request
     *
     * @param resp           framework.rest response
     * @param messagePattern pattern of message format  expected {0} position of status info
     *                       likely: run job - xxxx with yyyy -> {0}
     * @return message
     */
    private QCResponseMessage buildResultReport(Response resp, String messagePattern) {
        String statusInfo = QCConstants.getReturnStatus(resp.getStatus());
        String entry = resp.readEntity(String.class);
        String message = MessageFormat.format(messagePattern, statusInfo);
        log("DEBUG", message + entry);
        QCResponseMessage qcrm = new QCResponseMessage(getFirstEntityInResponse(entry), message, entry);
        return qcrm;
    }

    private String spaceResolver(String nameForQuery) {
        return "=" + '"' + nameForQuery.replace(" ", "*") + '"';
    }

    private String buildQuery(String attribute, String value) {
        return "{" + buildQueryPart(attribute, value) + "}";
    }

    private String buildQueryPart(String key, String content) {
        if (key.contains("name")) {
            return key + "[" + spaceResolver(content) + "]";
        } else {
            return key + "[" + content + "]";
        }
    }

    private String buildQuery(Map<String, String> paramenters) {
        StringBuilder query = new StringBuilder();
        paramenters.forEach((key, value) -> query.append(buildQueryPart(key, value)).append(";"));
        String content = query.toString();
        return "{" + content.substring(0, content.lastIndexOf(";")) + "}";
    }


    private LinkedList<String> createNewDesignStepsToTC(LinkedList<String[]> steps, String testcaseId) {
        LinkedList<String> results = new LinkedList<>();
        steps.stream()
                .map((entry) -> QCEntityBuilder.buildDesignStep(entry[0], entry[1], testcaseId, stepOrder++))
                .map(this::createEntityIntoQC).forEachOrdered((resp) -> {
            String pattern = "Create Step: " + stepOrder + " -> {0}\n";
            String resText = buildResultReport(resp, pattern).getMessage();
            results.add(resText);
        });
        stepOrder = 0;
        if (results.isEmpty()) {
            results.add("no new Steps created.");
        }
        return results;
    }

    /**
     * try to find entity with type via query
     *
     * @param entityType type
     * @param query      query
     * @return 0 if not found, else the id of entity
     */
    private QCEntityExistance getEntityExistence(int entityType, String query) {
        String context = getEntity(entityType, query);
        if (QCEntities.getTotalResults(context) == 0) {
            return new QCEntityExistance(false, -1, context);
        } else {
            return new QCEntityExistance(true, Integer.parseInt(getIdInResponse(context)), context);
        }
    }
}
