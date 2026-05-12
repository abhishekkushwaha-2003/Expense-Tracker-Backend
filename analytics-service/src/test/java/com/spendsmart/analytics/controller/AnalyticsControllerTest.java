package com.spendsmart.analytics.controller;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class AnalyticsControllerTest {

    private StubRestTemplate restTemplate;
    private AnalyticsController analyticsController;

    @BeforeEach
    void setUp() {
        restTemplate = new StubRestTemplate();
        analyticsController = new AnalyticsController();
        ReflectionTestUtils.setField(analyticsController, "restTemplate", restTemplate);
    }

    @Test
    void getTotalExpenseSumsExpenseAmounts() {
        restTemplate.response = List.of(
                Map.of("amount", 125.5),
                Map.of("amount", "74.5")
        );

        assertThat(analyticsController.getTotalExpense(10L)).isEqualTo(200.0);
        assertThat(restTemplate.url).isEqualTo("http://EXPENSE-SERVICE/expenses/user/10");
    }

    @Test
    void getTotalIncomeSumsIncomeAmounts() {
        restTemplate.response = List.of(
                Map.of("amount", 1000),
                Map.of("amount", "500.25")
        );

        assertThat(analyticsController.getTotalIncome(10L)).isEqualTo(1500.25);
        assertThat(restTemplate.url).isEqualTo("http://INCOME-SERVICE/income/user/10");
    }

    @Test
    void getSummaryReturnsIncomeExpenseAndBalance() {
        restTemplate.responses = Map.of(
                "http://INCOME-SERVICE/income/user/10", List.of(Map.of("amount", 1000.0)),
                "http://EXPENSE-SERVICE/expenses/user/10", List.of(Map.of("amount", 300.0))
        );

        Map<String, Object> summary = analyticsController.getSummary(10L);

        assertThat(summary).containsEntry("totalIncome", 1000.0);
        assertThat(summary).containsEntry("totalExpense", 300.0);
        assertThat(summary).containsEntry("balance", 700.0);
    }

    @Test
    void getTotalExpenseTreatsNullResponseAsEmptyList() {
        restTemplate.response = null;

        assertThat(analyticsController.getTotalExpense(10L)).isZero();
    }

    @Test
    void getTotalIncomeTreatsRestClientFailureAsEmptyList() {
        restTemplate.throwException = true;

        assertThat(analyticsController.getTotalIncome(10L)).isZero();
    }

    @Test
    void getTotalExpenseIgnoresMissingAndInvalidAmounts() {
        restTemplate.response = List.of(
                Map.of("title", "No amount"),
                Map.of("amount", "not-a-number"),
                Map.of("amount", 75.25)
        );

        assertThat(analyticsController.getTotalExpense(10L)).isEqualTo(75.25);
    }

    private static class StubRestTemplate extends RestTemplate {
        private Object response;
        private Map<String, Object> responses = Map.of();
        private String url;
        private boolean throwException;

        @Override
        public <T> T getForObject(String url, Class<T> responseType, Object... uriVariables) {
            this.url = url;
            if (throwException) {
                throw new RestClientException("Service unavailable");
            }
            Object resolved = responses.getOrDefault(url, response);
            return responseType.cast(resolved);
        }
    }
}
