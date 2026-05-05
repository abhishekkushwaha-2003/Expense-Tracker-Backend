package com.spendsmart.expense.service.impl;

import com.spendsmart.expense.entity.Expense;
import com.spendsmart.expense.entity.ExpenseType;
import com.spendsmart.expense.repository.ExpenseRepository;
import com.spendsmart.expense.service.ExpenseService;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class ExpenseServiceImpl implements ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public Expense addExpense(Expense expense) {
        if (expense.getType() == null) {
            expense.setType(ExpenseType.EXPENSE);
        }
        Expense savedExpense = expenseRepository.save(expense);
        syncBudgetUsage(savedExpense.getUserId(), savedExpense.getDate());
        return savedExpense;
    }

    @Override
    public Expense getExpenseById(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));
    }

    @Override
    public List<Expense> getExpensesByUser(Long userId) {
        return expenseRepository.findByUserId(userId);
    }

    @Override
    public Expense updateExpense(Long id, Expense expense) {

        Expense existing = getExpenseById(id);

        existing.setTitle(expense.getTitle());
        existing.setAmount(expense.getAmount());
        existing.setCurrency(expense.getCurrency());
        existing.setCategoryId(expense.getCategoryId());
        existing.setType(expense.getType() == null ? ExpenseType.EXPENSE : expense.getType());
        existing.setPaymentMethod(expense.getPaymentMethod());
        existing.setDate(expense.getDate());
        existing.setNotes(expense.getNotes());
        existing.setReceiptUrl(expense.getReceiptUrl());
        existing.setIsRecurring(expense.getIsRecurring());
        Expense savedExpense = expenseRepository.save(existing);
        syncBudgetUsage(savedExpense.getUserId(), savedExpense.getDate());
        return savedExpense;
    }

    @Override
    public void deleteExpense(Long id) {
        Expense existing = getExpenseById(id);
        expenseRepository.deleteById(id);
        syncBudgetUsage(existing.getUserId(), existing.getDate());
    }

    private void syncBudgetUsage(Long userId, LocalDateTime expenseDate) {
        if (userId == null || expenseDate == null) {
            return;
        }

        int month = expenseDate.getMonthValue();
        int year = expenseDate.getYear();
        double spentAmount = expenseRepository.findByUserId(userId).stream()
                .filter(expense -> expense.getDate() != null
                        && expense.getDate().getMonthValue() == month
                        && expense.getDate().getYear() == year)
                .mapToDouble(expense -> expense.getAmount() == null ? 0.0 : expense.getAmount())
                .sum();

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> budget = restTemplate.getForObject(
                    "http://BUDGET-SERVICE/budgets/user/{userId}/{month}/{year}",
                    Map.class,
                    userId,
                    month,
                    year
            );

            if (budget == null) {
                return;
            }

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("userId", userId);
            payload.put("monthlyLimit", budget.get("monthlyLimit"));
            payload.put("spentAmount", spentAmount);
            payload.put("currency", budget.get("currency"));
            payload.put("month", budget.get("month"));
            payload.put("year", budget.get("year"));
            payload.put("isActive", true);

            restTemplate.put(
                    "http://BUDGET-SERVICE/budgets/{budgetId}",
                    payload,
                    budget.get("budgetId")
            );
        } catch (HttpClientErrorException.NotFound ex) {
            // No budget exists for this month yet, so there is nothing to sync.
        } catch (Exception ex) {
            // Expense write should still succeed if budget sync is unavailable.
        }
    }
}
