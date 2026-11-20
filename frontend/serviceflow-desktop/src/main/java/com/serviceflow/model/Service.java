package com.serviceflow.model;

public class Service {
    private String name;
    private String category;
    private String description;
    private String duration;
    private double price;
    private String status;

    public Service(String name, String description, String duration,
                   double price, String status) {
        this(name, "Outros", description, duration, price, status);
    }

    public Service(String name, String category, String description,
                   String duration, double price, String status) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.duration = duration;
        this.price = price;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
