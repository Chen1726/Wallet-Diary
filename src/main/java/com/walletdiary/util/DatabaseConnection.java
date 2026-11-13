package com.walletdiary.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConnection {
    private static final String PROPERTIES_FILE = "db.properties";

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException, IOException {
        Properties properties = new Properties();

        try (InputStream input = DatabaseConnection.class
                .getClassLoader()
                .getResourceAsStream(PROPERTIES_FILE)) {
            if (input == null) {
                throw new IOException("Configuration file not found: " + PROPERTIES_FILE);
            }
            properties.load(input);
        }

        String url = properties.getProperty("db.url");
        String user = properties.getProperty("db.user");
        String password = System.getenv("DB_PASSWORD");

        if (password == null || password.isBlank()) {
            password = properties.getProperty("db.password");
        }

        if (url == null || user == null || password == null) {
            throw new SQLException("Missing database configuration in " + PROPERTIES_FILE);
        }

        return DriverManager.getConnection(url, user, password);
    }
}
