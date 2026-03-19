package model;

/**
 * INHERITANCE: AdminUser extends the abstract User class.
 * Represents an administrator with full system access.
 *
 * OOP Principles:
 * - INHERITANCE: extends User, inherits all base fields/methods
 * - POLYMORPHISM: overrides getDashboardTitle() and hasPermission()
 */
public class AdminUser extends User {

    /**
     * INHERITANCE: Calls parent constructor via super()
     */
    public AdminUser(int userId, String username, String password, boolean active) {
        super(userId, username, password, "ADMIN", active);
    }

    /**
     * POLYMORPHISM: Admin-specific dashboard title implementation.
     */
    @Override
    public String getDashboardTitle() {
        return "Admin Dashboard - Full System Access";
    }

    /**
     * POLYMORPHISM: Admin has ALL permissions.
     */
    @Override
    public boolean hasPermission(String action) {
        // Admin can do everything
        return true;
    }
}