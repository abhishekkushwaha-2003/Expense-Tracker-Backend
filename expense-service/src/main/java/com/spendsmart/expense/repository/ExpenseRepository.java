package com.spendsmart.expense.repository;

import com.spendsmart.expense.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // Basic queries
    List<Expense> findByUserId(Long userId);

    List<Expense> findByCategoryId(Long categoryId);

    // Date filters
    List<Expense> findByUserIdAndDateBetween(Long userId,
                                             LocalDateTime start,
                                             LocalDateTime end);


}