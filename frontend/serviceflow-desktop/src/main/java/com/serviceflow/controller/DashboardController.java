package com.serviceflow.controller;

import com.serviceflow.model.Appointment;
import com.serviceflow.service.AppointmentRepository;
import com.serviceflow.service.ClientRepository;
import com.serviceflow.service.ServiceRepository;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.awt.Desktop;
import java.net.URI;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class DashboardController {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private static final DateTimeFormatter DATE_FORMAT =
        DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", PT_BR);

    private static final DateTimeFormatter TIME_FORMAT =
        DateTimeFormatter.ofPattern("HH:mm");

    private static final String AGENDA_VIEW =
        "/com/serviceflow/view/AgendaView.fxml";

    @FXML private Label dateLabel;
    @FXML private Label todayAppointmentsValue;
    @FXML private Label clientsValue;
    @FXML private Label activeServicesValue;
    @FXML private VBox upcomingContainer;
    @FXML private VBox summaryContainer;

    // Data exibida atualmente; usada para detectar a virada do dia.
    private LocalDate shownDate;

    private Timeline dayWatcher;

    @FXML
    private void initialize() {
        refresh();
        startDayWatcher();
    }

    /**
     * Recarrega todos os indicadores a partir dos repositories.
     * O Dashboard é recriado a cada navegação, então os dados
     * refletem sempre o estado atual da aplicação.
     */
    private void refresh() {
        shownDate = LocalDate.now();

        dateLabel.setText(shownDate.format(DATE_FORMAT));

        List<Appointment> today = AppointmentRepository.getAll()
            .stream()
            .filter(a -> shownDate.equals(a.getDate()))
            .sorted(Comparator.comparing(Appointment::getTime))
            .collect(Collectors.toList());

        todayAppointmentsValue.setText(String.valueOf(today.size()));
        clientsValue.setText(String.valueOf(ClientRepository.count()));
        activeServicesValue.setText(
            String.valueOf(ServiceRepository.getActive().size())
        );

        renderUpcoming(today);
        renderSummary(today);
    }

    /**
     * Atualiza o Dashboard se o dia mudar com a tela aberta
     * (por exemplo, após a meia-noite).
     */
    private void startDayWatcher() {
        dayWatcher = new Timeline(new KeyFrame(Duration.seconds(30), event -> {
            if (!LocalDate.now().equals(shownDate)) {
                refresh();
            }
        }));
        dayWatcher.setCycleCount(Timeline.INDEFINITE);
        dayWatcher.play();

        // Para o verificador quando o Dashboard sai da tela.
        dateLabel.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (oldScene != null && newScene == null) {
                dayWatcher.stop();
            }
        });
    }

    private void renderUpcoming(List<Appointment> today) {
        upcomingContainer.getChildren().clear();

        if (today.isEmpty()) {
            Label empty = new Label("Nenhum agendamento para hoje.");
            empty.getStyleClass().add("appointment-service");
            upcomingContainer.getChildren().add(empty);
            return;
        }

        for (Appointment appointment : today) {
            upcomingContainer.getChildren().add(createAppointmentRow(appointment));
        }
    }

    private HBox createAppointmentRow(Appointment appointment) {
        HBox row = new HBox(12);
        row.getStyleClass().add("appointment-row");
        row.setAlignment(Pos.CENTER_LEFT);

        Label time = new Label(appointment.getTime().format(TIME_FORMAT));
        time.getStyleClass().add("appointment-time");

        VBox info = new VBox(3);

        Label client = new Label(appointment.getCliente());
        client.getStyleClass().add("appointment-name");

        Label service = new Label(
            appointment.getService()
                + " · " + appointment.getTime().format(TIME_FORMAT)
                + " às " + appointment.getEndTime().format(TIME_FORMAT)
        );
        service.getStyleClass().add("appointment-service");

        info.getChildren().addAll(client, service);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label status = new Label(appointment.getStatus());
        status.getStyleClass().add(
            isActive(appointment) ? "status-confirmed" : "status-pending"
        );

        row.getChildren().addAll(time, info, status);

        return row;
    }

    private void renderSummary(List<Appointment> today) {
        summaryContainer.getChildren().clear();

        long active = today.stream().filter(this::isActive).count();
        long inactive = today.size() - active;

        double expected = today.stream()
            .filter(this::isActive)
            .mapToDouble(Appointment::getPrice)
            .sum();

        summaryContainer.getChildren().addAll(
            summaryRow("Agendamentos ativos", String.valueOf(active)),
            summaryRow("Agendamentos inativos", String.valueOf(inactive))
        );

        HBox total = new HBox(10);
        total.getStyleClass().add("summary-total");
        total.setAlignment(Pos.CENTER_LEFT);

        Label totalLabel = new Label("Valor previsto");
        totalLabel.getStyleClass().add("summary-total-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label totalValue = new Label(
            NumberFormat.getCurrencyInstance(PT_BR).format(expected)
        );
        totalValue.getStyleClass().add("summary-total-value");

        total.getChildren().addAll(totalLabel, spacer, totalValue);

        summaryContainer.getChildren().add(total);
    }

    private HBox summaryRow(String text, String value) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label(text);
        label.getStyleClass().add("summary-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("summary-value");

        row.getChildren().addAll(label, spacer, valueLabel);

        return row;
    }

    private boolean isActive(Appointment appointment) {
        return "Ativo".equalsIgnoreCase(appointment.getStatus())
            || "Confirmado".equalsIgnoreCase(appointment.getStatus());
    }

    @FXML
    private void handleViewAgenda() {
        MainController.navigateTo(AGENDA_VIEW);
    }

    @FXML
    private void handleGitHub() {
        try {
            Desktop.getDesktop().browse(
                new URI("https://github.com/1Dioo")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
