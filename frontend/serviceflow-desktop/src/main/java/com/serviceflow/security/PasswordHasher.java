package com.serviceflow.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Geração e verificação de hashes de senha com PBKDF2-HMAC-SHA256.
 *
 * Formato armazenado (autodescritivo, permite aumentar as iterações
 * no futuro sem invalidar hashes antigos):
 *
 *   pbkdf2_sha256$<iterações>$<salt Base64>$<hash Base64>
 *
 * A senha nunca é armazenada nem registrada em log.
 */
public final class PasswordHasher {

    public static final String ALGORITHM_ID = "pbkdf2_sha256";

    /** Recomendação OWASP para PBKDF2-HMAC-SHA256. */
    public static final int ITERATIONS = 600_000;

    public static final int SALT_BYTES = 16;

    public static final int KEY_BITS = 256;

    private static final String JCA_ALGORITHM = "PBKDF2WithHmacSHA256";

    // Limites de segurança para valores lidos de um hash armazenado.
    private static final int MIN_ITERATIONS = 1;
    private static final int MAX_ITERATIONS = 10_000_000;

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {
    }

    /**
     * Gera o hash de uma senha com um salt aleatório individual.
     */
    public static String hash(String password) {
        if (password == null) {
            throw new IllegalArgumentException("A senha não pode ser nula.");
        }

        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);

        byte[] key = derive(password, salt, ITERATIONS, KEY_BITS);

        try {
            return ALGORITHM_ID
                + "$" + ITERATIONS
                + "$" + Base64.getEncoder().encodeToString(salt)
                + "$" + Base64.getEncoder().encodeToString(key);
        } finally {
            Arrays.fill(key, (byte) 0);
        }
    }

    /**
     * Verifica se a senha corresponde ao hash armazenado.
     * A comparação é feita em tempo constante. Hashes malformados
     * ou senhas nulas resultam em false (nunca em exceção).
     */
    public static boolean matches(String password, String encoded) {
        if (password == null || encoded == null) {
            return false;
        }

        String[] parts = encoded.split("\\$");

        if (parts.length != 4 || !ALGORITHM_ID.equals(parts[0])) {
            return false;
        }

        int iterations;
        byte[] salt;
        byte[] expected;

        try {
            iterations = Integer.parseInt(parts[1]);
            salt = Base64.getDecoder().decode(parts[2]);
            expected = Base64.getDecoder().decode(parts[3]);
        } catch (IllegalArgumentException e) {
            return false;
        }

        if (iterations < MIN_ITERATIONS || iterations > MAX_ITERATIONS
                || salt.length == 0 || expected.length == 0) {
            return false;
        }

        byte[] actual = derive(password, salt, iterations, expected.length * 8);

        try {
            return MessageDigest.isEqual(expected, actual);
        } finally {
            Arrays.fill(actual, (byte) 0);
        }
    }

    private static byte[] derive(String password, byte[] salt,
                                 int iterations, int keyBits) {
        char[] chars = password.toCharArray();
        PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, keyBits);

        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(JCA_ALGORITHM);
            return factory.generateSecret(spec).getEncoded();

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Erro ao proteger a senha.", e);

        } finally {
            spec.clearPassword();
            Arrays.fill(chars, '\0');
        }
    }
}
