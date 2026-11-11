package com.smartfoodmess.util;

import com.smartfoodmess.exception.DatabaseException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * The ONE central place for database configuration and connections.
 *
 * <p>Configuration is read once from {@code db.properties} (on the
 * classpath, packaged inside the WAR). Environment variables can override
 * the file so that real passwords never have to be committed to Git:</p>
 *
 * <pre>
 *   SFM_DB_URL       e.g. jdbc:mysql://localhost:3306/smart_food_mess
 *   SFM_DB_USER      e.g. root
 *   SFM_DB_PASSWORD  your MySQL password
 * </pre>
 *
 * <p>Every DAO simply calls {@link #getConnection()} - no other class knows
 * anything about URLs, drivers or credentials.</p>
 */
public final class DBConnection {

    private static final String CONFIG_RESOURCE = "/db.properties";

    private static String url;
    private static String user;
    private static String password;

    static {
        loadConfiguration();
    }

    private DBConnection() {
        // utility class - cannot be instantiated
    }

    /** Reads db.properties once and applies environment variable overrides. */
    private static void loadConfiguration() {
        Properties props = new Properties();
        try (InputStream in = DBConnection.class.getResourceAsStream(CONFIG_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException(
                        "db.properties not found on the classpath (expected at " + CONFIG_RESOURCE + ").");
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read db.properties configuration file.", e);
        }

        String defaultUrl = "jdbc:mysql://localhost:3306/smart_food_mess"
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        url = fromEnvironmentOr("SFM_DB_URL", props.getProperty("db.url", defaultUrl));
        user = fromEnvironmentOr("SFM_DB_USER", props.getProperty("db.user", "root"));
        password = fromEnvironmentOr("SFM_DB_PASSWORD", props.getProperty("db.password", ""));
    }

    private static String fromEnvironmentOr(String envName, String fallback) {
        String value = System.getenv(envName);
        if (value != null && !value.isBlank()) {
            return value;
        }
        return fallback;
    }

    /**
     * Opens a new database connection using the central configuration.
     *
     * @throws DatabaseException if the database cannot be reached. The
     *         message is user friendly; the original SQL error is kept as
     *         the cause and printed to the server log.
     */
    public static Connection getConnection() throws DatabaseException {
        try {
            // Driver auto-registers itself through the JDBC SPI (mysql-connector-j)
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            // Log the technical detail for developers...
            System.err.println("[DBConnection] Connection failed: " + e.getMessage());
            // ...but only show a friendly message to the user.
            throw new DatabaseException(
                    "Could not connect to the database. Please make sure MySQL is running "
                            + "and the settings in db.properties are correct.", e);
        }
    }

    // ---- read-only access for diagnostics -------------------------------

    public static String getUrl() {
        return url;
    }

    public static String getUser() {
        return user;
    }
}
