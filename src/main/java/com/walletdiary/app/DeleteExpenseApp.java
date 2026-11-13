package com.walletdiary.app;

import com.walletdiary.dao.ExpenseDeleteDAO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DeleteExpenseApp {
    private static final ExpenseDeleteDAO expenseDeleteDAO = new ExpenseDeleteDAO();

    public static void main(String[] args) {
        System.out.println("Wallet Diary - Delete Expense");

        try {
            int expenseId = readExpenseId();

            if (!expenseDeleteDAO.exists(expenseId)) {
                System.out.println("Expense not found.");
                return;
            }

            boolean deleted = expenseDeleteDAO.deleteExpense(expenseId);
            if (deleted) {
                System.out.println("Expense deleted successfully.");
                System.out.println("Deleted Expense ID: " + expenseId);
            } else {
                System.out.println("Expense not found.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid Expense ID. Please enter a valid number.");
        } catch (IOException e) {
            System.out.println("Input error. Please try again.");
        } catch (Exception e) {
            System.out.println("Database error: Unable to delete expense.");
            e.printStackTrace();
        }
    }

    private static int readExpenseId() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Enter Expense ID to delete: ");
        String input = reader.readLine().trim();
        return Integer.parseInt(input);
    }
}
