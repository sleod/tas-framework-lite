package io.github.sleod.tas.configuration;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.sleod.tas.common.IOUtils.FileOperation;
import io.github.sleod.tas.core.json.container.JSONRunnerConfig;
import io.github.sleod.tas.core.json.deserialization.JSONContainerFactory;
import io.github.sleod.tas.exception.ExceptionBase;
import io.github.sleod.tas.exception.ExceptionErrorKeys;

public class ConfigFileManager {

    /**
     * per Set Methode to define current DB ID to get proper config of DB
     * usage example: ConfigFileManager.setDB_ID("DEV_DB_1");
     */
    private final static ThreadLocal<String> DB_ID = new InheritableThreadLocal<>();

    /**
     * per Set Methode to define current REST Endpoint ID to get proper config
     * usage example: ConfigFileManager.setREST_ID("DEV_DB_1");
     */
    private final static ThreadLocal<String> REST_ID = new InheritableThreadLocal<>();


    public static String getDB_ID() {
        if (DB_ID.get() == null) {
            DB_ID.set("DEFAULT");
        }
        return DB_ID.get();
    }

    public static String getREST_ID() {
        if (REST_ID.get() == null) {
            REST_ID.set("DEFAULT");
        }
        return REST_ID.get();
    }

    public static void setDB_ID(String dbId) {
        DB_ID.set(dbId);
    }

    public static void setREST_ID(String restId) {
        REST_ID.set(restId);
    }

    /**
     * @return JSONRunnerConfig for JIRA Execution Config
     */
    public static JSONRunnerConfig getJiraExecutionConfig() {
        return getExecutionConfigFile(PropertyResolver.getJiraExecutionConfigFile());
    }

    /**
     * @return Root Rest Config Content as JsonNode
     */
    public static JsonNode getRestConfig() {
        return getConfigFile(PropertyResolver.getRestConfigFile());
    }

    /**
     * @return Root JIRA Config Content as JsonNode
     */
    public static JsonNode getJiraConfig() {
        return getConfigFile(PropertyResolver.getJiraConfigFile());
    }

    /**
     * @return Root JIRA Config Content as JsonNode
     */
    public static JsonNode getDriverDownloadConfig() {
        return getConfigFile(PropertyResolver.getDriverDownloadConfigFile());
    }

    /**
     * @return Root QC Config Content as JsonNode
     */
    public static JsonNode getQCConfig() {
        return getConfigFile(PropertyResolver.getQCConfigFile());
    }

    /**
     * @return Root Proxy Config Content as JsonNode
     */
    public static JsonNode getProxyConfig() {
        return getConfigFile(PropertyResolver.getProxyConfig());
    }

    /**
     * @return Root DB Config Content as JsonNode
     */
    public static JsonNode getDBConfig() {
        return getConfigFile(PropertyResolver.getDBConfigFile());
    }


    private static JsonNode getConfigFile(String fileName) {
        if (!FileOperation.isFileExists(fileName)) {
            throw new ExceptionBase(ExceptionErrorKeys.CONFIG_FILE_NOT_FOUND, fileName);
        }
        return JSONContainerFactory.getConfig(fileName);
    }

    private static JSONRunnerConfig getExecutionConfigFile(String fileName) {
        if (!FileOperation.isFileExists(fileName)) {
            throw new ExceptionBase(ExceptionErrorKeys.CONFIG_FILE_NOT_FOUND, fileName);
        }
        return JSONContainerFactory.getRunnerConfig(fileName);
    }
}
