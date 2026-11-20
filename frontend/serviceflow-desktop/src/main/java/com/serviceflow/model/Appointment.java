package com.serviceflow.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Appointment {
    private LocalDate date;
    private LocalTime time;

    private String cliente;
    private String service;
    private String category;
    private String description;
    private String duration;
    private double price;
    private String status;

    public Appointment(LocalDate date, LocalTime time, String cliente, String service,
            String category, String description, String duration, double price, String status) {
        this.date = date;
        this.time = time;
        this.cliente = cliente;
        this.service = service;
        this.category = category;
        this.description = description;
        this.duration = duration;
        this.price = price;
        this.status = status;
    }

    /**
     * Converte as durações usadas no sistema ("30 minutos", "1 hora",
     * "1 hora e 30 minutos", "2 horas"...) em minutos.
     * Retorna 0 quando o texto não é reconhecido.
     */
    public static int durationToMinutes(String duration) {
        if (duration == null) {
            return 0;
        }

        switch (duration.trim()) {
            case "30 minutos": return 30;
            case "45 minutos": return 45;
            case "1 hora": return 60;
            case "1 hora e 30 minutos": return 90;
            case "2 horas": return 120;
            case "3 horas": return 180;
            case "4 horas": return 240;
            default: return 0;
        }
    }

    /**
     * Duração em minutos. Durações não reconhecidas ocupam
     * ao menos um intervalo da agenda (30 minutos).
     */
    public int getDurationMinutes() {
        int minutes = durationToMinutes(duration);
        return minutes > 0 ? minutes : 30;
    }

    public LocalTime getEndTime() {
        return time.plusMinutes(getDurationMinutes());
    }

    /**
     * Indica se este agendamento ocupa algum minuto do intervalo
     * [start, start + minutes) na data informada.
     */
    public boolean overlaps(LocalDate otherDate, LocalTime start, int minutes) {
        if (date == null || time == null || !date.equals(otherDate)) {
            return false;
        }

        LocalTime end = start.plusMinutes(minutes);

        return start.isBefore(getEndTime()) && time.isBefore(end);
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
