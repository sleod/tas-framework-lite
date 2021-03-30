package ch.raiffeisen.testautomation.framework.common.utils;

import com.google.common.collect.Lists;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class XMLUtils {


    /**
     * create new doc with root element name
     *
     * @param rootElementName root element name
     * @return document
     */
    public static Document createNewDoc(String rootElementName) {
        Document document = new Document();
        document.setContent(new Element(rootElementName));
        return document;
    }


    /**
     * get jdom2 document from xml string
     *
     * @param xmlContent xml string
     * @return jdom2 document
     * @throws IOException io exceptions
     * @throws JDOMException JDOMException
     */
    public static Document getDocumentFromXML(String xmlContent) throws IOException, JDOMException {
        SAXBuilder saxBuilder = new SAXBuilder();
        return saxBuilder.build(new StringReader(xmlContent));
    }

    /**
     * parse xml file to SAX XML Document
     *
     * @param file the original xml file
     * @return document
     */
    public static Document getXMLDocumentFromXMLFile(final File file) throws JDOMException, IOException {
        SAXBuilder builder = new SAXBuilder();
        return builder.build(file);
    }

    /**
     * get first child element with tag name
     *
     * @param document     jdom2 document
     * @param childTagName tag name of child
     * @return doc element
     */
    public static Element getFirstElement(Document document, String childTagName) {
        return getFilteredElements(document.getRootElement(), childTagName).get(0);
    }

    /**
     * get all children with tag name
     *
     * @param root    root element of doc
     * @param tagName tag name of child
     * @return list of elements of childern
     */
    public static List<Element> getFilteredElements(Element root, String tagName) {
        if (root.getName().equalsIgnoreCase(tagName)) {
            return Lists.newArrayList(root);
        }
        ElementFilter filter = new ElementFilter(tagName);
        Iterator<Element> nodes = root.getDescendants(filter);
        return Lists.newArrayList(nodes);
    }

    /**
     * convert xml to string
     *
     * @param document xml doc
     * @return string of doc
     */
    public static String convertDocumentToString(Document document) {
        XMLOutputter xmlOutputter = new XMLOutputter();
        xmlOutputter.setFormat(Format.getCompactFormat());
        return xmlOutputter.outputString(document.getRootElement());
    }

    /**
     * fetch fields and name into map, case empty name = empty string
     *
     * @param root          root element
     * @param fieldTag      tag name of field
     * @param attributeName attribute name, likey <Field Name="xxxx"><Field/>
     * @param valueNodeName name node name, likely <Value><Value/>
     * @return map of attributes
     */
    public static Map<String, String> fetchFieldsAndValues(Element root, String fieldTag, String attributeName, String valueNodeName) {
        LinkedHashMap<String, String> fieldsAndValues = new LinkedHashMap<>();
        getFilteredElements(root, fieldTag).forEach(element -> {
            String attribute = element.getAttribute(attributeName).getValue();
            Element valueNode = element.getChild(valueNodeName);
            if (valueNode != null) {
                fieldsAndValues.put(attribute, valueNode.getValue());
            }
        });
        return fieldsAndValues;
    }

}
