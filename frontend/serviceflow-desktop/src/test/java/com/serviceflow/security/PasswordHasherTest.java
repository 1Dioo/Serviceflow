package com.serviceflow.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("PasswordHasher - PBKDF2-HMAC-SHA256")
class PasswordHasherTest {

    private static final String PASSWORD = "senhaSegura123";

    @Test
    @DisplayName("gera hash no formato pbkdf2_sha256$600000$salt$hash")
    void producesVersionedFormat() {
        String[] parts = PasswordHasher.hash(PASSWORD).split("\\$");

        assertEquals(4, parts.length);
        assertEquals("pbkdf2_sha256", parts[0]);
        assertEquals("600000", parts[1]);
        assertEquals(16, Base64.getDecoder().decode(parts[2]).length, "salt de 16 bytes");
        assertEquals(32, Base64.getDecoder().decode(parts[3]).length, "chave de 256 bits");
    }

    @Test
    @DisplayName("usa 600.000 iterações (recomendação OWASP)")
    void usesOwaspIterations() {
        assertEquals(600_000, PasswordHasher.ITERATIONS);
    }

    @Test
    @DisplayName("a mesma senha gera hashes diferentes (salt individual)")
    void samePasswordDifferentSalts() {
        String first = PasswordHasher.hash(PASSWORD);
        String second = PasswordHasher.hash(PASSWORD);

        assertNotEquals(first, second);
        assertNotEquals(first.split("\\$")[2], second.split("\\$")[2]);
    }

    @Test
    @DisplayName("o hash não contém a senha em texto")
    void doesNotContainPlainPassword() {
        assertFalse(PasswordHasher.hash(PASSWORD).contains(PASSWORD));
    }

    @Test
    @DisplayName("verifica a senha correta e recusa as erradas")
    void matchesOnlyCorrectPassword() {
        String encoded = PasswordHasher.hash(PASSWORD);

        assertTrue(PasswordHasher.matches(PASSWORD, encoded));
        assertFalse(PasswordHasher.matches("senhaSegura124", encoded));
        assertFalse(PasswordHasher.matches("", encoded));
        assertFalse(PasswordHasher.matches(PASSWORD.toUpperCase(), encoded));
    }

    @Test
    @DisplayName("respeita o número de iterações gravado no hash")
    void honoursStoredIterations() {
        String encoded = PasswordHasher.hash(PASSWORD);
        String[] parts = encoded.split("\\$");
        String tampered = parts[0] + "$1000$" + parts[2] + "$" + parts[3];

        // Mesmo salt e chave, mas outro número de iterações: não confere.
        assertFalse(PasswordHasher.matches(PASSWORD, tampered));
    }

    @Test
    @DisplayName("hashes malformados ou nulos resultam em false, sem exceção")
    void rejectsMalformedHashes() {
        String valid = PasswordHasher.hash(PASSWORD);
        String[] p = valid.split("\\$");

        assertFalse(PasswordHasher.matches(PASSWORD, null));
        assertFalse(PasswordHasher.matches(null, valid));
        assertFalse(PasswordHasher.matches(PASSWORD, ""));
        assertFalse(PasswordHasher.matches(PASSWORD, "abc"));
        assertFalse(PasswordHasher.matches(PASSWORD, "sha256$600000$" + p[2] + "$" + p[3]));
        assertFalse(PasswordHasher.matches(PASSWORD, p[0] + "$x$" + p[2] + "$" + p[3]));
        assertFalse(PasswordHasher.matches(PASSWORD, p[0] + "$0$" + p[2] + "$" + p[3]));
        assertFalse(PasswordHasher.matches(PASSWORD, p[0] + "$99999999$" + p[2] + "$" + p[3]));
        assertFalse(PasswordHasher.matches(PASSWORD, p[0] + "$600000$%%%$" + p[3]));
        assertFalse(PasswordHasher.matches(PASSWORD, p[0] + "$600000$$" + p[3]));
    }

    @Test
    @DisplayName("senha nula ao gerar hash é recusada")
    void rejectsNullOnHash() {
        assertThrows(IllegalArgumentException.class, () -> PasswordHasher.hash(null));
    }

    @Test
    @DisplayName("verificação leva menos de 1 segundo")
    void verificationIsFastEnoughForInteractiveLogin() {
        String encoded = PasswordHasher.hash(PASSWORD);

        long start = System.nanoTime();
        PasswordHasher.matches(PASSWORD, encoded);
        long millis = (System.nanoTime() - start) / 1_000_000;

        assertTrue(millis < 1000, "verificação levou " + millis + " ms");
    }
}
