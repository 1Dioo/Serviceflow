package com.serviceflow.service;

import com.serviceflow.model.Appointment;
import com.serviceflow.model.Client;

import java.util.List;

/**
 * Regras do vínculo entre agendamento e cliente usadas pelo formulário
 * de agendamento. Fica fora do controller para poder ser testada sem
 * a interface JavaFX.
 */
public final class AppointmentClients {

    public static final String NO_CLIENTS_MESSAGE =
        "Nenhum cliente cadastrado. Cadastre um cliente na tela Clientes.";

    public static final String CLIENT_REQUIRED_MESSAGE =
        "Selecione um cliente.";

    public static final String CLIENT_NOT_FOUND_MESSAGE =
        "O cliente selecionado não está mais cadastrado. Selecione outro cliente.";

    private AppointmentClients() {
    }

    /** Itens do seletor de clientes: os clientes cadastrados, em ordem alfabética. */
    public static List<Client> selectorItems() {
        return ClientRepository.getSelectable();
    }

    /**
     * Texto exibido no seletor. O telefone acompanha o nome para
     * diferenciar clientes com o mesmo nome.
     */
    public static String displayText(Client client) {
        if (client == null) {
            return "";
        }

        String name = client.getName() == null ? "" : client.getName();
        String phone = client.getPhone() == null ? "" : client.getPhone().trim();

        return phone.isEmpty() ? name : name + " · " + phone;
    }

    /**
     * Valida o cliente escolhido no formulário.
     *
     * @return mensagem de erro, ou null se o cliente for válido
     */
    public static String validate(Client selected) {
        if (selected == null) {
            return ClientRepository.count() == 0
                ? NO_CLIENTS_MESSAGE
                : CLIENT_REQUIRED_MESSAGE;
        }

        if (!ClientRepository.isRegistered(selected)) {
            return CLIENT_NOT_FOUND_MESSAGE;
        }

        return null;
    }

    /**
     * Item do seletor que corresponde ao cliente do agendamento em edição,
     * localizado pela identificação (não pelo nome exibido).
     *
     * @return o item correspondente, ou null se o cliente não estiver na lista
     */
    public static Client findSelected(List<Client> items, Appointment appointment) {
        if (items == null || appointment == null || appointment.getClientId() == null) {
            return null;
        }

        for (Client client : items) {
            if (appointment.getClientId().equals(client.getId())) {
                return client;
            }
        }

        return null;
    }
}
