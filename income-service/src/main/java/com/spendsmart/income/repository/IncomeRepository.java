package com.spendsmart.income.repository;

import com.spendsmart.income.entity.Income;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface IncomeRepository extends JpaRepository<Income, Long> {

    // Get all incomes by user
    List<Income> findByUserId(Long userId);

    // Date range filter
    List<Income> findByUserIdAndDateBetween(Long userId,
                                            LocalDateTime start,
                                            LocalDateTime end);

}