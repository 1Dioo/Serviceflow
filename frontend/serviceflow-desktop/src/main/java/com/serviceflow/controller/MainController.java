package com.serviceflow.controller;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

public class MainController {
    @FXML
    private VBox contentArea;

    @FXML
    private void initialize() {
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

    private void loadDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/serviceflow/view/DashboardView.fxml")
            );

            Node dashboard = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(dashboard);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadView(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource(fxml)
            );

            Node view = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}