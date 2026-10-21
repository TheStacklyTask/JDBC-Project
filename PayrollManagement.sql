
CREATE DATABASE IF NOT EXISTS PayrollDB;
USE PayrollDB;

CREATE TABLE IF NOT EXISTS employees (
    employee_id INT PRIMARY KEY AUTO_INCREMENT,
    employee_name VARCHAR(100) NOT NULL,
    department VARCHAR(50) NOT NULL,
    designation VARCHAR(50),
    joining_date DATE,
    basic_salary DECIMAL(10,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS salaries (
    salary_id INT PRIMARY KEY AUTO_INCREMENT,
    employee_id INT NOT NULL,
    salary_month DATE NOT NULL,
    basic_salary DECIMAL(10,2) NOT NULL,
    allowances DECIMAL(10,2) DEFAULT 0,
    other_deductions DECIMAL(10,2) DEFAULT 0,
    UNIQUE (employee_id, salary_month),
    FOREIGN KEY (employee_id)
        REFERENCES employees(employee_id)
);

CREATE TABLE IF NOT EXISTS payroll (
    payroll_id INT PRIMARY KEY AUTO_INCREMENT,
    employee_id INT NOT NULL,
    salary_month DATE NOT NULL,
    gross_salary DECIMAL(10,2) NOT NULL,
    tax_amount DECIMAL(10,2) DEFAULT 0,
    net_salary DECIMAL(10,2) NOT NULL,
    payment_status VARCHAR(20) DEFAULT 'PENDING',
    UNIQUE (employee_id, salary_month),
    FOREIGN KEY (employee_id)
        REFERENCES employees(employee_id)
);

INSERT INTO employees
(employee_name, department, designation, joining_date, basic_salary)
VALUES
('Arun Kumar', 'IT', 'Developer', '2024-01-10', 30000),
('Priya', 'HR', 'HR Executive', '2024-03-15', 25000),
('Karthik', 'Finance', 'Accountant', '2023-08-20', 35000);

SELECT * FROM employees;
SELECT * FROM salaries;
SELECT * FROM payroll;

