package ch.qa.testautomation.tas.core.component;

import ch.qa.testautomation.tas.common.IOUtils.FileLocator;
import ch.qa.testautomation.tas.common.IOUtils.FileOperation;
import ch.qa.testautomation.tas.common.enumerations.FileFormat;
import ch.qa.testautomation.tas.common.utils.DBConnector;
import ch.qa.testautomation.tas.common.utils.StringTextUtils;
import ch.qa.testautomation.tas.configuration.PropertyResolver;
import ch.qa.testautomation.tas.core.json.ObjectMapperSingleton;
import ch.qa.testautomation.tas.exception.ExceptionBase;
import ch.qa.testautomation.tas.exception.ExceptionErrorKeys;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ch.qa.testautomation.tas.common.logging.SystemLogger.info;

public class TestDataContainer {

    private JsonNode jsonData;
    private List<Map<String, Object>> dataContent;
    @Setter
    @Getter
    private boolean repeat = false;
    private static JsonNode globalTestData;
    private static final Map<String, Object> tempData = new LinkedHashMap<>();
    private final Map<String, Object> pageObjects = new LinkedHashMap<>();
    private boolean additionalData = false;
    private final static ObjectMapper mapper = ObjectMapperSingleton.getObjectMapper();

    public TestDataContainer(String testDataRef, String additional) {
        if (StringTextUtils.isValid(testDataRef)) {
            loadTestData(testDataRef);
            loadAdditionalTestData(additional);
        } else {
            throw new ExceptionBase(ExceptionErrorKeys.TEST_DATA_FOR_TEST_OBJECT_NOT_DEFINED);
        }
    }

    public TestDataContainer(List<Map<String, Object>> dataContent, String additional) {
        this.dataContent = dataContent;
        loadAdditionalTestData(additional);
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

    private void loadAdditionalTestData(String fileRef) {
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
        if(dataContent.isEmpty()) {
            warn("Test Data Content is empty!!!");
            return false;
        }else {
            return dataContent.getFirst().containsKey(key);
        }
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

    private void loadTestData(String testDataRef) {
        String[] token = testDataRef.split(":");
        if ("File".equals(token[0])) {
            String filePath;
            if (token[1].replace("\\", "/").contains("/")) {
                filePath = FileLocator.findResource(token[1]).toString();
            } else {
                String testDataLocation = FileLocator.findResource(PropertyResolver.getTestDataLocation()).toString();
                List<Path> filePaths = FileLocator.listRegularFilesRecursiveMatchedToName(testDataLocation, 5, token[1]);
                if (filePaths.size() > 1) {
                    testDataLocation = FileLocator.findResource(PropertyResolver.getTestDataFolder()).toString();
                    filePath = FileLocator.findExactFile(testDataLocation, 5, token[1]).toString();
                } else {
                    filePath = filePaths.get(0).toString();
                }
            }
            info("Load test data file: " + filePath);
            loadWithFile(filePath);
        } else {
            throw new ExceptionBase(ExceptionErrorKeys.TEST_DATA_REFERENCE_NO_MATCH, testDataRef);
        }
    }

    private void loadWithFile(String testDataRef) {
        Pattern pattern = Pattern.compile("\\.(\\w+)$");
        Matcher matcher = pattern.matcher(testDataRef);
        if (matcher.find()) {
            String suffix = matcher.group(1);
            FileFormat fileFormat = FileFormat.valueOf(suffix.toUpperCase());
            switch (fileFormat) {
                case EXCEL, EXCELX -> loadExcelContent(testDataRef);
                case CSV -> loadCSVContent(testDataRef);
                case SQL -> loadDBContent(testDataRef);
                case JSON -> loadJSONContent(testDataRef);
                default ->
                        throw new ExceptionBase(ExceptionErrorKeys.TEST_DATA_REFERENCE_FORMAT_UNSUPPORTED, testDataRef);
            }
        }
    }

    public static void loadGlobalTestData(Path path) {
        info("Load Global Test Data: " + path);
        String content = FileOperation.readFileToLinedString(path.toString());
        try {
            globalTestData = new ObjectMapper().readTree(content);
        } catch (JsonProcessingException ex) {
            throw new ExceptionBase(ExceptionErrorKeys.EXCEPTION_BY_DESERIALIZATION, ex, path);
        }
    }

    /**
     * load CSV content and regarding comment sign like #, //, "/* .. \*\/
     *
     * @param testDataRef testData Reference
     */
    private void loadCSVContent(String testDataRef) {
        List<String> content = FileOperation.readFileToStringList(testDataRef);
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
        String[] columns = lines.get(0).split(";");
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

    private void loadDBContent(String testDataRef) {
        String sql = FileOperation.readFileToLinedString(testDataRef);
        dataContent = DBConnector.connectAndExecute(sql);
        setRepeat(dataContent.size() > 1);
    }

    //todo: define excel content loader
    private void loadExcelContent(String testDataRef) {
        String content = FileOperation.readFileToLinedString(testDataRef);
    }

    private void loadJSONContent(String testDataRef) {
        String content = FileOperation.readFileToLinedString(testDataRef);
        ObjectNode data;
        try {
            data = (ObjectNode) mapper.readTree(content);
            Iterator<Map.Entry<String, JsonNode>> fields = data.fields();
            if (additionalData && jsonData != null) {
                ObjectNode workingNode = jsonData.deepCopy();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> field = fields.next();
                    String key = field.getKey();
                    JsonNode fieldValue = field.getValue();
                    if (!workingNode.has(field.getKey())) {
                        workingNode.set(key, fieldValue);
                    }
                }
                jsonData = workingNode;
            } else {
                jsonData = data;
            }
        } catch (JsonProcessingException ex) {
            throw new ExceptionBase(ExceptionErrorKeys.EXCEPTION_BY_DESERIALIZATION, ex, testDataRef);
        }
    }
}
