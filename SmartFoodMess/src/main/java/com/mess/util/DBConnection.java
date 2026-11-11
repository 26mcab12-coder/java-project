package com.mess.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Creates JDBC connections to the mess_db MySQL database. */
public final class DBConnection {

    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DB_URL =
            System.getenv().getOrDefault("MESS_DB_URL",
                    "jdbc:mysql://localhost:3306/mess_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
    private static final String DB_USER = System.getenv().getOrDefault("MESS_DB_USER", "root");
    private static final String DB_PASSWORD = System.getenv().getOrDefault("MESS_DB_PASSWORD", "MySQL@123");

    private DBConnection() {
        // Prevent instantiation outside this class.
    }

    /**
     * Opens a connection for one DAO operation. Callers own and close the
     * returned connection, keeping concurrent web requests isolated.
     *
     * @return an open JDBC Connection
     * @throws ClassNotFoundException if the MySQL JDBC driver is not on the classpath
     * @throws SQLException           if the connection cannot be established
     */
    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName(JDBC_DRIVER);
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}