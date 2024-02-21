package ch.qa.testautomation.tas.common.utils;

import ch.qa.testautomation.tas.common.IOUtils.FileLocator;
import ch.qa.testautomation.tas.common.IOUtils.FileOperation;
import ch.qa.testautomation.tas.configuration.PropertyResolver;
import ch.qa.testautomation.tas.exception.ExceptionBase;
import ch.qa.testautomation.tas.exception.ExceptionErrorKeys;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.qa.testautomation.tas.common.logging.SystemLogger.debug;
import static ch.qa.testautomation.tas.common.logging.SystemLogger.trace;
import static ch.qa.testautomation.tas.configuration.PropertyResolver.*;


/**
 * DB Connector
 * configured only for mysql and oracle now
 */
public class DBConnector {

    /**
     * get SQL Statement with given content or file name in testdata location
     *
     * @param sqlContentOrFilename content or file name
     * @return SQL Statement
     */
    public static String getSQLStatement(String sqlContentOrFilename) {
        String sqlStatement = sqlContentOrFilename;
        if (sqlContentOrFilename.endsWith(".sql")) {//read sql file
            String location = FileLocator.findResource(PropertyResolver.getTestDataLocation()).toString();
            sqlStatement = FileOperation.readFileToLinedString(FileLocator.findExactFile(location, 5, sqlContentOrFilename).toString());
            if (sqlStatement.isEmpty()) {
                throw new ExceptionBase(ExceptionErrorKeys.CUSTOM_MESSAGE, "Given .sql File can not be found in test data! SQL: " + sqlContentOrFilename);
            }
        } else if (!sqlContentOrFilename.toLowerCase().startsWith("select")) {//
            throw new ExceptionBase(ExceptionErrorKeys.CUSTOM_MESSAGE, "Given Context is neither .sql file nor Select Statement");
        }
        return sqlStatement;
    }

    /**
     * find line with given data in result set
     *
     * @param key       column or key of data
     * @param data      value of data
     * @param resultSet statement or response
     * @return values in map
     */
    public static Map<String, Object> findLineWithDataInResultSet(String key, Object data, List<Map<String, Object>> resultSet) {
        return resultSet.stream().filter(line -> line.get(key).equals(data)).findAny().orElse(Collections.emptyMap());
    }

    public static Map<String, String> normalizeDBData(Map<String, Object> dbData) {
        return dbData.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue())));
    }

    public static Map<String, String> normalizeDBData(Map<String, Object> dbData, String replaceNull) {
        return dbData.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> replaceNull(e.getValue(), replaceNull)));
    }

    private static String replaceNull(Object dbValue, String replacement) {
        return Objects.isNull(dbValue) ? replacement : dbValue.toString();
    }

    public static String formatDate(Object date, String srcFormat, String tarFormat) {
        if (Objects.nonNull(srcFormat) && Objects.nonNull(tarFormat) && !srcFormat.isEmpty() && !tarFormat.isEmpty()) {
            return DateTimeUtils.formatSimpleDate(date.toString(), srcFormat, tarFormat);
        } else {
            return date.toString();
        }
    }

    public static Map<String, Map<String, Object>> fetchDBDataIntoMap(String key, List<Map<String, Object>> dbData) {
        Map<String, Map<String, Object>> dbMap = new HashMap<>(dbData.size());
        dbData.forEach(row -> {
            dbMap.put(row.get(key).toString(), row);
        });
        return dbMap;
    }

    public static Map<String, Map<String, Object>> fetchDBDataIntoMapWithCombKeys(List<Map<String, Object>> dbData, String... keys) {
        Map<String, Map<String, Object>> dbMap = new HashMap<>(dbData.size());
        dbData.forEach(row -> {
            StringBuilder builder = new StringBuilder();
            Stream.of(keys).forEach(key -> builder.append(row.get(key).toString()));
            dbMap.put(builder.toString(), row);
        });
        return dbMap;
    }

    public static Map<String, List<Map<String, Object>>> fetchDBDataIntoMapListWithKey(String key, List<Map<String, Object>> dbData) {
        Map<String, List<Map<String, Object>>> dbMapList = new HashMap<>(dbData.size());
        dbData.forEach(row -> {
            String uniqueKey = String.valueOf(row.get(key));
            if (dbMapList.containsKey(uniqueKey)) {
                dbMapList.get(uniqueKey).add(row);
            } else {
                LinkedList<Map<String, Object>> innerList = new LinkedList<>();
                innerList.add(row);
                dbMapList.put(uniqueKey, innerList);
            }
        });
        return dbMapList;
    }

    public static Map<String, Object> convertKeyTo(Map<String, Object> dataMap, boolean isToLowercase) {
        if (Objects.nonNull(dataMap) && !dataMap.isEmpty()) {
            Map<String, Object> theMap = new HashMap<>(dataMap.size());
            dataMap.forEach((key, value) -> theMap.put(isToLowercase ? key.toLowerCase() : key.toUpperCase(), value));
            return theMap;
        } else {
            return dataMap;
        }
    }


    /**
     * Connect and execute sql in case DB config is well set
     *
     * @param sql sql statement
     * @return table content
     */
    public static List<Map<String, Object>> connectAndExecute(String sql) {
        return connectAndExecute(getDBType(), getDBHost(), getDBUser(), getDBPort(), getDBName(), getDBPassword(), sql);
    }

    /**
     * Connect to DB and execute statement
     *
     * @param dbType   type of db, like mysql, oracle-SID, oracle-SN, mssql
     * @param host     host
     * @param user     username
     * @param port     port
     * @param dbName   dbname or service name or service id
     * @param password password
     * @param sql      sql statement
     * @return list of map represent the result table
     */
    public static List<Map<String, Object>> connectAndExecute(String dbType, String host, String user, String port,
                                                              String dbName, String password, String sql) {
        Connection connect = null;
        List<Map<String, Object>> results;
        String url = "";
        try {
            switch (dbType) {
                case "mysql" -> {
                    // Load driver
                    Class.forName("com.mysql.jdbc.Driver");
                    url = "jdbc:mysql://" + host;
                }
                case "oracle-SID" -> {
                    // Load driver
                    Class.forName("oracle.jdbc.OracleDriver");
                    // Set up the connection of DB with SID
                    url = "jdbc:oracle:thin:@" + host + ":" + port + ":" + dbName;
                }
                case "oracle-SN" -> {
                    // Load driver
                    Class.forName("oracle.jdbc.OracleDriver");
                    // Set up the connection of DB with Service Name
                    url = "jdbc:oracle:thin:@" + host + ":" + port + "/" + dbName;
                }
                case "mssql" -> {
                    Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                    // Set up the connection with the DB
                    url = "jdbc:sqlserver://" + host + ":" + port + ";databaseName=" + dbName +";encrypt=true;trustServerCertificate=true;";
                }
            }
            if (!url.isEmpty()) {
                debug("Try to connect to DB: " + url);
                // Set up the connection with the DB
                connect = DriverManager.getConnection(url, user, PropertyResolver.decodeBase64(password));
            } else {
                throw new ExceptionBase(ExceptionErrorKeys.CONNECTION_TO_DB_IS_NOT_SET_WELL);
            }
            // Statements allow to issue SQL queries to the database
            Statement statement = Objects.requireNonNull(connect).createStatement();
            // Result set get the result of the SQL query
            trace("Execute SQL: " + sql);
            results = writeResultSet(statement.executeQuery(sql));
        } catch (ClassNotFoundException | SQLException ex) {
            throw new ExceptionBase(ExceptionErrorKeys.CUSTOM_MESSAGE, "Exception while connect to DB and execute SQL! " + ex.getMessage());
        } finally {
            if (connect != null) {
                try {
                    connect.close();
                } catch (SQLException ex) {
                    throw new ExceptionBase(ExceptionErrorKeys.CUSTOM_MESSAGE, "Exception by trying to close connection! " + ex.getMessage());
                }
            }
        }
        return results;
    }

    /**
     * use to write db result-set into list of map
     *
     * @param resultSet db result set
     * @return list of rows in map
     * @throws SQLException sql exception
     */
    private static List<Map<String, Object>> writeResultSet(ResultSet resultSet) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>(resultSet.getRow());
        // ResultSet is initially before the first data set
        while (resultSet.next()) {
            trace("Table Line: ----------------------------------------");
            // It is possible to get the columns via name
            // also possible to get the columns via the column number
            // which starts at 1
            // e.g. getResultSet.getString(2);
            ResultSetMetaData rsmd = resultSet.getMetaData();
            int columns = rsmd.getColumnCount();
            LinkedHashMap<String, Object> row = new LinkedHashMap<>(columns);
            for (int i = 1; i <= columns; i++) {
                String colName = rsmd.getColumnName(i);
                row.put(colName, resultSet.getString(colName));
                trace("Column: " + colName + ": " + row.get(colName));
            }
            results.add(row);
        }
        return results;
    }
}
