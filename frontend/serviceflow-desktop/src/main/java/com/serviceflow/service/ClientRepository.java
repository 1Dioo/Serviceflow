package com.serviceflow.service;

import com.serviceflow.model.Client;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Armazena os clientes durante a execução da aplicação.
 * Os dados ficam fora do controller para não se perderem
 * quando a tela de Clientes é recarregada pela navegação.
 */
public final class ClientRepository {
    private static final List<Client> CLIENTS = new ArrayList<>();

    /** Resultado da tentativa de excluir um cliente. */
    public enum RemovalResult {
        REMOVED,
        /** O cliente possui agendamentos vinculados e foi mantido. */
        HAS_APPOINTMENTS,
        NOT_FOUND
    }

    private ClientRepository() {
    }

    public static List<Client> getAll() {
        return Collections.unmodifiableList(CLIENTS);
    }

    /**
     * Clientes disponíveis para seleção em agendamentos,
     * em ordem alfabética (ignorando acentos e maiúsculas).
     */
    public static List<Client> getSelectable() {
        Collator collator = Collator.getInstance(Locale.forLanguageTag("pt-BR"));
        collator.setStrength(Collator.PRIMARY);

        List<Client> sorted = new ArrayList<>(CLIENTS);
        sorted.sort(Comparator.comparing(
            client -> client.getName() == null ? "" : client.getName(),
            collator
        ));

        return sorted;
    }

    public static void add(Client client) {
        if (client != null && !CLIENTS.contains(client)) {
            CLIENTS.add(client);
        }
    }

    /** Procura um cliente cadastrado pela identificação. */
    public static Client findById(String id) {
        if (id == null) {
            return null;
        }

        for (Client client : CLIENTS) {
            if (id.equals(client.getId())) {
                return client;
            }
        }

        return null;
    }

    /** true se o cliente informado está cadastrado (comparando pela identificação). */
    public static boolean isRegistered(Client client) {
        return client != null && findById(client.getId()) == client;
    }

    /**
     * Exclui o cliente somente se não houver agendamentos vinculados a ele,
     * para que nenhum agendamento fique sem cliente nem seja apagado
     * sem o usuário perceber.
     */
    public static RemovalResult remove(Client client) {
        if (!isRegistered(client)) {
            return RemovalResult.NOT_FOUND;
        }

        if (AppointmentRepository.countByClient(client) > 0) {
            return RemovalResult.HAS_APPOINTMENTS;
        }

        CLIENTS.remove(client);
        return RemovalResult.REMOVED;
    }

    public static int count() {
        return CLIENTS.size();
    }
}
