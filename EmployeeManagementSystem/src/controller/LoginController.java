
package controller;

import dao.UserDAO;
import model.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

/**
 * LoginController — handles authentication and polymorphic dashboard routing.
 *
 * POLYMORPHISM: The same login page authenticates both Admin and Employee.
 * Based on the returned User subtype, a different dashboard is opened.
 */
public class LoginController {

    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label         errorLabel;
    @FXML private Button        loginButton;

    private final UserDAO userDAO = new UserDAO();

    /**
     * POLYMORPHISM: Calls authenticateUser() which returns either AdminUser or EmployeeUser.
     * instanceof check routes to correct dashboard.
     */
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        try {
            // POLYMORPHISM: Returns AdminUser or EmployeeUser
            User user = userDAO.authenticateUser(username, password);

            if (user == null) {
                showError("Invalid username or password. Please try again.");
                return;
            }

            if (!user.isActive()) {
                showError("Your account has been deactivated. Please contact admin.");
                return;
            }

            // POLYMORPHISM: Open different dashboard based on actual type
            if (user instanceof AdminUser) {
                openAdminDashboard((AdminUser) user);
            } else if (user instanceof EmployeeUser) {
                openEmployeeDashboard((EmployeeUser) user);
            }

        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openAdminDashboard(AdminUser adminUser) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainView.fxml"));
            Parent root = loader.load();
            MainController ctrl = loader.getController();
            ctrl.initAdminUser(adminUser);

            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root, 1100, 720);
            scene.getStylesheets().add(getClass().getResource("/view/style.css").toExternalForm());
            stage.setTitle(adminUser.getDashboardTitle());
            stage.setScene(scene);
            stage.setResizable(true);
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Failed to load Admin Dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openEmployeeDashboard(EmployeeUser employeeUser) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EmployeeDashboard.fxml"));
            Parent root = loader.load();
            EmployeeDashboardController ctrl = loader.getController();
            ctrl.initEmployeeUser(employeeUser);

            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 680);
            scene.getStylesheets().add(getClass().getResource("/view/style.css").toExternalForm());
            stage.setTitle(employeeUser.getDashboardTitle());
            stage.setScene(scene);
            stage.setResizable(true);
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Failed to load Employee Dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String msg) {
        errorLabel.setText("⚠ " + msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}









