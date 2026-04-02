package dao;

import model.Task;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ABSTRACTION: TaskDAO encapsulates all task-related database operations.
 * UPDATED: Added searchTasks() for task list search requirement.
 */
public class TaskDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    /** Adds a new task and returns the generated task_id. */
    public int addTask(String taskName, String description, String status, int employeeId) throws SQLException {
        String sql = "INSERT INTO tasks (task_name, description, status, employee_id) VALUES (?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, taskName);
            ps.setString(2, description);
            ps.setString(3, status);
            ps.setInt(4, employeeId);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : -1;
        }
    }

    /** Updates an existing task. */
    public boolean updateTask(int taskId, String taskName, String description, String status, int employeeId) throws SQLException {
        String sql = "UPDATE tasks SET task_name=?, description=?, status=?, employee_id=? WHERE task_id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, taskName);
            ps.setString(2, description);
            ps.setString(3, status);
            ps.setInt(4, employeeId);
            ps.setInt(5, taskId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteTask(int taskId) throws SQLException {
        try (PreparedStatement ps = getConn().prepareStatement("DELETE FROM tasks WHERE task_id = ?")) {
            ps.setInt(1, taskId);
            return ps.executeUpdate() > 0;
        }
    }

    /** Gets all tasks for a specific employee (employee dashboard). */
    public List<Task> getTasksByEmployeeId(int employeeId) throws SQLException {
        List<Task> list = new ArrayList<>();
        String sql = "SELECT t.*, CONCAT(e.first_name, ' ', e.last_name) AS emp_name " +
                "FROM tasks t JOIN employees e ON t.employee_id = e.employee_id " +
                "WHERE t.employee_id = ? ORDER BY t.task_id DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Gets ALL tasks (admin dashboard). */
    public List<Task> getAllTasks() throws SQLException {
        List<Task> list = new ArrayList<>();
        String sql = "SELECT t.*, CONCAT(e.first_name, ' ', e.last_name) AS emp_name " +
                "FROM tasks t JOIN employees e ON t.employee_id = e.employee_id " +
                "ORDER BY t.task_id DESC";
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    /**
     * NEW — Search tasks by name, description, status, or assigned employee name.
     * Searches server-side with LIKE for all matching tasks.
     */
    public List<Task> searchTasks(String keyword) throws SQLException {
        List<Task> list = new ArrayList<>();
        String sql = "SELECT t.*, CONCAT(e.first_name, ' ', e.last_name) AS emp_name " +
                "FROM tasks t JOIN employees e ON t.employee_id = e.employee_id " +
                "WHERE t.task_name LIKE ? OR t.description LIKE ? OR t.status LIKE ? " +
                "   OR CONCAT(e.first_name, ' ', e.last_name) LIKE ? " +
                "ORDER BY t.task_id DESC";
        String like = "%" + keyword + "%";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * NEW — Validate that task name is not a duplicate (optionally excluding a task ID on update).
     */
    public boolean isTaskNameExists(String taskName, int excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tasks WHERE task_name = ? AND task_id != ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, taskName);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public int getTotalTaskCount() throws SQLException {
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM tasks")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int getTaskCountByStatus(String status) throws SQLException {
        try (PreparedStatement ps = getConn().prepareStatement(
                "SELECT COUNT(*) FROM tasks WHERE status = ?")) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public int getTaskCountByEmployeeAndStatus(int employeeId, String status) throws SQLException {
        try (PreparedStatement ps = getConn().prepareStatement(
                "SELECT COUNT(*) FROM tasks WHERE employee_id = ? AND status = ?")) {
            ps.setInt(1, employeeId);
            ps.setString(2, status);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private Task mapRow(ResultSet rs) throws SQLException {
        return new Task(
                rs.getInt("task_id"),
                rs.getString("task_name"),
                rs.getString("description"),
                rs.getString("status"),
                rs.getInt("employee_id"),
                rs.getString("emp_name")
        );
    }
}
