package com.spendsmart.expense.service.impl;

import com.spendsmart.expense.entity.Expense;
import com.spendsmart.expense.entity.ExpenseType;
import com.spendsmart.expense.entity.PaymentMethod;
import com.spendsmart.expense.repository.ExpenseRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceImplTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ExpenseServiceImpl expenseService;

    private Expense expense;
    private LocalDateTime mayDate;

    @BeforeEach
    void setUp() {
        mayDate = LocalDateTime.of(2026, 5, 3, 10, 0);
        expense = Expense.builder()
                .expenseId(1L)
                .userId(10L)
                .categoryId(2L)
                .title("Groceries")
                .amount(300.0)
                .currency("INR")
                .paymentMethod(PaymentMethod.CASH)
                .date(mayDate)
                .notes("weekly")
                .build();
    }

    @Test
    void addExpenseDefaultsTypeSavesAndSyncsBudgetUsage() {
        Expense otherMonth = Expense.builder().userId(10L).amount(1000.0).date(mayDate.minusMonths(1)).build();
        when(expenseRepository.save(expense)).thenReturn(expense);
        when(expenseRepository.findByUserId(10L)).thenReturn(List.of(expense, otherMonth));
        when(restTemplate.getForObject(
                "http://BUDGET-SERVICE/budgets/user/{userId}/{month}/{year}",
                Map.class,
                10L,
                5,
                2026
        )).thenReturn(Map.of(
                "budgetId", 7,
                "monthlyLimit", 5000.0,
                "currency", "INR",
                "month", 5,
                "year", 2026
        ));

        Expense result = expenseService.addExpense(expense);

        assertThat(result.getType()).isEqualTo(ExpenseType.EXPENSE);
        verify(expenseRepository).save(expense);
        verify(restTemplate).put(
                eq("http://BUDGET-SERVICE/budgets/{budgetId}"),
                any(Map.class),
                eq(7)
        );
    }

    @Test
    void addExpenseSkipsBudgetSyncWhenUserOrDateMissing() {
        expense.setUserId(null);
        when(expenseRepository.save(expense)).thenReturn(expense);

        expenseService.addExpense(expense);

        verify(expenseRepository, never()).findByUserId(any());
        verify(restTemplate, never()).getForObject(any(String.class), eq(Map.class), any(), any(), any());
    }

    @Test
    void getExpenseByIdReturnsExpenseWhenFound() {
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));

        assertThat(expenseService.getExpenseById(1L)).isSameAs(expense);
    }

    @Test
    void getExpenseByIdThrowsWhenMissing() {
        when(expenseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.getExpenseById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Expense not found");
    }

    @Test
    void getExpensesByUserDelegatesToRepository() {
        when(expenseRepository.findByUserId(10L)).thenReturn(List.of(expense));

        assertThat(expenseService.getExpensesByUser(10L)).containsExactly(expense);
    }

    @Test
    void updateExpenseCopiesFieldsDefaultsNullTypeAndIgnoresMissingBudget() {
        Expense update = Expense.builder()
                .title("Updated")
                .amount(450.0)
                .currency("USD")
                .categoryId(4L)
                .paymentMethod(PaymentMethod.CARD)
                .date(mayDate)
                .notes("new")
                .receiptUrl("receipt")
                .isRecurring(true)
                .build();
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));
        when(expenseRepository.save(expense)).thenReturn(expense);
        when(expenseRepository.findByUserId(10L)).thenReturn(List.of(expense));
        when(restTemplate.getForObject(any(String.class), eq(Map.class), eq(10L), eq(5), eq(2026)))
                .thenThrow(HttpClientErrorException.NotFound.class);

        Expense result = expenseService.updateExpense(1L, update);

        assertThat(result.getTitle()).isEqualTo("Updated");
        assertThat(result.getAmount()).isEqualTo(450.0);
        assertThat(result.getCurrency()).isEqualTo("USD");
        assertThat(result.getCategoryId()).isEqualTo(4L);
        assertThat(result.getType()).isEqualTo(ExpenseType.EXPENSE);
        assertThat(result.getPaymentMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(result.getReceiptUrl()).isEqualTo("receipt");
        assertThat(result.getIsRecurring()).isTrue();
    }

    @Test
    void deleteExpenseDeletesThenSyncsBudgetUsage() {
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));
        when(expenseRepository.findByUserId(10L)).thenReturn(List.of());
        when(restTemplate.getForObject(any(String.class), eq(Map.class), eq(10L), eq(5), eq(2026)))
                .thenReturn(null);

        expenseService.deleteExpense(1L);

        verify(expenseRepository).deleteById(1L);
    }
}
