//package testFramework.testQCConnection;
//
//
//import ch.raiffeisen.testautomation.framework.configuration.PropertyResolver;
//import ch.raiffeisen.testautomation.framework.rest.hpqc.connection.QCConnector;
//import ch.raiffeisen.testautomation.framework.rest.hpqc.connection.QCConstants;
//import ch.raiffeisen.testautomation.framework.rest.hpqc.connection.QCRestClient;
//import org.junit.Test;
//
//import java.util.LinkedList;
//
//import static ch.raiffeisen.testautomation.framework.common.logging.SystemLogger.error;
//import static ch.raiffeisen.testautomation.framework.common.logging.SystemLogger.log;
//
//
//public class QCConnectionTests {
//
//
//    private String qcUser = System.getProperty("user.name");
//    private String qcPassword = "cHc0UmFBMTAwKw==";
//    private String qcHost = "http://vaafist916.ad.raiffeisen.ch:8080/qcbin";
//    private String qcDomain = "DEFAULT";
//    private String qcProject = "TEST_Automation_S2_2018";
//    private String qcPlanRootFolder = "Subject/Trash";
//    private String qcLabRootFolder = "Root/Automation Framework TEST";
//    private boolean qcVersionControlEnabled = false;
//
//    private QCRestClient restClient = new QCRestClient(new QCConnector(qcHost, qcUser, PropertyResolver.decodeBase64(qcPassword)), qcDomain, qcProject, qcPlanRootFolder, qcLabRootFolder);
//
//
//    @Test
//    public void testQCConnectorActions() {
//        try {
//            //find test folder "trash"
//            String testFolderId = restClient.getEntityIDByName(QCConstants.ENTITY_TYPE_TEST_FOLDER, "Trash");
//            log("DEBUG", "find plan folder id: " + testFolderId);
//
//            //create test folder into trash
//            String targetFolderId = restClient.addNewTestPlanFolder("Test QCCONNECTION", testFolderId).getEntityId();
//            log("DEBUG", "new plan folder id: " + targetFolderId);
//
//            //create test case into folder
//            String testCaseId = restClient.syncTestCase("Demo1.json", "Demo Test Case for Rest API and Framework", targetFolderId, "AP.TFS").getEntityId();
//            log("DEBUG", "new test case id: " + targetFolderId);
//
//            //create test design steps into test case
//            LinkedList<String[]> steps = new LinkedList<>();
//            steps.add(new String[]{"Open Link", "Page Shows"});
//            steps.add(new String[]{"Search: Raiffeisenbank", "Search done"});
//            restClient.syncDesignStepsToTestCase(steps, testCaseId);
//
//            //find test lab folder "trach"
//            String testLabTestSetFolderId = restClient.getEntityIDByName(QCConstants.ENTITY_TYPE_TESTSETS_FOLDER, "Trash");
//            log("DEBUG", "find lab folder id: " + testLabTestSetFolderId);
//
//            //create test lab folder into trach
//            String testLabTestSetTargetFolderId = restClient.addNewTestSetFolder("TEST QCCONNECT", testLabTestSetFolderId).getEntityId();
//            log("DEBUG", "new lab folder id: " + testLabTestSetTargetFolderId);
//
//            //create test lab test set
//            String testSetId = restClient.addNewTestSet("test qcconnect test set", testLabTestSetTargetFolderId).getEntityId();
//            log("DEBUG", "new lab test set id: " + testSetId);
//
//            //create test instance into test set
//            String testInstanceId = restClient.addNewInstanceToSet(testCaseId, testSetId).getEntityId();
//            log("DEBUG", "new lab test instance id: " + testInstanceId);
//
//        } catch (Exception ex) {
//            error(ex);
//        } finally {
//            restClient.close();
//        }
//    }
//
//}
