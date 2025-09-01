package io.github.sleod.tas.rest.hpqc;

import io.github.sleod.tas.common.utils.DateTimeUtils;
import io.github.sleod.tas.exception.ExceptionBase;
import io.github.sleod.tas.exception.ExceptionErrorKeys;

import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;

import static io.github.sleod.tas.common.logging.SystemLogger.info;

/**
 * Object Class of QC Entity
 */
public class QCEntity implements Comparable<QCEntity> {

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
    public QCEntity(Map<String, String> fields, String xmlSchema, int entityType) {
        this.entityType = entityType;
        entityFields = QCEntities.getRequiredFields(xmlSchema);
        if (!verifyAttributs(entityFields, fields)) {
            entityFields.forEach((kk, vv) -> info(kk + " -> " + vv));
            throw new ExceptionBase(ExceptionErrorKeys.CUSTOM_MESSAGE, "QCEntity unsatisfied! Required Value of Field is missing! Check up the fields Definition for the Entity!");
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
    public String getEntityID() {
        return getFieldValue("id");
    }

    /**
     * get entity name
     *
     * @return name
     */
    public String getEntityName() {
        return getFieldValue("name");
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

    @Override
    public int compareTo(QCEntity target) {
        return DateTimeUtils.parseStringToInstant(getFieldValue("last-modified"), "yyyy-MM-dd HH:mm:ss", Locale.GERMANY)
                .compareTo(DateTimeUtils.parseStringToInstant(target.getFieldValue("last-modified"), "yyyy-MM-dd HH:mm:ss", Locale.GERMANY));
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
        attributes.forEach((key, value) -> attributeRequired.put(key, String.valueOf(value).replace("null", "")));
        return (!attributeRequired.containsValue(""));
    }

}
