package com.spendsmart.budget.repository;

import com.spendsmart.budget.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    // Get all budgets by user
    List<Budget> findByUserId(Long userId);

    // Get budget for specific month/year
    Optional<Budget> findByUserIdAndMonthAndYear(Long userId,
                                                 Integer month,
                                                 Integer year);
}