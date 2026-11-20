package com.serviceflow.service;

import com.serviceflow.model.User;
import com.serviceflow.model.User.Role;

import java.util.HashMap;
import java.util.Map;

public final class UserRepository {
    private static final Map<String, User> USERS = new HashMap<>();

    /*
     * CONTA ADMINISTRATIVA INICIAL
     */
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "121";

    private static User currentUser;

    static {
        User admin = new User(
            "Administrador",
            ADMIN_USERNAME,
            ADMIN_PASSWORD,
            Role.ADMIN
        );

        USERS.put(ADMIN_USERNAME.toLowerCase(), admin);
    }

    private UserRepository() {
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
        User user = findByUsername(username);

        if (user == null || user.getRole() != Role.USER) {
            return null;
        }

        return user.verifyPassword(password) ? user : null;
    }

    public static User authenticateAdmin(String username, String password) {
        User user = findByUsername(username);

        if (user == null || user.getRole() != Role.ADMIN) {
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
