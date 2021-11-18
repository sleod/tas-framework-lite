package ch.qa.testautomation.framework.rest.hpqc.connection;

import ch.qa.testautomation.framework.common.logging.SystemLogger;
import ch.qa.testautomation.framework.common.utils.StringTextUtils;
import ch.qa.testautomation.framework.common.utils.XMLUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Utils for Entities
 */
public class QCEntities {

    /**
     * fetch QC Entities from search Result
     *
     * @param searchResult search result
     * @return list of QC Entity from search result
     */
    public static List<QCEntity> getQCEntitiesFromXMLString(String searchResult) {
        LinkedList<QCEntity> qcEntities = new LinkedList<>();
        try {
            Element root = XMLUtils.getDocumentFromXML(searchResult).getRootElement();
            if (root.getName().equalsIgnoreCase("Entity")) {
                qcEntities.add(getQCEntityFromDocElement(root));
            }
            return fetchQCEntitiesFromDocument(root, "Entity");
        } catch (IOException | JDOMException ex) {
            SystemLogger.error(ex);
        }
        return qcEntities;
    }

    /**
     * fetch entity from document with tag name of entity
     *
     * @param root      root element of doc
     * @param entityTag tag name
     * @return list of QC Entity
     */
    private static List<QCEntity> fetchQCEntitiesFromDocument(Element root, String entityTag) {
        LinkedList<QCEntity> qcEntities = new LinkedList<>();
        List<Element> elements = XMLUtils.getFilteredElements(root, entityTag);
        elements.forEach(element -> {
            QCEntity qcEntity = getQCEntityFromDocElement(element);
            //avoid subject or root folder or some special object
            if (!qcEntity.getFieldValue("id").equals("0")) {
                qcEntities.add(qcEntity);
            }
        });
        return qcEntities;
    }

    /**
     * count number of entitie in search result
     *
     * @param document search result xml doc
     * @return number of count, [0, x]
     */
    public static int getTotalResults(Document document) {
        Element root = document.getRootElement();
        if (root.getName().equalsIgnoreCase("Entities")) {
            String count = root.getAttributeValue("TotalResults");
            return Integer.parseInt(count);
        } else {
            return 0;
        }
    }

    /**
     * count number of entities in search result
     *
     * @param content search result xml string
     * @return number of count, [0, x]
     */
    public static int getTotalResults(String content) {
        String count = StringTextUtils.getValueInContent(content, "TotalResults=\"(\\d+)\"");
        return Integer.parseInt(count);
    }

    /**
     * fetch required fields into map with empty String name
     *
     * @param xmlSchema xml schema
     * @return map of fields
     */
    public static Map<String, String> getRequiredFields(String xmlSchema) {
        LinkedHashMap<String, String> requiredFields = new LinkedHashMap<>();
        try {
            List<Element> fields = XMLUtils.getFilteredElements(XMLUtils.getDocumentFromXML(xmlSchema).getRootElement(), "Field");
            fields.forEach(field -> requiredFields.put(field.getAttributeValue("Name"), ""));
            return requiredFields;
        } catch (IOException | JDOMException ex) {
            SystemLogger.error(ex);
        }
        return requiredFields;
    }

    private static QCEntity getQCEntityFromDocElement(Element element) {
        QCEntity qce = QCEntityBuilder.buildEntityWithEntityDocElement(element);
        return qce;
    }

}
