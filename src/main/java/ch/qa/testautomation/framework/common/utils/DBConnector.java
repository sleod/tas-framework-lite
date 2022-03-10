package ch.qa.testautomation.framework.common.utils;

import ch.qa.testautomation.framework.common.logging.SystemLogger;

import java.sql.*;
import java.util.*;


/**
 * DB Connector
 * configured only for mysql and oracle now
 */
public class DBConnector {
    /**
     * @deprecated use {@link DBConnector#connectAndExecute(String, String, String, String, String, String, String, boolean)}
     */
    @Deprecated
    public static List<Map<String, Object>> connectAndExcute(String dbType, String host, String user, String port,
                                                             String dbName, String password, String sql) {
        return connectAndExecute(dbType, host, user, port, dbName, password, sql, true);
    }


    /**
     * Connect to DB and execute statement
     *
     * @param dbType    type of db, like mysql, oracle-SID, oracle-SN, mssql
     * @param host      host
     * @param user      username
     * @param port      port
     * @param dbName    dbname or service name or service id
     * @param password  password
     * @param sql       sql statement
     * @param traceInfo true or false to decide showing information or not
     * @return list of map represent the result table
     */
    public static List<Map<String, Object>> connectAndExecute(String dbType, String host, String user, String port,
                                                              String dbName, String password, String sql, boolean traceInfo) {
        Connection connect = null;
        List<Map<String, Object>> results = new ArrayList<>();
        String url = "";
        try {
            switch (dbType) {
                case "mysql":
                    // Load driver
                    Class.forName("com.mysql.jdbc.Driver");
                    url = "jdbc:mysql://" + host;
                    break;
                case "oracle-SID":
                    // Load driver
                    Class.forName("oracle.jdbc.OracleDriver");
                    // Setup the connection of DB with SID
                    url = "jdbc:oracle:thin:@" + host + ":" + port + ":" + dbName;
                    break;
                case "oracle-SN":
                    // Load driver
                    Class.forName("oracle.jdbc.OracleDriver");
                    // Setup the connection of DB with Service Name
                    url = "jdbc:oracle:thin:@" + host + ":" + port + "/" + dbName;
                    break;
                case "mssql":
                    Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                    // Setup the connection with the DB
                    url = "jdbc:sqlserver://" + host + ":" + port + ";databaseName=" + dbName;
                    break;
            }
            if (!url.isEmpty()) {
                SystemLogger.trace("Try to connect to DB: " + url);
                // Setup the connection with the DB
                connect = DriverManager.getConnection(url, user, password);
            } else {
                throw new RuntimeException("Info for Connection DB is not well set!");
            }
            // Statements allow to issue SQL queries to the database
            Statement statement = Objects.requireNonNull(connect).createStatement();
            // Result set get the result of the SQL query
            ResultSet resultSet = statement.executeQuery(sql);
            SystemLogger.trace("Execute SQL: " + sql);
            results = writeResultSet(resultSet, traceInfo);
        } catch (ClassNotFoundException | SQLException e) {
            SystemLogger.error(e);
        } finally {
            if (connect != null) {
                try {
                    connect.close();
                } catch (SQLException e) {
                    SystemLogger.error(e);
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
    private static List<Map<String, Object>> writeResultSet(ResultSet resultSet, boolean traceInfo) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>(resultSet.getRow());
        // ResultSet is initially before the first data set
        while (resultSet.next()) {
            if (traceInfo) {
                SystemLogger.trace("Table Line: ----------------------------------------");
            }
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
                if (traceInfo) {
                    SystemLogger.trace("Column: " + colName + ": " + row.get(colName));
                }
            }
            results.add(row);
        }
        return results;
    }
}
