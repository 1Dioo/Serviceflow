package com.serviceflow.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("User - verificação de senha")
class UserTest {

    private static final String PASSWORD = "senhaSegura123";

    private User newUser(String password) {
        return new User("Maria Souza", "maria", password, User.Role.USER);
    }

    @Test
    @DisplayName("aceita a senha correta")
    void acceptsCorrectPassword() {
        assertTrue(newUser(PASSWORD).verifyPassword(PASSWORD));
    }

    @Test
    @DisplayName("recusa senha errada")
    void rejectsWrongPassword() {
        assertFalse(newUser(PASSWORD).verifyPassword("outraSenha123"));
    }

    @Test
    @DisplayName("diferencia maiúsculas e minúsculas")
    void isCaseSensitive() {
        assertFalse(newUser(PASSWORD).verifyPassword(PASSWORD.toUpperCase()));
    }

    @Test
    @DisplayName("recusa senha vazia")
    void rejectsEmptyPassword() {
        assertFalse(newUser(PASSWORD).verifyPassword(""));
    }

    @Test
    @DisplayName("recusa senha com espaço extra")
    void rejectsPasswordWithExtraSpace() {
        assertFalse(newUser(PASSWORD).verifyPassword(PASSWORD + " "));
    }

    @Test
    @DisplayName("usuários diferentes com a mesma senha verificam de forma independente")
    void samePasswordForDifferentUsers() {
        User first = newUser(PASSWORD);
        User second = newUser(PASSWORD);

        assertTrue(first.verifyPassword(PASSWORD));
        assertTrue(second.verifyPassword(PASSWORD));
        assertFalse(second.verifyPassword("errada123456"));
    }

    @Test
    @DisplayName("aceita senhas com acentos e símbolos (UTF-8)")
    void supportsUnicodePasswords() {
        String unicode = "çãoÉ@#senha€2026";

        assertTrue(newUser(unicode).verifyPassword(unicode));
        assertFalse(newUser(unicode).verifyPassword("caoE@#senha€2026"));
    }

    @Test
    @DisplayName("armazena a senha como hash PBKDF2, nunca em texto")
    void storesPbkdf2Hash() {
        User user = newUser(PASSWORD);

        assertTrue(user.getPasswordHash().startsWith("pbkdf2_sha256$600000$"));
        assertFalse(user.getPasswordHash().contains(PASSWORD));
    }

    @Test
    @DisplayName("mesma senha em usuários diferentes gera hashes diferentes")
    void differentHashesForSamePassword() {
        assertFalse(newUser(PASSWORD).getPasswordHash()
            .equals(newUser(PASSWORD).getPasswordHash()));
    }

    @Test
    @DisplayName("senha nula na verificação é recusada sem exceção")
    void rejectsNullPassword() {
        assertFalse(newUser(PASSWORD).verifyPassword(null));
    }

    @Test
    @DisplayName("mantém nome, usuário e perfil informados")
    void keepsUserData() {
        User admin = new User("Administrador", "chefe", PASSWORD, User.Role.ADMIN);

        assertEquals("Administrador", admin.getName());
        assertEquals("chefe", admin.getUsername());
        assertEquals(User.Role.ADMIN, admin.getRole());
    }
}
