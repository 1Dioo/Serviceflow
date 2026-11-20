package com.serviceflow.controller;
import com.serviceflow.model.Appointment;
import com.serviceflow.model.Service;
import com.serviceflow.service.AppointmentRepository;
import com.serviceflow.service.ServiceRepository;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ServicesController {

    @FXML private TextField searchField;
    @FXML private VBox catalogContainer;
    @FXML private VBox servicesContainer;

    private static final Locale PT_BR =
        Locale.forLanguageTag("pt-BR");

    private static final String NEW_SERVICE_VIEW =
        "/com/serviceflow/view/NewServiceView.fxml";

    @FXML
    private void initialize() {
        renderCatalog();
        renderServices();

        searchField.textProperty().addListener(
            (observable, oldValue, newValue) -> {
                renderCatalog();
                renderServices();
            }
        );
    }

    // ------------------------------------------------------------------
    // Catálogo de serviços
    // ------------------------------------------------------------------

    @FXML
    private void handleNewService() {
        NewServiceController.setSelectedService(null);
        MainController.navigateTo(NEW_SERVICE_VIEW);
    }

    private void handleEditService(Service service) {
        NewServiceController.setSelectedService(service);
        MainController.navigateTo(NEW_SERVICE_VIEW);
    }

    private void handleDeleteService(Service service) {
        Alert alert = new Alert(
            Alert.AlertType.CONFIRMATION,
            "Deseja excluir o serviço \"" + service.getName() + "\" do catálogo? "
                + "Os agendamentos já registrados não serão alterados.",
            ButtonType.CANCEL,
            ButtonType.OK
        );

        alert.setTitle("Confirmar exclusão");
        alert.setHeaderText("Excluir serviço");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                ServiceRepository.remove(service);
                renderCatalog();
            }
        });
    }

    private String currentTerm() {
        return searchField.getText() == null
            ? ""
            : searchField.getText().trim().toLowerCase(PT_BR);
    }

    private void renderCatalog() {
        catalogContainer.getChildren().clear();

        String term = currentTerm();
        boolean found = false;

        for (Service service : ServiceRepository.getCatalog()) {
            boolean matches =
                service.getName().toLowerCase(PT_BR).contains(term)
                || service.getCategory().toLowerCase(PT_BR).contains(term);

            if (matches) {
                catalogContainer.getChildren().add(
                    createCatalogRow(service)
                );
                found = true;
            }
        }

        if (!found) {
            Label emptyLabel = new Label(
                "Nenhum serviço encontrado no catálogo."
            );

            emptyLabel.getStyleClass().add("service-description");
            emptyLabel.setWrapText(true);

            catalogContainer.getChildren().add(emptyLabel);
        }
    }

    private Node createCatalogRow(Service service) {
        HBox row = new HBox(15);
        row.getStyleClass().add("service-row");
        row.setAlignment(Pos.CENTER_LEFT);

        VBox info = new VBox(4);
        info.getStyleClass().add("service-info");

        Label name = new Label(service.getName());
        name.getStyleClass().add("service-name");

        Label category = new Label(
            "Categoria: " + service.getCategory()
        );
        category.getStyleClass().add("service-description");

        Label description = new Label(service.getDescription());
        description.getStyleClass().add("service-description");
        description.setWrapText(true);

        info.getChildren().addAll(name, category, description);

        Label duration = new Label(service.getDuration());
        duration.getStyleClass().add("service-data");

        Label price = new Label(
            NumberFormat.getCurrencyInstance(PT_BR).format(service.getPrice())
        );
        price.getStyleClass().add("service-price");

        Label status = new Label(service.getStatus());
        status.getStyleClass().add("service-status");

        if (!"Ativo".equalsIgnoreCase(service.getStatus())) {
            status.getStyleClass().add("service-status-inactive");
        }

        Button editButton = new Button("Editar");
        editButton.getStyleClass().add("small-button");
        editButton.setOnAction(event -> handleEditService(service));

        Button deleteButton = new Button("Excluir");
        deleteButton.getStyleClass().add("small-button-danger");
        deleteButton.setOnAction(event -> handleDeleteService(service));

        HBox actions = new HBox(6, editButton, deleteButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        row.getChildren().addAll(
            info, duration, price, status, actions
        );

        HBox.setHgrow(info, Priority.ALWAYS);

        return row;
    }

    // ------------------------------------------------------------------
    // Atendimentos agendados
    // ------------------------------------------------------------------

    private void renderServices() {
        servicesContainer.getChildren().clear();

        String term = searchField.getText() == null
            ? ""
            : searchField.getText().trim().toLowerCase(PT_BR);

        List<Appointment> appointments =
            AppointmentRepository.getAll();

        boolean found = false;

        for (Appointment appointment : appointments) {
            boolean matches =
                appointment.getService().toLowerCase(PT_BR).contains(term)
                || appointment.getCliente().toLowerCase(PT_BR).contains(term)
                || appointment.getCategory().toLowerCase(PT_BR).contains(term);

            if (matches) {
                servicesContainer.getChildren().add(
                    createServiceRow(appointment)
                );
                found = true;
            }
        }

        if (!found) {
            Label emptyLabel = new Label(
                "Nenhum atendimento agendado. "
                + "Os atendimentos aparecerão aqui após a confirmação "
                + "de um agendamento."
            );

            emptyLabel.getStyleClass().add("service-description");
            emptyLabel.setWrapText(true);

            servicesContainer.getChildren().add(emptyLabel);
        }
    }

    private Node createServiceRow(Appointment appointment) {
        HBox row = new HBox(15);
        row.getStyleClass().add("service-row");
        row.setAlignment(Pos.CENTER_LEFT);

        VBox info = new VBox(4);
        info.getStyleClass().add("service-info");

        Label name = new Label(appointment.getService());
        name.getStyleClass().add("service-name");

        Label client = new Label(
            "Cliente: " + appointment.getCliente()
        );
        client.getStyleClass().add("service-description");

        Label category = new Label(
            "Categoria: " + appointment.getCategory()
        );
        category.getStyleClass().add("service-description");

        Label description = new Label(
            appointment.getDescription()
        );
        description.getStyleClass().add("service-description");
        description.setWrapText(true);

        info.getChildren().addAll(
            name, client, category, description
        );

        Label duration = new Label(appointment.getDuration());
        duration.getStyleClass().add("service-data");

        NumberFormat currency =
            NumberFormat.getCurrencyInstance(PT_BR);

        Label price = new Label(
            currency.format(appointment.getPrice())
        );
        price.getStyleClass().add("service-price");

        Label status = new Label(appointment.getStatus());
        status.getStyleClass().add("service-status");

        Button editButton = new Button("Editar");
        editButton.getStyleClass().add("small-button");
        editButton.setOnAction(
            event -> editAppointment(appointment)
        );

        Button deleteButton = new Button("Excluir");
        deleteButton.getStyleClass().add("small-button-danger");
        deleteButton.setOnAction(
            event -> deleteAppointment(appointment)
        );

        HBox actions = new HBox(6, editButton, deleteButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        row.getChildren().addAll(
            info, duration, price, status, actions
        );

        HBox.setHgrow(info, Priority.ALWAYS);

        return row;
    }

    private void editAppointment(Appointment appointment) {
        openAppointmentForm(appointment);
    }

    private void openAppointmentForm(Appointment appointment) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                    "/com/serviceflow/view/NewAppointmentView.fxml"
                )
            );

            // Definir antes do load, pois initialize() é executado no load.
            NewAppointmentController.setSelectedAppointment(appointment);

            Parent root = loader.load();

            NewAppointmentController controller = loader.getController();
            controller.setOnAppointmentSaved(this::saveAppointment);

            Stage stage = new Stage();
            stage.setTitle("Editar atendimento");
            stage.setScene(new Scene(root, 460, 780));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

            renderServices();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Não foi possível abrir o formulário.");
        }
    }

    private void saveAppointment(Appointment appointment) {
        if (!AppointmentRepository.getAll().contains(appointment)) {
            AppointmentRepository.add(appointment);
        }

        renderServices();
    }

    private void deleteAppointment(Appointment appointment) {
        Alert alert = new Alert(
            Alert.AlertType.CONFIRMATION,
            "Deseja excluir o atendimento de \""
                + appointment.getCliente() + "\"?",
            ButtonType.CANCEL,
            ButtonType.OK
        );

        alert.setTitle("Confirmar exclusão");
        alert.setHeaderText("Excluir atendimento");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                AppointmentRepository.remove(appointment);
                renderServices();
            }
        });
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText("Operação não realizada");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
