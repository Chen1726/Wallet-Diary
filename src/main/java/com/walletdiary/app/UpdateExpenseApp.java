package com.walletdiary.app;

import com.walletdiary.dao.ExpenseUpdateDAO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class UpdateExpenseApp {
    private static final ExpenseUpdateDAO expenseUpdateDAO = new ExpenseUpdateDAO();
    private static final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) {
        System.out.println("Wallet Diary - Update Expense");

        try {
            int expenseId = readExpenseId();

            if (!expenseUpdateDAO.exists(expenseId)) {
                System.out.println("Expense not found.");
                return;
            }

            System.out.println("Enter the new values for Expense ID: " + expenseId);

            BigDecimal amount = readAmount();
            String category = readCategory();
            String description = readDescription();
            LocalDate expenseDate = readDate();

            boolean updated = expenseUpdateDAO.updateExpense(expenseId, amount, category, description, expenseDate);
            if (updated) {
                System.out.println("Expense updated successfully.");
                System.out.println("Updated Expense ID: " + expenseId);
            } else {
                System.out.println("Expense not found.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid Expense ID. Please enter a valid number.");
        } catch (IllegalArgumentException e) {
            System.out.println("Input error: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Input error. Please try again.");
        } catch (Exception e) {
            System.out.println("Database error: Unable to update expense.");
            e.printStackTrace();
        }
    }

    private static int readExpenseId() throws IOException {
        System.out.print("Enter Expense ID to update: ");
        String input = reader.readLine().trim();
        return Integer.parseInt(input);
    }

    private static BigDecimal readAmount() throws IOException {
        System.out.print("Enter new amount: ");
        String input = reader.readLine().trim();

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

    private static String readCategory() throws IOException {
        System.out.print("Enter new category: ");
        String category = reader.readLine().trim();

        if (category.isEmpty()) {
            throw new IllegalArgumentException("Category cannot be empty.");
        }

        return category;
    }

    private static String readDescription() throws IOException {
        System.out.print("Enter new description: ");
        return reader.readLine().trim();
    }

    private static LocalDate readDate() throws IOException {
        System.out.print("Enter new expense date (yyyy-MM-dd): ");
        String input = reader.readLine().trim();

        try {
            return LocalDate.parse(input);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Expense date must be a valid date in yyyy-MM-dd format.");
        }
    }
}
