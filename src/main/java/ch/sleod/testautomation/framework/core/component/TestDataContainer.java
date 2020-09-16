package ch.sleod.testautomation.framework.core.component;

import ch.sleod.testautomation.framework.common.IOUtils.FileLocator;
import ch.sleod.testautomation.framework.common.IOUtils.FileOperation;
import ch.sleod.testautomation.framework.common.enumerations.FileFormat;
import ch.sleod.testautomation.framework.common.logging.SystemLogger;
import ch.sleod.testautomation.framework.common.utils.DBConnector;
import ch.sleod.testautomation.framework.configuration.PropertyResolver;
import ch.sleod.testautomation.framework.core.json.deserialization.JSONContainerFactory;
import ch.sleod.testautomation.framework.intefaces.DBDataCollector;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ch.sleod.testautomation.framework.common.logging.SystemLogger.error;
import static ch.sleod.testautomation.framework.common.logging.SystemLogger.info;
import static java.util.Arrays.asList;

public class TestDataContainer {

    private final Map<Class<?>, Object> instances = new LinkedHashMap<>();
    private JSONObject jsonData;
    private FileFormat fileFormat;
    private List<Map<String, Object>> dataContent;
    private boolean repeat = false;
    private static JSONObject globalTestData;
    private static final Map<String, Object> tempData = new LinkedHashMap<>();

    public TestDataContainer(String testDataRef) {
        loadTestData(testDataRef);
    }

    public TestDataContainer(List<Map<String, Object>> dataContent) {
        this.dataContent = dataContent;
    }

    public static Object getTempData(String key) {
        return tempData.get(key);
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
        return dataContent;
    }

    /**
     * get object of global test data
     *
     * @param key to name
     * @return object can be null
     */
    public static Object getGlobalTestData(String key) {
        return globalTestData.get(key);
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
            if (jsonData != null) {
                return getTestDataInJSON(key, jsonData);
            } else if (dataContent != null) {
                return dataContent.get(parameterRow).get(key);
            } else {
                return key;
            }
        }
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
            return storage.get(key);
        }
    }

    private void loadTestData(String testDataRef) {
        if (testDataRef.toLowerCase().contains("file:")) {
            loadWithFile(FileLocator.findResource(testDataRef.substring(5)).toString());
        } else if (testDataRef.toLowerCase().contains("sql:")) {
            loadDBContent(FileOperation.readFileToLinedString(FileLocator.findResource(testDataRef.substring(4)).toString()));
        } else if (testDataRef.toLowerCase().contains("db:")) {
            loadDBDataWith(testDataRef.substring(3));
        } else {
            throw new RuntimeException("Test Data Reference can not be loaded: no 'File:' ,'SQL:' or 'DB:' header found!");
        }
    }

    private void loadDBDataWith(String className) {
        try {
            DBDataCollector dbDataCollector = (DBDataCollector) Class.forName(className).newInstance();
            dataContent = dbDataCollector.getData();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
            info("The given Class Name of DB Data Collector may not correct or it does not implement the required interface 'DBDataCollector'!");
            error(ex);
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
                    loadDBContent(FileOperation.readFileToLinedString(testDataRef));
                    break;
                case XML:
                    loadXMLContent(testDataRef);
                    break;
                case JSON:
                    loadJSONContent(testDataRef);
                    break;
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
            } else if (content.size() == 2) {
                setRepeat(false);
            } else {
                setRepeat(true);
            }
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
                for (int i = 0; i < colums.length; i++) {
                    rowContent.put(colums[i], values[i]);
                }
                dataContent.add(rowContent);
            }
        }
    }

    private void loadDBContent(String sqlStatement) {
        JSONObject config = JSONContainerFactory.getConfig(PropertyResolver.getDBConfigFile());
        dataContent = DBConnector.connectAndExcute(config.getString("type"), config.getString("host"),
                config.getString("user"), config.getString("port"), config.getString("instance.name"), config.getString("password"), sqlStatement);
    }

    //todo: define xml content loader
    private void loadXMLContent(String testDataRef) {
        String content = FileOperation.readFileToLinedString(testDataRef);
    }

    //todo: define excel content loader
    private void loadExcelContent(String testDataRef) {
        String content = FileOperation.readFileToLinedString(testDataRef);
    }

    private void loadJSONContent(String testDataRef) {
        String content = FileOperation.readFileToLinedString(testDataRef);
        jsonData = JSONObject.fromObject(content);
    }
}
