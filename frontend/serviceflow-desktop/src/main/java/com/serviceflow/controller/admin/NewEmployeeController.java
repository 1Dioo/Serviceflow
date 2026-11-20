package com.serviceflow.controller.admin;

import com.serviceflow.model.Employee;
import com.serviceflow.service.EmployeeRepository;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class NewEmployeeController {

    @FXML private TextField nameField;
    @FXML private TextField contactField;
    @FXML private TextField educationField;
    @FXML private TextField specialtiesField;
    @FXML private TextField experienceField;

    @FXML private ComboBox<String> areaComboBox;
    @FXML private ComboBox<String> statusComboBox;

    @FXML
    public void initialize() {

        areaComboBox.getItems().addAll(
                "Frontend",
                "Backend",
                "Fullstack",
                "Design",
                "Suporte técnico",
                "Outro"
        );

        statusComboBox.getItems().addAll(
                "Ativo",
                "Inativo"
        );

        statusComboBox.setValue("Ativo");
    }

    @FXML
    private void handleSave() {

        String name = nameField.getText().trim();
        String contact = contactField.getText().trim();
        String education = educationField.getText().trim();
        String area = areaComboBox.getValue();
        String specialties = specialtiesField.getText().trim();
        String experience = experienceField.getText().trim();
        String status = statusComboBox.getValue();

        if (name.isEmpty()
                || contact.isEmpty()
                || education.isEmpty()
                || area == null
                || specialties.isEmpty()
                || experience.isEmpty()
                || status == null) {

            showMessage(
                    Alert.AlertType.WARNING,
                    "Preencha todos os campos antes de continuar."
            );

            return;
        }

        Employee employee = new Employee(
                name,
                contact,
                education,
                area,
                specialties,
                experience,
                status
        );

        EmployeeRepository.add(employee);

        showMessage(
                Alert.AlertType.INFORMATION,
                "Funcionário cadastrado com sucesso!"
        );

        closeWindow();
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    private void showMessage(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("ServiceFlow");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}