package com.serviceflow.controller;
import com.serviceflow.service.UserRepository;

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

public class RegisterController {
    @FXML
    private TextField nameField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label messageLabel;

    @FXML
    private Button registerButton;

    @FXML
    private Button backButton;

    @FXML
    private void handleRegister() {
        String name = nameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (name.isBlank() || username.isBlank()
                || password.isBlank() || confirmPassword.isBlank()) {
            showMessage("Preencha todos os campos.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showMessage("As senhas não coincidem.");
            return;
        }

        if (password.length() < 8) {
            showMessage("A senha deve possuir pelo menos 8 caracteres.");
            return;
        }

        if (UserRepository.usernameExists(username)) {
            showMessage("Esse nome de usuário já está em uso.");
            return;
        }

        boolean registered = UserRepository.register(name, username, password);

        if (registered) {
            showMessage("Conta criada! Agora você já pode entrar.");
            nameField.clear();
            usernameField.clear();
            passwordField.clear();
            confirmPasswordField.clear();
        } else {
            showMessage("Não foi possível criar a conta.");
        }
    }

    @FXML
    private void handleBack() {
        openLoginView();
    }

    private void openLoginView() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                    "/com/serviceflow/view/LoginView.fxml"
                )
            );

            Parent root = loader.load();

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setTitle("ServiceFlow");
            stage.setScene(new Scene(root, 480, 600));
            stage.setResizable(false);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showMessage(String message) {
        messageLabel.setText(message);
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }
}
