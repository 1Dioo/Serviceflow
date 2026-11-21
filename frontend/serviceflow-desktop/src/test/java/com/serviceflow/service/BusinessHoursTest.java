package com.serviceflow.service;

import com.serviceflow.model.Appointment;
import com.serviceflow.model.Client;
import com.serviceflow.service.AppointmentRepository.ScheduleCheck;
import com.serviceflow.service.AppointmentRepository.ScheduleStatus;

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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Horário de funcionamento (08:00 às 18:00) aplicado na criação e na
 * edição de agendamentos. Os repositórios são estáticos: cada teste
 * remove o que criou.
 */
@DisplayName("Horário de funcionamento dos agendamentos")
class BusinessHoursTest {

    private static final LocalDate DATE = LocalDate.of(2030, 6, 10);

    private final List<Client> createdClients = new ArrayList<>();
    private final List<Appointment> createdAppointments = new ArrayList<>();

    private Client client;

    @BeforeEach
    void setUp() {
        client = new Client("Ana", "1111", "", "");
        ClientRepository.add(client);
        createdClients.add(client);
    }

    @AfterEach
    void cleanUp() {
        createdAppointments.forEach(AppointmentRepository::remove);
        createdClients.forEach(ClientRepository::remove);

        assertTrue(AppointmentRepository.getAll().isEmpty(), "limpeza incompleta");
        assertEquals(0, ClientRepository.count(), "limpeza incompleta");
    }

    private static LocalTime at(int hour, int minute) {
        return LocalTime.of(hour, minute);
    }

    private Appointment appointment(LocalTime time, String duration) {
        return new Appointment(DATE, time, client, "Formatação", "Software e sistemas",
            "Formatar notebook", duration, 150.0, "Ativo");
    }

    /** Cria pelo repositório (mesmo caminho do formulário). */
    private Appointment save(LocalTime time, String duration) {
        Appointment appointment = appointment(time, duration);
        AppointmentRepository.add(appointment);
        createdAppointments.add(appointment);
        return appointment;
    }

    private static ScheduleCheck check(LocalTime time, String duration, Appointment ignore) {
        return AppointmentRepository.checkSchedule(
            DATE, time, AppointmentRepository.effectiveMinutes(duration), ignore);
    }

    @Nested
    @DisplayName("Definição do expediente")
    class Definition {

        @Test
        @DisplayName("expediente das 08:00 às 18:00, igual ao já usado no projeto")
        void openingAndClosing() {
            assertEquals(at(8, 0), BusinessHours.OPENING);
            assertEquals(at(18, 0), BusinessHours.CLOSING);
            assertEquals("das 08:00 às 18:00", BusinessHours.describe());
        }

        @Test
        @DisplayName("grade da Agenda: 08:00 a 18:00 de 30 em 30 minutos (inalterada)")
        void agendaRows() {
            List<LocalTime> rows = BusinessHours.agendaRows();

            assertEquals(21, rows.size());
            assertEquals(at(8, 0), rows.get(0));
            assertEquals(at(8, 30), rows.get(1));
            assertEquals(at(18, 0), rows.get(rows.size() - 1));
        }

        @Test
        @DisplayName("horários de início do formulário: 08:00 a 17:30 (18:00 nunca seria válido)")
        void startTimes() {
            List<LocalTime> starts = BusinessHours.startTimes();

            assertEquals(20, starts.size());
            assertEquals(at(8, 0), starts.get(0));
            assertEquals(at(17, 30), starts.get(starts.size() - 1));
            assertFalse(starts.contains(at(18, 0)));
            starts.forEach(start -> assertTrue(BusinessHours.fits(start, 30)));
        }

        @Test
        @DisplayName("último horário de início conforme a duração")
        void latestStart() {
            assertEquals(at(17, 30), BusinessHours.latestStart(30));
            assertEquals(at(17, 0), BusinessHours.latestStart(60));
            assertEquals(at(14, 0), BusinessHours.latestStart(240));
            assertEquals(at(8, 0), BusinessHours.latestStart(600));
            assertNull(BusinessHours.latestStart(601));
        }

        @Test
        @DisplayName("texto das durações nas mensagens")
        void formatMinutes() {
            assertEquals("30 minutos", AppointmentRepository.formatMinutes(30));
            assertEquals("1 hora", AppointmentRepository.formatMinutes(60));
            assertEquals("1 hora e 30 minutos", AppointmentRepository.formatMinutes(90));
            assertEquals("4 horas", AppointmentRepository.formatMinutes(240));
        }
    }

    @Nested
    @DisplayName("Criação")
    class Creation {

        @Test
        @DisplayName("aceita agendamento exatamente no horário de abertura")
        void atOpening() {
            assertTrue(check(at(8, 0), "1 hora", null).isValid());

            Appointment saved = save(at(8, 0), "1 hora");

            assertTrue(AppointmentRepository.getAll().contains(saved));
        }

        @Test
        @DisplayName("bloqueia agendamento antes da abertura")
        void beforeOpening() {
            ScheduleCheck result = check(at(7, 30), "1 hora", null);

            assertEquals(ScheduleStatus.BEFORE_OPENING, result.status());
            assertEquals("Horário fora do expediente: o atendimento começaria às 07:30, "
                + "antes da abertura (08:00). O expediente é das 08:00 às 18:00.",
                result.message());

            assertThrows(IllegalArgumentException.class,
                () -> AppointmentRepository.add(appointment(at(7, 30), "1 hora")));
            assertTrue(AppointmentRepository.getAll().isEmpty());
        }

        @Test
        @DisplayName("aceita agendamento que termina exatamente no fechamento")
        void endsAtClosing() {
            assertTrue(check(at(17, 30), "30 minutos", null).isValid());
            assertTrue(check(at(17, 0), "1 hora", null).isValid());

            Appointment saved = save(at(17, 0), "1 hora");

            assertEquals(at(18, 0), saved.getEndTime());
        }

        @Test
        @DisplayName("bloqueia agendamento que ultrapassa o fechamento")
        void endsAfterClosing() {
            ScheduleCheck result = check(at(17, 30), "1 hora", null);

            assertEquals(ScheduleStatus.AFTER_CLOSING, result.status());
            assertEquals("Horário fora do expediente: o atendimento de 1 hora terminaria "
                + "às 18:30, depois do fechamento (18:00). Para essa duração, "
                + "o último horário de início é 17:00.", result.message());

            assertThrows(IllegalArgumentException.class,
                () -> AppointmentRepository.add(appointment(at(17, 30), "1 hora")));
            assertTrue(AppointmentRepository.getAll().isEmpty());
        }

        @Test
        @DisplayName("considera a duração completa do serviço")
        void serviceDurationExceedsBusinessHours() {
            // O mesmo início é válido ou não conforme a duração do serviço.
            assertTrue(check(at(14, 30), "30 minutos", null).isValid());
            assertTrue(check(at(14, 0), "4 horas", null).isValid());

            ScheduleCheck result = check(at(14, 30), "4 horas", null);

            assertEquals(ScheduleStatus.AFTER_CLOSING, result.status());
            assertTrue(result.message().contains("de 4 horas terminaria às 18:30"));
            assertTrue(result.message().contains("o último horário de início é 14:00"));
            // 16:30 + 1h30 termina exatamente às 18:00; 17:00 + 1h30, às 18:30.
            assertTrue(check(at(16, 30), "1 hora e 30 minutos", null).isValid());
            assertEquals(ScheduleStatus.AFTER_CLOSING,
                check(at(17, 0), "1 hora e 30 minutos", null).status());
        }

        @Test
        @DisplayName("bloqueia início no fechamento, após o fechamento e que passaria da meia-noite")
        void startsAtOrAfterClosing() {
            assertEquals(ScheduleStatus.AFTER_CLOSING, check(at(18, 0), "30 minutos", null).status());
            assertEquals(ScheduleStatus.AFTER_CLOSING, check(at(19, 0), "1 hora", null).status());

            ScheduleCheck lateNight = check(at(23, 30), "1 hora", null);
            assertEquals(ScheduleStatus.AFTER_CLOSING, lateNight.status());
            assertTrue(lateNight.message().contains("terminaria no dia seguinte"));
        }

        @Test
        @DisplayName("bloqueia dados de horário ausentes")
        void missingData() {
            assertEquals(ScheduleStatus.INVALID,
                AppointmentRepository.checkSchedule(null, at(9, 0), 60, null).status());
            assertEquals(ScheduleStatus.INVALID,
                AppointmentRepository.checkSchedule(DATE, null, 60, null).status());
            assertEquals(ScheduleStatus.INVALID,
                AppointmentRepository.checkSchedule(DATE, at(9, 0), 0, null).status());
        }
    }

    @Nested
    @DisplayName("Edição")
    class Editing {

        @Test
        @DisplayName("aceita reagendar dentro do expediente")
        void rescheduleInside() {
            Appointment appointment = save(at(9, 0), "1 hora");
            LocalDate newDate = DATE.plusDays(1);

            AppointmentRepository.reschedule(appointment, newDate, at(17, 0), "1 hora");

            assertEquals(newDate, appointment.getDate());
            assertEquals(at(17, 0), appointment.getTime());
            assertEquals(at(18, 0), appointment.getEndTime());
        }

        @Test
        @DisplayName("bloqueia reagendar para fora do expediente e não altera o agendamento")
        void rescheduleOutside() {
            Appointment appointment = save(at(9, 0), "1 hora");

            IllegalArgumentException afterClosing = assertThrows(IllegalArgumentException.class,
                () -> AppointmentRepository.reschedule(appointment, DATE, at(17, 30), "1 hora"));
            assertTrue(afterClosing.getMessage().startsWith("Horário fora do expediente"));

            assertThrows(IllegalArgumentException.class,
                () -> AppointmentRepository.reschedule(appointment, DATE, at(7, 0), "1 hora"));

            assertEquals(DATE, appointment.getDate());
            assertEquals(at(9, 0), appointment.getTime());
            assertEquals("1 hora", appointment.getDuration());
        }

        @Test
        @DisplayName("bloqueia troca para um serviço mais longo que não cabe no expediente")
        void longerServiceOnEdit() {
            Appointment appointment = save(at(16, 0), "1 hora");

            assertEquals(ScheduleStatus.AFTER_CLOSING, check(at(16, 0), "4 horas", appointment).status());
            assertThrows(IllegalArgumentException.class,
                () -> AppointmentRepository.reschedule(appointment, DATE, at(16, 0), "4 horas"));

            assertEquals("1 hora", appointment.getDuration());
        }

        @Test
        @DisplayName("edição usa as mesmas regras da criação")
        void sameRulesAsCreation() {
            Appointment appointment = save(at(10, 0), "1 hora");

            for (LocalTime start : List.of(at(7, 30), at(8, 0), at(17, 0), at(17, 30), at(18, 0))) {
                assertEquals(
                    check(start, "1 hora", null).status(),
                    check(start, "1 hora", appointment).status(),
                    "início " + start
                );
            }
        }
    }

    @Nested
    @DisplayName("Conflitos de horário preservados")
    class Conflicts {

        @Test
        @DisplayName("detecta conflito dentro do expediente, com a mesma mensagem de antes")
        void conflictDetected() {
            Appointment existing = save(at(9, 0), "1 hora");

            ScheduleCheck result = check(at(9, 30), "1 hora", null);

            assertEquals(ScheduleStatus.CONFLICT, result.status());
            assertSame(existing, result.conflict());
            assertEquals("Horário indisponível: conflita com o agendamento de Ana (09:00 às 10:00).",
                result.message());

            assertThrows(IllegalArgumentException.class,
                () -> AppointmentRepository.add(appointment(at(9, 30), "1 hora")));
            assertEquals(List.of(existing), AppointmentRepository.getAll());
        }

        @Test
        @DisplayName("horários vizinhos não conflitam")
        void adjacentSlots() {
            save(at(9, 0), "1 hora");

            assertTrue(check(at(10, 0), "1 hora", null).isValid());
            assertTrue(check(at(8, 0), "1 hora", null).isValid());
        }

        @Test
        @DisplayName("na edição, o agendamento não conflita consigo mesmo, mas conflita com outros")
        void conflictsOnEdit() {
            Appointment first = save(at(9, 0), "1 hora");
            Appointment second = save(at(11, 0), "1 hora");

            AppointmentRepository.reschedule(first, DATE, at(9, 30), "1 hora");
            assertEquals(at(9, 30), first.getTime());

            assertThrows(IllegalArgumentException.class,
                () -> AppointmentRepository.reschedule(first, DATE, at(10, 30), "1 hora"));
            assertEquals(at(9, 30), first.getTime());
            assertEquals(at(11, 0), second.getTime());
        }
    }
}
