package com.serviceflow.controller;
import com.serviceflow.model.Appointment;
import com.serviceflow.model.Service;
import com.serviceflow.service.ServiceRepository;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class NewAppointmentController {
    @FXML
    private TextField clientField;

    @FXML
    private ComboBox<String> serviceComboBox;

    @FXML
    private ComboBox<String> categoryCombo;

    @FXML
    private TextArea descriptionField;

    @FXML
    private ComboBox<String> durationCombo;

    @FXML
    private TextField priceField;

    @FXML
    private DatePicker datePicker;

    @FXML
    private ComboBox<String> timeComboBox;

    @FXML
    private ComboBox<String> statusCombo;

    @FXML
    private Label messageLabel;

    private static Appointment selectedAppointment;

    private Consumer<Appointment> onAppointmentSaved;

    private Appointment appointmentToEdit;

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    public static void setSelectedAppointment(Appointment appointment) {
        selectedAppointment = appointment;
    }

    public void setOnAppointmentSaved(Consumer<Appointment> callback) {
        this.onAppointmentSaved = callback;
    }

    @FXML
    private void initialize() {
        appointmentToEdit = selectedAppointment;
        selectedAppointment = null;

        datePicker.setValue(LocalDate.now());

        serviceComboBox.getItems().setAll(
            ServiceRepository.getActive()
                .stream()
                .map(Service::getName)
                .collect(Collectors.toList())
        );

        categoryCombo.getItems().setAll(
            "Manutenção de computadores",
            "Software e sistemas",
            "Redes e internet",
            "Suporte técnico",
            "Outros"
        );

        durationCombo.getItems().setAll(
            "30 minutos",
            "45 minutos",
            "1 hora",
            "1 hora e 30 minutos",
            "2 horas",
            "3 horas",
            "4 horas"
        );

        statusCombo.getItems().setAll("Ativo", "Inativo");
        statusCombo.setValue("Ativo");

        for (int hour = 8; hour <= 18; hour++) {
            timeComboBox.getItems().add(
                String.format("%02d:00", hour)
            );

            if (hour < 18) {
                timeComboBox.getItems().add(
                    String.format("%02d:30", hour)
                );
            }
        }

        durationCombo.valueProperty().addListener(
            (observable, oldValue, newValue) -> updatePrice(newValue)
        );

        if (appointmentToEdit != null) {
            loadAppointmentData();
        }
    }

    private void loadAppointmentData() {
        clientField.setText(appointmentToEdit.getCliente());
        serviceComboBox.setValue(appointmentToEdit.getService());
        categoryCombo.setValue(appointmentToEdit.getCategory());
        descriptionField.setText(appointmentToEdit.getDescription());
        durationCombo.setValue(appointmentToEdit.getDuration());
        datePicker.setValue(appointmentToEdit.getDate());
        timeComboBox.setValue(appointmentToEdit.getTime().toString());
        statusCombo.setValue(appointmentToEdit.getStatus());

        updatePrice(appointmentToEdit.getDuration());
    }

    public void setSelectedDate(LocalDate date) {
        if (appointmentToEdit == null && date != null) {
            datePicker.setValue(date);
        }
    }

    private void updatePrice(String duration) {
        if (duration == null) {
            priceField.clear();
            return;
        }

        int minutes = getDurationInMinutes(duration);

        if (minutes <= 0) {
            priceField.clear();
            return;
        }

        double price = (minutes / 60.0) * 100.0;

        NumberFormat currency =
            NumberFormat.getCurrencyInstance(PT_BR);

        priceField.setText(currency.format(price));
    }

    private int getDurationInMinutes(String duration) {
        switch (duration) {
            case "30 minutos": return 30;
            case "45 minutos": return 45;
            case "1 hora": return 60;
            case "1 hora e 30 minutos": return 90;
            case "2 horas": return 120;
            case "3 horas": return 180;
            case "4 horas": return 240;
            default: return 0;
        }
    }

    @FXML
    private void handleSave() {
        String client = clientField.getText() == null
            ? "" : clientField.getText().trim();

        String service = serviceComboBox.getValue();
        String category = categoryCombo.getValue();

        String description = descriptionField.getText() == null
            ? "" : descriptionField.getText().trim();

        String duration = durationCombo.getValue();
        LocalDate date = datePicker.getValue();
        String selectedTime = timeComboBox.getValue();
        String status = statusCombo.getValue();

        if (client.isEmpty() || service == null || category == null
                || description.isEmpty() || duration == null
                || date == null || selectedTime == null
                || status == null) {
            showMessage("Preencha todos os campos.");
            return;
        }

        int minutes = getDurationInMinutes(duration);
        double price = (minutes / 60.0) * 100.0;

        Appointment appointment;

        if (appointmentToEdit == null) {
            appointment = new Appointment(
                date,
                LocalTime.parse(selectedTime),
                client,
                service,
                category,
                description,
                duration,
                price,
                status
            );
        } else {
            appointment = appointmentToEdit;

            appointment.setDate(date);
            appointment.setTime(LocalTime.parse(selectedTime));
            appointment.setCliente(client);
            appointment.setService(service);
            appointment.setCategory(category);
            appointment.setDescription(description);
            appointment.setDuration(duration);
            appointment.setPrice(price);
            appointment.setStatus(status);
        }

        if (onAppointmentSaved != null) {
            onAppointmentSaved.accept(appointment);
        }

        closeWindow();
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) clientField.getScene().getWindow();
        stage.close();
    }

    private void showMessage(String message) {
        messageLabel.setText(message);
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }
}
