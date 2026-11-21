package com.serviceflow.model;

import com.serviceflow.security.PasswordHasher;

public class User {
    public enum Role {
        USER,
        ADMIN
    }

    private final String name;
    private final String username;

    // Hash PBKDF2 no formato pbkdf2_sha256$iterações$salt$hash
    // (o salt individual fica dentro do próprio hash).
    private final String passwordHash;

    private final Role role;

    public User(String name, String username, String password, Role role) {
        this.name = name;
        this.username = username;
        this.role = role;
        this.passwordHash = PasswordHasher.hash(password);
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public Role getRole() {
        return role;
    }

    public boolean verifyPassword(String password) {
        return PasswordHasher.matches(password, passwordHash);
    }

    // Uso restrito ao pacote (testes): nunca expor fora do modelo.
    String getPasswordHash() {
        return passwordHash;
    }
}
