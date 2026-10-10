package com.serviceflow.controller;

import com.serviceflow.model.Service;
import com.serviceflow.service.ServiceRepository;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class NewServiceController {

    @FXML private Label titleLabel;
    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> durationCombo;
    @FXML private TextField priceField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Label messageLabel;

    private Service serviceToEdit;

    private static Service selectedService;

    public static void setSelectedService(Service service) {
        selectedService = service;
    }

    @FXML
    private void initialize() {
        durationCombo.getItems().setAll(
                "30 minutos",
                "1 hora",
                "1 hora e 30 minutos",
                "2 horas",
                "3 horas",
                "4 horas"
        );

        statusCombo.getItems().setAll("Ativo", "Inativo");
        statusCombo.setValue("Ativo");

        serviceToEdit = selectedService;
        selectedService = null;

        if (serviceToEdit != null) {
            titleLabel.setText("Editar serviço");
            nameField.setText(serviceToEdit.getName());
            descriptionField.setText(serviceToEdit.getDescription());
            durationCombo.setValue(serviceToEdit.getDuration());
            priceField.setText(String.format(
                    Locale.forLanguageTag("pt-BR"),
                    "%.2f",
                    serviceToEdit.getPrice()
            ).replace('.', ','));
            statusCombo.setValue(serviceToEdit.getStatus());
        } else {
            titleLabel.setText("Novo serviço");
        }
    }

    @FXML
    private void handleSave() {
        String name = nameField.getText().trim();
        String description = descriptionField.getText().trim();
        String duration = durationCombo.getValue();
        String priceText = priceField.getText().trim();
        String status = statusCombo.getValue();

        if (name.isEmpty() || description.isEmpty()
                || duration == null || priceText.isEmpty()
                || status == null) {
            messageLabel.setText("Preencha todos os campos.");
            return;
        }

        double price;

        try {
            String normalized = priceText
                    .replace("R$", "")
                    .replace(" ", "")
                    .replace(".", "")
                    .replace(',', '.');

            price = Double.parseDouble(normalized);

            if (!Double.isFinite(price) || price < 0) {
                messageLabel.setText("Informe um preço válido.");
                return;
            }
        } catch (NumberFormatException e) {
            messageLabel.setText("Preço inválido. Ex.: 120,00");
            return;
        }

        if (serviceToEdit == null) {
            ServiceRepository.add(new Service(
                    name, description, duration, price, status
            ));
        } else {
            serviceToEdit.setName(name);
            serviceToEdit.setDescription(description);
            serviceToEdit.setDuration(duration);
            serviceToEdit.setPrice(price);
            serviceToEdit.setStatus(status);
        }

        MainController.navigateToServices();
    }

    @FXML
    private void handleCancel() {
        MainController.navigateToServices();
    }
}
