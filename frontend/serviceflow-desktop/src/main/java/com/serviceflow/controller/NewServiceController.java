package com.serviceflow.controller;
import com.serviceflow.model.Service;
import com.serviceflow.service.ServiceRepository;

import java.text.NumberFormat;
import java.util.Locale;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class NewServiceController {
    @FXML private Label titleLabel;
    @FXML private TextField nameField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> durationCombo;
    @FXML private TextField priceField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Label messageLabel;

    private Service serviceToEdit;

    private static Service selectedService;

    private final NumberFormat currencyFormat =
        NumberFormat.getCurrencyInstance(
            Locale.forLanguageTag("pt-BR")
        );

    public static void setSelectedService(Service service) {
        selectedService = service;
    }

    @FXML
    private void initialize() {

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

        // O preço é atualizado sempre que a duração muda.
        durationCombo.valueProperty().addListener(
            (observable, oldValue, newValue) ->
                updatePrice(newValue)
        );

        serviceToEdit = selectedService;
        selectedService = null;

        if (serviceToEdit != null) {

            titleLabel.setText("Editar serviço");

            nameField.setText(serviceToEdit.getName());
            categoryCombo.setValue(serviceToEdit.getCategory());
            descriptionField.setText(serviceToEdit.getDescription());
            durationCombo.setValue(serviceToEdit.getDuration());
            statusCombo.setValue(serviceToEdit.getStatus());

        } else {

            titleLabel.setText("Novo serviço");
            priceField.clear();
        }
    }

    private void updatePrice(String duration) {

        if (duration == null) {
            priceField.clear();
            return;
        }

        double price = calculatePrice(duration);

        priceField.setText(currencyFormat.format(price));
    }

    private double calculatePrice(String duration) {

        int minutes;

        switch (duration) {
            case "30 minutos":
                minutes = 30;
                break;

            case "45 minutos":
                minutes = 45;
                break;

            case "1 hora":
                minutes = 60;
                break;

            case "1 hora e 30 minutos":
                minutes = 90;
                break;

            case "2 horas":
                minutes = 120;
                break;

            case "3 horas":
                minutes = 180;
                break;

            case "4 horas":
                minutes = 240;
                break;

            default:
                throw new IllegalArgumentException(
                    "Duração não reconhecida: " + duration
                );
        }

        // R$ 100,00 por hora.
        double price = minutes * (100.0 / 60.0);

        return Math.round(price * 100.0) / 100.0;
    }

    @FXML
    private void handleSave() {

        String name = nameField.getText() == null
            ? "" : nameField.getText().trim();

        String category = categoryCombo.getValue();

        String description = descriptionField.getText() == null
            ? "" : descriptionField.getText().trim();

        String duration = durationCombo.getValue();

        String status = statusCombo.getValue();

        if (name.isEmpty() || category == null
                || description.isEmpty() || duration == null
                || status == null) {

            showMessage("Preencha todos os campos.");
            return;
        }

        // O preço nunca é recebido do usuário
        // Ele é calculado diretamente pela duração
        double price = calculatePrice(duration);

        if (serviceToEdit == null) {

            ServiceRepository.add(new Service(
                name,
                category,
                description,
                duration,
                price,
                status
            ));

        } else {

            serviceToEdit.setName(name);
            serviceToEdit.setCategory(category);
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

    private void showMessage(String message) {
        messageLabel.setText(message);
    }
}
