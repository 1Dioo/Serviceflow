package com.serviceflow.controller.admin;

import com.serviceflow.controller.LoginController;
import com.serviceflow.model.User;
import com.serviceflow.service.UserRepository;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Tela de autenticação do modo administrativo.
 * Só contas com perfil ADMIN conseguem acessar o painel.
 */
public class AdminLoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    private Button loginButton;

    @FXML
    private Button cancelButton;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText() == null
            ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null
            ? "" : passwordField.getText();

        if (username.isBlank() || password.isBlank()) {
            showMessage("Preencha o usuário e a senha.");
            return;
        }

        User admin = UserRepository.authenticateAdmin(username, password);

        if (admin == null) {
            passwordField.clear();
            passwordField.requestFocus();
            showMessage("Credenciais administrativas inválidas.");
            return;
        }

        Stage dialog = (Stage) loginButton.getScene().getWindow();
        Window owner = dialog.getOwner();

        UserRepository.setCurrentUser(admin);

        if (!(owner instanceof Stage)) {
            UserRepository.logout();
            showMessage("Não foi possível abrir o painel administrativo.");
            return;
        }

        if (!LoginController.openMainView(owner)) {
            UserRepository.logout();
            passwordField.clear();
            showMessage("Não foi possível abrir o painel administrativo.");
            return;
        }

        dialog.close();
    }

    @FXML
    private void handleCancel() {
        Stage dialog = (Stage) cancelButton.getScene().getWindow();
        dialog.close();
    }

    private void showMessage(String message) {
        messageLabel.setText(message);
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }
}
