

package controller;

import dao.*;
import model.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * MainController — Admin Dashboard Controller.
 * Manages 4 tabs: Dashboard, Employee Management, Task Management, Attendance.
 *
 * ENCAPSULATION: All UI fields are private. DAO objects encapsulate DB access.
 */
public class MainController {

    // ─── DAOs ─────────────────────────────────────────────────────────────────
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final UserDAO     userDAO     = new UserDAO();
    private final TaskDAO     taskDAO     = new TaskDAO();
    private final AttendanceDAO attDAO    = new AttendanceDAO();

    // ─── Dashboard Tab ────────────────────────────────────────────────────────
    @FXML private Label adminNameLabel;
    @FXML private Label lblTotalEmp, lblTotalTasks, lblPendingTasks, lblCompletedTasks;

    // ─── Employee Tab ─────────────────────────────────────────────────────────
    @FXML private TextField empFirstName, empMiddleName, empLastName;
    @FXML private DatePicker empDOB;
    @FXML private TextField empContact, empEmail;
    @FXML private TextArea  empAddress;
    @FXML private TextField empPosition, empDepartment, empUsername, empPassword;
    @FXML private Label     empErrorLabel;
    @FXML private TextField empSearch;
    @FXML private TableView<Employee>            empTable;
    @FXML private TableColumn<Employee,Number>   colEmpId;
    @FXML private TableColumn<Employee,String>   colEmpFirst, colEmpLast, colEmpContact, colEmpEmail, colEmpPos, colEmpDept;

    private ObservableList<Employee> allEmployees = FXCollections.observableArrayList();
    private Employee selectedEmployee = null;

    // ─── Task Tab ─────────────────────────────────────────────────────────────
    @FXML private TextField  taskName;
    @FXML private TextArea   taskDesc;
    @FXML private ComboBox<String>   taskStatus;
    @FXML private ComboBox<Employee> taskEmployee;
    @FXML private Label      taskErrorLabel;
    @FXML private TableView<Task>         taskTable;
    @FXML private TableColumn<Task,Number> colTaskId;
    @FXML private TableColumn<Task,String> colTaskName, colTaskDesc, colTaskStatus, colTaskEmp;

    private ObservableList<Task> allTasks = FXCollections.observableArrayList();
    private Task selectedTask = null;

    // ─── Attendance Tab ───────────────────────────────────────────────────────
    @FXML private ComboBox<Employee> attEmployee;
    @FXML private DatePicker  attDate;
    @FXML private ComboBox<String> attStatus;
    @FXML private TextField   attRemarks;
    @FXML private Label       attErrorLabel;
    @FXML private TableView<Attendance>         attTable;
    @FXML private TableColumn<Attendance,String> colAttEmp, colAttDate, colAttStatus, colAttRemarks;

    private ObservableList<Attendance> allAttendance = FXCollections.observableArrayList();

    // ─── Validation Patterns ──────────────────────────────────────────────────
    private static final Pattern EMAIL_PATTERN   = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern CONTACT_PATTERN = Pattern.compile("^\\d{10}$");

    // ─── Init ─────────────────────────────────────────────────────────────────

    public void initAdminUser(AdminUser admin) {
        adminNameLabel.setText("Logged in as: " + admin.getUsername());
        initialize();
    }

    @FXML
    public void initialize() {
        setupEmployeeTab();
        setupTaskTab();
        setupAttendanceTab();
        refreshDashboard();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // DASHBOARD TAB
    // ══════════════════════════════════════════════════════════════════════════

    @FXML
    public void refreshDashboard() {
        try {
            lblTotalEmp.setText(String.valueOf(employeeDAO.getTotalEmployeeCount()));
            lblTotalTasks.setText(String.valueOf(taskDAO.getTotalTaskCount()));
            lblPendingTasks.setText(String.valueOf(taskDAO.getTaskCountByStatus("Pending")));
            lblCompletedTasks.setText(String.valueOf(taskDAO.getTaskCountByStatus("Completed")));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not refresh statistics: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // EMPLOYEE TAB
    // ══════════════════════════════════════════════════════════════════════════

    private void setupEmployeeTab() {
        colEmpId.setCellValueFactory(c -> c.getValue().employeeIdProperty());
        colEmpFirst.setCellValueFactory(c -> c.getValue().firstNameProperty());
        colEmpLast.setCellValueFactory(c -> c.getValue().lastNameProperty());
        colEmpContact.setCellValueFactory(c -> c.getValue().contactNoProperty());
        colEmpEmail.setCellValueFactory(c -> c.getValue().emailProperty());
        colEmpPos.setCellValueFactory(c -> c.getValue().positionProperty());
        colEmpDept.setCellValueFactory(c -> c.getValue().departmentProperty());

        empTable.setItems(allEmployees);

        // Row click → populate form
        empTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) populateEmployeeForm(sel);
        });

        // Search filter
        empSearch.textProperty().addListener((obs, old, nv) -> filterEmployees(nv));

        loadEmployees();
    }

    private void loadEmployees() {
        try {
            allEmployees.setAll(employeeDAO.getAllEmployees());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load employees: " + e.getMessage());
        }
    }

    private void filterEmployees(String query) {
        if (query == null || query.isBlank()) {
            empTable.setItems(allEmployees);
            return;
        }
        String lower = query.toLowerCase();
        ObservableList<Employee> filtered = FXCollections.observableArrayList();
        for (Employee e : allEmployees) {
            if (e.getFirstName().toLowerCase().contains(lower)
                    || e.getLastName().toLowerCase().contains(lower)) {
                filtered.add(e);
            }
        }
        empTable.setItems(filtered);
    }

    private void populateEmployeeForm(Employee e) {
        selectedEmployee = e;
        empFirstName.setText(e.getFirstName());
        empMiddleName.setText(e.getMiddleName() == null ? "" : e.getMiddleName());
        empLastName.setText(e.getLastName());
        empDOB.setValue(e.getDateOfBirth());
        empContact.setText(e.getContactNo());
        empEmail.setText(e.getEmail());
        empAddress.setText(e.getAddress());
        empPosition.setText(e.getPosition());
        empDepartment.setText(e.getDepartment());
        try {
            empUsername.setText(userDAO.getUsernameByEmployeeId(e.getEmployeeId()));
        } catch (SQLException ex) { empUsername.clear(); }
        empPassword.clear();
        hideEmpError();
    }

    @FXML
    private void handleAddEmployee() {
        String err = validateEmployeeForm(false);
        if (err != null) { showEmpError(err); return; }

        try {
            int empId = employeeDAO.addEmployee(
                    empFirstName.getText().trim(),
                    empMiddleName.getText().trim(),
                    empLastName.getText().trim(),
                    empDOB.getValue(),
                    empContact.getText().trim(),
                    empEmail.getText().trim(),
                    empAddress.getText().trim(),
                    empPosition.getText().trim(),
                    empDepartment.getText().trim()
            );
            userDAO.createEmployeeUser(empId, empUsername.getText().trim(), empPassword.getText().trim());
            handleClearEmployee();
            loadEmployees();
            refreshEmployeeDropdowns();
            refreshDashboard();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Employee added successfully!");
        } catch (SQLException e) {
            showEmpError("Database error: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateEmployee() {
        if (selectedEmployee == null) { showEmpError("Please select an employee to update."); return; }

        String err = validateEmployeeForm(true);
        if (err != null) { showEmpError(err); return; }

        try {
            employeeDAO.updateEmployee(
                    selectedEmployee.getEmployeeId(),
                    empFirstName.getText().trim(),
                    empMiddleName.getText().trim(),
                    empLastName.getText().trim(),
                    empDOB.getValue(),
                    empContact.getText().trim(),
                    empEmail.getText().trim(),
                    empAddress.getText().trim(),
                    empPosition.getText().trim(),
                    empDepartment.getText().trim()
            );
            String newUser = empUsername.getText().trim();
            String newPass = empPassword.getText().trim();
            if (!newUser.isEmpty()) {
                if (newPass.isEmpty()) {
                    showEmpError("Provide a password when changing username.");
                    return;
                }
                userDAO.updateUserCredentials(selectedEmployee.getEmployeeId(), newUser, newPass);
            }
            handleClearEmployee();
            loadEmployees();
            refreshEmployeeDropdowns();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Employee updated successfully!");
        } catch (SQLException e) {
            showEmpError("Database error: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteEmployee() {
        if (selectedEmployee == null) { showEmpError("Please select an employee to delete."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete employee '" + selectedEmployee.getFullName() + "'?\n" +
                        "This will also delete all their tasks, attendance records, and login credentials.",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText(null);
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            boolean ok = employeeDAO.deleteEmployee(selectedEmployee.getEmployeeId());
            if (ok) {
                handleClearEmployee();
                loadEmployees();
                loadTasks();
                loadAttendance();
                refreshEmployeeDropdowns();
                refreshDashboard();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Employee and all related data deleted.");
            } else {
                showEmpError("Failed to delete employee. Please try again.");
            }
        }
    }

    @FXML
    private void handleClearEmployee() {
        selectedEmployee = null;
        empFirstName.clear(); empMiddleName.clear(); empLastName.clear();
        empDOB.setValue(null); empContact.clear(); empEmail.clear();
        empAddress.clear(); empPosition.clear(); empDepartment.clear();
        empUsername.clear(); empPassword.clear();
        empTable.getSelectionModel().clearSelection();
        hideEmpError();
    }

    private String validateEmployeeForm(boolean isUpdate) {
        String fn = empFirstName.getText().trim();
        String ln = empLastName.getText().trim();
        LocalDate dob = empDOB.getValue();
        String contact = empContact.getText().trim();
        String email = empEmail.getText().trim();
        String addr = empAddress.getText().trim();
        String pos = empPosition.getText().trim();
        String dept = empDepartment.getText().trim();
        String uname = empUsername.getText().trim();
        String pass = empPassword.getText().trim();

        if (fn.isEmpty() || ln.isEmpty() || dob == null || contact.isEmpty()
                || email.isEmpty() || addr.isEmpty() || pos.isEmpty() || dept.isEmpty()) {
            return "Please fill in all required fields.";
        }
        if (Period.between(dob, LocalDate.now()).getYears() < 18) {
            return "Employee must be at least 18 years old.";
        }
        if (!CONTACT_PATTERN.matcher(contact).matches()) {
            return "Contact number must be exactly 10 digits.";
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return "Invalid email format.";
        }

        int excludeId = (isUpdate && selectedEmployee != null) ? selectedEmployee.getEmployeeId() : -1;
        try {
            if (employeeDAO.isContactExists(contact, excludeId)) return "Contact number already exists.";
            if (employeeDAO.isEmailExists(email, excludeId))    return "Email already exists.";
        } catch (SQLException e) { return "DB validation error: " + e.getMessage(); }

        if (!isUpdate) {
            // Required for new employee
            if (uname.length() < 3) return "Username must be at least 3 characters.";
            if (pass.length() < 6)  return "Password must be at least 6 characters.";
            try {
                if (userDAO.isUsernameExists(uname)) return "Username already taken.";
            } catch (SQLException e) { return "DB validation error: " + e.getMessage(); }
        } else if (!uname.isEmpty()) {
            if (uname.length() < 3) return "Username must be at least 3 characters.";
            if (pass.length() < 6)  return "Password must be at least 6 characters.";
            try {
                if (userDAO.isUsernameExists(uname, excludeId)) return "Username already taken.";
            } catch (SQLException e) { return "DB validation error: " + e.getMessage(); }
        }
        return null;
    }

    private void showEmpError(String msg)  { empErrorLabel.setText("⚠ " + msg); empErrorLabel.setVisible(true); empErrorLabel.setManaged(true); }
    private void hideEmpError()            { empErrorLabel.setVisible(false); empErrorLabel.setManaged(false); }

    // ══════════════════════════════════════════════════════════════════════════
    // TASK TAB
    // ══════════════════════════════════════════════════════════════════════════

    private void setupTaskTab() {
        taskStatus.getItems().setAll("Pending", "In Progress", "Completed");

        colTaskId.setCellValueFactory(c -> c.getValue().taskIdProperty());
        colTaskName.setCellValueFactory(c -> c.getValue().taskNameProperty());
        colTaskDesc.setCellValueFactory(c -> c.getValue().descriptionProperty());
        colTaskStatus.setCellValueFactory(c -> c.getValue().statusProperty());
        colTaskEmp.setCellValueFactory(c -> c.getValue().employeeNameProperty());

        taskTable.setItems(allTasks);
        taskTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) populateTaskForm(sel);
        });

        loadTasks();
        refreshEmployeeDropdowns();
    }

    private void loadTasks() {
        try {
            allTasks.setAll(taskDAO.getAllTasks());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load tasks: " + e.getMessage());
        }
    }

    private void populateTaskForm(Task t) {
        selectedTask = t;
        taskName.setText(t.getTaskName());
        taskDesc.setText(t.getDescription());
        taskStatus.setValue(t.getStatus());
        for (Employee e : taskEmployee.getItems()) {
            if (e.getEmployeeId() == t.getEmployeeId()) { taskEmployee.setValue(e); break; }
        }
        hideTaskError();
    }

    @FXML
    private void handleAddTask() {
        String err = validateTaskForm();
        if (err != null) { showTaskError(err); return; }
        try {
            taskDAO.addTask(taskName.getText().trim(), taskDesc.getText().trim(),
                    taskStatus.getValue(), taskEmployee.getValue().getEmployeeId());
            handleClearTask();
            loadTasks();
            refreshDashboard();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Task added successfully!");
        } catch (SQLException e) { showTaskError("DB error: " + e.getMessage()); }
    }

    @FXML
    private void handleUpdateTask() {
        if (selectedTask == null) { showTaskError("Select a task to update."); return; }
        String err = validateTaskForm();
        if (err != null) { showTaskError(err); return; }
        try {
            taskDAO.updateTask(selectedTask.getTaskId(), taskName.getText().trim(),
                    taskDesc.getText().trim(), taskStatus.getValue(),
                    taskEmployee.getValue().getEmployeeId());
            handleClearTask();
            loadTasks();
            refreshDashboard();
        } catch (SQLException e) { showTaskError("DB error: " + e.getMessage()); }
    }

    @FXML
    private void handleDeleteTask() {
        if (selectedTask == null) { showTaskError("Select a task to delete."); return; }
        Alert c = new Alert(Alert.AlertType.CONFIRMATION, "Delete task '" + selectedTask.getTaskName() + "'?", ButtonType.YES, ButtonType.NO);
        c.setHeaderText(null);
        Optional<ButtonType> r = c.showAndWait();
        if (r.isPresent() && r.get() == ButtonType.YES) {
            try {
                taskDAO.deleteTask(selectedTask.getTaskId());
                handleClearTask();
                loadTasks();
                refreshDashboard();
            } catch (SQLException e) { showTaskError("DB error: " + e.getMessage()); }
        }
    }

    @FXML
    private void handleClearTask() {
        selectedTask = null;
        taskName.clear(); taskDesc.clear(); taskStatus.setValue(null); taskEmployee.setValue(null);
        taskTable.getSelectionModel().clearSelection();
        hideTaskError();
    }

    private String validateTaskForm() {
        if (taskName.getText().trim().isEmpty()) return "Task name is required.";
        if (taskDesc.getText().trim().isEmpty()) return "Description is required.";
        if (taskStatus.getValue() == null)       return "Please select a status.";
        if (taskEmployee.getValue() == null)     return "Please select an employee.";
        return null;
    }

    private void showTaskError(String msg) { taskErrorLabel.setText("⚠ " + msg); taskErrorLabel.setVisible(true); taskErrorLabel.setManaged(true); }
    private void hideTaskError()           { taskErrorLabel.setVisible(false); taskErrorLabel.setManaged(false); }

    // ══════════════════════════════════════════════════════════════════════════
    // ATTENDANCE TAB
    // ══════════════════════════════════════════════════════════════════════════

    private void setupAttendanceTab() {
        attStatus.getItems().setAll("Present", "Leave", "Late", "Absent");
        attDate.setValue(LocalDate.now());

        colAttEmp.setCellValueFactory(c -> c.getValue().employeeNameProperty());
        colAttDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAttendanceDate().toString()));
        colAttStatus.setCellValueFactory(c -> c.getValue().statusProperty());
        colAttRemarks.setCellValueFactory(c -> c.getValue().remarksProperty());

        attTable.setItems(allAttendance);
        loadAttendance();
    }

    private void loadAttendance() {
        try {
            allAttendance.setAll(attDAO.getAllAttendance());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load attendance: " + e.getMessage());
        }
    }

    @FXML
    private void handleMarkAttendance() {
        Employee emp = attEmployee.getValue();
        LocalDate date = attDate.getValue();
        String status = attStatus.getValue();

        if (emp == null || date == null || status == null) {
            showAttError("Employee, date, and status are required.");
            return;
        }
        if (date.isAfter(LocalDate.now())) {
            showAttError("Cannot mark attendance for future dates.");
            return;
        }
        try {
            if (attDAO.attendanceExists(emp.getEmployeeId(), date)) {
                showAttError("Attendance already marked for this employee on " + date + ".");
                return;
            }
            attDAO.markAttendance(emp.getEmployeeId(), date, status, attRemarks.getText().trim());
            attEmployee.setValue(null);
            attDate.setValue(LocalDate.now());
            attStatus.setValue(null);
            attRemarks.clear();
            hideAttError();
            loadAttendance();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Attendance marked successfully!");
        } catch (SQLException e) { showAttError("DB error: " + e.getMessage()); }
    }

    private void showAttError(String msg) { attErrorLabel.setText("⚠ " + msg); attErrorLabel.setVisible(true); attErrorLabel.setManaged(true); }
    private void hideAttError()           { attErrorLabel.setVisible(false); attErrorLabel.setManaged(false); }

    // ══════════════════════════════════════════════════════════════════════════
    // SHARED HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    private void refreshEmployeeDropdowns() {
        try {
            List<Employee> emps = employeeDAO.getAllEmployees();
            taskEmployee.getItems().setAll(emps);
            attEmployee.getItems().setAll(emps);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not refresh employee list: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/Login.fxml"));
            Stage stage = (Stage) adminNameLabel.getScene().getWindow();
            Scene scene = new Scene(root, 480, 550);
            scene.getStylesheets().add(getClass().getResource("/view/style.css").toExternalForm());
            stage.setTitle("Employee Management System");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type, msg, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(null);
        a.showAndWait();
    }
}








