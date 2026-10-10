package com.serviceflow.service;

import com.serviceflow.model.Service;
import java.util.ArrayList;
import java.util.List;

public class ServiceRepository {
    private static final List<Service> SERVICES = new ArrayList<>();

    static {
        SERVICES.add(new Service(
            "Manutenção de computador",
            "Diagnóstico e manutenção de hardware",
            "2 horas", 120.00, "Ativo"));

        SERVICES.add(new Service(
            "Formatação e instalação do sistema",
            "Formatação e configuração do sistema operacional",
            "2 hora", 100.00, "Ativo"));

        SERVICES.add(new Service(
            "Configuração de rede",
            "Configuração de rede local e dispositivos",
            "1 hora e 30 minutos", 90.00, "Ativo"));

        SERVICES.add(new Service(
            "Manutenção de notebook",
            "Diagnóstico e manutenção de notebook",
            "2 hora", 150.00, "Ativo"));

        SERVICES.add(new Service(
            "Instalação de software",
            "Instalação e configuração de softwares",
            "1 horas", 60.00, "Ativo"));

        SERVICES.add(new Service(
            "Suporte técnico",
            "Atendimento para problemas de software e hardware",
            "1 horas", 50.00, "Ativo"));
    }

    private  ServiceRepository(){
    }

    public static List<Service> getAll() {
        return SERVICES;
    }

    public static void add(Service service) {
        SERVICES.add(service);
    }

    public static void remove(Service service) {
        SERVICES.remove(service);
    }
}
