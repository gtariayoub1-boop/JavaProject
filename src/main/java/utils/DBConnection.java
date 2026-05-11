package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 3306;
    private static final String DEFAULT_DATABASE = "gestion_utilisateur";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    private static DBConnection instance;
    private Connection connection;
    private final String host;
    private final int port;
    private final String database;
    private final String user;
    private final String password;
    private final String url;

    private DBConnection() {
        this.host = AppSecrets.get("db.host").isBlank() ? DEFAULT_HOST : AppSecrets.get("db.host");
        this.port = AppSecrets.getInt("db.port", DEFAULT_PORT);
        this.database = AppSecrets.get("db.name").isBlank() ? DEFAULT_DATABASE : AppSecrets.get("db.name");
        this.user = AppSecrets.get("db.user").isBlank() ? DEFAULT_USER : AppSecrets.get("db.user");
        this.password = AppSecrets.get("db.password").isBlank() ? DEFAULT_PASSWORD : AppSecrets.get("db.password");
        this.url =
                "jdbc:mysql://" + host + ":" + port + "/" + database
                        + "?useUnicode=true"
                        + "&characterEncoding=utf8"
                        + "&useSSL=false"
                        + "&allowPublicKeyRetrieval=true"
                        + "&serverTimezone=UTC";
        connect();
    }

    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    private void connect() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, user, password);
                System.out.println("Database connection established successfully.");
            }
        } catch (SQLException e) {
            System.err.println("Failed to connect to database (" + host + ":" + port + "/" + database + "): " + e.getMessage());
            connection = null;
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            System.err.println("Error checking connection status: " + e.getMessage());
            connection = null;
        }
        return connection;
    }

    public static boolean verifyConnection() {
        try {
            Connection connection = getInstance().getConnection();
            if (connection == null) {
                System.err.println("Database connection is null");
                return false;
            }
            boolean isValid = connection.isValid(2);
            if (isValid) {
                System.out.println("Database connection is valid");
            } else {
                System.err.println("Database connection is not valid");
            }
            return isValid;
        } catch (SQLException e) {
            System.err.println("Database connection verification failed: " + e.getMessage());
            return false;
        }
    }

    public static String getDatabaseUrl() {
        return getInstance().url;
    }

    public static void closeConnection() {
        DBConnection instance = getInstance();
        if (instance.connection == null) {
            return;
        }

        try {
            if (!instance.connection.isClosed()) {
                instance.connection.close();
                System.out.println("Database connection closed successfully.");
            }
        } catch (SQLException e) {
            System.err.println("Error while closing database connection: " + e.getMessage());
        }
    }
}
