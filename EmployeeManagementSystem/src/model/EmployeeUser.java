package model;

/**
 * INHERITANCE: EmployeeUser extends the abstract User class.
 * Represents a regular employee with limited access.
 *
 * OOP Principles:
 * - INHERITANCE: extends User
 * - COMPOSITION: contains an Employee object
 * - POLYMORPHISM: overrides getDashboardTitle() and hasPermission()
 * - ENCAPSULATION: private employee field with public getter
 */
public class EmployeeUser extends User {

    /**
     * COMPOSITION: EmployeeUser HAS-AN Employee (composition relationship)
     */
    private Employee employee;

    /**
     * INHERITANCE: Calls parent constructor via super()
     */
    public EmployeeUser(int userId, String username, String password, boolean active, Employee employee) {
        super(userId, username, password, "EMPLOYEE", active);
        this.employee = employee;
    }

    /**
     * POLYMORPHISM: Employee-specific dashboard title.
     */
    @Override
    public String getDashboardTitle() {
        return "Employee Dashboard - My Workspace";
    }

    /**
     * POLYMORPHISM: Employees can only VIEW their own data.
     */
    @Override
    public boolean hasPermission(String action) {
        // Employees can only perform read/view operations on their own data
        return action.equalsIgnoreCase("VIEW_OWN_TASKS")
                || action.equalsIgnoreCase("VIEW_OWN_ATTENDANCE")
                || action.equalsIgnoreCase("VIEW_OWN_PROFILE");
    }

    // ENCAPSULATION: getter for composed Employee object
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
}