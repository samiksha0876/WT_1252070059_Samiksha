-- Run this file in phpMyAdmin (SQL tab) or via MySQL command line
-- It creates the database and the table used by the billing app.

CREATE DATABASE IF NOT EXISTS electricity_billing;
USE electricity_billing;

CREATE TABLE IF NOT EXISTS bills (
    id INT AUTO_INCREMENT PRIMARY KEY,
    consumer_name VARCHAR(100) NOT NULL,
    consumer_no VARCHAR(50) NOT NULL,
    units_consumed INT NOT NULL,
    bill_amount DECIMAL(10,2) NOT NULL,
    bill_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
