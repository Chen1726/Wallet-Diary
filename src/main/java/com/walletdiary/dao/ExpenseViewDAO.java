package com.walletdiary.dao;

import com.walletdiary.model.Expense;
import com.walletdiary.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ExpenseViewDAO {
    private static final String SELECT_ALL_SQL =
            "SELECT expense_id, amount, category, description, expense_date FROM expenses ORDER BY expense_date DESC";

    public List<Expense> getAllExpenses() throws SQLException, java.io.IOException {
        List<Expense> expenses = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_SQL);
             ResultSet resultSet = preparedStatement.executeQuery()) {

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

        return expenses;
    }
}
