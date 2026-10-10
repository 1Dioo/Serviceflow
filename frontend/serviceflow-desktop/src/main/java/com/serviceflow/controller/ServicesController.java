package com.serviceflow.controller;

import com.serviceflow.model.Service;
import com.serviceflow.service.ServiceRepository;
import java.text.NumberFormat;
import java.util.Locale;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ServicesController {

    @FXML private TextField searchField;
    @FXML private VBox servicesContainer;

    private final ObservableList<Service> services =
            FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        services.setAll(ServiceRepository.getAll());
        renderServices(services);

        searchField.textProperty().addListener(
                (observable, oldValue, newValue) -> filterServices(newValue)
        );
    }

    private void filterServices(String search) {
        String term = search == null
                ? ""
                : search.trim().toLowerCase(Locale.ROOT);

        ObservableList<Service> filtered =
                FXCollections.observableArrayList();

        for (Service service : ServiceRepository.getAll()) {
            if (service.getName().toLowerCase(Locale.ROOT).contains(term)
                    || service.getDescription().toLowerCase(Locale.ROOT).contains(term)) {
                filtered.add(service);
            }
        }

        renderServices(filtered);
    }

    private void renderServices(ObservableList<Service> items) {
        servicesContainer.getChildren().clear();

        if (items.isEmpty()) {
            Label emptyLabel = new Label("Nenhum serviço encontrado.");
            emptyLabel.getStyleClass().add("service-description");
            servicesContainer.getChildren().add(emptyLabel);
            return;
        }

        for (Service service : items) {
            servicesContainer.getChildren().add(createServiceRow(service));
        }
    }

    private Node createServiceRow(Service service) {
        HBox row = new HBox(15);
        row.getStyleClass().add("service-row");
        row.setAlignment(Pos.CENTER_LEFT);

        VBox info = new VBox(3);
        info.getStyleClass().add("service-info");

        Label name = new Label(service.getName());
        name.getStyleClass().add("service-name");

        Label description = new Label(service.getDescription());
        description.getStyleClass().add("service-description");
        description.setWrapText(true);

        info.getChildren().addAll(name, description);

        Label duration = new Label(service.getDuration());
        duration.getStyleClass().add("service-data");

        NumberFormat currency = NumberFormat.getCurrencyInstance(
                Locale.forLanguageTag("pt-BR")
        );
        Label price = new Label(currency.format(service.getPrice()));
        price.getStyleClass().add("service-price");

        Label status = new Label(service.getStatus());
        status.getStyleClass().add("service-status");

        Button editButton = new Button("Editar");
        editButton.getStyleClass().add("small-button");
        editButton.setOnAction(event -> editService(service));

        Button deleteButton = new Button("Excluir");
        deleteButton.getStyleClass().add("small-button-danger");
        deleteButton.setOnAction(event -> deleteService(service));

        HBox actions = new HBox(6, editButton, deleteButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        row.getChildren().addAll(info, duration, price, status, actions);
        HBox.setHgrow(info, Priority.ALWAYS);

        return row;
    }

    @FXML
    private void handleNewService() {
        NewServiceController.setSelectedService(null);
        MainController.navigateTo(
            "/com/serviceflow/view/NewServiceView.fxml"
        );
    }

    private void editService(Service service) {
        NewServiceController.setSelectedService(service);
        MainController.navigateTo(
            "/com/serviceflow/view/NewServiceView.fxml"
        );
    }

    private void deleteService(Service service) {
        Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Deseja excluir o serviço \"" + service.getName() + "\"?",
                ButtonType.CANCEL,
                ButtonType.OK
        );

        alert.setTitle("Confirmar exclusão");
        alert.setHeaderText("Excluir serviço");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                ServiceRepository.remove(service);
                filterServices(searchField.getText());
            }
        });
    }
}
