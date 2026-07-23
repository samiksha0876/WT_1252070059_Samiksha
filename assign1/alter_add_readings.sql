-- Run this ONLY if you already created the "bills" table before
-- (i.e. it doesn't yet have previous_reading / current_reading columns).
-- If you are setting up the database for the first time, just use
-- database.sql instead — you do NOT need this file.

USE electricity_billing;

ALTER TABLE bills
    ADD COLUMN previous_reading INT NOT NULL DEFAULT 0 AFTER consumer_no,
    ADD COLUMN current_reading INT NOT NULL DEFAULT 0 AFTER previous_reading;
