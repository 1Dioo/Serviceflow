package com.serviceflow.controller;

import java.io.IOException;
import java.util.function.Consumer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;

public class MainController {

    @FXML
    private VBox contentArea;

    private static Consumer<String> navigator;

    @FXML
    private void initialize() {
        navigator = this::loadView;
        loadDashboard();
    }

    @FXML
    private void handleDashboard() {
        loadView("/com/serviceflow/view/DashboardView.fxml");
    }

    @FXML
    private void handleAgenda() {
        loadView("/com/serviceflow/view/AgendaView.fxml");
    }

    @FXML
    private void handleClientes() {
        loadView("/com/serviceflow/view/ClientesView.fxml");
    }

    @FXML
    private void handleServicos() {
        loadView("/com/serviceflow/view/ServicesView.fxml");
    }

    @FXML
    private void handleProfissionais() {
        loadView("/com/serviceflow/view/ProfissionaisView.fxml");
    }

    @FXML
    private void handleRelatorios() {
        loadView("/com/serviceflow/view/RelatoriosView.fxml");
    }

    @FXML
    private void handleSettings() {
        loadView("/com/serviceflow/view/SettingsView.fxml");
    }

    public static void navigateTo(String fxml) {
        if (navigator != null) {
            navigator.accept(fxml);
        }
    }

    public static void navigateToServices() {
        navigateTo("/com/serviceflow/view/ServicesView.fxml");
    }

    private void loadDashboard() {
        loadView("/com/serviceflow/view/DashboardView.fxml");
    }

    private void loadView(String fxml) {
        try {
            var resource = getClass().getResource(fxml);

            if (resource == null) {
                throw new IOException("FXML não encontrado: " + fxml);
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Node view = loader.load();

            contentArea.getChildren().setAll(view);

        } catch (IOException | RuntimeException e) {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro ao abrir tela");
            alert.setHeaderText("Não foi possível carregar a tela.");
            alert.setContentText(
                e.getMessage() == null
                    ? e.getClass().getSimpleName()
                    : e.getMessage()
            );
            alert.showAndWait();
        }
    }
}
