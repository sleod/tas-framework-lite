package ch.sleod.testautomation.framework.rest.hpqc.connection;

import ch.sleod.testautomation.framework.common.logging.SystemLogger;

import java.util.LinkedList;
import java.util.Map;

import static ch.sleod.testautomation.framework.common.logging.SystemLogger.log;

/**
 * Object Class of QC Entity
 */
public class QCEntity {

    private final Map<String, String> entityFields;
    private final int entityType;

    /**
     * parse the XML and checkFields the Attribute Map with key and name=""
     * Value should be setup
     *
     * @param xmlSchema  is the REST Required Fields in XML
     * @param fields     name of required fields
     * @param entityType type of Entity
     */
    public QCEntity(Map<String, String> fields, String xmlSchema, int entityType) throws IllegalArgumentException {
        this.entityType = entityType;
        entityFields = QCEntities.getRequiredFields(xmlSchema);
        if (!verifyAttributs(entityFields, fields)) {
            entityFields.forEach((kk, vv) -> SystemLogger.log("INFO", kk + " -> " + vv));
            throw new IllegalArgumentException("QCEntity unsatisfied! Required Value of Field is missing! Check up the fields Definition for the Entity!");
        }
    }

    /**
     * init QC entity with fields and type
     *
     * @param fields     entity fields
     * @param entityType entity type
     */
    public QCEntity(Map<String, String> fields, int entityType) {
        this.entityType = entityType;
        entityFields = fields;
    }

    /**
     * get qc entity id
     *
     * @return id
     */
    public String getQcEntityID() {
        return entityFields.get("id");
    }

    /**
     * get entity type
     *
     * @return type
     */
    public int getEntityType() {
        return entityType;
    }

    /**
     * get entity fields map
     *
     * @return map
     */
    public Map<String, String> getEntityFields() {
        return entityFields;
    }

    /**
     * set entity field
     *
     * @param key   field name
     * @param value name
     * @return result
     */
    public String setAttribute(String key, String value) {
        return entityFields.put(key, value);
    }

    /**
     * get field name of entity
     *
     * @param key
     * @return
     */
    public String getFieldValue(String key) {
        return entityFields.get(key);
    }

    public String getXMLContent() {
        return QCEntityBuilder.buildEntityXMLContent(this);
    }

    public void cleanUpEmptyValue() {
        LinkedList<String> keys = new LinkedList<>();
        entityFields.forEach((key, value) -> {
            if (value.isEmpty()) {
                keys.add(key);
            }
        });
        keys.forEach(entityFields::remove);
    }

    /**
     * insert override input attributes into required fields map
     * then after this, required fields should not contain empty String name
     * otherwise verification failed
     *
     * @param attributeRequired required fields
     * @param attributes        input attributes
     * @return verification result
     */
    private boolean verifyAttributs(Map<String, String> attributeRequired, Map<String, String> attributes) {
        attributes.forEach((kk, vv) -> {
            if (!vv.isEmpty())
                attributeRequired.put(kk, vv);
        });
        return (!attributeRequired.containsValue(""));
    }
}
