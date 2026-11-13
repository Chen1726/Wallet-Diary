package com.walletdiary.util;

import java.sql.Connection;

public class DatabaseConnectionTest {
    public static void main(String[] args) {
        System.out.println("Testing MySQL connection...");

        try (Connection connection = DatabaseConnection.getConnection()) {
            if (connection != null && !connection.isClosed()) {
                System.out.println("Connection successful!");
                System.out.println("Connected to database: " + connection.getMetaData().getURL());
            } else {
                System.out.println("Connection failed: connection is closed.");
            }
        } catch (Exception e) {
            System.out.println("Connection failed.");
            e.printStackTrace();
        }
    }
}
