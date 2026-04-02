package controller;

import dao.*;
import model.*;
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
import java.util.List;

public class EmployeeDashboardController {

    private final TaskDAO       taskDAO = new TaskDAO();
    private final AttendanceDAO attDAO  = new AttendanceDAO();

    private EmployeeUser currentUser;
    private int employeeId;

    // Dashboard Tab
    @FXML private Label empWelcomeLabel;
    @FXML private Label empLblTotal;
    @FXML private Label empLblPending;
    @FXML private Label empLblInProgress;   // matches fx:id in FXML
    @FXML private Label empLblCompleted;
    @FXML private Label empLblPresent;
    @FXML private TableView<Task>          recentTaskTable;
    @FXML private TableColumn<Task,String> colRecentName, colRecentStatus, colRecentDesc;

    // My Tasks Tab
    @FXML private ComboBox<String> taskFilterCombo;
    @FXML private TableView<Task>          myTaskTable;
    @FXML private TableColumn<Task,String> colMyTaskName, colMyTaskStatus;
    @FXML private Label    detailTaskName, detailTaskStatus;
    @FXML private TextArea detailTaskDesc;

    private ObservableList<Task> allMyTasks = FXCollections.observableArrayList();

    // My Attendance Tab
    @FXML private Label attLblPresent, attLblLeave, attLblLate, attLblAbsent, attLblRate;
    @FXML private TableView<Attendance>          myAttTable;
    @FXML private TableColumn<Attendance,String> colAttDate2, colAttStatus2, colAttRemarks2;

    // My Profile Tab
    @FXML private Label profileId, profileName, profileDOB, profileContact;
    @FXML private Label profileEmail, profileDept, profilePos, profileAddr;
    @FXML private Label profileTasks, profileAttRate;

    // -------------------------------------------------------------------------

    public void initEmployeeUser(EmployeeUser user) {
        this.currentUser = user;
        this.employeeId  = user.getEmployee().getEmployeeId();
        empWelcomeLabel.setText("Welcome, " + user.getEmployee().getFullName());
        loadAllData();
    }

    private void loadAllData() {
        loadDashboard();
        setupTasksTab();
        loadAttendance();
        loadProfile();
    }

    // =========================================================================
    // DASHBOARD TAB
    // =========================================================================

    private void loadDashboard() {
        try {
            int pending    = taskDAO.getTaskCountByEmployeeAndStatus(employeeId, "Pending");
            int inProgress = taskDAO.getTaskCountByEmployeeAndStatus(employeeId, "In Progress");
            int completed  = taskDAO.getTaskCountByEmployeeAndStatus(employeeId, "Completed");
            int total      = pending + inProgress + completed;
            int present    = attDAO.getAttendanceCountByEmployeeAndStatus(employeeId, "Present");

            empLblTotal.setText(String.valueOf(total));
            empLblPending.setText(String.valueOf(pending));
            empLblInProgress.setText(String.valueOf(inProgress));
            empLblCompleted.setText(String.valueOf(completed));
            empLblPresent.setText(String.valueOf(present));

            colRecentName.setCellValueFactory(c -> c.getValue().taskNameProperty());
            colRecentStatus.setCellValueFactory(c -> c.getValue().statusProperty());
            colRecentDesc.setCellValueFactory(c -> c.getValue().descriptionProperty());

            List<Task> tasks = taskDAO.getTasksByEmployeeId(employeeId);
            ObservableList<Task> recent = FXCollections.observableArrayList(
                    tasks.subList(0, Math.min(5, tasks.size()))
            );
            recentTaskTable.setItems(recent);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =========================================================================
    // MY TASKS TAB
    // =========================================================================

    private void setupTasksTab() {
        taskFilterCombo.getItems().setAll("All Tasks", "Pending", "In Progress", "Completed");
        taskFilterCombo.setValue("All Tasks");
        taskFilterCombo.valueProperty().addListener((obs, old, nv) -> applyTaskFilter(nv));

        colMyTaskName.setCellValueFactory(c -> c.getValue().taskNameProperty());
        colMyTaskStatus.setCellValueFactory(c -> c.getValue().statusProperty());

        myTaskTable.setItems(allMyTasks);
        myTaskTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) showTaskDetail(sel);
        });

        loadMyTasks();
    }

    private void loadMyTasks() {
        try {
            allMyTasks.setAll(taskDAO.getTasksByEmployeeId(employeeId));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        applyTaskFilter(taskFilterCombo.getValue());
    }

    private void applyTaskFilter(String filter) {
        if (filter == null || filter.equals("All Tasks")) {
            myTaskTable.setItems(allMyTasks);
            return;
        }
        ObservableList<Task> filtered = FXCollections.observableArrayList();
        for (Task t : allMyTasks) {
            if (t.getStatus().equals(filter)) filtered.add(t);
        }
        myTaskTable.setItems(filtered);
    }

    private void showTaskDetail(Task t) {
        detailTaskName.setText(t.getTaskName());
        detailTaskStatus.setText(t.getStatus());
        detailTaskDesc.setText(t.getDescription());
    }

    @FXML
    private void handleRefreshTasks() {
        loadMyTasks();
        loadDashboard();
    }

    // =========================================================================
    // MY ATTENDANCE TAB
    // =========================================================================

    private void loadAttendance() {
        try {
            int present = attDAO.getAttendanceCountByEmployeeAndStatus(employeeId, "Present");
            int leave   = attDAO.getAttendanceCountByEmployeeAndStatus(employeeId, "Leave");
            int late    = attDAO.getAttendanceCountByEmployeeAndStatus(employeeId, "Late");
            int absent  = attDAO.getAttendanceCountByEmployeeAndStatus(employeeId, "Absent");
            int total   = attDAO.getTotalAttendanceByEmployee(employeeId);

            attLblPresent.setText(String.valueOf(present));
            attLblLeave.setText(String.valueOf(leave));
            attLblLate.setText(String.valueOf(late));
            attLblAbsent.setText(String.valueOf(absent));

            double rate = total > 0 ? (present * 100.0 / total) : 0;
            attLblRate.setText(String.format("%.1f%%", rate));

            colAttDate2.setCellValueFactory(c -> new SimpleStringProperty(
                    c.getValue().getAttendanceDate().toString()));
            colAttStatus2.setCellValueFactory(c -> c.getValue().statusProperty());
            colAttRemarks2.setCellValueFactory(c -> c.getValue().remarksProperty());

            ObservableList<Attendance> attList = FXCollections.observableArrayList(
                    attDAO.getAttendanceByEmployeeId(employeeId)
            );
            myAttTable.setItems(attList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =========================================================================
    // MY PROFILE TAB
    // =========================================================================

    private void loadProfile() {
        Employee emp = currentUser.getEmployee();
        profileId.setText(String.valueOf(emp.getEmployeeId()));
        profileName.setText(emp.getFullName());
        profileDOB.setText(emp.getDateOfBirth().toString());
        profileContact.setText(emp.getContactNo());
        profileEmail.setText(emp.getEmail());
        profileDept.setText(emp.getDepartment());
        profilePos.setText(emp.getPosition());
        profileAddr.setText(emp.getAddress());

        try {
            int pending    = taskDAO.getTaskCountByEmployeeAndStatus(employeeId, "Pending");
            int inProgress = taskDAO.getTaskCountByEmployeeAndStatus(employeeId, "In Progress");
            int completed  = taskDAO.getTaskCountByEmployeeAndStatus(employeeId, "Completed");
            int total      = pending + inProgress + completed;

            profileTasks.setText(total + " total  |  " + inProgress + " in progress  |  " + completed + " completed");

            int present  = attDAO.getAttendanceCountByEmployeeAndStatus(employeeId, "Present");
            int totalAtt = attDAO.getTotalAttendanceByEmployee(employeeId);
            double rate  = totalAtt > 0 ? (present * 100.0 / totalAtt) : 0;
            profileAttRate.setText(String.format("%.1f%%", rate));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =========================================================================
    // LOGOUT
    // =========================================================================

    @FXML
    private void handleLogout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/Login.fxml"));
            Stage stage = (Stage) empWelcomeLabel.getScene().getWindow();
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
}