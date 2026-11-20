package com.serviceflow.controller.admin;

import java.io.IOException;

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

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        showDashboard();
    }

    @FXML
    private void showDashboard() {

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
        loadView("/com/serviceflow/view/EmployeesView.fxml");
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
        showUnavailable("Relatórios");
    }

    @FXML
    private void showSettings() {
        showUnavailable("Configurações");
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

    private void loadView(String path) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent view = loader.load();

            contentArea.getChildren().setAll(view);

        } catch (IOException e) {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Não foi possível abrir a tela.");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleLogout() {
        UserRepository.logout();

        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/com/serviceflow/view/LoginView.fxml")
            );

            Stage stage = (Stage) contentArea.getScene().getWindow();

            stage.setScene(new Scene(root, 480, 600));
            stage.setTitle("ServiceFlow - Login");
            stage.setResizable(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}