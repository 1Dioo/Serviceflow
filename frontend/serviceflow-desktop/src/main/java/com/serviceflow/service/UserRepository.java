package com.serviceflow.service;

import com.serviceflow.model.User;
import com.serviceflow.model.User.Role;
import com.serviceflow.security.PasswordHasher;

import java.util.HashMap;
import java.util.Map;

public final class UserRepository {
    private static final Map<String, User> USERS = new HashMap<>();

    /*
     * Hash fictício usado quando o usuário não existe (ou não tem o
     * perfil exigido). A verificação leva o mesmo tempo de uma senha
     * real, evitando revelar pelo tempo de resposta quais usuários existem.
     * Criado apenas no primeiro uso.
     */
    private static final class DummyHashHolder {
        private static final String HASH =
            PasswordHasher.hash("serviceflow-dummy-password");
    }

    private static final System.Logger LOGGER =
        System.getLogger(UserRepository.class.getName());

    /*
     * CONTA ADMINISTRATIVA
     * Não há senha no código: ela vem da propriedade JVM
     * serviceflow.admin.password ou da variável SERVICEFLOW_ADMIN_PASSWORD
     * (ver AdminAccountConfig). Sem configuração válida, a conta ADMIN
     * não é criada e o login administrativo fica indisponível.
     */
    private static AdminAccountConfig.Status adminStatus;
    private static String adminKey;

    private static User currentUser;

    static {
        configureAdmin(AdminAccountConfig.fromEnvironment());
    }

    private UserRepository() {
    }

    /**
     * Cria (ou recria) a conta administrativa a partir da configuração.
     * Remove a conta ADMIN anterior; se a configuração for inválida,
     * nenhuma conta ADMIN permanece. Visível no pacote para os testes.
     */
    static synchronized void configureAdmin(AdminAccountConfig config) {
        if (adminKey != null) {
            USERS.remove(adminKey);
            adminKey = null;
        }

        adminStatus = config.status();

        if (!config.isValid()) {
            // Nunca registrar a senha, apenas o motivo.
            LOGGER.log(System.Logger.Level.WARNING,
                "Modo administrativo não configurado (" + adminStatus + "): "
                + "a conta ADMIN não foi criada.");
            return;
        }

        String key = config.username().toLowerCase();

        User admin = new User(
            "Administrador",
            config.username(),
            config.password(),
            Role.ADMIN
        );

        USERS.put(key, admin);
        adminKey = key;
    }

    /** true se a conta ADMIN foi criada com uma configuração válida. */
    public static synchronized boolean isAdminConfigured() {
        return adminStatus == AdminAccountConfig.Status.CONFIGURED && adminKey != null;
    }

    public static synchronized AdminAccountConfig.Status getAdminConfigStatus() {
        return adminStatus;
    }

    public static boolean register(String name, String username, String password) {

        String normalizedUsername = username.trim().toLowerCase();

        if (USERS.containsKey(normalizedUsername)) {
            return false;
        }

        User user = new User(
            name.trim(),
            username.trim(),
            password,
            Role.USER
        );

        USERS.put(normalizedUsername, user);

        return true;
    }

    public static User authenticateUser(String username, String password) {
        return authenticate(username, password, Role.USER);
    }

    public static User authenticateAdmin(String username, String password) {
        return authenticate(username, password, Role.ADMIN);
    }

    private static User authenticate(String username, String password, Role requiredRole) {
        User user = findByUsername(username);

        if (user == null || user.getRole() != requiredRole) {
            // Mesmo custo de uma verificação real; o resultado é descartado.
            PasswordHasher.matches(password, DummyHashHolder.HASH);
            return null;
        }

        return user.verifyPassword(password) ? user : null;
    }

    public static boolean usernameExists(String username) {
        return findByUsername(username) != null;
    }

    private static User findByUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }

        return USERS.get(username.trim().toLowerCase());
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }
}
