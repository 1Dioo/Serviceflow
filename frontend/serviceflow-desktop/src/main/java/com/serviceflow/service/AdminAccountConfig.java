package com.serviceflow.service;

import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * Configuração da conta administrativa, lida do ambiente de execução.
 *
 * Senha (obrigatória, mínimo de {@value #MIN_PASSWORD_LENGTH} caracteres):
 *  1. propriedade JVM {@value #PASSWORD_PROPERTY} (prioridade); ou
 *  2. variável de ambiente {@value #PASSWORD_ENV}.
 *
 * Usuário (opcional): variável de ambiente {@value #USERNAME_ENV};
 * se ausente ou em branco, usa {@value #DEFAULT_USERNAME}.
 *
 * Se a senha estiver ausente ou abaixo do mínimo, a configuração é
 * inválida e a conta ADMIN não deve ser criada.
 *
 * A senha nunca é incluída em toString(), logs ou mensagens.
 */
public final class AdminAccountConfig {

    public static final String PASSWORD_ENV = "SERVICEFLOW_ADMIN_PASSWORD";
    public static final String PASSWORD_PROPERTY = "serviceflow.admin.password";
    public static final String USERNAME_ENV = "SERVICEFLOW_ADMIN_USER";

    public static final String DEFAULT_USERNAME = "admin";
    public static final int MIN_PASSWORD_LENGTH = 12;

    public enum Status {
        /** Senha presente e válida: a conta ADMIN pode ser criada. */
        CONFIGURED,
        /** Nenhuma senha informada (ou somente espaços). */
        MISSING_PASSWORD,
        /** Senha informada com menos caracteres que o mínimo. */
        PASSWORD_TOO_SHORT
    }

    private final Status status;
    private final String username;
    private final String password;

    private AdminAccountConfig(Status status, String username, String password) {
        this.status = status;
        this.username = username;
        this.password = password;
    }

    /** Lê a configuração real: propriedades da JVM e variáveis de ambiente. */
    public static AdminAccountConfig fromEnvironment() {
        return resolve(System::getenv, System::getProperty);
    }

    /**
     * Resolve a configuração a partir das fontes informadas
     * (permite testar sem alterar o ambiente real).
     *
     * @param env        consulta de variáveis de ambiente
     * @param properties consulta de propriedades da JVM
     */
    public static AdminAccountConfig resolve(UnaryOperator<String> env,
                                             UnaryOperator<String> properties) {
        Objects.requireNonNull(env);
        Objects.requireNonNull(properties);

        String username = env.apply(USERNAME_ENV);
        username = username == null || username.isBlank()
            ? DEFAULT_USERNAME
            : username.trim();

        String password = properties.apply(PASSWORD_PROPERTY);

        if (password == null || password.isBlank()) {
            password = env.apply(PASSWORD_ENV);
        }

        if (password == null || password.isBlank()) {
            return new AdminAccountConfig(Status.MISSING_PASSWORD, username, null);
        }

        if (password.codePointCount(0, password.length()) < MIN_PASSWORD_LENGTH) {
            return new AdminAccountConfig(Status.PASSWORD_TOO_SHORT, username, null);
        }

        return new AdminAccountConfig(Status.CONFIGURED, username, password);
    }

    public Status status() {
        return status;
    }

    public boolean isValid() {
        return status == Status.CONFIGURED;
    }

    public String username() {
        return username;
    }

    /**
     * Senha em texto, somente quando válida; usada apenas para gerar o
     * hash PBKDF2 ao criar a conta. Restrita ao pacote.
     */
    String password() {
        return password;
    }

    @Override
    public String toString() {
        // Nunca inclui a senha.
        return "AdminAccountConfig[status=" + status + ", username=" + username + "]";
    }
}
