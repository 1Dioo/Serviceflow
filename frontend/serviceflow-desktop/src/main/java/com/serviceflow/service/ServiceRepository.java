package com.serviceflow.service;
import com.serviceflow.model.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class ServiceRepository {
    private static final List<Service> DEFAULT_SERVICES = new ArrayList<>();

    private static final List<Service> SERVICES = new ArrayList<>();

    static {
        DEFAULT_SERVICES.add(new Service(
            "Formatação e instalação do sistema operacional",
            "Manutenção de computadores",
            "Formatação e instalação limpa do sistema operacional.",
            "2 horas", 120.00, "Ativo"
        ));

        DEFAULT_SERVICES.add(new Service(
            "Limpeza física de computador",
            "Manutenção de computadores",
            "Limpeza física e preventiva dos componentes.",
            "1 hora", 80.00, "Ativo"
        ));

        DEFAULT_SERVICES.add(new Service(
            "Upgrade de memória RAM ou SSD",
            "Manutenção de computadores",
            "Instalação e configuração de memória RAM ou SSD.",
            "1 hora", 60.00, "Ativo"
        ));

        DEFAULT_SERVICES.add(new Service(
            "Diagnóstico de hardware",
            "Manutenção de computadores",
            "Identificação de possíveis falhas nos componentes.",
            "45 minutos", 50.00, "Ativo"
        ));

        DEFAULT_SERVICES.add(new Service(
            "Instalação e configuração de programas",
            "Software e sistemas",
            "Instalação e configuração de softwares.",
            "45 minutos", 40.00, "Ativo"
        ));

        DEFAULT_SERVICES.add(new Service(
            "Remoção de vírus e softwares maliciosos",
            "Software e sistemas",
            "Verificação e remoção de softwares maliciosos.",
            "1 hora", 70.00, "Ativo"
        ));

        DEFAULT_SERVICES.add(new Service(
            "Configuração de impressora",
            "Software e sistemas",
            "Instalação e configuração de impressoras.",
            "45 minutos", 50.00, "Ativo"
        ));

        DEFAULT_SERVICES.add(new Service(
            "Backup e transferência de arquivos",
            "Software e sistemas",
            "Cópia e transferência de arquivos entre dispositivos.",
            "1 hora", 60.00, "Ativo"
        ));

        DEFAULT_SERVICES.add(new Service(
            "Configuração de roteador e Wi-Fi",
            "Redes e internet",
            "Configuração de roteador e conexão sem fio.",
            "1 hora", 70.00, "Ativo"
        ));

        DEFAULT_SERVICES.add(new Service(
            "Instalação de rede cabeada",
            "Redes e internet",
            "Instalação e configuração de rede cabeada.",
            "2 horas", 100.00, "Ativo"
        ));

        DEFAULT_SERVICES.add(new Service(
            "Compartilhamento de arquivos e impressoras",
            "Redes e internet",
            "Configuração de compartilhamento na rede local.",
            "1 hora", 60.00, "Ativo"
        ));

        DEFAULT_SERVICES.add(new Service(
            "Diagnóstico de lentidão",
            "Suporte técnico",
            "Análise de possíveis causas de lentidão.",
            "30 minutos", 40.00, "Ativo"
        ));

        DEFAULT_SERVICES.add(new Service(
            "Configuração inicial de computador",
            "Suporte técnico",
            "Preparação inicial do computador para uso.",
            "1 hora", 60.00, "Ativo"
        ));

        DEFAULT_SERVICES.add(new Service(
            "Suporte técnico remoto",
            "Suporte técnico",
            "Atendimento técnico remoto.",
            "45 minutos", 50.00, "Ativo"
        ));

        DEFAULT_SERVICES.add(new Service(
            "Instalação e configuração de periféricos",
            "Suporte técnico",
            "Instalação de mouse, teclado, webcam e periféricos.",
            "30 minutos", 35.00, "Ativo"
        ));
    }

    private ServiceRepository() {
    }

    // Serviços cadastrados pelo usuário
    public static List<Service> getAll() {
        return SERVICES;
    }

    // Serviços disponíveis para agendamento
    public static List<Service> getActive() {
        List<Service> available = new ArrayList<>(DEFAULT_SERVICES);
        available.addAll(SERVICES);

        return available.stream()
            .filter(service -> "Ativo".equalsIgnoreCase(service.getStatus()))
            .collect(Collectors.toList());
    }

    // Catálogo completo: serviços padrão + cadastrados pelo usuário
    public static List<Service> getCatalog() {
        List<Service> catalog = new ArrayList<>(DEFAULT_SERVICES);
        catalog.addAll(SERVICES);

        return catalog;
    }

    // Busca um serviço do catálogo pelo nome (sem diferenciar maiúsculas)
    public static Service findByName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }

        for (Service service : getCatalog()) {
            if (service.getName().equalsIgnoreCase(name.trim())) {
                return service;
            }
        }

        return null;
    }

    public static void add(Service service) {
        SERVICES.add(service);
    }

    public static void remove(Service service) {
        if (!SERVICES.remove(service)) {
            DEFAULT_SERVICES.remove(service);
        }
    }
}
