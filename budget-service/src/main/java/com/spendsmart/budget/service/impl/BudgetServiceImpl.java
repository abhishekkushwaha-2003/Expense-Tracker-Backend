package com.spendsmart.budget.service.impl;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.spendsmart.budget.entity.Budget;
import com.spendsmart.budget.repository.BudgetRepository;
import com.spendsmart.budget.service.BudgetService;

@Service
public class BudgetServiceImpl implements BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public Budget createBudget(Budget budget) {
        applyDerivedFields(budget);
        Budget savedBudget = budgetRepository.save(budget);
        notifyBudgetLimitReached(savedBudget);
        return savedBudget;
    }

    @Override
    public Budget getBudgetById(Long id) {
        return budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found"));
    }

    @Override
    public List<Budget> getBudgetsByUser(Long userId) {
        return budgetRepository.findByUserId(userId);
    }

    @Override
    public Budget getBudgetByMonth(Long userId, Integer month, Integer year) {
        return budgetRepository.findByUserIdAndMonthAndYear(userId, month, year)
                .orElseThrow(() -> new RuntimeException("Budget not found for this month"));
    }

    @Override
    public Budget updateBudget(Long id, Budget budget) {

        Budget existing = getBudgetById(id);

        existing.setUserId(budget.getUserId());
        existing.setMonthlyLimit(budget.getMonthlyLimit());
        existing.setSpentAmount(budget.getSpentAmount());
        existing.setCurrency(budget.getCurrency());
        existing.setMonth(budget.getMonth());
        existing.setYear(budget.getYear());
        applyDerivedFields(existing);
        Budget savedBudget = budgetRepository.save(existing);
        notifyBudgetLimitReached(savedBudget);
        return savedBudget;
    }

    @Override
    public void deleteBudget(Long id) {
        budgetRepository.deleteById(id);
    }

    private void applyDerivedFields(Budget budget) {
        if (budget.getMonthlyLimit() != null) {
            budget.setLimitAmount(BigDecimal.valueOf(budget.getMonthlyLimit()));
        }
    }

    private void notifyBudgetLimitReached(Budget budget) {
        double monthlyLimit = budget.getMonthlyLimit() == null ? 0.0 : budget.getMonthlyLimit();
        if (monthlyLimit <= 0) {
            return;
        }

        double spentAmount = budget.getSpentAmount() == null ? 0.0 : budget.getSpentAmount();
        double alertThreshold = budget.getAlertThreshold() == null ? 80.0 : budget.getAlertThreshold();
        if (spentAmount < monthlyLimit) {
            return;
        }

        try {
            String recipientEmail = fetchRecipientEmail(budget.getUserId());
            Map<String, Object> request = new LinkedHashMap<>();
            request.put("recipientId", budget.getUserId());
            request.put("recipientEmail", recipientEmail);
            request.put("budgetId", budget.getBudgetId());
            request.put("categoryId", budget.getCategoryId() == null || budget.getCategoryId() == 0 ? null : budget.getCategoryId());
            request.put("budgetName", budget.getName());
            request.put("spentAmount", spentAmount);
            request.put("limitAmount", monthlyLimit);
            request.put("alertThreshold", alertThreshold);
            request.put("currency", budget.getCurrency() == null || budget.getCurrency().isBlank() ? "INR" : budget.getCurrency());

            restTemplate.postForObject(
                    "http://NOTIFICATION-SERVICE/notifications/budget-alert",
                    request,
                    Object.class
            );
        } catch (Exception ex) {
            // Budget save should still succeed if notification delivery is unavailable.
        }
    }

    private String fetchRecipientEmail(Long userId) {
        if (userId == null) {
            return null;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> user = restTemplate.getForObject(
                "http://AUTH-SERVICE/auth/internal/users/{userId}",
                Map.class,
                userId
        );

        if (user == null) {
            return null;
        }

        Object email = user.get("email");
        return email == null ? null : email.toString();
    }
}
