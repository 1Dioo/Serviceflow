package com.serviceflow.service;

import com.serviceflow.model.User;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * O UserRepository é estático (em memória). Cada teste usa um nome de
 * usuário único para não depender da ordem de execução.
 */
@DisplayName("UserRepository - cadastro, autenticação e sessão")
class UserRepositoryTest {

    private static final String PASSWORD = "senhaSegura123";

    private static String uniqueUsername() {
        return "teste_" + UUID.randomUUID().toString().substring(0, 8);
    }

    @AfterEach
    void clearSession() {
        UserRepository.logout();
    }

    @Test
    @DisplayName("cadastra um novo usuário")
    void registersNewUser() {
        String username = uniqueUsername();

        assertTrue(UserRepository.register("Maria", username, PASSWORD));
        assertTrue(UserRepository.usernameExists(username));
    }

    @Test
    @DisplayName("recusa nome de usuário repetido, sem diferenciar maiúsculas e espaços")
    void rejectsDuplicateUsername() {
        String username = uniqueUsername();
        UserRepository.register("Maria", username, PASSWORD);

        assertFalse(UserRepository.register("Outra", username.toUpperCase(), PASSWORD));
        assertFalse(UserRepository.register("Outra", "  " + username + "  ", PASSWORD));
        assertTrue(UserRepository.usernameExists(" " + username.toUpperCase() + " "));
    }

    @Test
    @DisplayName("novo cadastro sempre recebe o perfil USER")
    void registeredUserHasUserRole() {
        String username = uniqueUsername();
        UserRepository.register("Maria", username, PASSWORD);

        User user = UserRepository.authenticateUser(username, PASSWORD);

        assertNotNull(user);
        assertEquals(User.Role.USER, user.getRole());
        assertEquals("Maria", user.getName());
    }

    @Test
    @DisplayName("autenticação de usuário comum com a senha correta")
    void authenticatesUserWithCorrectPassword() {
        String username = uniqueUsername();
        UserRepository.register("Maria", username, PASSWORD);

        assertNotNull(UserRepository.authenticateUser(username, PASSWORD));
        assertNotNull(UserRepository.authenticateUser(username.toUpperCase(), PASSWORD));
    }

    @Test
    @DisplayName("recusa senha errada, usuário inexistente e usuário em branco")
    void rejectsInvalidCredentials() {
        String username = uniqueUsername();
        UserRepository.register("Maria", username, PASSWORD);

        assertNull(UserRepository.authenticateUser(username, "senhaErrada123"));
        assertNull(UserRepository.authenticateUser(uniqueUsername(), PASSWORD));
        assertNull(UserRepository.authenticateUser("   ", PASSWORD));
        assertNull(UserRepository.authenticateUser(null, PASSWORD));
    }

    @Test
    @DisplayName("conta USER não entra pelo login administrativo")
    void userCannotAuthenticateAsAdmin() {
        String username = uniqueUsername();
        UserRepository.register("Maria", username, PASSWORD);

        assertNull(UserRepository.authenticateAdmin(username, PASSWORD));
    }

    private static long bestTimeMillis(Runnable action) {
        long best = Long.MAX_VALUE;

        for (int i = 0; i < 3; i++) {
            long start = System.nanoTime();
            action.run();
            best = Math.min(best, System.nanoTime() - start);
        }

        return best / 1_000_000;
    }

    @Test
    @DisplayName("usuário inexistente leva tempo semelhante a senha errada (não revela quem existe)")
    void unknownUserTakesSimilarTime() {
        String username = uniqueUsername();
        UserRepository.register("Maria", username, PASSWORD);
        String unknown = uniqueUsername();

        // Aquecimento (inclui a criação do hash fictício no primeiro uso).
        UserRepository.authenticateUser(unknown, PASSWORD);
        UserRepository.authenticateUser(username, "senhaErrada123");

        long existing = bestTimeMillis(
            () -> UserRepository.authenticateUser(username, "senhaErrada123"));
        long missing = bestTimeMillis(
            () -> UserRepository.authenticateUser(unknown, "senhaErrada123"));
        long wrongRole = bestTimeMillis(
            () -> UserRepository.authenticateAdmin(username, PASSWORD));

        assertTrue(missing >= existing / 2,
            "inexistente: " + missing + " ms; existente: " + existing + " ms");
        assertTrue(wrongRole >= existing / 2,
            "perfil errado: " + wrongRole + " ms; existente: " + existing + " ms");
    }

    @Test
    @DisplayName("sessão: define o usuário atual e encerra no logout")
    void keepsAndClearsCurrentUser() {
        String username = uniqueUsername();
        UserRepository.register("Maria", username, PASSWORD);
        User user = UserRepository.authenticateUser(username, PASSWORD);

        UserRepository.setCurrentUser(user);
        assertSame(user, UserRepository.getCurrentUser());

        UserRepository.logout();
        assertNull(UserRepository.getCurrentUser());
    }
}
