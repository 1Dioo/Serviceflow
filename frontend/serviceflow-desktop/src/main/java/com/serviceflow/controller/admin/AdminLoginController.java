package com.serviceflow.controller.admin;

import com.serviceflow.controller.LoginController;
import com.serviceflow.model.User;
import com.serviceflow.service.AdminAccountConfig;
import com.serviceflow.service.LoginAttemptService;
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
 * Sem configuração administrativa válida, a tela informa o estado
 * e bloqueia a autenticação.
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

    static final String NOT_CONFIGURED_MESSAGE =
        "Modo administrativo não configurado. Defina uma senha de pelo menos "
        + AdminAccountConfig.MIN_PASSWORD_LENGTH + " caracteres em "
        + AdminAccountConfig.PASSWORD_ENV + " ou -D"
        + AdminAccountConfig.PASSWORD_PROPERTY
        + " e reinicie o ServiceFlow.";

    @FXML
    private void initialize() {
        if (!UserRepository.isAdminConfigured()) {
            usernameField.setDisable(true);
            passwordField.setDisable(true);
            loginButton.setDisable(true);
            loginButton.setDefaultButton(false);
            showMessage(NOT_CONFIGURED_MESSAGE);
        }
    }

    @FXML
    private void handleLogin() {
        if (!UserRepository.isAdminConfigured()) {
            passwordField.clear();
            showMessage(NOT_CONFIGURED_MESSAGE);
            return;
        }

        String username = usernameField.getText() == null
            ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null
            ? "" : passwordField.getText();

        if (username.isBlank() || password.isBlank()) {
            showMessage("Preencha o usuário e a senha.");
            return;
        }

        LoginAttemptService.Result result = LoginAttemptService.shared().attempt(
            LoginAttemptService.Channel.ADMIN,
            username,
            () -> UserRepository.authenticateAdmin(username, password)
        );

        if (result.status() != LoginAttemptService.Status.SUCCESS) {
            passwordField.clear();
            passwordField.requestFocus();
            showMessage(
                result.status() == LoginAttemptService.Status.LOCKED
                    ? LoginAttemptService.lockMessage(result.lockRemaining())
                    : "Credenciais administrativas inválidas."
            );
            return;
        }

        User admin = result.user();

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
