package com.serviceflow.controller;
import com.serviceflow.model.Appointment;
import com.serviceflow.model.Service;
import com.serviceflow.service.AppointmentRepository;
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
import java.time.format.DateTimeFormatter;
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

    // Evita que o carregamento de um agendamento existente
    // sobrescreva o preço salvo com o preço atual do catálogo.
    private boolean loadingAppointment = false;

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private static final DateTimeFormatter TIME_FORMAT =
        DateTimeFormatter.ofPattern("HH:mm");

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

        // Categoria, duração e preço vêm do serviço selecionado.
        categoryCombo.setDisable(true);
        durationCombo.setDisable(true);

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

        serviceComboBox.valueProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (!loadingAppointment) {
                    applyService(newValue);
                }
            }
        );

        if (appointmentToEdit != null) {
            loadAppointmentData();
        }
    }

    private void loadAppointmentData() {
        loadingAppointment = true;

        try {
            clientField.setText(appointmentToEdit.getCliente());
            serviceComboBox.setValue(appointmentToEdit.getService());
            categoryCombo.setValue(appointmentToEdit.getCategory());
            descriptionField.setText(appointmentToEdit.getDescription());
            durationCombo.setValue(appointmentToEdit.getDuration());
            datePicker.setValue(appointmentToEdit.getDate());
            timeComboBox.setValue(
                appointmentToEdit.getTime().format(TIME_FORMAT)
            );
            statusCombo.setValue(appointmentToEdit.getStatus());

            // Mantém o valor combinado no agendamento original.
            showPrice(appointmentToEdit.getPrice());

        } finally {
            loadingAppointment = false;
        }
    }

    public void setSelectedDate(LocalDate date) {
        if (appointmentToEdit == null && date != null) {
            datePicker.setValue(date);
        }
    }

    /**
     * Preenche categoria, duração e preço a partir do serviço do catálogo.
     */
    private void applyService(String serviceName) {
        Service service = ServiceRepository.findByName(serviceName);

        if (service == null) {
            categoryCombo.setValue(null);
            durationCombo.setValue(null);
            priceField.clear();
            return;
        }

        categoryCombo.setValue(service.getCategory());
        durationCombo.setValue(service.getDuration());
        showPrice(service.getPrice());
    }

    private void showPrice(double price) {
        NumberFormat currency =
            NumberFormat.getCurrencyInstance(PT_BR);

        priceField.setText(currency.format(price));
    }

    private boolean isOriginalService(String serviceName) {
        return appointmentToEdit != null
            && serviceName != null
            && serviceName.equals(appointmentToEdit.getService());
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

        double price;

        if (isOriginalService(service)) {
            // Serviço não mudou: preserva o preço já registrado.
            price = appointmentToEdit.getPrice();
        } else {
            Service catalogService = ServiceRepository.findByName(service);

            if (catalogService == null
                    || !"Ativo".equalsIgnoreCase(catalogService.getStatus())) {
                showMessage("O serviço selecionado não está disponível.");
                return;
            }

            category = catalogService.getCategory();
            duration = catalogService.getDuration();
            price = catalogService.getPrice();
        }

        LocalTime time = LocalTime.parse(selectedTime);

        int minutes = Appointment.durationToMinutes(duration);

        if (minutes <= 0) {
            minutes = 30;
        }

        Appointment conflict = AppointmentRepository.findConflict(
            date, time, minutes, appointmentToEdit
        );

        if (conflict != null) {
            showMessage(
                "Horário indisponível: conflita com o agendamento de "
                + conflict.getCliente()
                + " (" + conflict.getTime().format(TIME_FORMAT)
                + " às " + conflict.getEndTime().format(TIME_FORMAT) + ")."
            );
            return;
        }

        Appointment appointment;

        if (appointmentToEdit == null) {
            appointment = new Appointment(
                date,
                time,
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
            appointment.setTime(time);
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
