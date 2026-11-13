package com.walletdiary.dao;

import com.walletdiary.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ExpenseUpdateDAO {
    private static final String CHECK_EXISTS_SQL =
            "SELECT expense_id FROM expenses WHERE expense_id = ?";

    private static final String UPDATE_SQL =
            "UPDATE expenses SET amount = ?, category = ?, description = ?, expense_date = ? WHERE expense_id = ?";

    public boolean exists(int expenseId) throws SQLException, java.io.IOException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(CHECK_EXISTS_SQL)) {

            preparedStatement.setInt(1, expenseId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public boolean updateExpense(int expenseId, java.math.BigDecimal amount, String category,
                                String description, java.time.LocalDate expenseDate)
            throws SQLException, java.io.IOException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_SQL)) {

            preparedStatement.setBigDecimal(1, amount);
            preparedStatement.setString(2, category);
            preparedStatement.setString(3, description);
            preparedStatement.setDate(4, java.sql.Date.valueOf(expenseDate));
            preparedStatement.setInt(5, expenseId);

            int rowsUpdated = preparedStatement.executeUpdate();
            return rowsUpdated > 0;
        }
    }
}
