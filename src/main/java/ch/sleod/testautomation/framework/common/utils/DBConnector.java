package ch.sleod.testautomation.framework.common.utils;

import java.sql.*;
import java.util.*;

import static ch.sleod.testautomation.framework.common.logging.SystemLogger.error;
import static ch.sleod.testautomation.framework.common.logging.SystemLogger.trace;


/**
 * DB Connector
 * configured only for mysql and oracle now
 */
public class DBConnector {

    public static List<Map<String, Object>> connectAndExcute(String dbType, String host, String user, String port, String dbName, String password, String sql) {
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
                case "oracle":
                    // Load driver
                    Class.forName("oracle.jdbc.OracleDriver");
                    // Setup the connection with the DB
                    url = "jdbc:oracle:thin:@" + host + ":" + port + ":" + dbName;
                    break;
                case "mssql":
                    Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                    // Setup the connection with the DB
                    url = "jdbc:sqlserver://" + host + ":" + port + ";databaseName=" + dbName;
                    break;
            }
            if (!url.isEmpty()) {
                trace("Try to connect to DB: " + url);
                // Setup the connection with the DB
                connect = DriverManager.getConnection(url, user, password);
            } else {
                throw new RuntimeException("Info for Connection DB is not well set!");
            }
            // Statements allow to issue SQL queries to the database
            Statement statement = Objects.requireNonNull(connect).createStatement();
            // Result set get the result of the SQL query
            ResultSet resultSet = statement.executeQuery(sql);
            trace("Execute SQL: " + sql);
            results = writeResultSet(resultSet);
        } catch (ClassNotFoundException | SQLException e) {
            error(e);
        } finally {
            if (connect != null) {
                try {
                    connect.close();
                } catch (SQLException e) {
                    error(e);
                }
            }
        }
        return results;
    }

    /**
     * use to write db resultset into list of map
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
