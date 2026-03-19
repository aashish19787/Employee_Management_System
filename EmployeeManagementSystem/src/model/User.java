package model;

/**
 * ABSTRACTION + INHERITANCE BASE CLASS:
 * Abstract class User defines the contract for all user types.
 * Cannot be instantiated directly — must be subclassed.
 *
 * OOP Principles demonstrated:
 * - ABSTRACTION: abstract methods force subclasses to provide implementations
 * - ENCAPSULATION: private fields with public getters/setters
 * - INHERITANCE: AdminUser and EmployeeUser extend this class
 */
public abstract class User {

    // ENCAPSULATION: private fields
    private int userId;
    private String username;
    private String password;
    private String userType;
    private boolean active;

    // Constructor
    public User(int userId, String username, String password, String userType, boolean active) {
        this.userId   = userId;
        this.username = username;
        this.password = password;
        this.userType = userType;
        this.active   = active;
    }

    // ABSTRACTION: Subclasses MUST implement these methods
    public abstract String getDashboardTitle();
    public abstract boolean hasPermission(String action);

    // ENCAPSULATION: Public getters
    public int getUserId()      { return userId; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getUserType() { return userType; }
    public boolean isActive()   { return active; }

    // ENCAPSULATION: Public setters
    public void setUserId(int userId)       { this.userId = userId; }
    public void setUsername(String username){ this.username = username; }
    public void setPassword(String password){ this.password = password; }
    public void setUserType(String userType){ this.userType = userType; }
    public void setActive(boolean active)   { this.active = active; }
}