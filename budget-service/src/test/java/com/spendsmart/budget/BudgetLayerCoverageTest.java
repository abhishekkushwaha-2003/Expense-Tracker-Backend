package com.spendsmart.budget;

import com.spendsmart.budget.config.RabbitMqConfig;
import com.spendsmart.budget.config.RestConfig;
import com.spendsmart.budget.controller.BudgetController;
import com.spendsmart.budget.entity.Budget;
import com.spendsmart.budget.entity.BudgetPeriod;
import com.spendsmart.budget.messaging.NotificationPublisher;
import com.spendsmart.budget.service.BudgetService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BudgetLayerCoverageTest {

    @Test
    void controllerDelegatesToService() {
        BudgetService service = mock(BudgetService.class);
        BudgetController controller = new BudgetController();
        ReflectionTestUtils.setField(controller, "budgetService", service);
        Budget budget = Budget.builder().budgetId(1L).userId(10L).month(5).year(2026).build();
        when(service.createBudget(budget)).thenReturn(budget);
        when(service.getBudgetById(1L)).thenReturn(budget);
        when(service.getBudgetsByUser(10L)).thenReturn(List.of(budget));
        when(service.getBudgetByMonth(10L, 5, 2026)).thenReturn(budget);
        when(service.updateBudget(1L, budget)).thenReturn(budget);

        assertSame(budget, controller.createBudget(budget));
        assertSame(budget, controller.getBudget(1L));
        assertEquals(List.of(budget), controller.getByUser(10L));
        assertSame(budget, controller.getByMonth(10L, 5, 2026));
        assertSame(budget, controller.updateBudget(1L, budget));
        assertEquals("Deleted successfully", controller.deleteBudget(1L));
        verify(service).deleteBudget(1L);
    }

    @Test
    void entityLifecycleAppliesDefaultsAndUpdatesTimestamp() {
        Budget budget = Budget.builder().monthlyLimit(1200.0).name(" ").build();
        budget.onCreate();
        assertNotNull(budget.getCreatedAt());
        assertTrue(budget.getIsActive());
        assertEquals(0.0, budget.getSpentAmount());
        assertEquals(50, budget.getAlertThreshold());
        assertEquals(0, budget.getCategoryId());
        assertEquals(BigDecimal.valueOf(1200.0), budget.getLimitAmount());
        assertEquals("Monthly Budget", budget.getName());
        assertEquals(BudgetPeriod.MONTHLY, budget.getPeriod());
        assertFalse(budget.getThresholdAlertSent());
        assertFalse(budget.getLimitAlertSent());
        budget.onUpdate();
        assertNotNull(budget.getUpdatedAt());
    }

    @Test
    void configAndPublisherBeansWork() {
        assertTrue(new RestConfig().restTemplate() instanceof RestTemplate);
        assertTrue(new RabbitMqConfig().jsonMessageConverter() instanceof Jackson2JsonMessageConverter);
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        NotificationPublisher publisher = new NotificationPublisher(rabbitTemplate, "exchange", "budget.alert");
        Map<String, Object> payload = Map.of("budgetId", 1L);
        publisher.publishBudgetAlert(payload);
        verify(rabbitTemplate).convertAndSend("exchange", "budget.alert", payload);
    }
}
