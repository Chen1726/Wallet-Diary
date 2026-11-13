package com.walletdiary.app;

import com.walletdiary.dao.ExpenseSearchDAO;
import com.walletdiary.model.Expense;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

public class SearchExpenseApp {
    private static final ExpenseSearchDAO expenseSearchDAO = new ExpenseSearchDAO();

    public static void main(String[] args) {
        System.out.println("Wallet Diary - Search Expense");

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Enter category to search: ");
            String category = reader.readLine().trim();

            if (category.isEmpty()) {
                System.out.println("Category cannot be empty.");
                return;
            }

            List<Expense> expenses = expenseSearchDAO.searchByCategory(category);

            if (expenses.isEmpty()) {
                System.out.println("No expenses found for this category.");
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
            System.out.println("Database error: Unable to search expenses.");
            e.printStackTrace();
        }
    }
}
