-- ============================================================
-- CPSC 4340: 
-- Name: Orion Medrano
-- Due Date: 11/5/2026
-- Assignment: Stored Procedures, Views, and Triggers
-- Description:
-- Demonstrates SQL views, stored procedures, triggers,
-- error handling, and database testing.
-- ============================================================
-- DATABASE SETUP
-- ============================================================

-- Start with a clean assignment database

DROP DATABASE IF EXISTS CPSC4340_SQL_Assignment;

CREATE DATABASE CPSC4340_SQL_Assignment;

USE CPSC4340_SQL_Assignment;


-- Department table
CREATE TABLE Department (
dept_id INT PRIMARY KEY,
dept_name VARCHAR(50) NOT NULL,
budget DECIMAL(12,2)
);

-- Employee table
CREATE TABLE Employee (
emp_id INT PRIMARY KEY,
emp_name VARCHAR(100) NOT NULL,
dept_id INT,
salary DECIMAL(10,2) CHECK (salary >= 0),
hire_date DATE,
FOREIGN KEY (dept_id) REFERENCES Department(dept_id)
);

-- SalaryLog table
CREATE TABLE SalaryLog (
log_id INT AUTO_INCREMENT PRIMARY KEY,
emp_id INT,
old_salary DECIMAL(10,2),
new_salary DECIMAL(10,2),
changed_by VARCHAR(80),
changed_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
FOREIGN KEY (emp_id) REFERENCES Employee(emp_id)
);

INSERT INTO Department VALUES
(10, 'Engineering', 850000),
(20, 'Sales', 420000),
(30, 'HR', 180000);

INSERT INTO Employee VALUES
(101, 'Alice Chen', 10, 92000, '2021-06-15'),
(102, 'Ben Ortiz', 10, 78000, '2022-01-10'),
(103, 'Chika Ade', 20, 68000, '2020-09-01'),
(104, 'Dev Patel', 20, 54000, '2023-03-22'),
(105, 'Elena Ruiz', 30, 61000, '2019-11-04'),
(106, 'Farid Rahman', 10, 105000, '2018-04-19');


-- _________________________________
-- PART 1: VIEWS
-- _________________________________

-- Task 1: Create a view showing employees earning
-- more than $75,000.
CREATE VIEW HighEarners AS
SELECT emp_id, emp_name, salary
FROM Employee
WHERE salary > 75000;

-- Test the HighEarners view
SELECT * FROM HighEarners;


-- Task 2: Create a view showing department names,
-- employee counts, and average salaries.

CREATE VIEW DepartmentSummary AS
SELECT
    d.dept_name,
    COUNT(e.emp_id) AS employee_count,
    AVG(e.salary) AS avg_salary
FROM Department d
LEFT JOIN Employee e
    ON d.dept_id = e.dept_id
GROUP BY d.dept_id, d.dept_name;

-- Test the DepartmentSummary view
SELECT * FROM DepartmentSummary;


-- Task 3: Find the department with the highest
-- average salary using DepartmentSummary.
SELECT dept_name, avg_salary
FROM DepartmentSummary
ORDER BY avg_salary DESC
LIMIT 1;


-- ___________________________________
-- PART 2: STORED PROCEDURES 
-- ___________________________________

DELIMITER $$

-- Task 1: GiveRaise
-- Increases an employee's salary by a specified percentage.

CREATE PROCEDURE GiveRaise(
    IN p_emp_id INT,
    IN p_percent DECIMAL(5,2)
)
BEGIN
    UPDATE Employee
    SET salary = salary * (1 + p_percent / 100)
    WHERE emp_id = p_emp_id;
END$$


-- Task 2: TransferEmployee
-- Moves an employee to another department.
-- Raises an error if the destination does not exist.
CREATE PROCEDURE TransferEmployee(
    IN p_emp_id INT,
    IN p_new_dept_id INT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM Department
        WHERE dept_id = p_new_dept_id
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Destination department does not exist';
    ELSE
        UPDATE Employee
        SET dept_id = p_new_dept_id
        WHERE emp_id = p_emp_id;
    END IF;
END$$

DELIMITER ;



-- PART 3: TRIGGERS
DELIMITER $$

-- Task 1: Audit salary changes after an UPDATE.
-- Records the employee ID, old salary, new salary,
-- and the database user who made the change.

CREATE TRIGGER trg_log_salary_change
AFTER UPDATE ON Employee
FOR EACH ROW
BEGIN
    IF NOT (OLD.salary <=> NEW.salary) THEN
        INSERT INTO SalaryLog (
            emp_id,
            old_salary,
            new_salary,
            changed_by
        )
        VALUES (
            NEW.emp_id,
            OLD.salary,
            NEW.salary,
            CURRENT_USER()
        );
    END IF;
END$$


-- Task 2: Reject inserted salaries above $500,000.
CREATE TRIGGER trg_check_salary_cap_insert
BEFORE INSERT ON Employee
FOR EACH ROW
BEGIN
    IF NEW.salary > 500000 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Salary cannot exceed 500000';
    END IF;
END$$


-- Reject updated salaries above $500,000.
CREATE TRIGGER trg_check_salary_cap_update
BEFORE UPDATE ON Employee
FOR EACH ROW
BEGIN
    IF NEW.salary > 500000 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Salary cannot exceed 500000';
    END IF;
END$$

DELIMITER ;


-- _____________________
-- PART 4: TEST SCRIPT
-- _____________________

-- TEST 1: Verify the HighEarners view
SELECT * FROM HighEarners;


-- TEST 2: Verify the DepartmentSummary view
SELECT * FROM DepartmentSummary;


-- TEST 3: GiveRaise before and after
-- Check Alice Chen's original salary.
SELECT emp_id, emp_name, salary
FROM Employee
WHERE emp_id = 101;

-- Give Alice a 10 percent raise.
CALL GiveRaise(101, 10.00);

-- Verify Alice's new salary.
SELECT emp_id, emp_name, salary
FROM Employee
WHERE emp_id = 101;

-- TEST 4: Verify the salary audit trigger.
-- Alice's raise should have created a log entry.
SELECT * FROM SalaryLog;


-- TEST 5: TransferEmployee successful call.
-- Check Ben Ortiz's current department.
SELECT emp_id, emp_name, dept_id
FROM Employee
WHERE emp_id = 102;

-- Transfer Ben from Engineering to Sales.
CALL TransferEmployee(102, 20);

-- Verify Ben's new department.
SELECT emp_id, emp_name, dept_id
FROM Employee
WHERE emp_id = 102;


-- TEST 6: Trigger an expected department error.
-- Department 999 does not exist.
-- This CALL should return SQLSTATE 45000.
CALL TransferEmployee(102, 999);


-- TEST 7: Trigger an expected salary cap error.
INSERT INTO Employee (
    emp_id,
    emp_name,
    dept_id,
    salary,
    hire_date
)
VALUES (
    107,
    'Test Employee',
    10,
    600000,
    '2026-11-05'
);

-- TEST 8: Verify the rejected employee was not added.
SELECT * FROM Employee WHERE emp_id = 107;

-- TEST 9: Verify the final employee records.
SELECT * FROM Employee;

-- TEST 10: Verify the final salary audit records.
SELECT * FROM SalaryLog;


-- TEST 11: Verify salary cap on UPDATE
-- This update should fail because 550000 exceeds the cap.
UPDATE Employee
SET salary = 550000
WHERE emp_id = 103;

-- Verify Chika Ade's salary was not changed.
SELECT emp_id, emp_name, salary
FROM Employee
WHERE emp_id = 103;


SHOW TABLES;