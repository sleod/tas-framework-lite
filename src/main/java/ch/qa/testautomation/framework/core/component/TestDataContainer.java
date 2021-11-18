package ch.qa.testautomation.framework.core.component;

import ch.qa.testautomation.framework.common.IOUtils.FileLocator;
import ch.qa.testautomation.framework.common.IOUtils.FileOperation;
import ch.qa.testautomation.framework.common.enumerations.FileFormat;
import ch.qa.testautomation.framework.common.logging.SystemLogger;
import ch.qa.testautomation.framework.common.utils.DBConnector;
import ch.qa.testautomation.framework.configuration.PropertyResolver;
import ch.qa.testautomation.framework.core.json.deserialization.JSONContainerFactory;
import ch.qa.testautomation.framework.intefaces.DBDataCollector;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;

public class TestDataContainer {

    private final Map<Class<?>, Object> instances = new LinkedHashMap<>();
    private JSONObject jsonData;
    private FileFormat fileFormat;
    private List<Map<String, Object>> dataContent;
    private boolean repeat = false;
    private static JSONObject globalTestData;
    private static final Map<String, Object> tempData = new LinkedHashMap<>();
    private boolean additionalData = false;

    public TestDataContainer(String testDataRef, String additional) {
        if (testDataRef == null || testDataRef.isEmpty()) {
            throw new RuntimeException("No Test Data for Test Object defined!");
        }
        loadTestData(testDataRef);
        loadAdditionalTestData(additional);
    }

    public TestDataContainer(List<Map<String, Object>> dataContent, String additional) {
        this.dataContent = dataContent;
        loadAdditionalTestData(additional);
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

    public Map<Class<?>, Object> getInstances() {
        return instances;
    }

    public List<Map<String, Object>> getDataContent() {
        if (dataContent == null) {
            return Collections.singletonList(jsonData);
        }
        return dataContent;
    }

    private void loadAdditionalTestData(String fileRef) {
        if (fileRef == null || fileRef.isEmpty()) {
            additionalData = false;
        } else {
            String addTestDataRef = fileRef.toLowerCase();
            if (!addTestDataRef.startsWith("file:") && !addTestDataRef.endsWith(".json")) {
                throw new RuntimeException("Additional Test Data can only be .json file! Other formats are not supported yet!");
            } else {
                additionalData = true;
                loadTestData(fileRef);
            }
        }
    }

    /**
     * get object of global test data
     *
     * @param key to name
     * @return object can be null
     */
    public static Object getGlobalTestData(String key) {
        Object value = globalTestData.get(key);
        if (value instanceof JSONArray) {
            return ((JSONArray) value).toArray().clone();
        } else if (value instanceof JSONObject) {
            return JSONObject.fromObject(value);
        } else return value;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public Object getParameter(String key, int parameterRow) {
        if (key.startsWith("global.")) {
            return getTestDataInJSON(key.replace("global.", ""), globalTestData);
        } else {
            if (dataContent != null && checkDataContentContainsKey(key)) {

                return dataContent.get(parameterRow).get(key);
            } else if (jsonData != null) {

                return getTestDataInJSON(key, jsonData);
            } else {
                return key;
            }
        }
    }

    private boolean checkDataContentContainsKey(String key) {
        return dataContent.get(0).containsKey(key);
    }

    public Integer getDataContentSize() {
        return dataContent.size();
    }


    private Object getTestDataInJSON(String key, JSONObject storage) {
        if (key.contains(".")) {
            String[] layers = key.split("\\.");
            Object current = storage.get(layers[0]);
            for (int index = 1; index < layers.length - 1; index++) {
                if (current instanceof JSONObject) {
                    current = ((JSONObject) current).get(layers[index]);
                } else {
                    throw new RuntimeException("Check Parameter definition with given KEY. JSONArray has no key for value!");
                }
            }
            if (current instanceof JSONObject) {
                return ((JSONObject) current).get(layers[layers.length - 1]);
            } else if (current instanceof JSONArray && key.endsWith(".values")) {
                return asList(((JSONArray) current).toArray());
            } else {
                throw new RuntimeException("Check Parameter definition with given KEY! No Test Data can be Selected.");
            }
        } else {

            if (storage.containsKey(key)) {
                return storage.get(key);
            } else {
                return key;
            }
        }
    }

    private void loadTestData(String testDataRef) {
        String[] token = testDataRef.split(":");
        if (!"db".equalsIgnoreCase(token[0])) {
            String filePath;
            if (token[1].replace("\\", "/").contains("/")) {
                filePath = FileLocator.findResource(token[1]).toString();
            } else {
                String testDataLocation = FileLocator.findResource(PropertyResolver.getDefaultTestDataLocation()).toString();
                List<Path> filePaths = FileLocator.listRegularFilesRecursiveMatchedToName(testDataLocation, 5, token[1]);
                if (filePaths.size() > 1) {
                    testDataLocation = FileLocator.findResource(PropertyResolver.getTestDataFolder()).toString();
                    filePath = FileLocator.findExactFile(testDataLocation, 5, token[1]).toString();
                } else {
                    filePath = filePaths.get(0).toString();
                }
            }
            SystemLogger.trace("Take test data file: " + filePath);
            if ("file".equalsIgnoreCase(token[0])) {
                loadWithFile(filePath);
            } else if ("sql".equalsIgnoreCase(token[0])) {
                loadDBContent(filePath);
            } else
                throw new RuntimeException("Test Data Reference can not be loaded: no 'File:' ,'SQL:' or 'DB:' header found!");
        } else loadDBDataWith(token[1]);

    }

    private void loadDBDataWith(String className) {
        try {
            DBDataCollector dbDataCollector = (DBDataCollector) Class.forName(className).getDeclaredConstructor().newInstance();
            dataContent = dbDataCollector.getData();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException ex) {
            SystemLogger.info("The given Class Name of DB Data Collector may not correct or it does not implement the required interface 'DBDataCollector'!");
            SystemLogger.error(ex);
        }
    }

    private void loadWithFile(String testDataRef) {
        Pattern pattern = Pattern.compile("\\.(\\w+)$");
        Matcher matcher = pattern.matcher(testDataRef);
        if (matcher.find()) {
            String suffix = matcher.group(1);
            fileFormat = FileFormat.valueOf(suffix.toUpperCase());
            switch (fileFormat) {
                case EXCEL:
                case EXCELX:
                    loadExcelContent(testDataRef);
                    break;
                case CSV:
                    loadCSVContent(testDataRef);
                    break;
                case SQL:
                    loadDBContent(testDataRef);
                    break;
                case XML:
                    loadXMLContent(testDataRef);
                    break;
                case JSON:
                    loadJSONContent(testDataRef);
                    break;
                default:
                    throw new RuntimeException("Test Data Reference can not be loaded: File Format is not supported jet! " + "<" + testDataRef + ">");
            }
        }
    }

    public FileFormat testDataContainerFileFormat() {
        return fileFormat;
    }

    public static void loadGlobalTestData(Path path) {
        String content = FileOperation.readFileToLinedString(path.toString());
        globalTestData = JSONObject.fromObject(content);
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
                SystemLogger.warn("CSV data has no content or no header!");
            } else setRepeat(content.size() != 2);
            parseCSVNonComment(content);
        }
    }

    private void parseCSVNonComment(List<String> lines) {
        String[] colums = lines.get(0).split(";");
        boolean commentBlockClosed = true;
        for (int rowNumber = 1; rowNumber < lines.size(); rowNumber++) {
            Map<String, Object> rowContent = new HashMap<>(colums.length);
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
                if (values.length != colums.length) {
                    throw new RuntimeException("In der Zeilennummer " + rowNumber + " stimmt die Anzahl der Werte nicht mit der Anzahl im CSV Header überrein");
                }
                for (int i = 0; i < colums.length; i++) {
                    rowContent.put(colums[i], values[i]);
                }
                dataContent.add(rowContent);
            }
        }
    }

    private void loadDBContent(String testDataRef) {
        String sql = FileOperation.readFileToLinedString(testDataRef);
        JSONObject config = JSONContainerFactory.getConfig(PropertyResolver.getDBConfigFile());
        dataContent = DBConnector.connectAndExcute(config.getString("type"), config.getString("host"),
                config.getString("user"), config.getString("port"), config.getString("instance.name"), config.getString("password"), sql);
    }

    //todo: define xml content loader
    private void loadXMLContent(String testDataRef) {
        String content = FileOperation.readFileToLinedString(testDataRef);
    }

    //todo: define excel content loader
    private void loadExcelContent(String testDataRef) {
        String content = FileOperation.readFileToLinedString(testDataRef);
    }

    @SuppressWarnings("unchecked")
    private void loadJSONContent(String testDataRef) {
        String content = FileOperation.readFileToLinedString(testDataRef);
        JSONObject data = JSONObject.fromObject(content);
        if (additionalData && jsonData != null) {
            data.forEach((key, value) -> jsonData.putIfAbsent(key, value));
        } else jsonData = data;
    }
}
