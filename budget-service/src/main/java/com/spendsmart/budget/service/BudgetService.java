package com.spendsmart.budget.service;

import com.spendsmart.budget.entity.Budget;

import java.util.List;

public interface BudgetService {

    Budget createBudget(Budget budget);

    Budget getBudgetById(Long id);

    List<Budget> getBudgetsByUser(Long userId);

    Budget getBudgetByMonth(Long userId, Integer month, Integer year);

    Budget updateBudget(Long id, Budget budget);

    void deleteBudget(Long id);
}