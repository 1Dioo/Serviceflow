package com.serviceflow.model;

import com.serviceflow.security.PasswordHasher;
import com.serviceflow.service.AdminAccountConfig;
import com.serviceflow.service.AdminTestSupport;
import com.serviceflow.service.UserRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** A senha administrativa configurada é guardada apenas como hash PBKDF2. */
@DisplayName("Conta ADMIN - integração com PBKDF2")
class AdminPasswordHashTest {

    // Valor fictício, usado somente neste teste.
    private static final String ADMIN_PASSWORD = "teste-hash-admin-77";

    @AfterEach
    void restore() {
        AdminTestSupport.restoreFromEnvironment();
    }

    @Test
    @DisplayName("senha do ADMIN armazenada como hash PBKDF2, nunca em texto")
    void adminPasswordStoredAsPbkdf2() {
        AdminTestSupport.configure(null, ADMIN_PASSWORD);

        User admin = UserRepository.authenticateAdmin(
            AdminAccountConfig.DEFAULT_USERNAME, ADMIN_PASSWORD);

        assertNotNull(admin);

        String hash = admin.getPasswordHash();

        assertTrue(hash.startsWith(
            PasswordHasher.ALGORITHM_ID + "$" + PasswordHasher.ITERATIONS + "$"));
        assertFalse(hash.contains(ADMIN_PASSWORD));
        assertTrue(PasswordHasher.matches(ADMIN_PASSWORD, hash));
    }
}
