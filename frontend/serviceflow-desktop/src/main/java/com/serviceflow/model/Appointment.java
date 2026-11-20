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
