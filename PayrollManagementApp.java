
package org.JDBC;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class PayrollManagementApp {

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        try (Connection con = DBConnection.getConnection("PayrollDB")) {

            System.out.println("Payroll Management System Started");

            while (true) {
                System.out.println("\n===== PAYROLL MANAGEMENT SYSTEM =====");
                System.out.println("1. Add Employee");
                System.out.println("2. View Employees");
                System.out.println("3. Update Employee Basic Salary");
                System.out.println("4. Delete Employee");
                System.out.println("5. Process Monthly Payroll (Batch)");
                System.out.println("6. Generate Payslip");
                System.out.println("7. View Payroll Records");
                System.out.println("8. Salary Reports");
                System.out.println("9. Exit");
                System.out.print("Enter your choice: ");

                int choice = readInt();

                switch (choice) {
                    case 1:
                        addEmployee(con);
                        break;
                    case 2:
                        viewEmployees(con);
                        break;
                    case 3:
                        updateSalary(con);
                        break;
                    case 4:
                        deleteEmployee(con);
                        break;
                    case 5:
                        processPayroll(con);
                        break;
                    case 6:
                        generatePayslip(con);
                        break;
                    case 7:
                        viewPayroll(con);
                        break;
                    case 8:
                        salaryReports(con);
                        break;
                    case 9:
                        System.out.println("Application closed.");
                        return;
                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    static int readInt() {
        while (!sc.hasNextInt()) {
            System.out.print("Enter a valid number: ");
            sc.next();
        }
        return sc.nextInt();
    }

    static double readDouble() {
        while (!sc.hasNextDouble()) {
            System.out.print("Enter a valid amount: ");
            sc.next();
        }
        return sc.nextDouble();
    }

    static void addEmployee(Connection con) throws SQLException {

        sc.nextLine();

        System.out.print("Employee name: ");
        String name = sc.nextLine();

        System.out.print("Department: ");
        String department = sc.nextLine();

        System.out.print("Designation: ");
        String designation = sc.nextLine();

        System.out.print("Joining date (YYYY-MM-DD): ");
        String joiningDate = sc.nextLine();

        System.out.print("Basic salary: ");
        double salary = readDouble();

        if (salary <= 0) {
            System.out.println("Salary must be greater than zero.");
            return;
        }

        String sql = "INSERT INTO employees " +
                "(employee_name, department, designation, joining_date, basic_salary) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, department);
            ps.setString(3, designation);
            ps.setDate(4, Date.valueOf(LocalDate.parse(joiningDate)));
            ps.setDouble(5, salary);

            ps.executeUpdate();
            System.out.println("Employee added successfully.");
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid date. Use YYYY-MM-DD.");
        }
    }

    static void viewEmployees(Connection con) throws SQLException {

        String sql = "SELECT * FROM employees ORDER BY employee_id";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            System.out.println("\n--- Employee Details ---");

            while (rs.next()) {
                System.out.println(
                    "ID: " + rs.getInt("employee_id") +
                    " | Name: " + rs.getString("employee_name") +
                    " | Department: " + rs.getString("department") +
                    " | Designation: " + rs.getString("designation") +
                    " | Basic Salary: " + rs.getDouble("basic_salary")
                );
            }
        }
    }

    static void updateSalary(Connection con) throws SQLException {

        System.out.print("Enter employee ID: ");
        int id = readInt();

        System.out.print("Enter new basic salary: ");
        double salary = readDouble();

        if (salary <= 0) {
            System.out.println("Salary must be greater than zero.");
            return;
        }

        String sql = "UPDATE employees SET basic_salary = ? WHERE employee_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, salary);
            ps.setInt(2, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Basic salary updated.");
            } else {
                System.out.println("Employee ID not found.");
            }
        }
    }

    static void deleteEmployee(Connection con) throws SQLException {

        System.out.print("Enter employee ID to delete: ");
        int id = readInt();

        String sql = "DELETE FROM employees WHERE employee_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Employee deleted.");
            } else {
                System.out.println("Employee ID not found.");
            }
        } catch (SQLException e) {
            System.out.println(
                "Cannot delete this employee because salary/payroll records exist."
            );
        }
    }

    static void processPayroll(Connection con) throws SQLException {

        sc.nextLine();

        System.out.print("Enter salary month (YYYY-MM): ");
        String monthInput = sc.nextLine();

        LocalDate month;

        try {
            month = LocalDate.parse(
                monthInput + "-01",
                DateTimeFormatter.ofPattern("yyyy-MM-dd")
            );
        } catch (Exception e) {
            System.out.println("Invalid month. Example: 2026-10");
            return;
        }

        System.out.print("Monthly allowance for each employee: ");
        double allowance = readDouble();

        System.out.print("Other deduction for each employee: ");
        double deduction = readDouble();

        System.out.print("Tax percentage (example 5 for 5%): ");
        double taxRate = readDouble();

        if (allowance < 0 || deduction < 0 ||
                taxRate < 0 || taxRate > 100) {
            System.out.println("Invalid allowance, deduction, or tax rate.");
            return;
        }

        String employeeSql =
            "SELECT employee_id, basic_salary FROM employees";

        String salarySql =
            "INSERT INTO salaries " +
            "(employee_id, salary_month, basic_salary, allowances, other_deductions) " +
            "VALUES (?, ?, ?, ?, ?)";

        String payrollSql =
            "INSERT INTO payroll " +
            "(employee_id, salary_month, gross_salary, tax_amount, net_salary) " +
            "VALUES (?, ?, ?, ?, ?)";

        boolean oldAutoCommit = con.getAutoCommit();
        int processed = 0;

        try {
            con.setAutoCommit(false);

            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(employeeSql);
                 PreparedStatement salaryPs = con.prepareStatement(salarySql);
                 PreparedStatement payrollPs = con.prepareStatement(payrollSql)) {

                while (rs.next()) {

                    int employeeId = rs.getInt("employee_id");
                    double basic = rs.getDouble("basic_salary");

                    double gross = basic + allowance;
                    double tax = gross * taxRate / 100.0;
                    double net = gross - tax - deduction;

                    if (net < 0) {
                        throw new SQLException(
                            "Deductions exceed salary for employee ID " + employeeId
                        );
                    }

                    salaryPs.setInt(1, employeeId);
                    salaryPs.setDate(2, Date.valueOf(month));
                    salaryPs.setDouble(3, basic);
                    salaryPs.setDouble(4, allowance);
                    salaryPs.setDouble(5, deduction);
                    salaryPs.addBatch();

                    payrollPs.setInt(1, employeeId);
                    payrollPs.setDate(2, Date.valueOf(month));
                    payrollPs.setDouble(3, gross);
                    payrollPs.setDouble(4, tax);
                    payrollPs.setDouble(5, net);
                    payrollPs.addBatch();

                    processed++;
                }

                if (processed == 0) {
                    con.rollback();
                    System.out.println("No employees found.");
                    return;
                }

                salaryPs.executeBatch();
                payrollPs.executeBatch();
            }

            con.commit();

            System.out.println("Monthly payroll processed successfully.");
            System.out.println("Employees processed: " + processed);

        } catch (SQLException e) {
            con.rollback();
            System.out.println(
                "Payroll failed. All changes rolled back. Reason: " + e.getMessage()
            );
        } finally {
            con.setAutoCommit(oldAutoCommit);
        }
    }

    static void generatePayslip(Connection con) throws SQLException {

        System.out.print("Enter employee ID: ");
        int id = readInt();

        sc.nextLine();

        System.out.print("Enter salary month (YYYY-MM): ");
        String month = sc.nextLine();

        String sql =
            "SELECT e.employee_name, e.department, " +
            "s.salary_month, s.basic_salary, s.allowances, " +
            "s.other_deductions, p.gross_salary, p.tax_amount, " +
            "p.net_salary, p.payment_status " +
            "FROM employees e " +
            "JOIN salaries s ON e.employee_id = s.employee_id " +
            "JOIN payroll p ON p.employee_id = s.employee_id " +
            "AND p.salary_month = s.salary_month " +
            "WHERE e.employee_id = ? AND s.salary_month = ?";

        try {
            Date salaryMonth = Date.valueOf(month + "-01");

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.setDate(2, salaryMonth);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("\n========== PAYSLIP ==========");
                        System.out.println("Employee: " +
                            rs.getString("employee_name"));
                        System.out.println("Department: " +
                            rs.getString("department"));
                        System.out.println("Month: " +
                            rs.getDate("salary_month"));
                        System.out.println("Basic Salary: " +
                            rs.getDouble("basic_salary"));
                        System.out.println("Allowances: " +
                            rs.getDouble("allowances"));
                        System.out.println("Gross Salary: " +
                            rs.getDouble("gross_salary"));
                        System.out.println("Tax: " +
                            rs.getDouble("tax_amount"));
                        System.out.println("Other Deductions: " +
                            rs.getDouble("other_deductions"));
                        System.out.println("Net Salary: " +
                            rs.getDouble("net_salary"));
                        System.out.println("Payment Status: " +
                            rs.getString("payment_status"));
                        System.out.println("==============================");
                    } else {
                        System.out.println("Payslip not found for this month.");
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid month. Use YYYY-MM.");
        }
    }

    static void viewPayroll(Connection con) throws SQLException {

        String sql =
            "SELECT p.payroll_id, e.employee_name, p.salary_month, " +
            "p.gross_salary, p.tax_amount, p.net_salary, p.payment_status " +
            "FROM payroll p JOIN employees e " +
            "ON p.employee_id = e.employee_id " +
            "ORDER BY p.salary_month DESC, p.employee_id";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            System.out.println("\n--- Payroll Records ---");

            while (rs.next()) {
                System.out.println(
                    "Payroll ID: " + rs.getInt("payroll_id") +
                    " | Employee: " + rs.getString("employee_name") +
                    " | Month: " + rs.getDate("salary_month") +
                    " | Gross: " + rs.getDouble("gross_salary") +
                    " | Tax: " + rs.getDouble("tax_amount") +
                    " | Net: " + rs.getDouble("net_salary") +
                    " | Status: " + rs.getString("payment_status")
                );
            }
        }
    }

    static void salaryReports(Connection con) throws SQLException {

        String sql =
            "SELECT COUNT(*) AS employee_count, " +
            "COALESCE(SUM(net_salary), 0) AS total_salary, " +
            "COALESCE(AVG(net_salary), 0) AS average_salary, " +
            "COALESCE(MAX(net_salary), 0) AS highest_salary " +
            "FROM payroll";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                System.out.println("\n--- Salary Reports (All Payroll Records) ---");
                System.out.println("Payroll Records: " +
                    rs.getInt("employee_count"));
                System.out.println("Total Net Salary: " +
                    rs.getDouble("total_salary"));
                System.out.println("Average Net Salary: " +
                    rs.getDouble("average_salary"));
                System.out.println("Highest Net Salary: " +
                    rs.getDouble("highest_salary"));
            }
        }
    }
}
