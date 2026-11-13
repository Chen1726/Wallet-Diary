package com.walletdiary.app;

import com.walletdiary.dao.ExpenseViewDAO;
import com.walletdiary.model.Expense;

import java.util.List;

public class ViewExpensesApp {
    private static final ExpenseViewDAO expenseViewDAO = new ExpenseViewDAO();

    public static void main(String[] args) {
        System.out.println("Wallet Diary - View Expenses");

        try {
            List<Expense> expenses = expenseViewDAO.getAllExpenses();

            if (expenses.isEmpty()) {
                System.out.println("No expenses found.");
                return;
            }

            System.out.println("-------------------------------------------------------------");
            System.out.printf("%-10s %-12s %-15s %-25s %-12s%n",
                    "Expense ID", "Amount", "Category", "Description", "Date");
            System.out.println("-------------------------------------------------------------");

            for (Expense expense : expenses) {
                System.out.printf("%-10s %-12s %-15s %-25s %-12s%n",
                        expense.getExpenseId(),
                        expense.getAmount(),
                        expense.getCategory(),
                        expense.getDescription(),
                        expense.getExpenseDate());
            }

            System.out.println("-------------------------------------------------------------");
        } catch (Exception e) {
            System.out.println("Database error: Unable to load expenses.");
            e.printStackTrace();
        }
    }
}
