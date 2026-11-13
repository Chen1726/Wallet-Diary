package com.walletdiary.app;

import com.walletdiary.dao.ExpenseDAO;
import com.walletdiary.model.Expense;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class ExpenseApp {
    private static final ExpenseDAO expenseDAO = new ExpenseDAO();

    public static void main(String[] args) {
        System.out.println("Wallet Diary - Add Expense");

        try {
            Expense expense = readExpenseFromConsole();
            int generatedId = expenseDAO.addExpense(expense);

            System.out.println("Expense added successfully!");
            System.out.println("Generated Expense ID: " + generatedId);
        } catch (IllegalArgumentException e) {
            System.out.println("Input error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Database error: Unable to add expense. Please check your MySQL connection and table.");
            e.printStackTrace();
        }
    }

    private static Expense readExpenseFromConsole() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.print("Enter amount: ");
        String amountInput = reader.readLine().trim();
        BigDecimal amount = parseAmount(amountInput);

        System.out.print("Enter category: ");
        String category = reader.readLine().trim();
        if (category.isEmpty()) {
            throw new IllegalArgumentException("Category cannot be empty.");
        }

        System.out.print("Enter description: ");
        String description = reader.readLine().trim();

        System.out.print("Enter expense date (yyyy-MM-dd): ");
        String dateInput = reader.readLine().trim();
        LocalDate expenseDate = parseDate(dateInput);

        return new Expense(amount, category, description, expenseDate);
    }

    private static BigDecimal parseAmount(String input) {
        try {
            BigDecimal amount = new BigDecimal(input);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Amount must be a positive number.");
            }
            return amount;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Amount must be a valid positive number.");
        }
    }

    private static LocalDate parseDate(String input) {
        try {
            return LocalDate.parse(input);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Expense date must be a valid date in yyyy-MM-dd format.");
        }
    }
}
