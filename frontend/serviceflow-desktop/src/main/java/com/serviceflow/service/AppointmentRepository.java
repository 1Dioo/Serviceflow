package com.serviceflow.service;
import com.serviceflow.model.Appointment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public final class AppointmentRepository {
    private static final List<Appointment> APPOINTMENTS =
            new ArrayList<>();

    private AppointmentRepository() {
    }

    public static List<Appointment> getAll() {
        return APPOINTMENTS;
    }

    public static void add(Appointment appointment) {
        APPOINTMENTS.add(appointment);
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
