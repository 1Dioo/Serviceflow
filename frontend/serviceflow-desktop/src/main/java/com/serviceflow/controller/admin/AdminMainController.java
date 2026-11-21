package com.serviceflow.controller.admin;

import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

import com.serviceflow.controller.MainController;
import com.serviceflow.security.AccessPolicy;
import com.serviceflow.service.UserRepository;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Scene;

public class AdminMainController {

    private static final String STYLESHEET = "/com/serviceflow/css/style.css";

    @FXML
    private StackPane contentArea;

    // Itens do menu lateral (somente para destacar a seção aberta).
    @FXML private Button dashboardButton;
    @FXML private Button agendaButton;
    @FXML private Button clientsButton;
    @FXML private Button servicesButton;
    @FXML private Button employeesButton;
    @FXML private Button reportsButton;
    @FXML private Button settingsButton;

    private static final String ACTIVE_NAV_CLASS = "nav-item-active";

    // Navegação interna das telas reutilizadas (ex.: Serviços ↔ Novo serviço),
    // sempre exibida no painel e sujeita às mesmas verificações de acesso.
    private final Consumer<String> panelNavigator = path -> loadView(path, true);

    @FXML
    public void initialize() {
        if (!isAdmin()) {
            showRestricted();
            return;
        }

        MainController.registerNavigator(panelNavigator);

        showDashboard();
    }

    @FXML
    private void showDashboard() {
        if (!ensureAdmin()) {
            return;
        }

        Label title = new Label(
                "Bem-vindo ao painel administrativo do ServiceFlow"
        );
        title.getStyleClass().add("page-title");

        // Mesmo espaçamento e alinhamento das demais páginas.
        VBox home = new VBox(title);
        home.getStyleClass().add("admin-home");

        contentArea.getChildren().setAll(home);
        highlightMenu(dashboardButton);
    }

    @FXML
    private void showEmployees() {
        loadView(AccessPolicy.EMPLOYEES_VIEW, false);
    }

    /**
     * Reutiliza a tela da Agenda principal (mesmo repositório, regras de
     * expediente e conflitos). Recebe o style.css como Relatórios e
     * Configurações.
     */
    @FXML
    private void showAgenda() {
        loadView(AccessPolicy.AGENDA_VIEW, true);
    }

    @FXML
    private void showClients() {
        showUnavailable("Clientes");
    }

    /** Reutiliza a tela de Serviços (catálogo e atendimentos) da aplicação. */
    @FXML
    private void showServices() {
        loadView(AccessPolicy.SERVICES_VIEW, true);
    }

    @FXML
    private void showReports() {
        // Telas do tema escuro: recebem o style.css apenas no próprio nó,
        // sem alterar o visual das telas administrativas já existentes.
        loadView(AccessPolicy.REPORTS_VIEW, true);
    }

    @FXML
    private void showSettings() {
        loadView(AccessPolicy.SETTINGS_VIEW, true);
    }

    private void showUnavailable(String section) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("ServiceFlow");
        alert.setHeaderText(section);
        alert.setContentText(
                "Essa seção será integrada ao painel administrativo posteriormente."
        );
        alert.showAndWait();
    }

    private boolean isAdmin() {
        return AccessPolicy.isAdmin(UserRepository.getCurrentUser());
    }

    /**
     * Garante que apenas administradores autenticados usem o painel.
     */
    private boolean ensureAdmin() {
        if (isAdmin()) {
            return true;
        }

        showRestricted();

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Acesso restrito");
        alert.setHeaderText("Permissão necessária");
        alert.setContentText(
                "Essa funcionalidade está disponível apenas para administradores."
        );
        alert.showAndWait();

        return false;
    }

    private void showRestricted() {
        Label message = new Label(
                "Acesso restrito a administradores autenticados."
        );

        message.getStyleClass().add("restricted-message");

        contentArea.getChildren().setAll(message);
        highlightMenu(null);
    }

    /** Destaca no menu a seção aberta (apenas aparência). */
    private void highlightMenu(Button active) {
        for (Button item : new Button[] {
                dashboardButton, agendaButton, clientsButton, servicesButton,
                employeesButton, reportsButton, settingsButton}) {
            if (item != null) {
                item.getStyleClass().remove(ACTIVE_NAV_CLASS);
            }
        }

        if (active != null) {
            active.getStyleClass().add(ACTIVE_NAV_CLASS);
        }
    }

    private Button menuItemFor(String path) {
        if (AccessPolicy.AGENDA_VIEW.equals(path)) {
            return agendaButton;
        }
        if (AccessPolicy.SERVICES_VIEW.equals(path) || AccessPolicy.NEW_SERVICE_VIEW.equals(path)) {
            return servicesButton;
        }
        if (AccessPolicy.EMPLOYEES_VIEW.equals(path)) {
            return employeesButton;
        }
        if (AccessPolicy.REPORTS_VIEW.equals(path)) {
            return reportsButton;
        }
        if (AccessPolicy.SETTINGS_VIEW.equals(path)) {
            return settingsButton;
        }
        return null;
    }

    private void loadView(String path, boolean applyAppStylesheet) {
        if (!ensureAdmin()) {
            return;
        }

        // Somente as telas previstas para o painel podem ser abertas nele.
        if (!AccessPolicy.canOpenInAdminPanel(UserRepository.getCurrentUser(), path)) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Acesso restrito");
            alert.setHeaderText("Tela indisponível");
            alert.setContentText(
                    "Essa tela não está disponível no painel administrativo."
            );
            alert.showAndWait();
            return;
        }

        try {
            URL resource = getClass().getResource(path);

            if (resource == null) {
                throw new IOException("FXML não encontrado: " + path);
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent view = loader.load();

            if (applyAppStylesheet) {
                URL stylesheet = getClass().getResource(STYLESHEET);

                if (stylesheet != null) {
                    view.getStylesheets().add(stylesheet.toExternalForm());
                }
            }

            contentArea.getChildren().setAll(view);
            highlightMenu(menuItemFor(path));

        } catch (IOException | RuntimeException e) {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Não foi possível abrir a tela.");
            alert.setContentText(
                    e.getMessage() == null
                            ? e.getClass().getSimpleName()
                            : e.getMessage()
            );
            alert.showAndWait();
        }
    }

    @FXML
    private void handleLogout() {
        UserRepository.logout();
        MainController.clearNavigator(panelNavigator);

        try {
            URL resource = getClass().getResource(
                    "/com/serviceflow/view/LoginView.fxml"
            );

            if (resource == null) {
                throw new IOException("FXML não encontrado: LoginView.fxml");
            }

            Parent root = FXMLLoader.load(resource);

            Stage stage = (Stage) contentArea.getScene().getWindow();

            // Volta aos limites da tela de login (o painel define 1000x650).
            stage.setMaximized(false);
            stage.setMinWidth(360);
            stage.setMinHeight(450);

            stage.setScene(new Scene(root, 480, 600));
            stage.setTitle("ServiceFlow");
            stage.setResizable(true);
            stage.sizeToScene();
            stage.centerOnScreen();
            stage.show();

        } catch (IOException | RuntimeException e) {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro ao sair");
            alert.setHeaderText("Não foi possível retornar ao login.");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}
