package com.walletdiary.dao;

import com.walletdiary.model.Expense;
import com.walletdiary.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ExpenseSearchDAO {
    private static final String SEARCH_BY_CATEGORY_SQL =
            "SELECT expense_id, amount, category, description, expense_date FROM expenses WHERE category = ? ORDER BY expense_date DESC";

    public List<Expense> searchByCategory(String category) throws SQLException, java.io.IOException {
        List<Expense> expenses = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SEARCH_BY_CATEGORY_SQL)) {

            preparedStatement.setString(1, category);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Expense expense = new Expense();
                    expense.setExpenseId(resultSet.getInt("expense_id"));
                    expense.setAmount(resultSet.getBigDecimal("amount"));
                    expense.setCategory(resultSet.getString("category"));
                    expense.setDescription(resultSet.getString("description"));
                    expense.setExpenseDate(resultSet.getDate("expense_date").toLocalDate());
                    expenses.add(expense);
                }
            }
        }

        return expenses;
    }
}
