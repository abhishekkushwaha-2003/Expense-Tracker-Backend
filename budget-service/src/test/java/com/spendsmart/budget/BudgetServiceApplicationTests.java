package com.spendsmart.budget;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.spendsmart.budget.entity.Budget;
import com.spendsmart.budget.entity.BudgetPeriod;
import com.spendsmart.budget.messaging.NotificationPublisher;
import com.spendsmart.budget.repository.BudgetRepository;
import com.spendsmart.budget.service.impl.BudgetServiceImpl;

@ExtendWith(MockitoExtension.class)
class BudgetServiceApplicationTests {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private NotificationPublisher notificationPublisher;

    @InjectMocks
    private BudgetServiceImpl service;

    @Test
    void createBudgetUpdatesExistingMonthBudgetAndSyncsProfile() {
        ReflectionTestUtils.setField(service, "asyncNotificationEnabled", false);
        Budget existing = Budget.builder()
                .budgetId(10L)
                .userId(5L)
                .month(5)
                .year(2026)
                .monthlyLimit(1000.0)
                .spentAmount(200.0)
                .currency("INR")
                .build();
        Budget input = Budget.builder()
                .userId(5L)
                .month(5)
                .year(2026)
                .monthlyLimit(1800.0)
                .currency("usd")
                .alertThreshold(80)
                .name("Family")
                .period(BudgetPeriod.MONTHLY)
                .build();

        when(budgetRepository.findByUserIdAndMonthAndYearOrderByBudgetIdDesc(5L, 5, 2026)).thenReturn(List.of(existing));
        when(budgetRepository.save(any(Budget.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Budget saved = service.createBudget(input);

        assertEquals(10L, saved.getBudgetId());
        assertEquals(1800.0, saved.getMonthlyLimit());
        assertEquals(BigDecimal.valueOf(1800.0), saved.getLimitAmount());
        verify(restTemplate).put("http://AUTH-SERVICE/auth/users/{userId}/preferences", Map.of("monthlyBudget", 1800.0), 5L);
    }

    @Test
    void createBudgetSendsThresholdAlertWhenUsageCrossesLimit() {
        ReflectionTestUtils.setField(service, "asyncNotificationEnabled", true);
        ReflectionTestUtils.setField(service, "notificationPublisher", notificationPublisher);
        Budget budget = Budget.builder()
                .userId(9L)
                .month(5)
                .year(2026)
                .monthlyLimit(1000.0)
                .spentAmount(800.0)
                .alertThreshold(75)
                .currency("INR")
                .name("Travel")
                .thresholdAlertSent(false)
                .limitAlertSent(false)
                .build();

        when(budgetRepository.findByUserIdAndMonthAndYearOrderByBudgetIdDesc(9L, 5, 2026)).thenReturn(List.of());
        when(budgetRepository.save(any(Budget.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(restTemplate.getForObject("http://AUTH-SERVICE/auth/internal/users/{userId}", Map.class, 9L))
                .thenReturn(Map.of("email", "budget@example.com"));

        service.createBudget(budget);

        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(notificationPublisher).publishBudgetAlert(payloadCaptor.capture());
        assertEquals("budget@example.com", payloadCaptor.getValue().get("recipientEmail"));
        assertEquals(800.0, payloadCaptor.getValue().get("spentAmount"));
    }

    @Test
    void createBudgetSkipsAlertWhenLimitIsNotPositive() {
        ReflectionTestUtils.setField(service, "asyncNotificationEnabled", true);
        Budget budget = Budget.builder().userId(1L).month(5).year(2026).monthlyLimit(0.0).build();
        when(budgetRepository.findByUserIdAndMonthAndYearOrderByBudgetIdDesc(1L, 5, 2026)).thenReturn(List.of());
        when(budgetRepository.save(any(Budget.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.createBudget(budget);

        verify(notificationPublisher, never()).publishBudgetAlert(any());
    }
}

