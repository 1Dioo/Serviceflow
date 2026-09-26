package com.serviceflow.controller;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
    @FXML 
    private TextField usernameField;

    @FXML 
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;
    
    @FXML 
    private Button loginButton;

    @FXML 
    private Button registerButton;

    @FXML 
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isBlank() || password.isBlank()) {
            showMessage("Preencha o usuário e a senha");
            return;
        }
        openMainView();
    }

    @FXML 
    private void handleRegister() {
        openView(
            "/com/serviceflow/view/RegisterView.fxml",
            "Criar conta",
            480,
            650
        );
    }

    private void openMainView() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                    "/com/serviceflow/view/MainView.fxml"
                )
            );

            Parent root = loader.load();
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root, 1100, 700);

            stage.setTitle("Serviceflow");
            stage.setScene(scene);

            stage.setMinWidth(900);
            stage.setMinHeight(600);
            stage.setResizable(true);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showMessage("Não foi possível abrir a aplicação");
        }
    }

    private void openView(
        String fxml,
        String title,
        double width,
        double height
    ) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource(fxml)
            );

            Parent root = loader.load();
            Stage stage = (Stage) registerButton.getScene().getWindow();
            Scene scene = new Scene(root, width, height);

            stage.setTitle(title);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showMessage("Não foi possível abrir a tela");
        }
    }

    private void showMessage(String message) {
        messageLabel.setText(message);
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }
}