package com.serviceflow.controller;

import java.io.IOException;

import com.serviceflow.model.User;
import com.serviceflow.service.LoginAttemptService;
import com.serviceflow.service.UserRepository;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
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
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isBlank() || password.isBlank()) {
            showMessage("Preencha o usuário e a senha.");
            return;
        }

        LoginAttemptService.Result result = LoginAttemptService.shared().attempt(
            LoginAttemptService.Channel.USER,
            username,
            () -> UserRepository.authenticateUser(username, password)
        );

        if (result.status() != LoginAttemptService.Status.SUCCESS) {
            passwordField.clear();
            passwordField.requestFocus();
            showMessage(
                result.status() == LoginAttemptService.Status.LOCKED
                    ? LoginAttemptService.lockMessage(result.lockRemaining())
                    : "Usuário ou senha inválidos."
            );
            return;
        }

        User user = result.user();

        UserRepository.setCurrentUser(user);

        if (!openMainView(loginButton.getScene().getWindow())) {
            UserRepository.logout();
            passwordField.clear();
            showMessage("Não foi possível abrir o sistema.");
        }
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

    @FXML
    private void handleAdminMode() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                    "/com/serviceflow/view/AdminLoginView.fxml"
                )
            );

            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.setTitle("Acesso administrativo");
            dialog.initOwner(loginButton.getScene().getWindow());
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root, 460, 540));
            dialog.setResizable(false);
            dialog.showAndWait();

        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            showMessage("Não foi possível abrir o acesso administrativo.");
        }
    }

    /**
     * Abre a tela principal de acordo com o perfil do usuário logado:
     * ADMIN vai para o painel administrativo, USER para o MainView.
     *
     * @return true se a tela foi aberta; false em caso de erro.
     */
    public static boolean openMainView(javafx.stage.Window owner) {
        try {
            User currentUser = UserRepository.getCurrentUser();

            if (currentUser == null || !(owner instanceof Stage)) {
                return false;
            }

            String viewPath;

            if (currentUser.getRole() == User.Role.ADMIN) {
                viewPath = "/com/serviceflow/view/AdminMainView.fxml";
            } else {
                viewPath = "/com/serviceflow/view/MainView.fxml";
            }

            var resource = LoginController.class.getResource(viewPath);

            if (resource == null) {
                throw new IOException("FXML não encontrado: " + viewPath);
            }

            FXMLLoader loader = new FXMLLoader(resource);

            Parent root = loader.load();
            Stage stage = (Stage) owner;
            Scene scene = new Scene(root, 1200, 750);
            stage.setTitle(
                    currentUser.getRole() == User.Role.ADMIN
                            ? "ServiceFlow - Administração"
                            : "ServiceFlow"
            );

            stage.setScene(scene);
            stage.setMinWidth(1000);
            stage.setMinHeight(650);
            stage.setResizable(true);
            stage.show();

            return true;

        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void openView(
        String fxml,
        String title,
        double width,
        double height
    ) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            Stage stage = (Stage) registerButton.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(new Scene(root, width, height));
            stage.setResizable(false);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showMessage("Não foi possível abrir a tela.");
        }
    }

    private void showMessage(String message) {
        messageLabel.setText(message);
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }
}
