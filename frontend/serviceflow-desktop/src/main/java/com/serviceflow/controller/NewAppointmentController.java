package com.serviceflow.controller;
import com.serviceflow.model.Appointment;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.function.Consumer;

public class NewAppointmentController {
    @FXML 
    private TextField clientField;

    @FXML 
    private TextField serviceField;

    @FXML
    private DatePicker datePicker;

    @FXML 
    private ComboBox<String> timeComboBox;

    @FXML 
    private Label messageLabel;

    @FXML 
    private Button saveButton;

    @FXML 
    private Consumer<Appointment> onAppointmentSaved;

    @FXML 
    private void initialize() {
        datePicker.setValue(LocalDate.now());

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
    }

    public void setSelectedDate(LocalDate date) {
        datePicker.setValue(date);
    }

    public void setOnAppointmentSaved(Consumer<Appointment> onAppointmentSaved) {
        this.onAppointmentSaved = onAppointmentSaved;
    }

    @FXML 
    public void handleSave() {
        String client = clientField.getText();
        String service = serviceField.getText();
        LocalDate date = datePicker.getValue();
        String selectedTime = timeComboBox.getValue();

        if (client.isBlank() || service.isBlank() || date == null || selectedTime == null) {
            showMessage("Preencha todos os campos");
            return ;
        }

        LocalTime time = LocalTime.parse(selectedTime);

        Appointment appointment = new Appointment(
            date,
            time,
            client,
            service,
            "Pendente"
        );

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
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    private void showMessage(String message) {
        messageLabel.setText(message);
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }
}
