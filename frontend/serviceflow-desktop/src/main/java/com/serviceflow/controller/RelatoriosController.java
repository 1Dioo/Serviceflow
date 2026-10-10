package com.serviceflow.controller;

import java.time.LocalDate;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;

public class RelatoriosController {

    @FXML private ComboBox<String> periodCombo;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Label periodMessage;

    @FXML private Label revenueValue;
    @FXML private Label appointmentsValue;
    @FXML private Label completedValue;
    @FXML private Label cancelledValue;

    @FXML private BarChart<String, Number> revenueChart;
    @FXML private PieChart appointmentsChart;

    @FXML private Label revenueChartEmpty;
    @FXML private Label appointmentsChartEmpty;
    @FXML private Label servicesRankingEmpty;
    @FXML private Label professionalsRankingEmpty;

    @FXML
    private void initialize() {
        periodCombo.setItems(FXCollections.observableArrayList(
            "Hoje",
            "Últimos 7 dias",
            "Últimos 30 dias",
            "Este mês",
            "Personalizado"
        ));

        periodCombo.setValue("Este mês");
        aplicarPeriodo("Este mês");

        periodCombo.setOnAction(event -> {
            String periodo = periodCombo.getValue();

            if (periodo != null) {
                aplicarPeriodo(periodo);
            }
        });

        startDatePicker.valueProperty().addListener(
            (observable, anterior, atual) -> validarPeriodo()
        );

        endDatePicker.valueProperty().addListener(
            (observable, anterior, atual) -> validarPeriodo()
        );

        revenueChart.setData(FXCollections.observableArrayList());
        appointmentsChart.setData(FXCollections.observableArrayList());

        atualizarEstadoSemDados();
    }

    private void aplicarPeriodo(String periodo) {
        LocalDate hoje = LocalDate.now();
        LocalDate inicio;

        switch (periodo) {
            case "Hoje" -> inicio = hoje;
            case "Últimos 7 dias" -> inicio = hoje.minusDays(6);
            case "Últimos 30 dias" -> inicio = hoje.minusDays(29);
            case "Este mês" -> inicio = hoje.withDayOfMonth(1);
            case "Personalizado" -> {
                validarPeriodo();
                return;
            }
            default -> {
                return;
            }
        }

        startDatePicker.setValue(inicio);
        endDatePicker.setValue(hoje);

        validarPeriodo();
    }

    private void validarPeriodo() {
        LocalDate inicio = startDatePicker.getValue();
        LocalDate fim = endDatePicker.getValue();

        if (inicio == null || fim == null) {
            periodMessage.setText(
                "Informe as datas inicial e final."
            );
            return;
        }

        if (inicio.isAfter(fim)) {
            periodMessage.setText(
                "A data inicial não pode ser posterior à data final."
            );
            return;
        }

        periodMessage.setText(
            "Período selecionado: " + inicio + " até " + fim
            + ". Aguardando integração dos dados."
        );

        atualizarEstadoSemDados();
    }

    private void atualizarEstadoSemDados() {
        revenueValue.setText("R$ 0,00");
        appointmentsValue.setText("0");
        completedValue.setText("0");
        cancelledValue.setText("0");

        revenueChart.getData().clear();
        appointmentsChart.getData().clear();

        revenueChartEmpty.setVisible(true);
        revenueChartEmpty.setManaged(true);

        appointmentsChartEmpty.setVisible(true);
        appointmentsChartEmpty.setManaged(true);

        servicesRankingEmpty.setVisible(true);
        servicesRankingEmpty.setManaged(true);

        professionalsRankingEmpty.setVisible(true);
        professionalsRankingEmpty.setManaged(true);
    }
}
