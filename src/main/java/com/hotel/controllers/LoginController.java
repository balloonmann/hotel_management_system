package com.hotel.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    @FXML
    public void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.equals("admin") && password.equals("admin123")) {
            loadDashboard("/fxml/AdminDashboard.fxml", "Admin Dashboard");
        } else if (username.equals("user") && password.equals("user123")) {
            loadDashboard("/fxml/UserDashboard.fxml", "User Dashboard");
        } else {
            errorLabel.setText("Invalid credentials.");
            errorLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private void loadDashboard(String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root, 1200, 750);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/styles.css")).toExternalForm());
            stage.setScene(scene);
            stage.setTitle(title);
        } catch (IOException e) {
            e.printStackTrace();
            String message = (e.getCause() != null) ? e.getCause().getMessage() : e.getMessage();
            errorLabel.setText("View Loading Error: " + message);
            errorLabel.setStyle("-fx-text-fill: red;");
        }
    }

    /**
     * Static utility method for logging out from any dashboard back to Login.
     */
    public static void loadLoginScreen(Stage stage) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(
                LoginController.class.getResource("/fxml/Login.fxml")));
            Scene scene = new Scene(root, 460, 420);
            scene.getStylesheets().add(Objects.requireNonNull(
                LoginController.class.getResource("/css/styles.css")).toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Hotel Management System - Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}