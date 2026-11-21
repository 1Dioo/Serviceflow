package com.serviceflow.service;

import com.serviceflow.model.Appointment;
import com.serviceflow.model.Client;
import com.serviceflow.model.Service;
import com.serviceflow.service.AppointmentRepository.ScheduleStatus;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * O painel administrativo reutiliza as telas de Agenda e Serviços, que
 * leem os mesmos repositórios da janela principal. Estes testes verificam
 * que os dados e as regras são os mesmos, independentemente da janela.
 */
@DisplayName("Painel administrativo - dados compartilhados de Agenda e Serviços")
class AdminSharedDataTest {

    private static final LocalDate DATE = LocalDate.of(2030, 7, 1);

    private final List<Service> createdServices = new ArrayList<>();
    private final List<Client> createdClients = new ArrayList<>();
    private final List<Appointment> createdAppointments = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        createdAppointments.forEach(AppointmentRepository::remove);
        createdClients.forEach(ClientRepository::remove);
        createdServices.forEach(ServiceRepository::remove);

        assertTrue(AppointmentRepository.getAll().isEmpty(), "limpeza incompleta");
        assertEquals(0, ClientRepository.count(), "limpeza incompleta");
        assertTrue(ServiceRepository.getAll().isEmpty(), "limpeza incompleta");
    }

    private Service newService(String name, String duration, double price, String status) {
        Service service = new Service(name, "Outros", "Descrição", duration, price, status);
        ServiceRepository.add(service);
        createdServices.add(service);
        return service;
    }

    private Client newClient(String name) {
        Client client = new Client(name, "1111", "", "");
        ClientRepository.add(client);
        createdClients.add(client);
        return client;
    }

    private Appointment newAppointment(Client client, Service service, LocalTime time) {
        Appointment appointment = new Appointment(DATE, time, client, service.getName(),
            service.getCategory(), "Atendimento", service.getDuration(), service.getPrice(), "Ativo");
        AppointmentRepository.add(appointment);
        createdAppointments.add(appointment);
        return appointment;
    }

    private static List<String> names(List<Service> services) {
        return services.stream().map(Service::getName).collect(Collectors.toList());
    }

    @Nested
    @DisplayName("Serviços")
    class Services {

        @Test
        @DisplayName("catálogo exibido inclui serviços padrão e cadastrados")
        void catalogVisible() {
            int defaults = ServiceRepository.getCatalog().size();
            assertTrue(defaults > 0);

            newService("Teste Admin Serviço", "1 hora", 100.0, "Ativo");

            assertEquals(defaults + 1, ServiceRepository.getCatalog().size());
            assertTrue(names(ServiceRepository.getCatalog()).contains("Teste Admin Serviço"));
        }

        @Test
        @DisplayName("criação: o serviço aparece no catálogo e no formulário de agendamento")
        void creationReflected() {
            Service created = newService("Teste Criado", "2 horas", 200.0, "Ativo");

            assertTrue(ServiceRepository.getCatalog().contains(created));
            assertTrue(ServiceRepository.getActive().contains(created));
            assertSame(created, ServiceRepository.findByName("Teste Criado"));
        }

        @Test
        @DisplayName("nome duplicado é detectado sem diferenciar maiúsculas e espaços")
        void duplicateNameDetected() {
            Service created = newService("Teste Duplicado", "1 hora", 100.0, "Ativo");

            assertSame(created, ServiceRepository.findByName("teste duplicado"));
            assertSame(created, ServiceRepository.findByName("  TESTE DUPLICADO  "));
            // Serviço padrão também conta como nome existente.
            assertTrue(ServiceRepository.findByName("diagnóstico de hardware") != null);
        }

        @Test
        @DisplayName("edição: alterações aparecem em todas as listas")
        void editingReflected() {
            Service service = newService("Teste Edição", "1 hora", 100.0, "Ativo");

            // Mesmo procedimento do NewServiceController ao salvar a edição.
            service.setName("Teste Editado");
            service.setDuration("2 horas");
            service.setPrice(200.0);

            assertNull(ServiceRepository.findByName("Teste Edição"));
            assertSame(service, ServiceRepository.findByName("Teste Editado"));
            assertEquals("2 horas", ServiceRepository.findByName("Teste Editado").getDuration());
            assertTrue(ServiceRepository.getActive().contains(service));
        }

        @Test
        @DisplayName("serviço inativo some do formulário de agendamento, mas continua no catálogo")
        void inactiveService() {
            Service service = newService("Teste Inativo", "1 hora", 100.0, "Ativo");

            service.setStatus("Inativo");

            assertFalse(ServiceRepository.getActive().contains(service));
            assertTrue(ServiceRepository.getCatalog().contains(service));
        }

        @Test
        @DisplayName("exclusão: o serviço some das listas e os agendamentos existentes são mantidos")
        void deletionReflected() {
            Service service = newService("Teste Exclusão", "1 hora", 100.0, "Ativo");
            Appointment appointment = newAppointment(newClient("Ana"), service, LocalTime.of(9, 0));

            ServiceRepository.remove(service);
            createdServices.remove(service);

            assertFalse(ServiceRepository.getCatalog().contains(service));
            assertFalse(ServiceRepository.getActive().contains(service));
            assertNull(ServiceRepository.findByName("Teste Exclusão"));

            // Regra atual: "Os agendamentos já registrados não serão alterados".
            assertTrue(AppointmentRepository.getAll().contains(appointment));
            assertEquals("Teste Exclusão", appointment.getService());
            assertEquals(100.0, appointment.getPrice());
        }
    }

    @Nested
    @DisplayName("Agenda")
    class Agenda {

        @Test
        @DisplayName("mostra os agendamentos corretos, com cliente, serviço, horário, duração e preço")
        void appointmentsVisible() {
            Service service = newService("Teste Agenda", "1 hora e 30 minutos", 150.0, "Ativo");
            Client ana = newClient("Ana");
            Appointment appointment = newAppointment(ana, service, LocalTime.of(10, 0));

            assertEquals(List.of(appointment), AppointmentRepository.getAll());
            assertEquals("Ana", appointment.getCliente());
            assertEquals("Teste Agenda", appointment.getService());
            assertEquals(LocalTime.of(10, 0), appointment.getTime());
            assertEquals(LocalTime.of(11, 30), appointment.getEndTime());
            assertEquals(150.0, appointment.getPrice());
        }

        @Test
        @DisplayName("alterações em agendamentos e clientes ficam visíveis para as duas janelas")
        void updatesVisible() {
            Service service = newService("Teste Atualização", "1 hora", 100.0, "Ativo");
            Client ana = newClient("Ana");
            Appointment appointment = newAppointment(ana, service, LocalTime.of(9, 0));

            AppointmentRepository.reschedule(appointment, DATE, LocalTime.of(14, 0), "1 hora");
            ana.setName("Ana Paula");

            Appointment shown = AppointmentRepository.getAll().get(0);
            assertSame(appointment, shown);
            assertEquals(LocalTime.of(14, 0), shown.getTime());
            assertEquals("Ana Paula", shown.getCliente());

            AppointmentRepository.remove(appointment);
            createdAppointments.remove(appointment);
            assertTrue(AppointmentRepository.getAll().isEmpty());
        }

        @Test
        @DisplayName("regras de expediente e conflito continuam valendo com serviços do catálogo")
        void schedulingRulesPreserved() {
            Service twoHours = newService("Teste Duas Horas", "2 horas", 200.0, "Ativo");
            Client ana = newClient("Ana");
            newAppointment(ana, twoHours, LocalTime.of(9, 0));

            int minutes = AppointmentRepository.effectiveMinutes(twoHours.getDuration());

            assertEquals(ScheduleStatus.CONFLICT,
                AppointmentRepository.checkSchedule(DATE, LocalTime.of(10, 0), minutes, null).status());
            assertEquals(ScheduleStatus.AFTER_CLOSING,
                AppointmentRepository.checkSchedule(DATE, LocalTime.of(16, 30), minutes, null).status());
            assertEquals(ScheduleStatus.BEFORE_OPENING,
                AppointmentRepository.checkSchedule(DATE, LocalTime.of(7, 0), minutes, null).status());
            assertTrue(
                AppointmentRepository.checkSchedule(DATE, LocalTime.of(16, 0), minutes, null).isValid());

            assertThrows(IllegalArgumentException.class,
                () -> AppointmentRepository.add(new Appointment(DATE, LocalTime.of(10, 0), ana,
                    twoHours.getName(), "Outros", "x", "2 horas", 200.0, "Ativo")));
        }
    }
}
