package com.serviceflow.service;
import com.serviceflow.model.Appointment;

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
}
