package com.serviceflow.controller;

import com.serviceflow.security.AccessPolicy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Navegação compartilhada entre a janela principal e o painel
 * administrativo, e regra de preço dos serviços.
 */
@DisplayName("Navegação do painel administrativo e regras de serviços")
class AdminNavigationTest {

    private final List<String> panelHistory = new ArrayList<>();
    private final Consumer<String> panel = panelHistory::add;

    @AfterEach
    void clear() {
        MainController.clearNavigator(panel);
    }

    @Test
    @DisplayName("telas de Serviços navegam dentro do painel registrado")
    void navigationGoesToRegisteredPanel() {
        MainController.registerNavigator(panel);

        // Mesmo caminho usado por ServicesController e NewServiceController.
        MainController.navigateTo(AccessPolicy.NEW_SERVICE_VIEW);
        MainController.navigateToServices();

        assertEquals(List.of(AccessPolicy.NEW_SERVICE_VIEW, AccessPolicy.SERVICES_VIEW), panelHistory);
    }

    @Test
    @DisplayName("após sair, o painel deixa de receber navegação")
    void clearedAfterLogout() {
        MainController.registerNavigator(panel);
        MainController.clearNavigator(panel);

        MainController.navigateToServices();

        assertTrue(panelHistory.isEmpty());
    }

    @Test
    @DisplayName("um navegador não remove o registro de outro")
    void clearOnlyOwnNavigator() {
        List<String> otherHistory = new ArrayList<>();
        Consumer<String> other = otherHistory::add;

        MainController.registerNavigator(panel);
        MainController.clearNavigator(other);
        MainController.navigateToServices();

        assertEquals(List.of(AccessPolicy.SERVICES_VIEW), panelHistory);
        assertTrue(otherHistory.isEmpty());
    }

    @Test
    @DisplayName("preço do serviço continua calculado pela duração (R$ 100,00 por hora)")
    void priceRuleUnchanged() {
        assertEquals(50.00, NewServiceController.calculatePrice("30 minutos"));
        assertEquals(75.00, NewServiceController.calculatePrice("45 minutos"));
        assertEquals(100.00, NewServiceController.calculatePrice("1 hora"));
        assertEquals(150.00, NewServiceController.calculatePrice("1 hora e 30 minutos"));
        assertEquals(200.00, NewServiceController.calculatePrice("2 horas"));
        assertEquals(400.00, NewServiceController.calculatePrice("4 horas"));
        assertThrows(IllegalArgumentException.class,
            () -> NewServiceController.calculatePrice("5 horas"));
    }
}
