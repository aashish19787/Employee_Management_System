package dao;

import model.Employee;
import util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * ABSTRACTION: EmployeeDAO hides all SQL complexity behind simple method calls.
 * ENCAPSULATION: Database logic is contained within this class.
 */
public class EmployeeDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    /**
     * ABSTRACTION: Adds a new employee and returns the generated employee_id.
     */
    public int addEmployee(String firstName, String middleName, String lastName,
                           LocalDate dob, String contactNo, String email,
                           String address, String position, String department) throws SQLException {
        String sql = "INSERT INTO employees (first_name, middle_name, last_name, date_of_birth, " +
                "contact_no, email, address, position, department) VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, firstName);
            ps.setString(2, middleName == null || middleName.isBlank() ? null : middleName);
            ps.setString(3, lastName);
            ps.setDate(4, Date.valueOf(dob));
            ps.setString(5, contactNo);
            ps.setString(6, email);
            ps.setString(7, address);
            ps.setString(8, position);
            ps.setString(9, department);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
            throw new SQLException("No generated key returned for employee insert.");
        }
    }

    /**
     * ABSTRACTION: Updates an existing employee record.
     */
    public boolean updateEmployee(int employeeId, String firstName, String middleName, String lastName,
                                  LocalDate dob, String contactNo, String email,
                                  String address, String position, String department) throws SQLException {
        String sql = "UPDATE employees SET first_name=?, middle_name=?, last_name=?, date_of_birth=?, " +
                "contact_no=?, email=?, address=?, position=?, department=? WHERE employee_id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, firstName);
            ps.setString(2, middleName == null || middleName.isBlank() ? null : middleName);
            ps.setString(3, lastName);
            ps.setDate(4, Date.valueOf(dob));
            ps.setString(5, contactNo);
            ps.setString(6, email);
            ps.setString(7, address);
            ps.setString(8, position);
            ps.setString(9, department);
            ps.setInt(10, employeeId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * ABSTRACTION: CASCADE DELETE — removes all employee data in a transaction.
     * Deletes: users → tasks → attendance → employees
     * Ensures ATOMICITY: all or nothing.
     */
    public boolean deleteEmployee(int employeeId) {
        Connection conn = getConn();
        try {
            conn.setAutoCommit(false); // Start transaction

            // Step 1: Delete user credentials
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM users WHERE employee_id = ? AND user_type = 'EMPLOYEE'")) {
                ps.setInt(1, employeeId);
                ps.executeUpdate();
            }

            // Step 2: Delete tasks
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM tasks WHERE employee_id = ?")) {
                ps.setInt(1, employeeId);
                ps.executeUpdate();
            }

            // Step 3: Delete attendance records
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM attendance WHERE employee_id = ?")) {
                ps.setInt(1, employeeId);
                ps.executeUpdate();
            }

            // Step 4: Delete employee record
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM employees WHERE employee_id = ?")) {
                ps.setInt(1, employeeId);
                ps.executeUpdate();
            }

            conn.commit(); // Commit all changes atomically
            return true;

        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ignored) {}
            e.printStackTrace();
            return false;
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    /**
     * ABSTRACTION: Retrieves all employees from the database.
     */
    public List<Employee> getAllEmployees() throws SQLException {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT * FROM employees ORDER BY last_name, first_name";
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * ABSTRACTION: Retrieves a single employee by ID.
     */
    public Employee getEmployeeById(int employeeId) throws SQLException {
        String sql = "SELECT * FROM employees WHERE employee_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public boolean isEmailExists(String email) throws SQLException {
        return isEmailExists(email, -1);
    }

    public boolean isEmailExists(String email, int excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM employees WHERE email = ? AND employee_id != ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public boolean isContactExists(String contact) throws SQLException {
        return isContactExists(contact, -1);
    }

    public boolean isContactExists(String contact, int excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM employees WHERE contact_no = ? AND employee_id != ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, contact);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public int getTotalEmployeeCount() throws SQLException {
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM employees")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /** Map ResultSet row → Employee object */
    private Employee mapRow(ResultSet rs) throws SQLException {
        return new Employee(
                rs.getInt("employee_id"),
                rs.getString("first_name"),
                rs.getString("middle_name"),
                rs.getString("last_name"),
                rs.getDate("date_of_birth").toLocalDate(),
                rs.getString("contact_no"),
                rs.getString("email"),
                rs.getString("address"),
                rs.getString("position"),
                rs.getString("department")
        );
    }
}