package ch.qa.testautomation.tas.rest.connection.hpqc;

import ch.qa.testautomation.tas.common.utils.DateTimeUtils;
import ch.qa.testautomation.tas.common.utils.StringTextUtils;
import ch.qa.testautomation.tas.common.utils.XMLUtils;
import ch.qa.testautomation.tas.exception.ExceptionBase;
import ch.qa.testautomation.tas.exception.ExceptionErrorKeys;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class QCEntityBuilder {
    /**
     * build QC Entity with content and verify with schema
     *
     * @param qce input
     * @return verified qc entity
     */
    public static String buildEntityXMLContent(QCEntity qce) {
        String template = QCConstants.ENTITY_TEMPLATE;
        try {
            return buildContext(XMLUtils.getDocumentFromXML(template), qce);
        } catch (IOException | JDOMException ex) {
            throw new ExceptionBase(ExceptionErrorKeys.CUSTOM_MESSAGE, "Exception while build QC Entity!", ex);
        }
    }

    /**
     * build test case entity with
     *
     * @param tcName         test case name
     * @param description    description
     * @param parentId       parent id
     * @param requiredFields required fields
     * @return qc entity
     */
    public static QCEntity buildNewTestCaseEntity(String owner, String tcName, String description, String parentId,
                                                  String requiredFields, Map<String, String> testCaseRequiredFields) {
        Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put("owner", owner);
        attributes.put("subtype-id", "MANUAL");
        attributes.put("parent-id", parentId);
        attributes.put("name", tcName);
        attributes.put("description", description);
        attributes.put("status", "Ready");
        testCaseRequiredFields.forEach(attributes::putIfAbsent);
        int entityType = QCConstants.ENTITY_TYPE_TEST_CASE;
        return buildEntityWithAttributes(attributes, entityType, requiredFields);
    }

    /**
     * build new qc Entity for new run of lab instance
     *
     * @param qcInsEntity    QC Entity of instance
     * @param requiredFields required field of new run
     * @param owner          run owner
     * @return QC Entity of Run
     */
    public static QCEntity buildNewRunEntity(QCEntity qcInsEntity, String requiredFields, String owner, String name) {
        //prepair new Run
        int entityType = QCConstants.ENTITY_TYPE_RUN;
        String testId = qcInsEntity.getFieldValue("test-id");
        String instanceId = qcInsEntity.getFieldValue("id");
        LinkedHashMap<String, String> runAttris = new LinkedHashMap<>();
        String dateTime = DateTimeUtils.getFormattedDateTimeNow("MM-dd_HH-mm");
        String dateNow = DateTimeUtils.getFormattedDateNow("yyyy-MM-dd");
        runAttris.put("name", "Run_" + dateTime + name);
        runAttris.put("test-id", testId);
        runAttris.put("testcycl-id", instanceId);
        runAttris.put("cycle-id", qcInsEntity.getFieldValue("cycle-id"));
        runAttris.put("owner", owner);
        runAttris.put("execution-date", dateNow);
        runAttris.put("subtype-id", "hp.qc.run.MANUAL");
        return new QCEntity(runAttris, requiredFields, entityType);
    }

    /**
     * Build new test folder entity
     *
     * @param dirName        folder name
     * @param parentId       parent id
     * @param requiredFields required fields
     * @return qc entity
     */
    public static QCEntity buildNewTestPlanFolderEntity(String dirName, String parentId, String requiredFields) {
        //Folder Entity
        int entityType = QCConstants.ENTITY_TYPE_TESTPLAN_FOLDER;
        LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
        attributes.put("parent-id", parentId);
        attributes.put("name", dirName);
        return new QCEntity(attributes, requiredFields, entityType);
    }

    /**
     * build qc entity with xml doc element
     *
     * @param element doc Element of single entity, likely startNow with <Entity><Entity/>
     * @return qc entity
     */
    public static QCEntity buildEntityWithEntityDocElement(Element element) {
        String eType = element.getAttributeValue("Type");
        int entityType = QCConstants.getEntityType(eType);
        return new QCEntity(fetchEntityFieldsOfEntityDocElement(element), entityType);
    }

    /**
     * build new design step entity
     *
     * @param description description
     * @param expected    expected
     * @param parentId    test case id of step
     * @param stepOrder   order of the step
     * @return qc entity
     */
    public static QCEntity buildDesignStep(String description, String expected, String parentId, int stepOrder) {
        LinkedHashMap<String, String> fields = new LinkedHashMap<>();
        int entityType = QCConstants.ENTITY_TYPE_DESIGN_STEP;
        fields.put("description", description);
        fields.put("expected", expected);
        fields.put("parent-id", parentId);
        fields.put("step-order", String.valueOf(stepOrder));
        fields.put("name", "Step " + stepOrder);
        return new QCEntity(fields, entityType);
    }

    /**
     * build new test set entity
     *
     * @param tsName         test set name
     * @param pid            parent id
     * @param requiredFields required fields
     * @return qc entity
     */
    public static QCEntity buildNewTestSetEntity(String tsName, String pid, String requiredFields) {
        int entityType = QCConstants.ENTITY_TYPE_TESTSET;
        LinkedHashMap<String, String> fields = new LinkedHashMap<>();
        fields.put("parent-id", pid);
        fields.put("name", tsName);
        fields.put("subtype-id", "hp.qc.test-set.default");
        return buildEntityWithAttributes(fields, entityType, requiredFields);
    }

    /**
     * build new test set entity
     *
     * @param tsfName        test set folder name
     * @param pid            parent id
     * @param requiredFields required fields
     * @return qc entity
     */
    public static QCEntity buildNewTestLabFolderEntity(String tsfName, String pid, String requiredFields) {
        int entityType = QCConstants.ENTITY_TYPE_TESTLAB_FOLDER;
        LinkedHashMap<String, String> fields = new LinkedHashMap<>();
        fields.put("parent-id", pid);
        fields.put("name", tsfName);
        return buildEntityWithAttributes(fields, entityType, requiredFields);
    }

    /**
     * build new instance entity
     *
     * @param cycleId        parent id of instance = test set id
     * @param tcId           test case id ref to test case in test plan
     * @param owner          owner
     * @param requiredFields required fields
     * @return qc entity
     */
    public static QCEntity buildNewInstanceEntity(String cycleId, String tcId, String owner, String requiredFields) {
        LinkedHashMap<String, String> fields = new LinkedHashMap<>();
        fields.put("cycle-id", cycleId);
        fields.put("test-id", tcId);
        //fields.put("test-order", "1");
        fields.put("subtype-id", "hp.qc.test-instance.MANUAL");
        fields.put("owner", owner);
        int entityType = QCConstants.ENTITY_TYPE_INSTANCE;
        return buildEntityWithAttributes(fields, entityType, requiredFields);
    }

    /**
     * build new Requirement Entity
     *
     * @param reqId          requirement id in qc
     * @param testId         test case id
     * @param requiredFields required fields
     * @return qc entity
     */
    public static QCEntity buildNewRequirementEntity(String reqId, String testId, String requiredFields) {
        LinkedHashMap<String, String> fields = new LinkedHashMap<>();
        fields.put("coverage-mode", "All Configurations");
        fields.put("requirement-id", reqId);
        fields.put("test-id", testId);
        fields.put("entity-type", "test");
        int entityType = QCConstants.ENTITY_TYPE_COVERAGE;
        return buildEntityWithAttributes(fields, entityType, requiredFields);
    }

    /**
     * build run step entity of instance in lab
     *
     * @param runId        instance id
     * @param testId       test case id
     * @param designStepId design step id
     * @param status       run status
     * @param required     required fields
     * @return qc entity
     */
    public static QCEntity buildNewInstanceStep(String designStepId, String testId, String runId, String status, String required) {
        LinkedHashMap<String, String> stepAttris = new LinkedHashMap<>();
        int entityType = QCConstants.ENTITY_TYPE_RUN_STEP;
        stepAttris.put("desstep-id", designStepId);
        stepAttris.put("test-id", testId);
        stepAttris.put("parent-id", runId);
        stepAttris.put("execution-date", DateTimeUtils.getFormattedDateNow("yyyy-MM-dd"));
        stepAttris.put("status", status);
        return buildEntityWithAttributes(stepAttris, entityType, required);
    }

    /**
     * build simple xml content of status update for test instance
     *
     * @param fieldName field Name
     * @param value     value
     * @return xml content
     */
    public static String buildSingleFieldPayload(String fieldName, String value) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Entity Type=\"run\">"
                + "<Fields><Field Name=\"" + fieldName + "\"><Value>" + StringTextUtils.escapeHTML(value) + "</Value></Field></Fields></Entity>";
    }

    /**
     * build QC Entity with xml document, likely:
     * <Entity Type="ttt"><Fields><Field Name="xxx"><Value>yyy<Value/><Field/><Fields/><Entity/>
     *
     * @param docElement Doc element
     * @return attributes of qc entity
     */
    private static Map<String, String> fetchEntityFieldsOfEntityDocElement(Element docElement) {
        return XMLUtils.fetchFieldsAndValues(docElement, "Field", "Name", "Value");
    }

    private static String buildContext(Document document, QCEntity qce) {
        Element root = document.getRootElement();
        String value = QCConstants.getEntityName(qce.getEntityType());
        root.setAttribute("Type", value);
        Document doc = buildEntityContent(document, qce);
        return XMLUtils.convertDocumentToString(doc);
    }

    private static Document buildEntityContent(Document document, QCEntity qce) {
        Element root = document.getRootElement();
        Element fields = root.addContent(new Element("Fields")).getChild("Fields");
        //feed the required Fields
        qce.getEntityFields().forEach((key, value) -> {
            Element field = new Element("Field");
            field.setAttribute("Name", key)
                    .addContent(new Element("Value"))
                    .getChild("Value").addContent(value);
            fields.addContent(field);
        });
        return document;
    }

    /**
     * build Entity with attributes, entity type and required fields
     *
     * @param attributes     attis
     * @param entityType     entity type
     * @param requiredFields required fields
     * @return qc entity
     */
    private static QCEntity buildEntityWithAttributes(Map<String, String> attributes, int entityType, String requiredFields) {
        return new QCEntity(attributes, requiredFields, entityType);
    }

}
