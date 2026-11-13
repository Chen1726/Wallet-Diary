package com.walletdiary.app;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class WalletDiaryApp {
    public static void main(String[] args) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                System.out.println();
                System.out.println("====================================");
                System.out.println("Wallet Diary - Personal Expense Tracking");
                System.out.println("====================================");
                System.out.println("1. Add Expense");
                System.out.println("2. View Expenses");
                System.out.println("3. Update Expense");
                System.out.println("4. Delete Expense");
                System.out.println("5. Search Expense");
                System.out.println("6. Expense Summary");
                System.out.println("7. Exit");
                System.out.println("====================================");
                System.out.print("Enter your choice: ");

                String input = reader.readLine();

                if (input == null) {
                    System.out.println("Goodbye!");
                    break;
                }

                String trimmed = input.trim();

                switch (trimmed) {
                    case "1":
                        ExpenseApp.main(new String[0]);
                        break;
                    case "2":
                        ViewExpensesApp.main(new String[0]);
                        break;
                    case "3":
                        UpdateExpenseApp.main(new String[0]);
                        break;
                    case "4":
                        DeleteExpenseApp.main(new String[0]);
                        break;
                    case "5":
                        SearchExpenseApp.main(new String[0]);
                        break;
                    case "6":
                        ExpenseSummaryApp.main(new String[0]);
                        break;
                    case "7":
                        System.out.println("Goodbye!");
                        return;
                    default:
                        System.out.println("Invalid choice. Please enter a number from 1 to 7.");
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println("Application error. Please try again.");
            e.printStackTrace();
        }
    }
}
