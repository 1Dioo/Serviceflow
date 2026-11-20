package com.serviceflow.controller.admin;

import java.io.IOException;
import java.net.URL;

import com.serviceflow.model.User;
import com.serviceflow.service.UserRepository;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.Scene;

public class AdminMainController {

    private static final String STYLESHEET = "/com/serviceflow/css/style.css";

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        if (!isAdmin()) {
            showRestricted();
            return;
        }

        showDashboard();
    }

    @FXML
    private void showDashboard() {
        if (!ensureAdmin()) {
            return;
        }

        Label title = new Label(
                "Bem-vindo ao painel administrativo do ServiceFlow"
        );

        title.setStyle(
                "-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #202438;"
        );

        contentArea.getChildren().setAll(title);
    }

    @FXML
    private void showEmployees() {
        loadView("/com/serviceflow/view/EmployeesView.fxml", false);
    }

    @FXML
    private void showAgenda() {
        showUnavailable("Agenda");
    }

    @FXML
    private void showClients() {
        showUnavailable("Clientes");
    }

    @FXML
    private void showServices() {
        showUnavailable("Serviços");
    }

    @FXML
    private void showReports() {
        // Telas do tema escuro: recebem o style.css apenas no próprio nó,
        // sem alterar o visual das telas administrativas já existentes.
        loadView("/com/serviceflow/view/RelatoriosView.fxml", true);
    }

    @FXML
    private void showSettings() {
        loadView("/com/serviceflow/view/SettingsView.fxml", true);
    }

    private void showUnavailable(String section) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("ServiceFlow");
        alert.setHeaderText(section);
        alert.setContentText(
                "Essa seção será integrada ao painel administrativo posteriormente."
        );
        alert.showAndWait();
    }

    private boolean isAdmin() {
        User currentUser = UserRepository.getCurrentUser();

        return currentUser != null
                && currentUser.getRole() == User.Role.ADMIN;
    }

    /**
     * Garante que apenas administradores autenticados usem o painel.
     */
    private boolean ensureAdmin() {
        if (isAdmin()) {
            return true;
        }

        showRestricted();

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Acesso restrito");
        alert.setHeaderText("Permissão necessária");
        alert.setContentText(
                "Essa funcionalidade está disponível apenas para administradores."
        );
        alert.showAndWait();

        return false;
    }

    private void showRestricted() {
        Label message = new Label(
                "Acesso restrito a administradores autenticados."
        );

        message.setStyle(
                "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #d94c4c;"
        );

        contentArea.getChildren().setAll(message);
    }

    private void loadView(String path, boolean applyAppStylesheet) {
        if (!ensureAdmin()) {
            return;
        }

        try {
            URL resource = getClass().getResource(path);

            if (resource == null) {
                throw new IOException("FXML não encontrado: " + path);
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent view = loader.load();

            if (applyAppStylesheet) {
                URL stylesheet = getClass().getResource(STYLESHEET);

                if (stylesheet != null) {
                    view.getStylesheets().add(stylesheet.toExternalForm());
                }
            }

            contentArea.getChildren().setAll(view);

        } catch (IOException | RuntimeException e) {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Não foi possível abrir a tela.");
            alert.setContentText(
                    e.getMessage() == null
                            ? e.getClass().getSimpleName()
                            : e.getMessage()
            );
            alert.showAndWait();
        }
    }

    @FXML
    private void handleLogout() {
        UserRepository.logout();

        try {
            URL resource = getClass().getResource(
                    "/com/serviceflow/view/LoginView.fxml"
            );

            if (resource == null) {
                throw new IOException("FXML não encontrado: LoginView.fxml");
            }

            Parent root = FXMLLoader.load(resource);

            Stage stage = (Stage) contentArea.getScene().getWindow();

            // Volta aos limites da tela de login (o painel define 1000x650).
            stage.setMaximized(false);
            stage.setMinWidth(360);
            stage.setMinHeight(450);

            stage.setScene(new Scene(root, 480, 600));
            stage.setTitle("ServiceFlow");
            stage.setResizable(true);
            stage.sizeToScene();
            stage.centerOnScreen();
            stage.show();

        } catch (IOException | RuntimeException e) {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro ao sair");
            alert.setHeaderText("Não foi possível retornar ao login.");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}
