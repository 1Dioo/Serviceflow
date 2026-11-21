package com.serviceflow.service;

import com.serviceflow.service.AdminAccountConfig.Status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Resolução da configuração administrativa com fontes simuladas
 * (o ambiente real da máquina não é lido nem alterado).
 * As senhas abaixo são valores fictícios usados somente nos testes.
 */
@DisplayName("AdminAccountConfig - leitura da configuração administrativa")
class AdminAccountConfigTest {

    private static final String VALID = "teste-config-valida-01";

    private final Map<String, String> env = new HashMap<>();
    private final Map<String, String> props = new HashMap<>();

    private AdminAccountConfig resolve() {
        return AdminAccountConfig.resolve(env::get, props::get);
    }

    @Test
    @DisplayName("senha pela variável de ambiente é aceita")
    void passwordFromEnvironment() {
        env.put(AdminAccountConfig.PASSWORD_ENV, VALID);

        AdminAccountConfig config = resolve();

        assertEquals(Status.CONFIGURED, config.status());
        assertTrue(config.isValid());
        assertEquals(VALID, config.password());
    }

    @Test
    @DisplayName("senha pela propriedade JVM é aceita")
    void passwordFromJvmProperty() {
        props.put(AdminAccountConfig.PASSWORD_PROPERTY, VALID);

        AdminAccountConfig config = resolve();

        assertTrue(config.isValid());
        assertEquals(VALID, config.password());
    }

    @Test
    @DisplayName("propriedade JVM tem prioridade sobre a variável de ambiente")
    void jvmPropertyHasPriority() {
        env.put(AdminAccountConfig.PASSWORD_ENV, "teste-valor-do-ambiente");
        props.put(AdminAccountConfig.PASSWORD_PROPERTY, VALID);

        assertEquals(VALID, resolve().password());
    }

    @Test
    @DisplayName("propriedade JVM em branco recorre à variável de ambiente")
    void blankPropertyFallsBackToEnvironment() {
        props.put(AdminAccountConfig.PASSWORD_PROPERTY, "   ");
        env.put(AdminAccountConfig.PASSWORD_ENV, VALID);

        assertEquals(VALID, resolve().password());
    }

    @Test
    @DisplayName("senha ausente torna a configuração inválida")
    void missingPassword() {
        AdminAccountConfig config = resolve();

        assertEquals(Status.MISSING_PASSWORD, config.status());
        assertFalse(config.isValid());
        assertNull(config.password());
    }

    @Test
    @DisplayName("senha vazia ou só com espaços é tratada como ausente")
    void blankPasswordIsMissing() {
        env.put(AdminAccountConfig.PASSWORD_ENV, "");
        props.put(AdminAccountConfig.PASSWORD_PROPERTY, "            ");

        assertEquals(Status.MISSING_PASSWORD, resolve().status());
    }

    @Test
    @DisplayName("senha com 11 caracteres é recusada e não é mantida")
    void passwordBelowMinimum() {
        env.put(AdminAccountConfig.PASSWORD_ENV, "a".repeat(11));

        AdminAccountConfig config = resolve();

        assertEquals(Status.PASSWORD_TOO_SHORT, config.status());
        assertFalse(config.isValid());
        assertNull(config.password());
    }

    @Test
    @DisplayName("senha com exatamente 12 caracteres é aceita")
    void passwordAtMinimum() {
        env.put(AdminAccountConfig.PASSWORD_ENV, "b".repeat(12));

        assertEquals(12, AdminAccountConfig.MIN_PASSWORD_LENGTH);
        assertTrue(resolve().isValid());
    }

    @Test
    @DisplayName("mínimo conta caracteres, não unidades UTF-16 (emoji)")
    void minimumCountsCodePoints() {
        // 6 emojis = 12 unidades UTF-16, mas apenas 6 caracteres.
        env.put(AdminAccountConfig.PASSWORD_ENV, "🔒".repeat(6));

        assertEquals(Status.PASSWORD_TOO_SHORT, resolve().status());
    }

    @Test
    @DisplayName("usuário padrão quando SERVICEFLOW_ADMIN_USER não é informado")
    void defaultUsername() {
        env.put(AdminAccountConfig.PASSWORD_ENV, VALID);

        assertEquals(AdminAccountConfig.DEFAULT_USERNAME, resolve().username());

        env.put(AdminAccountConfig.USERNAME_ENV, "   ");
        assertEquals(AdminAccountConfig.DEFAULT_USERNAME, resolve().username());
    }

    @Test
    @DisplayName("usuário configurado por SERVICEFLOW_ADMIN_USER, sem espaços nas pontas")
    void customUsername() {
        env.put(AdminAccountConfig.PASSWORD_ENV, VALID);
        env.put(AdminAccountConfig.USERNAME_ENV, "  gestor  ");

        assertEquals("gestor", resolve().username());
    }

    @Test
    @DisplayName("toString nunca expõe a senha")
    void toStringHidesPassword() {
        env.put(AdminAccountConfig.PASSWORD_ENV, VALID);

        String text = resolve().toString();

        assertFalse(text.contains(VALID));
        assertTrue(text.contains("CONFIGURED"));
    }
}
