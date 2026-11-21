package com.serviceflow.security;

import com.serviceflow.model.User;

import java.util.Set;

/**
 * Regras de acesso às telas, centralizadas para a janela principal
 * (MainController) e para o painel administrativo (AdminMainController).
 *
 *  - Relatórios e Configurações exigem perfil ADMIN na janela principal
 *    (regra que já existia no MainController).
 *  - O painel administrativo exige perfil ADMIN e só abre as telas da
 *    lista ADMIN_PANEL_VIEWS; qualquer outro caminho é recusado.
 */
public final class AccessPolicy {

    private static final String VIEW_DIR = "/com/serviceflow/view/";

    public static final String AGENDA_VIEW = VIEW_DIR + "AgendaView.fxml";
    public static final String SERVICES_VIEW = VIEW_DIR + "ServicesView.fxml";
    public static final String NEW_SERVICE_VIEW = VIEW_DIR + "NewServiceView.fxml";
    public static final String EMPLOYEES_VIEW = VIEW_DIR + "EmployeesView.fxml";
    public static final String REPORTS_VIEW = VIEW_DIR + "RelatoriosView.fxml";
    public static final String SETTINGS_VIEW = VIEW_DIR + "SettingsView.fxml";

    /** Telas da janela principal restritas a administradores. */
    private static final Set<String> ADMIN_ONLY_VIEWS = Set.of(
        REPORTS_VIEW,
        SETTINGS_VIEW
    );

    /** Telas que podem ser exibidas dentro do painel administrativo. */
    private static final Set<String> ADMIN_PANEL_VIEWS = Set.of(
        AGENDA_VIEW,
        SERVICES_VIEW,
        NEW_SERVICE_VIEW,
        EMPLOYEES_VIEW,
        REPORTS_VIEW,
        SETTINGS_VIEW
    );

    private AccessPolicy() {
    }

    public static boolean isAdmin(User user) {
        return user != null && user.getRole() == User.Role.ADMIN;
    }

    /** true se a tela da janela principal exige perfil ADMIN. */
    public static boolean requiresAdmin(String view) {
        return view != null && ADMIN_ONLY_VIEWS.contains(view);
    }

    /** Painel administrativo: somente ADMIN e somente telas permitidas. */
    public static boolean canOpenInAdminPanel(User user, String view) {
        return isAdmin(user) && view != null && ADMIN_PANEL_VIEWS.contains(view);
    }
}
