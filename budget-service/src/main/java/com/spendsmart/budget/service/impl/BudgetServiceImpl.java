package com.spendsmart.budget.service.impl;

import com.spendsmart.budget.entity.Budget;
import com.spendsmart.budget.repository.BudgetRepository;
import com.spendsmart.budget.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BudgetServiceImpl implements BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Override
    public Budget createBudget(Budget budget) {
        return budgetRepository.save(budget);
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

        existing.setMonthlyLimit(budget.getMonthlyLimit());
        existing.setCurrency(budget.getCurrency());
        existing.setMonth(budget.getMonth());
        existing.setYear(budget.getYear());

        return budgetRepository.save(existing);
    }

    @Override
    public void deleteBudget(Long id) {
        budgetRepository.deleteById(id);
    }
}