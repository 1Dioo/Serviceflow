
package com.serviceflow.controller;

import com.serviceflow.model.Appointment;
import com.serviceflow.service.AppointmentRepository;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AgendaController {

    @FXML private Label dateLabel;
    @FXML private Label dayLabel;
    @FXML private VBox scheduleContainer;

    private LocalDate selectedDate = LocalDate.now();

    private static final DateTimeFormatter TIME_FORMAT =
        DateTimeFormatter.ofPattern("HH:mm");

    private final DateTimeFormatter dateFormatter =
        DateTimeFormatter.ofPattern(
            "dd 'de' MMMM 'de' yyyy",
            new Locale("pt", "BR")
        );

    @FXML
    private void initialize() {
        updateDate();
        renderSchedule();
    }

    @FXML
    private void handlePreviousDay() {
        selectedDate = selectedDate.minusDays(1);
        updateDate();
        renderSchedule();
    }

    @FXML
    private void handleNextDay() {
        selectedDate = selectedDate.plusDays(1);
        updateDate();
        renderSchedule();
    }

    @FXML
    private void handleToday() {
        selectedDate = LocalDate.now();
        updateDate();
        renderSchedule();
    }

    @FXML
    private void handleNewAppointment() {
        openAppointmentForm(null);
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

            NewAppointmentController controller =
                loader.getController();

            controller.setSelectedDate(selectedDate);
            controller.setOnAppointmentSaved(this::saveAppointment);

            Stage stage = new Stage();
            stage.setTitle(
                appointment == null
                    ? "Novo agendamento"
                    : "Editar agendamento"
            );

            stage.setScene(new Scene(root, 460, 780));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

            renderSchedule();

        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            showError("Não foi possível abrir o formulário de agendamento.");
        }
    }

    private void saveAppointment(Appointment appointment) {
        if (!AppointmentRepository.getAll().contains(appointment)) {
            AppointmentRepository.add(appointment);
        }

        renderSchedule();
    }

    private void updateDate() {
        dateLabel.setText(selectedDate.format(dateFormatter));

        String day = selectedDate.getDayOfWeek().getDisplayName(
            TextStyle.FULL,
            new Locale("pt", "BR")
        );

        day = Character.toUpperCase(day.charAt(0))
            + day.substring(1);

        dayLabel.setText(day);
    }

    private void renderSchedule() {
        scheduleContainer.getChildren().clear();

        for (int hour = 8; hour <= 18; hour++) {
            addTimeRow(hour, 0);

            if (hour < 18) {
                addTimeRow(hour, 30);
            }
        }
    }

    private void addTimeRow(int hour, int minute) {
        HBox row = new HBox();

        row.getStyleClass().add("time-row");
        row.setMinHeight(65);

        Label timeLabel = new Label(
            String.format("%02d:%02d", hour, minute)
        );

        timeLabel.getStyleClass().add("time-label");
        row.getChildren().add(timeLabel);

        Region separator = new Region();
        separator.getStyleClass().add("time-separator");
        row.getChildren().add(separator);

        LocalTime slot = LocalTime.of(hour, minute);

        // Todos os agendamentos que começam neste horário
        // (nenhum deles é ocultado).
        List<Appointment> starting = findAppointmentsStartingAt(slot);

        // Agendamentos iniciados antes que ainda ocupam este horário.
        List<Appointment> continuing = findAppointmentsContinuingAt(slot);

        if (!starting.isEmpty() || !continuing.isEmpty()) {
            row.setSpacing(6);

            for (Appointment appointment : starting) {
                row.getChildren().add(
                    createAppointmentBlock(appointment)
                );
            }

            for (Appointment appointment : continuing) {
                row.getChildren().add(
                    createContinuationBlock(appointment)
                );
            }
        } else {
            Region emptySpace = new Region();

            HBox.setHgrow(
                emptySpace,
                javafx.scene.layout.Priority.ALWAYS
            );

            row.getChildren().add(emptySpace);
        }

        scheduleContainer.getChildren().add(row);
    }

    private List<Appointment> findAppointmentsStartingAt(LocalTime slot) {
        List<Appointment> result = new ArrayList<>();

        for (Appointment appointment : AppointmentRepository.getAll()) {
            if (appointment.getDate().equals(selectedDate)
                    && appointment.getTime().equals(slot)) {
                result.add(appointment);
            }
        }

        return result;
    }

    private List<Appointment> findAppointmentsContinuingAt(LocalTime slot) {
        List<Appointment> result = new ArrayList<>();

        for (Appointment appointment : AppointmentRepository.getAll()) {
            if (appointment.getDate().equals(selectedDate)
                    && appointment.getTime().isBefore(slot)
                    && appointment.getEndTime().isAfter(slot)) {
                result.add(appointment);
            }
        }

        return result;
    }

    private String formatPeriod(Appointment appointment) {
        return appointment.getTime().format(TIME_FORMAT)
            + " às "
            + appointment.getEndTime().format(TIME_FORMAT);
    }

    /**
     * Indica que o horário está ocupado por um atendimento
     * iniciado em um horário anterior.
     */
    private VBox createContinuationBlock(Appointment appointment) {
        VBox block = new VBox(2);
        block.getStyleClass().addAll(
            "appointment-block",
            "appointment-continuation"
        );

        Label client = new Label(
            "Ocupado · " + appointment.getCliente()
        );
        client.getStyleClass().add("agenda-service");

        Label period = new Label(
            "Continuação (" + formatPeriod(appointment) + ")"
        );
        period.getStyleClass().add("agenda-service");

        block.getChildren().addAll(client, period);

        HBox.setHgrow(
            block,
            javafx.scene.layout.Priority.ALWAYS
        );

        return block;
    }

    private VBox createAppointmentBlock(Appointment appointment) {
        VBox block = new VBox();
        block.getStyleClass().add("appointment-block");
        block.setSpacing(3);

        Label client = new Label(appointment.getCliente());
        client.getStyleClass().add("agenda-client");

        Label service = new Label(
            appointment.getService() + " · " + formatPeriod(appointment)
        );
        service.getStyleClass().add("agenda-service");

        Label status = new Label(appointment.getStatus());

        if ("Ativo".equalsIgnoreCase(appointment.getStatus())
                || "Confirmado".equalsIgnoreCase(appointment.getStatus())) {
            status.getStyleClass().add("status-confirmed");
        } else {
            status.getStyleClass().add("status-pending");
        }

        Button editButton = new Button("Editar");
        editButton.getStyleClass().add("small-button");
        editButton.setOnAction(
            event -> openAppointmentForm(appointment)
        );

        Button deleteButton = new Button("Excluir");
        deleteButton.getStyleClass().add("small-button-danger");
        deleteButton.setOnAction(
            event -> handleDeleteAppointment(appointment)
        );

        HBox actions = new HBox(6, editButton, deleteButton);

        block.getChildren().addAll(
            client,
            service,
            status,
            actions
        );

        HBox.setHgrow(
            block,
            javafx.scene.layout.Priority.ALWAYS
        );

        return block;
    }

    private void handleDeleteAppointment(Appointment appointment) {
        Alert alert = new Alert(
            Alert.AlertType.CONFIRMATION,
            "Deseja excluir o agendamento de \""
                + appointment.getCliente()
                + "\" em "
                + appointment.getTime()
                + "?",
            ButtonType.CANCEL,
            ButtonType.OK
        );

        alert.setTitle("Confirmar exclusão");
        alert.setHeaderText("Excluir agendamento");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                AppointmentRepository.remove(appointment);
                renderSchedule();
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
