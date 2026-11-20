package com.serviceflow.model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class User {
    public enum Role {
        USER,
        ADMIN
    }

    private final String name;
    private final String username;
    private final String salt;
    private final String passwordHash;
    private final Role role;

    public User(String name, String username, String password, Role role) {
        this.name = name;
        this.username = username;
        this.role = role;

        byte[] saltBytes = new byte[16];
        new SecureRandom().nextBytes(saltBytes);
        this.salt = bytesToHex(saltBytes);
        this.passwordHash = hashPassword(password, salt);
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
        return passwordHash.equals(hashPassword(password, salt));
    }

    private static String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(hexToBytes(salt));

            byte[] hash = digest.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            return bytesToHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Erro ao proteger a senha.", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();

        for (byte value : bytes) {
            result.append(String.format("%02x", value));
        }

        return result.toString();
    }

    private static byte[] hexToBytes(String hex) {
        byte[] result = new byte[hex.length() / 2];

        for (int i = 0; i < result.length; i++) {
            int index = i * 2;
            result[i] = (byte) Integer.parseInt(
                hex.substring(index, index + 2), 16
            );
        }

        return result;
    }
}
