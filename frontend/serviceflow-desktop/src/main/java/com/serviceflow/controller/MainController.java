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
}