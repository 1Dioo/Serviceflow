package com.serviceflow.service;

import java.util.HashMap;
import java.util.Map;

/**
 * Apoio aos testes: configura a conta administrativa a partir de
 * valores simulados, sem ler nem alterar o ambiente real.
 */
public final class AdminTestSupport {

    private AdminTestSupport() {
    }

    /** Configura o ADMIN como se as variáveis/propriedade tivessem esses valores. */
    public static AdminAccountConfig configure(String username, String password) {
        Map<String, String> env = new HashMap<>();

        if (username != null) {
            env.put(AdminAccountConfig.USERNAME_ENV, username);
        }

        if (password != null) {
            env.put(AdminAccountConfig.PASSWORD_ENV, password);
        }

        AdminAccountConfig config = AdminAccountConfig.resolve(env::get, key -> null);
        UserRepository.configureAdmin(config);
        return config;
    }

    /** Restaura a configuração lida do ambiente real da execução. */
    public static void restoreFromEnvironment() {
        UserRepository.configureAdmin(AdminAccountConfig.fromEnvironment());
    }
}
