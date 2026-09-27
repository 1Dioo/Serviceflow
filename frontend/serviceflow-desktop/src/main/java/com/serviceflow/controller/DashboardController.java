package com.serviceflow.controller;

import javafx.fxml.FXML;

import java.awt.Desktop;
import java.net.URI;

public class DashboardController {
    @FXML
    private void handleGitHub() {
        try {
            Desktop.getDesktop().browse(
                new URI("https://github.com/1Dioo")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}