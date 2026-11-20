package com.serviceflow.service;

import com.serviceflow.model.Client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Armazena os clientes durante a execução da aplicação.
 * Os dados ficam fora do controller para não se perderem
 * quando a tela de Clientes é recarregada pela navegação.
 */
public final class ClientRepository {
    private static final List<Client> CLIENTS = new ArrayList<>();

    private ClientRepository() {
    }

    public static List<Client> getAll() {
        return Collections.unmodifiableList(CLIENTS);
    }

    public static void add(Client client) {
        if (client != null && !CLIENTS.contains(client)) {
            CLIENTS.add(client);
        }
    }

    public static void remove(Client client) {
        CLIENTS.remove(client);
    }

    public static int count() {
        return CLIENTS.size();
    }
}
