package com.spendsmart.budget.service.impl;

import com.spendsmart.budget.entity.Budget;
import com.spendsmart.budget.entity.BudgetPeriod;
import com.spendsmart.budget.messaging.NotificationPublisher;
import com.spendsmart.budget.repository.BudgetRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetServiceImplTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private NotificationPublisher notificationPublisher;

    @InjectMocks
    private BudgetServiceImpl budgetService;

    private Budget budget;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(budgetService, "asyncNotificationEnabled", false);
        budget = Budget.builder()
                .budgetId(1L)
                .userId(10L)
                .monthlyLimit(1000.0)
                .spentAmount(500.0)
                .currency("INR")
                .month(5)
                .year(2026)
                .alertThreshold(75)
                .categoryId(2)
                .name("Food")
                .period(BudgetPeriod.MONTHLY)
                .isActive(true)
                .thresholdAlertSent(false)
                .limitAlertSent(false)
                .build();
    }

    @Test
    void createBudgetCreatesNewBudgetAppliesLimitAmountAndSyncsProfile() {
        when(budgetRepository.findByUserIdAndMonthAndYearOrderByBudgetIdDesc(10L, 5, 2026)).thenReturn(List.of());
        when(budgetRepository.save(budget)).thenReturn(budget);

        Budget result = budgetService.createBudget(budget);

        assertThat(result.getLimitAmount()).isEqualByComparingTo(BigDecimal.valueOf(1000.0));
        verify(restTemplate).put(
                eq("http://AUTH-SERVICE/auth/internal/users/{userId}/preferences"),
                any(Map.class),
                eq(10L)
        );
        verify(restTemplate, never()).postForObject(any(String.class), any(), eq(Object.class));
    }

    @Test
    void createBudgetUpdatesExistingBudgetForSameMonth() {
        Budget existing = Budget.builder()
                .budgetId(9L)
                .userId(10L)
                .monthlyLimit(800.0)
                .spentAmount(200.0)
                .month(5)
                .year(2026)
                .isActive(false)
                .build();
        when(budgetRepository.findByUserIdAndMonthAndYearOrderByBudgetIdDesc(10L, 5, 2026)).thenReturn(List.of(existing));
        when(budgetRepository.save(existing)).thenReturn(existing);

        Budget result = budgetService.createBudget(budget);

        assertThat(result.getBudgetId()).isEqualTo(9L);
        assertThat(result.getMonthlyLimit()).isEqualTo(1000.0);
        assertThat(result.getSpentAmount()).isEqualTo(500.0);
        assertThat(result.getIsActive()).isTrue();
    }

    @Test
    void getBudgetByIdAndMonthReturnBudgetOrThrow() {
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budget));
        when(budgetRepository.findById(99L)).thenReturn(Optional.empty());
        when(budgetRepository.findByUserIdAndMonthAndYearOrderByBudgetIdDesc(10L, 5, 2026)).thenReturn(List.of(budget));
        when(budgetRepository.findByUserIdAndMonthAndYearOrderByBudgetIdDesc(10L, 6, 2026)).thenReturn(List.of());

        assertThat(budgetService.getBudgetById(1L)).isSameAs(budget);
        assertThat(budgetService.getBudgetByMonth(10L, 5, 2026)).isSameAs(budget);
        assertThatThrownBy(() -> budgetService.getBudgetById(99L)).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> budgetService.getBudgetByMonth(10L, 6, 2026)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void getBudgetsByUserDelegatesToRepository() {
        when(budgetRepository.findByUserId(10L)).thenReturn(List.of(budget));

        assertThat(budgetService.getBudgetsByUser(10L)).containsExactly(budget);
    }

    @Test
    void updateBudgetCopiesFieldsAndSendsThresholdAlert() {
        Budget update = Budget.builder()
                .userId(10L)
                .monthlyLimit(1000.0)
                .spentAmount(800.0)
                .currency("")
                .month(5)
                .year(2026)
                .alertThreshold(75)
                .categoryId(0)
                .name("Food")
                .period(BudgetPeriod.MONTHLY)
                .isActive(true)
                .build();
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budget));
        when(budgetRepository.save(budget)).thenReturn(budget);
        when(restTemplate.getForObject("http://AUTH-SERVICE/auth/internal/users/{userId}", Map.class, 10L))
                .thenReturn(Map.of("email", "user@example.com"));

        Budget result = budgetService.updateBudget(1L, update);

        assertThat(result.getThresholdAlertSent()).isTrue();
        assertThat(result.getLimitAlertSent()).isFalse();
        ArgumentCaptor<Map<String, Object>> payload = ArgumentCaptor.forClass(Map.class);
        verify(restTemplate).postForObject(
                eq("http://NOTIFICATION-SERVICE/notifications/budget-alert"),
                payload.capture(),
                eq(Object.class)
        );
        assertThat(payload.getValue()).containsEntry("recipientEmail", "user@example.com");
        assertThat(payload.getValue()).containsEntry("currency", "INR");
    }

    @Test
    void updateBudgetSendsLimitAlertWithAsyncPublisherWhenEnabled() {
        ReflectionTestUtils.setField(budgetService, "asyncNotificationEnabled", true);
        Budget update = Budget.builder()
                .userId(10L)
                .monthlyLimit(1000.0)
                .spentAmount(1000.0)
                .currency("INR")
                .month(5)
                .year(2026)
                .alertThreshold(75)
                .categoryId(2)
                .name("Food")
                .period(BudgetPeriod.MONTHLY)
                .isActive(true)
                .build();
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budget));
        when(budgetRepository.save(budget)).thenReturn(budget);
        when(restTemplate.getForObject("http://AUTH-SERVICE/auth/internal/users/{userId}", Map.class, 10L))
                .thenReturn(Map.of());

        Budget result = budgetService.updateBudget(1L, update);

        assertThat(result.getLimitAlertSent()).isTrue();
        verify(notificationPublisher).publishBudgetAlert(any(Map.class));
    }

    @Test
    void updateBudgetResetsAlertFlagsWhenUsageFallsBelowThreshold() {
        budget.setSpentAmount(900.0);
        budget.setThresholdAlertSent(true);
        budget.setLimitAlertSent(true);
        Budget update = Budget.builder()
                .userId(10L)
                .monthlyLimit(1000.0)
                .spentAmount(100.0)
                .currency("INR")
                .month(5)
                .year(2026)
                .alertThreshold(75)
                .isActive(true)
                .build();
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budget));
        when(budgetRepository.save(budget)).thenReturn(budget);

        Budget result = budgetService.updateBudget(1L, update);

        assertThat(result.getThresholdAlertSent()).isFalse();
        assertThat(result.getLimitAlertSent()).isFalse();
    }

    @Test
    void deleteBudgetDelegatesToRepository() {
        budgetService.deleteBudget(1L);

        verify(budgetRepository).deleteById(1L);
    }
}
