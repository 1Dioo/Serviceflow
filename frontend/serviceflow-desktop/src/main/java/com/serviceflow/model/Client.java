package com.serviceflow.model;

import java.util.UUID;

public class Client {
    // Identificação única e imutável, usada para vincular agendamentos
    // (nomes podem se repetir ou ser alterados na edição).
    private final String id;

    private String name;
    private String phone;
    private String email;
    private String notes;

    public Client(String name, String phone, String email, String notes) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.notes = notes;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}