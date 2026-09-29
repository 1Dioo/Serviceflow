package com.serviceflow.controller;
import com.serviceflow.model.Client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClientesController {
    @FXML
    private TextField searchField;

    @FXML
    private Label clientCountLabel;

    @FXML
    private VBox clientsContainer;

    private final List<Client> clients = new ArrayList<>();

    @FXML
    private void initialize() {
        addExampleClients();

        searchField.textProperty().addListener(
            (observable, oldValue, newValue) -> filterClients(newValue)
        );

        renderClients();
    }

    @FXML
    private void handleNewClient() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                    "/com/serviceflow/view/NewClientView.fxml"
                )
            );

            Parent root = loader.load();
            NewClientController controller = loader.getController();

            controller.setOnClientSaved(
                this::addClient
            );

            Stage stage = new Stage();
            stage.setTitle("Novo cliente");
            stage.setScene(
                new Scene(root, 430, 560)
            );

            stage.initModality(
                Modality.APPLICATION_MODAL
            );

            stage.setResizable(false);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addClient(Client client) {
        clients.add(client);
        renderClients();
    }

    private void renderClients() {
        clientsContainer.getChildren().clear();

        for (Client client : clients) {
            addClientRow(client);
        }

        updateClientCount();
    }

    private void filterClients(String search) {
        clientsContainer.getChildren().clear();
        String query = search == null ? "" : search.trim().toLowerCase();

        for (Client client : clients) {
            boolean matchesName =
                client.getName()
                    .toLowerCase()
                    .contains(query);

            boolean matchesPhone =
                client.getPhone()
                    .toLowerCase()
                    .contains(query);

            if (matchesName || matchesPhone) {

                addClientRow(client);
            }
        }

        updateClientCount();
    }

    private void addClientRow(Client client) {
        HBox row = new HBox();

        row.setAlignment(
            javafx.geometry.Pos.CENTER_LEFT
        );

        row.getStyleClass().add("client-row");

        Label name = new Label(
            client.getName()
        );

        name.getStyleClass().add(
            "client-name"
        );
        name.setMaxWidth(Double.MAX_VALUE);

        HBox.setHgrow(
            name,
            Priority.ALWAYS
        );

        Label phone = new Label(
            client.getPhone()
        );

        phone.getStyleClass().add(
            "client-data"
        );

        phone.setPrefWidth(150);

        Label email = new Label(
            client.getEmail()
        );

        email.getStyleClass().add(
            "client-data"
        );

        email.setPrefWidth(220);

        HBox actions = new HBox(
            8
        );

        actions.setPrefWidth(150);

        Button editButton = new Button(
            "Editar"
        );

        editButton.getStyleClass().add(
            "small-button"
        );

        editButton.setOnAction(
            event -> handleEditClient(client)
        );

        Button deleteButton = new Button(
            "Excluir"
        );

        deleteButton.getStyleClass().add(
            "small-button-danger"
        );

        deleteButton.setOnAction(
            event -> handleDeleteClient(client)
        );

        actions.getChildren().addAll(
            editButton,
            deleteButton
        );

        row.getChildren().addAll(
            name,
            phone,
            email,
            actions
        );

        clientsContainer.getChildren().add(
            row
        );
    }

    private void handleEditClient(Client client) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                    "/com/serviceflow/view/NewClientView.fxml"
                )
            );

            Parent root = loader.load();
            NewClientController controller = loader.getController();
            controller.setClient(client);

            controller.setOnClientSaved(
                updateClient -> renderClients()
            );

            Stage stage = new Stage();
            stage.setTitle("Editar cliente");
            stage.setScene(
                new Scene(root, 430, 560)
            );
            stage.initModality(
                Modality.APPLICATION_MODAL
            );
            stage.setResizable(false);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteClient(Client client) {
        clients.remove(client);
        renderClients();
    }

    private void updateClientCount() {
        int count =
            clients.size();

        clientCountLabel.setText(
            count
                + (count == 1
                    ? " cliente"
                    : " clientes")
        );
    }

    private void addExampleClients() {
        clients.add(
            new Client(
                "Ana Silva",
                "(88) 99999-9999",
                "ana@email.com",
                "Cliente desde 2026"
            )
        );

        clients.add(
            new Client(
                "Bruno Oliveira",
                "(88) 98888-8888",
                "bruno@email.com",
                ""
            )
        );

        clients.add(
            new Client(
                "Carlos Eduardo",
                "(88) 97777-7777",
                "carlos@email.com",
                ""
            )
        );

        clients.add(
            new Client(
                "Maria Souza",
                "(88) 96666-6666",
                "maria@email.com",
                ""
            )
        );
    }
}