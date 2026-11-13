-- Create the database
CREATE DATABASE wallet_diary;

-- Use the database
USE wallet_diary;

-- Create the expenses table
CREATE TABLE expenses (
    expense_id INT PRIMARY KEY AUTO_INCREMENT,
    amount DECIMAL(10, 2) NOT NULL,
    category VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    expense_date DATE NOT NULL
);

-- Optional test data (do not run unless you want to test the table)
-- INSERT INTO expenses (amount, category, description, expense_date)
-- VALUES (250.00, 'Food', 'Lunch with team', '2026-10-01');
