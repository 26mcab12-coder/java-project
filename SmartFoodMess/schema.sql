-- =====================================================================
-- Smart Food Mess Management System - Phase 1 Database Schema
-- Database: mess_db
-- Run with:  mysql -u root -p < schema.sql
-- =====================================================================

CREATE DATABASE IF NOT EXISTS mess_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE mess_db;

-- ---------------------------------------------------------------------
-- users table: holds ADMIN and STUDENT accounts
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    user_id    INT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100)        NOT NULL,
    email      VARCHAR(150)        NOT NULL UNIQUE,
    password   VARCHAR(255)        NOT NULL,
    role       ENUM('ADMIN', 'STUDENT') NOT NULL,
    created_at TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- menu table: weekly meal plan
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS menu (
    menu_id     INT AUTO_INCREMENT PRIMARY KEY,
    day_of_week VARCHAR(20)                        NOT NULL,
    meal_type   ENUM('BREAKFAST', 'LUNCH', 'DINNER') NOT NULL,
    items       TEXT                               NOT NULL,
    price       DECIMAL(10,2)                      NOT NULL DEFAULT 0.00,
    CONSTRAINT uq_menu_slot UNIQUE (day_of_week, meal_type)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- meal_records table: student meal opt-in/opt-out/consumed tracking
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS meal_records (
    record_id  INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT                                   NOT NULL,
    meal_date  DATE                                  NOT NULL,
    meal_type  ENUM('BREAKFAST', 'LUNCH', 'DINNER')  NOT NULL,
    status     ENUM('OPTED_IN', 'OPTED_OUT', 'CONSUMED') NOT NULL,
    expense    DECIMAL(10,2)                         NULL,
    CONSTRAINT fk_meal_records_student
        FOREIGN KEY (student_id) REFERENCES users (user_id)
        ON DELETE CASCADE,
    CONSTRAINT uq_student_meal UNIQUE (student_id, meal_date, meal_type)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Initial test data
-- ---------------------------------------------------------------------
INSERT INTO users (name, email, password, role) VALUES
    ('Admin User', 'admin@mess.com', 'admin123', 'ADMIN'),
    ('John Doe',   'john@student.com', 'student123', 'STUDENT');

INSERT INTO menu (day_of_week, meal_type, items, price) VALUES
    ('MONDAY', 'BREAKFAST', 'Idli, Sambar, Coconut Chutney, Filter Coffee', 45.00),
    ('MONDAY', 'LUNCH',     'Steamed Rice, Dal, Mixed Vegetable Curry, Curd, Pickle', 120.00),
    ('MONDAY', 'DINNER',    'Chapati, Paneer Butter Masala, Salad, Gulab Jamun', 160.00),
    ('TUESDAY', 'BREAKFAST', 'Masala Dosa, Vada, Chutney, Tea', 60.00),
    ('TUESDAY', 'LUNCH',     'Jeera Rice, Rajma Masala, Boondi Raita, Sweet Pongal', 140.00),
    ('TUESDAY', 'DINNER',    'Roti, Chicken Curry / Paneer Curry, Rice, Salad', 180.00);

INSERT INTO meal_records (student_id, meal_date, meal_type, status) VALUES
    (2, CURDATE(), 'BREAKFAST', 'OPTED_IN'),
    (2, CURDATE(), 'LUNCH',     'CONSUMED');