package com.serviceflow.controller;
import com.serviceflow.model.Client;
import com.serviceflow.service.AppointmentRepository;
import com.serviceflow.service.ClientRepository;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientesController {
    @FXML
    private TextField searchField;

    @FXML
    private Label clientCountLabel;

    @FXML
    private VBox clientsContainer;

    @FXML
    private void initialize() {
        searchField.textProperty().addListener(
            (observable, oldValue, newValue) -> renderClients()
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
        ClientRepository.add(client);
        renderClients();
    }

    /**
     * Monta a lista sempre aplicando a busca atual, para que cadastro,
     * edição e exclusão não descartem o filtro digitado.
     */
    private void renderClients() {
        clientsContainer.getChildren().clear();

        String search = searchField.getText();
        String query = search == null ? "" : search.trim().toLowerCase();

        int shown = 0;

        for (Client client : ClientRepository.getAll()) {
            boolean matchesName =
                client.getName()
                    .toLowerCase()
                    .contains(query);

            boolean matchesPhone =
                client.getPhone()
                    .toLowerCase()
                    .contains(query);

            if (query.isEmpty() || matchesName || matchesPhone) {
                addClientRow(client);
                shown++;
            }
        }

        updateClientCount(shown, !query.isEmpty());
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
        int linked = AppointmentRepository.countByClient(client);

        if (linked > 0) {
            showDeletionBlocked(client, linked);
            return;
        }

        Alert alert = new Alert(
            Alert.AlertType.CONFIRMATION,
            "Deseja excluir o cliente \"" + client.getName() + "\"?",
            ButtonType.CANCEL,
            ButtonType.OK
        );

        alert.setTitle("Confirmar exclusão");
        alert.setHeaderText("Excluir cliente");

        alert.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) {
                return;
            }

            // A regra também é verificada no repositório, caso um
            // agendamento tenha sido vinculado enquanto o alerta estava aberto.
            ClientRepository.RemovalResult result = ClientRepository.remove(client);

            if (result == ClientRepository.RemovalResult.HAS_APPOINTMENTS) {
                showDeletionBlocked(client, AppointmentRepository.countByClient(client));
            }

            renderClients();
        });
    }

    /**
     * Clientes com agendamentos não são excluídos: os agendamentos
     * ficariam sem cliente ou teriam de ser apagados sem aviso.
     */
    private void showDeletionBlocked(Client client, int linked) {
        String appointments = linked == 1
            ? "1 agendamento vinculado"
            : linked + " agendamentos vinculados";

        Alert alert = new Alert(
            Alert.AlertType.WARNING,
            "O cliente \"" + client.getName() + "\" possui " + appointments
                + ". Exclua os agendamentos ou altere o cliente deles "
                + "antes de excluir este cadastro.",
            ButtonType.OK
        );

        alert.setTitle("Exclusão não permitida");
        alert.setHeaderText("Cliente com agendamentos");
        alert.showAndWait();
    }

    /**
     * Sem busca: "N clientes". Com busca ativa: "X de N clientes",
     * onde X é a quantidade exibida na lista.
     */
    private void updateClientCount(int shown, boolean searching) {
        int total = ClientRepository.count();

        String suffix = (searching ? total : shown) == 1
            ? " cliente"
            : " clientes";

        clientCountLabel.setText(
            searching
                ? shown + " de " + total + suffix
                : shown + suffix
        );
    }
}