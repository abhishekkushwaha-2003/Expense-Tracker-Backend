package com.spendsmart.budget.service.impl;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.spendsmart.budget.messaging.NotificationPublisher;
import com.spendsmart.budget.entity.Budget;
import com.spendsmart.budget.repository.BudgetRepository;
import com.spendsmart.budget.service.BudgetService;

@Service
public class BudgetServiceImpl implements BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired(required = false)
    private NotificationPublisher notificationPublisher;

    @Value("${app.messaging.notification.async-enabled:false}")
    private boolean asyncNotificationEnabled;

    @Override
    public Budget createBudget(Budget budget) {
        Budget existing = findLatestBudgetForMonth(budget.getUserId(), budget.getMonth(), budget.getYear());
        if (existing != null) {
            copyBudgetFields(existing, budget);
            applyDerivedFields(existing);
            Budget savedBudget = budgetRepository.save(existing);
            syncUserMonthlyBudget(savedBudget);
            notifyBudgetAlerts(savedBudget);
            return savedBudget;
        }

        applyDerivedFields(budget);
        Budget savedBudget = budgetRepository.save(budget);
        syncUserMonthlyBudget(savedBudget);
        notifyBudgetAlerts(savedBudget);
        return savedBudget;
    }

    @Override
    public Budget getBudgetById(Long id) {
        return budgetRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Budget not found"));
    }

    @Override
    public List<Budget> getBudgetsByUser(Long userId) {
        return budgetRepository.findByUserId(userId);
    }

    @Override
    public Budget getBudgetByMonth(Long userId, Integer month, Integer year) {
        Budget budget = findLatestBudgetForMonth(userId, month, year);
        if (budget == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Budget not found for this month");
        }

        return budget;
    }

    @Override
    public Budget updateBudget(Long id, Budget budget) {

        Budget existing = getBudgetById(id);

        copyBudgetFields(existing, budget);
        applyDerivedFields(existing);
        Budget savedBudget = budgetRepository.save(existing);
        syncUserMonthlyBudget(savedBudget);
        notifyBudgetAlerts(savedBudget);
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

    private Budget findLatestBudgetForMonth(Long userId, Integer month, Integer year) {
        if (userId == null || month == null || year == null) {
            return null;
        }

        List<Budget> matches = budgetRepository.findByUserIdAndMonthAndYearOrderByBudgetIdDesc(userId, month, year);
        return matches.isEmpty() ? null : matches.get(0);
    }

    private void copyBudgetFields(Budget target, Budget source) {
        target.setUserId(source.getUserId());
        target.setMonthlyLimit(source.getMonthlyLimit());
        target.setSpentAmount(source.getSpentAmount() == null ? target.getSpentAmount() : source.getSpentAmount());
        target.setCurrency(source.getCurrency());
        target.setMonth(source.getMonth());
        target.setYear(source.getYear());
        target.setAlertThreshold(source.getAlertThreshold());
        target.setCategoryId(source.getCategoryId());
        target.setName(source.getName());
        target.setPeriod(source.getPeriod());
        if (source.getIsActive() != null) {
            target.setIsActive(source.getIsActive());
        }
    }

    private void notifyBudgetAlerts(Budget budget) {
        double monthlyLimit = budget.getMonthlyLimit() == null ? 0.0 : budget.getMonthlyLimit();
        if (monthlyLimit <= 0) {
            return;
        }

        double spentAmount = budget.getSpentAmount() == null ? 0.0 : budget.getSpentAmount();
        double alertThreshold = budget.getAlertThreshold() == null ? 50.0 : budget.getAlertThreshold();
        double usagePercent = (spentAmount / monthlyLimit) * 100.0;
        boolean thresholdReached = usagePercent >= alertThreshold && spentAmount < monthlyLimit;
        boolean limitReached = spentAmount >= monthlyLimit;

        boolean changed = false;
        if (!thresholdReached && Boolean.TRUE.equals(budget.getThresholdAlertSent())) {
            budget.setThresholdAlertSent(false);
            changed = true;
        }
        if (!limitReached && Boolean.TRUE.equals(budget.getLimitAlertSent())) {
            budget.setLimitAlertSent(false);
            changed = true;
        }
        if (changed) {
            budgetRepository.save(budget);
        }

        if (!thresholdReached && !limitReached) {
            return;
        }

        try {
            String recipientEmail = fetchRecipientEmail(budget.getUserId());
            if (thresholdReached && !Boolean.TRUE.equals(budget.getThresholdAlertSent())) {
                sendBudgetAlert(buildBudgetAlertRequest(budget, recipientEmail, spentAmount, monthlyLimit, alertThreshold));
                budget.setThresholdAlertSent(true);
                changed = true;
            }

            if (limitReached && !Boolean.TRUE.equals(budget.getLimitAlertSent())) {
                sendBudgetAlert(buildBudgetAlertRequest(budget, recipientEmail, spentAmount, monthlyLimit, alertThreshold));
                budget.setLimitAlertSent(true);
                changed = true;
            }
        } catch (Exception ex) {
            // Budget save should still succeed if notification delivery is unavailable.
        }

        if (changed) {
            budgetRepository.save(budget);
        }
    }

    private Map<String, Object> buildBudgetAlertRequest(Budget budget,
                                                        String recipientEmail,
                                                        double spentAmount,
                                                        double monthlyLimit,
                                                        double alertThreshold) {
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
        return request;
    }

    private void sendBudgetAlert(Map<String, Object> payload) {
        if (asyncNotificationEnabled && notificationPublisher != null) {
            notificationPublisher.publishBudgetAlert(payload);
            return;
        }

        restTemplate.postForObject(
                "http://NOTIFICATION-SERVICE/notifications/budget-alert",
                payload,
                Object.class
        );
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

    private void syncUserMonthlyBudget(Budget budget) {
        if (budget.getUserId() == null) {
            return;
        }

        try {
            Map<String, Object> request = new LinkedHashMap<>();
            request.put("monthlyBudget", budget.getMonthlyLimit());
            restTemplate.put(
                    "http://AUTH-SERVICE/auth/internal/users/{userId}/preferences",
                    request,
                    budget.getUserId()
            );
        } catch (Exception ex) {
            // Budget save should still succeed if profile sync is unavailable.
        }
    }
}
