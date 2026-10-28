package io.github.sleod.tas.core.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.sleod.tas.common.IOUtils.ResourceUtils;
import io.github.sleod.tas.common.enumerations.FileFormat;
import io.github.sleod.tas.common.utils.DBConnector;
import io.github.sleod.tas.common.utils.StringTextUtils;
import io.github.sleod.tas.configuration.PropertyResolver;
import io.github.sleod.tas.common.IOUtils.ObjectMapperSingleton;
import io.github.sleod.tas.exception.ExceptionBase;
import io.github.sleod.tas.exception.ExceptionErrorKeys;
import io.github.sleod.tas.exception.TestDataEmptyException;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.sleod.tas.common.logging.SystemLogger.debug;
import static io.github.sleod.tas.common.logging.SystemLogger.info;

public class TestDataContainer {

    private JsonNode jsonData;
    private List<Map<String, Object>> dataContent;
    private boolean repeat = false;
    private static JsonNode globalTestData;
    private static final Map<String, Object> tempData = new LinkedHashMap<>();
    private final Map<String, Object> pageObjects = new LinkedHashMap<>();
    private boolean additionalData = false;
    private final static ObjectMapper mapper = ObjectMapperSingleton.mapper();

    public TestDataContainer(String testDataRef, String additional) {
        if (StringTextUtils.isValid(testDataRef)) {
            try {
                loadTestData(testDataRef);
            } catch (IOException ex) {
                throw new ExceptionBase(ExceptionErrorKeys.TEST_DATA_LOAD_FAILED, ex, "Error while loading test data from reference: ", testDataRef);
            }
            try {
                loadAdditionalTestData(additional);
            } catch (IOException ex) {
                throw new ExceptionBase(ExceptionErrorKeys.TEST_DATA_LOAD_FAILED, ex, "Error while loading additional test data from reference: ", additional);
            }
        } else {
            throw new ExceptionBase(ExceptionErrorKeys.TEST_DATA_FOR_TEST_OBJECT_NOT_DEFINED);
        }
    }

    public TestDataContainer(List<Map<String, Object>> dataContent, String additional) {
        this.dataContent = dataContent;
        try {
            loadAdditionalTestData(additional);
        } catch (IOException ex) {
            throw new ExceptionBase(ExceptionErrorKeys.TEST_DATA_LOAD_FAILED, ex, "Error while loading additional test data from reference: ", additional);
        }
    }

    public Object getPageObject(String key) {
        return pageObjects.get(key);
    }

    public void addPageObject(String key, Object pageObject) {
        pageObjects.put(key, pageObject);
    }

    public void clearPageObjects() {
        pageObjects.clear();
    }

    public static Object getTempData(String key) {
        return tempData.get(key);
    }

    public static String getTempStringData(String key) {
        return String.valueOf(tempData.get(key));
    }

    public static void setTempData(String key, Object object) {
        tempData.put(key, object);
    }

    public static void clearTempData() {
        tempData.clear();
    }

    public static void removeTempData(String key) {
        tempData.remove(key);
    }

    public List<Map<String, Object>> getDataContent() {
        if (dataContent == null) {
            Map<String, Object> mapJsonData = mapper.convertValue(jsonData, new TypeReference<>() {
            });
            return Collections.singletonList(mapJsonData);
        }
        return mapper.convertValue(dataContent, new TypeReference<>() {
        });
    }

    private void loadAdditionalTestData(String fileRef) throws IOException {
        if (StringTextUtils.isValid(fileRef)) {
            String addTestDataRef = fileRef.toLowerCase();
            if (!addTestDataRef.startsWith("file:") || !addTestDataRef.endsWith(".json")) {
                throw new ExceptionBase(ExceptionErrorKeys.ADDITIONAL_TEST_DATA_FILE_CAN_ONLY_BE_JSON);
            } else {
                additionalData = true;
                loadTestData(fileRef);
            }
        } else {
            additionalData = false;
        }
    }

    /**
     * get object of global test data
     *
     * @param key to name
     * @return object can be null
     */
    public static JsonNode getGlobalTestDataReturnJsonNode(String key) {
        return globalTestData.get(key);
    }

    public static Object getGlobalTestData(String key) {
        Object value = globalTestData.get(key);
        if (value instanceof ArrayNode) {
            return mapper.convertValue(value, new TypeReference<Object[]>() {
            });
        } else if (value instanceof ObjectNode) {
            return mapper.convertValue(value, new TypeReference<Map<String, Object>>() {
            });
        } else return value;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public Object getParameter(String key, int parameterRow) {
        if (key.isEmpty()) {
            throw new ExceptionBase(ExceptionErrorKeys.CUSTOM_MESSAGE, "Key of parameter should not be empty!");
        }
        if (key.startsWith("global.")) {
            return getTestDataInJSON(key.replace("global.", ""), globalTestData);
        } else {
            //check first in dataContent
            if (Objects.nonNull(dataContent) && checkDataContentContainsKey(key)) {
                return dataContent.get(parameterRow).get(key);
            } else if (Objects.nonNull(jsonData)) {
                //check jsonData if not null
                return getTestDataInJSON(key, jsonData);
            } else {
                //return key self for customized input
                return key;
            }
        }
    }

    private boolean checkDataContentContainsKey(String key) {
        if (dataContent == null || dataContent.isEmpty()) {
            throw new TestDataEmptyException("Data content is empty or not initialized.", "TestDataContainer -> dataContent");
        }
        return dataContent.getFirst().containsKey(key);
    }

    public Integer getDataContentSize() {
        return dataContent.size();
    }

    private Object getTestDataInJSON(String key, JsonNode storage) {
        if (key.startsWith("'") && key.endsWith("'")) {
            return key.replace("'", "");
        } else if (storage.has(key)) {
            return storage.get(key);
        } else {
            if (key.contains(".")) {
                String[] layers = key.split("\\.");
                JsonNode current = storage.get(layers[0]);
                for (int index = 1; index < layers.length - 1; index++) {
                    if (current instanceof ObjectNode) {
                        current = current.get(layers[index]);
                    } else {
                        throw new ExceptionBase(ExceptionErrorKeys.TEST_DATA_PARAMETER_NOT_FOUND, key);
                    }
                }
                if (current instanceof ObjectNode) {
                    return current.get(layers[layers.length - 1]);
                } else if (current instanceof ArrayNode && key.endsWith(".values")) {
                    return current;
                } else {
                    throw new ExceptionBase(ExceptionErrorKeys.TEST_DATA_PARAMETER_NOT_FOUND, key);
                }
            }
            if (PropertyResolver.isSimpleStringParameterAllowed()) {
                return key;
            } else {
                throw new ExceptionBase(ExceptionErrorKeys.KEY_NOT_FOUND_IN_TEST_DATA_SOURCE, key);
            }
        }
    }

    private void loadTestData(String testDataRef) throws IOException {
        String[] token = testDataRef.split(":");
        if ("File".equals(token[0])) {
            String filePath;
            String fileName = new File(token[1].replace("\\", "/")).getName();
            String testDataFilePath = ResourceUtils.getFilePathWithName(fileName);
            info("Load test data file: " + testDataFilePath);
            loadWithFile(testDataFilePath);
        } else {
            throw new ExceptionBase(ExceptionErrorKeys.TEST_DATA_REFERENCE_NO_MATCH, testDataRef);
        }
    }

    private void loadWithFile(String filepath) throws IOException {
        //get file extension to determine the file format
        Pattern pattern = Pattern.compile("\\.(\\w+)$");
        Matcher matcher = pattern.matcher(filepath);
        if (matcher.find()) {
            String suffix = matcher.group(1);
            FileFormat fileFormat = FileFormat.valueOf(suffix.toUpperCase());
            switch (fileFormat) {
                case EXCEL, EXCELX -> loadExcelContent(filepath);
                case CSV -> loadCSVContent(filepath);
                case SQL -> loadDBContentWithSQL(filepath);
                case JSON -> loadJSONContent(filepath);
                default -> throw new ExceptionBase(ExceptionErrorKeys.TEST_DATA_REFERENCE_FORMAT_UNSUPPORTED, filepath);
            }
        }
    }

    public static void loadGlobalTestData(String path) {
        info("Load Global Test Data: " + path);
        String content;
        try {
            content = ResourceUtils.readResourceFileToString(path);
        } catch (IOException ex) {
            throw new ExceptionBase(ExceptionErrorKeys.EXCEPTION_BY_DESERIALIZATION, ex, path);
        }
        try {
            globalTestData = mapper.readTree(content);
        } catch (JsonProcessingException ex) {
            throw new ExceptionBase(ExceptionErrorKeys.EXCEPTION_BY_DESERIALIZATION, ex, path);
        }
    }

    /**
     * load CSV content and regarding comment sign like #, //, "/* .. \*\/
     *
     * @param filepath testData file path
     */
    private void loadCSVContent(String filepath) throws IOException {
        List<String> content = ResourceUtils.readResourceFileToLinedString(filepath);
        if (!content.isEmpty()) {
            if (dataContent == null) {
                dataContent = new LinkedList<>();
            }
            if (content.size() == 1) {
                info("CSV data has no content or no header!");
            } else setRepeat(content.size() > 2);
            parseCSVNonComment(content);
        }
    }

    private void parseCSVNonComment(List<String> lines) {
        String[] columns = lines.getFirst().split(";");
        boolean commentBlockClosed = true;
        for (int rowNumber = 1; rowNumber < lines.size(); rowNumber++) {
            Map<String, Object> rowContent = new HashMap<>(columns.length);
            String line = lines.get(rowNumber);
            if (line.startsWith("//") || line.startsWith("#")) {
                continue;
            } else if (line.startsWith("/*")) {
                commentBlockClosed = false;
                continue;
            } else if (line.startsWith("*/")) {
                commentBlockClosed = true;
                continue;
            }
            if (commentBlockClosed) {
                Object[] values = line.split(";");
                if (values.length != columns.length) {
                    throw new ExceptionBase(ExceptionErrorKeys.COLUMN_AND_CSV_HEADER_ARE_NOT_IDENTICAL, rowNumber);
                }
                for (int i = 0; i < columns.length; i++) {
                    rowContent.put(columns[i], values[i]);
                }
                dataContent.add(rowContent);
            }
        }
    }

    private void loadDBContentWithSQL(String filepath) throws IOException {
        String sql = ResourceUtils.readResourceFileToString(filepath);
        dataContent = DBConnector.connectAndExecute(sql);
        setRepeat(dataContent.size() > 1);
    }

    //todo: define excel content loader
    private void loadExcelContent(String filepath) {
        debug("Load Excel content: " + filepath);
    }

    private void loadJSONContent(String filepath) throws IOException {
        debug("Load Json content: " + filepath);
        String content = ResourceUtils.readResourceFileToString(filepath);
        ObjectNode jsonNodes;
        try {
            jsonNodes = (ObjectNode) mapper.readTree(content);
            if (additionalData && jsonData != null) {
                ObjectNode workingNode = jsonData.deepCopy();
                for (Map.Entry<String, JsonNode> field : jsonNodes.properties()) {
                    String key = field.getKey();
                    JsonNode fieldValue = field.getValue();
                    if (!workingNode.has(key)) {
                        workingNode.set(key, fieldValue);
                    }
                }
                jsonData = workingNode;
            } else {
                jsonData = jsonNodes;
            }
        } catch (JsonProcessingException ex) {
            throw new ExceptionBase(ExceptionErrorKeys.EXCEPTION_BY_DESERIALIZATION, ex, filepath);
        }
    }
}
