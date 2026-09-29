package com.serviceflow.controller;
import com.serviceflow.model.Client;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class NewClientController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField emailField;

    @FXML
    private TextArea notesField;

    @FXML
    private Label messageLabel;

    @FXML
    private Label formTitle;

    @FXML
    private Label formSubtitle;

    @FXML
    private Button saveButton;

    private Consumer<Client> onClientSaved;

    private Client clientToEdit;

    private boolean editMode = false;

    public void setOnClientSaved(Consumer<Client> onClientSaved) {
        this.onClientSaved = onClientSaved;
    }

    public void setClient(Client client) {
        this.clientToEdit = client;
        this.editMode = true;

        nameField.setText(client.getName());
        phoneField.setText(client.getPhone());
        emailField.setText(client.getEmail());
        notesField.setText(client.getNotes());

        formTitle.setText("Editar cliente");
        formSubtitle.setText("Atualize as informações do cliente");
        saveButton.setText("Salvar alterações");
    }

    @FXML
    private void handleSave() {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String notes = notesField.getText().trim();

        if (name.isBlank()) {
            showMessage("Informe o nome do cliente.");
            nameField.requestFocus();
            return;
        }

        if (phone.isBlank()) {
            showMessage("Informe o telefone do cliente.");
            phoneField.requestFocus();
            return;
        }

        if (editMode) {

            clientToEdit.setName(name);
            clientToEdit.setPhone(phone);
            clientToEdit.setEmail(email);
            clientToEdit.setNotes(notes);

            if (onClientSaved != null) {
                onClientSaved.accept(clientToEdit);
            }

        } else {

            Client client = new Client(
                name,
                phone,
                email,
                notes
            );

            if (onClientSaved != null) {
                onClientSaved.accept(client);
            }
        }

        closeWindow();
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage =
            (Stage) nameField.getScene().getWindow();

        stage.close();
    }

    private void showMessage(String message) {
        messageLabel.setText(message);
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }
}