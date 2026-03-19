package dao;

import model.Attendance;
import util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * ABSTRACTION: AttendanceDAO encapsulates all attendance-related database operations.
 */
public class AttendanceDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    /**
     * ABSTRACTION: Marks attendance for an employee on a given date.
     */
    public boolean markAttendance(int employeeId, LocalDate date, String status, String remarks) throws SQLException {
        String sql = "INSERT INTO attendance (employee_id, attendance_date, status, remarks) VALUES (?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setDate(2, Date.valueOf(date));
            ps.setString(3, status);
            ps.setString(4, remarks == null || remarks.isBlank() ? null : remarks);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * ABSTRACTION: Checks if attendance already exists for employee on a date (prevent duplicates).
     */
    public boolean attendanceExists(int employeeId, LocalDate date) throws SQLException {
        String sql = "SELECT COUNT(*) FROM attendance WHERE employee_id = ? AND attendance_date = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setDate(2, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * ABSTRACTION: Gets all attendance records for a specific employee.
     * Real-time data sync — always loads from MySQL.
     */
    public List<Attendance> getAttendanceByEmployeeId(int employeeId) throws SQLException {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT a.*, CONCAT(e.first_name, ' ', e.last_name) AS emp_name " +
                "FROM attendance a JOIN employees e ON a.employee_id = e.employee_id " +
                "WHERE a.employee_id = ? ORDER BY a.attendance_date DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * ABSTRACTION: Gets ALL attendance records (admin view).
     */
    public List<Attendance> getAllAttendance() throws SQLException {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT a.*, CONCAT(e.first_name, ' ', e.last_name) AS emp_name " +
                "FROM attendance a JOIN employees e ON a.employee_id = e.employee_id " +
                "ORDER BY a.attendance_date DESC";
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public int getAttendanceCountByEmployeeAndStatus(int employeeId, String status) throws SQLException {
        try (PreparedStatement ps = getConn().prepareStatement(
                "SELECT COUNT(*) FROM attendance WHERE employee_id = ? AND status = ?")) {
            ps.setInt(1, employeeId);
            ps.setString(2, status);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public int getTotalAttendanceByEmployee(int employeeId) throws SQLException {
        try (PreparedStatement ps = getConn().prepareStatement(
                "SELECT COUNT(*) FROM attendance WHERE employee_id = ?")) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private Attendance mapRow(ResultSet rs) throws SQLException {
        return new Attendance(
                rs.getInt("attendance_id"),
                rs.getInt("employee_id"),
                rs.getString("emp_name"),
                rs.getDate("attendance_date").toLocalDate(),
                rs.getString("status"),
                rs.getString("remarks")
        );
    }
}