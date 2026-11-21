package com.serviceflow.service;
import com.serviceflow.model.Appointment;
import com.serviceflow.model.Client;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public final class AppointmentRepository {
    private static final List<Appointment> APPOINTMENTS =
            new ArrayList<>();

    /** Situação de um horário pretendido para um agendamento. */
    public enum ScheduleStatus {
        OK,
        /** Data, horário ou duração ausentes ou inválidos. */
        INVALID,
        /** Começa antes da abertura. */
        BEFORE_OPENING,
        /** Termina depois do fechamento (ou começa após ele). */
        AFTER_CLOSING,
        /** Ocupa parte do período de outro agendamento. */
        CONFLICT
    }

    /**
     * Resultado da verificação de horário.
     *
     * @param status   situação do horário
     * @param conflict agendamento em conflito (somente em CONFLICT)
     * @param message  mensagem para o usuário (null quando OK)
     */
    public record ScheduleCheck(ScheduleStatus status, Appointment conflict, String message) {
        public boolean isValid() {
            return status == ScheduleStatus.OK;
        }
    }

    private AppointmentRepository() {
    }

    public static List<Appointment> getAll() {
        return APPOINTMENTS;
    }

    /**
     * Adiciona um agendamento. Só aceita agendamentos vinculados a um
     * cliente cadastrado, dentro do horário de funcionamento e sem
     * conflito com outros agendamentos; o formulário já valida isso
     * antes de salvar.
     *
     * @throws IllegalArgumentException se alguma dessas regras não for atendida
     */
    public static void add(Appointment appointment) {
        if (appointment == null) {
            throw new IllegalArgumentException("Agendamento inválido.");
        }

        if (!ClientRepository.isRegistered(appointment.getClient())) {
            throw new IllegalArgumentException(
                "O agendamento precisa de um cliente cadastrado.");
        }

        ScheduleCheck check = checkSchedule(
            appointment.getDate(),
            appointment.getTime(),
            appointment.getDurationMinutes(),
            appointment
        );

        if (!check.isValid()) {
            throw new IllegalArgumentException(check.message());
        }

        APPOINTMENTS.add(appointment);
    }

    /**
     * Altera data, horário e duração de um agendamento existente,
     * aplicando as mesmas regras da criação (expediente e conflitos).
     * Se o novo horário for inválido, nada é alterado.
     *
     * @throws IllegalArgumentException se o novo horário for inválido
     */
    public static void reschedule(Appointment appointment, LocalDate date,
                                  LocalTime time, String duration) {
        if (appointment == null) {
            throw new IllegalArgumentException("Agendamento inválido.");
        }

        ScheduleCheck check = checkSchedule(
            date, time, effectiveMinutes(duration), appointment
        );

        if (!check.isValid()) {
            throw new IllegalArgumentException(check.message());
        }

        appointment.setDate(date);
        appointment.setTime(time);
        appointment.setDuration(duration);
    }

    /**
     * Verifica se o período pretendido respeita o horário de funcionamento
     * e não conflita com outro agendamento. Usada na criação e na edição.
     *
     * @param ignore agendamento que está sendo editado (não conflita consigo mesmo)
     */
    public static ScheduleCheck checkSchedule(LocalDate date, LocalTime start,
                                              int minutes, Appointment ignore) {
        if (date == null || start == null || minutes <= 0) {
            return new ScheduleCheck(ScheduleStatus.INVALID, null,
                "Informe a data, o horário e a duração do atendimento.");
        }

        if (BusinessHours.startsBeforeOpening(start)) {
            return new ScheduleCheck(ScheduleStatus.BEFORE_OPENING, null,
                "Horário fora do expediente: o atendimento começaria às "
                + BusinessHours.format(start) + ", antes da abertura ("
                + BusinessHours.format(BusinessHours.OPENING) + "). O expediente é "
                + BusinessHours.describe() + ".");
        }

        if (BusinessHours.endsAfterClosing(start, minutes)) {
            return new ScheduleCheck(ScheduleStatus.AFTER_CLOSING, null,
                afterClosingMessage(start, minutes));
        }

        Appointment conflict = findConflict(date, start, minutes, ignore);

        if (conflict != null) {
            return new ScheduleCheck(ScheduleStatus.CONFLICT, conflict,
                "Horário indisponível: conflita com o agendamento de "
                + conflict.getCliente()
                + " (" + BusinessHours.format(conflict.getTime())
                + " às " + BusinessHours.format(conflict.getEndTime()) + ").");
        }

        return new ScheduleCheck(ScheduleStatus.OK, null, null);
    }

    private static String afterClosingMessage(LocalTime start, int minutes) {
        String duration = formatMinutes(minutes);
        LocalTime latest = BusinessHours.latestStart(minutes);

        if (latest == null) {
            return "A duração do serviço (" + duration + ") ultrapassa o expediente, "
                + BusinessHours.describe() + ".";
        }

        LocalTime end = start.plusMinutes(minutes);
        String endText = end.isBefore(start)
            ? "no dia seguinte"
            : "às " + BusinessHours.format(end);

        return "Horário fora do expediente: o atendimento de " + duration
            + " terminaria " + endText + ", depois do fechamento ("
            + BusinessHours.format(BusinessHours.CLOSING) + "). Para essa duração, "
            + "o último horário de início é " + BusinessHours.format(latest) + ".";
    }

    /**
     * Duração em minutos usada nas validações. Durações não reconhecidas
     * ocupam um intervalo (30 minutos), como em Appointment.getDurationMinutes().
     */
    public static int effectiveMinutes(String duration) {
        int minutes = Appointment.durationToMinutes(duration);
        return minutes > 0 ? minutes : 30;
    }

    /** Ex.: "30 minutos", "1 hora", "1 hora e 30 minutos", "2 horas". */
    static String formatMinutes(int minutes) {
        int hours = minutes / 60;
        int rest = minutes % 60;

        String hoursText = hours == 1 ? "1 hora" : hours + " horas";
        String minutesText = rest == 1 ? "1 minuto" : rest + " minutos";

        if (hours == 0) {
            return minutesText;
        }

        return rest == 0 ? hoursText : hoursText + " e " + minutesText;
    }

    /** Agendamentos vinculados ao cliente (comparando pela identificação). */
    public static List<Appointment> findByClient(Client client) {
        List<Appointment> result = new ArrayList<>();

        for (Appointment appointment : APPOINTMENTS) {
            if (appointment.belongsTo(client)) {
                result.add(appointment);
            }
        }

        return result;
    }

    public static int countByClient(Client client) {
        return findByClient(client).size();
    }

    public static void remove(Appointment appointment) {
        APPOINTMENTS.remove(appointment);
    }

    /**
     * Procura um agendamento que ocupe parte do período informado.
     *
     * @param ignore agendamento que está sendo editado (não conflita consigo mesmo)
     * @return o agendamento em conflito, ou null se o horário estiver livre
     */
    public static Appointment findConflict(LocalDate date, LocalTime start,
                                           int minutes, Appointment ignore) {
        if (date == null || start == null || minutes <= 0) {
            return null;
        }

        for (Appointment appointment : APPOINTMENTS) {
            if (appointment != ignore
                    && appointment.overlaps(date, start, minutes)) {
                return appointment;
            }
        }

        return null;
    }
}
