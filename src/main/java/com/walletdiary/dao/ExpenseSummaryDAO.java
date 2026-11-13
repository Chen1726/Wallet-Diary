package com.walletdiary.dao;

import com.walletdiary.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ExpenseSummaryDAO {
    private static final String SUMMARY_SQL =
            "SELECT COUNT(*) AS total_expenses, COALESCE(SUM(amount), 0) AS total_amount FROM expenses";

    public ExpenseSummary getSummary() throws SQLException, java.io.IOException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SUMMARY_SQL);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            if (resultSet.next()) {
                long totalExpenses = resultSet.getLong("total_expenses");
                BigDecimal totalAmount = resultSet.getBigDecimal("total_amount");
                return new ExpenseSummary(totalExpenses, totalAmount);
            }
        }

        return new ExpenseSummary(0, BigDecimal.ZERO);
    }

    public static class ExpenseSummary {
        private final long totalExpenses;
        private final BigDecimal totalAmount;

        public ExpenseSummary(long totalExpenses, BigDecimal totalAmount) {
            this.totalExpenses = totalExpenses;
            this.totalAmount = totalAmount;
        }

        public long getTotalExpenses() {
            return totalExpenses;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }
    }
}
