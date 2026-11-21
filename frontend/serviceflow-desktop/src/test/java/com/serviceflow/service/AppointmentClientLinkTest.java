package com.serviceflow.service;

import com.serviceflow.model.Appointment;
import com.serviceflow.model.Client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Vínculo entre agendamentos e clientes cadastrados.
 * Os repositórios são estáticos (em memória): cada teste remove o que
 * criou, primeiro os agendamentos e depois os clientes.
 */
@DisplayName("Agendamentos vinculados a clientes")
class AppointmentClientLinkTest {

    private static final LocalDate DATE = LocalDate.of(2030, 5, 20);

    private final List<Client> createdClients = new ArrayList<>();
    private final List<Appointment> createdAppointments = new ArrayList<>();

    @BeforeEach
    void requireEmptyRepositories() {
        // Nenhum outro teste cadastra clientes ou agendamentos.
        assertEquals(0, ClientRepository.count());
        assertTrue(AppointmentRepository.getAll().isEmpty());
    }

    @AfterEach
    void cleanUp() {
        createdAppointments.forEach(AppointmentRepository::remove);
        createdClients.forEach(ClientRepository::remove);

        assertEquals(0, ClientRepository.count(), "limpeza incompleta");
        assertTrue(AppointmentRepository.getAll().isEmpty(), "limpeza incompleta");
    }

    private Client registerClient(String name, String phone) {
        Client client = new Client(name, phone, "", "");
        ClientRepository.add(client);
        createdClients.add(client);
        return client;
    }

    private Appointment newAppointment(Client client, LocalTime time) {
        return new Appointment(DATE, time, client, "Formatação",
            "Software e sistemas", "Formatar notebook", "1 hora", 150.0, "Ativo");
    }

    private Appointment saveAppointment(Client client, LocalTime time) {
        Appointment appointment = newAppointment(client, time);
        AppointmentRepository.add(appointment);
        createdAppointments.add(appointment);
        return appointment;
    }

    @Nested
    @DisplayName("Seletor de clientes")
    class Selector {

        @Test
        @DisplayName("carrega todos os clientes cadastrados em ordem alfabética")
        void loadsRegisteredClients() {
            Client carla = registerClient("Carla", "1111");
            Client ana = registerClient("Ana", "2222");
            Client alvaro = registerClient("Álvaro", "3333");

            assertEquals(List.of(alvaro, ana, carla), AppointmentClients.selectorItems());
        }

        @Test
        @DisplayName("reflete cadastros novos e exclusões")
        void reflectsRepositoryChanges() {
            Client ana = registerClient("Ana", "2222");
            assertEquals(List.of(ana), AppointmentClients.selectorItems());

            Client beto = registerClient("Beto", "4444");
            assertEquals(List.of(ana, beto), AppointmentClients.selectorItems());

            assertEquals(ClientRepository.RemovalResult.REMOVED, ClientRepository.remove(ana));
            assertEquals(List.of(beto), AppointmentClients.selectorItems());
        }

        @Test
        @DisplayName("lista vazia quando não há clientes")
        void emptyWhenNoClients() {
            assertTrue(AppointmentClients.selectorItems().isEmpty());
        }

        @Test
        @DisplayName("exibe nome e telefone para diferenciar homônimos")
        void displayText() {
            assertEquals("Maria · (11) 9999-0000",
                AppointmentClients.displayText(new Client("Maria", "(11) 9999-0000", "", "")));
            assertEquals("Maria",
                AppointmentClients.displayText(new Client("Maria", "  ", "", "")));
            assertEquals("", AppointmentClients.displayText(null));
        }
    }

    @Nested
    @DisplayName("Criação")
    class Creation {

        @Test
        @DisplayName("cria agendamento com cliente existente, vinculado pela identificação")
        void createsWithExistingClient() {
            Client ana = registerClient("Ana", "2222");

            assertNull(AppointmentClients.validate(ana));

            Appointment appointment = saveAppointment(ana, LocalTime.of(9, 0));

            assertEquals(ana.getId(), appointment.getClientId());
            assertSame(ana, appointment.getClient());
            assertEquals("Ana", appointment.getCliente());
            assertEquals(List.of(appointment), AppointmentRepository.findByClient(ana));
        }

        @Test
        @DisplayName("bloqueia agendamento sem cliente selecionado")
        void blocksWithoutClient() {
            assertEquals(AppointmentClients.NO_CLIENTS_MESSAGE, AppointmentClients.validate(null));

            registerClient("Ana", "2222");
            assertEquals(AppointmentClients.CLIENT_REQUIRED_MESSAGE, AppointmentClients.validate(null));

            assertThrows(IllegalArgumentException.class,
                () -> AppointmentRepository.add(newAppointment(null, LocalTime.of(9, 0))));
            assertTrue(AppointmentRepository.getAll().isEmpty());
        }

        @Test
        @DisplayName("bloqueia cliente que nunca foi cadastrado")
        void blocksUnregisteredClient() {
            registerClient("Ana", "2222");
            Client ghost = new Client("Ana", "2222", "", "");

            assertEquals(AppointmentClients.CLIENT_NOT_FOUND_MESSAGE, AppointmentClients.validate(ghost));
            assertThrows(IllegalArgumentException.class,
                () -> AppointmentRepository.add(newAppointment(ghost, LocalTime.of(9, 0))));
            assertTrue(AppointmentRepository.getAll().isEmpty());
        }

        @Test
        @DisplayName("bloqueia cliente que foi excluído")
        void blocksRemovedClient() {
            Client ana = registerClient("Ana", "2222");
            ClientRepository.remove(ana);

            assertEquals(AppointmentClients.CLIENT_NOT_FOUND_MESSAGE, AppointmentClients.validate(ana));
            assertThrows(IllegalArgumentException.class,
                () -> AppointmentRepository.add(newAppointment(ana, LocalTime.of(9, 0))));
        }

        @Test
        @DisplayName("clientes com o mesmo nome recebem identificações diferentes")
        void sameNameDifferentIds() {
            Client first = registerClient("Maria", "1111");
            Client second = registerClient("Maria", "2222");

            assertNotEquals(first.getId(), second.getId());

            Appointment appointment = saveAppointment(second, LocalTime.of(9, 0));

            assertFalse(appointment.belongsTo(first));
            assertTrue(appointment.belongsTo(second));
            assertEquals(0, AppointmentRepository.countByClient(first));
        }
    }

    @Nested
    @DisplayName("Edição")
    class Editing {

        @Test
        @DisplayName("seleciona o cliente associado ao abrir a edição")
        void preselectsAssociatedClient() {
            registerClient("Ana", "2222");
            Client beto = registerClient("Beto", "4444");
            Appointment appointment = saveAppointment(beto, LocalTime.of(9, 0));

            List<Client> items = AppointmentClients.selectorItems();

            assertSame(beto, AppointmentClients.findSelected(items, appointment));
        }

        @Test
        @DisplayName("localiza pelo id, não pelo nome (homônimos e nome alterado)")
        void preselectsById() {
            Client first = registerClient("Maria", "1111");
            Client second = registerClient("Maria", "2222");
            Appointment appointment = saveAppointment(second, LocalTime.of(9, 0));

            second.setName("Maria Souza");

            assertSame(second,
                AppointmentClients.findSelected(AppointmentClients.selectorItems(), appointment));
            assertNotEquals(first, appointment.getClient());
            assertEquals("Maria Souza", appointment.getCliente());
        }

        @Test
        @DisplayName("sem cliente correspondente na lista, nada é pré-selecionado")
        void noMatch() {
            Client ana = registerClient("Ana", "2222");
            Appointment appointment = saveAppointment(ana, LocalTime.of(9, 0));

            assertNull(AppointmentClients.findSelected(List.of(), appointment));
            assertNull(AppointmentClients.findSelected(AppointmentClients.selectorItems(), null));
        }

        @Test
        @DisplayName("troca o cliente do agendamento durante a edição")
        void changesClient() {
            Client ana = registerClient("Ana", "2222");
            Client beto = registerClient("Beto", "4444");
            Appointment appointment = saveAppointment(ana, LocalTime.of(9, 0));

            assertNull(AppointmentClients.validate(beto));
            appointment.setClient(beto);

            assertEquals(beto.getId(), appointment.getClientId());
            assertEquals(0, AppointmentRepository.countByClient(ana));
            assertEquals(1, AppointmentRepository.countByClient(beto));
            assertSame(beto,
                AppointmentClients.findSelected(AppointmentClients.selectorItems(), appointment));
        }
    }

    @Nested
    @DisplayName("Alteração e exclusão de clientes")
    class ClientChanges {

        @Test
        @DisplayName("cliente com agendamentos não é excluído e os agendamentos são mantidos")
        void blocksDeletionWithAppointments() {
            Client ana = registerClient("Ana", "2222");
            Appointment first = saveAppointment(ana, LocalTime.of(9, 0));
            Appointment second = saveAppointment(ana, LocalTime.of(14, 0));

            assertEquals(ClientRepository.RemovalResult.HAS_APPOINTMENTS,
                ClientRepository.remove(ana));

            assertTrue(ClientRepository.isRegistered(ana));
            assertEquals(List.of(first, second), AppointmentRepository.getAll());
            assertSame(ana, first.getClient());
        }

        @Test
        @DisplayName("após remover os agendamentos, o cliente pode ser excluído")
        void allowsDeletionWithoutAppointments() {
            Client ana = registerClient("Ana", "2222");
            Appointment appointment = saveAppointment(ana, LocalTime.of(9, 0));

            AppointmentRepository.remove(appointment);
            createdAppointments.remove(appointment);

            assertEquals(ClientRepository.RemovalResult.REMOVED, ClientRepository.remove(ana));
            assertFalse(ClientRepository.isRegistered(ana));
            assertEquals(ClientRepository.RemovalResult.NOT_FOUND, ClientRepository.remove(ana));
        }

        @Test
        @DisplayName("após trocar o cliente do agendamento, o antigo pode ser excluído")
        void allowsDeletionAfterReassignment() {
            Client ana = registerClient("Ana", "2222");
            Client beto = registerClient("Beto", "4444");
            Appointment appointment = saveAppointment(ana, LocalTime.of(9, 0));

            appointment.setClient(beto);

            assertEquals(ClientRepository.RemovalResult.REMOVED, ClientRepository.remove(ana));
            assertSame(beto, appointment.getClient());
        }

        @Test
        @DisplayName("editar o cliente atualiza o nome exibido nos agendamentos")
        void editingClientUpdatesAppointments() {
            Client ana = registerClient("Ana", "2222");
            Appointment appointment = saveAppointment(ana, LocalTime.of(9, 0));
            String id = ana.getId();

            ana.setName("Ana Paula");
            ana.setPhone("9999");

            assertEquals("Ana Paula", appointment.getCliente());
            assertEquals(id, appointment.getClientId());
            assertSame(ana, ClientRepository.findById(id));
        }

        @Test
        @DisplayName("busca por id: encontra cadastrados e ignora ids desconhecidos")
        void findById() {
            Client ana = registerClient("Ana", "2222");

            assertSame(ana, ClientRepository.findById(ana.getId()));
            assertNull(ClientRepository.findById("id-inexistente"));
            assertNull(ClientRepository.findById(null));
        }
    }

    @Nested
    @DisplayName("Regras de agendamento preservadas")
    class PreservedRules {

        @Test
        @DisplayName("conflito de horário continua detectado com clientes vinculados")
        void conflictsStillDetected() {
            Client ana = registerClient("Ana", "2222");
            Client beto = registerClient("Beto", "4444");
            Appointment existing = saveAppointment(ana, LocalTime.of(9, 0));

            assertSame(existing,
                AppointmentRepository.findConflict(DATE, LocalTime.of(9, 30), 60, null));
            assertNull(
                AppointmentRepository.findConflict(DATE, LocalTime.of(10, 0), 60, null));
            // Em edição, o agendamento não conflita consigo mesmo.
            assertNull(
                AppointmentRepository.findConflict(DATE, LocalTime.of(9, 0), 60, existing));
            assertEquals("Ana", existing.getCliente());
            assertEquals(0, AppointmentRepository.countByClient(beto));
        }

        @Test
        @DisplayName("duração, horário de término e preço continuam iguais")
        void durationAndPrice() {
            Client ana = registerClient("Ana", "2222");
            Appointment appointment = saveAppointment(ana, LocalTime.of(9, 0));

            assertEquals(60, appointment.getDurationMinutes());
            assertEquals(LocalTime.of(10, 0), appointment.getEndTime());
            assertEquals(150.0, appointment.getPrice());
            assertEquals(90, Appointment.durationToMinutes("1 hora e 30 minutos"));
        }
    }
}
