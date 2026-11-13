package com.walletdiary.dao;

import com.walletdiary.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ExpenseDeleteDAO {
    private static final String CHECK_EXISTS_SQL =
            "SELECT expense_id FROM expenses WHERE expense_id = ?";

    private static final String DELETE_SQL =
            "DELETE FROM expenses WHERE expense_id = ?";

    public boolean exists(int expenseId) throws SQLException, java.io.IOException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(CHECK_EXISTS_SQL)) {

            preparedStatement.setInt(1, expenseId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public boolean deleteExpense(int expenseId) throws SQLException, java.io.IOException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(DELETE_SQL)) {

            preparedStatement.setInt(1, expenseId);
            int rowsDeleted = preparedStatement.executeUpdate();
            return rowsDeleted > 0;
        }
    }
}
