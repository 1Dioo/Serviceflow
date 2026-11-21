package com.serviceflow.service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Horário de funcionamento do ServiceFlow (fonte única das regras).
 *
 * Os valores reproduzem o expediente que já era usado no projeto:
 * grade da Agenda (08:00 a 18:00), horários do formulário de agendamento
 * e valores padrão da tela de Configurações.
 *
 * Regras:
 *  - o atendimento não pode começar antes da abertura;
 *  - o atendimento não pode terminar depois do fechamento
 *    (terminar exatamente no fechamento é permitido);
 *  - a duração completa do serviço é considerada.
 *
 * Não há restrição de dias da semana: o projeto não define dias de
 * funcionamento.
 */
public final class BusinessHours {

    public static final LocalTime OPENING = LocalTime.of(8, 0);
    public static final LocalTime CLOSING = LocalTime.of(18, 0);

    /** Intervalo entre os horários exibidos na Agenda e no formulário. */
    public static final int SLOT_MINUTES = 30;

    private static final DateTimeFormatter TIME_FORMAT =
        DateTimeFormatter.ofPattern("HH:mm");

    private BusinessHours() {
    }

    /**
     * Linhas da grade da Agenda: da abertura ao fechamento, inclusive
     * (a linha do fechamento marca o fim do expediente).
     */
    public static List<LocalTime> agendaRows() {
        return slots(CLOSING);
    }

    /**
     * Horários de início oferecidos no formulário: da abertura até o
     * último horário em que o menor atendimento (um intervalo) ainda
     * termina dentro do expediente.
     */
    public static List<LocalTime> startTimes() {
        return slots(CLOSING.minusMinutes(SLOT_MINUTES));
    }

    private static List<LocalTime> slots(LocalTime last) {
        List<LocalTime> result = new ArrayList<>();

        for (LocalTime time = OPENING; !time.isAfter(last); time = time.plusMinutes(SLOT_MINUTES)) {
            result.add(time);
        }

        return result;
    }

    public static boolean startsBeforeOpening(LocalTime start) {
        return start.isBefore(OPENING);
    }

    /**
     * Indica se um atendimento iniciado em {@code start} com a duração
     * informada termina depois do fechamento. O cálculo é feito em minutos
     * do dia, para não "dar a volta" na meia-noite.
     */
    public static boolean endsAfterClosing(LocalTime start, int minutes) {
        return minuteOfDay(start) + minutes > minuteOfDay(CLOSING);
    }

    /** true se o atendimento inteiro cabe no expediente. */
    public static boolean fits(LocalTime start, int minutes) {
        return start != null
            && minutes > 0
            && !startsBeforeOpening(start)
            && !endsAfterClosing(start, minutes);
    }

    /**
     * Último horário de início para a duração informada,
     * ou null se a duração for maior que o expediente inteiro.
     */
    public static LocalTime latestStart(int minutes) {
        int latest = minuteOfDay(CLOSING) - minutes;

        return latest < minuteOfDay(OPENING)
            ? null
            : LocalTime.of(latest / 60, latest % 60);
    }

    /** Texto do expediente para mensagens, ex.: "das 08:00 às 18:00". */
    public static String describe() {
        return "das " + format(OPENING) + " às " + format(CLOSING);
    }

    public static String format(LocalTime time) {
        return time.format(TIME_FORMAT);
    }

    private static int minuteOfDay(LocalTime time) {
        return time.getHour() * 60 + time.getMinute();
    }
}
