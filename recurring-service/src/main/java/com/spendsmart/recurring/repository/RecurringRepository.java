package com.spendsmart.recurring.repository;

import com.spendsmart.recurring.entity.Recurring;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RecurringRepository extends JpaRepository<Recurring, Long> {

    // Get all active recurring whose execution date is today
    List<Recurring> findByNextExecutionDateAndActive(LocalDate date, boolean active);
}