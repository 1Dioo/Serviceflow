package com.serviceflow.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Employee {

    private final StringProperty name;
    private final StringProperty contact;
    private final StringProperty education;
    private final StringProperty area;
    private final StringProperty specialties;
    private final StringProperty experience;
    private final StringProperty status;

    public Employee(String name, String contact, String education,
            String area, String specialties, String experience, String status) {
        this.name = new SimpleStringProperty(name);
        this.contact = new SimpleStringProperty(contact);
        this.education = new SimpleStringProperty(education);
        this.area = new SimpleStringProperty(area);
        this.specialties = new SimpleStringProperty(specialties);
        this.experience = new SimpleStringProperty(experience);
        this.status = new SimpleStringProperty(status);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getContact() {
        return contact.get();
    }

    public StringProperty contactProperty() {
        return contact;
    }

    public String getEducation() {
        return education.get();
    }

    public StringProperty educationProperty() {
        return education;
    }

    public String getArea() {
        return area.get();
    }

    public StringProperty areaProperty() {
        return area;
    }

    public String getSpecialties() {
        return specialties.get();
    }

    public StringProperty specialtiesProperty() {
        return specialties;
    }

    public String getExperience() {
        return experience.get();
    }

    public StringProperty experienceProperty() {
        return experience;
    }

    public String getStatus() {
        return status.get();
    }

    public StringProperty statusProperty() {
        return status;
    }
}