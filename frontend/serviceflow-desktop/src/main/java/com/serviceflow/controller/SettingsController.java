
package com.serviceflow.controller;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class SettingsController {

    @FXML private TextField companyNameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextField addressField;

    @FXML private ComboBox<String> themeCombo;
    @FXML private ComboBox<String> openingTimeCombo;
    @FXML private ComboBox<String> closingTimeCombo;
    @FXML private ComboBox<String> intervalCombo;

    @FXML private CheckBox appointmentNotificationsCheck;
    @FXML private CheckBox reminderNotificationsCheck;
    @FXML private CheckBox systemNotificationsCheck;

    @FXML private Label feedbackLabel;

    private static final String DEFAULT_COMPANY = "";
    private static final String DEFAULT_PHONE = "";
    private static final String DEFAULT_EMAIL = "";
    private static final String DEFAULT_ADDRESS = "";

    @FXML
    private void initialize() {
        themeCombo.setItems(FXCollections.observableArrayList(
            "Escuro",
            "Claro",
            "Usar tema padrão"
        ));

        List<String> times = IntStream.rangeClosed(8, 22)
            .mapToObj(hour -> String.format("%02d:00", hour))
            .collect(Collectors.toList());

        openingTimeCombo.setItems(
            FXCollections.observableArrayList(times)
        );

        closingTimeCombo.setItems(
            FXCollections.observableArrayList(times)
        );

        intervalCombo.setItems(FXCollections.observableArrayList(
            "Sem intervalo",
            "5 minutos",
            "10 minutos",
            "15 minutos",
            "20 minutos",
            "30 minutos"
        ));

        restaurarValores();
    }

    @FXML
    private void handleSave() {
        String company = companyNameField.getText().trim();
        String email = emailField.getText().trim();

        if (company.isEmpty()) {
            mostrarMensagem("Informe o nome da empresa.", true);
            companyNameField.requestFocus();
            return;
        }

        if (!email.isEmpty() && !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            mostrarMensagem("Informe um e-mail válido.", true);
            emailField.requestFocus();
            return;
        }

        /*
         * Por enquanto, os valores permanecem apenas nos controles
         * desta tela. A persistência será implementada futuramente.
         */
        mostrarMensagem(
            "Campos validados. As configurações ainda não são permanentes.",
            false
        );
    }

    @FXML
    private void handleReset() {
        restaurarValores();
        mostrarMensagem("Valores iniciais restaurados nesta tela.", false);
    }

    private void restaurarValores() {
        companyNameField.setText(DEFAULT_COMPANY);
        phoneField.setText(DEFAULT_PHONE);
        emailField.setText(DEFAULT_EMAIL);
        addressField.setText(DEFAULT_ADDRESS);

        themeCombo.setValue("Escuro");
        openingTimeCombo.setValue("08:00");
        closingTimeCombo.setValue("18:00");
        intervalCombo.setValue("15 minutos");

        appointmentNotificationsCheck.setSelected(true);
        reminderNotificationsCheck.setSelected(true);
        systemNotificationsCheck.setSelected(true);

        if (feedbackLabel != null) {
            feedbackLabel.setText("");
        }
    }

    private void mostrarMensagem(String mensagem, boolean erro) {
        feedbackLabel.setText(mensagem);

        feedbackLabel.getStyleClass().removeAll(
            "configuracoes-feedback-error",
            "configuracoes-feedback-success"
        );

        feedbackLabel.getStyleClass().add(
            erro
                ? "configuracoes-feedback-error"
                : "configuracoes-feedback-success"
        );
    }
}
