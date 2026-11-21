package com.serviceflow.security;

import com.serviceflow.model.User;
import com.serviceflow.service.AdminTestSupport;
import com.serviceflow.service.UserRepository;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Permissões do painel administrativo. As contas são obtidas pela
 * autenticação real (PBKDF2), sem atalhos. Senhas fictícias.
 */
@DisplayName("AccessPolicy - acesso ao painel administrativo")
class AccessPolicyTest {

    private static final String ADMIN_PASSWORD = "teste-acesso-admin-88";
    private static final String USER_PASSWORD = "senhaSegura123";

    private static final List<String> PANEL_VIEWS = List.of(
        AccessPolicy.AGENDA_VIEW,
        AccessPolicy.SERVICES_VIEW,
        AccessPolicy.NEW_SERVICE_VIEW,
        AccessPolicy.EMPLOYEES_VIEW,
        AccessPolicy.REPORTS_VIEW,
        AccessPolicy.SETTINGS_VIEW
    );

    private static User admin;
    private static User regularUser;

    @BeforeAll
    static void authenticate() {
        AdminTestSupport.configure(null, ADMIN_PASSWORD);
        admin = UserRepository.authenticateAdmin("admin", ADMIN_PASSWORD);

        String username = "teste_" + UUID.randomUUID().toString().substring(0, 8);
        UserRepository.register("Maria", username, USER_PASSWORD);
        regularUser = UserRepository.authenticateUser(username, USER_PASSWORD);

        assertNotNull(admin);
        assertNotNull(regularUser);
    }

    @AfterAll
    static void restore() {
        AdminTestSupport.restoreFromEnvironment();
    }

    @Test
    @DisplayName("ADMIN acessa Agenda e Serviços no painel")
    void adminAccessesAgendaAndServices() {
        assertTrue(AccessPolicy.isAdmin(admin));
        assertTrue(AccessPolicy.canOpenInAdminPanel(admin, AccessPolicy.AGENDA_VIEW));
        assertTrue(AccessPolicy.canOpenInAdminPanel(admin, AccessPolicy.SERVICES_VIEW));
        assertTrue(AccessPolicy.canOpenInAdminPanel(admin, AccessPolicy.NEW_SERVICE_VIEW));
    }

    @Test
    @DisplayName("telas administrativas já existentes continuam acessíveis ao ADMIN")
    void existingAdminViewsStillAllowed() {
        assertTrue(AccessPolicy.canOpenInAdminPanel(admin, AccessPolicy.EMPLOYEES_VIEW));
        assertTrue(AccessPolicy.canOpenInAdminPanel(admin, AccessPolicy.REPORTS_VIEW));
        assertTrue(AccessPolicy.canOpenInAdminPanel(admin, AccessPolicy.SETTINGS_VIEW));
    }

    @Test
    @DisplayName("usuário comum não abre nenhuma tela do painel")
    void regularUserBlocked() {
        assertFalse(AccessPolicy.isAdmin(regularUser));

        for (String view : PANEL_VIEWS) {
            assertFalse(AccessPolicy.canOpenInAdminPanel(regularUser, view), view);
        }
    }

    @Test
    @DisplayName("sem sessão, nenhuma tela do painel é aberta")
    void noSessionBlocked() {
        assertFalse(AccessPolicy.isAdmin(null));

        for (String view : PANEL_VIEWS) {
            assertFalse(AccessPolicy.canOpenInAdminPanel(null, view), view);
        }
    }

    @Test
    @DisplayName("painel só abre telas previstas, mesmo para o ADMIN")
    void onlyAllowedViewsInPanel() {
        for (String view : List.of(
                "/com/serviceflow/view/MainView.fxml",
                "/com/serviceflow/view/DashboardView.fxml",
                "/com/serviceflow/view/LoginView.fxml",
                "/com/serviceflow/view/ClientesView.fxml",
                "/com/serviceflow/view/../view/AgendaView.fxml",
                "")) {
            assertFalse(AccessPolicy.canOpenInAdminPanel(admin, view), view);
        }

        assertFalse(AccessPolicy.canOpenInAdminPanel(admin, null));
        assertFalse(AccessPolicy.requiresAdmin(null));
    }

    @Test
    @DisplayName("janela principal: regra anterior mantida (só Relatórios e Configurações exigem ADMIN)")
    void mainWindowRuleUnchanged() {
        assertTrue(AccessPolicy.requiresAdmin(AccessPolicy.REPORTS_VIEW));
        assertTrue(AccessPolicy.requiresAdmin(AccessPolicy.SETTINGS_VIEW));

        assertFalse(AccessPolicy.requiresAdmin(AccessPolicy.AGENDA_VIEW));
        assertFalse(AccessPolicy.requiresAdmin(AccessPolicy.SERVICES_VIEW));
        assertFalse(AccessPolicy.requiresAdmin(AccessPolicy.NEW_SERVICE_VIEW));
    }

    @Test
    @DisplayName("perfil ADMIN não é obtido pelo login comum nem pela credencial antiga")
    void authenticationNotWeakened() {
        assertFalse(AccessPolicy.isAdmin(UserRepository.authenticateUser("admin", ADMIN_PASSWORD)));
        assertFalse(AccessPolicy.isAdmin(UserRepository.authenticateAdmin("admin", "121")));
    }
}
