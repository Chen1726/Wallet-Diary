package com.walletdiary.dao;

import com.walletdiary.model.Expense;
import com.walletdiary.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ExpenseDAO {
    private static final String INSERT_SQL =
            "INSERT INTO expenses (amount, category, description, expense_date) VALUES (?, ?, ?, ?)";

    public int addExpense(Expense expense) throws SQLException, java.io.IOException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_SQL,
                     PreparedStatement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setBigDecimal(1, expense.getAmount());
            preparedStatement.setString(2, expense.getCategory());
            preparedStatement.setString(3, expense.getDescription());
            preparedStatement.setDate(4, java.sql.Date.valueOf(expense.getExpenseDate()));

            int rowsInserted = preparedStatement.executeUpdate();
            if (rowsInserted == 0) {
                throw new SQLException("Expense insert failed. No rows were affected.");
            }

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }

            throw new SQLException("Expense insert failed. No generated ID was returned.");
        }
    }
}
