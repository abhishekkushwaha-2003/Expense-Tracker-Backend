package com.spendsmart.budget.repository;

import com.spendsmart.budget.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    // Get all budgets by user
    List<Budget> findByUserId(Long userId);

    // Get budgets for specific month/year. Ordered latest first to tolerate old duplicate rows.
    List<Budget> findByUserIdAndMonthAndYearOrderByBudgetIdDesc(Long userId,
                                                                Integer month,
                                                                Integer year);
}
