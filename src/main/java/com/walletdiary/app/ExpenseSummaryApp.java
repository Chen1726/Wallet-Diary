package com.walletdiary.app;

import com.walletdiary.dao.ExpenseSummaryDAO;

import java.math.BigDecimal;

public class ExpenseSummaryApp {
    private static final ExpenseSummaryDAO expenseSummaryDAO = new ExpenseSummaryDAO();

    public static void main(String[] args) {
        System.out.println("Wallet Diary - Expense Summary");

        try {
            ExpenseSummaryDAO.ExpenseSummary summary = expenseSummaryDAO.getSummary();

            long totalExpenses = summary.getTotalExpenses();
            BigDecimal totalAmount = summary.getTotalAmount();

            if (totalExpenses == 0) {
                System.out.println("No expenses found.");
                System.out.println("Total amount spent: 0.00");
                return;
            }

            System.out.println("Total number of expenses: " + totalExpenses);
            System.out.println("Total amount spent: " + totalAmount.setScale(2, java.math.RoundingMode.HALF_UP));
        } catch (Exception e) {
            System.out.println("Database error: Unable to calculate expense summary.");
            e.printStackTrace();
        }
    }
}
