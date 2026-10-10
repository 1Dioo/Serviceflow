package com.serviceflow.controller;

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

    private final List<Professional> professionals = List.of(
        new Professional(
            "Diogo Rodrigues", "Desenvolvedor Front-end", 3,
            "Análise e Desenvolvimento de Sistemas",
            "React, TypeScript, CSS, JavaScript",
            "Desenvolvimento de interfaces modernas e responsivas.",
            "Tecnologia", "Disponível", "Ativo"
        ),
        new Professional(
            "Mariana Costa", "Desenvolvedora Back-end", 4,
            "Sistemas de Informação",
            "Java, Spring Boot, SQL, APIs REST",
            "Desenvolvimento de serviços e integrações.",
            "Tecnologia", "Disponível", "Ativo"
        ),
        new Professional(
            "Lucas Almeida", "Técnico de Hardware", 5,
            "Técnico em Manutenção de Computadores",
            "Montagem, diagnóstico, manutenção",
            "Manutenção preventiva e corretiva de equipamentos.",
            "Suporte", "Ocupado", "Ativo"
        ),
        new Professional(
            "Beatriz Santos", "Analista de Suporte", 3,
            "Redes de Computadores",
            "Windows, atendimento, redes",
            "Suporte técnico e resolução de problemas.",
            "Suporte", "Disponível", "Ativo"
        ),
        new Professional(
            "Rafael Oliveira", "Administrador de Redes", 6,
            "Segurança da Informação",
            "TCP/IP, roteadores, Linux",
            "Configuração e monitoramento de redes.",
            "Infraestrutura", "Indisponível", "Ativo"
        ),
        new Professional(
            "Camila Ferreira", "Desenvolvedora Mobile", 2,
            "Análise e Desenvolvimento de Sistemas",
            "Flutter, Dart, Firebase",
            "Criação de aplicativos para dispositivos móveis.",
            "Tecnologia", "Disponível", "Ativo"
        ),
        new Professional(
            "Pedro Henrique", "Analista de Segurança", 4,
            "Segurança da Informação",
            "Linux, monitoramento, boas práticas",
            "Apoio à segurança e prevenção de riscos digitais.",
            "Infraestrutura", "Indisponível", "Inativo"
        ),
        new Professional(
            "Juliana Martins", "UI/UX Designer", 3,
            "Design Digital",
            "Figma, prototipação, acessibilidade",
            "Criação de interfaces e experiências digitais.",
            "Design", "Ocupado", "Ativo"
        )
    );

    @FXML
    private void initialize() {
        availabilityFilter.getItems().setAll(
            "Todas", "Disponível", "Ocupado", "Indisponível"
        );
        availabilityFilter.setValue("Todas");

        areaFilter.getItems().setAll(
            "Todas", "Tecnologia", "Suporte",
            "Infraestrutura", "Design"
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

        totalLabel.setText(String.valueOf(professionals.size()));

        long available = professionals.stream()
            .filter(p -> p.availability().equals("Disponível"))
            .count();

        availableLabel.setText(String.valueOf(available));

        renderProfessionals();
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
                "Nenhum profissional encontrado."
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

        Label profileStatus = new Label("Perfil: " + p.status());
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

        HBox statusRow = new HBox(10, availability, profileStatus);
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

        HBox actions = new HBox(10, hireButton, profileButton);
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
        if (!p.status().equals("Ativo")) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Profissional indisponível");
            alert.setHeaderText("Este perfil está inativo");
            alert.setContentText(
                "No momento, não é possível iniciar uma contratação "
                + "por meio deste perfil."
            );
            alert.showAndWait();
            return;
        }

        if (!p.availability().equals("Disponível")) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Disponibilidade");
            alert.setHeaderText(p.name());
            alert.setContentText(
                "Este profissional está marcado como "
                + p.availability().toLowerCase(Locale.ROOT) + "."
            );
            alert.showAndWait();
            return;
        }

        Alert alert = new Alert(
            Alert.AlertType.CONFIRMATION,
            "Deseja demonstrar interesse em contratar "
                + p.name() + " para a função de " + p.role() + "?",
            ButtonType.CANCEL,
            ButtonType.OK
        );

        alert.setTitle("Confirmar contratação");
        alert.setHeaderText("Confirmar interesse");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Alert result = new Alert(Alert.AlertType.INFORMATION);
                result.setTitle("Confirmação");
                result.setHeaderText("Interesse confirmado");
                result.setContentText(
                    "Você confirmou interesse em " + p.name() + ".\n\n"
                    + "Esta é uma demonstração local. "
                    + "Nenhuma solicitação foi enviada ou salva."
                );
                result.showAndWait();
            }
        });
    }

    private void showProfile(Professional p) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
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
