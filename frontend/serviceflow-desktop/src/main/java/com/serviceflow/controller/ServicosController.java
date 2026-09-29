package com.serviceflow.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ServicosController {
    @FXML
    private TextField searchField;

    @FXML
    private VBox servicesContainer;

    @FXML
    private void initialize() {
        createService(
            "Manutenção de computador",
            "Diagnóstico e manutenção de hardware",
            "2h",
            "R$ 120,00"
        );

        createService(
            "Formatação e instalação do sistema",
            "Formatação e configuração do sistema operacional",
            "2h",
            "R$ 100,00"
        );

        createService(
            "Configuração de rede",
            "Configuração de rede local e dispositivos",
            "1h 30min",
            "R$ 90,00"
        );

        createService(
            "Manutenção de notebook",
            "Diagnóstico e manutenção de notebooks",
            "2h",
            "R$ 150,00"
        );

        createService(
            "Instalação de software",
            "Instalação e configuração de softwares",
            "1h",
            "R$ 60,00"
        );

        createService(
            "Suporte técnico",
            "Atendimento para problemas de software e hardware",
            "1h",
            "R$ 50,00"
        );
    }

    private void createService(
            String name,
            String description,
            String duration,
            String price) {

        HBox row = new HBox();
        row.setSpacing(15);
        row.getStyleClass().add("service-row");

        VBox serviceInfo = new VBox(3);
        serviceInfo.getStyleClass().add("service-info");

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("service-name");

        Label descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().add("service-description");

        serviceInfo.getChildren().addAll(
            nameLabel,
            descriptionLabel
        );

        Label durationLabel = new Label(duration);
        durationLabel.getStyleClass().add("service-data");

        Label priceLabel = new Label(price);
        priceLabel.getStyleClass().add("service-price");

        Label statusLabel = new Label("Ativo");
        statusLabel.getStyleClass().add("service-status");

        Button editButton = new Button("Editar");
        editButton.getStyleClass().add("small-button");

        Button deleteButton = new Button("Excluir");
        deleteButton.getStyleClass().add("small-button-danger");

        HBox actions = new HBox(6);
        actions.getChildren().addAll(
            editButton,
            deleteButton
        );

        row.getChildren().addAll(
            serviceInfo,
            durationLabel,
            priceLabel,
            statusLabel,
            actions
        );

        HBox.setHgrow(serviceInfo, javafx.scene.layout.Priority.ALWAYS);

        servicesContainer.getChildren().add(row);
    }

    @FXML
    private void handleNewService() {
        System.out.println("Novo serviço");
    }
}