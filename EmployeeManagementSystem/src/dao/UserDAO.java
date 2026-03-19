package dao;

import model.*;
import util.DatabaseConnection;

import java.sql.*;

/**
 * ABSTRACTION: UserDAO encapsulates all user authentication and credential operations.
 * POLYMORPHISM: authenticateUser() returns either AdminUser or EmployeeUser based on user_type.
 */
public class UserDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    /**
     * POLYMORPHISM: Returns the correct User subtype based on database user_type.
     * Same method — different object returned depending on credentials.
     */
    public User authenticateUser(String username, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND is_active = TRUE";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null; // Invalid credentials

                int     userId   = rs.getInt("user_id");
                String  uType    = rs.getString("user_type");
                boolean isActive = rs.getBoolean("is_active");
                int     empId    = rs.getInt("employee_id");

                if ("ADMIN".equals(uType)) {
                    // POLYMORPHISM: Returns AdminUser instance
                    return new AdminUser(userId, username, password, isActive);
                } else {
                    // POLYMORPHISM: Returns EmployeeUser instance with composed Employee
                    EmployeeDAO empDao = new EmployeeDAO();
                    model.Employee emp = empDao.getEmployeeById(empId);
                    return new EmployeeUser(userId, username, password, isActive, emp);
                }
            }
        }
    }

    /**
     * ABSTRACTION: Creates user credentials for an employee in one call.
     */
    public boolean createEmployeeUser(int employeeId, String username, String password) throws SQLException {
        String sql = "INSERT INTO users (username, password, user_type, employee_id, is_active) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, "EMPLOYEE");
            ps.setInt(4, employeeId);
            ps.setBoolean(5, true);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean isUsernameExists(String username) throws SQLException {
        return isUsernameExists(username, -1);
    }

    public boolean isUsernameExists(String username, int excludeEmployeeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ? AND (employee_id != ? OR employee_id IS NULL)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setInt(2, excludeEmployeeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * ABSTRACTION: Updates an employee's login credentials.
     */
    public boolean updateUserCredentials(int employeeId, String username, String password) throws SQLException {
        String sql = "UPDATE users SET username = ?, password = ? WHERE employee_id = ? AND user_type = 'EMPLOYEE'";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setInt(3, employeeId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * ABSTRACTION: Deactivates a user account without deleting it.
     */
    public boolean deactivateUser(int employeeId) throws SQLException {
        String sql = "UPDATE users SET is_active = FALSE WHERE employee_id = ? AND user_type = 'EMPLOYEE'";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            return ps.executeUpdate() > 0;
        }
    }

    /** Get username for a given employee_id */
    public String getUsernameByEmployeeId(int employeeId) throws SQLException {
        String sql = "SELECT username FROM users WHERE employee_id = ? AND user_type = 'EMPLOYEE'";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("username") : "";
            }
        }
    }
}