package util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * ABSTRACTION + SINGLETON PATTERN:
 * DatabaseConnection hides the complexity of managing JDBC connections.
 * Only one instance exists throughout the application lifecycle.
 */
public class DatabaseConnection {

    // ENCAPSULATION: private static instance
    private static DatabaseConnection instance;
    private Connection connection;

    private String url;
    private String username;
    private String password;

    /**
     * ENCAPSULATION: Private constructor prevents direct instantiation.
     * Loads database config from database.properties file.
     */
    private DatabaseConnection() {
        loadProperties();
    }

    /**
     * ABSTRACTION: Hides the loading of properties from the caller.
     */
    private void loadProperties() {
        Properties props = new Properties();
        try (InputStream in = getClass().getResourceAsStream("/config/database.properties")) {
            if (in == null) {
                throw new RuntimeException("Cannot find database.properties in classpath");
            }
            props.load(in);
            url      = props.getProperty("db.url");
            username = props.getProperty("db.username");
            password = props.getProperty("db.password");
            String driver = props.getProperty("db.driver");
            Class.forName(driver);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to load database configuration: " + e.getMessage(), e);
        }
    }

    /**
     * SINGLETON: Returns the single shared instance.
     * Thread-safe double-checked locking.
     */
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    /**
     * ABSTRACTION: Returns a live connection, reconnecting if closed.
     */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, username, password);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Cannot connect to database: " + e.getMessage(), e);
        }
        return connection;
    }
}
