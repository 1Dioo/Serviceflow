package com.serviceflow.controller.admin;

import java.io.IOException;
import java.util.List;

import com.serviceflow.model.Employee;
import com.serviceflow.service.EmployeeRepository;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class EmployeesController {

    @FXML private TableView<Employee> employeeTable;

    @FXML private TableColumn<Employee, String> nameColumn;
    @FXML private TableColumn<Employee, String> contactColumn;
    @FXML private TableColumn<Employee, String> areaColumn;
    @FXML private TableColumn<Employee, String> educationColumn;
    @FXML private TableColumn<Employee, String> experienceColumn;
    @FXML private TableColumn<Employee, String> statusColumn;

    @FXML private Label totalLabel;
    @FXML private Label activeLabel;

    @FXML private TextField searchField;

    @FXML
    public void initialize() {

        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        contactColumn.setCellValueFactory(data -> data.getValue().contactProperty());
        areaColumn.setCellValueFactory(data -> data.getValue().areaProperty());
        educationColumn.setCellValueFactory(data -> data.getValue().educationProperty());
        experienceColumn.setCellValueFactory(data -> data.getValue().experienceProperty());
        statusColumn.setCellValueFactory(data -> data.getValue().statusProperty());

        employeeTable.setItems(EmployeeRepository.findAll());

        updateStatistics();
    }

    @FXML
    private void handleNewEmployee() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/serviceflow/view/NewEmployeeView.fxml")
            );

            Parent root = loader.load();

            Stage dialog = new Stage();

            dialog.setTitle("Cadastrar funcionário");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(employeeTable.getScene().getWindow());
            dialog.setScene(new Scene(root, 500, 650));
            dialog.setResizable(false);

            dialog.showAndWait();

            employeeTable.refresh();
            updateStatistics();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Não foi possível abrir o cadastro de funcionários.");
        }
    }

    @FXML
    private void handleSearch() {

        String text = searchField.getText().trim();

        if (text.isEmpty()) {
            employeeTable.setItems(EmployeeRepository.findAll());
            return;
        }

        List<Employee> results = EmployeeRepository.search(text);

        employeeTable.setItems(
                FXCollections.observableArrayList(results)
        );
    }

    @FXML
    private void handleRemoveEmployee() {

        Employee selected = employeeTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Selecione um funcionário para remover.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);

        confirmation.setTitle("Remover funcionário");
        confirmation.setHeaderText("Deseja remover este funcionário?");
        confirmation.setContentText(selected.getName());

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                EmployeeRepository.remove(selected);
                employeeTable.setItems(EmployeeRepository.findAll());
                updateStatistics();
            }
        });
    }

    private void updateStatistics() {
        totalLabel.setText(String.valueOf(EmployeeRepository.count()));
        activeLabel.setText(String.valueOf(EmployeeRepository.countActive()));
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atenção");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}