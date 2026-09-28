package com.serviceflow.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Appointment {
    private LocalDate date;
    private LocalTime time;
    private String cliente;
    private String service;
    private String status;

    public Appointment(LocalDate date, LocalTime time, String cliente, String service, String status) {
        this.date = date;
        this.time = time;
        this.cliente = cliente;
        this.service = service;
        this.status = status;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }

    public String getCliente() {
        return cliente;
    }

    public String getService() {
        return service;
    }

    public String getStatus() {
        return status;
    }
}
