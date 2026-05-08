package com.spendsmart.expense;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import com.spendsmart.expense.entity.Expense;
import com.spendsmart.expense.entity.ExpenseType;
import com.spendsmart.expense.repository.ExpenseRepository;
import com.spendsmart.expense.service.impl.ExpenseServiceImpl;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceApplicationTests {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ExpenseServiceImpl service;

    @Test
    void addExpenseDefaultsTypeAndSyncsBudgetUsage() {
        LocalDateTime date = LocalDateTime.of(2026, 5, 4, 10, 0);
        Expense expense = Expense.builder().userId(3L).amount(250.0).date(date).build();
        Expense savedExpense = Expense.builder().expenseId(11L).userId(3L).amount(250.0).date(date).type(ExpenseType.EXPENSE).build();

        when(expenseRepository.save(any(Expense.class))).thenReturn(savedExpense);
        when(expenseRepository.findByUserId(3L)).thenReturn(List.of(savedExpense));
        when(restTemplate.getForObject("http://BUDGET-SERVICE/budgets/user/{userId}/{month}/{year}", Map.class, 3L, 5, 2026))
                .thenReturn(Map.of("budgetId", 8L, "monthlyLimit", 1000.0, "currency", "INR", "month", 5, "year", 2026));

        Expense result = service.addExpense(expense);

        ArgumentCaptor<Expense> expenseCaptor = ArgumentCaptor.forClass(Expense.class);
        verify(expenseRepository).save(expenseCaptor.capture());
        verify(restTemplate).put(eq("http://BUDGET-SERVICE/budgets/{budgetId}"), any(Map.class), eq(8L));
        assertEquals(ExpenseType.EXPENSE, expenseCaptor.getValue().getType());
        assertEquals(savedExpense, result);
    }

    @Test
    void deleteExpenseRemovesEntityAndSyncsBudgetUsage() {
        LocalDateTime date = LocalDateTime.of(2026, 5, 1, 8, 0);
        Expense expense = Expense.builder().expenseId(7L).userId(2L).amount(40.0).date(date).build();

        when(expenseRepository.findById(7L)).thenReturn(Optional.of(expense));
        when(expenseRepository.findByUserId(2L)).thenReturn(List.of());
        when(restTemplate.getForObject("http://BUDGET-SERVICE/budgets/user/{userId}/{month}/{year}", Map.class, 2L, 5, 2026))
                .thenReturn(Map.of("budgetId", 4L, "monthlyLimit", 500.0, "currency", "INR", "month", 5, "year", 2026));

        service.deleteExpense(7L);

        verify(expenseRepository).deleteById(7L);
        verify(restTemplate).put(eq("http://BUDGET-SERVICE/budgets/{budgetId}"), any(Map.class), eq(4L));
    }
}
