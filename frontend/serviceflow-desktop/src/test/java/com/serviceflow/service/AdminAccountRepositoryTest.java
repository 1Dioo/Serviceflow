package com.serviceflow.service;

import com.serviceflow.model.User;
import com.serviceflow.service.LoginAttemptService.Channel;
import com.serviceflow.service.LoginAttemptService.Result;
import com.serviceflow.service.LoginAttemptService.Status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Conta administrativa criada a partir da configuração.
 * As senhas abaixo são fictícias e existem apenas nos testes.
 */
@DisplayName("UserRepository - conta administrativa configurável")
class AdminAccountRepositoryTest {

    private static final String ADMIN_PASSWORD = "teste-admin-valida-42";
    private static final String USER_PASSWORD = "senhaSegura123";

    /** Credencial antiga, removida do código na Etapa 4.3. */
    private static final String OLD_USERNAME = "admin";
    private static final String OLD_PASSWORD = "121";

    @AfterEach
    void restore() {
        UserRepository.logout();
        AdminTestSupport.restoreFromEnvironment();
    }

    private static String uniqueUsername() {
        return "teste_" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Test
    @DisplayName("configuração válida cria a conta ADMIN com o usuário padrão")
    void validConfigurationCreatesAdmin() {
        AdminTestSupport.configure(null, ADMIN_PASSWORD);

        assertTrue(UserRepository.isAdminConfigured());
        assertEquals(AdminAccountConfig.Status.CONFIGURED, UserRepository.getAdminConfigStatus());

        User admin = UserRepository.authenticateAdmin(
            AdminAccountConfig.DEFAULT_USERNAME, ADMIN_PASSWORD);

        assertNotNull(admin);
        assertEquals(User.Role.ADMIN, admin.getRole());
        assertEquals(AdminAccountConfig.DEFAULT_USERNAME, admin.getUsername());
    }

    @Test
    @DisplayName("ADMIN recusa senha errada e não entra pelo login comum")
    void adminRejectsWrongPasswordAndUserChannel() {
        AdminTestSupport.configure(null, ADMIN_PASSWORD);

        assertNull(UserRepository.authenticateAdmin(
            AdminAccountConfig.DEFAULT_USERNAME, ADMIN_PASSWORD + "x"));
        assertNull(UserRepository.authenticateUser(
            AdminAccountConfig.DEFAULT_USERNAME, ADMIN_PASSWORD));
    }

    @Test
    @DisplayName("usuário administrativo configurado por SERVICEFLOW_ADMIN_USER")
    void customAdminUsername() {
        AdminTestSupport.configure("Gestor", ADMIN_PASSWORD);

        assertNotNull(UserRepository.authenticateAdmin("gestor", ADMIN_PASSWORD));
        assertNotNull(UserRepository.authenticateAdmin(" GESTOR ", ADMIN_PASSWORD));
        assertNull(UserRepository.authenticateAdmin(
            AdminAccountConfig.DEFAULT_USERNAME, ADMIN_PASSWORD));
        assertFalse(UserRepository.usernameExists(AdminAccountConfig.DEFAULT_USERNAME));
    }

    @Test
    @DisplayName("senha ausente: ADMIN não é criado")
    void missingPasswordDoesNotCreateAdmin() {
        AdminTestSupport.configure(null, null);

        assertFalse(UserRepository.isAdminConfigured());
        assertEquals(AdminAccountConfig.Status.MISSING_PASSWORD,
            UserRepository.getAdminConfigStatus());
        assertFalse(UserRepository.usernameExists(AdminAccountConfig.DEFAULT_USERNAME));
    }

    @Test
    @DisplayName("senha abaixo do mínimo: ADMIN não é criado nem autentica")
    void shortPasswordDoesNotCreateAdmin() {
        String shortPassword = "curta-11chr";
        assertEquals(11, shortPassword.length());

        AdminTestSupport.configure("gestor", shortPassword);

        assertFalse(UserRepository.isAdminConfigured());
        assertEquals(AdminAccountConfig.Status.PASSWORD_TOO_SHORT,
            UserRepository.getAdminConfigStatus());
        assertFalse(UserRepository.usernameExists("gestor"));
        assertNull(UserRepository.authenticateAdmin("gestor", shortPassword));
    }

    @Test
    @DisplayName("configuração inválida remove um ADMIN criado anteriormente")
    void invalidReconfigurationRemovesAdmin() {
        AdminTestSupport.configure(null, ADMIN_PASSWORD);
        assertTrue(UserRepository.isAdminConfigured());

        AdminTestSupport.configure(null, null);

        assertFalse(UserRepository.isAdminConfigured());
        assertNull(UserRepository.authenticateAdmin(
            AdminAccountConfig.DEFAULT_USERNAME, ADMIN_PASSWORD));
    }

    @Test
    @DisplayName("credencial administrativa antiga não funciona em nenhum cenário")
    void oldCredentialNoLongerWorks() {
        AdminTestSupport.restoreFromEnvironment();
        assertNull(UserRepository.authenticateAdmin(OLD_USERNAME, OLD_PASSWORD));

        AdminTestSupport.configure(null, null);
        assertNull(UserRepository.authenticateAdmin(OLD_USERNAME, OLD_PASSWORD));

        AdminTestSupport.configure(null, ADMIN_PASSWORD);
        assertNull(UserRepository.authenticateAdmin(OLD_USERNAME, OLD_PASSWORD));

        // Mesmo que alguém tente configurá-la, fica abaixo do mínimo.
        AdminTestSupport.configure(OLD_USERNAME, OLD_PASSWORD);
        assertFalse(UserRepository.isAdminConfigured());
        assertNull(UserRepository.authenticateAdmin(OLD_USERNAME, OLD_PASSWORD));
    }

    @Test
    @DisplayName("usuários USER funcionam com ADMIN configurado ou não")
    void regularUsersUnaffected() {
        AdminTestSupport.configure(null, null);
        String first = uniqueUsername();
        assertTrue(UserRepository.register("Maria", first, USER_PASSWORD));
        assertNotNull(UserRepository.authenticateUser(first, USER_PASSWORD));

        AdminTestSupport.configure(null, ADMIN_PASSWORD);
        String second = uniqueUsername();
        assertTrue(UserRepository.register("João", second, USER_PASSWORD));

        User firstUser = UserRepository.authenticateUser(first, USER_PASSWORD);
        User secondUser = UserRepository.authenticateUser(second, USER_PASSWORD);

        assertNotNull(firstUser);
        assertNotNull(secondUser);
        assertEquals(User.Role.USER, firstUser.getRole());
        assertNull(UserRepository.authenticateAdmin(second, USER_PASSWORD));
    }

    @Test
    @DisplayName("com ADMIN configurado, não é possível cadastrar USER com o mesmo nome")
    void cannotRegisterUserWithAdminUsername() {
        AdminTestSupport.configure("gestor", ADMIN_PASSWORD);

        assertFalse(UserRepository.register("Intruso", "Gestor", USER_PASSWORD));
        assertNotNull(UserRepository.authenticateAdmin("gestor", ADMIN_PASSWORD));
    }

    @Test
    @DisplayName("integração com LoginAttemptService: bloqueio do canal ADMIN")
    void integratesWithLoginAttemptService() {
        AdminTestSupport.configure(null, ADMIN_PASSWORD);

        LoginAttemptServiceTest.MutableClock clock = new LoginAttemptServiceTest.MutableClock();
        LoginAttemptService service = new LoginAttemptService(clock);
        String username = AdminAccountConfig.DEFAULT_USERNAME;

        Result ok = service.attempt(Channel.ADMIN, username,
            () -> UserRepository.authenticateAdmin(username, ADMIN_PASSWORD));
        assertEquals(Status.SUCCESS, ok.status());
        assertEquals(User.Role.ADMIN, ok.user().getRole());

        for (int i = 0; i < Channel.ADMIN.getMaxFailures() - 1; i++) {
            assertEquals(Status.INVALID, service.attempt(Channel.ADMIN, username,
                () -> UserRepository.authenticateAdmin(username, "errada")).status());
        }

        Result locked = service.attempt(Channel.ADMIN, username,
            () -> UserRepository.authenticateAdmin(username, "errada"));
        assertEquals(Status.LOCKED, locked.status());

        // Durante o bloqueio, nem a senha correta é aceita.
        assertEquals(Status.LOCKED, service.attempt(Channel.ADMIN, username,
            () -> UserRepository.authenticateAdmin(username, ADMIN_PASSWORD)).status());

        clock.advance(Channel.ADMIN.getFirstLockout().plus(Duration.ofSeconds(1)));

        assertEquals(Status.SUCCESS, service.attempt(Channel.ADMIN, username,
            () -> UserRepository.authenticateAdmin(username, ADMIN_PASSWORD)).status());
    }

    @Test
    @DisplayName("sem configuração, tentativas administrativas nunca têm sucesso")
    void attemptsFailWhenNotConfigured() {
        AdminTestSupport.configure(null, null);

        LoginAttemptService service =
            new LoginAttemptService(new LoginAttemptServiceTest.MutableClock());

        Result result = service.attempt(Channel.ADMIN, OLD_USERNAME,
            () -> UserRepository.authenticateAdmin(OLD_USERNAME, OLD_PASSWORD));

        assertEquals(Status.INVALID, result.status());
        assertNull(result.user());
    }
}
