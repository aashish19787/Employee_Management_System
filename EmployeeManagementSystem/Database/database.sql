-- ============================================================
-- Employee Management System - Database Schema
-- ============================================================

CREATE DATABASE IF NOT EXISTS employee_management_system_db;
USE employee_management_system_db;

-- Drop tables in reverse order of dependency
DROP TABLE IF EXISTS attendance;
DROP TABLE IF EXISTS tasks;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS employees;

-- ============================================================
-- TABLE: employees
-- ============================================================
CREATE TABLE employees (
    employee_id   INT AUTO_INCREMENT PRIMARY KEY,
    first_name    VARCHAR(50)  NOT NULL,
    middle_name   VARCHAR(50),
    last_name     VARCHAR(50)  NOT NULL,
    date_of_birth DATE         NOT NULL,
    contact_no    CHAR(10)     NOT NULL UNIQUE,
    email         VARCHAR(100) NOT NULL UNIQUE,
    address       TEXT         NOT NULL,
    position      VARCHAR(100) NOT NULL,
    department    VARCHAR(100) NOT NULL
);

-- ============================================================
-- TABLE: users
-- ============================================================
CREATE TABLE users (
    user_id     INT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    user_type   ENUM('ADMIN','EMPLOYEE') NOT NULL,
    employee_id INT,
    is_active   BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_users_employee FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
);

-- ============================================================
-- TABLE: tasks
-- ============================================================
CREATE TABLE tasks (
    task_id     INT AUTO_INCREMENT PRIMARY KEY,
    task_name   VARCHAR(100) NOT NULL,
    description TEXT         NOT NULL,
    status      ENUM('Pending','In Progress','Completed') NOT NULL DEFAULT 'Pending',
    employee_id INT NOT NULL,
    CONSTRAINT fk_tasks_employee FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
);

-- ============================================================
-- TABLE: attendance
-- ============================================================
CREATE TABLE attendance (
    attendance_id   INT AUTO_INCREMENT PRIMARY KEY,
    employee_id     INT  NOT NULL,
    attendance_date DATE NOT NULL,
    status          ENUM('Present','Leave','Late','Absent') NOT NULL,
    remarks         VARCHAR(255),
    CONSTRAINT fk_attendance_employee FOREIGN KEY (employee_id) REFERENCES employees(employee_id),
    CONSTRAINT uq_attendance UNIQUE (employee_id, attendance_date)
);

-- ============================================================
-- SAMPLE DATA
-- ============================================================

-- Admin user (no employee_id)
INSERT INTO users (username, password, user_type, is_active)
VALUES ('admin', 'password', 'ADMIN', TRUE);

-- Sample Employee 1
INSERT INTO employees (first_name, middle_name, last_name, date_of_birth, contact_no, email, address, position, department)
VALUES ('John', 'Michael', 'Doe', '1990-01-15', '9876543210', 'john.doe@company.com', '123 Main St, Springfield', 'Software Developer', 'IT');

INSERT INTO users (username, password, user_type, employee_id, is_active)
VALUES ('john.doe', 'john123', 'EMPLOYEE', 1, TRUE);

-- Sample Employee 2
INSERT INTO employees (first_name, middle_name, last_name, date_of_birth, contact_no, email, address, position, department)
VALUES ('Jane', 'Anne', 'Smith', '1992-05-20', '8765432109', 'jane.smith@company.com', '456 Oak Ave, Shelbyville', 'HR Manager', 'Human Resources');

INSERT INTO users (username, password, user_type, employee_id, is_active)
VALUES ('jane.smith', 'jane123', 'EMPLOYEE', 2, TRUE);

-- Sample Employee 3
INSERT INTO employees (first_name, middle_name, last_name, date_of_birth, contact_no, email, address, position, department)
VALUES ('Robert', NULL, 'Johnson', '1988-11-30', '7654321098', 'robert.johnson@company.com', '789 Pine Rd, Capital City', 'Accountant', 'Finance');

INSERT INTO users (username, password, user_type, employee_id, is_active)
VALUES ('robert.j', 'robert123', 'EMPLOYEE', 3, TRUE);

-- Sample Tasks
INSERT INTO tasks (task_name, description, status, employee_id)
VALUES ('Develop Login Module', 'Create the login functionality for the EMS system', 'Completed', 1);

INSERT INTO tasks (task_name, description, status, employee_id)
VALUES ('Database Design', 'Design and implement the MySQL database schema', 'In Progress', 1);

INSERT INTO tasks (task_name, description, status, employee_id)
VALUES ('Recruit New Developers', 'Post job listings and screen candidates for 3 developer positions', 'Pending', 2);

INSERT INTO tasks (task_name, description, status, employee_id)
VALUES ('Q1 Financial Report', 'Prepare and submit the Q1 financial report to management', 'Pending', 3);

INSERT INTO tasks (task_name, description, status, employee_id)
VALUES ('Onboarding Documentation', 'Update employee onboarding documents for 2025', 'Completed', 2);

-- Sample Attendance
INSERT INTO attendance (employee_id, attendance_date, status, remarks)
VALUES (1, CURDATE() - INTERVAL 1 DAY, 'Present', NULL);

INSERT INTO attendance (employee_id, attendance_date, status, remarks)
VALUES (1, CURDATE() - INTERVAL 2 DAY, 'Present', NULL);

INSERT INTO attendance (employee_id, attendance_date, status, remarks)
VALUES (2, CURDATE() - INTERVAL 1 DAY, 'Leave', 'Annual leave');

INSERT INTO attendance (employee_id, attendance_date, status, remarks)
VALUES (3, CURDATE() - INTERVAL 1 DAY, 'Late', 'Traffic delay');

SELECT 'Database setup complete!' AS status;
