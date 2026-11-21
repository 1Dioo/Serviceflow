package com.serviceflow.controller;
import com.serviceflow.model.User;
import com.serviceflow.model.User.Role;
import com.serviceflow.security.AccessPolicy;
import com.serviceflow.service.UserRepository;

import java.io.IOException;
import java.util.function.Consumer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainController {
    @FXML
    private VBox contentArea;

    @FXML
    private Label dashboardMenuItem;

    @FXML
    private Label agendaMenuItem;

    @FXML
    private Label clientsMenuItem;

    @FXML
    private Label servicesMenuItem;

    @FXML
    private Label professionalsMenuItem;

    @FXML
    private Label reportsMenuItem;

    @FXML
    private Label settingsMenuItem;

    private static Consumer<String> navigator;

    /** Classe visual do item do menu correspondente à tela aberta. */
    static final String ACTIVE_NAV_CLASS = "nav-item-active";

    @FXML
    private void initialize() {
        navigator = this::loadView;

        User currentUser = UserRepository.getCurrentUser();
        boolean isAdmin = currentUser != null
                && currentUser.getRole() == Role.ADMIN;

        reportsMenuItem.setVisible(isAdmin);
        reportsMenuItem.setManaged(isAdmin);

        settingsMenuItem.setVisible(isAdmin);
        settingsMenuItem.setManaged(isAdmin);

        loadDashboard();
    }

    @FXML
    private void handleDashboard() {
        loadView("/com/serviceflow/view/DashboardView.fxml");
    }

    @FXML
    private void handleAgenda() {
        loadView("/com/serviceflow/view/AgendaView.fxml");
    }

    @FXML
    private void handleClientes() {
        loadView("/com/serviceflow/view/ClientesView.fxml");
    }

    @FXML
    private void handleServicos() {
        loadView("/com/serviceflow/view/ServicesView.fxml");
    }

    @FXML
    private void handleProfissionais() {
        loadView("/com/serviceflow/view/ProfissionaisView.fxml");
    }

    @FXML
    private void handleRelatorios() {
        loadView("/com/serviceflow/view/RelatoriosView.fxml");
    }

    @FXML
    private void handleSettings() {
        loadView("/com/serviceflow/view/SettingsView.fxml");
    }
    
    @FXML
    private void handleLogout() {
        try {
            // Encerra a sessão atual
            UserRepository.logout();

            // Limpa o navegador estático da tela anterior
            navigator = null;

            FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                    "/com/serviceflow/view/LoginView.fxml"
                )
            );

            Parent root = loader.load();

            Stage stage = (Stage) contentArea.getScene().getWindow();

            stage.setTitle("ServiceFlow");
            stage.setScene(new javafx.scene.Scene(root, 480, 650));

            stage.setMinWidth(480);
            stage.setMinHeight(600);
            stage.setResizable(false);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro ao sair");
            alert.setHeaderText("Não foi possível retornar ao login.");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Define quem exibe as telas navegadas por navigateTo (por exemplo,
     * Serviços ↔ Novo serviço). O painel administrativo se registra aqui
     * para reutilizar as mesmas telas; a janela principal se registra
     * ao ser carregada.
     */
    public static void registerNavigator(Consumer<String> target) {
        navigator = target;
    }

    /** Remove o navegador apenas se ainda for o informado. */
    public static void clearNavigator(Consumer<String> target) {
        if (navigator == target) {
            navigator = null;
        }
    }

    public static void navigateTo(String fxml) {
        if (navigator != null) {
            navigator.accept(fxml);
        }
    }

    public static void navigateToServices() {
        navigateTo("/com/serviceflow/view/ServicesView.fxml");
    }

    /**
     * Destaca no menu a seção da tela aberta (apenas aparência).
     * Novo serviço pertence à seção Serviços.
     */
    private void highlightMenu(String fxml) {
        Label active = switch (fxml) {
            case "/com/serviceflow/view/DashboardView.fxml" -> dashboardMenuItem;
            case "/com/serviceflow/view/AgendaView.fxml" -> agendaMenuItem;
            case "/com/serviceflow/view/ClientesView.fxml" -> clientsMenuItem;
            case "/com/serviceflow/view/ServicesView.fxml",
                 "/com/serviceflow/view/NewServiceView.fxml" -> servicesMenuItem;
            case "/com/serviceflow/view/ProfissionaisView.fxml" -> professionalsMenuItem;
            case "/com/serviceflow/view/RelatoriosView.fxml" -> reportsMenuItem;
            case "/com/serviceflow/view/SettingsView.fxml" -> settingsMenuItem;
            default -> null;
        };

        for (Label item : new Label[] {
                dashboardMenuItem, agendaMenuItem, clientsMenuItem, servicesMenuItem,
                professionalsMenuItem, reportsMenuItem, settingsMenuItem}) {
            if (item != null) {
                item.getStyleClass().remove(ACTIVE_NAV_CLASS);
            }
        }

        if (active != null) {
            active.getStyleClass().add(ACTIVE_NAV_CLASS);
        }
    }

    private void loadDashboard() {
        loadView("/com/serviceflow/view/DashboardView.fxml");
    }

    private boolean isAdmin() {
        return AccessPolicy.isAdmin(UserRepository.getCurrentUser());
    }

    private void loadView(String fxml) {

        if (AccessPolicy.requiresAdmin(fxml) && !isAdmin()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Acesso restrito");
            alert.setHeaderText("Permissão necessária");
            alert.setContentText(
                "Essa funcionalidade está disponível apenas para administradores."
            );
            alert.showAndWait();
            return;
        }

        try {
            var resource = getClass().getResource(fxml);

            if (resource == null) {
                throw new IOException("FXML não encontrado: " + fxml);
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Node view = loader.load();

            contentArea.getChildren().setAll(view);
            highlightMenu(fxml);

        } catch (IOException | RuntimeException e) {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro ao abrir tela");
            alert.setHeaderText("Não foi possível carregar a tela.");
            alert.setContentText(
                e.getMessage() == null
                    ? e.getClass().getSimpleName()
                    : e.getMessage()
            );
            alert.showAndWait();
        }
    }
}
