package com.serviceflow.service;
import com.serviceflow.model.Employee;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class EmployeeRepository {

    private static final ObservableList<Employee> employees =
            FXCollections.observableArrayList();

    private EmployeeRepository() {
    }

    public static void add(Employee employee) {
        employees.add(employee);
    }

    public static ObservableList<Employee> findAll() {
        return employees;
    }

    public static void remove(Employee employee) {
        employees.remove(employee);
    }

    public static int count() {
        return employees.size();
    }

    public static int countActive() {
        return (int) employees.stream()
                .filter(employee -> employee.getStatus().equals("Ativo"))
                .count();
    }

    public static List<Employee> search(String text) {
        List<Employee> result = new ArrayList<>();

        for (Employee employee : employees) {
            if (employee.getName().toLowerCase().contains(text.toLowerCase())
                    || employee.getArea().toLowerCase().contains(text.toLowerCase())) {
                result.add(employee);
            }
        }

        return result;
    }
}