package com.serviceflow.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ProfissionaisController {

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> availabilityFilter;

    @FXML
    private ComboBox<String> areaFilter;

    @FXML
    private FlowPane professionalsContainer;

    @FXML
    private Label totalLabel;

    @FXML
    private Label availableLabel;

    // Lista vazia: nenhum profissional fictício.
    private final List<Professional> professionals = new ArrayList<>();

    @FXML
    private void initialize() {

        availabilityFilter.getItems().setAll(
            "Todas",
            "Disponível",
            "Ocupado",
            "Indisponível"
        );

        availabilityFilter.setValue("Todas");

        areaFilter.getItems().setAll(
            "Todas",
            "Tecnologia",
            "Suporte",
            "Infraestrutura",
            "Design"
        );

        areaFilter.setValue("Todas");

        searchField.textProperty().addListener(
            (obs, oldValue, newValue) -> renderProfessionals()
        );

        availabilityFilter.valueProperty().addListener(
            (obs, oldValue, newValue) -> renderProfessionals()
        );

        areaFilter.valueProperty().addListener(
            (obs, oldValue, newValue) -> renderProfessionals()
        );

        updateStatistics();
        renderProfessionals();
    }

    private void updateStatistics() {

        totalLabel.setText(
            String.valueOf(professionals.size())
        );

        long available = professionals.stream()
            .filter(p -> p.availability().equals("Disponível"))
            .count();

        availableLabel.setText(
            String.valueOf(available)
        );
    }

    private void renderProfessionals() {

        professionalsContainer.getChildren().clear();

        String query = searchField.getText() == null
            ? ""
            : searchField.getText().trim().toLowerCase(Locale.ROOT);

        String availability = availabilityFilter.getValue();
        String area = areaFilter.getValue();

        List<Professional> filtered = professionals.stream()

            .filter(p -> {
                String searchable = (
                    p.name() + " " +
                    p.role() + " " +
                    p.skills() + " " +
                    p.education() + " " +
                    p.summary()
                ).toLowerCase(Locale.ROOT);

                return searchable.contains(query);
            })

            .filter(p ->
                availability == null ||
                availability.equals("Todas") ||
                p.availability().equals(availability)
            )

            .filter(p ->
                area == null ||
                area.equals("Todas") ||
                p.area().equals(area)
            )

            .collect(Collectors.toList());

        for (Professional professional : filtered) {
            professionalsContainer.getChildren().add(
                createProfessionalCard(professional)
            );
        }

        if (filtered.isEmpty()) {

            Label empty = new Label(
                "Nenhum profissional cadastrado."
            );

            empty.getStyleClass().add("professional-empty");

            professionalsContainer.getChildren().add(empty);
        }
    }

    private VBox createProfessionalCard(Professional p) {

        VBox card = new VBox(12);
        card.getStyleClass().add("professional-card");

        card.setPrefWidth(320);
        card.setMinWidth(280);
        card.setMaxWidth(360);

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label avatar = new Label(getInitials(p.name()));
        avatar.getStyleClass().add("professional-avatar");

        VBox identity = new VBox(5);
        identity.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(identity, Priority.ALWAYS);

        Label name = new Label(p.name());
        name.getStyleClass().add("professional-name");
        name.setWrapText(true);

        Label role = new Label(p.role());
        role.getStyleClass().add("professional-role");
        role.setWrapText(true);

        identity.getChildren().addAll(name, role);
        header.getChildren().addAll(avatar, identity);

        Label availability = new Label(
            "● " + p.availability()
        );

        availability.getStyleClass().add("professional-availability");

        availability.getStyleClass().add(
            p.availability().equals("Disponível")
                ? "status-available"
                : p.availability().equals("Ocupado")
                    ? "status-busy"
                    : "status-unavailable"
        );

        Label profileStatus = new Label(
            "Perfil: " + p.status()
        );

        profileStatus.getStyleClass().add("professional-profile-status");

        profileStatus.getStyleClass().add(
            p.status().equals("Ativo")
                ? "profile-active"
                : "profile-inactive"
        );

        Label experience = new Label(
            p.experience() + " anos de experiência"
        );

        experience.getStyleClass().add("professional-experience");

        Label educationTitle = sectionLabel("FORMAÇÃO ACADÊMICA");
        Label education = bodyLabel(p.education());

        Label skillsTitle = sectionLabel("HABILIDADES");
        Label skills = bodyLabel(p.skills());
        skills.setWrapText(true);

        Label summaryTitle = sectionLabel("SOBRE");
        Label summary = bodyLabel(p.summary());
        summary.setWrapText(true);

        HBox statusRow = new HBox(
            10,
            availability,
            profileStatus
        );

        statusRow.setAlignment(Pos.CENTER_LEFT);

        Button hireButton = new Button("Contratar");
        hireButton.getStyleClass().add("professional-hire-button");
        hireButton.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(hireButton, Priority.ALWAYS);
        hireButton.setOnAction(event -> handleHire(p));

        Button profileButton = new Button("Ver perfil");
        profileButton.getStyleClass().add("professional-profile-button");
        profileButton.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(profileButton, Priority.ALWAYS);
        profileButton.setOnAction(event -> showProfile(p));

        HBox actions = new HBox(
            10,
            hireButton,
            profileButton
        );

        actions.setAlignment(Pos.CENTER);

        card.getChildren().addAll(
            header,
            statusRow,
            experience,
            educationTitle,
            education,
            skillsTitle,
            skills,
            summaryTitle,
            summary,
            actions
        );

        return card;
    }

    private Label sectionLabel(String text) {

        Label label = new Label(text);
        label.getStyleClass().add("professional-section-label");

        return label;
    }

    private Label bodyLabel(String text) {

        Label label = new Label(text);
        label.getStyleClass().add("professional-body-text");

        return label;
    }

    private String getInitials(String name) {

        String[] parts = name.trim().split("\\s+");

        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase(Locale.ROOT);
        }

        return (
            parts[0].substring(0, 1) +
            parts[parts.length - 1].substring(0, 1)
        ).toUpperCase(Locale.ROOT);
    }

    private void handleHire(Professional p) {

        Alert alert = new Alert(
            Alert.AlertType.INFORMATION
        );

        alert.setTitle("Contratação");
        alert.setHeaderText("Funcionalidade indisponível");
        alert.setContentText(
            "O cadastro de profissionais ainda não está implementado."
        );

        alert.showAndWait();
    }

    private void showProfile(Professional p) {

        Alert alert = new Alert(
            Alert.AlertType.INFORMATION
        );

        alert.setTitle("Perfil profissional");
        alert.setHeaderText(p.name());

        alert.setContentText(
            "Cargo: " + p.role()
            + "\nExperiência: " + p.experience() + " anos"
            + "\nFormação: " + p.education()
            + "\nHabilidades: " + p.skills()
            + "\nÁrea: " + p.area()
            + "\nDisponibilidade: " + p.availability()
            + "\nStatus do perfil: " + p.status()
            + "\n\n" + p.summary()
        );

        alert.showAndWait();
    }

    private record Professional(
        String name,
        String role,
        int experience,
        String education,
        String skills,
        String summary,
        String area,
        String availability,
        String status
    ) {}
}
