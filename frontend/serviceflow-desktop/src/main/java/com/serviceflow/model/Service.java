package com.serviceflow.model;

public class Service {
    private String name;
    private String description;
    private String duration;
    private double price;
    private String status;

    public Service(String name, String description, String duration, double price, String status) {
        this.name = name;
        this.description = description;
        this.duration = duration;
        this.price = price;
        this.status = status;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDuration() {
        return this.duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public double getPrice() {
        return this.price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
